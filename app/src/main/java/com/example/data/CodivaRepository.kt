package com.example.data

import android.content.Context
import com.example.R
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class CodivaRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    private fun actorEmail(): String {
        return auth.currentUser?.email?.trim()?.take(120)?.ifBlank { "member@codiva.com" }
            ?: "member@codiva.com"
    }

    suspend fun ensureUserProfileAndWorkspace(
        displayName: String?,
        email: String?,
        onboarding: AccountOnboardingInput? = null
    ): Result<Unit> {
        var docPath = "users/unknown"
        return try {
            val uid = requireUserId()
            val docRef = db.collection("users").document(uid)
            docPath = docRef.path
            val snapshot = docRef.get().await()
            val safeEmail = email?.trim()?.take(120)?.ifBlank { "member@codiva.com" } ?: "member@codiva.com"
            val isFounder = UserRoleManager.isFounderEmail(safeEmail)

            val portalRef = db.collection("public_portal").document("config")
            val portalSnapshot = try {
                portalRef.get().await()
            } catch (_: Exception) {
                null
            }
            val portalConfig = portalSnapshot?.toObject(
                PublicPortalConfig::class.java,
                DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
            )

            if (isFounder && (portalSnapshot == null || !portalSnapshot.exists())) {
                val defaults = PublicPortalConfig()
                portalRef.set(
                    mapOf(
                        "portalTitle" to defaults.portalTitle,
                        "publicUrl" to defaults.publicUrl,
                        "customDomain" to defaults.customDomain,
                        "welcomeMessage" to defaults.welcomeMessage,
                        "inviteCode" to defaults.inviteCode,
                        "portalPublished" to defaults.portalPublished,
                        "allowPublicSignUp" to defaults.allowPublicSignUp,
                        "requireAdminApproval" to defaults.requireAdminApproval,
                        "updatedBy" to safeEmail,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } else if (isFounder && portalConfig?.customDomain == "codiva.tech") {
                portalRef.update(
                    mapOf(
                        "customDomain" to CodivaCatalog.OFFICIAL_DOMAIN,
                        "updatedBy" to safeEmail,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }

            if (!snapshot.exists()) {
                val chosenName = onboarding?.displayNameOverride?.trim()?.take(80)
                    ?.ifBlank { displayName?.trim()?.take(80) }
                    ?: displayName?.trim()?.take(80)
                val safeName = chosenName?.ifBlank { "CODIVA Member" } ?: "CODIVA Member"
                val requestedRole = onboarding?.requestedRole ?: AccountRole.MEMBER
                val effectiveRole = when {
                    isFounder -> AccountRole.SUPER_ADMIN
                    requestedRole in UserRoleManager.selfSignupRoles() -> requestedRole
                    else -> AccountRole.MEMBER
                }
                val hasValidInvite = !onboarding?.inviteCode.isNullOrBlank() &&
                    onboarding!!.inviteCode.trim().equals(
                        portalConfig?.inviteCode ?: "CODIVA-2026",
                        ignoreCase = true
                    )
                val status = UserRoleManager.resolveSignupStatus(
                    email = safeEmail,
                    requestedRole = effectiveRole,
                    requireAdminApproval = portalConfig?.requireAdminApproval ?: false,
                    hasValidInviteCode = hasValidInvite
                )
                val payload = mutableMapOf<String, Any>(
                    "userId" to uid,
                    "displayName" to safeName,
                    "email" to safeEmail,
                    "accountRole" to effectiveRole.name,
                    "membershipStatus" to status.name,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                val cleanOrg = onboarding?.organization?.trim()?.take(100).orEmpty()
                if (cleanOrg.isNotEmpty()) {
                    payload["organization"] = cleanOrg
                }
                docRef.set(payload).await()
                appendAuditLog(
                    uid,
                    "ACCOUNT_REGISTERED",
                    "$safeName (${effectiveRole.label} • ${status.name})",
                    AuditSeverity.INFO
                )
            }

            // Ensure default system settings document exists
            val settingsRef = docRef.collection("system_config").document("settings")
            if (!settingsRef.get().await().exists()) {
                settingsRef.set(
                    mapOf(
                        "userId" to uid,
                        "autoApproveMembers" to false,
                        "maintenanceMode" to false,
                        "paymentsEnabled" to true,
                        "auditLoggingEnabled" to true,
                        "holidayNoticeEnabled" to true,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }

            // Seed starter workspace records once if empty
            val existingRecords = docRef.collection("workspace_records").limit(1).get().await()
            if (existingRecords.isEmpty) {
                val batch = db.batch()
                CodivaCatalog.initialWorkspaceRecords.forEach { seed ->
                    val recRef = docRef.collection("workspace_records").document(seed.docId)
                    batch.set(
                        recRef,
                        mapOf(
                            "userId" to uid,
                            "moduleType" to seed.moduleType.name,
                            "title" to seed.title,
                            "subtitle" to seed.subtitle,
                            "detail" to seed.detail,
                            "category" to seed.category,
                            "status" to seed.status,
                            "amountCents" to seed.amountCents,
                            "createdAt" to FieldValue.serverTimestamp(),
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    )
                }
                val logRef = docRef.collection("audit_logs").document("seed_boot")
                batch.set(
                    logRef,
                    mapOf(
                        "userId" to uid,
                        "actorEmail" to safeEmail,
                        "action" to "WORKSPACE_INITIALIZED",
                        "target" to "CODIVA Ecosystem Control Panel",
                        "severity" to AuditSeverity.INFO.name,
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                )
                batch.commit().await()
            }

            // Seed shared Admin Publications & App Store Releases once when Founder signs in
            if (isFounder) {
                val pubsEmpty = db.collection("publications").limit(1).get().await().isEmpty
                if (pubsEmpty) {
                    val pubBatch = db.batch()
                    CodivaCatalog.initialPublications.forEach { seed ->
                        val pRef = db.collection("publications").document(seed.docId)
                        pubBatch.set(
                            pRef,
                            mapOf(
                                "title" to seed.title,
                                "summary" to seed.summary,
                                "content" to seed.content,
                                "category" to seed.category.name,
                                "targetRole" to seed.targetRole,
                                "documentUrl" to seed.documentUrl,
                                "documentLabel" to seed.documentLabel,
                                "expiresAtLabel" to seed.expiresAtLabel,
                                "status" to seed.status.name,
                                "publishedBy" to safeEmail,
                                "createdBy" to uid,
                                "createdAt" to FieldValue.serverTimestamp(),
                                "updatedAt" to FieldValue.serverTimestamp()
                            )
                        )
                    }
                    pubBatch.commit().await()
                }

                val relEmpty = db.collection("app_releases").limit(1).get().await().isEmpty
                if (relEmpty) {
                    val relBatch = db.batch()
                    CodivaCatalog.initialAppReleases.forEach { seed ->
                        val rRef = db.collection("app_releases").document(seed.docId)
                        relBatch.set(
                            rRef,
                            mapOf(
                                "platform" to seed.platform.name,
                                "versionName" to seed.versionName,
                                "buildNumber" to seed.buildNumber,
                                "releaseNotes" to seed.releaseNotes,
                                "downloadUrl" to seed.downloadUrl,
                                "status" to seed.status.name,
                                "updatedBy" to safeEmail,
                                "createdAt" to FieldValue.serverTimestamp(),
                                "updatedAt" to FieldValue.serverTimestamp()
                            )
                        )
                    }
                    relBatch.commit().await()
                }
            }

            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docPath)
            Result.failure(e)
        }
    }

    fun observeUserProfile(userId: String): Flow<UserProfile?> = flow {
        val uid = requireUserId()
        check(uid == userId) { "Authenticated user mismatch." }
        val path = "users/$uid"
        emitAll(
            db.collection("users").document(uid)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObject(UserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(exceptionToLog, OperationType.GET, path)
                    }
                    throw error
                }
        )
    }

    suspend fun updateUserProfile(
        displayName: String,
        accountRole: AccountRole,
        organization: String,
        phone: String
    ): Result<Unit> {
        var docPath = "users/${auth.currentUser?.uid ?: "unauthenticated"}"
        return try {
            val uid = requireUserId()
            val docRef = db.collection("users").document(uid)
            docPath = docRef.path
            val currentSnapshot = docRef.get().await()
            val currentRole = currentSnapshot.toObject(UserProfile::class.java)?.roleEnum
            val effectiveRole = UserRoleManager.resolveEffectiveRole(accountRole, actorEmail(), currentRole)
            val cleanName = displayName.trim()
            require(cleanName.length in 1..80) { "Display name must be 1 to 80 characters." }
            val cleanOrg = organization.trim().take(100)
            val cleanPhone = phone.trim().take(30)
            docRef.update(
                mapOf(
                    "displayName" to cleanName,
                    "accountRole" to effectiveRole.name,
                    "organization" to cleanOrg,
                    "phone" to cleanPhone,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            appendAuditLog(uid, "ROLE_PROFILE_UPDATED", "Switched role to ${effectiveRole.label}", AuditSeverity.INFO)
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun switchActiveRole(accountRole: AccountRole): Result<Unit> {
        var docPath = "users/${auth.currentUser?.uid ?: "unauthenticated"}"
        return try {
            val uid = requireUserId()
            val docRef = db.collection("users").document(uid)
            docPath = docRef.path
            val currentSnapshot = docRef.get().await()
            val currentRole = currentSnapshot.toObject(UserProfile::class.java)?.roleEnum
            val effectiveRole = UserRoleManager.resolveEffectiveRole(accountRole, actorEmail(), currentRole)
            docRef.update(
                mapOf(
                    "accountRole" to effectiveRole.name,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            appendAuditLog(uid, "DASHBOARD_ROLE_SWITCH", effectiveRole.label, AuditSeverity.INFO)
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }

    fun observeInquiries(userId: String): Flow<List<ProjectInquiry>> = flow {
        val uid = requireUserId()
        check(uid == userId) { "Authenticated user mismatch." }
        val path = "users/$uid/inquiries"
        emitAll(
            db.collection("users").document(uid).collection("inquiries")
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(ProjectInquiry::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    suspend fun submitInquiry(
        serviceCategory: String,
        projectTitle: String,
        details: String,
        budgetTier: String
    ): Result<Unit> {
        var docPath = "users/${auth.currentUser?.uid ?: "unauthenticated"}/inquiries"
        return try {
            val uid = requireUserId()
            val cleanCategory = serviceCategory.trim()
            val cleanTitle = projectTitle.trim()
            val cleanDetails = details.trim()
            val cleanBudget = budgetTier.trim()
            require(cleanCategory.length in 2..80) { "Select a service category." }
            require(cleanTitle.length in 3..100) { "Project title must be 3 to 100 characters." }
            require(cleanDetails.length in 10..1000) { "Please describe your project (10–1000 characters)." }
            require(cleanBudget.length in 2..40) { "Select an estimated timeline or budget." }

            val docRef = db.collection("users").document(uid).collection("inquiries").document()
            docPath = docRef.path
            docRef.set(
                mapOf(
                    "userId" to uid,
                    "serviceCategory" to cleanCategory,
                    "projectTitle" to cleanTitle,
                    "details" to cleanDetails,
                    "budgetTier" to cleanBudget,
                    "status" to InquiryStatus.SUBMITTED.name,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            appendAuditLog(uid, "INQUIRY_CREATED", cleanTitle, AuditSeverity.INFO)
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun cancelInquiry(inquiryId: String): Result<Unit> {
        var docPath = "users/${auth.currentUser?.uid ?: "unauthenticated"}/inquiries/$inquiryId"
        return try {
            val uid = requireUserId()
            require(inquiryId.isNotBlank()) { "Inquiry ID must not be blank." }
            val docRef = db.collection("users").document(uid).collection("inquiries").document(inquiryId)
            docPath = docRef.path
            docRef.update(
                mapOf(
                    "status" to InquiryStatus.CANCELLED.name,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            appendAuditLog(uid, "INQUIRY_CANCELLED", inquiryId, AuditSeverity.WARNING)
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun deleteInquiry(inquiryId: String): Result<Unit> {
        var docPath = "users/${auth.currentUser?.uid ?: "unauthenticated"}/inquiries/$inquiryId"
        return try {
            val uid = requireUserId()
            require(inquiryId.isNotBlank()) { "Inquiry ID must not be blank." }
            val docRef = db.collection("users").document(uid).collection("inquiries").document(inquiryId)
            docPath = docRef.path
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docPath)
            Result.failure(e)
        }
    }

    fun observeEnrollments(userId: String): Flow<List<CourseEnrollment>> = flow {
        val uid = requireUserId()
        check(uid == userId) { "Authenticated user mismatch." }
        val path = "users/$uid/enrollments"
        emitAll(
            db.collection("users").document(uid).collection("enrollments")
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(CourseEnrollment::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    suspend fun enrollInCourse(program: AcademyProgram): Result<Unit> {
        var docPath = "users/${auth.currentUser?.uid ?: "unauthenticated"}/enrollments/${program.id}"
        return try {
            val uid = requireUserId()
            val docRef = db.collection("users").document(uid).collection("enrollments").document(program.id)
            docPath = docRef.path
            if (docRef.get().await().exists()) {
                return Result.success(Unit)
            }
            docRef.set(
                mapOf(
                    "userId" to uid,
                    "courseId" to program.id,
                    "courseTitle" to program.title,
                    "programType" to program.programType,
                    "progressPercent" to 0L,
                    "status" to EnrollmentStatus.ENROLLED.name,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            appendAuditLog(uid, "COURSE_ENROLLED", program.title, AuditSeverity.INFO)
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun advanceEnrollmentProgress(enrollmentId: String, currentProgress: Long): Result<Unit> {
        var docPath = "users/${auth.currentUser?.uid ?: "unauthenticated"}/enrollments/$enrollmentId"
        return try {
            val uid = requireUserId()
            require(enrollmentId.isNotBlank()) { "Enrollment ID must not be blank." }
            val nextProgress = (currentProgress + 25L).coerceAtMost(100L)
            val nextStatus = if (nextProgress >= 100L) EnrollmentStatus.COMPLETED.name else EnrollmentStatus.ENROLLED.name
            val docRef = db.collection("users").document(uid).collection("enrollments").document(enrollmentId)
            docPath = docRef.path
            docRef.update(
                mapOf(
                    "progressPercent" to nextProgress,
                    "status" to nextStatus,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }

    fun observeApplications(userId: String): Flow<List<CareerApplication>> = flow {
        val uid = requireUserId()
        check(uid == userId) { "Authenticated user mismatch." }
        val path = "users/$uid/applications"
        emitAll(
            db.collection("users").document(uid).collection("applications")
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(CareerApplication::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    suspend fun submitApplication(
        role: CareerRole,
        applicantName: String,
        portfolioNote: String
    ): Result<Unit> {
        var docPath = "users/${auth.currentUser?.uid ?: "unauthenticated"}/applications/${role.id}"
        return try {
            val uid = requireUserId()
            val cleanName = applicantName.trim()
            val cleanNote = portfolioNote.trim()
            require(cleanName.length in 2..80) { "Full name must be 2 to 80 characters." }
            require(cleanNote.length in 5..600) { "Application note must be 5 to 600 characters." }

            val docRef = db.collection("users").document(uid).collection("applications").document(role.id)
            docPath = docRef.path
            docRef.set(
                mapOf(
                    "userId" to uid,
                    "roleId" to role.id,
                    "roleTitle" to role.title,
                    "applicantName" to cleanName,
                    "portfolioNote" to cleanNote,
                    "status" to ApplicationStatus.SUBMITTED.name,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            appendAuditLog(uid, "CAREER_APPLIED", role.title, AuditSeverity.INFO)
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun withdrawApplication(applicationId: String): Result<Unit> {
        var docPath = "users/${auth.currentUser?.uid ?: "unauthenticated"}/applications/$applicationId"
        return try {
            val uid = requireUserId()
            require(applicationId.isNotBlank()) { "Application ID must not be blank." }
            val docRef = db.collection("users").document(uid).collection("applications").document(applicationId)
            docPath = docRef.path
            docRef.update(
                mapOf(
                    "status" to ApplicationStatus.WITHDRAWN.name,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            appendAuditLog(uid, "CAREER_WITHDRAWN", applicationId, AuditSeverity.WARNING)
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }

    fun observeWorkspaceRecords(userId: String): Flow<List<WorkspaceRecord>> = flow {
        val uid = requireUserId()
        check(uid == userId) { "Authenticated user mismatch." }
        val path = "users/$uid/workspace_records"
        emitAll(
            db.collection("users").document(uid).collection("workspace_records")
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(WorkspaceRecord::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    suspend fun addWorkspaceRecord(
        moduleType: WorkspaceModuleType,
        title: String,
        subtitle: String,
        detail: String,
        category: String,
        status: String,
        amountCents: Long = 0L
    ): Result<Unit> {
        var docPath = "users/${auth.currentUser?.uid ?: "unauthenticated"}/workspace_records"
        return try {
            val uid = requireUserId()
            val cleanTitle = title.trim().take(120)
            val cleanSubtitle = subtitle.trim().take(160)
            val cleanDetail = detail.trim().take(600)
            val cleanCategory = category.trim().take(60).ifBlank { "General" }
            val cleanStatus = status.trim().take(40).ifBlank { "ACTIVE" }
            val safeAmount = amountCents.coerceIn(0L, 100_000_000L)
            require(cleanTitle.length >= 2) { "Title must be at least 2 characters." }

            val docRef = db.collection("users").document(uid).collection("workspace_records").document()
            docPath = docRef.path
            docRef.set(
                mapOf(
                    "userId" to uid,
                    "moduleType" to moduleType.name,
                    "title" to cleanTitle,
                    "subtitle" to cleanSubtitle,
                    "detail" to cleanDetail,
                    "category" to cleanCategory,
                    "status" to cleanStatus,
                    "amountCents" to safeAmount,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            appendAuditLog(uid, "ADMIN_${moduleType.name}_ADDED", cleanTitle, AuditSeverity.INFO)
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun updateWorkspaceRecordStatus(
        recordId: String,
        newStatus: String,
        titleForAudit: String
    ): Result<Unit> {
        var docPath = "users/${auth.currentUser?.uid ?: "unauthenticated"}/workspace_records/$recordId"
        return try {
            val uid = requireUserId()
            require(recordId.isNotBlank()) { "Record ID must not be blank." }
            val cleanStatus = newStatus.trim().take(40).ifBlank { "ACTIVE" }
            val docRef = db.collection("users").document(uid).collection("workspace_records").document(recordId)
            docPath = docRef.path
            docRef.update(
                mapOf(
                    "status" to cleanStatus,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            appendAuditLog(uid, "STATUS_UPDATED_$cleanStatus", titleForAudit, AuditSeverity.INFO)
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun deleteWorkspaceRecord(recordId: String, titleForAudit: String): Result<Unit> {
        var docPath = "users/${auth.currentUser?.uid ?: "unauthenticated"}/workspace_records/$recordId"
        return try {
            val uid = requireUserId()
            require(recordId.isNotBlank()) { "Record ID must not be blank." }
            val docRef = db.collection("users").document(uid).collection("workspace_records").document(recordId)
            docPath = docRef.path
            docRef.delete().await()
            appendAuditLog(uid, "RECORD_DELETED", titleForAudit, AuditSeverity.WARNING)
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docPath)
            Result.failure(e)
        }
    }

    fun observeAuditLogs(userId: String): Flow<List<AuditLogEntry>> = flow {
        val uid = requireUserId()
        check(uid == userId) { "Authenticated user mismatch." }
        val path = "users/$uid/audit_logs"
        emitAll(
            db.collection("users").document(uid).collection("audit_logs")
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(AuditLogEntry::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    fun observeSystemSettings(userId: String): Flow<SystemSettingsDoc?> = flow {
        val uid = requireUserId()
        check(uid == userId) { "Authenticated user mismatch." }
        val path = "users/$uid/system_config/settings"
        emitAll(
            db.collection("users").document(uid).collection("system_config").document("settings")
                .snapshots()
                .map { snapshot ->
                    snapshot.toObject(SystemSettingsDoc::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(exceptionToLog, OperationType.GET, path)
                    }
                    throw error
                }
        )
    }

    suspend fun updateSystemSettings(settings: SystemSettingsDoc): Result<Unit> {
        var docPath = "users/${auth.currentUser?.uid ?: "unauthenticated"}/system_config/settings"
        return try {
            val uid = requireUserId()
            val docRef = db.collection("users").document(uid).collection("system_config").document("settings")
            docPath = docRef.path
            docRef.set(
                mapOf(
                    "userId" to uid,
                    "autoApproveMembers" to settings.autoApproveMembers,
                    "maintenanceMode" to settings.maintenanceMode,
                    "paymentsEnabled" to settings.paymentsEnabled,
                    "auditLoggingEnabled" to settings.auditLoggingEnabled,
                    "holidayNoticeEnabled" to settings.holidayNoticeEnabled,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            appendAuditLog(
                uid,
                "SYSTEM_SETTINGS_UPDATED",
                "Maintenance=${settings.maintenanceMode}, Payments=${settings.paymentsEnabled}",
                if (settings.maintenanceMode) AuditSeverity.CRITICAL else AuditSeverity.INFO
            )
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }

    fun observePublicPortalConfig(): Flow<PublicPortalConfig?> = flow {
        val uid = requireUserId()
        val path = "public_portal/config"
        emitAll(
            db.collection("public_portal").document("config")
                .snapshots()
                .map { snapshot ->
                    snapshot.toObject(PublicPortalConfig::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(exceptionToLog, OperationType.GET, path)
                    }
                    throw error
                }
        )
    }

    suspend fun updatePublicPortalConfig(config: PublicPortalConfig): Result<Unit> {
        val path = "public_portal/config"
        return try {
            val uid = requireUserId()
            val email = actorEmail()
            require(UserRoleManager.isFounderEmail(email)) {
                "Only the Super Administrator can update public hosting settings."
            }
            val cleanTitle = config.portalTitle.trim().take(80)
            val cleanUrl = config.publicUrl.trim().take(300)
            val cleanDomain = config.customDomain.trim().take(120)
            val cleanWelcome = config.welcomeMessage.trim().take(400)
            val cleanCode = config.inviteCode.trim().take(40)
            require(cleanTitle.length >= 2) { "Portal title must be at least 2 characters." }
            require(cleanUrl.length >= 8) { "Enter a valid public URL." }
            require(cleanWelcome.length >= 5) { "Welcome message must be at least 5 characters." }
            require(cleanCode.length >= 3) { "Invite code must be at least 3 characters." }

            db.collection("public_portal").document("config")
                .set(
                    mapOf(
                        "portalTitle" to cleanTitle,
                        "publicUrl" to cleanUrl,
                        "customDomain" to cleanDomain,
                        "welcomeMessage" to cleanWelcome,
                        "inviteCode" to cleanCode,
                        "portalPublished" to config.portalPublished,
                        "allowPublicSignUp" to config.allowPublicSignUp,
                        "requireAdminApproval" to config.requireAdminApproval,
                        "updatedBy" to email,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            appendAuditLog(
                uid,
                "PUBLIC_PORTAL_UPDATED",
                "Published=${config.portalPublished}, SignUp=${config.allowPublicSignUp}",
                if (config.portalPublished) AuditSeverity.INFO else AuditSeverity.WARNING
            )
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    fun observeRegisteredUsers(): Flow<List<UserProfile>> = flow {
        val uid = requireUserId()
        if (!UserRoleManager.isFounderEmail(auth.currentUser?.email)) {
            emit(emptyList())
            return@flow
        }
        val path = "users"
        emitAll(
            db.collection("users")
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(UserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    suspend fun adminUpdateRegisteredUser(
        targetUserId: String,
        newRole: AccountRole,
        newStatus: MembershipStatus,
        displayNameForAudit: String
    ): Result<Unit> {
        var docPath = "users/$targetUserId"
        return try {
            val uid = requireUserId()
            require(UserRoleManager.isFounderEmail(actorEmail())) {
                "Only the Super Administrator can manage registered user accounts."
            }
            require(targetUserId.isNotBlank()) { "Target user ID must not be blank." }
            val docRef = db.collection("users").document(targetUserId)
            docPath = docRef.path
            docRef.update(
                mapOf(
                    "accountRole" to newRole.name,
                    "membershipStatus" to newStatus.name,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            appendAuditLog(
                uid,
                "REGISTERED_USER_UPDATED",
                "$displayNameForAudit -> ${newRole.label} (${newStatus.name})",
                if (newStatus == MembershipStatus.SUSPENDED) AuditSeverity.WARNING else AuditSeverity.INFO
            )
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }

    fun observePublications(): Flow<List<AdminPublication>> = flow {
        val uid = requireUserId()
        val isFounder = UserRoleManager.isFounderEmail(auth.currentUser?.email)
        val path = "publications"
        val query = if (isFounder) {
            db.collection(path)
        } else {
            db.collection(path).whereEqualTo("status", PublicationStatus.PUBLISHED.name)
        }
        emitAll(
            query.snapshots()
                .map { snapshot ->
                    snapshot.toObjects(AdminPublication::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    suspend fun savePublication(
        existingId: String?,
        title: String,
        summary: String,
        content: String,
        category: PublicationCategory,
        targetRole: String,
        documentUrl: String,
        documentLabel: String,
        expiresAtLabel: String,
        status: PublicationStatus
    ): Result<Unit> {
        var docPath = "publications/${existingId ?: "new"}"
        return try {
            val uid = requireUserId()
            val email = actorEmail()
            require(UserRoleManager.isFounderEmail(email)) {
                "Only the Super Administrator can publish or edit CODIVA bulletins."
            }
            val cleanTitle = title.trim().take(120)
            val cleanSummary = summary.trim().take(240)
            val cleanContent = content.trim().take(2000)
            val cleanDocUrl = documentUrl.trim().take(400)
            val cleanDocLabel = documentLabel.trim().take(80)
            val cleanExpiry = expiresAtLabel.trim().take(60)
            val allowedTargets = setOf("ALL", "USER", "MEMBER", "STAFF", "LECTURER", "PARENT", "HOD", "FINANCE", "PARTNER")
            val safeTarget = targetRole.trim().uppercase().takeIf { it in allowedTargets } ?: "ALL"
            require(cleanTitle.length >= 2) { "Title must be at least 2 characters." }
            require(cleanSummary.length >= 2) { "Summary must be at least 2 characters." }
            require(cleanContent.length >= 5) { "Content must be at least 5 characters." }

            if (existingId.isNullOrBlank()) {
                val ref = db.collection("publications").document()
                docPath = ref.path
                ref.set(
                    mapOf(
                        "title" to cleanTitle,
                        "summary" to cleanSummary,
                        "content" to cleanContent,
                        "category" to category.name,
                        "targetRole" to safeTarget,
                        "documentUrl" to cleanDocUrl,
                        "documentLabel" to cleanDocLabel,
                        "expiresAtLabel" to cleanExpiry,
                        "status" to status.name,
                        "publishedBy" to email,
                        "createdBy" to uid,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
                appendAuditLog(uid, "PUBLICATION_CREATED", "$cleanTitle (${status.label})", AuditSeverity.INFO)
            } else {
                val ref = db.collection("publications").document(existingId)
                docPath = ref.path
                ref.update(
                    mapOf(
                        "title" to cleanTitle,
                        "summary" to cleanSummary,
                        "content" to cleanContent,
                        "category" to category.name,
                        "targetRole" to safeTarget,
                        "documentUrl" to cleanDocUrl,
                        "documentLabel" to cleanDocLabel,
                        "expiresAtLabel" to cleanExpiry,
                        "status" to status.name,
                        "publishedBy" to email,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
                appendAuditLog(uid, "PUBLICATION_UPDATED", "$cleanTitle (${status.label})", AuditSeverity.INFO)
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docPath)
            Result.failure(e)
        }
    }

    suspend fun updatePublicationStatus(
        pubId: String,
        newStatus: PublicationStatus,
        titleForAudit: String
    ): Result<Unit> {
        var docPath = "publications/$pubId"
        return try {
            val uid = requireUserId()
            require(UserRoleManager.isFounderEmail(actorEmail())) {
                "Only the Super Administrator can update publication status."
            }
            require(pubId.isNotBlank()) { "Publication ID must not be blank." }
            val ref = db.collection("publications").document(pubId)
            docPath = ref.path
            ref.update(
                mapOf(
                    "status" to newStatus.name,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            appendAuditLog(
                uid,
                "PUBLICATION_${newStatus.name}",
                titleForAudit,
                if (newStatus == PublicationStatus.EXPIRED) AuditSeverity.WARNING else AuditSeverity.INFO
            )
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun deletePublication(pubId: String, titleForAudit: String): Result<Unit> {
        var docPath = "publications/$pubId"
        return try {
            val uid = requireUserId()
            require(UserRoleManager.isFounderEmail(actorEmail())) {
                "Only the Super Administrator can delete publications."
            }
            require(pubId.isNotBlank()) { "Publication ID must not be blank." }
            val ref = db.collection("publications").document(pubId)
            docPath = ref.path
            ref.delete().await()
            appendAuditLog(uid, "PUBLICATION_DELETED", titleForAudit, AuditSeverity.WARNING)
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docPath)
            Result.failure(e)
        }
    }

    fun observeAppReleases(): Flow<List<AppRelease>> = flow {
        val uid = requireUserId()
        val isFounder = UserRoleManager.isFounderEmail(auth.currentUser?.email)
        val path = "app_releases"
        val query = if (isFounder) {
            db.collection(path)
        } else {
            db.collection(path).whereEqualTo("status", ReleaseStatus.LIVE.name)
        }
        emitAll(
            query.snapshots()
                .map { snapshot ->
                    snapshot.toObjects(AppRelease::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    suspend fun saveAppRelease(
        existingId: String?,
        platform: ReleasePlatform,
        versionName: String,
        buildNumber: Long,
        releaseNotes: String,
        downloadUrl: String,
        status: ReleaseStatus
    ): Result<Unit> {
        var docPath = "app_releases/${existingId ?: "new"}"
        return try {
            val uid = requireUserId()
            val email = actorEmail()
            require(UserRoleManager.isFounderEmail(email)) {
                "Only the Super Administrator can manage CODIVA app releases."
            }
            val cleanVersion = versionName.trim().take(40)
            val safeBuild = buildNumber.coerceIn(1L, 1_000_000L)
            val cleanNotes = releaseNotes.trim().take(600)
            val cleanUrl = downloadUrl.trim().take(400)
            require(cleanVersion.isNotEmpty()) { "Version name is required." }
            require(cleanNotes.length >= 3) { "Release notes must be at least 3 characters." }
            require(cleanUrl.length >= 8) { "Enter a valid store or download URL." }

            if (existingId.isNullOrBlank()) {
                val ref = db.collection("app_releases").document()
                docPath = ref.path
                ref.set(
                    mapOf(
                        "platform" to platform.name,
                        "versionName" to cleanVersion,
                        "buildNumber" to safeBuild,
                        "releaseNotes" to cleanNotes,
                        "downloadUrl" to cleanUrl,
                        "status" to status.name,
                        "updatedBy" to email,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
                appendAuditLog(uid, "APP_RELEASE_CREATED", "${platform.label} v$cleanVersion", AuditSeverity.INFO)
            } else {
                val ref = db.collection("app_releases").document(existingId)
                docPath = ref.path
                ref.update(
                    mapOf(
                        "platform" to platform.name,
                        "versionName" to cleanVersion,
                        "buildNumber" to safeBuild,
                        "releaseNotes" to cleanNotes,
                        "downloadUrl" to cleanUrl,
                        "status" to status.name,
                        "updatedBy" to email,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
                appendAuditLog(uid, "APP_RELEASE_UPDATED", "${platform.label} v$cleanVersion (${status.label})", AuditSeverity.INFO)
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docPath)
            Result.failure(e)
        }
    }

    suspend fun updateAppReleaseStatus(
        releaseId: String,
        newStatus: ReleaseStatus,
        labelForAudit: String
    ): Result<Unit> {
        var docPath = "app_releases/$releaseId"
        return try {
            val uid = requireUserId()
            require(UserRoleManager.isFounderEmail(actorEmail())) {
                "Only the Super Administrator can update release status."
            }
            require(releaseId.isNotBlank()) { "Release ID must not be blank." }
            val ref = db.collection("app_releases").document(releaseId)
            docPath = ref.path
            ref.update(
                mapOf(
                    "status" to newStatus.name,
                    "updatedBy" to actorEmail(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            appendAuditLog(uid, "APP_RELEASE_${newStatus.name}", labelForAudit, AuditSeverity.INFO)
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun deleteAppRelease(releaseId: String, labelForAudit: String): Result<Unit> {
        var docPath = "app_releases/$releaseId"
        return try {
            val uid = requireUserId()
            require(UserRoleManager.isFounderEmail(actorEmail())) {
                "Only the Super Administrator can delete app releases."
            }
            require(releaseId.isNotBlank()) { "Release ID must not be blank." }
            val ref = db.collection("app_releases").document(releaseId)
            docPath = ref.path
            ref.delete().await()
            appendAuditLog(uid, "APP_RELEASE_DELETED", labelForAudit, AuditSeverity.WARNING)
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docPath)
            Result.failure(e)
        }
    }

    private suspend fun appendAuditLog(
        uid: String,
        action: String,
        target: String,
        severity: AuditSeverity
    ) {
        try {
            val logRef = db.collection("users").document(uid).collection("audit_logs").document()
            logRef.set(
                mapOf(
                    "userId" to uid,
                    "actorEmail" to actorEmail(),
                    "action" to action.trim().take(80).let { if (it.length < 2) "SYSTEM_EVENT" else it },
                    "target" to target.trim().take(120).let { if (it.length < 2) "System" else it },
                    "severity" to severity.name,
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, "users/$uid/audit_logs")
        }
    }
}

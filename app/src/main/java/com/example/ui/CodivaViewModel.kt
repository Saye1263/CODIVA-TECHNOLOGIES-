package com.example.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AcademyProgram
import com.example.data.AccountOnboardingInput
import com.example.data.AccountRole
import com.example.data.AdminPublication
import com.example.data.AppRelease
import com.example.data.AuditLogEntry
import com.example.data.CareerApplication
import com.example.data.CareerRole
import com.example.data.CodivaRepository
import com.example.data.CourseEnrollment
import com.example.data.MembershipStatus
import com.example.data.ProjectInquiry
import com.example.data.PublicPortalConfig
import com.example.data.PublicationCategory
import com.example.data.PublicationStatus
import com.example.data.ReleasePlatform
import com.example.data.ReleaseStatus
import com.example.data.SystemSettingsDoc
import com.example.data.UserProfile
import com.example.data.WorkspaceModuleType
import com.example.data.WorkspaceRecord
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

class CodivaViewModel(
    private val repository: CodivaRepository,
    private val currentUserId: String,
    private val initialDisplayName: String?,
    private val initialEmail: String?,
    private val initialOnboarding: AccountOnboardingInput? = null
) : ViewModel() {

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    private val _feedbackMessage = MutableStateFlow<String?>(null)
    val feedbackMessage: StateFlow<String?> = _feedbackMessage.asStateFlow()

    val profileState: StateFlow<UiState<UserProfile?>> =
        repository.observeUserProfile(currentUserId)
            .toUiState("profile")

    val inquiriesState: StateFlow<UiState<List<ProjectInquiry>>> =
        repository.observeInquiries(currentUserId)
            .map { list -> list.sortedByDescending { it.createdAt?.seconds ?: 0L } }
            .toUiState("inquiries")

    val enrollmentsState: StateFlow<UiState<List<CourseEnrollment>>> =
        repository.observeEnrollments(currentUserId)
            .map { list -> list.sortedByDescending { it.createdAt?.seconds ?: 0L } }
            .toUiState("enrollments")

    val applicationsState: StateFlow<UiState<List<CareerApplication>>> =
        repository.observeApplications(currentUserId)
            .map { list -> list.sortedByDescending { it.createdAt?.seconds ?: 0L } }
            .toUiState("applications")

    val workspaceRecordsState: StateFlow<UiState<List<WorkspaceRecord>>> =
        repository.observeWorkspaceRecords(currentUserId)
            .map { list -> list.sortedByDescending { it.createdAt?.seconds ?: 0L } }
            .toUiState("workspace_records")

    val auditLogsState: StateFlow<UiState<List<AuditLogEntry>>> =
        repository.observeAuditLogs(currentUserId)
            .map { list -> list.sortedByDescending { it.createdAt?.seconds ?: 0L } }
            .toUiState("audit_logs")

    val settingsState: StateFlow<UiState<SystemSettingsDoc?>> =
        repository.observeSystemSettings(currentUserId)
            .toUiState("system_settings")

    val publicPortalState: StateFlow<UiState<PublicPortalConfig?>> =
        repository.observePublicPortalConfig()
            .toUiState("public_portal")

    val registeredUsersState: StateFlow<UiState<List<UserProfile>>> =
        repository.observeRegisteredUsers()
            .map { list -> list.sortedByDescending { it.createdAt?.seconds ?: 0L } }
            .toUiState("registered_users")

    val publicationsState: StateFlow<UiState<List<AdminPublication>>> =
        repository.observePublications()
            .map { list -> list.sortedByDescending { it.updatedAt?.seconds ?: 0L } }
            .toUiState("publications")

    val appReleasesState: StateFlow<UiState<List<AppRelease>>> =
        repository.observeAppReleases()
            .map { list -> list.sortedBy { it.platform } }
            .toUiState("app_releases")

    init {
        viewModelScope.launch {
            repository.ensureUserProfileAndWorkspace(initialDisplayName, initialEmail, initialOnboarding)
                .onFailure { error ->
                    Log.w(TAG, "Profile & workspace bootstrap failed", error)
                }
        }
    }

    private fun <T> Flow<T>.toUiState(label: String): StateFlow<UiState<T>> =
        map<T, UiState<T>> { UiState.Success(it) }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val rootError = firestoreError ?: error
                Log.w(TAG, "Error observing $label", rootError)
                emit(UiState.Error(rootError.localizedMessage ?: "Failed to load $label"))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                initialValue = UiState.Loading
            )

    fun clearFeedback() {
        _feedbackMessage.value = null
    }

    fun updateProfile(
        displayName: String,
        accountRole: AccountRole,
        organization: String,
        phone: String,
        onComplete: () -> Unit = {}
    ) {
        viewModelScope.launch {
            _isSubmitting.value = true
            repository.updateUserProfile(displayName, accountRole, organization, phone)
                .onSuccess {
                    _feedbackMessage.value = "Profile updated (${accountRole.label})."
                    onComplete()
                }
                .onFailure { error ->
                    _feedbackMessage.value = error.localizedMessage ?: "Could not update profile."
                }
            _isSubmitting.value = false
        }
    }

    fun switchActiveRole(accountRole: AccountRole) {
        viewModelScope.launch {
            _isSubmitting.value = true
            repository.switchActiveRole(accountRole)
                .onSuccess {
                    _feedbackMessage.value = "Switched active dashboard to ${accountRole.label}."
                }
                .onFailure { error ->
                    _feedbackMessage.value = error.localizedMessage ?: "Could not switch role."
                }
            _isSubmitting.value = false
        }
    }

    fun submitInquiry(
        serviceCategory: String,
        projectTitle: String,
        details: String,
        budgetTier: String,
        onComplete: () -> Unit = {}
    ) {
        viewModelScope.launch {
            _isSubmitting.value = true
            repository.submitInquiry(serviceCategory, projectTitle, details, budgetTier)
                .onSuccess {
                    _feedbackMessage.value = "Project inquiry submitted."
                    onComplete()
                }
                .onFailure { error ->
                    _feedbackMessage.value = error.localizedMessage ?: "Could not submit inquiry."
                }
            _isSubmitting.value = false
        }
    }

    fun cancelInquiry(inquiryId: String) {
        viewModelScope.launch {
            _isSubmitting.value = true
            repository.cancelInquiry(inquiryId)
                .onSuccess { _feedbackMessage.value = "Inquiry cancelled." }
                .onFailure { error ->
                    _feedbackMessage.value = error.localizedMessage ?: "Could not cancel inquiry."
                }
            _isSubmitting.value = false
        }
    }

    fun deleteInquiry(inquiryId: String) {
        viewModelScope.launch {
            _isSubmitting.value = true
            repository.deleteInquiry(inquiryId)
                .onSuccess { _feedbackMessage.value = "Inquiry removed." }
                .onFailure { error ->
                    _feedbackMessage.value = error.localizedMessage ?: "Could not remove inquiry."
                }
            _isSubmitting.value = false
        }
    }

    fun enrollInCourse(program: AcademyProgram) {
        viewModelScope.launch {
            _isSubmitting.value = true
            repository.enrollInCourse(program)
                .onSuccess { _feedbackMessage.value = "Enrolled in ${program.title}." }
                .onFailure { error ->
                    _feedbackMessage.value = error.localizedMessage ?: "Enrollment failed."
                }
            _isSubmitting.value = false
        }
    }

    fun advanceEnrollmentProgress(enrollment: CourseEnrollment) {
        viewModelScope.launch {
            _isSubmitting.value = true
            repository.advanceEnrollmentProgress(enrollment.id, enrollment.progressPercent)
                .onSuccess {
                    val next = (enrollment.progressPercent + 25L).coerceAtMost(100L)
                    _feedbackMessage.value = if (next >= 100L) {
                        "Congratulations! ${enrollment.courseTitle} completed."
                    } else {
                        "Progress updated to $next%."
                    }
                }
                .onFailure { error ->
                    _feedbackMessage.value = error.localizedMessage ?: "Could not update progress."
                }
            _isSubmitting.value = false
        }
    }

    fun submitApplication(
        role: CareerRole,
        applicantName: String,
        portfolioNote: String,
        onComplete: () -> Unit = {}
    ) {
        viewModelScope.launch {
            _isSubmitting.value = true
            repository.submitApplication(role, applicantName, portfolioNote)
                .onSuccess {
                    _feedbackMessage.value = "Application submitted for ${role.title}."
                    onComplete()
                }
                .onFailure { error ->
                    _feedbackMessage.value = error.localizedMessage ?: "Could not submit application."
                }
            _isSubmitting.value = false
        }
    }

    fun withdrawApplication(applicationId: String) {
        viewModelScope.launch {
            _isSubmitting.value = true
            repository.withdrawApplication(applicationId)
                .onSuccess { _feedbackMessage.value = "Application withdrawn." }
                .onFailure { error ->
                    _feedbackMessage.value = error.localizedMessage ?: "Could not withdraw application."
                }
            _isSubmitting.value = false
        }
    }

    fun addWorkspaceRecord(
        moduleType: WorkspaceModuleType,
        title: String,
        subtitle: String,
        detail: String,
        category: String,
        status: String,
        amountCents: Long = 0L,
        onComplete: () -> Unit = {}
    ) {
        viewModelScope.launch {
            _isSubmitting.value = true
            repository.addWorkspaceRecord(moduleType, title, subtitle, detail, category, status, amountCents)
                .onSuccess {
                    _feedbackMessage.value = "${moduleType.label}: $title saved."
                    onComplete()
                }
                .onFailure { error ->
                    _feedbackMessage.value = error.localizedMessage ?: "Could not save record."
                }
            _isSubmitting.value = false
        }
    }

    fun updateWorkspaceRecordStatus(record: WorkspaceRecord, newStatus: String) {
        viewModelScope.launch {
            _isSubmitting.value = true
            repository.updateWorkspaceRecordStatus(record.id, newStatus, record.title)
                .onSuccess {
                    _feedbackMessage.value = "${record.title} marked $newStatus."
                }
                .onFailure { error ->
                    _feedbackMessage.value = error.localizedMessage ?: "Could not update status."
                }
            _isSubmitting.value = false
        }
    }

    fun deleteWorkspaceRecord(record: WorkspaceRecord) {
        viewModelScope.launch {
            _isSubmitting.value = true
            repository.deleteWorkspaceRecord(record.id, record.title)
                .onSuccess { _feedbackMessage.value = "${record.title} deleted." }
                .onFailure { error ->
                    _feedbackMessage.value = error.localizedMessage ?: "Could not delete record."
                }
            _isSubmitting.value = false
        }
    }

    fun updateSystemSettings(settings: SystemSettingsDoc) {
        viewModelScope.launch {
            _isSubmitting.value = true
            repository.updateSystemSettings(settings)
                .onSuccess { _feedbackMessage.value = "System settings saved & logged." }
                .onFailure { error ->
                    _feedbackMessage.value = error.localizedMessage ?: "Could not update settings."
                }
            _isSubmitting.value = false
        }
    }

    fun updatePublicPortalConfig(config: PublicPortalConfig, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            _isSubmitting.value = true
            repository.updatePublicPortalConfig(config)
                .onSuccess {
                    _feedbackMessage.value = if (config.portalPublished) {
                        "CODIVA Portal published online."
                    } else {
                        "CODIVA Portal set to private mode."
                    }
                    onComplete()
                }
                .onFailure { error ->
                    _feedbackMessage.value = error.localizedMessage ?: "Could not update hosting config."
                }
            _isSubmitting.value = false
        }
    }

    fun adminUpdateRegisteredUser(
        user: UserProfile,
        newRole: AccountRole,
        newStatus: MembershipStatus
    ) {
        viewModelScope.launch {
            _isSubmitting.value = true
            repository.adminUpdateRegisteredUser(user.id, newRole, newStatus, user.displayName)
                .onSuccess {
                    _feedbackMessage.value = "${user.displayName}: ${newRole.label} (${newStatus.label})."
                }
                .onFailure { error ->
                    _feedbackMessage.value = error.localizedMessage ?: "Could not update user account."
                }
            _isSubmitting.value = false
        }
    }

    fun savePublication(
        existingId: String?,
        title: String,
        summary: String,
        content: String,
        category: PublicationCategory,
        targetRole: String,
        documentUrl: String,
        documentLabel: String,
        expiresAtLabel: String,
        status: PublicationStatus,
        onComplete: () -> Unit = {}
    ) {
        viewModelScope.launch {
            _isSubmitting.value = true
            repository.savePublication(
                existingId = existingId,
                title = title,
                summary = summary,
                content = content,
                category = category,
                targetRole = targetRole,
                documentUrl = documentUrl,
                documentLabel = documentLabel,
                expiresAtLabel = expiresAtLabel,
                status = status
            ).onSuccess {
                _feedbackMessage.value = if (existingId.isNullOrBlank()) {
                    "Publication \"$title\" posted."
                } else {
                    "Publication \"$title\" updated."
                }
                onComplete()
            }.onFailure { error ->
                _feedbackMessage.value = error.localizedMessage ?: "Could not save publication."
            }
            _isSubmitting.value = false
        }
    }

    fun updatePublicationStatus(pub: AdminPublication, newStatus: PublicationStatus) {
        viewModelScope.launch {
            _isSubmitting.value = true
            repository.updatePublicationStatus(pub.id, newStatus, pub.title)
                .onSuccess {
                    _feedbackMessage.value = "${pub.title} marked ${newStatus.label}."
                }
                .onFailure { error ->
                    _feedbackMessage.value = error.localizedMessage ?: "Could not update publication status."
                }
            _isSubmitting.value = false
        }
    }

    fun deletePublication(pub: AdminPublication) {
        viewModelScope.launch {
            _isSubmitting.value = true
            repository.deletePublication(pub.id, pub.title)
                .onSuccess {
                    _feedbackMessage.value = "${pub.title} deleted."
                }
                .onFailure { error ->
                    _feedbackMessage.value = error.localizedMessage ?: "Could not delete publication."
                }
            _isSubmitting.value = false
        }
    }

    fun saveAppRelease(
        existingId: String?,
        platform: ReleasePlatform,
        versionName: String,
        buildNumber: Long,
        releaseNotes: String,
        downloadUrl: String,
        status: ReleaseStatus,
        onComplete: () -> Unit = {}
    ) {
        viewModelScope.launch {
            _isSubmitting.value = true
            repository.saveAppRelease(
                existingId = existingId,
                platform = platform,
                versionName = versionName,
                buildNumber = buildNumber,
                releaseNotes = releaseNotes,
                downloadUrl = downloadUrl,
                status = status
            ).onSuccess {
                _feedbackMessage.value = "${platform.label} v$versionName saved."
                onComplete()
            }.onFailure { error ->
                _feedbackMessage.value = error.localizedMessage ?: "Could not save app release."
            }
            _isSubmitting.value = false
        }
    }

    fun updateAppReleaseStatus(release: AppRelease, newStatus: ReleaseStatus) {
        viewModelScope.launch {
            _isSubmitting.value = true
            repository.updateAppReleaseStatus(release.id, newStatus, "${release.platformEnum.label} v${release.versionName}")
                .onSuccess {
                    _feedbackMessage.value = "${release.platformEnum.label} marked ${newStatus.label}."
                }
                .onFailure { error ->
                    _feedbackMessage.value = error.localizedMessage ?: "Could not update release status."
                }
            _isSubmitting.value = false
        }
    }

    fun deleteAppRelease(release: AppRelease) {
        viewModelScope.launch {
            _isSubmitting.value = true
            repository.deleteAppRelease(release.id, "${release.platformEnum.label} v${release.versionName}")
                .onSuccess {
                    _feedbackMessage.value = "${release.platformEnum.label} release deleted."
                }
                .onFailure { error ->
                    _feedbackMessage.value = error.localizedMessage ?: "Could not delete app release."
                }
            _isSubmitting.value = false
        }
    }

    fun showFeedback(message: String) {
        _feedbackMessage.value = message
    }

    private companion object {
        const val TAG = "CodivaViewModel"
        const val STOP_TIMEOUT_MILLIS = 5000L
    }
}

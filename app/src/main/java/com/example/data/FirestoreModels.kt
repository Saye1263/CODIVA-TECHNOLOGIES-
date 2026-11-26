package com.example.data

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

enum class AccountRole(val label: String, val tagline: String) {
    USER("User", "Personal courses, certificates & inquiries"),
    MEMBER("Member", "Community membership, projects & news"),
    STAFF("Staff", "Operations tasks, attendance & delivery"),
    LECTURER("Lecturer", "Courses, students, grading & lessons"),
    PARENT("Parent", "Student progress, fees & faculty messages"),
    HOD("HOD", "Department staff, approvals & reports"),
    FINANCE("Finance", "Payments, invoices & financial balance"),
    PARTNER("Partner", "Strategic partnerships, joint projects & resources"),
    SUPER_ADMIN("Super Admin", "Founder & CEO full ecosystem control")
}

enum class MembershipStatus(val label: String) {
    ACTIVE("Active"),
    PENDING("Pending Approval"),
    SUSPENDED("Suspended")
}

data class AccountOnboardingInput(
    val displayNameOverride: String = "",
    val requestedRole: AccountRole = AccountRole.MEMBER,
    val organization: String = "",
    val inviteCode: String = ""
)

enum class InquiryStatus(val label: String) {
    SUBMITTED("Submitted"),
    CANCELLED("Cancelled")
}

enum class ProgramType(val label: String) {
    TRAINING("Training program"),
    CERTIFICATION("Certification program")
}

enum class EnrollmentStatus(val label: String) {
    ENROLLED("Enrolled"),
    COMPLETED("Completed")
}

enum class ApplicationStatus(val label: String) {
    SUBMITTED("Submitted"),
    WITHDRAWN("Withdrawn")
}

enum class WorkspaceModuleType(val label: String) {
    USER("User Management"),
    COURSE("Academy Management"),
    EMPLOYEE("Employees & Departments"),
    PAYMENT("Payments & Finance"),
    ANNOUNCEMENT("Announcements"),
    NEWS("News & Media"),
    SUPPORT("Messages & Support"),
    TASK("Staff Tasks")
}

enum class AuditSeverity(val label: String) {
    INFO("INFO"),
    WARNING("WARNING"),
    CRITICAL("CRITICAL")
}

enum class PublicationCategory(val label: String) {
    ANNOUNCEMENT("Announcement"),
    DOCUMENT("Document"),
    NEWS("News"),
    GUIDE("User Guide"),
    FAQ("FAQ")
}

enum class PublicationStatus(val label: String) {
    PUBLISHED("Published"),
    DRAFT("Draft"),
    EXPIRED("Expired")
}

enum class ReleasePlatform(val label: String, val storeLabel: String) {
    ANDROID("Android", "Google Play / APK"),
    IOS("iPhone & iPad", "Apple App Store"),
    WINDOWS("Windows", "Desktop / PWA"),
    WEB("Web", "Codiva.com")
}

enum class ReleaseStatus(val label: String) {
    LIVE("Live"),
    BETA("Beta"),
    RETIRED("Retired")
}

inline fun <reified T : Enum<T>> safeEnumValueOf(raw: String?, default: T): T {
    return enumValues<T>().firstOrNull { it.name.equals(raw, ignoreCase = true) } ?: default
}

@Keep
@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val displayName: String = "",
    val email: String = "",
    val accountRole: String = AccountRole.SUPER_ADMIN.name,
    val organization: String = "",
    val phone: String = "",
    val membershipStatus: String = "ACTIVE",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    val roleEnum: AccountRole
        get() = safeEnumValueOf(accountRole, AccountRole.MEMBER)
    val statusEnum: MembershipStatus
        get() = safeEnumValueOf(membershipStatus, MembershipStatus.ACTIVE)
}

@Keep
@IgnoreExtraProperties
data class ProjectInquiry(
    @DocumentId val id: String = "",
    val userId: String = "",
    val serviceCategory: String = "",
    val projectTitle: String = "",
    val details: String = "",
    val budgetTier: String = "",
    val status: String = InquiryStatus.SUBMITTED.name,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    val statusEnum: InquiryStatus
        get() = safeEnumValueOf(status, InquiryStatus.SUBMITTED)
}

@Keep
@IgnoreExtraProperties
data class CourseEnrollment(
    @DocumentId val id: String = "",
    val userId: String = "",
    val courseId: String = "",
    val courseTitle: String = "",
    val programType: String = ProgramType.TRAINING.name,
    val progressPercent: Long = 0L,
    val status: String = EnrollmentStatus.ENROLLED.name,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    val typeEnum: ProgramType
        get() = safeEnumValueOf(programType, ProgramType.TRAINING)
    val statusEnum: EnrollmentStatus
        get() = safeEnumValueOf(status, EnrollmentStatus.ENROLLED)
}

@Keep
@IgnoreExtraProperties
data class CareerApplication(
    @DocumentId val id: String = "",
    val userId: String = "",
    val roleId: String = "",
    val roleTitle: String = "",
    val applicantName: String = "",
    val portfolioNote: String = "",
    val status: String = ApplicationStatus.SUBMITTED.name,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    val statusEnum: ApplicationStatus
        get() = safeEnumValueOf(status, ApplicationStatus.SUBMITTED)
}

@Keep
@IgnoreExtraProperties
data class WorkspaceRecord(
    @DocumentId val id: String = "",
    val userId: String = "",
    val moduleType: String = WorkspaceModuleType.USER.name,
    val title: String = "",
    val subtitle: String = "",
    val detail: String = "",
    val category: String = "",
    val status: String = "",
    val amountCents: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    val moduleEnum: WorkspaceModuleType
        get() = safeEnumValueOf(moduleType, WorkspaceModuleType.USER)
}

@Keep
@IgnoreExtraProperties
data class AuditLogEntry(
    @DocumentId val id: String = "",
    val userId: String = "",
    val actorEmail: String = "",
    val action: String = "",
    val target: String = "",
    val severity: String = AuditSeverity.INFO.name,
    @ServerTimestamp val createdAt: Timestamp? = null
) {
    val severityEnum: AuditSeverity
        get() = safeEnumValueOf(severity, AuditSeverity.INFO)
}

@Keep
@IgnoreExtraProperties
data class SystemSettingsDoc(
    @DocumentId val id: String = "",
    val userId: String = "",
    val autoApproveMembers: Boolean = false,
    val maintenanceMode: Boolean = false,
    val paymentsEnabled: Boolean = true,
    val auditLoggingEnabled: Boolean = true,
    val holidayNoticeEnabled: Boolean = true,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class PublicPortalConfig(
    @DocumentId val id: String = "",
    val portalTitle: String = "CODIVA Technologies",
    val publicUrl: String = CodivaCatalog.PUBLIC_PORTAL_URL,
    val customDomain: String = CodivaCatalog.OFFICIAL_DOMAIN,
    val welcomeMessage: String = "Welcome to CODIVA Technologies (${CodivaCatalog.OFFICIAL_DOMAIN}). Create an account to access Academy courses, projects, and community dashboards.",
    val inviteCode: String = "CODIVA-2026",
    val portalPublished: Boolean = true,
    val allowPublicSignUp: Boolean = true,
    val requireAdminApproval: Boolean = false,
    val updatedBy: String = CodivaCatalog.FOUNDER_EMAIL,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class AdminPublication(
    @DocumentId val id: String = "",
    val title: String = "",
    val summary: String = "",
    val content: String = "",
    val category: String = PublicationCategory.ANNOUNCEMENT.name,
    val targetRole: String = "ALL",
    val documentUrl: String = "",
    val documentLabel: String = "",
    val expiresAtLabel: String = "",
    val status: String = PublicationStatus.PUBLISHED.name,
    val publishedBy: String = CodivaCatalog.FOUNDER_EMAIL,
    val createdBy: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    val categoryEnum: PublicationCategory
        get() = safeEnumValueOf(category, PublicationCategory.ANNOUNCEMENT)
    val statusEnum: PublicationStatus
        get() = safeEnumValueOf(status, PublicationStatus.PUBLISHED)
}

@Keep
@IgnoreExtraProperties
data class AppRelease(
    @DocumentId val id: String = "",
    val platform: String = ReleasePlatform.ANDROID.name,
    val versionName: String = "1.0.0",
    val buildNumber: Long = 1L,
    val releaseNotes: String = "",
    val downloadUrl: String = CodivaCatalog.PUBLIC_PORTAL_URL,
    val status: String = ReleaseStatus.LIVE.name,
    val updatedBy: String = CodivaCatalog.FOUNDER_EMAIL,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    val platformEnum: ReleasePlatform
        get() = safeEnumValueOf(platform, ReleasePlatform.ANDROID)
    val statusEnum: ReleaseStatus
        get() = safeEnumValueOf(status, ReleaseStatus.LIVE)
}

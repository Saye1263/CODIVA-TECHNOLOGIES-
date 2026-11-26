package com.example.data

enum class DashboardPermission(val label: String) {
    VIEW_FOUNDER_DASHBOARD("Founder Control Panel"),
    VIEW_FULL_ADMIN_CONSOLE("11-Module Admin Console"),
    MANAGE_USERS("User Management"),
    MANAGE_ACADEMY("Academy Management"),
    TEACH_ACADEMY_COHORTS("Teach Cohorts & Grading"),
    MANAGE_PROJECTS("Project Management"),
    MANAGE_SECURITY_SETTINGS("Security & Audit Logs"),
    MANAGE_STAFF_TASKS("Staff Tasks & Attendance"),
    VIEW_PARENT_PORTAL("Student Progress & Tuition"),
    VIEW_MEMBER_COMMUNITY("Community & News"),
    MANAGE_DEPARTMENTS("Departments & Staff"),
    VIEW_FINANCE_LEDGER("Payments & Invoices"),
    VIEW_ANALYTICS("Executive Analytics"),
    MANAGE_PUBLIC_HOSTING("Public Hosting & Onboarding"),
    MANAGE_PUBLICATIONS("Publications & Documents"),
    MANAGE_RELEASES("App Downloads & Store Releases"),
    VIEW_PARTNER_PORTAL("Partner Hub & Initiatives")
}

enum class DashboardSection(
    val title: String,
    val requiredPermission: DashboardPermission
) {
    FOUNDER_CONTROL_PANEL("Founder Dashboard", DashboardPermission.VIEW_FOUNDER_DASHBOARD),
    HOSTING_LAUNCH("Hosting & Public Launch", DashboardPermission.MANAGE_PUBLIC_HOSTING),
    PUBLICATIONS_MANAGER("Publications & Docs", DashboardPermission.MANAGE_PUBLICATIONS),
    APP_RELEASES_MANAGER("App Downloads & Stores", DashboardPermission.MANAGE_RELEASES),
    USER_MANAGEMENT("User Management", DashboardPermission.MANAGE_USERS),
    ACADEMY_MANAGEMENT("Academy Management", DashboardPermission.MANAGE_ACADEMY),
    PROJECT_MANAGEMENT("Project Management", DashboardPermission.MANAGE_PROJECTS),
    SECURITY_MANAGEMENT("Security Management", DashboardPermission.MANAGE_SECURITY_SETTINGS),
    ANALYTICS_OVERVIEW("Executive Analytics", DashboardPermission.VIEW_ANALYTICS),
    MEMBER_COMMUNITY("Member Hub & News", DashboardPermission.VIEW_MEMBER_COMMUNITY),
    STAFF_OPERATIONS("Staff Tasks & Operations", DashboardPermission.MANAGE_STAFF_TASKS),
    LECTURER_COHORTS("Lecturer Courses & Cohorts", DashboardPermission.TEACH_ACADEMY_COHORTS),
    PARENT_OVERVIEW("Parent & Student Progress", DashboardPermission.VIEW_PARENT_PORTAL),
    PARTNER_OVERVIEW("Partner Collaboration Hub", DashboardPermission.VIEW_PARTNER_PORTAL),
    DEPARTMENT_MANAGEMENT("Department Oversight", DashboardPermission.MANAGE_DEPARTMENTS),
    FINANCE_OPERATIONS("Finance & Ledger", DashboardPermission.VIEW_FINANCE_LEDGER)
}

/**
 * Compatibility facade delegating role-based access control (RBAC) checks to [UserRoleManager].
 */
object RolePermissionManager {

    fun isFounderEmail(email: String?): Boolean = UserRoleManager.isFounderEmail(email)

    fun permissionsFor(role: AccountRole): Set<DashboardPermission> =
        UserRoleManager.permissionsFor(role)

    fun hasPermission(role: AccountRole, permission: DashboardPermission): Boolean =
        UserRoleManager.hasPermission(role, permission)

    fun canAccessSection(role: AccountRole, section: DashboardSection): Boolean =
        UserRoleManager.canAccessSection(role, section)

    fun canAccessFounderDashboard(role: AccountRole): Boolean =
        UserRoleManager.canAccessFounderDashboard(role)

    fun canAccessFullAdminConsole(role: AccountRole): Boolean =
        UserRoleManager.canAccessFullAdminConsole(role)

    fun canAccessWorkspaceModule(role: AccountRole, moduleType: WorkspaceModuleType): Boolean =
        UserRoleManager.canAccessWorkspaceModule(role, moduleType)

    fun visibleSectionsFor(role: AccountRole): List<DashboardSection> =
        UserRoleManager.visibleSectionsFor(role)

    fun permissionLabelsFor(role: AccountRole): List<String> =
        UserRoleManager.permissionLabelsFor(role)

    fun consumerRoles(): List<AccountRole> = UserRoleManager.consumerRoles()

    fun assignableRolesFor(email: String?, currentRole: AccountRole? = null): List<AccountRole> =
        UserRoleManager.assignableRolesFor(email, currentRole)

    fun selfSignupRoles(): List<AccountRole> = UserRoleManager.selfSignupRoles()

    fun resolveEffectiveRole(
        requestedRole: AccountRole,
        email: String?,
        currentPersistedRole: AccountRole? = null
    ): AccountRole = UserRoleManager.resolveEffectiveRole(requestedRole, email, currentPersistedRole)

    fun resolveSignupStatus(
        email: String?,
        requestedRole: AccountRole,
        requireAdminApproval: Boolean,
        hasValidInviteCode: Boolean
    ): MembershipStatus = UserRoleManager.resolveSignupStatus(
        email = email,
        requestedRole = requestedRole,
        requireAdminApproval = requireAdminApproval,
        hasValidInviteCode = hasValidInviteCode
    )
}

package com.example.data

/**
 * Canonical route keys used by Navigation Compose and enforced by [UserRoleManager].
 */
object CodivaAppRoutes {
    const val HOME = "home"
    const val SOLUTIONS = "solutions"
    const val ECOSYSTEM = "ecosystem"
    const val ACADEMY = "academy"
    const val PORTAL = "portal"
    const val CAREERS = "careers"
}

/**
 * Role-based access control (RBAC) manager for CODIVA Technologies.
 *
 * Handles core roles (Super Admin, Member, Staff, Lecturer, Parent) as well as specialized
 * operational roles (User, HOD, Finance, Partner), enforcing conditional UI visibility and
 * navigation access across all user dashboards.
 */
class UserRoleManager(
    val activeRole: AccountRole,
    val userEmail: String? = null,
    val membershipStatus: MembershipStatus = MembershipStatus.ACTIVE
) {
    val isFounder: Boolean
        get() = isFounderEmail(userEmail)

    val isSuperAdmin: Boolean
        get() = activeRole == AccountRole.SUPER_ADMIN && membershipStatus != MembershipStatus.SUSPENDED

    val isMember: Boolean
        get() = activeRole == AccountRole.MEMBER

    val isStaff: Boolean
        get() = activeRole == AccountRole.STAFF

    val isLecturer: Boolean
        get() = activeRole == AccountRole.LECTURER

    val isParent: Boolean
        get() = activeRole == AccountRole.PARENT

    val isSuspended: Boolean
        get() = membershipStatus == MembershipStatus.SUSPENDED && !isFounder

    val isPendingApproval: Boolean
        get() = membershipStatus == MembershipStatus.PENDING && !isFounder

    val isActiveAccount: Boolean
        get() = membershipStatus == MembershipStatus.ACTIVE || isFounder

    fun permissions(): Set<DashboardPermission> {
        if (isSuspended) return emptySet()
        return permissionsFor(activeRole)
    }

    fun permissionLabels(): List<String> {
        return permissions().map { it.label }
    }

    fun hasPermission(permission: DashboardPermission): Boolean {
        if (isSuspended) return false
        return permissionsFor(activeRole).contains(permission)
    }

    fun hasAnyPermission(vararg required: DashboardPermission): Boolean {
        if (isSuspended) return false
        val granted = permissionsFor(activeRole)
        return required.any { it in granted }
    }

    fun hasAllPermissions(vararg required: DashboardPermission): Boolean {
        if (isSuspended) return false
        val granted = permissionsFor(activeRole)
        return required.all { it in granted }
    }

    fun canAccessSection(section: DashboardSection): Boolean {
        return hasPermission(section.requiredPermission)
    }

    fun visibleSections(): List<DashboardSection> {
        if (isSuspended) return emptyList()
        return DashboardSection.entries.filter { canAccessSection(it) }
    }

    fun canAccessFounderDashboard(): Boolean {
        return !isSuspended && hasPermission(DashboardPermission.VIEW_FOUNDER_DASHBOARD)
    }

    fun canAccessFullAdminConsole(): Boolean {
        return !isSuspended && hasPermission(DashboardPermission.VIEW_FULL_ADMIN_CONSOLE)
    }

    fun canAccessRoleDashboard(targetRole: AccountRole): Boolean {
        if (isSuspended) return false
        if (canAccessFounderDashboard()) return true
        return activeRole == targetRole
    }

    fun canSwitchToRole(targetRole: AccountRole): Boolean {
        return targetRole in assignableRoles()
    }

    fun assignableRoles(): List<AccountRole> {
        return assignableRolesFor(userEmail, activeRole)
    }

    fun canAccessWorkspaceModule(moduleType: WorkspaceModuleType): Boolean {
        if (isSuspended) return moduleType == WorkspaceModuleType.SUPPORT
        return canAccessWorkspaceModule(activeRole, moduleType)
    }

    fun canCreateProjectInquiry(): Boolean {
        if (isSuspended) return false
        return activeRole == AccountRole.SUPER_ADMIN ||
            activeRole == AccountRole.MEMBER ||
            activeRole == AccountRole.USER ||
            activeRole == AccountRole.PARTNER ||
            activeRole == AccountRole.HOD
    }

    fun canManageUsers(): Boolean = hasPermission(DashboardPermission.MANAGE_USERS)

    fun canManagePublications(): Boolean = hasPermission(DashboardPermission.MANAGE_PUBLICATIONS)

    fun canManageReleases(): Boolean = hasPermission(DashboardPermission.MANAGE_RELEASES)

    fun canExportReports(): Boolean = hasPermission(DashboardPermission.VIEW_ANALYTICS)

    fun canSubmitSupportTicket(): Boolean = true

    fun shouldShowMaintenanceBanner(settings: SystemSettingsDoc): Boolean {
        return settings.maintenanceMode && !isSuperAdmin
    }

    fun canReadPublication(publication: AdminPublication): Boolean {
        if (canAccessFounderDashboard()) return true
        if (publication.statusEnum != PublicationStatus.PUBLISHED) return false
        return publication.targetRole.equals("ALL", ignoreCase = true) ||
            publication.targetRole.equals(activeRole.name, ignoreCase = true)
    }

    fun filterVisiblePublications(publications: List<AdminPublication>): List<AdminPublication> {
        return publications.filter { canReadPublication(it) }
    }

    /**
     * Enforces top-level route navigation access per role and account status.
     *
     * - Super Admin: All routes (Home, Solutions, Ecosystem, Academy, Portal, Careers)
     * - Member / User / HOD: All routes with role-scoped dashboard modules
     * - Staff: Home, Ecosystem (Staff Operations), Portal, Careers
     * - Lecturer: Home, Ecosystem (Lecturer Cohorts), Academy, Portal, Careers
     * - Parent: Home, Ecosystem (Parent Portal), Academy, Portal
     * - Finance: Home, Ecosystem (Finance Ledger), Portal
     * - Partner: Home, Solutions, Ecosystem (Partner Hub), Portal, Careers
     * - Suspended accounts: Restricted to Home & Portal until reinstated
     */
    fun canNavigateToRoute(route: String): Boolean {
        return when (route) {
            CodivaAppRoutes.HOME,
            CodivaAppRoutes.PORTAL -> true

            CodivaAppRoutes.ECOSYSTEM -> !isSuspended

            CodivaAppRoutes.ACADEMY -> !isSuspended && hasAnyPermission(
                DashboardPermission.VIEW_FOUNDER_DASHBOARD,
                DashboardPermission.TEACH_ACADEMY_COHORTS,
                DashboardPermission.MANAGE_ACADEMY,
                DashboardPermission.VIEW_PARENT_PORTAL,
                DashboardPermission.VIEW_MEMBER_COMMUNITY,
                DashboardPermission.MANAGE_DEPARTMENTS
            ) && activeRole != AccountRole.STAFF && activeRole != AccountRole.PARTNER

            CodivaAppRoutes.SOLUTIONS -> !isSuspended && (
                hasAnyPermission(
                    DashboardPermission.VIEW_FOUNDER_DASHBOARD,
                    DashboardPermission.MANAGE_PROJECTS,
                    DashboardPermission.VIEW_PARTNER_PORTAL
                ) || activeRole == AccountRole.USER
            )

            CodivaAppRoutes.CAREERS -> !isSuspended &&
                activeRole != AccountRole.PARENT &&
                activeRole != AccountRole.FINANCE

            else -> false
        }
    }

    fun accessibleRoutes(candidates: List<String> = DEFAULT_ROUTE_ORDER): List<String> {
        return candidates.filter { canNavigateToRoute(it) }
    }

    fun defaultRoute(): String = CodivaAppRoutes.HOME

    fun navigationDenialReason(route: String): String {
        if (isSuspended) {
            return "Your account is currently suspended. Only Home notices and Account Portal support remain accessible."
        }
        return when (route) {
            CodivaAppRoutes.ACADEMY ->
                "Academy courses are reserved for Super Admin, Members, Lecturers, Parents, and Department Heads."
            CodivaAppRoutes.SOLUTIONS ->
                "Client Solutions & Project Ordering are hidden for ${activeRole.label} accounts."
            CodivaAppRoutes.CAREERS ->
                "Careers & Recruitment pages are not enabled for ${activeRole.label} dashboards."
            CodivaAppRoutes.ECOSYSTEM ->
                "Dashboard access requires an active or pending-review CODIVA account."
            else ->
                "Your current role (${activeRole.label}) does not have permission to open this destination."
        }
    }

    companion object {
        val DEFAULT_ROUTE_ORDER = listOf(
            CodivaAppRoutes.HOME,
            CodivaAppRoutes.SOLUTIONS,
            CodivaAppRoutes.ECOSYSTEM,
            CodivaAppRoutes.ACADEMY,
            CodivaAppRoutes.PORTAL,
            CodivaAppRoutes.CAREERS
        )

        private val rolePermissions: Map<AccountRole, Set<DashboardPermission>> = mapOf(
            AccountRole.SUPER_ADMIN to DashboardPermission.entries.toSet(),
            AccountRole.HOD to setOf(
                DashboardPermission.MANAGE_DEPARTMENTS,
                DashboardPermission.MANAGE_PROJECTS,
                DashboardPermission.MANAGE_STAFF_TASKS,
                DashboardPermission.VIEW_ANALYTICS
            ),
            AccountRole.FINANCE to setOf(
                DashboardPermission.VIEW_FINANCE_LEDGER,
                DashboardPermission.VIEW_ANALYTICS
            ),
            AccountRole.LECTURER to setOf(
                DashboardPermission.TEACH_ACADEMY_COHORTS,
                DashboardPermission.MANAGE_ACADEMY,
                DashboardPermission.VIEW_MEMBER_COMMUNITY
            ),
            AccountRole.STAFF to setOf(
                DashboardPermission.MANAGE_STAFF_TASKS,
                DashboardPermission.VIEW_MEMBER_COMMUNITY
            ),
            AccountRole.PARENT to setOf(
                DashboardPermission.VIEW_PARENT_PORTAL
            ),
            AccountRole.PARTNER to setOf(
                DashboardPermission.VIEW_PARTNER_PORTAL,
                DashboardPermission.VIEW_MEMBER_COMMUNITY
            ),
            AccountRole.MEMBER to setOf(
                DashboardPermission.VIEW_MEMBER_COMMUNITY,
                DashboardPermission.MANAGE_PROJECTS
            ),
            AccountRole.USER to setOf(
                DashboardPermission.VIEW_MEMBER_COMMUNITY
            )
        )

        fun fromProfile(profile: UserProfile?, fallbackEmail: String? = null): UserRoleManager {
            val effectiveEmail = profile?.email?.takeIf { it.isNotBlank() } ?: fallbackEmail
            val defaultRole = if (isFounderEmail(effectiveEmail)) AccountRole.SUPER_ADMIN else AccountRole.MEMBER
            val effectiveRole = resolveEffectiveRole(
                requestedRole = profile?.roleEnum ?: defaultRole,
                email = effectiveEmail,
                currentPersistedRole = profile?.roleEnum
            )
            val status = profile?.statusEnum ?: MembershipStatus.ACTIVE
            return UserRoleManager(
                activeRole = effectiveRole,
                userEmail = effectiveEmail,
                membershipStatus = status
            )
        }

        fun forRole(
            role: AccountRole,
            email: String? = null,
            status: MembershipStatus = MembershipStatus.ACTIVE
        ): UserRoleManager {
            return UserRoleManager(
                activeRole = role,
                userEmail = email,
                membershipStatus = status
            )
        }

        /**
         * Primary CODIVA access tiers highlighted across role governance.
         */
        fun coreFiveRoles(): List<AccountRole> {
            return listOf(
                AccountRole.SUPER_ADMIN,
                AccountRole.MEMBER,
                AccountRole.STAFF,
                AccountRole.LECTURER,
                AccountRole.PARENT
            )
        }

        fun isFounderEmail(email: String?): Boolean {
            return !email.isNullOrBlank() &&
                email.trim().equals(CodivaCatalog.FOUNDER_EMAIL, ignoreCase = true)
        }

        fun permissionsFor(role: AccountRole): Set<DashboardPermission> {
            return rolePermissions[role].orEmpty()
        }

        fun hasPermission(role: AccountRole, permission: DashboardPermission): Boolean {
            return permissionsFor(role).contains(permission)
        }

        fun canAccessSection(role: AccountRole, section: DashboardSection): Boolean {
            return hasPermission(role, section.requiredPermission)
        }

        fun canAccessFounderDashboard(role: AccountRole): Boolean {
            return hasPermission(role, DashboardPermission.VIEW_FOUNDER_DASHBOARD)
        }

        fun canAccessFullAdminConsole(role: AccountRole): Boolean {
            return hasPermission(role, DashboardPermission.VIEW_FULL_ADMIN_CONSOLE)
        }

        fun canAccessWorkspaceModule(role: AccountRole, moduleType: WorkspaceModuleType): Boolean {
            return when (moduleType) {
                WorkspaceModuleType.USER -> hasPermission(role, DashboardPermission.MANAGE_USERS)
                WorkspaceModuleType.COURSE -> hasPermission(role, DashboardPermission.MANAGE_ACADEMY) ||
                    hasPermission(role, DashboardPermission.TEACH_ACADEMY_COHORTS)
                WorkspaceModuleType.EMPLOYEE -> hasPermission(role, DashboardPermission.MANAGE_DEPARTMENTS)
                WorkspaceModuleType.PAYMENT -> hasPermission(role, DashboardPermission.VIEW_FINANCE_LEDGER)
                WorkspaceModuleType.ANNOUNCEMENT,
                WorkspaceModuleType.NEWS -> hasPermission(role, DashboardPermission.VIEW_FOUNDER_DASHBOARD)
                WorkspaceModuleType.SUPPORT -> hasPermission(role, DashboardPermission.VIEW_FOUNDER_DASHBOARD) ||
                    hasPermission(role, DashboardPermission.VIEW_PARENT_PORTAL) ||
                    hasPermission(role, DashboardPermission.MANAGE_STAFF_TASKS)
                WorkspaceModuleType.TASK -> hasPermission(role, DashboardPermission.MANAGE_STAFF_TASKS)
            }
        }

        fun visibleSectionsFor(role: AccountRole): List<DashboardSection> {
            return DashboardSection.entries.filter { canAccessSection(role, it) }
        }

        fun permissionLabelsFor(role: AccountRole): List<String> {
            return permissionsFor(role).map { it.label }
        }

        fun consumerRoles(): List<AccountRole> {
            return listOf(
                AccountRole.MEMBER,
                AccountRole.USER,
                AccountRole.PARENT
            )
        }

        fun selfSignupRoles(): List<AccountRole> {
            return listOf(
                AccountRole.MEMBER,
                AccountRole.USER,
                AccountRole.PARENT,
                AccountRole.STAFF,
                AccountRole.LECTURER,
                AccountRole.PARTNER
            )
        }

        fun assignableRolesFor(email: String?, currentRole: AccountRole? = null): List<AccountRole> {
            return if (isFounderEmail(email)) {
                AccountRole.entries
            } else {
                (consumerRoles() + listOfNotNull(currentRole)).distinct()
            }
        }

        fun resolveEffectiveRole(
            requestedRole: AccountRole,
            email: String?,
            currentPersistedRole: AccountRole? = null
        ): AccountRole {
            return when {
                isFounderEmail(email) -> requestedRole
                requestedRole == currentPersistedRole -> requestedRole
                requestedRole in consumerRoles() -> requestedRole
                else -> currentPersistedRole ?: AccountRole.MEMBER
            }
        }

        fun resolveSignupStatus(
            email: String?,
            requestedRole: AccountRole,
            requireAdminApproval: Boolean,
            hasValidInviteCode: Boolean
        ): MembershipStatus {
            if (isFounderEmail(email)) return MembershipStatus.ACTIVE
            if (requestedRole == AccountRole.STAFF ||
                requestedRole == AccountRole.LECTURER ||
                requestedRole == AccountRole.PARTNER
            ) {
                return MembershipStatus.PENDING
            }
            if (hasValidInviteCode && requestedRole in consumerRoles()) return MembershipStatus.ACTIVE
            return if (requireAdminApproval) MembershipStatus.PENDING else MembershipStatus.ACTIVE
        }
    }
}

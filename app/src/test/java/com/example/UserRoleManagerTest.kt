package com.example

import com.example.data.AccountRole
import com.example.data.AdminPublication
import com.example.data.CodivaAppRoutes
import com.example.data.CodivaCatalog
import com.example.data.DashboardPermission
import com.example.data.DashboardSection
import com.example.data.MembershipStatus
import com.example.data.PublicationStatus
import com.example.data.SystemSettingsDoc
import com.example.data.UserProfile
import com.example.data.UserRoleManager
import com.example.data.WorkspaceModuleType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UserRoleManagerTest {

    @Test
    fun superAdminHasFullDashboardAndRouteAccess() {
        val manager = UserRoleManager.forRole(
            role = AccountRole.SUPER_ADMIN,
            email = CodivaCatalog.FOUNDER_EMAIL
        )

        assertTrue(manager.isFounder)
        assertTrue(manager.isSuperAdmin)
        assertTrue(manager.canAccessFounderDashboard())
        assertTrue(manager.canAccessFullAdminConsole())
        assertTrue(manager.canExportReports())
        assertTrue(manager.canManageUsers())
        assertTrue(manager.canManagePublications())
        assertTrue(manager.canManageReleases())
        assertTrue(manager.canCreateProjectInquiry())
        DashboardSection.entries.forEach { section ->
            assertTrue("Expected access to $section", manager.canAccessSection(section))
        }
        UserRoleManager.DEFAULT_ROUTE_ORDER.forEach { route ->
            assertTrue("Expected access to route $route", manager.canNavigateToRoute(route))
        }
    }

    @Test
    fun memberHasCommunityAndProjectAccessButNoAdminAccess() {
        val manager = UserRoleManager.forRole(
            role = AccountRole.MEMBER,
            email = "member@codiva.com"
        )

        assertTrue(manager.isMember)
        assertFalse(manager.canAccessFounderDashboard())
        assertFalse(manager.canAccessFullAdminConsole())
        assertTrue(manager.canAccessSection(DashboardSection.MEMBER_COMMUNITY))
        assertTrue(manager.canAccessSection(DashboardSection.PROJECT_MANAGEMENT))
        assertFalse(manager.canAccessSection(DashboardSection.USER_MANAGEMENT))
        assertFalse(manager.canAccessSection(DashboardSection.SECURITY_MANAGEMENT))

        assertTrue(manager.canNavigateToRoute(CodivaAppRoutes.HOME))
        assertTrue(manager.canNavigateToRoute(CodivaAppRoutes.SOLUTIONS))
        assertTrue(manager.canNavigateToRoute(CodivaAppRoutes.ECOSYSTEM))
        assertTrue(manager.canNavigateToRoute(CodivaAppRoutes.ACADEMY))
        assertTrue(manager.canNavigateToRoute(CodivaAppRoutes.PORTAL))
        assertTrue(manager.canNavigateToRoute(CodivaAppRoutes.CAREERS))
        assertTrue(manager.canCreateProjectInquiry())
    }

    @Test
    fun staffAccessesOperationsButNotSolutionsOrAcademyOrAdmin() {
        val manager = UserRoleManager.forRole(
            role = AccountRole.STAFF,
            email = "staff@codiva.com"
        )

        assertTrue(manager.isStaff)
        assertFalse(manager.canAccessFounderDashboard())
        assertTrue(manager.canAccessSection(DashboardSection.STAFF_OPERATIONS))
        assertTrue(manager.canAccessWorkspaceModule(WorkspaceModuleType.TASK))
        assertTrue(manager.canAccessWorkspaceModule(WorkspaceModuleType.SUPPORT))
        assertFalse(manager.canAccessWorkspaceModule(WorkspaceModuleType.PAYMENT))
        assertFalse(manager.canCreateProjectInquiry())

        assertTrue(manager.canNavigateToRoute(CodivaAppRoutes.HOME))
        assertTrue(manager.canNavigateToRoute(CodivaAppRoutes.ECOSYSTEM))
        assertTrue(manager.canNavigateToRoute(CodivaAppRoutes.PORTAL))
        assertTrue(manager.canNavigateToRoute(CodivaAppRoutes.CAREERS))
        assertFalse(manager.canNavigateToRoute(CodivaAppRoutes.SOLUTIONS))
        assertFalse(manager.canNavigateToRoute(CodivaAppRoutes.ACADEMY))
    }

    @Test
    fun lecturerAccessesTeachingCohortsAndAcademy() {
        val manager = UserRoleManager.forRole(
            role = AccountRole.LECTURER,
            email = "lecturer@codiva.com"
        )

        assertTrue(manager.isLecturer)
        assertFalse(manager.canAccessFounderDashboard())
        assertTrue(manager.canAccessSection(DashboardSection.LECTURER_COHORTS))
        assertTrue(manager.canAccessSection(DashboardSection.ACADEMY_MANAGEMENT))
        assertTrue(manager.canAccessWorkspaceModule(WorkspaceModuleType.COURSE))
        assertFalse(manager.canAccessWorkspaceModule(WorkspaceModuleType.USER))

        assertTrue(manager.canNavigateToRoute(CodivaAppRoutes.HOME))
        assertTrue(manager.canNavigateToRoute(CodivaAppRoutes.ECOSYSTEM))
        assertTrue(manager.canNavigateToRoute(CodivaAppRoutes.ACADEMY))
        assertTrue(manager.canNavigateToRoute(CodivaAppRoutes.PORTAL))
        assertFalse(manager.canNavigateToRoute(CodivaAppRoutes.SOLUTIONS))
    }

    @Test
    fun parentAccessesStudentProgressAndAcademyWithoutSolutionsOrCareers() {
        val manager = UserRoleManager.forRole(
            role = AccountRole.PARENT,
            email = "parent@codiva.com"
        )

        assertTrue(manager.isParent)
        assertFalse(manager.canAccessFounderDashboard())
        assertTrue(manager.canAccessSection(DashboardSection.PARENT_OVERVIEW))
        assertFalse(manager.canAccessSection(DashboardSection.FINANCE_OPERATIONS))
        assertTrue(manager.canAccessWorkspaceModule(WorkspaceModuleType.SUPPORT))
        assertFalse(manager.canCreateProjectInquiry())

        assertTrue(manager.canNavigateToRoute(CodivaAppRoutes.HOME))
        assertTrue(manager.canNavigateToRoute(CodivaAppRoutes.ECOSYSTEM))
        assertTrue(manager.canNavigateToRoute(CodivaAppRoutes.ACADEMY))
        assertTrue(manager.canNavigateToRoute(CodivaAppRoutes.PORTAL))
        assertFalse(manager.canNavigateToRoute(CodivaAppRoutes.SOLUTIONS))
        assertFalse(manager.canNavigateToRoute(CodivaAppRoutes.CAREERS))
    }

    @Test
    fun suspendedUsersAreRestrictedToHomeAndPortalOnly() {
        val profile = UserProfile(
            userId = "u-1",
            displayName = "Suspended Member",
            email = "suspended@codiva.com",
            accountRole = AccountRole.MEMBER.name,
            membershipStatus = MembershipStatus.SUSPENDED.name
        )
        val manager = UserRoleManager.fromProfile(profile)

        assertTrue(manager.isSuspended)
        assertTrue(manager.permissions().isEmpty())
        assertFalse(manager.hasPermission(DashboardPermission.VIEW_MEMBER_COMMUNITY))
        assertFalse(manager.canNavigateToRoute(CodivaAppRoutes.ECOSYSTEM))
        assertFalse(manager.canNavigateToRoute(CodivaAppRoutes.ACADEMY))
        assertFalse(manager.canNavigateToRoute(CodivaAppRoutes.SOLUTIONS))
        assertTrue(manager.canNavigateToRoute(CodivaAppRoutes.HOME))
        assertTrue(manager.canNavigateToRoute(CodivaAppRoutes.PORTAL))
    }

    @Test
    fun publicationVisibilityAndMaintenanceBannerRespectRoleRules() {
        val staffManager = UserRoleManager.forRole(AccountRole.STAFF, "staff@codiva.com")
        val adminManager = UserRoleManager.forRole(AccountRole.SUPER_ADMIN, CodivaCatalog.FOUNDER_EMAIL)

        val settings = SystemSettingsDoc(maintenanceMode = true)
        assertTrue(staffManager.shouldShowMaintenanceBanner(settings))
        assertFalse(adminManager.shouldShowMaintenanceBanner(settings))

        val publications = listOf(
            AdminPublication(id = "1", title = "Public Notice", targetRole = "ALL", status = PublicationStatus.PUBLISHED.name),
            AdminPublication(id = "2", title = "Staff Memo", targetRole = "STAFF", status = PublicationStatus.PUBLISHED.name),
            AdminPublication(id = "3", title = "Parent Notice", targetRole = "PARENT", status = PublicationStatus.PUBLISHED.name),
            AdminPublication(id = "4", title = "Old Draft", targetRole = "ALL", status = PublicationStatus.DRAFT.name)
        )

        val staffVisible = staffManager.filterVisiblePublications(publications)
        assertEquals(listOf("1", "2"), staffVisible.map { it.id })

        val adminVisible = adminManager.filterVisiblePublications(publications)
        assertEquals(listOf("1", "2", "3", "4"), adminVisible.map { it.id })
    }
}

package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AdminPanelSettings
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.InstallMobile
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.WorkOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.AccountRole
import com.example.data.AdminPublication
import com.example.data.AppRelease
import com.example.data.AuditLogEntry
import com.example.data.AuditSeverity
import com.example.data.CareerApplication
import com.example.data.CodivaCatalog
import com.example.data.CourseEnrollment
import com.example.data.DashboardSection
import com.example.data.InquiryStatus
import com.example.data.MembershipStatus
import com.example.data.ProjectInquiry
import com.example.data.PublicPortalConfig
import com.example.data.PublicationCategory
import com.example.data.PublicationStatus
import com.example.data.ReleasePlatform
import com.example.data.ReleaseStatus
import com.example.data.SystemSettingsDoc
import com.example.data.UserProfile
import com.example.data.UserRoleManager
import com.example.data.WorkspaceModuleType
import com.example.data.WorkspaceRecord
import com.example.ui.UiState
import com.example.ui.components.ActivityHeatmapAndCalendarRow
import com.example.ui.components.AddWorkspaceRecordSheet
import com.example.ui.components.AdminPublicationCard
import com.example.ui.components.AppReleaseCard
import com.example.ui.components.AppReleaseEditorSheet
import com.example.ui.components.AuditLogCard
import com.example.ui.components.BadgeTone
import com.example.ui.components.ErrorBanner
import com.example.ui.components.ExecutiveKpiOverviewRow
import com.example.ui.components.FounderAccessRestrictedView
import com.example.ui.components.FounderCommandHeader
import com.example.ui.components.FounderEmptyStateCard
import com.example.ui.components.FounderNavDestination
import com.example.ui.components.FounderNavigationHub
import com.example.ui.components.HostingLaunchCard
import com.example.ui.components.HostingSettingsSheet
import com.example.ui.components.InstallPlatformSheet
import com.example.ui.components.LegalDocumentsSheet
import com.example.ui.components.ModuleHeaderRow
import com.example.ui.components.PlatformAnalyticsChartCard
import com.example.ui.components.PlatformDistributionBarCard
import com.example.ui.components.ProductionPipelineCard
import com.example.ui.components.PublicationEditorSheet
import com.example.ui.components.PublicationReaderSheet
import com.example.ui.components.RegisteredUserAccountCard
import com.example.ui.components.SectionHeader
import com.example.ui.components.SettingToggleCard
import com.example.ui.components.StatusBadge
import com.example.ui.components.UserRolesDonutCard
import com.example.ui.components.WorkspaceRecordCard
import com.example.ui.theme.CodivaAmber
import com.example.ui.theme.CodivaCobalt
import com.example.ui.theme.CodivaEmerald
import com.example.ui.theme.CodivaOrange
import com.example.ui.theme.CodivaSkyBright
import com.example.ui.theme.CodivaViolet

enum class FounderSection(val label: String, val icon: ImageVector) {
    ALL("All Core", Icons.Rounded.AdminPanelSettings),
    USERS("User Management", Icons.Rounded.Groups),
    ACADEMY("Academy Courses", Icons.Rounded.School),
    PROJECTS("Project Tracking", Icons.Rounded.WorkOutline),
    SECURITY("Audit Logs", Icons.Rounded.Security),
    HOSTING("Hosting & Launch", Icons.Rounded.Public),
    PUBLICATIONS("Publications & Docs", Icons.Rounded.Description),
    RELEASES("App Releases", Icons.Rounded.InstallMobile)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FounderDashboard(
    workspaceRecordsState: UiState<List<WorkspaceRecord>>,
    auditLogsState: UiState<List<AuditLogEntry>>,
    settingsState: UiState<SystemSettingsDoc?>,
    publicPortalState: UiState<PublicPortalConfig?>,
    registeredUsersState: UiState<List<UserProfile>>,
    publicationsState: UiState<List<AdminPublication>>,
    appReleasesState: UiState<List<AppRelease>>,
    inquiriesState: UiState<List<ProjectInquiry>>,
    enrollmentsState: UiState<List<CourseEnrollment>>,
    applicationsState: UiState<List<CareerApplication>>,
    isSubmitting: Boolean,
    onAddWorkspaceRecord: (WorkspaceModuleType, String, String, String, String, String, Long, () -> Unit) -> Unit,
    onUpdateRecordStatus: (WorkspaceRecord, String) -> Unit,
    onDeleteRecord: (WorkspaceRecord) -> Unit,
    onUpdateSettings: (SystemSettingsDoc) -> Unit,
    onUpdatePublicPortalConfig: (PublicPortalConfig, () -> Unit) -> Unit,
    onAdminUpdateRegisteredUser: (UserProfile, AccountRole, MembershipStatus) -> Unit,
    onSavePublication: (String?, String, String, String, PublicationCategory, String, String, String, String, PublicationStatus, () -> Unit) -> Unit,
    onUpdatePublicationStatus: (AdminPublication, PublicationStatus) -> Unit,
    onDeletePublication: (AdminPublication) -> Unit,
    onSaveAppRelease: (String?, ReleasePlatform, String, Long, String, String, ReleaseStatus, () -> Unit) -> Unit,
    onUpdateAppReleaseStatus: (AppRelease, ReleaseStatus) -> Unit,
    onDeleteAppRelease: (AppRelease) -> Unit,
    onNotify: (String) -> Unit,
    onRequestQuote: (String) -> Unit,
    onCancelInquiry: (String) -> Unit,
    activeRole: AccountRole = AccountRole.SUPER_ADMIN,
    onBackToRoles: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val roleManager = remember(activeRole) { UserRoleManager.forRole(activeRole) }
    if (!roleManager.canAccessFounderDashboard()) {
        FounderAccessRestrictedView(
            activeRole = activeRole,
            roleManager = roleManager,
            onBackToRoles = onBackToRoles,
            modifier = modifier
        )
        return
    }

    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var selectedSection by remember { mutableStateOf(FounderSection.ALL) }
    var showFullConsole by remember { mutableStateOf(false) }
    var addSheetModule by remember { mutableStateOf<WorkspaceModuleType?>(null) }
    var showHostingSheet by remember { mutableStateOf(false) }
    var showPublicationEditor by remember { mutableStateOf(false) }
    var editingPublication by remember { mutableStateOf<AdminPublication?>(null) }
    var readingPublication by remember { mutableStateOf<AdminPublication?>(null) }
    var showReleaseEditor by remember { mutableStateOf(false) }
    var editingRelease by remember { mutableStateOf<AppRelease?>(null) }
    var showInstallPreview by remember { mutableStateOf(false) }
    var showLegalDocs by remember { mutableStateOf(false) }

    var userSearchQuery by remember { mutableStateOf("") }
    var userStatusFilter by remember { mutableStateOf<MembershipStatus?>(null) }
    var projectStatusFilter by remember { mutableStateOf<String?>(null) }
    var auditSeverityFilter by remember { mutableStateOf<AuditSeverity?>(null) }

    BackHandler(enabled = selectedSection != FounderSection.ALL && !showFullConsole) {
        selectedSection = FounderSection.ALL
    }

    val canOpenFullConsole = roleManager.canAccessFullAdminConsole()
    val canManageHosting = roleManager.canAccessSection(DashboardSection.HOSTING_LAUNCH)
    val canManagePublications = roleManager.canAccessSection(DashboardSection.PUBLICATIONS_MANAGER)
    val canManageReleases = roleManager.canAccessSection(DashboardSection.APP_RELEASES_MANAGER)
    val canManageUsers = roleManager.canAccessSection(DashboardSection.USER_MANAGEMENT)
    val canManageAcademy = roleManager.canAccessSection(DashboardSection.ACADEMY_MANAGEMENT)
    val canManageProjects = roleManager.canAccessSection(DashboardSection.PROJECT_MANAGEMENT)
    val canManageSecurity = roleManager.canAccessSection(DashboardSection.SECURITY_MANAGEMENT)
    val canViewAnalytics = roleManager.canAccessSection(DashboardSection.ANALYTICS_OVERVIEW)

    val visibleChips = remember(activeRole) {
        FounderSection.entries.filter { section ->
            when (section) {
                FounderSection.ALL -> true
                FounderSection.HOSTING -> canManageHosting
                FounderSection.PUBLICATIONS -> canManagePublications
                FounderSection.RELEASES -> canManageReleases
                FounderSection.USERS -> canManageUsers
                FounderSection.ACADEMY -> canManageAcademy
                FounderSection.PROJECTS -> canManageProjects
                FounderSection.SECURITY -> canManageSecurity
            }
        }
    }

    if (showFullConsole && canOpenFullConsole) {
        SuperAdminScreen(
            workspaceRecordsState = workspaceRecordsState,
            auditLogsState = auditLogsState,
            settingsState = settingsState,
            registeredUsersState = registeredUsersState,
            inquiriesState = inquiriesState,
            enrollmentsState = enrollmentsState,
            applicationsState = applicationsState,
            isSubmitting = isSubmitting,
            onAddWorkspaceRecord = onAddWorkspaceRecord,
            onUpdateRecordStatus = onUpdateRecordStatus,
            onDeleteRecord = onDeleteRecord,
            onUpdateSettings = onUpdateSettings,
            onNotify = onNotify,
            activeRole = activeRole,
            onBackToFounderDashboard = { showFullConsole = false },
            modifier = modifier
        )
        return
    }

    val records = (workspaceRecordsState as? UiState.Success)?.data ?: emptyList()
    val auditLogs = (auditLogsState as? UiState.Success)?.data ?: emptyList()
    val settings = (settingsState as? UiState.Success)?.data ?: SystemSettingsDoc()
    val portalConfig = (publicPortalState as? UiState.Success)?.data ?: PublicPortalConfig()
    val registeredUsers = (registeredUsersState as? UiState.Success)?.data ?: emptyList()
    val publications = (publicationsState as? UiState.Success)?.data ?: emptyList()
    val appReleases = (appReleasesState as? UiState.Success)?.data ?: emptyList()
    val inquiries = (inquiriesState as? UiState.Success)?.data ?: emptyList()
    val enrollments = (enrollmentsState as? UiState.Success)?.data ?: emptyList()

    val userRecords = remember(records) { records.filter { it.moduleEnum == WorkspaceModuleType.USER } }
    val courseRecords = remember(records) { records.filter { it.moduleEnum == WorkspaceModuleType.COURSE } }
    val paymentRecords = remember(records) { records.filter { it.moduleEnum == WorkspaceModuleType.PAYMENT } }
    val supportRecords = remember(records) { records.filter { it.moduleEnum == WorkspaceModuleType.SUPPORT } }
    val paidRevenueDollars = remember(paymentRecords) {
        paymentRecords.filter { it.status == "PAID" }.sumOf { it.amountCents } / 100L
    }
    val pendingUsersCount = remember(registeredUsers, userRecords) {
        registeredUsers.count { it.statusEnum == MembershipStatus.PENDING } +
            userRecords.count { it.status == "PENDING" }
    }

    val navDestinations = remember(
        visibleChips,
        selectedSection,
        registeredUsers.size,
        userRecords.size,
        pendingUsersCount,
        courseRecords.size,
        enrollments.size,
        inquiries.size,
        auditLogs.size,
        publications.size,
        appReleases.size,
        portalConfig.portalPublished
    ) {
        visibleChips.filter { it != FounderSection.ALL }.map { section ->
            val toggleClick = {
                selectedSection = if (selectedSection == section) FounderSection.ALL else section
            }
            when (section) {
                FounderSection.USERS -> FounderNavDestination(
                    id = "users",
                    title = section.label,
                    subtitle = "Roles, onboarding approvals & accounts",
                    metricLabel = "${registeredUsers.size + userRecords.size} total • $pendingUsersCount pending",
                    icon = section.icon,
                    accent = CodivaCobalt,
                    selected = selectedSection == section,
                    onClick = toggleClick
                )
                FounderSection.ACADEMY -> FounderNavDestination(
                    id = "academy",
                    title = section.label,
                    subtitle = "Training tracks, cohorts & enrollments",
                    metricLabel = "${CodivaCatalog.academyPrograms.size + courseRecords.size} courses",
                    icon = section.icon,
                    accent = CodivaSkyBright,
                    selected = selectedSection == section,
                    onClick = toggleClick
                )
                FounderSection.PROJECTS -> FounderNavDestination(
                    id = "projects",
                    title = section.label,
                    subtitle = "Client deliverables & active quote pipeline",
                    metricLabel = "${CodivaCatalog.projects.size + inquiries.size} tracked",
                    icon = section.icon,
                    accent = CodivaAmber,
                    selected = selectedSection == section,
                    onClick = toggleClick
                )
                FounderSection.SECURITY -> FounderNavDestination(
                    id = "security",
                    title = section.label,
                    subtitle = "Security policies, maintenance & trail",
                    metricLabel = "${auditLogs.size} events",
                    icon = section.icon,
                    accent = CodivaViolet,
                    selected = selectedSection == section,
                    onClick = toggleClick
                )
                FounderSection.HOSTING -> FounderNavDestination(
                    id = "hosting",
                    title = section.label,
                    subtitle = "Domain config, invite code & sign-up",
                    metricLabel = if (portalConfig.portalPublished) "Live" else "Paused",
                    icon = section.icon,
                    accent = CodivaEmerald,
                    selected = selectedSection == section,
                    onClick = toggleClick
                )
                FounderSection.PUBLICATIONS -> FounderNavDestination(
                    id = "publications",
                    title = section.label,
                    subtitle = "Announcements, guides & policy docs",
                    metricLabel = "${publications.size} docs",
                    icon = section.icon,
                    accent = CodivaOrange,
                    selected = selectedSection == section,
                    onClick = toggleClick
                )
                FounderSection.RELEASES -> FounderNavDestination(
                    id = "releases",
                    title = section.label,
                    subtitle = "Play Store, iOS, Windows & Web",
                    metricLabel = "${appReleases.size} builds",
                    icon = section.icon,
                    accent = CodivaCobalt,
                    selected = selectedSection == section,
                    onClick = toggleClick
                )
                FounderSection.ALL -> error("Filtered above")
            }
        }
    }

    fun exportCsvReport() {
        val csv = buildString {
            appendLine("Section,Metric,Value")
            appendLine("Portal,Domain,${portalConfig.customDomain}")
            appendLine("Portal,Public URL,${portalConfig.publicUrl}")
            appendLine("Users,Registered Accounts,${registeredUsers.size}")
            appendLine("Users,Pending Approvals,$pendingUsersCount")
            appendLine("Academy,Enrollments,${enrollments.size}")
            appendLine("Projects,Client Inquiries,${inquiries.size}")
            appendLine("Finance,Paid Revenue USD,$paidRevenueDollars")
            appendLine("Publishing,Documents & Bulletins,${publications.size}")
            appendLine("Releases,Store & Web Releases,${appReleases.size}")
            appendLine("Support,Open Tickets,${supportRecords.count { it.status == "OPEN" }}")
            appendLine("Security,Audit Events,${auditLogs.size}")
        }
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_SUBJECT, "CODIVA Technologies Platform Report")
            putExtra(Intent.EXTRA_TEXT, csv)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Export CODIVA CSV Report"))
        onNotify("CSV summary ready to share/save.")
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("founder_dashboard_list"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            FounderCommandHeader(
                userCount = registeredUsers.size,
                pendingCount = pendingUsersCount,
                academyCount = CodivaCatalog.academyPrograms.size + courseRecords.size,
                projectCount = CodivaCatalog.projects.size + inquiries.size,
                publicationCount = publications.count { it.statusEnum == PublicationStatus.PUBLISHED },
                ticketCount = supportRecords.count { it.status == "OPEN" },
                securityEventsCount = auditLogs.size,
                revenueDollars = paidRevenueDollars,
                maintenanceMode = settings.maintenanceMode,
                onOpenFullConsole = { showFullConsole = true }
            )
        }

        item {
            Row(
                modifier = Modifier
                    .widthIn(max = 920.dp)
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .testTag("founder_section_chips"),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                visibleChips.forEach { section ->
                    FilterChip(
                        selected = selectedSection == section,
                        onClick = { selectedSection = section },
                        leadingIcon = {
                            Icon(
                                imageVector = section.icon,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        label = { Text(section.label) },
                        modifier = Modifier.testTag("founder_chip_${section.name.lowercase()}")
                    )
                }
                if (canOpenFullConsole) {
                    OutlinedButton(
                        onClick = { showFullConsole = true },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("founder_open_full_console")
                    ) {
                        Icon(Icons.Rounded.Tune, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(stringResource(R.string.founder_full_console))
                    }
                }
            }
        }

        item {
            FounderNavigationHub(destinations = navDestinations)
        }

        if (workspaceRecordsState is UiState.Error) {
            item {
                ErrorBanner(
                    message = workspaceRecordsState.message,
                    modifier = Modifier.widthIn(max = 920.dp)
                )
            }
        }

        // User Management Section
        if (canManageUsers && (selectedSection == FounderSection.ALL || selectedSection == FounderSection.USERS)) {
            item {
                ModuleHeaderRow(
                    eyebrow = "Access & Roles",
                    title = "${stringResource(R.string.section_user_management)} (${registeredUsers.size + userRecords.size})",
                    buttonLabel = "Add User",
                    onAdd = { addSheetModule = WorkspaceModuleType.USER },
                    modifier = Modifier.testTag("founder_section_users")
                )
            }
            if (selectedSection == FounderSection.USERS) {
                item {
                    Column(
                        modifier = Modifier
                            .widthIn(max = 920.dp)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = userSearchQuery,
                            onValueChange = { userSearchQuery = it },
                            label = { Text("Search users by name, email, or role") },
                            leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("founder_user_search")
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = userStatusFilter == null,
                                onClick = { userStatusFilter = null },
                                label = { Text("All Statuses") }
                            )
                            MembershipStatus.entries.forEach { status ->
                                FilterChip(
                                    selected = userStatusFilter == status,
                                    onClick = { userStatusFilter = status },
                                    label = { Text(status.label) }
                                )
                            }
                        }
                    }
                }
            }
            val filteredRegistered = registeredUsers.filter { user ->
                val matchesQuery = selectedSection != FounderSection.USERS || userSearchQuery.isBlank() ||
                    user.displayName.contains(userSearchQuery, ignoreCase = true) ||
                    user.email.contains(userSearchQuery, ignoreCase = true) ||
                    user.roleEnum.label.contains(userSearchQuery, ignoreCase = true)
                val matchesStatus = selectedSection != FounderSection.USERS ||
                    userStatusFilter == null ||
                    user.statusEnum == userStatusFilter
                matchesQuery && matchesStatus
            }
            val filteredUserRecords = userRecords.filter { record ->
                val matchesQuery = selectedSection != FounderSection.USERS || userSearchQuery.isBlank() ||
                    record.title.contains(userSearchQuery, ignoreCase = true) ||
                    record.subtitle.contains(userSearchQuery, ignoreCase = true) ||
                    record.category.contains(userSearchQuery, ignoreCase = true)
                val matchesStatus = selectedSection != FounderSection.USERS ||
                    userStatusFilter == null ||
                    record.status.equals(userStatusFilter?.name, ignoreCase = true)
                matchesQuery && matchesStatus
            }

            if (filteredRegistered.isEmpty() && filteredUserRecords.isEmpty()) {
                item {
                    FounderEmptyStateCard(
                        icon = Icons.Rounded.Groups,
                        title = "No matching user accounts",
                        message = if (userSearchQuery.isNotBlank() || userStatusFilter != null) {
                            "Clear or adjust your search filter to view all registered accounts."
                        } else {
                            "Add a staff, lecturer, member, or student profile to manage access permissions."
                        },
                        actionLabel = if (userSearchQuery.isNotBlank() || userStatusFilter != null) "Clear Filters" else "Add User",
                        onAction = {
                            if (userSearchQuery.isNotBlank() || userStatusFilter != null) {
                                userSearchQuery = ""
                                userStatusFilter = null
                            } else {
                                addSheetModule = WorkspaceModuleType.USER
                            }
                        }
                    )
                }
            } else {
                val displayedRegistered = if (selectedSection == FounderSection.ALL) filteredRegistered.take(3) else filteredRegistered
                items(displayedRegistered, key = { "reg_${it.id}" }) { regUser ->
                    RegisteredUserAccountCard(
                        user = regUser,
                        isSubmitting = isSubmitting,
                        onAssignRole = { newRole ->
                            onAdminUpdateRegisteredUser(regUser, newRole, regUser.statusEnum)
                        },
                        onChangeStatus = { newStatus ->
                            onAdminUpdateRegisteredUser(regUser, regUser.roleEnum, newStatus)
                        }
                    )
                }
                val displayedUsers = if (selectedSection == FounderSection.ALL) filteredUserRecords.take(3) else filteredUserRecords
                items(displayedUsers, key = { "user_${it.id}" }) { record ->
                    WorkspaceRecordCard(
                        record = record,
                        primaryActionLabel = if (record.status == "PENDING") "Approve" else "Suspend",
                        onPrimaryAction = {
                            onUpdateRecordStatus(record, if (record.status == "PENDING") "ACTIVE" else "PENDING")
                        },
                        onDelete = { onDeleteRecord(record) },
                        isSubmitting = isSubmitting
                    )
                }
                if (selectedSection == FounderSection.ALL && (userRecords.size > 3 || registeredUsers.size > 3)) {
                    item {
                        TextButton(
                            onClick = { selectedSection = FounderSection.USERS },
                            modifier = Modifier.minimumInteractiveComponentSize()
                        ) {
                            Text("View all ${registeredUsers.size + userRecords.size} users")
                        }
                    }
                }
            }
        }

        // Academy Courses Section
        if (canManageAcademy && (selectedSection == FounderSection.ALL || selectedSection == FounderSection.ACADEMY)) {
            item {
                ModuleHeaderRow(
                    eyebrow = "Training & Certifications",
                    title = "${stringResource(R.string.section_academy_management)} (${CodivaCatalog.academyPrograms.size + courseRecords.size})",
                    buttonLabel = "Add Course",
                    onAdd = { addSheetModule = WorkspaceModuleType.COURSE },
                    modifier = Modifier.testTag("founder_section_academy")
                )
            }
            item {
                Card(
                    modifier = Modifier.widthIn(max = 920.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text("Active Academy Enrollments", style = MaterialTheme.typography.titleSmall)
                            Text(
                                "${enrollments.size} learner enrollment(s) synced • ${enrollments.count { it.progressPercent >= 100L }} completed",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        StatusBadge("${CodivaCatalog.academyPrograms.size} core tracks", BadgeTone.INFO)
                    }
                }
            }
            val displayedPrograms = if (selectedSection == FounderSection.ALL) {
                CodivaCatalog.academyPrograms.take(2)
            } else {
                CodivaCatalog.academyPrograms
            }
            items(displayedPrograms, key = { "prog_${it.id}" }) { prog ->
                Card(
                    modifier = Modifier.widthIn(max = 920.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(prog.title, style = MaterialTheme.typography.titleMedium)
                            Text(
                                "${prog.level} • ${prog.duration}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        StatusBadge(prog.programType, BadgeTone.INFO)
                    }
                }
            }
            if (courseRecords.isEmpty() && selectedSection == FounderSection.ACADEMY) {
                item {
                    FounderEmptyStateCard(
                        icon = Icons.Rounded.School,
                        title = "No custom courses added yet",
                        message = "Create specialized Academy cohorts, workshops, or certification tracks to publish alongside the core curriculum.",
                        actionLabel = "Add Course",
                        onAction = { addSheetModule = WorkspaceModuleType.COURSE }
                    )
                }
            } else {
                items(courseRecords, key = { "course_${it.id}" }) { record ->
                    WorkspaceRecordCard(
                        record = record,
                        primaryActionLabel = if (record.status == "ACTIVE") "Archive" else "Activate",
                        onPrimaryAction = {
                            onUpdateRecordStatus(record, if (record.status == "ACTIVE") "ARCHIVED" else "ACTIVE")
                        },
                        onDelete = { onDeleteRecord(record) },
                        isSubmitting = isSubmitting
                    )
                }
            }
        }

        // Project Tracking Section
        if (canManageProjects && (selectedSection == FounderSection.ALL || selectedSection == FounderSection.PROJECTS)) {
            item {
                ModuleHeaderRow(
                    eyebrow = "Delivery & Client Inquiries",
                    title = "${stringResource(R.string.section_project_management)} (${CodivaCatalog.projects.size + inquiries.size})",
                    buttonLabel = "New Project",
                    onAdd = { onRequestQuote(CodivaCatalog.services.first().title) },
                    modifier = Modifier.testTag("founder_section_projects")
                )
            }
            if (selectedSection == FounderSection.PROJECTS) {
                item {
                    Row(
                        modifier = Modifier
                            .widthIn(max = 920.dp)
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(null to "All Projects", "In Progress" to "In Progress", "Completed" to "Completed").forEach { (statusValue, label) ->
                            FilterChip(
                                selected = projectStatusFilter == statusValue,
                                onClick = { projectStatusFilter = statusValue },
                                label = { Text(label) }
                            )
                        }
                    }
                }
            }
            if (inquiries.isNotEmpty() && (selectedSection != FounderSection.PROJECTS || projectStatusFilter != "Completed")) {
                val displayedInquiries = if (selectedSection == FounderSection.ALL) inquiries.take(2) else inquiries
                items(displayedInquiries, key = { "inq_${it.id}" }) { inquiry ->
                    Card(
                        modifier = Modifier.widthIn(max = 920.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Text(inquiry.serviceCategory, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                Text(inquiry.projectTitle, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    "${inquiry.budgetTier} • ${inquiry.details}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                StatusBadge(
                                    text = inquiry.statusEnum.label,
                                    tone = if (inquiry.statusEnum == InquiryStatus.SUBMITTED) BadgeTone.INFO else BadgeTone.NEUTRAL
                                )
                                if (inquiry.statusEnum == InquiryStatus.SUBMITTED) {
                                    TextButton(
                                        onClick = { onCancelInquiry(inquiry.id) },
                                        enabled = !isSubmitting,
                                        modifier = Modifier.minimumInteractiveComponentSize()
                                    ) {
                                        Text("Cancel")
                                    }
                                }
                            }
                        }
                    }
                }
            }
            val filteredCatalogProjects = CodivaCatalog.projects.filter { proj ->
                selectedSection != FounderSection.PROJECTS ||
                    projectStatusFilter == null ||
                    proj.status.equals(projectStatusFilter, ignoreCase = true)
            }
            val displayedProjects = if (selectedSection == FounderSection.ALL) {
                filteredCatalogProjects.take(3)
            } else {
                filteredCatalogProjects
            }
            if (displayedProjects.isEmpty() && inquiries.isEmpty()) {
                item {
                    FounderEmptyStateCard(
                        icon = Icons.Rounded.WorkOutline,
                        title = "No tracked projects match this filter",
                        message = "Switch the project filter or create a new client engagement quote.",
                        actionLabel = "Show All",
                        onAction = { projectStatusFilter = null }
                    )
                }
            } else {
                items(displayedProjects, key = { "proj_${it.id}" }) { proj ->
                    Card(
                        modifier = Modifier.widthIn(max = 920.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(proj.title, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    "${proj.category} • ${proj.summary}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            StatusBadge(
                                proj.status,
                                if (proj.status == "Completed") BadgeTone.SUCCESS else BadgeTone.WARNING
                            )
                        }
                    }
                }
            }
        }

        // System Audit Logs & Security Governance Section
        if (canManageSecurity && (selectedSection == FounderSection.ALL || selectedSection == FounderSection.SECURITY)) {
            item {
                Column(
                    modifier = Modifier
                        .widthIn(max = 920.dp)
                        .testTag("founder_section_security"),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SectionHeader(
                        eyebrow = "Governance & Audit Trail",
                        title = "${stringResource(R.string.section_security_management)} (${auditLogs.size})",
                        actionLabel = if (selectedSection == FounderSection.ALL) "All Logs" else null,
                        onActionClick = if (selectedSection == FounderSection.ALL) {
                            { selectedSection = FounderSection.SECURITY }
                        } else null
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatusBadge("Google Auth: Verified", BadgeTone.SUCCESS)
                        StatusBadge("Firestore Rules: Enforced", BadgeTone.SUCCESS)
                        StatusBadge(
                            text = if (settings.maintenanceMode) "Maintenance: ON" else "Platform: Online",
                            tone = if (settings.maintenanceMode) BadgeTone.WARNING else BadgeTone.SUCCESS
                        )
                    }
                    SettingToggleCard(
                        title = "Security Audit Logging",
                        description = "Record administrative updates into the append-only Firestore audit trail.",
                        checked = settings.auditLoggingEnabled,
                        enabled = !isSubmitting,
                        onCheckedChange = { onUpdateSettings(settings.copy(auditLoggingEnabled = it)) }
                    )
                    SettingToggleCard(
                        title = "Auto-approve Member Accounts",
                        description = "Automatically activate verified new member profiles on sign-in.",
                        checked = settings.autoApproveMembers,
                        enabled = !isSubmitting,
                        onCheckedChange = { onUpdateSettings(settings.copy(autoApproveMembers = it)) }
                    )
                    SettingToggleCard(
                        title = "Maintenance Mode",
                        description = "Pause public service submissions during scheduled system upgrades.",
                        checked = settings.maintenanceMode,
                        enabled = !isSubmitting,
                        onCheckedChange = { onUpdateSettings(settings.copy(maintenanceMode = it)) }
                    )
                    if (selectedSection == FounderSection.SECURITY) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = auditSeverityFilter == null,
                                onClick = { auditSeverityFilter = null },
                                label = { Text("All Severities") }
                            )
                            AuditSeverity.entries.forEach { severity ->
                                FilterChip(
                                    selected = auditSeverityFilter == severity,
                                    onClick = { auditSeverityFilter = severity },
                                    label = { Text(severity.label) }
                                )
                            }
                        }
                    }
                }
            }
            val filteredLogs = auditLogs.filter { log ->
                selectedSection != FounderSection.SECURITY ||
                    auditSeverityFilter == null ||
                    log.severityEnum == auditSeverityFilter
            }
            val displayedLogs = if (selectedSection == FounderSection.ALL) filteredLogs.take(4) else filteredLogs
            if (displayedLogs.isEmpty()) {
                item {
                    FounderEmptyStateCard(
                        icon = Icons.Rounded.Security,
                        title = "No audit log entries",
                        message = if (auditSeverityFilter != null) {
                            "No security events recorded for the selected severity filter."
                        } else {
                            "Administrative role updates, policy changes, and account approvals will appear here."
                        },
                        actionLabel = if (auditSeverityFilter != null) "Show All Logs" else null,
                        onAction = if (auditSeverityFilter != null) {
                            { auditSeverityFilter = null }
                        } else null
                    )
                }
            } else {
                items(displayedLogs, key = { "audit_${it.id}" }) { log ->
                    AuditLogCard(log = log, modifier = Modifier.widthIn(max = 920.dp))
                }
            }
        }

        // Public Hosting & Online Onboarding Section
        if (canManageHosting && (selectedSection == FounderSection.ALL || selectedSection == FounderSection.HOSTING)) {
            item {
                Column(
                    modifier = Modifier
                        .widthIn(max = 920.dp)
                        .testTag("founder_section_hosting"),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SectionHeader(
                        eyebrow = "Online Hosting & Onboarding",
                        title = "Public Portal & Account Sign-Up",
                        actionLabel = "Configure",
                        onActionClick = { showHostingSheet = true }
                    )
                    HostingLaunchCard(
                        config = portalConfig,
                        isSubmitting = isSubmitting,
                        onCopyUrl = {
                            clipboardManager.setText(AnnotatedString(portalConfig.publicUrl))
                            onNotify("Public portal URL copied to clipboard.")
                        },
                        onShareInvite = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "Join ${portalConfig.portalTitle}")
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "Join ${portalConfig.portalTitle} online and create your account:\n${portalConfig.publicUrl}\nInvite Code: ${portalConfig.inviteCode}"
                                )
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share CODIVA Portal"))
                        },
                        onOpenLiveUrl = {
                            try {
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(portalConfig.publicUrl)))
                            } catch (_: Exception) {
                                onNotify("Unable to open URL in browser.")
                            }
                        },
                        onEditHosting = { showHostingSheet = true },
                        onTogglePublished = { published ->
                            onUpdatePublicPortalConfig(portalConfig.copy(portalPublished = published)) {}
                        },
                        onTogglePublicSignUp = { allow ->
                            onUpdatePublicPortalConfig(portalConfig.copy(allowPublicSignUp = allow)) {}
                        },
                        onToggleRequireApproval = { req ->
                            onUpdatePublicPortalConfig(portalConfig.copy(requireAdminApproval = req)) {}
                        }
                    )
                }
            }
        }

        // Official Publications & Document Broadcast Database
        if (canManagePublications && (selectedSection == FounderSection.ALL || selectedSection == FounderSection.PUBLICATIONS)) {
            item {
                ModuleHeaderRow(
                    eyebrow = "Admin Broadcast Database",
                    title = "Publications & Documents (${publications.size})",
                    buttonLabel = "Post Info / Doc",
                    onAdd = {
                        editingPublication = null
                        showPublicationEditor = true
                    },
                    modifier = Modifier.testTag("founder_section_publications")
                )
            }
            if (publications.isEmpty()) {
                item {
                    FounderEmptyStateCard(
                        icon = Icons.Rounded.Description,
                        title = "No publications posted yet",
                        message = "Post official announcements, handbooks, or document links to appear across user dashboards.",
                        actionLabel = "Post Info / Doc",
                        onAction = {
                            editingPublication = null
                            showPublicationEditor = true
                        }
                    )
                }
            } else {
                val displayedPubs = if (selectedSection == FounderSection.ALL) publications.take(3) else publications
                items(displayedPubs, key = { "pub_${it.id}" }) { pub ->
                    AdminPublicationCard(
                        publication = pub,
                        isAdminMode = true,
                        isSubmitting = isSubmitting,
                        onReadMore = { readingPublication = pub },
                        onOpenDocument = { url ->
                            try {
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                            } catch (_: Exception) {
                                onNotify("Unable to open document URL.")
                            }
                        },
                        onEdit = {
                            editingPublication = pub
                            showPublicationEditor = true
                        },
                        onChangeStatus = { nextStatus ->
                            onUpdatePublicationStatus(pub, nextStatus)
                        },
                        onDelete = { onDeletePublication(pub) }
                    )
                }
                if (selectedSection == FounderSection.ALL && publications.size > 3) {
                    item {
                        TextButton(
                            onClick = { selectedSection = FounderSection.PUBLICATIONS },
                            modifier = Modifier.minimumInteractiveComponentSize()
                        ) {
                            Text("Manage all ${publications.size} publications & documents")
                        }
                    }
                }
            }
        }

        // Multi-Platform App Downloads & Store Releases Database
        if (canManageReleases && (selectedSection == FounderSection.ALL || selectedSection == FounderSection.RELEASES)) {
            item {
                ModuleHeaderRow(
                    eyebrow = "App Store, Play Store, Windows & Web",
                    title = "App Downloads & Releases (${appReleases.size})",
                    buttonLabel = "Add Release",
                    onAdd = {
                        editingRelease = null
                        showReleaseEditor = true
                    },
                    modifier = Modifier.testTag("founder_section_releases")
                )
            }
            item {
                ProductionPipelineCard(
                    releaseCount = appReleases.count { it.statusEnum != ReleaseStatus.RETIRED },
                    onOpenInstallGuide = { showInstallPreview = true },
                    onOpenLegalDocs = { showLegalDocs = true }
                )
            }
            if (appReleases.isEmpty()) {
                item {
                    FounderEmptyStateCard(
                        icon = Icons.Rounded.InstallMobile,
                        title = "No app releases configured",
                        message = "Register Android Play Store, iOS App Store, Windows, or Web builds for users to download.",
                        actionLabel = "Add Release",
                        onAction = {
                            editingRelease = null
                            showReleaseEditor = true
                        }
                    )
                }
            } else {
                val displayedReleases = if (selectedSection == FounderSection.ALL) appReleases.take(2) else appReleases
                items(displayedReleases, key = { "rel_${it.id}" }) { rel ->
                    AppReleaseCard(
                        release = rel,
                        isAdminMode = true,
                        isSubmitting = isSubmitting,
                        onOpenDownload = { url ->
                            try {
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                            } catch (_: Exception) {
                                onNotify("Unable to open download URL.")
                            }
                        },
                        onEdit = {
                            editingRelease = rel
                            showReleaseEditor = true
                        },
                        onChangeStatus = { nextStatus ->
                            onUpdateAppReleaseStatus(rel, nextStatus)
                        },
                        onDelete = { onDeleteAppRelease(rel) }
                    )
                }
                if (selectedSection == FounderSection.ALL && appReleases.size > 2) {
                    item {
                        TextButton(
                            onClick = { selectedSection = FounderSection.RELEASES },
                            modifier = Modifier.minimumInteractiveComponentSize()
                        ) {
                            Text("Manage all ${appReleases.size} platform releases")
                        }
                    }
                }
            }
        }

        // Executive Analytics Snapshot in Core Overview (Real Firestore Database Metrics)
        if (canViewAnalytics && selectedSection == FounderSection.ALL) {
            item {
                val activeUsersCount = registeredUsers.count { it.statusEnum == MembershipStatus.ACTIVE }
                val studentUserCount = (registeredUsers.count {
                    it.roleEnum == AccountRole.USER || it.roleEnum == AccountRole.PARENT
                } + enrollments.size).toFloat()
                val memberPartnerCount = registeredUsers.count {
                    it.roleEnum == AccountRole.MEMBER || it.roleEnum == AccountRole.PARTNER
                }.toFloat()
                val staffLecturerCount = registeredUsers.count {
                    it.roleEnum == AccountRole.STAFF || it.roleEnum == AccountRole.LECTURER
                }.toFloat()
                val adminHodFinanceCount = registeredUsers.count {
                    it.roleEnum == AccountRole.SUPER_ADMIN || it.roleEnum == AccountRole.HOD || it.roleEnum == AccountRole.FINANCE
                }.toFloat()

                Column(
                    modifier = Modifier.widthIn(max = 920.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SectionHeader(
                        eyebrow = "Live Database Insights",
                        title = "Real Firestore Metrics & CSV Export",
                        actionLabel = "Full Console",
                        onActionClick = { showFullConsole = true }
                    )
                    OutlinedButton(
                        onClick = { exportCsvReport() },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("founder_export_csv_button")
                    ) {
                        Icon(Icons.Rounded.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Export Authorized CSV Report")
                    }
                    ExecutiveKpiOverviewRow(
                        registeredUsers = registeredUsers,
                        auditLogs = auditLogs,
                        paymentRecords = paymentRecords,
                        supportRecords = supportRecords
                    )
                    PlatformDistributionBarCard(appReleases = appReleases)
                    ActivityHeatmapAndCalendarRow(
                        registeredUsers = registeredUsers,
                        auditLogs = auditLogs,
                        inquiries = inquiries,
                        enrollments = enrollments,
                        publications = publications
                    )
                    PlatformAnalyticsChartCard(
                        visitorsSeries = listOf(
                            registeredUsers.size.toFloat(),
                            activeUsersCount.toFloat(),
                            publications.size.toFloat(),
                            appReleases.size.toFloat()
                        ),
                        enrollmentsSeries = listOf(
                            enrollments.size.toFloat(),
                            enrollments.count { it.progressPercent >= 50L }.toFloat(),
                            enrollments.count { it.progressPercent >= 100L }.toFloat(),
                            courseRecords.size.toFloat()
                        ),
                        paymentsSeries = listOf(
                            paymentRecords.size.toFloat(),
                            paymentRecords.count { it.status == "PAID" }.toFloat(),
                            inquiries.size.toFloat(),
                            auditLogs.size.toFloat()
                        ),
                        labels = listOf("Registered", "Active", "Published", "Synced"),
                        timeframeLabel = "Real Firestore Totals",
                        seriesLabels = Triple("Accounts & Docs", "Academy", "Activity & Logs")
                    )
                    UserRolesDonutCard(
                        slices = listOf(
                            Triple("Students & Parents", studentUserCount.coerceAtLeast(0f), CodivaCobalt),
                            Triple("Members & Partners", memberPartnerCount.coerceAtLeast(0f), CodivaEmerald),
                            Triple("Staff & Lecturers", staffLecturerCount.coerceAtLeast(0f), CodivaAmber),
                            Triple("Admins, HOD & Finance", adminHodFinanceCount.coerceAtLeast(0f), CodivaViolet)
                        ),
                        totalLabel = "${registeredUsers.size}"
                    )
                }
            }
        }
    }

    addSheetModule?.let { moduleType ->
        AddWorkspaceRecordSheet(
            moduleType = moduleType,
            isSubmitting = isSubmitting,
            onDismiss = { addSheetModule = null },
            onSubmit = { title, subtitle, detail, category, status, cents ->
                onAddWorkspaceRecord(moduleType, title, subtitle, detail, category, status, cents) {
                    addSheetModule = null
                }
            }
        )
    }

    if (showHostingSheet) {
        HostingSettingsSheet(
            initialConfig = portalConfig,
            isSubmitting = isSubmitting,
            onDismiss = { showHostingSheet = false },
            onSave = { updatedConfig ->
                onUpdatePublicPortalConfig(updatedConfig) {
                    showHostingSheet = false
                }
            }
        )
    }

    if (showPublicationEditor) {
        PublicationEditorSheet(
            existing = editingPublication,
            isSubmitting = isSubmitting,
            onDismiss = { showPublicationEditor = false },
            onSave = { existingId, title, summary, content, category, targetRole, docUrl, docLabel, expiresAt, status ->
                onSavePublication(existingId, title, summary, content, category, targetRole, docUrl, docLabel, expiresAt, status) {
                    showPublicationEditor = false
                    editingPublication = null
                }
            }
        )
    }

    readingPublication?.let { pub ->
        PublicationReaderSheet(
            publication = pub,
            onOpenDocument = { url ->
                try {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                } catch (_: Exception) {
                    onNotify("Unable to open document link.")
                }
            },
            onDismiss = { readingPublication = null }
        )
    }

    if (showReleaseEditor) {
        AppReleaseEditorSheet(
            existing = editingRelease,
            isSubmitting = isSubmitting,
            onDismiss = { showReleaseEditor = false },
            onSave = { existingId, platform, versionName, buildNumber, notes, url, status ->
                onSaveAppRelease(existingId, platform, versionName, buildNumber, notes, url, status) {
                    showReleaseEditor = false
                    editingRelease = null
                }
            }
        )
    }

    if (showInstallPreview) {
        InstallPlatformSheet(
            appReleases = appReleases,
            portalUrl = portalConfig.publicUrl.ifBlank { CodivaCatalog.PUBLIC_PORTAL_URL },
            onNotify = onNotify,
            onDismiss = { showInstallPreview = false }
        )
    }

    if (showLegalDocs) {
        LegalDocumentsSheet(onDismiss = { showLegalDocs = false })
    }
}

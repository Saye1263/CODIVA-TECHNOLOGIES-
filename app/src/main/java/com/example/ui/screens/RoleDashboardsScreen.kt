package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AdminPanelSettings
import androidx.compose.material.icons.rounded.Apartment
import androidx.compose.material.icons.rounded.AttachMoney
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.FamilyRestroom
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Handshake
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Work
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.AccountRole
import com.example.data.AdminPublication
import com.example.data.AppRelease
import com.example.data.AuditLogEntry
import com.example.data.CareerApplication
import com.example.data.CodivaCatalog
import com.example.data.CourseEnrollment
import com.example.data.DashboardSection
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
import com.example.ui.components.AddWorkspaceRecordSheet
import com.example.ui.components.AdminPublicationCard
import com.example.ui.components.BadgeTone
import com.example.ui.components.MaintenanceNoticeBanner
import com.example.ui.components.PublicationReaderSheet
import com.example.ui.components.RoleSectionGate
import com.example.ui.components.SectionHeader
import com.example.ui.components.StatusBadge
import com.example.ui.theme.CodivaEmerald

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EcosystemScreen(
    fallbackName: String,
    fallbackEmail: String,
    profileState: UiState<UserProfile?>,
    inquiriesState: UiState<List<ProjectInquiry>>,
    enrollmentsState: UiState<List<CourseEnrollment>>,
    applicationsState: UiState<List<CareerApplication>>,
    workspaceRecordsState: UiState<List<WorkspaceRecord>>,
    auditLogsState: UiState<List<AuditLogEntry>>,
    settingsState: UiState<SystemSettingsDoc?>,
    publicPortalState: UiState<PublicPortalConfig?>,
    registeredUsersState: UiState<List<UserProfile>>,
    publicationsState: UiState<List<AdminPublication>>,
    appReleasesState: UiState<List<AppRelease>>,
    isSubmitting: Boolean,
    onSwitchRole: (AccountRole) -> Unit,
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
    onOpenAcademy: () -> Unit,
    onRequestQuote: (String) -> Unit,
    onCancelInquiry: (String) -> Unit,
    roleManager: UserRoleManager = UserRoleManager.fromProfile((profileState as? UiState.Success)?.data, fallbackEmail),
    modifier: Modifier = Modifier
) {
    val profile = (profileState as? UiState.Success)?.data
    val effectiveEmail = profile?.email?.ifBlank { fallbackEmail } ?: fallbackEmail
    val activeRole = roleManager.activeRole
    val membershipStatus = roleManager.membershipStatus
    val portalConfig = (publicPortalState as? UiState.Success)?.data ?: PublicPortalConfig()
    val canViewFounder = roleManager.canAccessFounderDashboard()
    var topSegment by remember { mutableIntStateOf(if (canViewFounder) 1 else 0) }
    val segments = listOf("Role Dashboards", "Founder Dashboard")

    LaunchedEffect(canViewFounder) {
        if (!canViewFounder && topSegment == 1) {
            topSegment = 0
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        if (canViewFounder) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier
                        .widthIn(max = 920.dp)
                        .fillMaxWidth()
                        .testTag("ecosystem_mode_bar")
                ) {
                    segments.forEachIndexed { index, label ->
                        SegmentedButton(
                            selected = topSegment == index,
                            onClick = { topSegment = index },
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = segments.size),
                            modifier = Modifier.testTag("ecosystem_seg_$index")
                        ) {
                            Text(label)
                        }
                    }
                }
            }
        }

        if (canViewFounder && topSegment == 1) {
            FounderDashboard(
                workspaceRecordsState = workspaceRecordsState,
                auditLogsState = auditLogsState,
                settingsState = settingsState,
                publicPortalState = publicPortalState,
                registeredUsersState = registeredUsersState,
                publicationsState = publicationsState,
                appReleasesState = appReleasesState,
                inquiriesState = inquiriesState,
                enrollmentsState = enrollmentsState,
                applicationsState = applicationsState,
                isSubmitting = isSubmitting,
                onAddWorkspaceRecord = onAddWorkspaceRecord,
                onUpdateRecordStatus = onUpdateRecordStatus,
                onDeleteRecord = onDeleteRecord,
                onUpdateSettings = onUpdateSettings,
                onUpdatePublicPortalConfig = onUpdatePublicPortalConfig,
                onAdminUpdateRegisteredUser = onAdminUpdateRegisteredUser,
                onSavePublication = onSavePublication,
                onUpdatePublicationStatus = onUpdatePublicationStatus,
                onDeletePublication = onDeletePublication,
                onSaveAppRelease = onSaveAppRelease,
                onUpdateAppReleaseStatus = onUpdateAppReleaseStatus,
                onDeleteAppRelease = onDeleteAppRelease,
                onNotify = onNotify,
                onRequestQuote = onRequestQuote,
                onCancelInquiry = onCancelInquiry,
                activeRole = activeRole,
                onBackToRoles = { topSegment = 0 }
            )
        } else {
            val settings = (settingsState as? UiState.Success)?.data ?: SystemSettingsDoc()
            RoleDashboardsBody(
                fallbackName = fallbackName,
                roleManager = roleManager,
                activeRole = activeRole,
                membershipStatus = membershipStatus,
                portalPublished = portalConfig.portalPublished,
                maintenanceBannerVisible = roleManager.shouldShowMaintenanceBanner(settings),
                inquiriesState = inquiriesState,
                enrollmentsState = enrollmentsState,
                workspaceRecordsState = workspaceRecordsState,
                publicationsState = publicationsState,
                isSubmitting = isSubmitting,
                onSelectRole = { role ->
                    onSwitchRole(role)
                    if (UserRoleManager.canAccessFounderDashboard(role)) {
                        topSegment = 1
                    }
                },
                onAddWorkspaceRecord = onAddWorkspaceRecord,
                onUpdateRecordStatus = onUpdateRecordStatus,
                onOpenAcademy = onOpenAcademy,
                onRequestQuote = onRequestQuote,
                onOpenSuperAdmin = { topSegment = 1 },
                onNotify = onNotify
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RoleDashboardsBody(
    fallbackName: String,
    roleManager: UserRoleManager,
    activeRole: AccountRole,
    membershipStatus: MembershipStatus,
    portalPublished: Boolean,
    maintenanceBannerVisible: Boolean,
    inquiriesState: UiState<List<ProjectInquiry>>,
    enrollmentsState: UiState<List<CourseEnrollment>>,
    workspaceRecordsState: UiState<List<WorkspaceRecord>>,
    publicationsState: UiState<List<AdminPublication>>,
    isSubmitting: Boolean,
    onSelectRole: (AccountRole) -> Unit,
    onAddWorkspaceRecord: (WorkspaceModuleType, String, String, String, String, String, Long, () -> Unit) -> Unit,
    onUpdateRecordStatus: (WorkspaceRecord, String) -> Unit,
    onOpenAcademy: () -> Unit,
    onRequestQuote: (String) -> Unit,
    onOpenSuperAdmin: () -> Unit,
    onNotify: (String) -> Unit
) {
    val context = LocalContext.current
    var addSheetModule by remember { mutableStateOf<WorkspaceModuleType?>(null) }
    var readingPublication by remember { mutableStateOf<AdminPublication?>(null) }
    val assignableRoles = remember(roleManager.userEmail, activeRole) {
        roleManager.assignableRoles()
    }
    val permissionLabels = remember(activeRole, roleManager.membershipStatus) {
        roleManager.permissionLabels()
    }

    val inquiries = (inquiriesState as? UiState.Success)?.data ?: emptyList()
    val enrollments = (enrollmentsState as? UiState.Success)?.data ?: emptyList()
    val records = (workspaceRecordsState as? UiState.Success)?.data ?: emptyList()
    val allPublications = (publicationsState as? UiState.Success)?.data ?: emptyList()
    val rolePublications = remember(allPublications, activeRole, roleManager.membershipStatus) {
        roleManager.filterVisiblePublications(allPublications)
    }

    val tasks = remember(records) { records.filter { it.moduleEnum == WorkspaceModuleType.TASK } }
    val payments = remember(records) { records.filter { it.moduleEnum == WorkspaceModuleType.PAYMENT } }
    val support = remember(records) { records.filter { it.moduleEnum == WorkspaceModuleType.SUPPORT } }
    val courseRecords = remember(records) { records.filter { it.moduleEnum == WorkspaceModuleType.COURSE } }
    val employeeRecords = remember(records) { records.filter { it.moduleEnum == WorkspaceModuleType.EMPLOYEE } }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (maintenanceBannerVisible) {
            item { MaintenanceNoticeBanner() }
        }
        if (activeRole != AccountRole.SUPER_ADMIN && (membershipStatus != MembershipStatus.ACTIVE || !portalPublished)) {
            item {
                Card(
                    modifier = Modifier
                        .widthIn(max = 920.dp)
                        .testTag("role_account_status_banner"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        StatusBadge(
                            text = if (!portalPublished) "PORTAL PRIVATE MODE" else membershipStatus.label.uppercase(),
                            tone = BadgeTone.WARNING
                        )
                        Text(
                            text = when {
                                membershipStatus == MembershipStatus.SUSPENDED ->
                                    "Your account is currently suspended. Contact CODIVA administration to restore full dashboard access."
                                membershipStatus == MembershipStatus.PENDING ->
                                    "Your ${activeRole.label} account is awaiting Super Admin approval. You can preview your workspace while approval completes."
                                else ->
                                    "The public CODIVA portal is currently in private mode while updates are published."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
        }

        // Role Switcher Strip governed by UserRoleManager
        item {
            Column(
                modifier = Modifier.widthIn(max = 920.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SectionHeader(
                    eyebrow = "CODIVA Application Ecosystem",
                    title = "Role-Based Dashboards"
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    assignableRoles.forEach { role ->
                        FilterChip(
                            selected = activeRole == role,
                            onClick = { onSelectRole(role) },
                            leadingIcon = {
                                Icon(
                                    imageVector = roleIcon(role),
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            label = { Text(role.label) },
                            modifier = Modifier.testTag("role_chip_${role.name.lowercase()}")
                        )
                    }
                }
            }
        }

        // Welcome & Active Role Permissions Banner
        item {
            Card(
                modifier = Modifier.widthIn(max = 920.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_codiva_logo),
                            contentDescription = "CODIVA Logo",
                            modifier = Modifier
                                .size(54.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            StatusBadge(text = "${activeRole.label} Dashboard", tone = BadgeTone.INFO)
                            Text(
                                text = "Welcome, $fallbackName",
                                style = MaterialTheme.typography.titleLarge
                            )
                            Text(
                                text = activeRole.tagline,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.testTag("role_permissions_strip")
                    ) {
                        permissionLabels.take(5).forEach { label ->
                            StatusBadge(text = label, tone = BadgeTone.SUCCESS)
                        }
                    }
                }
            }
        }

        // Role-specific Dashboard Content gated by UserRoleManager
        when (activeRole) {
            AccountRole.USER -> {
                if (roleManager.canAccessSection(DashboardSection.MEMBER_COMMUNITY)) {
                    item {
                        RoleMetricGrid(
                            items = listOf(
                                "My Courses" to "${enrollments.size}",
                                "Certificates" to "${enrollments.count { it.progressPercent >= 100L }}",
                                "Messages" to "${support.size}",
                                "Projects" to "${inquiries.size}"
                            )
                        )
                    }
                    item {
                        DashboardActionCard(
                            title = "Enrolled Courses & Progress",
                            body = if (enrollments.isEmpty()) "Join Web Development, Mobile App Development, or Digital Skills." else "You have ${enrollments.size} active enrollment(s) synced in Firestore.",
                            primaryLabel = "Open Academy",
                            onPrimary = onOpenAcademy,
                            secondaryLabel = "Request Project",
                            onSecondary = { onRequestQuote(CodivaCatalog.services.first().title) }
                        )
                    }
                }
            }

            AccountRole.MEMBER -> {
                if (roleManager.canAccessSection(DashboardSection.MEMBER_COMMUNITY)) {
                    item {
                        RoleMetricGrid(
                            items = listOf(
                                "Membership" to membershipStatus.name,
                                "My Projects" to "${inquiries.size}",
                                "Courses" to "${enrollments.size}",
                                "News Items" to "${CodivaCatalog.newsArticles.size}"
                            )
                        )
                    }
                    items(CodivaCatalog.newsArticles.take(2), key = { it.id }) { news ->
                        Card(
                            modifier = Modifier.widthIn(max = 920.dp),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                StatusBadge("Latest News • ${news.publishedDate}", BadgeTone.INFO)
                                Text(news.title, style = MaterialTheme.typography.titleMedium)
                                Text(news.summary, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }

            AccountRole.STAFF -> {
                if (roleManager.canAccessSection(DashboardSection.STAFF_OPERATIONS)) {
                    item {
                        RoleMetricGrid(
                            items = listOf(
                                "My Tasks" to "${tasks.size}",
                                "Projects" to "${CodivaCatalog.projects.size}",
                                "Attendance" to "ON DUTY",
                                "Tickets" to "${support.count { it.status == "OPEN" }}"
                            )
                        )
                    }
                    item {
                        Row(
                            modifier = Modifier
                                .widthIn(max = 920.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Assigned Staff Tasks", style = MaterialTheme.typography.titleMedium)
                            Button(
                                onClick = { addSheetModule = WorkspaceModuleType.TASK },
                                modifier = Modifier.minimumInteractiveComponentSize()
                            ) {
                                Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Add Task")
                            }
                        }
                    }
                    items(tasks, key = { it.id }) { task ->
                        val done = task.status == "COMPLETED"
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
                                    Text(task.title, style = MaterialTheme.typography.titleSmall)
                                    Text(task.subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                TextButton(
                                    onClick = {
                                        onUpdateRecordStatus(task, if (done) "IN_PROGRESS" else "COMPLETED")
                                    },
                                    enabled = !isSubmitting,
                                    modifier = Modifier.minimumInteractiveComponentSize()
                                ) {
                                    Text(if (done) "Completed ✓" else "Mark Done")
                                }
                            }
                        }
                    }
                }
            }

            AccountRole.LECTURER -> {
                if (roleManager.canAccessSection(DashboardSection.LECTURER_COHORTS)) {
                    item {
                        RoleMetricGrid(
                            items = listOf(
                                "Courses" to "${CodivaCatalog.academyPrograms.size + courseRecords.size}",
                                "Enrollments" to "${enrollments.size}",
                                "Completed" to "${enrollments.count { it.progressPercent >= 100L }}",
                                "Bulletins" to "${rolePublications.size}"
                            )
                        )
                    }
                    items(CodivaCatalog.academyPrograms, key = { it.id }) { course ->
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
                                    Text(course.title, style = MaterialTheme.typography.titleMedium)
                                    Text("${course.level} • ${course.duration}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                StatusBadge("Teaching", BadgeTone.SUCCESS)
                            }
                        }
                    }
                }
            }

            AccountRole.PARENT -> {
                if (roleManager.canAccessSection(DashboardSection.PARENT_OVERVIEW)) {
                    val paidTuition = payments.filter { it.status == "PAID" && it.category.equals("Tuition", ignoreCase = true) }
                        .sumOf { it.amountCents } / 100L
                    val dueTuition = payments.filter { it.status == "PENDING" && it.category.equals("Tuition", ignoreCase = true) }
                        .sumOf { it.amountCents } / 100L
                    item {
                        RoleMetricGrid(
                            items = listOf(
                                "Enrollments" to "${enrollments.size}",
                                "Tuition Due" to "$$dueTuition",
                                "Tuition Paid" to "$$paidTuition",
                                "Support" to "${support.size}"
                            )
                        )
                    }
                    item {
                        DashboardActionCard(
                            title = "Student Progress & Tuition Records",
                            body = if (enrollments.isEmpty()) {
                                "No synced student enrollments yet. Visit Academy to enroll or message CODIVA support to link student progress."
                            } else {
                                "Synced enrollments: ${enrollments.size} (${enrollments.count { it.progressPercent >= 100L }} completed)."
                            },
                            primaryLabel = "Message Support",
                            onPrimary = { addSheetModule = WorkspaceModuleType.SUPPORT },
                            secondaryLabel = "View Academy",
                            onSecondary = onOpenAcademy
                        )
                    }
                }
            }

            AccountRole.HOD -> {
                if (roleManager.canAccessSection(DashboardSection.DEPARTMENT_MANAGEMENT)) {
                    item {
                        RoleMetricGrid(
                            items = listOf(
                                "Departments" to "${CodivaCatalog.departments.size}",
                                "Staff Records" to "${employeeRecords.size}",
                                "Projects" to "${CodivaCatalog.projects.size + inquiries.size}",
                                "Open Tickets" to "${support.count { it.status == "OPEN" }}"
                            )
                        )
                    }
                    items(CodivaCatalog.departments, key = { it.name }) { dept ->
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
                                    Text(dept.name, style = MaterialTheme.typography.titleMedium)
                                    Text("Lead: ${dept.headName} • ${dept.focus}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                StatusBadge("${dept.staffCount} staff", BadgeTone.INFO)
                            }
                        }
                    }
                }
            }

            AccountRole.FINANCE -> {
                if (roleManager.canAccessSection(DashboardSection.FINANCE_OPERATIONS)) {
                    val paidDollars = payments.filter { it.status == "PAID" }.sumOf { it.amountCents } / 100L
                    val pendingDollars = payments.filter { it.status == "PENDING" }.sumOf { it.amountCents } / 100L
                    item {
                        RoleMetricGrid(
                            items = listOf(
                                "Collected" to "$$paidDollars",
                                "Pending" to "$$pendingDollars",
                                "Invoices" to "${payments.size}",
                                "Ledger" to "Balanced"
                            )
                        )
                    }
                    item {
                        Row(
                            modifier = Modifier
                                .widthIn(max = 920.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Recent Transactions", style = MaterialTheme.typography.titleMedium)
                            Button(
                                onClick = { addSheetModule = WorkspaceModuleType.PAYMENT },
                                modifier = Modifier.minimumInteractiveComponentSize()
                            ) {
                                Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("New Invoice")
                            }
                        }
                    }
                    items(payments, key = { it.id }) { pay ->
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
                                    Text(pay.title, style = MaterialTheme.typography.titleSmall)
                                    Text("${pay.subtitle} • $${pay.amountCents / 100L}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                if (pay.status == "PENDING") {
                                    OutlinedButton(
                                        onClick = { onUpdateRecordStatus(pay, "PAID") },
                                        enabled = !isSubmitting,
                                        modifier = Modifier.minimumInteractiveComponentSize()
                                    ) {
                                        Text("Verify Paid")
                                    }
                                } else {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = CodivaEmerald, modifier = Modifier.size(16.dp))
                                        Text("PAID", style = MaterialTheme.typography.labelMedium, color = CodivaEmerald)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            AccountRole.PARTNER -> {
                if (roleManager.canAccessSection(DashboardSection.PARTNER_OVERVIEW)) {
                    item {
                        RoleMetricGrid(
                            items = listOf(
                                "Status" to membershipStatus.label,
                                "Initiatives" to "${inquiries.size}",
                                "Catalog" to "${CodivaCatalog.projects.size}",
                                "Bulletins" to "${rolePublications.size}"
                            )
                        )
                    }
                    item {
                        DashboardActionCard(
                            title = "CODIVA Strategic Partner Hub",
                            body = "Collaborate with CODIVA Technologies (${CodivaCatalog.OFFICIAL_DOMAIN}) on Liberia & pan-African software delivery, scholarships, and joint digital initiatives.",
                            primaryLabel = "Propose Joint Project",
                            onPrimary = { onRequestQuote("Custom software development") },
                            secondaryLabel = "Message Partner Desk",
                            onSecondary = { addSheetModule = WorkspaceModuleType.SUPPORT }
                        )
                    }
                }
            }

            AccountRole.SUPER_ADMIN -> {
                if (roleManager.canAccessSection(DashboardSection.FOUNDER_CONTROL_PANEL)) {
                    item {
                        DashboardActionCard(
                            title = "Founder & Super Administrator Control Panel",
                            body = "Manage User, Academy, Project, Publications, App Releases, and Security operations, or switch into the full 11-module CODIVA control console.",
                            primaryLabel = "Open Founder Dashboard",
                            onPrimary = onOpenSuperAdmin,
                            secondaryLabel = "Open Academy",
                            onSecondary = onOpenAcademy
                        )
                    }
                }
            }
        }

        // Official Admin Publications & Documents pushed to this user's dashboard
        if (rolePublications.isNotEmpty()) {
            item {
                SectionHeader(
                    eyebrow = "Posted by Administration",
                    title = "Official Documents & Notices (${rolePublications.size})",
                    modifier = Modifier.widthIn(max = 920.dp)
                )
            }
            items(rolePublications, key = { "role_pub_${it.id}" }) { pub ->
                AdminPublicationCard(
                    publication = pub,
                    isAdminMode = false,
                    isSubmitting = isSubmitting,
                    onReadMore = { readingPublication = pub },
                    onOpenDocument = { url ->
                        try {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                        } catch (_: Exception) {
                            onNotify("Unable to open document link.")
                        }
                    }
                )
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
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RoleMetricGrid(items: List<Pair<String, String>>) {
    FlowRow(
        modifier = Modifier
            .widthIn(max = 920.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        maxItemsInEachRow = 2
    ) {
        items.forEach { (label, value) ->
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(value, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
                    Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DashboardActionCard(
    title: String,
    body: String,
    primaryLabel: String,
    onPrimary: () -> Unit,
    secondaryLabel: String,
    onSecondary: () -> Unit
) {
    Card(
        modifier = Modifier.widthIn(max = 920.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(onClick = onPrimary, modifier = Modifier.minimumInteractiveComponentSize()) {
                    Text(primaryLabel)
                }
                OutlinedButton(onClick = onSecondary, modifier = Modifier.minimumInteractiveComponentSize()) {
                    Text(secondaryLabel)
                }
            }
        }
    }
}

private fun roleIcon(role: AccountRole) = when (role) {
    AccountRole.USER -> Icons.Rounded.Person
    AccountRole.MEMBER -> Icons.Rounded.Groups
    AccountRole.STAFF -> Icons.Rounded.Work
    AccountRole.LECTURER -> Icons.Rounded.School
    AccountRole.PARENT -> Icons.Rounded.FamilyRestroom
    AccountRole.HOD -> Icons.Rounded.Apartment
    AccountRole.FINANCE -> Icons.Rounded.AttachMoney
    AccountRole.PARTNER -> Icons.Rounded.Handshake
    AccountRole.SUPER_ADMIN -> Icons.Rounded.AdminPanelSettings
}

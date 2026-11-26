package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AdminPanelSettings
import androidx.compose.material.icons.rounded.Analytics
import androidx.compose.material.icons.rounded.Announcement
import androidx.compose.material.icons.rounded.Apartment
import androidx.compose.material.icons.rounded.Article
import androidx.compose.material.icons.rounded.AttachMoney
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.SupportAgent
import androidx.compose.material.icons.rounded.WorkOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.AccountRole
import com.example.data.AuditLogEntry
import com.example.data.CareerApplication
import com.example.data.CodivaCatalog
import com.example.data.CourseEnrollment
import com.example.data.DashboardPermission
import com.example.data.MembershipStatus
import com.example.data.ProjectInquiry
import com.example.data.SystemSettingsDoc
import com.example.data.UserProfile
import com.example.data.UserRoleManager
import com.example.data.WorkspaceModuleType
import com.example.data.WorkspaceRecord
import com.example.ui.UiState
import com.example.ui.components.ActivityHeatmapAndCalendarRow
import com.example.ui.components.AddWorkspaceRecordSheet
import com.example.ui.components.AdminKpiTile
import com.example.ui.components.AuditLogCard
import com.example.ui.components.BadgeTone
import com.example.ui.components.ErrorBanner
import com.example.ui.components.ExecutiveKpiOverviewRow
import com.example.ui.components.ModuleHeaderRow
import com.example.ui.components.PlatformAnalyticsChartCard
import com.example.ui.components.SectionHeader
import com.example.ui.components.SettingToggleCard
import com.example.ui.components.StatusBadge
import com.example.ui.components.UserRolesDonutCard
import com.example.ui.components.WorkspaceRecordCard
import com.example.ui.theme.CodivaAmber
import com.example.ui.theme.CodivaCobalt
import com.example.ui.theme.CodivaEmerald
import com.example.ui.theme.CodivaNavy
import com.example.ui.theme.CodivaSky
import com.example.ui.theme.CodivaViolet

enum class AdminModule(val label: String, val icon: ImageVector) {
    OVERVIEW("Overview", Icons.Rounded.AdminPanelSettings),
    USERS("Users", Icons.Rounded.Groups),
    ACADEMY("Academy", Icons.Rounded.School),
    EMPLOYEES("Departments", Icons.Rounded.Apartment),
    PAYMENTS("Finance", Icons.Rounded.AttachMoney),
    PROJECTS("Projects", Icons.Rounded.WorkOutline),
    NEWS("News & Media", Icons.Rounded.Article),
    ANNOUNCEMENTS("Announcements", Icons.Rounded.Announcement),
    SUPPORT("Support", Icons.Rounded.SupportAgent),
    REPORTS("Analytics", Icons.Rounded.Analytics),
    SECURITY("Audit Logs", Icons.Rounded.Security),
    SETTINGS("Settings", Icons.Rounded.Settings)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SuperAdminScreen(
    workspaceRecordsState: UiState<List<WorkspaceRecord>>,
    auditLogsState: UiState<List<AuditLogEntry>>,
    settingsState: UiState<SystemSettingsDoc?>,
    registeredUsersState: UiState<List<UserProfile>>,
    inquiriesState: UiState<List<ProjectInquiry>>,
    enrollmentsState: UiState<List<CourseEnrollment>>,
    applicationsState: UiState<List<CareerApplication>>,
    isSubmitting: Boolean,
    onAddWorkspaceRecord: (WorkspaceModuleType, String, String, String, String, String, Long, () -> Unit) -> Unit,
    onUpdateRecordStatus: (WorkspaceRecord, String) -> Unit,
    onDeleteRecord: (WorkspaceRecord) -> Unit,
    onUpdateSettings: (SystemSettingsDoc) -> Unit,
    onNotify: (String) -> Unit = {},
    activeRole: AccountRole = AccountRole.SUPER_ADMIN,
    onBackToFounderDashboard: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val roleManager = remember(activeRole) { UserRoleManager.forRole(activeRole) }
    val allowedModules = remember(roleManager) {
        AdminModule.entries.filter { module ->
            when (module) {
                AdminModule.OVERVIEW -> roleManager.canAccessFounderDashboard()
                AdminModule.USERS -> roleManager.hasPermission(DashboardPermission.MANAGE_USERS)
                AdminModule.ACADEMY -> roleManager.hasPermission(DashboardPermission.MANAGE_ACADEMY)
                AdminModule.EMPLOYEES -> roleManager.hasPermission(DashboardPermission.MANAGE_DEPARTMENTS)
                AdminModule.PAYMENTS -> roleManager.hasPermission(DashboardPermission.VIEW_FINANCE_LEDGER)
                AdminModule.PROJECTS -> roleManager.hasPermission(DashboardPermission.MANAGE_PROJECTS)
                AdminModule.NEWS,
                AdminModule.ANNOUNCEMENTS,
                AdminModule.SUPPORT -> roleManager.hasPermission(DashboardPermission.VIEW_FULL_ADMIN_CONSOLE)
                AdminModule.REPORTS -> roleManager.hasPermission(DashboardPermission.VIEW_ANALYTICS)
                AdminModule.SECURITY,
                AdminModule.SETTINGS -> roleManager.hasPermission(DashboardPermission.MANAGE_SECURITY_SETTINGS)
            }
        }
    }
    var activeModule by remember(allowedModules) {
        mutableStateOf(allowedModules.firstOrNull() ?: AdminModule.OVERVIEW)
    }
    var addSheetModule by remember { mutableStateOf<WorkspaceModuleType?>(null) }

    val records = (workspaceRecordsState as? UiState.Success)?.data ?: emptyList()
    val auditLogs = (auditLogsState as? UiState.Success)?.data ?: emptyList()
    val settings = (settingsState as? UiState.Success)?.data ?: SystemSettingsDoc()
    val registeredUsers = (registeredUsersState as? UiState.Success)?.data ?: emptyList()
    val inquiries = (inquiriesState as? UiState.Success)?.data ?: emptyList()
    val enrollments = (enrollmentsState as? UiState.Success)?.data ?: emptyList()
    val applications = (applicationsState as? UiState.Success)?.data ?: emptyList()

    val userRecords = remember(records) { records.filter { it.moduleEnum == WorkspaceModuleType.USER } }
    val courseRecords = remember(records) { records.filter { it.moduleEnum == WorkspaceModuleType.COURSE } }
    val employeeRecords = remember(records) { records.filter { it.moduleEnum == WorkspaceModuleType.EMPLOYEE } }
    val paymentRecords = remember(records) { records.filter { it.moduleEnum == WorkspaceModuleType.PAYMENT } }
    val announcementRecords = remember(records) { records.filter { it.moduleEnum == WorkspaceModuleType.ANNOUNCEMENT } }
    val newsRecords = remember(records) { records.filter { it.moduleEnum == WorkspaceModuleType.NEWS } }
    val supportRecords = remember(records) { records.filter { it.moduleEnum == WorkspaceModuleType.SUPPORT } }

    val paidRevenueDollars = remember(paymentRecords) {
        paymentRecords.filter { it.status == "PAID" }.sumOf { it.amountCents } / 100L
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // CEO & Founder Super Administrator Header Banner
        item {
            Card(
                modifier = Modifier.widthIn(max = 920.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = CodivaNavy),
                border = BorderStroke(1.dp, CodivaSky.copy(alpha = 0.25f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                listOf(CodivaNavy, Color(0xFF142B5A), Color(0xFF0A1428))
                            )
                        )
                        .padding(18.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.img_founder_ceo),
                                    contentDescription = "Ebenezer Yeapeh Jr. CEO and Founder",
                                    modifier = Modifier
                                        .size(68.dp)
                                        .clip(RoundedCornerShape(16.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                    StatusBadge(text = "SUPER ADMINISTRATOR", tone = BadgeTone.SUCCESS)
                                    Text(
                                        text = "Welcome back, ${CodivaCatalog.FOUNDER_NAME}",
                                        style = MaterialTheme.typography.titleLarge,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Founder & CEO · CODIVA TECHNOLOGIES",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = CodivaSky
                                    )
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = Color.Black,
                                border = BorderStroke(1.dp, CodivaSky.copy(alpha = 0.45f)),
                                modifier = Modifier
                                    .size(62.dp)
                                    .testTag("super_admin_profile_logo")
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.img_codiva_logo),
                                    contentDescription = "CODIVA Technologies Logo",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(3.dp),
                                    contentScale = ContentScale.Fit
                                )
                            }
                        }

                        Text(
                            text = "\"${CodivaCatalog.FOUNDER_MANTRA}\"",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFCBD5E1)
                        )

                        // KPI Summary Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            AdminKpiTile("Registered", "${registeredUsers.size}", CodivaCobalt)
                            AdminKpiTile("Academy", "${CodivaCatalog.academyPrograms.size + courseRecords.size + enrollments.size}", CodivaEmerald)
                            AdminKpiTile("Revenue", "$$paidRevenueDollars", Color(0xFF7C3AED))
                            AdminKpiTile("Projects", "${CodivaCatalog.projects.size + inquiries.size}", CodivaAmber)
                            AdminKpiTile("Tickets", "${supportRecords.size}", Color(0xFF0EA5E9))
                        }
                    }
                }
            }
        }

        // 11-Module Control Bar
        item {
            Row(
                modifier = Modifier
                    .widthIn(max = 920.dp)
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .testTag("admin_module_chips"),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (onBackToFounderDashboard != null) {
                    OutlinedButton(
                        onClick = onBackToFounderDashboard,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("back_to_founder_dashboard")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Founder Dashboard")
                    }
                }
                allowedModules.forEach { module ->
                    FilterChip(
                        selected = activeModule == module,
                        onClick = { activeModule = module },
                        leadingIcon = {
                            Icon(
                                imageVector = module.icon,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        label = { Text(module.label) },
                        modifier = Modifier.testTag("admin_chip_${module.name.lowercase()}")
                    )
                }
            }
        }

        if (workspaceRecordsState is UiState.Error) {
            item {
                ErrorBanner(
                    message = workspaceRecordsState.message,
                    modifier = Modifier.widthIn(max = 920.dp)
                )
            }
        }

        when (activeModule) {
            AdminModule.OVERVIEW -> {
                item {
                    Column(
                        modifier = Modifier.widthIn(max = 920.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        SectionHeader(
                            eyebrow = "Quick Access",
                            title = "Central Control Actions"
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            QuickActionChip("Add User") { addSheetModule = WorkspaceModuleType.USER }
                            QuickActionChip("Add Course") { addSheetModule = WorkspaceModuleType.COURSE }
                            QuickActionChip("Record Payment") { addSheetModule = WorkspaceModuleType.PAYMENT }
                            QuickActionChip("Publish Announcement") { addSheetModule = WorkspaceModuleType.ANNOUNCEMENT }
                            QuickActionChip("Post News") { addSheetModule = WorkspaceModuleType.NEWS }
                            QuickActionChip("Add Staff") { addSheetModule = WorkspaceModuleType.EMPLOYEE }
                        }

                        ExecutiveKpiOverviewRow(
                            registeredUsers = registeredUsers,
                            auditLogs = auditLogs,
                            paymentRecords = paymentRecords,
                            supportRecords = supportRecords
                        )

                        ActivityHeatmapAndCalendarRow(
                            registeredUsers = registeredUsers,
                            auditLogs = auditLogs,
                            inquiries = inquiries,
                            enrollments = enrollments,
                            publications = emptyList()
                        )

                        PlatformAnalyticsChartCard(
                            visitorsSeries = listOf(
                                registeredUsers.size.toFloat(),
                                registeredUsers.count { it.statusEnum == MembershipStatus.ACTIVE }.toFloat(),
                                inquiries.size.toFloat(),
                                applications.size.toFloat()
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
                                supportRecords.size.toFloat(),
                                auditLogs.size.toFloat()
                            ),
                            labels = listOf("Total", "Active", "Completed", "Logs"),
                            timeframeLabel = "Real Firestore Totals",
                            seriesLabels = Triple("Users & Inquiries", "Academy", "Payments & Logs")
                        )

                        UserRolesDonutCard(
                            slices = listOf(
                                Triple("Students & Parents", (registeredUsers.count { it.roleEnum == AccountRole.USER || it.roleEnum == AccountRole.PARENT } + enrollments.size).toFloat(), CodivaCobalt),
                                Triple("Members & Partners", registeredUsers.count { it.roleEnum == AccountRole.MEMBER || it.roleEnum == AccountRole.PARTNER }.toFloat(), CodivaEmerald),
                                Triple("Staff & Lecturers", registeredUsers.count { it.roleEnum == AccountRole.STAFF || it.roleEnum == AccountRole.LECTURER }.toFloat(), CodivaAmber),
                                Triple("Admins, HOD & Finance", registeredUsers.count { it.roleEnum == AccountRole.SUPER_ADMIN || it.roleEnum == AccountRole.HOD || it.roleEnum == AccountRole.FINANCE }.toFloat(), CodivaViolet)
                            ),
                            totalLabel = "${registeredUsers.size}"
                        )

                        // System Overview Card
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Column(
                                modifier = Modifier.padding(18.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("System Overview", style = MaterialTheme.typography.titleMedium)
                                    StatusBadge(
                                        text = if (settings.maintenanceMode) "Maintenance Mode" else "All Systems Online",
                                        tone = if (settings.maintenanceMode) BadgeTone.WARNING else BadgeTone.SUCCESS
                                    )
                                }
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    StatusBadge("Website: Online", BadgeTone.SUCCESS)
                                    StatusBadge("Firestore: Connected", BadgeTone.SUCCESS)
                                    StatusBadge("RLS Security: Enforced", BadgeTone.SUCCESS)
                                    StatusBadge("Audit Logs: ${auditLogs.size} events", BadgeTone.INFO)
                                }
                            }
                        }

                        SectionHeader(
                            eyebrow = "Recent Activities",
                            title = "Latest Audit Trail",
                            actionLabel = "All Logs",
                            onActionClick = { activeModule = AdminModule.SECURITY }
                        )
                    }
                }

                items(auditLogs.take(4), key = { it.id }) { log ->
                    AuditLogCard(log = log, modifier = Modifier.widthIn(max = 920.dp))
                }
            }

            AdminModule.USERS -> {
                item {
                    ModuleHeaderRow(
                        eyebrow = "Module 1",
                        title = "User Management (${userRecords.size})",
                        buttonLabel = "Add User",
                        onAdd = { addSheetModule = WorkspaceModuleType.USER }
                    )
                }
                items(userRecords, key = { it.id }) { record ->
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
            }

            AdminModule.ACADEMY -> {
                item {
                    ModuleHeaderRow(
                        eyebrow = "Module 2",
                        title = "Academy Management",
                        buttonLabel = "Add Course",
                        onAdd = { addSheetModule = WorkspaceModuleType.COURSE }
                    )
                }
                items(CodivaCatalog.academyPrograms, key = { it.id }) { prog ->
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
                items(courseRecords, key = { it.id }) { record ->
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

            AdminModule.EMPLOYEES -> {
                item {
                    ModuleHeaderRow(
                        eyebrow = "Module 3",
                        title = "Employees & Departments",
                        buttonLabel = "Add Employee",
                        onAdd = { addSheetModule = WorkspaceModuleType.EMPLOYEE }
                    )
                }
                items(CodivaCatalog.departments, key = { it.name }) { dept ->
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
                            Column(modifier = Modifier.weight(1f)) {
                                Text(dept.name, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    "HOD: ${dept.headName} • ${dept.focus}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            StatusBadge("${dept.staffCount} staff", BadgeTone.INFO)
                        }
                    }
                }
                items(employeeRecords, key = { it.id }) { record ->
                    WorkspaceRecordCard(
                        record = record,
                        primaryActionLabel = if (record.status == "ACTIVE") "On Leave" else "Activate",
                        onPrimaryAction = {
                            onUpdateRecordStatus(record, if (record.status == "ACTIVE") "ON_LEAVE" else "ACTIVE")
                        },
                        onDelete = { onDeleteRecord(record) },
                        isSubmitting = isSubmitting
                    )
                }
            }

            AdminModule.PAYMENTS -> {
                item {
                    ModuleHeaderRow(
                        eyebrow = "Module 4",
                        title = "Payments & Finance ($$paidRevenueDollars paid)",
                        buttonLabel = "Record Payment",
                        onAdd = { addSheetModule = WorkspaceModuleType.PAYMENT }
                    )
                }
                items(paymentRecords, key = { it.id }) { record ->
                    WorkspaceRecordCard(
                        record = record,
                        primaryActionLabel = if (record.status == "PENDING") "Mark Paid" else "Reopen",
                        onPrimaryAction = {
                            onUpdateRecordStatus(record, if (record.status == "PENDING") "PAID" else "PENDING")
                        },
                        onDelete = { onDeleteRecord(record) },
                        isSubmitting = isSubmitting
                    )
                }
            }

            AdminModule.PROJECTS -> {
                item {
                    SectionHeader(
                        eyebrow = "Module 5",
                        title = "Projects (${CodivaCatalog.projects.size}) & Inquiries (${inquiries.size})",
                        modifier = Modifier.widthIn(max = 920.dp)
                    )
                }
                items(CodivaCatalog.projects, key = { it.id }) { proj ->
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
                            StatusBadge(
                                proj.status,
                                if (proj.status == "Completed") BadgeTone.SUCCESS else BadgeTone.WARNING
                            )
                        }
                    }
                }
            }

            AdminModule.NEWS -> {
                item {
                    ModuleHeaderRow(
                        eyebrow = "Module 6",
                        title = "News & Media",
                        buttonLabel = "Post News",
                        onAdd = { addSheetModule = WorkspaceModuleType.NEWS }
                    )
                }
                items(newsRecords, key = { it.id }) { record ->
                    WorkspaceRecordCard(
                        record = record,
                        primaryActionLabel = if (record.status == "PUBLISHED") "Unpublish" else "Publish",
                        onPrimaryAction = {
                            onUpdateRecordStatus(record, if (record.status == "PUBLISHED") "DRAFT" else "PUBLISHED")
                        },
                        onDelete = { onDeleteRecord(record) },
                        isSubmitting = isSubmitting
                    )
                }
                items(CodivaCatalog.newsArticles, key = { it.id }) { article ->
                    Card(
                        modifier = Modifier.widthIn(max = 920.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(article.publishedDate, style = MaterialTheme.typography.labelSmall)
                                StatusBadge(article.category, BadgeTone.INFO)
                            }
                            Text(article.title, style = MaterialTheme.typography.titleMedium)
                            Text(article.summary, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            AdminModule.ANNOUNCEMENTS -> {
                item {
                    ModuleHeaderRow(
                        eyebrow = "Module 7",
                        title = "Announcements (${announcementRecords.size})",
                        buttonLabel = "New Notice",
                        onAdd = { addSheetModule = WorkspaceModuleType.ANNOUNCEMENT }
                    )
                }
                items(announcementRecords, key = { it.id }) { record ->
                    WorkspaceRecordCard(
                        record = record,
                        primaryActionLabel = if (record.status == "PUBLISHED") "Archive" else "Publish",
                        onPrimaryAction = {
                            onUpdateRecordStatus(record, if (record.status == "PUBLISHED") "DRAFT" else "PUBLISHED")
                        },
                        onDelete = { onDeleteRecord(record) },
                        isSubmitting = isSubmitting
                    )
                }
            }

            AdminModule.SUPPORT -> {
                item {
                    ModuleHeaderRow(
                        eyebrow = "Module 8",
                        title = "Messages & Support (${supportRecords.size})",
                        buttonLabel = "New Ticket",
                        onAdd = { addSheetModule = WorkspaceModuleType.SUPPORT }
                    )
                }
                items(supportRecords, key = { it.id }) { record ->
                    WorkspaceRecordCard(
                        record = record,
                        primaryActionLabel = if (record.status == "OPEN") "Resolve" else "Reopen",
                        onPrimaryAction = {
                            onUpdateRecordStatus(record, if (record.status == "OPEN") "RESOLVED" else "OPEN")
                        },
                        onDelete = { onDeleteRecord(record) },
                        isSubmitting = isSubmitting
                    )
                }
            }

            AdminModule.REPORTS -> {
                item {
                    Column(
                        modifier = Modifier.widthIn(max = 920.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        SectionHeader(
                            eyebrow = "Module 9",
                            title = "Reports & Platform Analytics"
                        )
                        OutlinedButton(
                            onClick = {
                                val csv = buildString {
                                    appendLine("Category,Metric,Value")
                                    appendLine("Users,Registered Accounts,${registeredUsers.size}")
                                    appendLine("Users,Pending Approval,${registeredUsers.count { it.statusEnum == MembershipStatus.PENDING }}")
                                    appendLine("Academy,Enrollments,${enrollments.size}")
                                    appendLine("Projects,Inquiries,${inquiries.size}")
                                    appendLine("Careers,Applications,${applications.size}")
                                    appendLine("Finance,Paid Revenue USD,$paidRevenueDollars")
                                    appendLine("Support,Open Tickets,${supportRecords.count { it.status == "OPEN" }}")
                                }
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/csv"
                                    putExtra(Intent.EXTRA_SUBJECT, "CODIVA Analytics CSV")
                                    putExtra(Intent.EXTRA_TEXT, csv)
                                }
                                context.startActivity(Intent.createChooser(intent, "Export CODIVA Analytics CSV"))
                                onNotify("Analytics CSV ready.")
                            },
                            modifier = Modifier.minimumInteractiveComponentSize()
                        ) {
                            Icon(Icons.Rounded.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Export Analytics CSV")
                        }
                        PlatformAnalyticsChartCard(
                            visitorsSeries = listOf(
                                registeredUsers.size.toFloat(),
                                registeredUsers.count { it.statusEnum == MembershipStatus.ACTIVE }.toFloat(),
                                inquiries.size.toFloat(),
                                applications.size.toFloat()
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
                                supportRecords.size.toFloat(),
                                auditLogs.size.toFloat()
                            ),
                            labels = listOf("Total", "Active", "Completed", "Logs"),
                            timeframeLabel = "Real Firestore Totals",
                            seriesLabels = Triple("Users & Inquiries", "Academy", "Payments & Logs")
                        )
                        UserRolesDonutCard(
                            slices = listOf(
                                Triple("Students & Parents", (registeredUsers.count { it.roleEnum == AccountRole.USER || it.roleEnum == AccountRole.PARENT } + enrollments.size).toFloat(), CodivaCobalt),
                                Triple("Members & Partners", registeredUsers.count { it.roleEnum == AccountRole.MEMBER || it.roleEnum == AccountRole.PARTNER }.toFloat(), CodivaEmerald),
                                Triple("Staff & Lecturers", registeredUsers.count { it.roleEnum == AccountRole.STAFF || it.roleEnum == AccountRole.LECTURER }.toFloat(), CodivaAmber),
                                Triple("Admins, HOD & Finance", registeredUsers.count { it.roleEnum == AccountRole.SUPER_ADMIN || it.roleEnum == AccountRole.HOD || it.roleEnum == AccountRole.FINANCE }.toFloat(), Color(0xFF7C3AED))
                            ),
                            totalLabel = "${registeredUsers.size}"
                        )
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("Operational Metrics (Live Firestore)", style = MaterialTheme.typography.titleMedium)
                                Text("• Registered Accounts: ${registeredUsers.size} (${registeredUsers.count { it.statusEnum == MembershipStatus.PENDING }} pending)", style = MaterialTheme.typography.bodyMedium)
                                Text("• Active Inquiries: ${inquiries.size} • Career Applications: ${applications.size}", style = MaterialTheme.typography.bodyMedium)
                                Text("• Paid Revenue Recorded: $$paidRevenueDollars USD", style = MaterialTheme.typography.bodyMedium)
                                Text("• Open Support Tickets: ${supportRecords.count { it.status == "OPEN" }}", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }

            AdminModule.SECURITY -> {
                item {
                    SectionHeader(
                        eyebrow = "Module 10",
                        title = "Security & Audit Logs (${auditLogs.size})",
                        modifier = Modifier.widthIn(max = 920.dp)
                    )
                }
                items(auditLogs, key = { it.id }) { log ->
                    AuditLogCard(log = log, modifier = Modifier.widthIn(max = 920.dp))
                }
            }

            AdminModule.SETTINGS -> {
                item {
                    Column(
                        modifier = Modifier.widthIn(max = 920.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SectionHeader(
                            eyebrow = "Module 11",
                            title = "System Settings"
                        )
                        SettingToggleCard(
                            title = "Auto-approve Member Registrations",
                            description = "Automatically activate new CODIVA members without manual admin review.",
                            checked = settings.autoApproveMembers,
                            enabled = !isSubmitting,
                            onCheckedChange = { onUpdateSettings(settings.copy(autoApproveMembers = it)) }
                        )
                        SettingToggleCard(
                            title = "Academy Payments Enabled",
                            description = "Allow tuition and project milestone billing across the Finance module.",
                            checked = settings.paymentsEnabled,
                            enabled = !isSubmitting,
                            onCheckedChange = { onUpdateSettings(settings.copy(paymentsEnabled = it)) }
                        )
                        SettingToggleCard(
                            title = "Security Audit Logging",
                            description = "Record all administrative mutations into the append-only Firestore audit trail.",
                            checked = settings.auditLoggingEnabled,
                            enabled = !isSubmitting,
                            onCheckedChange = { onUpdateSettings(settings.copy(auditLoggingEnabled = it)) }
                        )
                        SettingToggleCard(
                            title = "Monrovia Holiday Office Hours Banner",
                            description = "Display the Dec 18 – Jan 5 office schedule notice on the home ticker.",
                            checked = settings.holidayNoticeEnabled,
                            enabled = !isSubmitting,
                            onCheckedChange = { onUpdateSettings(settings.copy(holidayNoticeEnabled = it)) }
                        )
                        SettingToggleCard(
                            title = "Maintenance Mode",
                            description = "Pause public submissions while performing scheduled platform updates.",
                            checked = settings.maintenanceMode,
                            enabled = !isSubmitting,
                            onCheckedChange = { onUpdateSettings(settings.copy(maintenanceMode = it)) }
                        )
                    }
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
}

@Composable
private fun QuickActionChip(label: String, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.minimumInteractiveComponentSize()
    ) {
        Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(label)
    }
}

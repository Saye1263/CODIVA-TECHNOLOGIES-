package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.InstallMobile
import androidx.compose.material.icons.rounded.PrivacyTip
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Share
import com.example.BuildConfig
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.AccountRole
import com.example.data.AdminPublication
import com.example.data.AppRelease
import com.example.data.ApplicationStatus
import com.example.data.CareerApplication
import com.example.data.CodivaAppRoutes
import com.example.data.CodivaCatalog
import com.example.data.CourseEnrollment
import com.example.data.EnrollmentStatus
import com.example.data.InquiryStatus
import com.example.data.MembershipStatus
import com.example.data.ProjectInquiry
import com.example.data.PublicPortalConfig
import com.example.data.ReleaseStatus
import com.example.data.SystemSettingsDoc
import com.example.data.UserProfile
import com.example.data.UserRoleManager
import com.example.data.WorkspaceModuleType
import com.example.data.WorkspaceRecord
import com.example.ui.UiState
import com.example.ui.components.AddWorkspaceRecordSheet
import com.example.ui.components.AdminPublicationCard
import com.example.ui.components.AppReleaseCard
import com.example.ui.components.BadgeTone
import com.example.ui.components.ErrorBanner
import com.example.ui.components.HelpCenterSectionCard
import com.example.ui.components.InstallPlatformSheet
import com.example.ui.components.LegalDocumentsSheet
import com.example.ui.components.MaintenanceNoticeBanner
import com.example.ui.components.ProfileEditSheet
import com.example.ui.components.PublicationReaderSheet
import com.example.ui.components.SectionHeader
import com.example.ui.components.StatusBadge

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PortalScreen(
    fallbackName: String,
    fallbackEmail: String,
    profileState: UiState<UserProfile?>,
    publicPortalState: UiState<PublicPortalConfig?>,
    inquiriesState: UiState<List<ProjectInquiry>>,
    enrollmentsState: UiState<List<CourseEnrollment>>,
    applicationsState: UiState<List<CareerApplication>>,
    publicationsState: UiState<List<AdminPublication>>,
    appReleasesState: UiState<List<AppRelease>>,
    workspaceRecordsState: UiState<List<WorkspaceRecord>>,
    settingsState: UiState<SystemSettingsDoc?>,
    isSubmitting: Boolean,
    onUpdateProfile: (String, AccountRole, String, String, () -> Unit) -> Unit,
    onAddWorkspaceRecord: (WorkspaceModuleType, String, String, String, String, String, Long, () -> Unit) -> Unit,
    onNotify: (String) -> Unit,
    onNewInquiry: () -> Unit,
    onCancelInquiry: (String) -> Unit,
    onDeleteInquiry: (String) -> Unit,
    onWithdrawApplication: (String) -> Unit,
    onOpenCareersAbout: () -> Unit,
    onSignOut: () -> Unit,
    roleManager: UserRoleManager = UserRoleManager.fromProfile((profileState as? UiState.Success)?.data, fallbackEmail),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var showEditProfile by remember { mutableStateOf(false) }
    var showSupportTicketSheet by remember { mutableStateOf(false) }
    var showInstallGuideSheet by remember { mutableStateOf(false) }
    var showLegalDocsSheet by remember { mutableStateOf(false) }
    var readingPublication by remember { mutableStateOf<AdminPublication?>(null) }

    val profile = (profileState as? UiState.Success)?.data
    val portalConfig = (publicPortalState as? UiState.Success)?.data ?: PublicPortalConfig()
    val settings = (settingsState as? UiState.Success)?.data ?: SystemSettingsDoc()
    val inquiries = (inquiriesState as? UiState.Success)?.data ?: emptyList()
    val enrollments = (enrollmentsState as? UiState.Success)?.data ?: emptyList()
    val applications = (applicationsState as? UiState.Success)?.data ?: emptyList()
    val allPublications = (publicationsState as? UiState.Success)?.data ?: emptyList()
    val appReleases = ((appReleasesState as? UiState.Success)?.data ?: emptyList())
        .filter { it.statusEnum != ReleaseStatus.RETIRED }
    val supportTickets = ((workspaceRecordsState as? UiState.Success)?.data ?: emptyList())
        .filter { it.moduleEnum == WorkspaceModuleType.SUPPORT }

    val displayName = profile?.displayName?.ifBlank { fallbackName } ?: fallbackName
    val displayEmail = profile?.email?.ifBlank { fallbackEmail } ?: fallbackEmail
    val visiblePublications = remember(allPublications, roleManager.activeRole, roleManager.membershipStatus) {
        roleManager.filterVisiblePublications(allPublications)
    }
    val membershipStatus = roleManager.membershipStatus
    val statusBadgeTone = when (membershipStatus) {
        MembershipStatus.ACTIVE -> BadgeTone.SUCCESS
        MembershipStatus.PENDING -> BadgeTone.WARNING
        MembershipStatus.SUSPENDED -> BadgeTone.NEUTRAL
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (roleManager.shouldShowMaintenanceBanner(settings)) {
            item { MaintenanceNoticeBanner() }
        }
        // Application Profile Card featuring official CODIVA TECHNOLOGIES logo
        item {
            Card(
                modifier = Modifier.widthIn(max = 860.dp),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_codiva_logo),
                            contentDescription = "CODIVA Technologies application profile logo",
                            modifier = Modifier
                                .size(62.dp)
                                .clip(RoundedCornerShape(14.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = displayName, style = MaterialTheme.typography.titleLarge)
                            Text(
                                text = displayEmail,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = if (!profile?.organization.isNullOrBlank()) {
                                    "${profile!!.organization} • CODIVA Technologies"
                                } else {
                                    "CODIVA Technologies • ${CodivaCatalog.SLOGAN}"
                                },
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Column(
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            StatusBadge(
                                text = profile?.roleEnum?.label ?: "Member",
                                tone = BadgeTone.INFO
                            )
                            StatusBadge(
                                text = membershipStatus.label,
                                tone = statusBadgeTone
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MetricTile("Inquiries", inquiries.size.toString(), Modifier.weight(1f))
                        MetricTile("Courses", enrollments.size.toString(), Modifier.weight(1f))
                        MetricTile("Applications", applications.size.toString(), Modifier.weight(1f))
                    }

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showEditProfile = true },
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("edit_profile_button")
                        ) {
                            Icon(Icons.Rounded.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Edit Role & Profile")
                        }
                        if (roleManager.canNavigateToRoute(CodivaAppRoutes.CAREERS)) {
                            OutlinedButton(
                                onClick = onOpenCareersAbout,
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("open_careers_about_button")
                            ) {
                                Icon(Icons.Rounded.Badge, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Careers & Team")
                            }
                        }
                        OutlinedButton(
                            onClick = onSignOut,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("sign_out_button")
                        ) {
                            Icon(
                                Icons.AutoMirrored.Rounded.Logout,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(stringResource(R.string.sign_out))
                        }
                    }
                }
            }
        }

        if (portalConfig.portalPublished) {
            item {
                Card(
                    modifier = Modifier
                        .widthIn(max = 860.dp)
                        .testTag("portal_invite_share_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Public,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Invite & Share CODIVA",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        Text(
                            text = portalConfig.welcomeMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Brand: ${portalConfig.customDomain.ifBlank { CodivaCatalog.OFFICIAL_DOMAIN }} • Portal Link: ${portalConfig.publicUrl}\nInvite Code: ${portalConfig.inviteCode}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(
                                            Intent.EXTRA_SUBJECT,
                                            "Join ${portalConfig.portalTitle}"
                                        )
                                        putExtra(
                                            Intent.EXTRA_TEXT,
                                            "Join ${portalConfig.portalTitle} online and create your account:\n${portalConfig.publicUrl}\nInvite Code: ${portalConfig.inviteCode}"
                                        )
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Share CODIVA Invite"))
                                },
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("portal_share_invite_button")
                            ) {
                                Icon(Icons.Rounded.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Share Invite")
                            }
                            OutlinedButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(portalConfig.publicUrl))
                                    onNotify("Portal link copied to clipboard.")
                                },
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("portal_copy_link_button")
                            ) {
                                Icon(Icons.Rounded.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Copy Portal Link")
                            }
                        }
                    }
                }
            }
        }

        // My Project Inquiries (visible when role can request projects)
        if (roleManager.canCreateProjectInquiry()) {
            item {
                Column(
                    modifier = Modifier.widthIn(max = 860.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SectionHeader(
                        eyebrow = "Client Workspace",
                        title = "My Project Inquiries",
                        actionLabel = "New Inquiry",
                        onActionClick = onNewInquiry
                    )
                    when (inquiriesState) {
                        is UiState.Loading -> CircularProgressIndicator(modifier = Modifier.size(28.dp))
                        is UiState.Error -> ErrorBanner(inquiriesState.message)
                        is UiState.Success -> {
                            if (inquiries.isEmpty()) {
                                EmptyPortalCard(
                                    title = "No project inquiries yet",
                                    body = "Share your website, mobile app, or automation idea with our Monrovia team.",
                                    buttonText = "Create Project Inquiry",
                                    onClick = onNewInquiry
                                )
                            }
                        }
                    }
                }
            }

            items(inquiries, key = { it.id }) { inquiry ->
                val isCancelled = inquiry.statusEnum == InquiryStatus.CANCELLED
                Card(
                    modifier = Modifier.widthIn(max = 860.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = inquiry.serviceCategory,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            StatusBadge(
                                text = inquiry.statusEnum.label,
                                tone = if (isCancelled) BadgeTone.NEUTRAL else BadgeTone.INFO
                            )
                        }
                        Text(inquiry.projectTitle, style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = inquiry.details,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = inquiry.budgetTier,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row {
                                if (!isCancelled) {
                                    TextButton(
                                        onClick = { onCancelInquiry(inquiry.id) },
                                        enabled = !isSubmitting,
                                        modifier = Modifier.minimumInteractiveComponentSize()
                                    ) {
                                        Text("Cancel")
                                    }
                                }
                                IconButton(
                                    onClick = { onDeleteInquiry(inquiry.id) },
                                    enabled = !isSubmitting,
                                    modifier = Modifier.minimumInteractiveComponentSize()
                                ) {
                                    Icon(
                                        Icons.Rounded.DeleteOutline,
                                        contentDescription = "Delete inquiry"
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // My Academy Enrollments Summary (visible when role can access Academy)
        if (roleManager.canNavigateToRoute(CodivaAppRoutes.ACADEMY)) {
            item {
                Column(
                    modifier = Modifier.widthIn(max = 860.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SectionHeader(
                        eyebrow = "Learning Record",
                        title = "My Academy Enrollments"
                    )
                    if (enrollmentsState is UiState.Error) {
                        ErrorBanner(enrollmentsState.message)
                    } else if (enrollmentsState is UiState.Success && enrollments.isEmpty()) {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Text(
                                text = "No courses enrolled yet. Visit the Academy tab to join Web, Mobile, or Digital Skills programs.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }
            }

            items(enrollments, key = { it.id }) { enrollment ->
                val isCompleted = enrollment.statusEnum == EnrollmentStatus.COMPLETED
                Card(
                    modifier = Modifier.widthIn(max = 860.dp),
                    shape = RoundedCornerShape(16.dp),
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
                            Text(enrollment.courseTitle, style = MaterialTheme.typography.titleSmall)
                            Text(
                                text = "${enrollment.typeEnum.label} • ${enrollment.progressPercent}% complete",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        StatusBadge(
                            text = enrollment.statusEnum.label,
                            tone = if (isCompleted) BadgeTone.SUCCESS else BadgeTone.INFO
                        )
                    }
                }
            }
        }

        // My Career Applications (visible when role can access Careers)
        if (roleManager.canNavigateToRoute(CodivaAppRoutes.CAREERS)) {
            item {
                Column(
                    modifier = Modifier.widthIn(max = 860.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SectionHeader(
                        eyebrow = "Talent Track",
                        title = "My Career Applications"
                    )
                    if (applicationsState is UiState.Error) {
                        ErrorBanner(applicationsState.message)
                    } else if (applicationsState is UiState.Success && applications.isEmpty()) {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Text(
                                text = "No career applications submitted yet. Tap Careers & Team above to browse open Monrovia roles.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }
            }

            items(applications, key = { it.id }) { app ->
                val withdrawn = app.statusEnum == ApplicationStatus.WITHDRAWN
                Card(
                    modifier = Modifier.widthIn(max = 860.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(app.roleTitle, style = MaterialTheme.typography.titleSmall)
                            StatusBadge(
                                text = app.statusEnum.label,
                                tone = if (withdrawn) BadgeTone.NEUTRAL else BadgeTone.SUCCESS
                            )
                        }
                        Text(
                            text = app.portfolioNote,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (!withdrawn) {
                            TextButton(
                                onClick = { onWithdrawApplication(app.id) },
                                enabled = !isSubmitting,
                                modifier = Modifier.minimumInteractiveComponentSize()
                            ) {
                                Text("Withdraw Application")
                            }
                        }
                    }
                }
            }
        }

        // Direct Support Card
        item {
            Card(
                modifier = Modifier.widthIn(max = 860.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Direct CODIVA Support", style = MaterialTheme.typography.titleMedium)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                context.startActivity(
                                    Intent(Intent.ACTION_DIAL, Uri.parse("tel:${CodivaCatalog.PHONE.replace(" ", "")}"))
                                )
                            },
                            modifier = Modifier.minimumInteractiveComponentSize()
                        ) {
                            Icon(Icons.Rounded.Call, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Call Support")
                        }
                        OutlinedButton(
                            onClick = {
                                context.startActivity(
                                    Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${CodivaCatalog.EMAIL}"))
                                )
                            },
                            modifier = Modifier.minimumInteractiveComponentSize()
                        ) {
                            Icon(Icons.Rounded.Email, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(CodivaCatalog.EMAIL)
                        }
                    }
                }
            }
        }

        // App Downloads & Store Releases (Android, iPhone, Windows, Codiva.com)
        if (appReleases.isNotEmpty()) {
            item {
                SectionHeader(
                    eyebrow = "Multi-Platform Access (${CodivaCatalog.OFFICIAL_DOMAIN})",
                    title = "App Downloads & Updates",
                    actionLabel = "Install Guide",
                    onActionClick = { showInstallGuideSheet = true },
                    modifier = Modifier.widthIn(max = 860.dp)
                )
            }
            items(appReleases, key = { "portal_rel_${it.id}" }) { release ->
                AppReleaseCard(
                    release = release,
                    isAdminMode = false,
                    isSubmitting = isSubmitting,
                    onOpenDownload = { url ->
                        try {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                        } catch (_: Exception) {
                            onNotify("Unable to open store or download link.")
                        }
                    },
                    modifier = Modifier.widthIn(max = 860.dp)
                )
            }
        }

        // Official Documents & Bulletins posted by Administration
        if (visiblePublications.isNotEmpty()) {
            item {
                SectionHeader(
                    eyebrow = "CODIVA Administration",
                    title = "Official Documents & Bulletins",
                    modifier = Modifier.widthIn(max = 860.dp)
                )
            }
            items(visiblePublications, key = { "portal_pub_${it.id}" }) { pub ->
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
                    },
                    modifier = Modifier.widthIn(max = 860.dp)
                )
            }
        }

        // Help Centre: FAQs, Guides & Support Tickets
        item {
            HelpCenterSectionCard(
                onOpenSupportTicket = { showSupportTicketSheet = true },
                modifier = Modifier.widthIn(max = 860.dp)
            )
        }

        if (supportTickets.isNotEmpty()) {
            item {
                SectionHeader(
                    eyebrow = "Support Desk",
                    title = "My Support Tickets (${supportTickets.size})",
                    actionLabel = "New Ticket",
                    onActionClick = { showSupportTicketSheet = true },
                    modifier = Modifier.widthIn(max = 860.dp)
                )
            }
            items(supportTickets.take(5), key = { "sup_${it.id}" }) { ticket ->
                Card(
                    modifier = Modifier.widthIn(max = 860.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(ticket.title, style = MaterialTheme.typography.titleSmall)
                            if (ticket.detail.isNotBlank()) {
                                Text(
                                    text = ticket.detail,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2
                                )
                            }
                        }
                        StatusBadge(
                            text = ticket.status,
                            tone = if (ticket.status == "RESOLVED") BadgeTone.SUCCESS else BadgeTone.WARNING
                        )
                    }
                }
            }
        }

        // Legal & Version Info Card
        item {
            Card(
                modifier = Modifier
                    .widthIn(max = 860.dp)
                    .testTag("portal_legal_version_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Legal, Privacy & App Version", style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = "CODIVA Technologies (${CodivaCatalog.OFFICIAL_DOMAIN}) • v${BuildConfig.VERSION_NAME} (Build #${BuildConfig.VERSION_CODE})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showLegalDocsSheet = true },
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("portal_legal_button")
                        ) {
                            Icon(Icons.Rounded.PrivacyTip, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Privacy Policy & Terms")
                        }
                        OutlinedButton(
                            onClick = { showInstallGuideSheet = true },
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("portal_install_guide_button")
                        ) {
                            Icon(Icons.Rounded.InstallMobile, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Desktop & Mobile PWA Guide")
                        }
                    }
                }
            }
        }
    }

    if (showEditProfile) {
        ProfileEditSheet(
            profile = profile,
            fallbackName = fallbackName,
            userEmail = displayEmail,
            isSubmitting = isSubmitting,
            onDismiss = { showEditProfile = false },
            onSave = { name, role, org, phone ->
                onUpdateProfile(name, role, org, phone) {
                    showEditProfile = false
                }
            }
        )
    }

    if (showSupportTicketSheet) {
        AddWorkspaceRecordSheet(
            moduleType = WorkspaceModuleType.SUPPORT,
            isSubmitting = isSubmitting,
            onDismiss = { showSupportTicketSheet = false },
            onSubmit = { title, subtitle, detail, category, status, cents ->
                onAddWorkspaceRecord(WorkspaceModuleType.SUPPORT, title, subtitle, detail, category, status, cents) {
                    showSupportTicketSheet = false
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

    if (showInstallGuideSheet) {
        InstallPlatformSheet(
            appReleases = appReleases,
            portalUrl = portalConfig.publicUrl.ifBlank { CodivaCatalog.PUBLIC_PORTAL_URL },
            onNotify = onNotify,
            onDismiss = { showInstallGuideSheet = false }
        )
    }

    if (showLegalDocsSheet) {
        LegalDocumentsSheet(onDismiss = { showLegalDocsSheet = false })
    }
}

@Composable
private fun MetricTile(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = value, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun EmptyPortalCard(
    title: String,
    body: String,
    buttonText: String,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(body, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Button(
                onClick = onClick,
                modifier = Modifier.minimumInteractiveComponentSize()
            ) {
                Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(buttonText)
            }
        }
    }
}

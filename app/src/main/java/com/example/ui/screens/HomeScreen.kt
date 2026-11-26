package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.AdminPanelSettings
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.InstallMobile
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.AccountRole
import com.example.data.AdminPublication
import com.example.data.AnnouncementItem
import com.example.data.CodivaAppRoutes
import com.example.data.CodivaCatalog
import com.example.data.SystemSettingsDoc
import com.example.data.UserProfile
import com.example.data.UserRoleManager
import com.example.data.WorkspaceModuleType
import com.example.data.WorkspaceRecord
import com.example.ui.UiState
import com.example.ui.components.AdminPublicationCard
import com.example.ui.components.AnnouncementTickerCard
import com.example.ui.components.BadgeTone
import com.example.ui.components.MaintenanceNoticeBanner
import com.example.ui.components.PublicationReaderSheet
import com.example.ui.components.SectionHeader
import com.example.ui.components.StatusBadge
import com.example.ui.theme.CodivaCobalt
import com.example.ui.theme.CodivaEmerald
import com.example.ui.theme.CodivaNavy
import com.example.ui.theme.CodivaSky

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    displayName: String,
    profileState: UiState<UserProfile?>,
    workspaceRecordsState: UiState<List<WorkspaceRecord>>,
    publicationsState: UiState<List<AdminPublication>>,
    settingsState: UiState<SystemSettingsDoc?>,
    onExploreServices: () -> Unit,
    onRequestQuote: (String) -> Unit,
    onOpenEcosystem: () -> Unit,
    onViewTeam: () -> Unit,
    onOpenAcademy: () -> Unit,
    onOpenDownloadsPortal: () -> Unit,
    onNotify: (String) -> Unit,
    roleManager: UserRoleManager = UserRoleManager.fromProfile((profileState as? UiState.Success)?.data),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val profile = (profileState as? UiState.Success)?.data
    val settings = (settingsState as? UiState.Success)?.data ?: SystemSettingsDoc()
    val resolvedName = profile?.displayName?.ifBlank { displayName } ?: displayName
    val activeRole = roleManager.activeRole
    val roleLabel = activeRole.label
    val canOpenFounderDashboard = roleManager.canAccessFounderDashboard()
    var readingPublication by remember { mutableStateOf<AdminPublication?>(null) }

    val publishedFeed = remember(publicationsState, roleManager.activeRole, roleManager.membershipStatus) {
        val all = (publicationsState as? UiState.Success)?.data ?: emptyList()
        roleManager.filterVisiblePublications(all)
    }

    val liveAnnouncements = remember(workspaceRecordsState) {
        val custom = ((workspaceRecordsState as? UiState.Success)?.data ?: emptyList())
            .filter { it.moduleEnum == WorkspaceModuleType.ANNOUNCEMENT && it.status == "PUBLISHED" }
            .map {
                AnnouncementItem(
                    id = it.id,
                    priority = it.category,
                    title = it.title,
                    message = it.subtitle.ifBlank { it.detail }
                )
            }
        (custom + CodivaCatalog.announcements).distinctBy { it.title }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Column(
                modifier = Modifier.widthIn(max = 860.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color.Black,
                            border = BorderStroke(1.dp, CodivaSky.copy(alpha = 0.45f)),
                            modifier = Modifier
                                .size(46.dp)
                                .testTag("home_header_logo")
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.img_codiva_logo),
                                contentDescription = "CODIVA Technologies Logo",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(2.dp),
                                contentScale = ContentScale.Fit
                            )
                        }
                        Column {
                            Text(
                                text = "Welcome, $resolvedName",
                                style = MaterialTheme.typography.titleLarge
                            )
                            Text(
                                text = stringResource(R.string.brand_slogan),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    StatusBadge(text = roleLabel, tone = BadgeTone.INFO)
                }

                if (roleManager.shouldShowMaintenanceBanner(settings)) {
                    MaintenanceNoticeBanner()
                }

                AnnouncementTickerCard(announcements = liveAnnouncements)

                // Hero Card with Official Logo & Quick Ecosystem CTA
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = CodivaNavy),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.img_hero_codiva),
                                contentDescription = "CODIVA Technologies hero showcase",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(Color(0x440B162C), CodivaNavy)
                                        )
                                    )
                            )
                            Surface(
                                shape = RoundedCornerShape(999.dp),
                                color = CodivaCobalt.copy(alpha = 0.28f),
                                border = BorderStroke(1.dp, CodivaSky.copy(alpha = 0.4f)),
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.brand_vision_badge),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CodivaSky,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                                )
                            }
                        }

                        Column(
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.hero_headline),
                                style = MaterialTheme.typography.headlineMedium,
                                color = Color.White
                            )
                            Text(
                                text = stringResource(R.string.hero_subheadline),
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFFCBD5E1)
                            )
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                if (roleManager.canNavigateToRoute(CodivaAppRoutes.ECOSYSTEM)) {
                                    Button(
                                        onClick = onOpenEcosystem,
                                        colors = ButtonDefaults.buttonColors(containerColor = CodivaCobalt),
                                        modifier = Modifier
                                            .minimumInteractiveComponentSize()
                                            .testTag("home_open_ecosystem_button")
                                    ) {
                                        Icon(Icons.Rounded.Dashboard, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (canOpenFounderDashboard) "Founder & Admin" else "${activeRole.label} Dashboard",
                                            color = Color.White
                                        )
                                    }
                                }
                                if (roleManager.canNavigateToRoute(CodivaAppRoutes.SOLUTIONS)) {
                                    OutlinedButton(
                                        onClick = onExploreServices,
                                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.35f)),
                                        modifier = Modifier
                                            .minimumInteractiveComponentSize()
                                            .testTag("home_explore_services_button")
                                    ) {
                                        Text("Explore Services", color = Color.White)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Official Admin Bulletins & Shared Documents
        if (publishedFeed.isNotEmpty()) {
            item {
                Column(
                    modifier = Modifier.widthIn(max = 860.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SectionHeader(
                        eyebrow = "Posted by CODIVA Administration",
                        title = "Official Documents & Bulletins",
                        actionLabel = "All Docs & Apps",
                        onActionClick = onOpenDownloadsPortal
                    )
                    publishedFeed.take(3).forEach { pub ->
                        AdminPublicationCard(
                            publication = pub,
                            isAdminMode = false,
                            isSubmitting = false,
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
        }

        // Founder & CEO Spotlight with Real Portrait
        item {
            Column(
                modifier = Modifier.widthIn(max = 860.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SectionHeader(
                    eyebrow = "Founder & CEO",
                    title = "Leadership rooted in Liberia",
                    actionLabel = if (roleManager.canNavigateToRoute(CodivaAppRoutes.CAREERS)) "Full Team & Careers" else null,
                    onActionClick = if (roleManager.canNavigateToRoute(CodivaAppRoutes.CAREERS)) onViewTeam else null
                )
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                enabled = roleManager.canNavigateToRoute(CodivaAppRoutes.ECOSYSTEM),
                                onClick = onOpenEcosystem
                            )
                            .padding(18.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_founder_ceo),
                            contentDescription = "Ebenezer Yeapeh Jr, Founder and CEO of CODIVA Technologies",
                            modifier = Modifier
                                .size(70.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            StatusBadge(text = "FOUNDER & CEO • SUPER ADMIN", tone = BadgeTone.SUCCESS)
                            Text(
                                text = "Ebenezer Yeapeh Jr.",
                                style = MaterialTheme.typography.titleLarge
                            )
                            Text(
                                text = "Founder of CODIVA Technologies, building digital solutions and connecting people across Africa and the world.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    Icons.Rounded.AdminPanelSettings,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = if (canOpenFounderDashboard) {
                                        "Tap to open Founder Control Panel"
                                    } else {
                                        "Tap to open ${activeRole.label} Dashboard"
                                    },
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Four Pillars
        item {
            Column(
                modifier = Modifier.widthIn(max = 860.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SectionHeader(
                    eyebrow = "Digital Solutions",
                    title = "From first idea to running product"
                )
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    maxItemsInEachRow = 2
                ) {
                    CodivaCatalog.pillars.forEach { pillar ->
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = pillar.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = pillar.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // Featured Projects + Academy quick link
        item {
            Column(
                modifier = Modifier.widthIn(max = 860.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SectionHeader(
                    eyebrow = "Featured Work",
                    title = "Built for real businesses",
                    actionLabel = if (roleManager.canNavigateToRoute(CodivaAppRoutes.SOLUTIONS)) "All Projects" else null,
                    onActionClick = if (roleManager.canNavigateToRoute(CodivaAppRoutes.SOLUTIONS)) onExploreServices else null
                )
                CodivaCatalog.projects.take(3).forEach { project ->
                    Card(
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
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = project.category,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = project.title,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = project.summary,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            StatusBadge(
                                text = project.status,
                                tone = if (project.status == "Completed") BadgeTone.SUCCESS else BadgeTone.WARNING
                            )
                        }
                    }
                }

                if (roleManager.canNavigateToRoute(CodivaAppRoutes.ACADEMY)) {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(onClick = onOpenAcademy)
                                .padding(18.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "CODIVA ACADEMY",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Training & Certification Programs",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = "Web fundamentals, Mobile app development, and Digital skills certification.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                                contentDescription = "Open Academy",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }

        // Contact Strip
        item {
            Column(
                modifier = Modifier.widthIn(max = 860.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.LocationOn,
                                contentDescription = null,
                                tint = CodivaEmerald
                            )
                            Text(
                                text = "CODIVA Monrovia Office • WhatsApp & Direct Support",
                                style = MaterialTheme.typography.titleSmall
                            )
                        }
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            if (roleManager.canCreateProjectInquiry()) {
                                Button(
                                    onClick = { onRequestQuote(CodivaCatalog.services.first().title) },
                                    modifier = Modifier.minimumInteractiveComponentSize()
                                ) {
                                    Text("Request Quote")
                                }
                            }
                            OutlinedButton(
                                onClick = {
                                    context.startActivity(
                                        Intent(Intent.ACTION_DIAL, Uri.parse("tel:${CodivaCatalog.PHONE.replace(" ", "")}"))
                                    )
                                },
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("home_call_button")
                            ) {
                                Icon(Icons.Rounded.Call, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(CodivaCatalog.PHONE)
                            }
                            OutlinedButton(
                                onClick = {
                                    context.startActivity(
                                        Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${CodivaCatalog.EMAIL}"))
                                    )
                                },
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("home_email_button")
                            ) {
                                Icon(Icons.Rounded.Email, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(CodivaCatalog.EMAIL)
                            }
                        }
                    }
                }
                // App Downloads & Store Releases Shortcut
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onOpenDownloadsPortal)
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.InstallMobile,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text("Download CODIVA (${CodivaCatalog.OFFICIAL_DOMAIN})", style = MaterialTheme.typography.titleSmall)
                            Text(
                                "Get releases for Android, iPhone & iPad, Windows, and Web, plus Help Centre FAQs.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                            contentDescription = "Open App Downloads",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Text(
                    text = stringResource(R.string.copyright_notice),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
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

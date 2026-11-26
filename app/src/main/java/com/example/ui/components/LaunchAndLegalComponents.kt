package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddToHomeScreen
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.DesktopWindows
import androidx.compose.material.icons.rounded.Gavel
import androidx.compose.material.icons.rounded.InstallMobile
import androidx.compose.material.icons.rounded.IosShare
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.OpenInBrowser
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.PhoneIphone
import androidx.compose.material.icons.rounded.PrivacyTip
import androidx.compose.material.icons.rounded.RocketLaunch
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.BuildConfig
import com.example.R
import com.example.data.AppRelease
import com.example.data.CodivaCatalog
import com.example.data.ReleasePlatform
import com.example.data.ReleaseStatus
import com.example.ui.theme.CodivaCobalt
import com.example.ui.theme.CodivaEmerald
import com.example.ui.theme.CodivaNavy
import com.example.ui.theme.CodivaNavyDeep
import com.example.ui.theme.CodivaNavyMid
import com.example.ui.theme.CodivaSky
import com.example.ui.theme.CodivaSlateLight
import com.example.ui.theme.CodivaSlateMuted

@Composable
fun CodivaLaunchSplash(
    onSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.88f,
        animationSpec = tween(durationMillis = 480),
        label = "splash_scale"
    )
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 420),
        label = "splash_alpha"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(CodivaNavyDeep, CodivaNavy, CodivaNavyMid)
                )
            )
            .clickable(onClick = onSkip)
            .padding(24.dp)
            .testTag("codiva_launch_splash"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .scale(scale)
                .alpha(alpha),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = Color.Black,
                border = BorderStroke(2.dp, CodivaSky.copy(alpha = 0.55f)),
                modifier = Modifier.size(128.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_codiva_logo),
                    contentDescription = "CODIVA Technologies Splash Logo",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    contentScale = ContentScale.Fit
                )
            }
            Text(
                text = "CODIVA TECHNOLOGIES",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = CodivaCatalog.SLOGAN,
                style = MaterialTheme.typography.titleMedium,
                color = CodivaSky,
                textAlign = TextAlign.Center
            )
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = CodivaCobalt.copy(alpha = 0.25f),
                border = BorderStroke(1.dp, CodivaSky.copy(alpha = 0.4f))
            ) {
                Text(
                    text = "${CodivaCatalog.OFFICIAL_DOMAIN} • ${CodivaCatalog.HEADQUARTERS}",
                    style = MaterialTheme.typography.labelMedium,
                    color = CodivaSlateLight,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "v${BuildConfig.VERSION_NAME} • Tap anywhere to continue",
                style = MaterialTheme.typography.labelSmall,
                color = CodivaSlateMuted
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun InstallPlatformSheet(
    appReleases: List<AppRelease>,
    portalUrl: String,
    onNotify: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedPlatform by remember { mutableStateOf(ReleasePlatform.WINDOWS) }

    var pinTaskbar by remember { mutableStateOf(true) }
    var pinStart by remember { mutableStateOf(true) }
    var createShortcut by remember { mutableStateOf(false) }
    var autoStart by remember { mutableStateOf(false) }

    val matchingRelease = remember(appReleases, selectedPlatform) {
        appReleases.firstOrNull {
            it.platformEnum == selectedPlatform && it.statusEnum != ReleaseStatus.RETIRED
        }
    }
    val targetUrl = matchingRelease?.downloadUrl?.takeIf { it.isNotBlank() } ?: portalUrl

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp)
                .testTag("install_platform_sheet"),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.Black,
                    modifier = Modifier.size(44.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_codiva_logo),
                        contentDescription = "CODIVA icon",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(3.dp),
                        contentScale = ContentScale.Fit
                    )
                }
                Column {
                    Text(
                        text = "Install CODIVA App",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Publisher: ${CodivaCatalog.OFFICIAL_DOMAIN} • Multi-Platform Guide",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ReleasePlatform.entries.forEach { platform ->
                    val icon = when (platform) {
                        ReleasePlatform.ANDROID -> Icons.Rounded.PhoneAndroid
                        ReleasePlatform.IOS -> Icons.Rounded.PhoneIphone
                        ReleasePlatform.WINDOWS -> Icons.Rounded.DesktopWindows
                        ReleasePlatform.WEB -> Icons.Rounded.Language
                    }
                    FilterChip(
                        selected = selectedPlatform == platform,
                        onClick = { selectedPlatform = platform },
                        leadingIcon = { Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        label = { Text(platform.label) },
                        modifier = Modifier.testTag("install_tab_${platform.name.lowercase()}")
                    )
                }
            }

            when (selectedPlatform) {
                ReleasePlatform.WINDOWS -> {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Windows Desktop / Browser App Setup",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Open ${CodivaCatalog.OFFICIAL_DOMAIN} in Microsoft Edge or Google Chrome, click the Install App icon in the address bar, and choose your preferred desktop integration:",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            InstallPreferenceRow("Pin to taskbar", pinTaskbar) { pinTaskbar = it }
                            InstallPreferenceRow("Pin to Start", pinStart) { pinStart = it }
                            InstallPreferenceRow("Create Desktop shortcut", createShortcut) { createShortcut = it }
                            InstallPreferenceRow("Auto-start on device login", autoStart) { autoStart = it }
                        }
                    }
                }
                ReleasePlatform.IOS -> {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "Install PWA on iPhone & iPad",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(Icons.Rounded.IosShare, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Text(
                                    text = "1. Open ${CodivaCatalog.OFFICIAL_DOMAIN} in Safari and tap the Share icon.",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(Icons.Rounded.AddToHomeScreen, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Text(
                                    text = "2. Scroll down and tap 'Add to Home Screen', then tap 'Add' to install CODIVA alongside your apps.",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
                ReleasePlatform.ANDROID -> {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Installed Native Android Build",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Package: ${BuildConfig.APPLICATION_ID}\nVersion: v${BuildConfig.VERSION_NAME} (Build #${BuildConfig.VERSION_CODE})",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (matchingRelease != null) {
                                Text(
                                    text = "Latest Store Listing: v${matchingRelease.versionName} (${matchingRelease.statusEnum.label})",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
                ReleasePlatform.WEB -> {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Responsive Web Portal (${CodivaCatalog.OFFICIAL_DOMAIN})",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Works across mobile, tablet, and desktop browsers with offline-ready PWA installation and responsive layouts.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(targetUrl))
                        onNotify("Install link copied to clipboard.")
                    },
                    modifier = Modifier
                        .weight(1f)
                        .minimumInteractiveComponentSize()
                ) {
                    Icon(Icons.Rounded.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Copy Link")
                }
                Button(
                    onClick = {
                        try {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(targetUrl)))
                        } catch (_: Exception) {
                            onNotify("Unable to open URL in browser.")
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .minimumInteractiveComponentSize()
                ) {
                    Icon(Icons.Rounded.OpenInBrowser, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Open Link")
                }
            }
        }
    }
}

@Composable
private fun InstallPreferenceRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .minimumInteractiveComponentSize(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = label, style = MaterialTheme.typography.bodySmall)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LegalDocumentsSheet(
    initialTab: Int = 0,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedTab by remember { mutableIntStateOf(initialTab) }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp)
                .testTag("legal_documents_sheet"),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "CODIVA Legal & Governance",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    leadingIcon = { Icon(Icons.Rounded.PrivacyTip, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    label = { Text("Privacy Policy") }
                )
                FilterChip(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    leadingIcon = { Icon(Icons.Rounded.Gavel, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    label = { Text("Terms of Service") }
                )
            }
            HorizontalDivider()
            if (selectedTab == 0) {
                Text("Privacy Policy — CODIVA Technologies (${CodivaCatalog.OFFICIAL_DOMAIN})", style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "1. Data Collected: When you sign in with Google Identity, CODIVA Technologies stores your name, email address, selected account role, organization, and activity records (course enrollments, project quotes, and support tickets) in Cloud Firestore.\n\n" +
                        "2. Access Control: Access to records is strictly isolated by Firestore Role-Based Access Control (RBAC). Students, Members, Parents, Staff, Lecturers, and Super Administrators only see data authorized for their role.\n\n" +
                        "3. Audit & Security: Administrative role changes and approvals are recorded in the system audit trail. We do not sell user data or expose private credentials in client code.\n\n" +
                        "4. Contact & Data Requests: To update or request deletion of your account data, contact ${CodivaCatalog.EMAIL} (${CodivaCatalog.HEADQUARTERS}).",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text("Terms of Service — CODIVA Technologies (${CodivaCatalog.OFFICIAL_DOMAIN})", style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "1. Platform Use: By creating an account on CODIVA Technologies, you agree to provide accurate identity details and respect role boundaries across Academy, Client Delivery, and Community modules.\n\n" +
                        "2. Account Approval: Privileged roles (Staff, Lecturer, Partner, HOD, Finance) may require Super Administrator verification before elevated tools activate.\n\n" +
                        "3. Intellectual Property: Official curriculum materials, brand assets, and software deliverables remain governed by CODIVA Technologies client and Academy agreements.\n\n" +
                        "4. Service Availability: Scheduled maintenance windows may temporarily pause public form submissions while preserving access to existing records.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onDismiss, modifier = Modifier.minimumInteractiveComponentSize()) {
                    Text("Close")
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProductionPipelineCard(
    releaseCount: Int,
    onOpenInstallGuide: () -> Unit,
    onOpenLegalDocs: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .widthIn(max = 920.dp)
            .fillMaxWidth()
            .testTag("production_pipeline_card"),
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Rounded.RocketLaunch, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text("Release & Production Readiness", style = MaterialTheme.typography.titleMedium)
                }
                StatusBadge("v${BuildConfig.VERSION_NAME} (#${BuildConfig.VERSION_CODE})", BadgeTone.INFO)
            }
            val checks = listOf(
                "Adaptive App Icon & CODIVA Launch Splash configured",
                "Backend Active: Cloud Firestore + Google Auth (${BuildConfig.APPLICATION_ID})",
                "Multi-Platform Release Catalog: $releaseCount live channel(s) synced",
                "Privacy Policy & Terms of Service bundled and accessible"
            )
            checks.forEach { line ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = null,
                        tint = CodivaEmerald,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = line,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onOpenInstallGuide,
                    modifier = Modifier.minimumInteractiveComponentSize()
                ) {
                    Icon(Icons.Rounded.InstallMobile, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Install / PWA Preview")
                }
                OutlinedButton(
                    onClick = onOpenLegalDocs,
                    modifier = Modifier.minimumInteractiveComponentSize()
                ) {
                    Icon(Icons.Rounded.PrivacyTip, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Privacy & Terms")
                }
            }
        }
    }
}

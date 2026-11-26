package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.AccountRole
import com.example.data.CodivaCatalog
import com.example.data.UserRoleManager
import com.example.ui.theme.CodivaAmber
import com.example.ui.theme.CodivaCobalt
import com.example.ui.theme.CodivaEmerald
import com.example.ui.theme.CodivaNavy
import com.example.ui.theme.CodivaNavyDeep
import com.example.ui.theme.CodivaNavyMid
import com.example.ui.theme.CodivaOrange
import com.example.ui.theme.CodivaSky
import com.example.ui.theme.CodivaSkyBright
import com.example.ui.theme.CodivaSlateLight
import com.example.ui.theme.CodivaSlateMuted
import com.example.ui.theme.CodivaViolet

data class FounderNavDestination(
    val id: String,
    val title: String,
    val subtitle: String,
    val metricLabel: String,
    val icon: ImageVector,
    val accent: Color,
    val selected: Boolean,
    val onClick: () -> Unit
)

@Composable
fun FounderCommandHeader(
    userCount: Int,
    pendingCount: Int,
    academyCount: Int,
    projectCount: Int,
    publicationCount: Int,
    ticketCount: Int,
    securityEventsCount: Int,
    revenueDollars: Long,
    maintenanceMode: Boolean,
    onOpenFullConsole: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.widthIn(max = 920.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CodivaNavy),
        border = BorderStroke(1.dp, CodivaSky.copy(alpha = 0.3f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(CodivaNavy, CodivaNavyMid, CodivaNavyDeep)
                    )
                )
                .padding(18.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("founder_profile_section"),
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
                                .size(70.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .border(1.5.dp, CodivaSky.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                                .testTag("founder_profile_avatar"),
                            contentScale = ContentScale.Crop
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                StatusBadge(
                                    text = if (maintenanceMode) "SUPER ADMIN • MAINTENANCE" else "SUPER ADMINISTRATOR",
                                    tone = if (maintenanceMode) BadgeTone.WARNING else BadgeTone.SUCCESS
                                )
                                Surface(
                                    shape = RoundedCornerShape(999.dp),
                                    color = CodivaSky.copy(alpha = 0.16f),
                                    border = BorderStroke(1.dp, CodivaSky.copy(alpha = 0.45f))
                                ) {
                                    Text(
                                        text = CodivaCatalog.OFFICIAL_DOMAIN,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = CodivaSky,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                            Text(
                                text = stringResource(R.string.founder_dashboard_title),
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${CodivaCatalog.FOUNDER_NAME} • Founder & CEO",
                                style = MaterialTheme.typography.labelMedium,
                                color = CodivaSky
                            )
                            Text(
                                text = "CODIVA TECHNOLOGIES",
                                style = MaterialTheme.typography.labelSmall,
                                color = CodivaSlateLight
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color.Black,
                        border = BorderStroke(1.dp, CodivaSky.copy(alpha = 0.45f)),
                        modifier = Modifier
                            .size(64.dp)
                            .testTag("founder_profile_logo")
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
                    color = CodivaSlateMuted
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AdminKpiTile("Registered", "$userCount", CodivaCobalt)
                    AdminKpiTile("Pending", "$pendingCount", CodivaAmber)
                    AdminKpiTile("Published Docs", "$publicationCount", CodivaEmerald)
                    AdminKpiTile("Academy", "$academyCount", CodivaSkyBright)
                    AdminKpiTile("Projects", "$projectCount", CodivaAmber)
                    AdminKpiTile("Open Tickets", "$ticketCount", CodivaOrange)
                    AdminKpiTile("Audit Logs", "$securityEventsCount", CodivaSkyBright)
                    AdminKpiTile("Revenue", "$$revenueDollars", CodivaViolet)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.founder_control_subtitle),
                        style = MaterialTheme.typography.labelSmall,
                        color = CodivaSlateMuted,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onOpenFullConsole,
                        modifier = Modifier.minimumInteractiveComponentSize()
                    ) {
                        Icon(Icons.Rounded.Dashboard, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(stringResource(R.string.founder_full_console))
                    }
                }
            }
        }
    }
}

@Composable
fun FounderNavigationHub(
    destinations: List<FounderNavDestination>,
    modifier: Modifier = Modifier
) {
    if (destinations.isEmpty()) return
    Column(
        modifier = modifier
            .widthIn(max = 920.dp)
            .fillMaxWidth()
            .testTag("founder_nav_hub"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = stringResource(R.string.founder_nav_hub_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val twoColumns = maxWidth >= 340.dp
            val columns = if (maxWidth >= 700.dp) 4 else if (twoColumns) 2 else 1
            val rows = destinations.chunked(columns)
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                rows.forEach { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        rowItems.forEach { dest ->
                            FounderNavTile(
                                destination = dest,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        repeat(columns - rowItems.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FounderNavTile(
    destination: FounderNavDestination,
    modifier: Modifier = Modifier
) {
    val containerColor = if (destination.selected) {
        destination.accent.copy(alpha = 0.14f)
    } else {
        MaterialTheme.colorScheme.surface
    }
    val borderColor = if (destination.selected) {
        destination.accent
    } else {
        MaterialTheme.colorScheme.outlineVariant
    }
    Card(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = destination.onClick)
            .testTag("founder_nav_card_${destination.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(if (destination.selected) 1.5.dp else 1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = destination.accent.copy(alpha = 0.16f),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = destination.icon,
                            contentDescription = destination.title,
                            tint = destination.accent,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = destination.accent.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = destination.metricLabel,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = destination.accent,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = destination.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = destination.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun FounderEmptyStateCard(
    icon: ImageVector,
    title: String,
    message: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .widthIn(max = 920.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(text = title, style = MaterialTheme.typography.titleSmall)
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (actionLabel != null && onAction != null) {
                    OutlinedButton(
                        onClick = onAction,
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .minimumInteractiveComponentSize()
                    ) {
                        Text(actionLabel)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FounderAccessRestrictedView(
    activeRole: AccountRole,
    roleManager: UserRoleManager = UserRoleManager.forRole(activeRole),
    onBackToRoles: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp)
            .testTag("founder_access_restricted"),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.widthIn(max = 560.dp),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatusBadge(text = "ACCESS RESTRICTED", tone = BadgeTone.WARNING)
                Text(
                    text = "Founder Dashboard requires Super Admin role",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = "Your active role is ${activeRole.label}. Only Super Administrator accounts have permission to access User, Academy, Project, and System Audit controls here.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Granted permissions for ${activeRole.label}:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    roleManager.permissionLabels().forEach { label ->
                        StatusBadge(text = label, tone = BadgeTone.INFO)
                    }
                }
                if (onBackToRoles != null) {
                    Button(
                        onClick = onBackToRoles,
                        modifier = Modifier.minimumInteractiveComponentSize()
                    ) {
                        Text("Return to ${activeRole.label} Dashboard")
                    }
                }
            }
        }
    }
}

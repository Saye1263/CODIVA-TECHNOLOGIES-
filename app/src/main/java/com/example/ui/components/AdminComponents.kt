package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.OpenInBrowser
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.AccountRole
import com.example.data.AuditLogEntry
import com.example.data.AuditSeverity
import com.example.data.MembershipStatus
import com.example.data.PublicPortalConfig
import com.example.data.UserProfile
import com.example.data.UserRoleManager
import com.example.data.WorkspaceRecord
import com.example.ui.theme.CodivaEmerald
import com.example.ui.theme.CodivaRose

@Composable
fun AdminKpiTile(label: String, value: String, accent: Color) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = accent.copy(alpha = 0.22f),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.55f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(value, style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color(0xFFE2E8F0))
        }
    }
}

@Composable
fun ModuleHeaderRow(
    eyebrow: String,
    title: String,
    buttonLabel: String,
    onAdd: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .widthIn(max = 920.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(eyebrow.uppercase(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            Text(title, style = MaterialTheme.typography.headlineSmall)
        }
        Button(
            onClick = onAdd,
            modifier = Modifier.minimumInteractiveComponentSize()
        ) {
            Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(buttonLabel)
        }
    }
}

@Composable
fun WorkspaceRecordCard(
    record: WorkspaceRecord,
    primaryActionLabel: String,
    onPrimaryAction: () -> Unit,
    onDelete: () -> Unit,
    isSubmitting: Boolean,
    modifier: Modifier = Modifier
) {
    val tone = when (record.status) {
        "ACTIVE", "PAID", "PUBLISHED", "RESOLVED", "COMPLETED" -> BadgeTone.SUCCESS
        "PENDING", "OPEN", "IN_PROGRESS" -> BadgeTone.WARNING
        else -> BadgeTone.NEUTRAL
    }
    Card(
        modifier = modifier.widthIn(max = 920.dp),
        shape = RoundedCornerShape(18.dp),
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
                Text(
                    text = if (record.amountCents > 0L) "${record.category} • $${record.amountCents / 100L}" else record.category,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                StatusBadge(text = record.status, tone = tone)
            }
            Text(record.title, style = MaterialTheme.typography.titleMedium)
            if (record.subtitle.isNotBlank()) {
                Text(record.subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
            }
            if (record.detail.isNotBlank()) {
                Text(record.detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = onPrimaryAction,
                    enabled = !isSubmitting,
                    modifier = Modifier.minimumInteractiveComponentSize()
                ) {
                    Text(primaryActionLabel)
                }
                IconButton(
                    onClick = onDelete,
                    enabled = !isSubmitting,
                    modifier = Modifier.minimumInteractiveComponentSize()
                ) {
                    Icon(Icons.Rounded.DeleteOutline, contentDescription = "Delete record")
                }
            }
        }
    }
}

@Composable
fun AuditLogCard(
    log: AuditLogEntry,
    modifier: Modifier = Modifier
) {
    val tone = when (log.severityEnum) {
        AuditSeverity.INFO -> BadgeTone.INFO
        AuditSeverity.WARNING -> BadgeTone.WARNING
        AuditSeverity.CRITICAL -> BadgeTone.NEUTRAL
    }
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.CheckCircle,
                contentDescription = null,
                tint = if (log.severityEnum == AuditSeverity.CRITICAL) CodivaRose else CodivaEmerald,
                modifier = Modifier.size(20.dp)
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(log.action, style = MaterialTheme.typography.titleSmall)
                Text("${log.target} • ${log.actorEmail}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            StatusBadge(text = log.severityEnum.label, tone = tone)
        }
    }
}

@Composable
fun SettingToggleCard(
    title: String,
    description: String,
    checked: Boolean,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = enabled) { onCheckedChange(!checked) }
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(title, style = MaterialTheme.typography.titleSmall)
                Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                enabled = enabled
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HostingLaunchCard(
    config: PublicPortalConfig,
    isSubmitting: Boolean,
    onCopyUrl: () -> Unit,
    onShareInvite: () -> Unit,
    onOpenLiveUrl: () -> Unit,
    onEditHosting: () -> Unit,
    onTogglePublished: (Boolean) -> Unit,
    onTogglePublicSignUp: (Boolean) -> Unit,
    onToggleRequireApproval: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.widthIn(max = 920.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Public,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Column {
                        Text(config.portalTitle, style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = if (config.customDomain.isNotBlank()) "Domain: ${config.customDomain} • 1,000+ Users Ready" else "Cloud Hosted Portal",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                StatusBadge(
                    text = if (config.portalPublished) "PUBLISHED ONLINE" else "PRIVATE",
                    tone = if (config.portalPublished) BadgeTone.SUCCESS else BadgeTone.WARNING
                )
            }

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Public Access Link",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = config.publicUrl,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "Invite Code: ${config.inviteCode}",
                        style = MaterialTheme.typography.labelMedium,
                        color = CodivaEmerald
                    )
                }
            }

            Text(
                text = config.welcomeMessage,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onShareInvite,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("hosting_share_button")
                ) {
                    Icon(Icons.Rounded.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Share Invite")
                }
                OutlinedButton(
                    onClick = onCopyUrl,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("hosting_copy_button")
                ) {
                    Icon(Icons.Rounded.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Copy Link")
                }
                OutlinedButton(
                    onClick = onOpenLiveUrl,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("hosting_open_url_button")
                ) {
                    Icon(Icons.Rounded.OpenInBrowser, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Open URL")
                }
                OutlinedButton(
                    onClick = onEditHosting,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("hosting_edit_button")
                ) {
                    Icon(Icons.Rounded.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Configure")
                }
            }

            SettingToggleCard(
                title = "Publish CODIVA Online",
                description = "Make the CODIVA portal live for public access via the shared URL.",
                checked = config.portalPublished,
                enabled = !isSubmitting,
                onCheckedChange = onTogglePublished
            )
            SettingToggleCard(
                title = "Allow Public Account Creation",
                description = "Let new Users, Members, Parents, Staff, and Lecturers create accounts.",
                checked = config.allowPublicSignUp,
                enabled = !isSubmitting,
                onCheckedChange = onTogglePublicSignUp
            )
            SettingToggleCard(
                title = "Require Admin Approval",
                description = "Hold self-registered accounts in Pending state until Super Admin approves.",
                checked = config.requireAdminApproval,
                enabled = !isSubmitting,
                onCheckedChange = onToggleRequireApproval
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RegisteredUserAccountCard(
    user: UserProfile,
    isSubmitting: Boolean,
    onAssignRole: (AccountRole) -> Unit,
    onChangeStatus: (MembershipStatus) -> Unit,
    modifier: Modifier = Modifier
) {
    val isFounderAccount = UserRoleManager.isFounderEmail(user.email)
    val statusTone = when (user.statusEnum) {
        MembershipStatus.ACTIVE -> BadgeTone.SUCCESS
        MembershipStatus.PENDING -> BadgeTone.WARNING
        MembershipStatus.SUSPENDED -> BadgeTone.NEUTRAL
    }
    Card(
        modifier = modifier.widthIn(max = 920.dp),
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(user.displayName, style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = if (user.organization.isNotBlank()) "${user.email} • ${user.organization}" else user.email,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                StatusBadge(text = user.statusEnum.label, tone = statusTone)
            }

            if (!isFounderAccount) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf(
                        AccountRole.MEMBER,
                        AccountRole.USER,
                        AccountRole.STAFF,
                        AccountRole.LECTURER,
                        AccountRole.PARENT,
                        AccountRole.PARTNER,
                        AccountRole.HOD,
                        AccountRole.FINANCE
                    ).forEach { role ->
                        FilterChip(
                            selected = user.roleEnum == role,
                            onClick = { if (!isSubmitting) onAssignRole(role) },
                            label = { Text(role.label) }
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    if (user.statusEnum != MembershipStatus.ACTIVE) {
                        TextButton(
                            onClick = { onChangeStatus(MembershipStatus.ACTIVE) },
                            enabled = !isSubmitting,
                            modifier = Modifier.minimumInteractiveComponentSize()
                        ) {
                            Text("Approve Account")
                        }
                    }
                    if (user.statusEnum != MembershipStatus.SUSPENDED) {
                        TextButton(
                            onClick = { onChangeStatus(MembershipStatus.SUSPENDED) },
                            enabled = !isSubmitting,
                            modifier = Modifier.minimumInteractiveComponentSize()
                        ) {
                            Text("Suspend")
                        }
                    }
                }
            } else {
                StatusBadge(text = "Founder & Super Administrator", tone = BadgeTone.INFO)
            }
        }
    }
}

package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.DesktopWindows
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.HelpOutline
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.OpenInBrowser
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.PhoneIphone
import androidx.compose.material.icons.rounded.SupportAgent
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.AdminPublication
import com.example.data.AppRelease
import com.example.data.CodivaCatalog
import com.example.data.PublicationCategory
import com.example.data.PublicationStatus
import com.example.data.ReleasePlatform
import com.example.data.ReleaseStatus

@Composable
fun MaintenanceNoticeBanner(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .widthIn(max = 920.dp)
            .fillMaxWidth()
            .testTag("maintenance_notice_banner"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.Build,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onErrorContainer
            )
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "Scheduled Maintenance Mode Active",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Text(
                    text = "CODIVA Administration is performing platform upgrades. Existing records remain readable while new public changes are paused.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AdminPublicationCard(
    publication: AdminPublication,
    isAdminMode: Boolean,
    isSubmitting: Boolean,
    onReadMore: () -> Unit,
    onOpenDocument: (String) -> Unit,
    onEdit: () -> Unit = {},
    onChangeStatus: (PublicationStatus) -> Unit = {},
    onDelete: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val statusTone = when (publication.statusEnum) {
        PublicationStatus.PUBLISHED -> BadgeTone.SUCCESS
        PublicationStatus.DRAFT -> BadgeTone.INFO
        PublicationStatus.EXPIRED -> BadgeTone.WARNING
    }
    Card(
        modifier = modifier
            .widthIn(max = 920.dp)
            .fillMaxWidth()
            .clickable(onClick = onReadMore)
            .testTag("publication_card_${publication.id}"),
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Description,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "${publication.categoryEnum.label} • Audience: ${publication.targetRole}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                StatusBadge(text = publication.statusEnum.label, tone = statusTone)
            }

            Text(publication.title, style = MaterialTheme.typography.titleMedium)
            Text(
                text = publication.summary,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (publication.expiresAtLabel.isNotBlank()) {
                Text(
                    text = "Valid until: ${publication.expiresAtLabel}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                TextButton(
                    onClick = onReadMore,
                    modifier = Modifier.minimumInteractiveComponentSize()
                ) {
                    Text("Read Details")
                }
                if (publication.documentUrl.isNotBlank()) {
                    OutlinedButton(
                        onClick = { onOpenDocument(publication.documentUrl) },
                        modifier = Modifier.minimumInteractiveComponentSize()
                    ) {
                        Icon(Icons.Rounded.OpenInBrowser, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(publication.documentLabel.ifBlank { "Open Document" })
                    }
                }
                if (isAdminMode) {
                    OutlinedButton(
                        onClick = onEdit,
                        enabled = !isSubmitting,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("pub_edit_${publication.id}")
                    ) {
                        Icon(Icons.Rounded.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Edit")
                    }
                    TextButton(
                        onClick = {
                            val next = if (publication.statusEnum == PublicationStatus.PUBLISHED) {
                                PublicationStatus.EXPIRED
                            } else {
                                PublicationStatus.PUBLISHED
                            }
                            onChangeStatus(next)
                        },
                        enabled = !isSubmitting,
                        modifier = Modifier.minimumInteractiveComponentSize()
                    ) {
                        Text(
                            if (publication.statusEnum == PublicationStatus.PUBLISHED) "Mark Expired" else "Publish"
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        enabled = !isSubmitting,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("pub_delete_${publication.id}")
                    ) {
                        Icon(Icons.Rounded.DeleteOutline, contentDescription = "Delete publication")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublicationReaderSheet(
    publication: AdminPublication,
    onOpenDocument: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatusBadge(
                text = "${publication.categoryEnum.label} • Audience: ${publication.targetRole}",
                tone = BadgeTone.INFO
            )
            Text(publication.title, style = MaterialTheme.typography.headlineSmall)
            Text(
                text = publication.summary,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(publication.content, style = MaterialTheme.typography.bodyLarge)
            if (publication.expiresAtLabel.isNotBlank()) {
                Text(
                    text = "Schedule / Expiry: ${publication.expiresAtLabel}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            if (publication.documentUrl.isNotBlank()) {
                Button(
                    onClick = { onOpenDocument(publication.documentUrl) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .minimumInteractiveComponentSize()
                ) {
                    Icon(Icons.Rounded.OpenInBrowser, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(publication.documentLabel.ifBlank { "Open Attached Document" })
                }
            }
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .minimumInteractiveComponentSize()
            ) {
                Text("Close")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PublicationEditorSheet(
    existing: AdminPublication?,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onSave: (
        existingId: String?,
        title: String,
        summary: String,
        content: String,
        category: PublicationCategory,
        targetRole: String,
        documentUrl: String,
        documentLabel: String,
        expiresAtLabel: String,
        status: PublicationStatus
    ) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var title by remember(existing) { mutableStateOf(existing?.title ?: "") }
    var summary by remember(existing) { mutableStateOf(existing?.summary ?: "") }
    var content by remember(existing) { mutableStateOf(existing?.content ?: "") }
    var category by remember(existing) { mutableStateOf(existing?.categoryEnum ?: PublicationCategory.ANNOUNCEMENT) }
    var targetRole by remember(existing) { mutableStateOf(existing?.targetRole ?: "ALL") }
    var documentUrl by remember(existing) { mutableStateOf(existing?.documentUrl ?: "") }
    var documentLabel by remember(existing) { mutableStateOf(existing?.documentLabel ?: "") }
    var expiresAtLabel by remember(existing) { mutableStateOf(existing?.expiresAtLabel ?: "") }
    var status by remember(existing) { mutableStateOf(existing?.statusEnum ?: PublicationStatus.PUBLISHED) }
    var validationError by remember { mutableStateOf<String?>(null) }

    val audiences = listOf("ALL", "MEMBER", "USER", "STAFF", "LECTURER", "PARENT", "HOD", "FINANCE", "PARTNER")

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = if (existing == null) "Post Official Information or Document" else "Edit Publication / Document",
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = "Published items appear immediately on user dashboards matching the target role.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = title,
                onValueChange = {
                    title = it
                    validationError = null
                },
                label = { Text("Headline or document title") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("pub_title_input")
            )
            OutlinedTextField(
                value = summary,
                onValueChange = {
                    summary = it
                    validationError = null
                },
                label = { Text("Short summary") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("pub_summary_input")
            )
            OutlinedTextField(
                value = content,
                onValueChange = {
                    content = it
                    validationError = null
                },
                label = { Text("Full notice / details") },
                minLines = 3,
                maxLines = 5,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("pub_content_input")
            )
            OutlinedTextField(
                value = documentUrl,
                onValueChange = { documentUrl = it },
                label = { Text("Document or file URL (optional)") },
                placeholder = { Text("https://...") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = documentLabel,
                onValueChange = { documentLabel = it },
                label = { Text("Button label for document (optional)") },
                placeholder = { Text("e.g., Download PDF Handbook") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = expiresAtLabel,
                onValueChange = { expiresAtLabel = it },
                label = { Text("Expiry date note (optional)") },
                placeholder = { Text("e.g., Dec 31, 2026") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Text("Category", style = MaterialTheme.typography.titleSmall)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                PublicationCategory.entries.forEach { cat ->
                    FilterChip(
                        selected = category == cat,
                        onClick = { category = cat },
                        label = { Text(cat.label) }
                    )
                }
            }

            Text("Target Dashboard Audience", style = MaterialTheme.typography.titleSmall)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                audiences.forEach { role ->
                    FilterChip(
                        selected = targetRole == role,
                        onClick = { targetRole = role },
                        label = { Text(role) }
                    )
                }
            }

            Text("Status", style = MaterialTheme.typography.titleSmall)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PublicationStatus.entries.forEach { st ->
                    FilterChip(
                        selected = status == st,
                        onClick = { status = st },
                        label = { Text(st.label) }
                    )
                }
            }

            if (validationError != null) {
                Text(validationError!!, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onDismiss, modifier = Modifier.minimumInteractiveComponentSize()) {
                    Text("Cancel")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        val t = title.trim()
                        val s = summary.trim()
                        val c = content.trim()
                        when {
                            t.length < 2 -> validationError = "Title must be at least 2 characters."
                            s.length < 2 -> validationError = "Summary must be at least 2 characters."
                            c.length < 5 -> validationError = "Details must be at least 5 characters."
                            else -> onSave(
                                existing?.id,
                                t,
                                s,
                                c,
                                category,
                                targetRole,
                                documentUrl.trim(),
                                documentLabel.trim(),
                                expiresAtLabel.trim(),
                                status
                            )
                        }
                    },
                    enabled = !isSubmitting,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("pub_save_button")
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    } else {
                        Text(if (existing == null) "Publish Now" else "Save Changes")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AppReleaseCard(
    release: AppRelease,
    isAdminMode: Boolean,
    isSubmitting: Boolean,
    onOpenDownload: (String) -> Unit,
    onEdit: () -> Unit = {},
    onChangeStatus: (ReleaseStatus) -> Unit = {},
    onDelete: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val platform = release.platformEnum
    val icon = when (platform) {
        ReleasePlatform.ANDROID -> Icons.Rounded.PhoneAndroid
        ReleasePlatform.IOS -> Icons.Rounded.PhoneIphone
        ReleasePlatform.WINDOWS -> Icons.Rounded.DesktopWindows
        ReleasePlatform.WEB -> Icons.Rounded.Language
    }
    val statusTone = when (release.statusEnum) {
        ReleaseStatus.LIVE -> BadgeTone.SUCCESS
        ReleaseStatus.BETA -> BadgeTone.INFO
        ReleaseStatus.RETIRED -> BadgeTone.WARNING
    }

    Card(
        modifier = modifier
            .widthIn(max = 920.dp)
            .fillMaxWidth()
            .testTag("release_card_${release.id}"),
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = platform.label,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier
                                .padding(10.dp)
                                .size(22.dp)
                        )
                    }
                    Column {
                        Text("${platform.label} • v${release.versionName} (#${release.buildNumber})", style = MaterialTheme.typography.titleMedium)
                        Text(platform.storeLabel, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    }
                }
                StatusBadge(text = release.statusEnum.label, tone = statusTone)
            }

            Text(
                text = release.releaseNotes,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Button(
                    onClick = { onOpenDownload(release.downloadUrl) },
                    modifier = Modifier.minimumInteractiveComponentSize()
                ) {
                    Icon(Icons.Rounded.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (platform == ReleasePlatform.WEB) "Open ${CodivaCatalog.OFFICIAL_DOMAIN}" else "Download / Store")
                }
                if (isAdminMode) {
                    OutlinedButton(
                        onClick = onEdit,
                        enabled = !isSubmitting,
                        modifier = Modifier.minimumInteractiveComponentSize()
                    ) {
                        Icon(Icons.Rounded.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Edit")
                    }
                    TextButton(
                        onClick = {
                            val next = if (release.statusEnum == ReleaseStatus.LIVE) {
                                ReleaseStatus.RETIRED
                            } else {
                                ReleaseStatus.LIVE
                            }
                            onChangeStatus(next)
                        },
                        enabled = !isSubmitting,
                        modifier = Modifier.minimumInteractiveComponentSize()
                    ) {
                        Text(if (release.statusEnum == ReleaseStatus.LIVE) "Retire" else "Set Live")
                    }
                    IconButton(
                        onClick = onDelete,
                        enabled = !isSubmitting,
                        modifier = Modifier.minimumInteractiveComponentSize()
                    ) {
                        Icon(Icons.Rounded.DeleteOutline, contentDescription = "Delete release")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AppReleaseEditorSheet(
    existing: AppRelease?,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onSave: (
        existingId: String?,
        platform: ReleasePlatform,
        versionName: String,
        buildNumber: Long,
        releaseNotes: String,
        downloadUrl: String,
        status: ReleaseStatus
    ) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var platform by remember(existing) { mutableStateOf(existing?.platformEnum ?: ReleasePlatform.ANDROID) }
    var versionName by remember(existing) { mutableStateOf(existing?.versionName ?: "1.0.0") }
    var buildNumberText by remember(existing) { mutableStateOf((existing?.buildNumber ?: 1L).toString()) }
    var releaseNotes by remember(existing) { mutableStateOf(existing?.releaseNotes ?: "") }
    var downloadUrl by remember(existing) { mutableStateOf(existing?.downloadUrl ?: CodivaCatalog.PUBLIC_PORTAL_URL) }
    var status by remember(existing) { mutableStateOf(existing?.statusEnum ?: ReleaseStatus.LIVE) }
    var validationError by remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = if (existing == null) "Add App Store / Download Release" else "Edit App Release",
                style = MaterialTheme.typography.headlineSmall
            )
            Text("Platform", style = MaterialTheme.typography.titleSmall)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                ReleasePlatform.entries.forEach { p ->
                    FilterChip(
                        selected = platform == p,
                        onClick = { platform = p },
                        label = { Text(p.label) }
                    )
                }
            }
            OutlinedTextField(
                value = versionName,
                onValueChange = {
                    versionName = it
                    validationError = null
                },
                label = { Text("Version name (e.g., 1.2.0)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = buildNumberText,
                onValueChange = { buildNumberText = it.filter { ch -> ch.isDigit() } },
                label = { Text("Build number") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = downloadUrl,
                onValueChange = {
                    downloadUrl = it
                    validationError = null
                },
                label = { Text("Play Store / App Store / APK / Web URL") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = releaseNotes,
                onValueChange = {
                    releaseNotes = it
                    validationError = null
                },
                label = { Text("Release notes & installation instructions") },
                minLines = 2,
                maxLines = 4,
                modifier = Modifier.fillMaxWidth()
            )
            Text("Release Status", style = MaterialTheme.typography.titleSmall)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ReleaseStatus.entries.forEach { st ->
                    FilterChip(
                        selected = status == st,
                        onClick = { status = st },
                        label = { Text(st.label) }
                    )
                }
            }
            if (validationError != null) {
                Text(validationError!!, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onDismiss, modifier = Modifier.minimumInteractiveComponentSize()) {
                    Text("Cancel")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        val v = versionName.trim()
                        val b = (buildNumberText.toLongOrNull() ?: 1L).coerceAtLeast(1L)
                        val notes = releaseNotes.trim()
                        val url = downloadUrl.trim()
                        when {
                            v.isEmpty() -> validationError = "Enter a version name."
                            url.length < 8 -> validationError = "Enter a valid store or download URL."
                            notes.length < 3 -> validationError = "Enter brief release notes."
                            else -> onSave(existing?.id, platform, v, b, notes, url, status)
                        }
                    },
                    enabled = !isSubmitting,
                    modifier = Modifier.minimumInteractiveComponentSize()
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Save Release")
                    }
                }
            }
        }
    }
}

@Composable
fun HelpCenterSectionCard(
    onOpenSupportTicket: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expandedId by remember { mutableStateOf<String?>(CodivaCatalog.helpFaqs.first().id) }
    Card(
        modifier = modifier
            .widthIn(max = 920.dp)
            .fillMaxWidth()
            .testTag("help_center_card"),
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
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Rounded.HelpOutline, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text("Help Centre, FAQs & Guides", style = MaterialTheme.typography.titleMedium)
                }
                OutlinedButton(
                    onClick = onOpenSupportTicket,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("help_submit_ticket_button")
                ) {
                    Icon(Icons.Rounded.SupportAgent, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("New Ticket")
                }
            }

            CodivaCatalog.helpFaqs.forEach { faq ->
                val expanded = expandedId == faq.id
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expandedId = if (expanded) null else faq.id }
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(faq.category, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                Text(faq.question, style = MaterialTheme.typography.titleSmall)
                            }
                            Icon(
                                imageVector = if (expanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                                contentDescription = if (expanded) "Collapse" else "Expand"
                            )
                        }
                        AnimatedVisibility(visible = expanded) {
                            Text(
                                text = faq.answer,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.AccountRole
import com.example.data.CareerRole
import com.example.data.CodivaCatalog
import com.example.data.NewsArticle
import com.example.data.PublicPortalConfig
import com.example.data.UserProfile
import com.example.data.UserRoleManager
import com.example.data.WorkspaceModuleType

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProjectInquirySheet(
    initialService: String,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (serviceCategory: String, projectTitle: String, details: String, budgetTier: String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedService by remember(initialService) {
        mutableStateOf(initialService.ifBlank { CodivaCatalog.services.first().title })
    }
    var projectTitle by remember { mutableStateOf("") }
    var details by remember { mutableStateOf("") }
    var selectedTimeline by remember { mutableStateOf("Standard (4–8 weeks)") }
    var validationError by remember { mutableStateOf<String?>(null) }

    val timelines = listOf("Urgent (2–4 weeks)", "Standard (4–8 weeks)", "Long-term Partnership")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Tell us about your project",
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = "Your enquiry is confidential, with no obligation to proceed. Track its status in your CODIVA Portal.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text("Service Category", style = MaterialTheme.typography.titleSmall)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                CodivaCatalog.services.forEach { service ->
                    FilterChip(
                        selected = selectedService == service.title,
                        onClick = { selectedService = service.title },
                        label = { Text(service.title) }
                    )
                }
            }

            OutlinedTextField(
                value = projectTitle,
                onValueChange = {
                    projectTitle = it
                    validationError = null
                },
                label = { Text("Project title") },
                placeholder = { Text("e.g., Clinic appointment mobile app") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("inquiry_title_input")
            )

            OutlinedTextField(
                value = details,
                onValueChange = {
                    details = it
                    validationError = null
                },
                label = { Text("Project goals & requirements") },
                placeholder = { Text("Share what you are building, target users, and key workflows...") },
                minLines = 3,
                maxLines = 5,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("inquiry_details_input")
            )

            Text("Preferred Timeline", style = MaterialTheme.typography.titleSmall)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                timelines.forEach { tier ->
                    FilterChip(
                        selected = selectedTimeline == tier,
                        onClick = { selectedTimeline = tier },
                        label = { Text(tier) }
                    )
                }
            }

            if (validationError != null) {
                Text(
                    text = validationError!!,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.minimumInteractiveComponentSize()
                ) {
                    Text("Cancel")
                }
                Spacer(modifier = Modifier.size(8.dp))
                Button(
                    onClick = {
                        val t = projectTitle.trim()
                        val d = details.trim()
                        when {
                            t.length < 3 -> validationError = "Please enter a project title (at least 3 characters)."
                            d.length < 10 -> validationError = "Please describe your project (at least 10 characters)."
                            else -> onSubmit(selectedService, t, d, selectedTimeline)
                        }
                    },
                    enabled = !isSubmitting,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("submit_inquiry_button")
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Submit Inquiry")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CareerApplicationSheet(
    role: CareerRole,
    defaultName: String,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (applicantName: String, portfolioNote: String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var applicantName by remember(defaultName) { mutableStateOf(defaultName) }
    var portfolioNote by remember { mutableStateOf("") }
    var validationError by remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Apply: ${role.title}",
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = "${role.department} • ${role.location} • ${role.employmentType}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = role.summary,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = applicantName,
                onValueChange = {
                    applicantName = it
                    validationError = null
                },
                label = { Text("Full name") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("career_name_input")
            )

            OutlinedTextField(
                value = portfolioNote,
                onValueChange = {
                    portfolioNote = it
                    validationError = null
                },
                label = { Text("Experience & portfolio link") },
                placeholder = { Text("Summarize relevant work or include your GitHub / portfolio URL...") },
                minLines = 3,
                maxLines = 5,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("career_note_input")
            )

            if (validationError != null) {
                Text(
                    text = validationError!!,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.minimumInteractiveComponentSize()
                ) {
                    Text("Close")
                }
                Spacer(modifier = Modifier.size(8.dp))
                Button(
                    onClick = {
                        val n = applicantName.trim()
                        val p = portfolioNote.trim()
                        when {
                            n.length < 2 -> validationError = "Please enter your full name."
                            p.length < 5 -> validationError = "Please add a short experience note."
                            else -> onSubmit(n, p)
                        }
                    },
                    enabled = !isSubmitting,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("submit_application_button")
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Submit Application")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProfileEditSheet(
    profile: UserProfile?,
    fallbackName: String,
    userEmail: String? = profile?.email,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onSave: (displayName: String, role: AccountRole, organization: String, phone: String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val currentPersistedRole = profile?.roleEnum
    val assignableRoles = remember(userEmail, currentPersistedRole) {
        UserRoleManager.assignableRolesFor(userEmail, currentPersistedRole)
    }
    val initialRole = remember(profile, userEmail) {
        val base = profile?.roleEnum ?: if (UserRoleManager.isFounderEmail(userEmail)) AccountRole.SUPER_ADMIN else AccountRole.MEMBER
        UserRoleManager.resolveEffectiveRole(base, userEmail, currentPersistedRole)
    }
    var displayName by remember(profile) { mutableStateOf(profile?.displayName?.ifBlank { fallbackName } ?: fallbackName) }
    var selectedRole by remember(initialRole) { mutableStateOf(initialRole) }
    var organization by remember(profile) { mutableStateOf(profile?.organization ?: "") }
    var phone by remember(profile) { mutableStateOf(profile?.phone ?: "") }
    var validationError by remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "CODIVA Ecosystem Profile & Role",
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = "Select your account dashboard role across the CODIVA Technologies ecosystem.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text("Account Dashboard Type", style = MaterialTheme.typography.titleSmall)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                assignableRoles.forEach { role ->
                    FilterChip(
                        selected = selectedRole == role,
                        onClick = { selectedRole = role },
                        label = { Text(role.label) }
                    )
                }
            }
            Text(
                text = "Access: " + UserRoleManager.permissionLabelsFor(selectedRole).joinToString(" • "),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )

            OutlinedTextField(
                value = displayName,
                onValueChange = {
                    displayName = it
                    validationError = null
                },
                label = { Text("Display name") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("profile_name_input")
            )

            OutlinedTextField(
                value = organization,
                onValueChange = { organization = it },
                label = { Text("Organization or department (optional)") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("profile_org_input")
            )

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Phone or WhatsApp (optional)") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("profile_phone_input")
            )

            if (validationError != null) {
                Text(
                    text = validationError!!,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.minimumInteractiveComponentSize()
                ) {
                    Text("Cancel")
                }
                Spacer(modifier = Modifier.size(8.dp))
                Button(
                    onClick = {
                        val cleanName = displayName.trim()
                        if (cleanName.isEmpty()) {
                            validationError = "Display name cannot be empty."
                        } else {
                            onSave(cleanName, selectedRole, organization.trim(), phone.trim())
                        }
                    },
                    enabled = !isSubmitting,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("save_profile_button")
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Save Profile")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddWorkspaceRecordSheet(
    moduleType: WorkspaceModuleType,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (title: String, subtitle: String, detail: String, category: String, status: String, amountCents: Long) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var title by remember { mutableStateOf("") }
    var subtitle by remember { mutableStateOf("") }
    var detail by remember { mutableStateOf("") }
    var amountDollars by remember { mutableStateOf("50") }
    val statusOptions = when (moduleType) {
        WorkspaceModuleType.USER -> listOf("ACTIVE", "PENDING")
        WorkspaceModuleType.PAYMENT -> listOf("PAID", "PENDING")
        WorkspaceModuleType.SUPPORT -> listOf("OPEN", "RESOLVED")
        WorkspaceModuleType.TASK -> listOf("IN_PROGRESS", "COMPLETED")
        WorkspaceModuleType.ANNOUNCEMENT, WorkspaceModuleType.NEWS -> listOf("PUBLISHED", "DRAFT")
        else -> listOf("ACTIVE", "ARCHIVED")
    }
    val categoryOptions = when (moduleType) {
        WorkspaceModuleType.USER -> listOf("USER", "MEMBER", "STAFF", "LECTURER", "PARENT", "HOD", "FINANCE")
        WorkspaceModuleType.PAYMENT -> listOf("Tuition", "Project", "Service", "Product")
        WorkspaceModuleType.EMPLOYEE -> listOf("Engineering", "Academy", "Operations", "Finance")
        WorkspaceModuleType.ANNOUNCEMENT -> listOf("IMPORTANT", "NORMAL", "EVENT")
        WorkspaceModuleType.NEWS -> listOf("Announcement", "Article", "Press")
        WorkspaceModuleType.COURSE -> listOf("TRAINING", "CERTIFICATION")
        else -> listOf("General", "Priority")
    }
    var selectedStatus by remember { mutableStateOf(statusOptions.first()) }
    var selectedCategory by remember { mutableStateOf(categoryOptions.first()) }
    var validationError by remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Add ${moduleType.label} Entry",
                style = MaterialTheme.typography.headlineSmall
            )

            OutlinedTextField(
                value = title,
                onValueChange = {
                    title = it
                    validationError = null
                },
                label = {
                    Text(
                        when (moduleType) {
                            WorkspaceModuleType.USER -> "Full name"
                            WorkspaceModuleType.PAYMENT -> "Invoice or payment title"
                            WorkspaceModuleType.COURSE -> "Course title"
                            WorkspaceModuleType.EMPLOYEE -> "Employee name"
                            else -> "Title"
                        }
                    )
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("workspace_title_input")
            )

            OutlinedTextField(
                value = subtitle,
                onValueChange = { subtitle = it },
                label = {
                    Text(
                        when (moduleType) {
                            WorkspaceModuleType.USER -> "Email address"
                            WorkspaceModuleType.EMPLOYEE -> "Department & role"
                            WorkspaceModuleType.PAYMENT -> "Client or student name"
                            else -> "Summary or subtitle"
                        }
                    )
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("workspace_subtitle_input")
            )

            OutlinedTextField(
                value = detail,
                onValueChange = { detail = it },
                label = { Text("Notes / description") },
                minLines = 2,
                maxLines = 4,
                modifier = Modifier.fillMaxWidth()
            )

            if (moduleType == WorkspaceModuleType.PAYMENT) {
                OutlinedTextField(
                    value = amountDollars,
                    onValueChange = { amountDollars = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Amount (USD)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Text("Category", style = MaterialTheme.typography.titleSmall)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                categoryOptions.forEach { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat) }
                    )
                }
            }

            Text("Initial Status", style = MaterialTheme.typography.titleSmall)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                statusOptions.forEach { st ->
                    FilterChip(
                        selected = selectedStatus == st,
                        onClick = { selectedStatus = st },
                        label = { Text(st) }
                    )
                }
            }

            if (validationError != null) {
                Text(
                    text = validationError!!,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.minimumInteractiveComponentSize()
                ) {
                    Text("Cancel")
                }
                Spacer(modifier = Modifier.size(8.dp))
                Button(
                    onClick = {
                        val cleanTitle = title.trim()
                        if (cleanTitle.length < 2) {
                            validationError = "Title must be at least 2 characters."
                        } else {
                            val cents = if (moduleType == WorkspaceModuleType.PAYMENT) {
                                (amountDollars.toLongOrNull() ?: 0L) * 100L
                            } else 0L
                            onSubmit(cleanTitle, subtitle.trim(), detail.trim(), selectedCategory, selectedStatus, cents)
                        }
                    },
                    enabled = !isSubmitting,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("save_workspace_record_button")
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Save Entry")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsDetailSheet(
    article: NewsArticle,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatusBadge(text = "${article.category} • ${article.publishedDate}", tone = BadgeTone.INFO)
            Text(
                text = article.title,
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = article.summary,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = article.fullStory,
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(6.dp))
            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .minimumInteractiveComponentSize()
            ) {
                Text("Done")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HostingSettingsSheet(
    initialConfig: PublicPortalConfig,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onSave: (PublicPortalConfig) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var portalTitle by remember(initialConfig) { mutableStateOf(initialConfig.portalTitle) }
    var publicUrl by remember(initialConfig) { mutableStateOf(initialConfig.publicUrl) }
    var customDomain by remember(initialConfig) { mutableStateOf(initialConfig.customDomain) }
    var inviteCode by remember(initialConfig) { mutableStateOf(initialConfig.inviteCode) }
    var welcomeMessage by remember(initialConfig) { mutableStateOf(initialConfig.welcomeMessage) }
    var validationError by remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Public Hosting & Portal Launch",
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = "Configure your hosted CODIVA URL, custom domain, team invite code, and public onboarding greeting.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = portalTitle,
                onValueChange = {
                    portalTitle = it
                    validationError = null
                },
                label = { Text("Portal title") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("hosting_title_input")
            )

            OutlinedTextField(
                value = publicUrl,
                onValueChange = {
                    publicUrl = it
                    validationError = null
                },
                label = { Text("Published portal URL") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("hosting_url_input")
            )

            OutlinedTextField(
                value = customDomain,
                onValueChange = { customDomain = it },
                label = { Text("Custom domain (optional)") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("hosting_domain_input")
            )

            OutlinedTextField(
                value = inviteCode,
                onValueChange = {
                    inviteCode = it
                    validationError = null
                },
                label = { Text("Onboarding invite code") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("hosting_invite_input")
            )

            OutlinedTextField(
                value = welcomeMessage,
                onValueChange = {
                    welcomeMessage = it
                    validationError = null
                },
                label = { Text("Public welcome message") },
                minLines = 2,
                maxLines = 4,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("hosting_welcome_input")
            )

            if (validationError != null) {
                Text(
                    text = validationError!!,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.minimumInteractiveComponentSize()
                ) {
                    Text("Cancel")
                }
                Spacer(modifier = Modifier.size(8.dp))
                Button(
                    onClick = {
                        val t = portalTitle.trim()
                        val u = publicUrl.trim()
                        val c = inviteCode.trim()
                        val w = welcomeMessage.trim()
                        when {
                            t.length < 2 -> validationError = "Portal title must be at least 2 characters."
                            u.length < 8 -> validationError = "Please enter a valid portal URL."
                            c.length < 3 -> validationError = "Invite code must be at least 3 characters."
                            w.length < 5 -> validationError = "Welcome message must be at least 5 characters."
                            else -> onSave(
                                initialConfig.copy(
                                    portalTitle = t,
                                    publicUrl = u,
                                    customDomain = customDomain.trim(),
                                    inviteCode = c,
                                    welcomeMessage = w
                                )
                            )
                        }
                    },
                    enabled = !isSubmitting,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("save_hosting_config_button")
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Publish Changes")
                    }
                }
            }
        }
    }
}

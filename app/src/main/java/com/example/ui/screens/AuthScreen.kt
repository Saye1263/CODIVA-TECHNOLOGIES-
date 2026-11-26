package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AdminPanelSettings
import androidx.compose.material.icons.rounded.InstallMobile
import androidx.compose.material.icons.rounded.Login
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material.icons.rounded.PrivacyTip
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import com.example.R
import com.example.auth.GoogleAuthHelper
import com.example.data.AccountOnboardingInput
import com.example.data.AccountRole
import com.example.data.CodivaCatalog
import com.example.data.UserRoleManager
import com.example.ui.components.ErrorBanner
import com.example.ui.components.InstallPlatformSheet
import com.example.ui.components.LegalDocumentsSheet
import com.example.ui.theme.CodivaCobalt
import com.example.ui.theme.CodivaEmerald
import com.example.ui.theme.CodivaNavy
import com.example.ui.theme.CodivaSky
import com.example.ui.theme.CodivaSlateLight
import com.example.ui.theme.CodivaSlateMuted

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    onAuthSuccess: (AccountOnboardingInput?) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    var authMode by remember { mutableIntStateOf(0) } // 0 = Sign In, 1 = Create Account
    var selectedRole by remember { mutableStateOf(AccountRole.MEMBER) }
    var fullName by remember { mutableStateOf("") }
    var organization by remember { mutableStateOf("") }
    var inviteCode by remember { mutableStateOf("") }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showInstallSheet by remember { mutableStateOf(false) }
    var showLegalSheet by remember { mutableStateOf(false) }

    val signupRoles = remember { UserRoleManager.selfSignupRoles() }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color.Black, CodivaNavy, MaterialTheme.colorScheme.background)
                )
            )
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        val isWide = maxWidth >= 720.dp

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (isWide) {
                Row(
                    modifier = Modifier
                        .widthIn(max = 1040.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(22.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        VisionPillBadge()
                        BrandHeroCard()
                    }
                    AuthPortalCard(
                        authMode = authMode,
                        onAuthModeChange = {
                            authMode = it
                            errorMessage = null
                        },
                        signupRoles = signupRoles,
                        selectedRole = selectedRole,
                        onSelectRole = { selectedRole = it },
                        fullName = fullName,
                        onFullNameChange = { fullName = it },
                        organization = organization,
                        onOrganizationChange = { organization = it },
                        inviteCode = inviteCode,
                        onInviteCodeChange = { inviteCode = it },
                        isLoading = isLoading,
                        errorMessage = errorMessage,
                        onSubmitAuth = {
                            errorMessage = null
                            isLoading = true
                            val onboardingPayload = if (authMode == 1) {
                                AccountOnboardingInput(
                                    displayNameOverride = fullName.trim(),
                                    requestedRole = selectedRole,
                                    organization = organization.trim(),
                                    inviteCode = inviteCode.trim()
                                )
                            } else {
                                null
                            }
                            GoogleAuthHelper.onGoogleSignInClicked(
                                context = context,
                                credentialManager = credentialManager,
                                onAuthSuccess = {
                                    isLoading = false
                                    onAuthSuccess(onboardingPayload)
                                },
                                onAuthError = { msg ->
                                    isLoading = false
                                    errorMessage = msg
                                },
                                scope = scope,
                                onAuthCancelled = { isLoading = false }
                            )
                        },
                        onOpenInstallGuide = { showInstallSheet = true },
                        onOpenLegal = { showLegalSheet = true },
                        modifier = Modifier.weight(1.05f)
                    )
                }
            } else {
                Column(
                    modifier = Modifier.widthIn(max = 560.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    VisionPillBadge()
                    BrandHeroCard()
                    AuthPortalCard(
                        authMode = authMode,
                        onAuthModeChange = {
                            authMode = it
                            errorMessage = null
                        },
                        signupRoles = signupRoles,
                        selectedRole = selectedRole,
                        onSelectRole = { selectedRole = it },
                        fullName = fullName,
                        onFullNameChange = { fullName = it },
                        organization = organization,
                        onOrganizationChange = { organization = it },
                        inviteCode = inviteCode,
                        onInviteCodeChange = { inviteCode = it },
                        isLoading = isLoading,
                        errorMessage = errorMessage,
                        onSubmitAuth = {
                            errorMessage = null
                            isLoading = true
                            val onboardingPayload = if (authMode == 1) {
                                AccountOnboardingInput(
                                    displayNameOverride = fullName.trim(),
                                    requestedRole = selectedRole,
                                    organization = organization.trim(),
                                    inviteCode = inviteCode.trim()
                                )
                            } else {
                                null
                            }
                            GoogleAuthHelper.onGoogleSignInClicked(
                                context = context,
                                credentialManager = credentialManager,
                                onAuthSuccess = {
                                    isLoading = false
                                    onAuthSuccess(onboardingPayload)
                                },
                                onAuthError = { msg ->
                                    isLoading = false
                                    errorMessage = msg
                                },
                                scope = scope,
                                onAuthCancelled = { isLoading = false }
                            )
                        },
                        onOpenInstallGuide = { showInstallSheet = true },
                        onOpenLegal = { showLegalSheet = true },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }

    if (showInstallSheet) {
        InstallPlatformSheet(
            appReleases = emptyList(),
            portalUrl = CodivaCatalog.PUBLIC_PORTAL_URL,
            onNotify = { msg -> Toast.makeText(context, msg, Toast.LENGTH_SHORT).show() },
            onDismiss = { showInstallSheet = false }
        )
    }

    if (showLegalSheet) {
        LegalDocumentsSheet(onDismiss = { showLegalSheet = false })
    }
}

@Composable
private fun VisionPillBadge() {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = CodivaCobalt.copy(alpha = 0.22f),
        border = BorderStroke(1.dp, CodivaSky.copy(alpha = 0.45f))
    ) {
        Text(
            text = "LIBERIAN-FOUNDED • PAN-AFRICAN • GLOBAL VISION",
            style = MaterialTheme.typography.labelSmall,
            color = CodivaSky,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BrandHeroCard() {
    val ecosystemDashboards = listOf(
        "Super Admin", "User", "Member", "Staff",
        "Lecturer", "Parent", "Partner", "HOD", "Finance"
    )
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Black),
        border = BorderStroke(1.dp, CodivaSky.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_codiva_logo),
                contentDescription = "CODIVA Technologies official logo",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(164.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .testTag("entry_codiva_logo"),
                contentScale = ContentScale.Fit
            )
            Text(
                text = stringResource(R.string.hero_headline),
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White
            )
            Text(
                text = stringResource(R.string.hero_subheadline),
                style = MaterialTheme.typography.bodyMedium,
                color = CodivaSlateMuted
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ecosystemDashboards.forEach { roleLabel ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color.White.copy(alpha = 0.08f)
                    ) {
                        Text(
                            text = roleLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = CodivaSlateLight,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun AuthPortalCard(
    authMode: Int,
    onAuthModeChange: (Int) -> Unit,
    signupRoles: List<AccountRole>,
    selectedRole: AccountRole,
    onSelectRole: (AccountRole) -> Unit,
    fullName: String,
    onFullNameChange: (String) -> Unit,
    organization: String,
    onOrganizationChange: (String) -> Unit,
    inviteCode: String,
    onInviteCodeChange: (String) -> Unit,
    isLoading: Boolean,
    errorMessage: String?,
    onSubmitAuth: () -> Unit,
    onOpenInstallGuide: () -> Unit,
    onOpenLegal: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("auth_mode_selector")
            ) {
                listOf("Sign In", "Create Account").forEachIndexed { index, label ->
                    SegmentedButton(
                        selected = authMode == index,
                        onClick = { onAuthModeChange(index) },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = 2),
                        modifier = Modifier.testTag("auth_mode_$index")
                    ) {
                        Text(label)
                    }
                }
            }

            if (authMode == 0) {
                Text(text = "Welcome Back!", style = MaterialTheme.typography.titleLarge)
                Text(
                    text = "Sign in to access your CODIVA Technologies role dashboard, Founder control panel, Academy courses, and client portal.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FeatureMiniBadge(Icons.Rounded.AdminPanelSettings, "Admin", Modifier.weight(1f))
                    FeatureMiniBadge(Icons.Rounded.School, "Academy", Modifier.weight(1f))
                    FeatureMiniBadge(Icons.Rounded.Public, "Ecosystem", Modifier.weight(1f))
                }
            } else {
                Text(text = "Create Your CODIVA Account", style = MaterialTheme.typography.titleLarge)
                Text(
                    text = "Choose your account type and complete registration with your Google account.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text("Select Account Role", style = MaterialTheme.typography.titleSmall)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    signupRoles.forEach { role ->
                        FilterChip(
                            selected = selectedRole == role,
                            onClick = { onSelectRole(role) },
                            label = { Text(role.label) },
                            modifier = Modifier.testTag("signup_role_${role.name.lowercase()}")
                        )
                    }
                }
                Text(
                    text = selectedRole.tagline,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
                OutlinedTextField(
                    value = fullName,
                    onValueChange = onFullNameChange,
                    label = { Text("Display name (optional)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("signup_name_input")
                )
                OutlinedTextField(
                    value = organization,
                    onValueChange = onOrganizationChange,
                    label = { Text("Organization or school (optional)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("signup_org_input")
                )
                OutlinedTextField(
                    value = inviteCode,
                    onValueChange = onInviteCodeChange,
                    label = { Text("Team / Member invite code (optional)") },
                    placeholder = { Text("e.g., CODIVA-2026") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("signup_invite_input")
                )
            }

            if (errorMessage != null) {
                ErrorBanner(message = errorMessage)
            }

            Button(
                onClick = onSubmitAuth,
                enabled = !isLoading,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CodivaCobalt),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .minimumInteractiveComponentSize()
                    .testTag("google_sign_in_button")
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = if (authMode == 1) Icons.Rounded.PersonAdd else Icons.Rounded.Login,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.size(10.dp))
                    Text(
                        text = if (authMode == 1) {
                            "Create ${selectedRole.label} Account with Google"
                        } else {
                            stringResource(R.string.sign_in_google)
                        },
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.White
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Security,
                        contentDescription = null,
                        tint = CodivaEmerald,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Secured with Google Identity & Cloud Firestore",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = "Official Brand: ${CodivaCatalog.OFFICIAL_DOMAIN} • Android • iOS • Windows • Web",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    OutlinedButton(
                        onClick = onOpenInstallGuide,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("auth_install_guide_button")
                    ) {
                        Icon(Icons.Rounded.InstallMobile, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Install App / PWA")
                    }
                    TextButton(
                        onClick = onOpenLegal,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("auth_legal_button")
                    ) {
                        Icon(Icons.Rounded.PrivacyTip, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Privacy & Terms")
                    }
                }
            }
        }
    }
}

@Composable
private fun FeatureMiniBadge(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
            Text(text = label, style = MaterialTheme.typography.labelLarge)
        }
    }
}

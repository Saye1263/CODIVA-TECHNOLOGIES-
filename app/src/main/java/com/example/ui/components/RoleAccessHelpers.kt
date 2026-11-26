package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.DashboardPermission
import com.example.data.DashboardSection
import com.example.data.UserProfile
import com.example.data.UserRoleManager

/**
 * Remembers a [UserRoleManager] derived from the signed-in [UserProfile] and email.
 */
@Composable
fun rememberUserRoleManager(
    profile: UserProfile?,
    fallbackEmail: String? = null
): UserRoleManager {
    return remember(
        profile?.accountRole,
        profile?.email,
        profile?.membershipStatus,
        fallbackEmail
    ) {
        UserRoleManager.fromProfile(profile, fallbackEmail)
    }
}

/**
 * Conditionally renders UI elements only if the [roleManager] grants [requiredPermission].
 */
@Composable
fun RoleVisibilityGate(
    roleManager: UserRoleManager,
    requiredPermission: DashboardPermission,
    fallback: @Composable () -> Unit = {},
    content: @Composable () -> Unit
) {
    if (roleManager.hasPermission(requiredPermission)) {
        content()
    } else {
        fallback()
    }
}

/**
 * Conditionally renders a dashboard section only if [roleManager] allows [section].
 */
@Composable
fun RoleSectionGate(
    roleManager: UserRoleManager,
    section: DashboardSection,
    fallback: @Composable () -> Unit = {},
    content: @Composable () -> Unit
) {
    if (roleManager.canAccessSection(section)) {
        content()
    } else {
        fallback()
    }
}

/**
 * Enforces navigation access for [route] using [UserRoleManager].
 * Displays an accessible restricted notice if the active role or account status is unauthorized.
 */
@Composable
fun RoleNavigationGuard(
    roleManager: UserRoleManager,
    route: String,
    onReturnToAllowed: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    if (roleManager.canNavigateToRoute(route)) {
        content()
    } else {
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(24.dp)
                .testTag("role_navigation_guard_$route"),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier.widthIn(max = 520.dp),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Lock,
                        contentDescription = "Access restricted",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(40.dp)
                    )
                    StatusBadge(
                        text = "${roleManager.activeRole.label.uppercase()} • RESTRICTED",
                        tone = BadgeTone.WARNING
                    )
                    Text(
                        text = "Navigation Access Restricted",
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = roleManager.navigationDenialReason(route),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Button(
                        onClick = onReturnToAllowed,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("role_guard_return_button")
                    ) {
                        Text("Return to Authorized Home")
                    }
                }
            }
        }
    }
}

package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Work
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.auth.GoogleAuthHelper
import com.example.data.AccountOnboardingInput
import com.example.data.CodivaAppRoutes
import com.example.data.CodivaCatalog
import com.example.data.CodivaRepository
import com.example.data.UserProfile
import com.example.ui.CodivaViewModel
import com.example.ui.UiState
import com.example.ui.components.CodivaLaunchSplash
import com.example.ui.components.ProjectInquirySheet
import com.example.ui.components.RoleNavigationGuard
import com.example.ui.components.rememberUserRoleManager
import kotlinx.coroutines.delay
import com.example.ui.screens.AcademyScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.CareersAboutScreen
import com.example.ui.screens.EcosystemScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.PortalScreen
import com.example.ui.screens.SolutionsScreen
import com.example.ui.theme.CodivaSky
import com.example.ui.theme.CodivaTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

object CodivaRoutes {
    const val HOME = CodivaAppRoutes.HOME
    const val SOLUTIONS = CodivaAppRoutes.SOLUTIONS
    const val ECOSYSTEM = CodivaAppRoutes.ECOSYSTEM
    const val ACADEMY = CodivaAppRoutes.ACADEMY
    const val PORTAL = CodivaAppRoutes.PORTAL
    const val CAREERS = CodivaAppRoutes.CAREERS
}

private data class NavDestination(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

private val codivaDestinations = listOf(
    NavDestination(CodivaRoutes.HOME, "Home", Icons.Rounded.Home, Icons.Outlined.Home),
    NavDestination(CodivaRoutes.SOLUTIONS, "Solutions", Icons.Rounded.Build, Icons.Outlined.Build),
    NavDestination(CodivaRoutes.ECOSYSTEM, "Ecosystem", Icons.Rounded.Dashboard, Icons.Outlined.Dashboard),
    NavDestination(CodivaRoutes.ACADEMY, "Academy", Icons.Rounded.School, Icons.Outlined.School),
    NavDestination(CodivaRoutes.PORTAL, "Portal", Icons.Rounded.AccountCircle, Icons.Outlined.AccountCircle)
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CodivaTheme {
                AppContent()
            }
        }
    }
}

@Composable
fun AppContent() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }
    var currentUser by remember { mutableStateOf(Firebase.auth.currentUser) }
    var pendingOnboarding by remember { mutableStateOf<AccountOnboardingInput?>(null) }
    var showSplash by remember { mutableStateOf(true) }

    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            currentUser = auth.currentUser
        }
        Firebase.auth.addAuthStateListener(listener)
        onDispose { Firebase.auth.removeAuthStateListener(listener) }
    }

    LaunchedEffect(Unit) {
        delay(850L)
        showSplash = false
    }

    LaunchedEffect(Unit) {
        if (Firebase.auth.currentUser == null) {
            GoogleAuthHelper.attemptAutoSignIn(
                context = context,
                credentialManager = credentialManager,
                onAuthSuccess = { currentUser = Firebase.auth.currentUser },
                onUnauthenticated = {},
                scope = scope
            )
        }
    }

    if (showSplash) {
        CodivaLaunchSplash(onSkip = { showSplash = false })
        return
    }

    val user = currentUser
    if (user == null) {
        AuthScreen(
            onAuthSuccess = { onboarding ->
                pendingOnboarding = onboarding
                currentUser = Firebase.auth.currentUser
            }
        )
    } else {
        key(user.uid) {
            CodivaMainShell(
                currentUser = user,
                initialOnboarding = pendingOnboarding,
                onSignOut = {
                    GoogleAuthHelper.signOut(
                        credentialManager = credentialManager,
                        scope = scope,
                        onSignOutComplete = {
                            pendingOnboarding = null
                            currentUser = null
                        }
                    )
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CodivaMainShell(
    currentUser: FirebaseUser,
    initialOnboarding: AccountOnboardingInput?,
    onSignOut: () -> Unit
) {
    val currentUserId = currentUser.uid
    val fallbackName = initialOnboarding?.displayNameOverride?.takeIf { it.isNotBlank() }
        ?: currentUser.displayName?.takeIf { it.isNotBlank() }
        ?: "CODIVA Member"
    val fallbackEmail = currentUser.email?.takeIf { it.isNotBlank() } ?: "member@codiva.com"

    val viewModel: CodivaViewModel = viewModel(
        key = currentUserId,
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) {
                    "APPLICATION_KEY missing from CreationExtras"
                }
                val databaseId = app.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(databaseId)
                CodivaViewModel(
                    repository = CodivaRepository(db),
                    currentUserId = currentUserId,
                    initialDisplayName = fallbackName,
                    initialEmail = fallbackEmail,
                    initialOnboarding = initialOnboarding
                )
            }
        }
    )

    val profileState by viewModel.profileState.collectAsStateWithLifecycle()
    val inquiriesState by viewModel.inquiriesState.collectAsStateWithLifecycle()
    val enrollmentsState by viewModel.enrollmentsState.collectAsStateWithLifecycle()
    val applicationsState by viewModel.applicationsState.collectAsStateWithLifecycle()
    val workspaceRecordsState by viewModel.workspaceRecordsState.collectAsStateWithLifecycle()
    val auditLogsState by viewModel.auditLogsState.collectAsStateWithLifecycle()
    val settingsState by viewModel.settingsState.collectAsStateWithLifecycle()
    val publicPortalState by viewModel.publicPortalState.collectAsStateWithLifecycle()
    val registeredUsersState by viewModel.registeredUsersState.collectAsStateWithLifecycle()
    val publicationsState by viewModel.publicationsState.collectAsStateWithLifecycle()
    val appReleasesState by viewModel.appReleasesState.collectAsStateWithLifecycle()
    val isSubmitting by viewModel.isSubmitting.collectAsStateWithLifecycle()
    val feedbackMessage by viewModel.feedbackMessage.collectAsStateWithLifecycle()

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: CodivaRoutes.HOME
    val snackbarHostState = remember { SnackbarHostState() }

    val activeProfile: UserProfile? = (profileState as? UiState.Success)?.data
    val roleManager = rememberUserRoleManager(
        profile = activeProfile,
        fallbackEmail = fallbackEmail
    )
    val accessibleDestinations = remember(roleManager.activeRole, roleManager.membershipStatus) {
        codivaDestinations.filter { roleManager.canNavigateToRoute(it.route) }
    }

    var inquiryInitialService by remember { mutableStateOf<String?>(null) }
    var careersInitialTab by remember { mutableIntStateOf(0) }

    LaunchedEffect(feedbackMessage) {
        val msg = feedbackMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(msg)
        viewModel.clearFeedback()
    }

    fun navigateToTab(route: String) {
        if (!roleManager.canNavigateToRoute(route)) {
            viewModel.showFeedback(roleManager.navigationDenialReason(route))
            return
        }
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useRail = maxWidth >= 600.dp

        Scaffold(
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.testTag("app_header_brand")
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color.Black,
                                border = BorderStroke(1.dp, CodivaSky.copy(alpha = 0.45f)),
                                modifier = Modifier
                                    .size(40.dp)
                                    .testTag("app_header_logo")
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
                                    text = "CODIVA Technologies",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = CodivaCatalog.SLOGAN,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    },
                    actions = {
                        if (roleManager.canNavigateToRoute(CodivaRoutes.CAREERS)) {
                            IconButton(
                                onClick = {
                                    careersInitialTab = 0
                                    navController.navigate(CodivaRoutes.CAREERS) {
                                        launchSingleTop = true
                                    }
                                },
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("topbar_careers_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Work,
                                    contentDescription = "Careers and About CODIVA"
                                )
                            }
                        }
                    }
                )
            },
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            floatingActionButton = {
                if ((currentRoute == CodivaRoutes.HOME || currentRoute == CodivaRoutes.SOLUTIONS) &&
                    roleManager.canCreateProjectInquiry()
                ) {
                    FloatingActionButton(
                        onClick = { inquiryInitialService = CodivaCatalog.services.first().title },
                        modifier = Modifier.testTag("fab_new_inquiry")
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = "Request project quote")
                    }
                }
            },
            bottomBar = {
                if (!useRail) {
                    NavigationBar {
                        accessibleDestinations.forEach { dest ->
                            val selected = currentRoute == dest.route
                            NavigationBarItem(
                                selected = selected,
                                onClick = { navigateToTab(dest.route) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                        contentDescription = dest.label
                                    )
                                },
                                label = { Text(dest.label) },
                                modifier = Modifier.testTag("nav_${dest.route}")
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (useRail) {
                    NavigationRail {
                        accessibleDestinations.forEach { dest ->
                            val selected = currentRoute == dest.route
                            NavigationRailItem(
                                selected = selected,
                                onClick = { navigateToTab(dest.route) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                        contentDescription = dest.label
                                    )
                                },
                                label = { Text(dest.label) },
                                modifier = Modifier.testTag("rail_${dest.route}")
                            )
                        }
                    }
                }

                NavHost(
                    navController = navController,
                    startDestination = CodivaRoutes.HOME,
                    modifier = Modifier.weight(1f)
                ) {
                    composable(CodivaRoutes.HOME) {
                        HomeScreen(
                            displayName = fallbackName,
                            profileState = profileState,
                            workspaceRecordsState = workspaceRecordsState,
                            publicationsState = publicationsState,
                            settingsState = settingsState,
                            roleManager = roleManager,
                            onExploreServices = { navigateToTab(CodivaRoutes.SOLUTIONS) },
                            onRequestQuote = { service -> inquiryInitialService = service },
                            onOpenEcosystem = { navigateToTab(CodivaRoutes.ECOSYSTEM) },
                            onViewTeam = {
                                careersInitialTab = 1
                                if (roleManager.canNavigateToRoute(CodivaRoutes.CAREERS)) {
                                    navController.navigate(CodivaRoutes.CAREERS) {
                                        launchSingleTop = true
                                    }
                                }
                            },
                            onOpenAcademy = { navigateToTab(CodivaRoutes.ACADEMY) },
                            onOpenDownloadsPortal = { navigateToTab(CodivaRoutes.PORTAL) },
                            onNotify = { msg -> viewModel.showFeedback(msg) }
                        )
                    }
                    composable(CodivaRoutes.SOLUTIONS) {
                        RoleNavigationGuard(
                            roleManager = roleManager,
                            route = CodivaRoutes.SOLUTIONS,
                            onReturnToAllowed = { navigateToTab(roleManager.defaultRoute()) }
                        ) {
                            SolutionsScreen(
                                onRequestQuote = { service -> inquiryInitialService = service }
                            )
                        }
                    }
                    composable(CodivaRoutes.ECOSYSTEM) {
                        RoleNavigationGuard(
                            roleManager = roleManager,
                            route = CodivaRoutes.ECOSYSTEM,
                            onReturnToAllowed = { navigateToTab(roleManager.defaultRoute()) }
                        ) {
                            EcosystemScreen(
                                fallbackName = fallbackName,
                                fallbackEmail = fallbackEmail,
                                profileState = profileState,
                                inquiriesState = inquiriesState,
                                enrollmentsState = enrollmentsState,
                                applicationsState = applicationsState,
                                workspaceRecordsState = workspaceRecordsState,
                                auditLogsState = auditLogsState,
                                settingsState = settingsState,
                                publicPortalState = publicPortalState,
                                registeredUsersState = registeredUsersState,
                                publicationsState = publicationsState,
                                appReleasesState = appReleasesState,
                                isSubmitting = isSubmitting,
                                roleManager = roleManager,
                                onSwitchRole = { role -> viewModel.switchActiveRole(role) },
                                onAddWorkspaceRecord = { type, title, sub, detail, cat, status, cents, done ->
                                    viewModel.addWorkspaceRecord(type, title, sub, detail, cat, status, cents, done)
                                },
                                onUpdateRecordStatus = { rec, status ->
                                    viewModel.updateWorkspaceRecordStatus(rec, status)
                                },
                                onDeleteRecord = { rec -> viewModel.deleteWorkspaceRecord(rec) },
                                onUpdateSettings = { s -> viewModel.updateSystemSettings(s) },
                                onUpdatePublicPortalConfig = { cfg, done ->
                                    viewModel.updatePublicPortalConfig(cfg, done)
                                },
                                onAdminUpdateRegisteredUser = { targetUser, role, status ->
                                    viewModel.adminUpdateRegisteredUser(targetUser, role, status)
                                },
                                onSavePublication = { existingId, title, summary, content, category, targetRole, docUrl, docLabel, expiresAt, status, done ->
                                    viewModel.savePublication(existingId, title, summary, content, category, targetRole, docUrl, docLabel, expiresAt, status, done)
                                },
                                onUpdatePublicationStatus = { pub, status ->
                                    viewModel.updatePublicationStatus(pub, status)
                                },
                                onDeletePublication = { pub -> viewModel.deletePublication(pub) },
                                onSaveAppRelease = { existingId, platform, versionName, buildNumber, notes, url, status, done ->
                                    viewModel.saveAppRelease(existingId, platform, versionName, buildNumber, notes, url, status, done)
                                },
                                onUpdateAppReleaseStatus = { rel, status ->
                                    viewModel.updateAppReleaseStatus(rel, status)
                                },
                                onDeleteAppRelease = { rel -> viewModel.deleteAppRelease(rel) },
                                onNotify = { msg -> viewModel.showFeedback(msg) },
                                onOpenAcademy = { navigateToTab(CodivaRoutes.ACADEMY) },
                                onRequestQuote = { service -> inquiryInitialService = service },
                                onCancelInquiry = { id -> viewModel.cancelInquiry(id) }
                            )
                        }
                    }
                    composable(CodivaRoutes.ACADEMY) {
                        RoleNavigationGuard(
                            roleManager = roleManager,
                            route = CodivaRoutes.ACADEMY,
                            onReturnToAllowed = { navigateToTab(roleManager.defaultRoute()) }
                        ) {
                            AcademyScreen(
                                enrollmentsState = enrollmentsState,
                                isSubmitting = isSubmitting,
                                onEnroll = { program -> viewModel.enrollInCourse(program) },
                                onAdvanceProgress = { enrollment -> viewModel.advanceEnrollmentProgress(enrollment) }
                            )
                        }
                    }
                    composable(CodivaRoutes.CAREERS) {
                        RoleNavigationGuard(
                            roleManager = roleManager,
                            route = CodivaRoutes.CAREERS,
                            onReturnToAllowed = { navigateToTab(roleManager.defaultRoute()) }
                        ) {
                            CareersAboutScreen(
                                defaultApplicantName = fallbackName,
                                initialTab = careersInitialTab,
                                applicationsState = applicationsState,
                                isSubmitting = isSubmitting,
                                onApplyRole = { role, name, note, onDone ->
                                    viewModel.submitApplication(role, name, note, onDone)
                                }
                            )
                        }
                    }
                    composable(CodivaRoutes.PORTAL) {
                        PortalScreen(
                            fallbackName = fallbackName,
                            fallbackEmail = fallbackEmail,
                            profileState = profileState,
                            publicPortalState = publicPortalState,
                            inquiriesState = inquiriesState,
                            enrollmentsState = enrollmentsState,
                            applicationsState = applicationsState,
                            publicationsState = publicationsState,
                            appReleasesState = appReleasesState,
                            workspaceRecordsState = workspaceRecordsState,
                            settingsState = settingsState,
                            isSubmitting = isSubmitting,
                            roleManager = roleManager,
                            onUpdateProfile = { name, role, org, phone, onDone ->
                                viewModel.updateProfile(name, role, org, phone, onDone)
                            },
                            onAddWorkspaceRecord = { type, title, sub, detail, cat, status, cents, done ->
                                viewModel.addWorkspaceRecord(type, title, sub, detail, cat, status, cents, done)
                            },
                            onNotify = { msg -> viewModel.showFeedback(msg) },
                            onNewInquiry = { inquiryInitialService = CodivaCatalog.services.first().title },
                            onCancelInquiry = { id -> viewModel.cancelInquiry(id) },
                            onDeleteInquiry = { id -> viewModel.deleteInquiry(id) },
                            onWithdrawApplication = { id -> viewModel.withdrawApplication(id) },
                            onOpenCareersAbout = {
                                careersInitialTab = 0
                                navigateToTab(CodivaRoutes.CAREERS)
                            },
                            onSignOut = onSignOut
                        )
                    }
                }
            }
        }
    }

    inquiryInitialService?.let { initialService ->
        ProjectInquirySheet(
            initialService = initialService,
            isSubmitting = isSubmitting,
            onDismiss = { inquiryInitialService = null },
            onSubmit = { category, title, details, budget ->
                viewModel.submitInquiry(category, title, details, budget) {
                    inquiryInitialService = null
                }
            }
        )
    }
}

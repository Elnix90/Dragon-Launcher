package org.elnix.dragonlauncher.ui

import android.R.attr.versionCode
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.data.helpers.DrawerActions
import org.elnix.dragonlauncher.data.stores.DrawerSettingsStore
import org.elnix.dragonlauncher.data.stores.PrivateSettingsStore
import org.elnix.dragonlauncher.ui.drawer.AppDrawerScreen
import org.elnix.dragonlauncher.ui.helpers.SetDefaultLauncherBanner
import org.elnix.dragonlauncher.ui.settings.appearance.AppearanceTab
import org.elnix.dragonlauncher.ui.settings.appearance.ColorSelectorTab
import org.elnix.dragonlauncher.ui.settings.appearance.DrawerTab
import org.elnix.dragonlauncher.ui.settings.backup.BackupTab
import org.elnix.dragonlauncher.ui.settings.debug.DebugTab
import org.elnix.dragonlauncher.ui.settings.language.LanguageTab
import org.elnix.dragonlauncher.ui.settings.workspace.WorkspaceDetailScreen
import org.elnix.dragonlauncher.ui.settings.workspace.WorkspaceListScreen
import org.elnix.dragonlauncher.ui.welcome.WelcomeScreen
import org.elnix.dragonlauncher.ui.whatsnew.ChangelogsScreen
import org.elnix.dragonlauncher.ui.whatsnew.WhatsNewBottomSheet
import org.elnix.dragonlauncher.utils.getVersionCode
import org.elnix.dragonlauncher.utils.isDefaultLauncher
import org.elnix.dragonlauncher.utils.loadChangelogs
import org.elnix.dragonlauncher.utils.models.AppDrawerViewModel
import org.elnix.dragonlauncher.utils.models.BackupViewModel
import org.elnix.dragonlauncher.utils.models.WorkspaceViewModel

// -------------------- SETTINGS --------------------

object SETTINGS {
    const val ROOT = "settings"
    const val ADVANCED_ROOT = "settings/advanced"
    const val APPEARANCE = "settings/advanced/appearance"
    const val COLORS = "settings/advanced/appearance/colors"
    const val DRAWER = "settings/advanced/drawer"
    const val WORKSPACE = "settings/advanced/workspace"
    const val WORKSPACE_DETAIL = "settings/advanced/workspace/{id}"
    const val BACKUP = "settings/advanced/backup"
    const val DEBUG = "/advanced/debug"
    const val LANGUAGE = "settings/advanced/language"
    const val CHANGELOGS = "settings/advanced/changelogs"
}

object ROUTES {
    const val MAIN = "main"
    const val DRAWER = "drawer"
    const val WELCOME = "welcome"
}

@Suppress("AssignedValueIsNeverRead")
@Composable
fun MainAppUi(
    backupViewModel: BackupViewModel,
    appViewModel: AppDrawerViewModel,
    workspaceViewModel: WorkspaceViewModel,
    navController: NavHostController
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    // Changelogs system
    val lastSeenVersionCode by PrivateSettingsStore.getLastSeenVersionCode(ctx)
        .collectAsState(initial = Int.MAX_VALUE)

    val currentVersionCode = getVersionCode(ctx)
    var showWhatsNewBottomSheet by remember { mutableStateOf(false) }

    val updates by produceState(initialValue = emptyList()) {
        value = loadChangelogs(ctx, versionCode)
    }

    val showAppIconsInDrawer by DrawerSettingsStore.getShowAppIconsInDrawer(ctx)
        .collectAsState(initial = true)

    val showAppLabelsInDrawer by DrawerSettingsStore.getShowAppLabelsInDrawer(ctx)
        .collectAsState(initial = true)

    val autoShowKeyboardOnDrawer by DrawerSettingsStore.getAutoShowKeyboardOnDrawer(ctx)
        .collectAsState(initial = false)

    val gridSize by DrawerSettingsStore.getGridSize(ctx)
        .collectAsState(initial = 1)

//    val searchBarBottom by DrawerSettingsStore.getSearchBarBottom(ctx)
//        .collectAsState(initial = true)
    val searchBarBottom = false



    val leftDrawerAction by DrawerSettingsStore.getLeftDrawerAction(ctx)
        .collectAsState(initial = DrawerActions.TOGGLE_KB)

    val rightDrawerAction by DrawerSettingsStore.getRightDrawerAction(ctx)
        .collectAsState(initial = DrawerActions.CLOSE)

    val leftDrawerWidth by DrawerSettingsStore.getLeftDrawerWidth(ctx)
        .collectAsState(initial = 0.1f)
    val rightDrawerWidth  by DrawerSettingsStore.getRightDrawerWidth(ctx)
        .collectAsState(initial = 0.1f)


    val showSetDefaultLauncherBanner by PrivateSettingsStore.getShowSetDefaultLauncherBanner(ctx)
        .collectAsState(initial = false)


    val lifecycleOwner = LocalLifecycleOwner.current
    var isDefaultLauncher by remember { mutableStateOf(ctx.isDefaultLauncher) }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    LaunchedEffect(Unit, lastSeenVersionCode, currentRoute) {
        if (lastSeenVersionCode < currentVersionCode && currentRoute != ROUTES.WELCOME) {
            showWhatsNewBottomSheet = true
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            // The activity resumes when the user returns from the Home settings screen
            if (event == Lifecycle.Event.ON_RESUME) {
                // IMPORTANT: Re-check the status and update the state
                isDefaultLauncher = ctx.isDefaultLauncher
            }
        }

        // Add the observer to the lifecycle
        lifecycleOwner.lifecycle.addObserver(observer)

        // When the composable leaves the screen, remove the observer
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }


    fun goMainScreen() {
        navController.navigate(ROUTES.MAIN) {
            popUpTo(0) { inclusive = true }
        }
    }

    fun goSettingsRoot() =  navController.navigate(SETTINGS.ROOT)
    fun goAdvSettingsRoot() =  navController.navigate(SETTINGS.ADVANCED_ROOT)
    fun goDrawer() = navController.navigate(ROUTES.DRAWER)
    fun goWelcome() = navController.navigate(ROUTES.WELCOME)





    val showBanner = showSetDefaultLauncherBanner &&
            !isDefaultLauncher &&
            currentRoute != ROUTES.WELCOME


    BackHandler { }

    Scaffold(
        topBar = {
            if (showBanner) { SetDefaultLauncherBanner() }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = ROUTES.MAIN,
            modifier = Modifier.padding(paddingValues)
        ) {
            // Main App (LauncherScreen + Drawer)
            composable(ROUTES.MAIN) {
                MainScreen(
                    appsViewModel = appViewModel,
                    onAppDrawer = { goDrawer() },
                    onGoWelcome = { goWelcome() },
                    onLongPress3Sec = { goSettingsRoot() }
                )
            }

            composable(ROUTES.DRAWER) {
                AppDrawerScreen(
                    appsViewModel = appViewModel,
                    workspaceViewModel = workspaceViewModel,
                    showIcons = showAppIconsInDrawer,
                    showLabels = showAppLabelsInDrawer,
                    autoShowKeyboard = autoShowKeyboardOnDrawer,
                    gridSize = gridSize,
                    searchBarBottom = searchBarBottom,
                    leftAction = leftDrawerAction,
                    leftWidth = leftDrawerWidth,
                    rightAction = rightDrawerAction,
                    rightWidth = rightDrawerWidth,
                ) { goMainScreen() }
            }


            // Settings + Welcome

            composable(ROUTES.WELCOME) {
                WelcomeScreen(
                    backupVm =  backupViewModel,
                    onEnterSettings = { goSettingsRoot() },
                    onEnterApp = { goMainScreen() }
                )
            }

            composable(SETTINGS.ROOT) {
                SettingsScreen(
                    appsViewModel = appViewModel,
                    workspaceViewModel = workspaceViewModel,
                    onAdvSettings = { goAdvSettingsRoot() },
                    onBack = { goMainScreen() }
                )
            }
            composable(SETTINGS.ADVANCED_ROOT) { AdvancedSettingsScreen(appViewModel, navController, onReset = { goMainScreen() } ) { goSettingsRoot() } }

            composable(SETTINGS.APPEARANCE) { AppearanceTab(navController) { goAdvSettingsRoot() } }
            composable(SETTINGS.DRAWER)     { DrawerTab(appViewModel) { goAdvSettingsRoot() } }
            composable(SETTINGS.COLORS)     { ColorSelectorTab { goAdvSettingsRoot() } }
            composable(SETTINGS.DEBUG)      { DebugTab(navController, onShowWelcome = { goWelcome() } ) { goAdvSettingsRoot() } }
            composable(SETTINGS.LANGUAGE)   { LanguageTab { goAdvSettingsRoot() } }
            composable(SETTINGS.BACKUP)     { BackupTab(backupViewModel) { goAdvSettingsRoot() } }
            composable(SETTINGS.CHANGELOGS) { ChangelogsScreen { goAdvSettingsRoot() } }

            composable(SETTINGS.WORKSPACE) {
                WorkspaceListScreen(
                    workspaceViewModel = workspaceViewModel,
                    onOpenWorkspace = { id ->
                        navController.navigate(
                            SETTINGS.WORKSPACE_DETAIL.replace("{id}", id)
                        )
                    },
                    onBack = { goAdvSettingsRoot() }
                )
            }

            composable(
                SETTINGS.WORKSPACE_DETAIL,
                arguments = listOf(navArgument("id") { type = NavType.StringType })
            ) { backStack ->
                WorkspaceDetailScreen(
                    workspaceId = backStack.arguments!!.getString("id")!!,
                    appsViewModel = appViewModel,
                    workspaceViewModel = workspaceViewModel,
                    showIcons = showAppIconsInDrawer,
                    showLabels = showAppLabelsInDrawer,
                    gridSize = gridSize,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }

    if (showWhatsNewBottomSheet) {
        WhatsNewBottomSheet(
            updates = updates
        ) {
            showWhatsNewBottomSheet = false
            scope.launch { PrivateSettingsStore.setLastSeenVersionCode(ctx, currentVersionCode) }
        }
    }
}

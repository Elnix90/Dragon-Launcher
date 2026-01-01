package org.elnix.dragonlauncher.ui

import android.R.attr.versionCode
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.net.toUri
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
import org.elnix.dragonlauncher.R
import org.elnix.dragonlauncher.data.helpers.DrawerActions
import org.elnix.dragonlauncher.data.stores.BackupSettingsStore
import org.elnix.dragonlauncher.data.stores.DrawerSettingsStore
import org.elnix.dragonlauncher.data.stores.PrivateSettingsStore
import org.elnix.dragonlauncher.data.stores.WallpaperSettingsStore
import org.elnix.dragonlauncher.ui.components.dialogs.UserValidation
import org.elnix.dragonlauncher.ui.components.dialogs.WidgetPickerDialog
import org.elnix.dragonlauncher.ui.drawer.AppDrawerScreen
import org.elnix.dragonlauncher.ui.helpers.ReselectAutoBackupBanner
import org.elnix.dragonlauncher.ui.helpers.SetDefaultLauncherBanner
import org.elnix.dragonlauncher.ui.settings.backup.BackupTab
import org.elnix.dragonlauncher.ui.settings.customization.AppearanceTab
import org.elnix.dragonlauncher.ui.settings.customization.BehaviorTab
import org.elnix.dragonlauncher.ui.settings.customization.ColorSelectorTab
import org.elnix.dragonlauncher.ui.settings.customization.DrawerTab
import org.elnix.dragonlauncher.ui.settings.customization.IconPackTab
import org.elnix.dragonlauncher.ui.settings.customization.StatusBarTab
import org.elnix.dragonlauncher.ui.settings.customization.ThemesTab
import org.elnix.dragonlauncher.ui.settings.customization.WallpaperTab
import org.elnix.dragonlauncher.ui.settings.customization.WidgetsTab
import org.elnix.dragonlauncher.ui.settings.debug.DebugTab
import org.elnix.dragonlauncher.ui.settings.language.LanguageTab
import org.elnix.dragonlauncher.ui.settings.workspace.WorkspaceDetailScreen
import org.elnix.dragonlauncher.ui.settings.workspace.WorkspaceListScreen
import org.elnix.dragonlauncher.ui.welcome.WelcomeScreen
import org.elnix.dragonlauncher.ui.whatsnew.ChangelogsScreen
import org.elnix.dragonlauncher.ui.whatsnew.WhatsNewBottomSheet
import org.elnix.dragonlauncher.utils.getVersionCode
import org.elnix.dragonlauncher.utils.hasUriReadWritePermission
import org.elnix.dragonlauncher.utils.isDefaultLauncher
import org.elnix.dragonlauncher.utils.loadChangelogs
import org.elnix.dragonlauncher.utils.models.AppsViewModel
import org.elnix.dragonlauncher.utils.models.BackupViewModel
import org.elnix.dragonlauncher.utils.models.WidgetsViewModel
import org.elnix.dragonlauncher.utils.models.WorkspaceViewModel

// -------------------- SETTINGS --------------------

object SETTINGS {
    const val ROOT = "settings"
    const val ADVANCED_ROOT = "settings/advanced"
    const val APPEARANCE = "settings/advanced/appearance"
    const val WALLPAPER = "settings/advanced/appearance/wallpaper"
    const val ICON_PACK = "settings/advanced/appearance/icon_pack"
    const val STATUS_BAR = "settings/advanced/appearance/status_bar"
    const val THEME = "settings/advanced/appearance/theme"
    const val WIDGETS = "settings/advanced/appearance/widgets"
    const val BEHAVIOR = "settings/advanced/behavior"
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
    appsViewModel: AppsViewModel,
    workspaceViewModel: WorkspaceViewModel,
    widgetsViewModel: WidgetsViewModel,
    navController: NavHostController,
    onLaunchSystemWidgetPicker: () -> Unit
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    val result by backupViewModel.result.collectAsState()

    // Changelogs system
    val lastSeenVersionCode by PrivateSettingsStore.getLastSeenVersionCode(ctx)
        .collectAsState(initial = Int.MAX_VALUE)

    val currentVersionCode = getVersionCode(ctx)
    var showWhatsNewBottomSheet by remember { mutableStateOf(false) }

    var showWidgetPicker by remember { mutableStateOf(false) }

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


    val useMainWallpaper by WallpaperSettingsStore.getUseOnMain(ctx).collectAsState(initial = false)
    val mainWallpaper by WallpaperSettingsStore.loadMainBlurredFlow(ctx).collectAsState(initial = null)
    val useDrawerWallpaper by WallpaperSettingsStore.getUseOnDrawer(ctx).collectAsState(initial = false)
    val drawerWallpaper by WallpaperSettingsStore.loadDrawerBlurredFlow(ctx).collectAsState(initial = null)



    val lifecycleOwner = LocalLifecycleOwner.current
    var isDefaultLauncher by remember { mutableStateOf(ctx.isDefaultLauncher) }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route


    val autoBackupEnabled by BackupSettingsStore.getAutoBackupEnabled(ctx).collectAsState(initial = false)
    val autoBackupUriString by BackupSettingsStore.getAutoBackupUri(ctx).collectAsState(initial = null)
    val autoBackupUri = autoBackupUriString?.toUri()


    LaunchedEffect(Unit, lastSeenVersionCode, currentRoute) {
        showWhatsNewBottomSheet = lastSeenVersionCode < currentVersionCode && currentRoute != ROUTES.WELCOME
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
    fun goAppearance() = navController.navigate(SETTINGS.APPEARANCE)



    fun launchWidgetsPicker(system: Boolean = true) {
        if (system) onLaunchSystemWidgetPicker()
        else showWidgetPicker = true
    }


    val showSetAsDefaultBanner = showSetDefaultLauncherBanner &&
            !isDefaultLauncher &&
            currentRoute != ROUTES.WELCOME


    var hasAutoBackupPermission by remember {
        mutableStateOf<Boolean?>(null)
    }

    LaunchedEffect(autoBackupUri) {
        hasAutoBackupPermission = if (autoBackupUri == null) {
            null
        } else {
            ctx.hasUriReadWritePermission(autoBackupUri)
        }
    }


    val showReselectAutoBackupFile =
        autoBackupEnabled &&
        hasAutoBackupPermission == false &&
        autoBackupUri != null &&
        currentRoute != ROUTES.WELCOME



    val autoBackupLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            ctx.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )

            hasAutoBackupPermission = true

            scope.launch {
                BackupSettingsStore.setAutoBackupUri(ctx, uri)
                BackupSettingsStore.setAutoBackupEnabled(ctx, true)
            }
        }
    }


    Scaffold(
        topBar = {
            Column {
                if (showSetAsDefaultBanner) {
                    SetDefaultLauncherBanner()
                }
                if (showReselectAutoBackupFile) {
                    ReselectAutoBackupBanner {
                        autoBackupLauncher.launch("dragonlauncher-auto-backup.json")
                    }
                }
            }
        },
        contentWindowInsets = WindowInsets(0)
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = ROUTES.MAIN,
            modifier = Modifier.padding(paddingValues)
        ) {
            // Main App (LauncherScreen)
            composable(ROUTES.MAIN) {
                MainScreen(
                    appsViewModel = appsViewModel,
                    wallpaper = mainWallpaper,
                    useWallpaper = useMainWallpaper,
                    onAppDrawer = { goDrawer() },
                    onGoWelcome = { goWelcome() },
                    onLongPress3Sec = { goSettingsRoot() },
                    widgetsViewModel = widgetsViewModel
                )
            }

            composable(ROUTES.DRAWER) {
                AppDrawerScreen(
                    appsViewModel = appsViewModel,
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
                    wallpaper = drawerWallpaper,
                    useWallpaper = useDrawerWallpaper,
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
                    appsViewModel = appsViewModel,
                    workspaceViewModel = workspaceViewModel,
                    onAdvSettings = { goAdvSettingsRoot() },
                    onBack = { goMainScreen() }
                )
            }
            composable(SETTINGS.ADVANCED_ROOT) { AdvancedSettingsScreen(appsViewModel, navController, onReset = { goMainScreen() } ) { goSettingsRoot() } }

            composable(SETTINGS.APPEARANCE) { AppearanceTab(navController) { goAdvSettingsRoot() } }
            composable(SETTINGS.WALLPAPER)  { WallpaperTab { goAppearance() } }
            composable(SETTINGS.ICON_PACK)  { IconPackTab(appsViewModel) { goAppearance() } }
            composable(SETTINGS.STATUS_BAR) { StatusBarTab { goAppearance() } }
            composable(SETTINGS.THEME)      { ThemesTab { goAppearance() } }
            composable(SETTINGS.WIDGETS)    { WidgetsTab(widgetsViewModel, { goAppearance() } ) { launchWidgetsPicker() } }
            composable(SETTINGS.BEHAVIOR)   { BehaviorTab(appsViewModel, workspaceViewModel) { goAdvSettingsRoot() } }
            composable(SETTINGS.DRAWER)     { DrawerTab(appsViewModel) { goAdvSettingsRoot() } }
            composable(SETTINGS.COLORS)     { ColorSelectorTab { goAppearance() } }
            composable(SETTINGS.DEBUG)      { DebugTab(navController, appsViewModel, onShowWelcome = { goWelcome() } ) { goAdvSettingsRoot() } }
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
                    appsViewModel = appsViewModel,
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

    if (showWidgetPicker) {
        WidgetPickerDialog(widgetsViewModel) { showWidgetPicker = false }
    }

    // ------------------------------------------------------------
    // RESULT DIALOG ( IMPORT / EXPORT )
    // ------------------------------------------------------------
    result?.let { res ->
        val isError = res.error
        val isExport = res.export
        val errorMessage = res.message

        UserValidation(
            title = when {
                isError && isExport -> stringResource(R.string.export_failed)
                isError && !isExport -> stringResource(R.string.import_failed)
                !isError && isExport -> stringResource(R.string.export_successful)
                else -> stringResource(R.string.import_successful)
            },
            message = when {
                isError -> errorMessage.ifBlank { stringResource(R.string.unknown_error) }
                isExport -> stringResource(R.string.export_successful)
                else -> stringResource(R.string.import_successful)
            },
            titleIcon = if (isError) Icons.Default.Warning else Icons.Default.Check,
            titleColor = if (isError) MaterialTheme.colorScheme.error else Color.Green,
            cancelText = null,
            copy = isError,
            onCancel = {},
            onAgree = { backupViewModel.setResult(null) }
        )
    }
}

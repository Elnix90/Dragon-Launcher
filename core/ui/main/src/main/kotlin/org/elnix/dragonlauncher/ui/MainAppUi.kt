package org.elnix.dragonlauncher.ui

import android.annotation.SuppressLint
import android.content.ComponentName
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.metadata
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import org.elnix.dragonlauncher.common.R
import org.elnix.dragonlauncher.common.messyfolder.Constants
import org.elnix.dragonlauncher.common.messyfolder.Constants.Logging.APP_LAUNCH_TAG
import org.elnix.dragonlauncher.common.messyfolder.Constants.Logging.SHIZUKU_TAG
import org.elnix.dragonlauncher.common.messyfolder.Constants.Logging.TAG
import org.elnix.dragonlauncher.common.messyfolder.Constants.PackageNames.SHIZUKU_PACKAGE_NAME
import org.elnix.dragonlauncher.common.messyfolder.Constants.URLs.URL_SHIZUKU_SITE
import org.elnix.dragonlauncher.common.messyfolder.findFragmentActivity
import org.elnix.dragonlauncher.common.messyfolder.openUrl
import org.elnix.dragonlauncher.common.messyfolder.showToast
import org.elnix.dragonlauncher.common.navigaton.NavigationRoute
import org.elnix.dragonlauncher.common.navigaton.isInIgnoredReturnScreen
import org.elnix.dragonlauncher.common.navigaton.isInTransparentScreen
import org.elnix.dragonlauncher.common.serializables.FloatingAppObject
import org.elnix.dragonlauncher.common.serializables.SwipeActionSerializable
import org.elnix.dragonlauncher.common.serializables.SwipePointSerializable
import org.elnix.dragonlauncher.common.serializables.SwipePointSerializable.Companion.dummySwipePoint
import org.elnix.dragonlauncher.common.utils.PermissionsUtils.hasUriReadWritePermission
import org.elnix.dragonlauncher.common.utils.PermissionsUtils.isAppInstalled
import org.elnix.dragonlauncher.common.utils.rememberIsDefaultLauncher
import org.elnix.dragonlauncher.enumsui.other.ReminderMode
import org.elnix.dragonlauncher.enumsui.toggle.DrawerToolbar
import org.elnix.dragonlauncher.logging.logD
import org.elnix.dragonlauncher.logging.logE
import org.elnix.dragonlauncher.logging.logW
import org.elnix.dragonlauncher.models.AppLifecycleViewModel
import org.elnix.dragonlauncher.models.AppsViewModel
import org.elnix.dragonlauncher.models.LockScreenViewModel
import org.elnix.dragonlauncher.models.PrivateSpaceViewModel
import org.elnix.dragonlauncher.models.ShizukuViewModel
import org.elnix.dragonlauncher.settings.stores.BackupSettingsStore
import org.elnix.dragonlauncher.settings.stores.BehaviorSettingsStore
import org.elnix.dragonlauncher.settings.stores.ColorModesSettingsStore
import org.elnix.dragonlauncher.settings.stores.DebugSettingsStore
import org.elnix.dragonlauncher.settings.stores.DrawerSettingsStore
import org.elnix.dragonlauncher.settings.stores.PrivateSettingsStore
import org.elnix.dragonlauncher.settings.stores.SwipeSettingsStore
import org.elnix.dragonlauncher.settings.stores.WellbeingSettingsStore
import org.elnix.dragonlauncher.ui.actions.AppLaunchException
import org.elnix.dragonlauncher.ui.actions.launchAppDirectly
import org.elnix.dragonlauncher.ui.actions.launchSwipeAction
import org.elnix.dragonlauncher.ui.base.asState
import org.elnix.dragonlauncher.ui.base.asStateNull
import org.elnix.dragonlauncher.ui.base.components.AnimatedFab
import org.elnix.dragonlauncher.ui.base.components.Spacer
import org.elnix.dragonlauncher.ui.base.overlays.OverlayHost
import org.elnix.dragonlauncher.ui.components.DebugViewModel
import org.elnix.dragonlauncher.ui.composition.LocalPoints
import org.elnix.dragonlauncher.ui.dialogs.AdbCommandInputDialog
import org.elnix.dragonlauncher.ui.dialogs.BackupResultDialog
import org.elnix.dragonlauncher.ui.dialogs.FilePickerDialog
import org.elnix.dragonlauncher.ui.dialogs.GoogleLockingWarningDialog
import org.elnix.dragonlauncher.ui.dialogs.MainScreeLayersTab
import org.elnix.dragonlauncher.ui.dialogs.PinUnlock
import org.elnix.dragonlauncher.ui.dialogs.ShizukuOutputDialog
import org.elnix.dragonlauncher.ui.dialogs.ShizukuUnavailableDialog
import org.elnix.dragonlauncher.ui.dialogs.WidgetPickerDialog
import org.elnix.dragonlauncher.ui.drawer.AppDrawerScreen
import org.elnix.dragonlauncher.ui.helpers.FpsCounterGraph
import org.elnix.dragonlauncher.ui.helpers.LauncherSnackbarHost
import org.elnix.dragonlauncher.ui.helpers.PrivateSpaceStateDebugDialog
import org.elnix.dragonlauncher.ui.helpers.ReselectAutoBackupBanner
import org.elnix.dragonlauncher.ui.helpers.SetDefaultLauncherBanner
import org.elnix.dragonlauncher.ui.navigation.horizontalMetadata
import org.elnix.dragonlauncher.ui.navigation.verticalMetadata
import org.elnix.dragonlauncher.ui.settings.backup.BackupTab
import org.elnix.dragonlauncher.ui.settings.customization.AngleLineTab
import org.elnix.dragonlauncher.ui.settings.customization.AppDisplayTab
import org.elnix.dragonlauncher.ui.settings.customization.AppearanceTab
import org.elnix.dragonlauncher.ui.settings.customization.BehaviorTab
import org.elnix.dragonlauncher.ui.settings.customization.ColorSelectorTab
import org.elnix.dragonlauncher.ui.settings.customization.DrawerTab
import org.elnix.dragonlauncher.ui.settings.customization.FontTab
import org.elnix.dragonlauncher.ui.settings.customization.HoldToActivateArcTab
import org.elnix.dragonlauncher.ui.settings.customization.IconPackTab
import org.elnix.dragonlauncher.ui.settings.customization.NestEditingScreen
import org.elnix.dragonlauncher.ui.settings.customization.StatusBarTab
import org.elnix.dragonlauncher.ui.settings.customization.ThemesTab
import org.elnix.dragonlauncher.ui.settings.customization.WallpaperTab
import org.elnix.dragonlauncher.ui.settings.customization.WidgetsTab
import org.elnix.dragonlauncher.ui.settings.debug.DebugTab
import org.elnix.dragonlauncher.ui.settings.debug.LogsTab
import org.elnix.dragonlauncher.ui.settings.debug.LogsViewerScreen
import org.elnix.dragonlauncher.ui.settings.debug.SettingsDebugTab
import org.elnix.dragonlauncher.ui.settings.extensions.ExtensionsTab
import org.elnix.dragonlauncher.ui.settings.wellbeing.WellbeingTab
import org.elnix.dragonlauncher.ui.settings.workspace.WorkspaceDetailScreen
import org.elnix.dragonlauncher.ui.settings.workspace.WorkspaceListScreen
import org.elnix.dragonlauncher.ui.welcome.WelcomeScreen
import org.elnix.dragonlauncher.ui.wellbeing.AppTimerService
import org.elnix.dragonlauncher.ui.wellbeing.DigitalPauseActivity
import org.elnix.dragonlauncher.ui.whatsnew.BackupReminder
import org.elnix.dragonlauncher.ui.whatsnew.ChangelogsScreen
import org.elnix.dragonlauncher.ui.whatsnew.WhatsNewBottomSheet
import rikka.shizuku.Shizuku


@Composable
inline fun <reified VM : ViewModel> activityViewModel(): VM {
    val activity = LocalActivity.current as ComponentActivity
    return hiltViewModel(activity)
}

@SuppressLint("LocalContextGetResourceValueCall")
@Composable
fun MainAppUi(
    appLifecycleViewModel: AppLifecycleViewModel = activityViewModel(),
    appsViewModel: AppsViewModel = activityViewModel(),
    lockScreenViewModel: LockScreenViewModel = activityViewModel(),
    privateSpaceViewModel: PrivateSpaceViewModel = activityViewModel(),
    shizukuViewModel: ShizukuViewModel = activityViewModel(),
    onBindCustomWidget: (Int, ComponentName, nestId: Int) -> Unit,
    onResetWidgetSize: (id: Int, widgetId: Int) -> Unit,
    onRemoveFloatingApp: (FloatingAppObject) -> Unit
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    val privateSpaceState = appsViewModel.privateSpaceState

    var showWidgetPicker by remember { mutableStateOf<Int?>(null) }
    var showFilePicker: SwipePointSerializable? by remember { mutableStateOf(null) }


    var showShizukuCommandPromter by remember { mutableStateOf<SwipeActionSerializable.RunAdbCommand?>(null) }
    val showShizukuUnavailableDialog by shizukuViewModel.showUnavailable.collectAsState()
    val hasShizukuPermission by shizukuViewModel.shizukuPermissionState().collectAsState()
    var isShizukuInstalled by rememberSaveable {
        mutableStateOf(
            ctx.isAppInstalled(
                SHIZUKU_PACKAGE_NAME
            )
        )
    }


    val autoShowKeyboardOnDrawer by DrawerSettingsStore.autoShowKeyboardOnDrawer.asState()

    val selectedToolbarItemsStringSet by DrawerSettingsStore.toolbarsOrder.asState()
    val selectedToolbarItems by remember {
        derivedStateOf {
            try {
                selectedToolbarItemsStringSet.split(',').map {
                    DrawerToolbar.valueOf(it)
                }
            } catch (e: Exception) {
                logE(Constants.Logging.DRAWER_TAG, e) { "Unable to decode drawerToolbars order, using default value" }
                DrawerToolbar.entries
            }
        }
    }


    val homeAction by BehaviorSettingsStore.homeAction.asState()

    val leftDrawerAction by DrawerSettingsStore.leftDrawerAction.asState()
    val rightDrawerAction by DrawerSettingsStore.rightDrawerAction.asState()

    val leftDrawerWidth by DrawerSettingsStore.leftDrawerWidth.asState()
    val rightDrawerWidth by DrawerSettingsStore.rightDrawerWidth.asState()


    val useAccessibilityInsteadOfContextToExpandActionPanel by DebugSettingsStore
        .useAccessibilityInsteadOfContextToExpandActionPanel.asState()


    val lifecycleOwner = LocalLifecycleOwner.current

    val startScreen = NavigationRoute.Main
    val backStack = rememberNavBackStack(startScreen)
    val currentRoute by remember {
        derivedStateOf { backStack.lastOrNull() ?: NavigationRoute.Main }
    }


    val isLocked by lockScreenViewModel.isLocked.collectAsState()
    val screenToUnlock by lockScreenViewModel.screenToUnlock.collectAsState()

    LaunchedEffect(currentRoute) {
        lockScreenViewModel.onEnterNewRoute(currentRoute)
    }


    /*  ─────────────  Wellbeing Settings  ─────────────  */ // TODO move this shit into a viewmodel
    val socialMediaPauseEnabled by WellbeingSettingsStore.socialMediaPauseEnabled.asState()
    val guiltModeEnabled by WellbeingSettingsStore.guiltModeEnabled.asState()
    val pauseDuration by WellbeingSettingsStore.pauseDurationSeconds.asState()
    val pausedApps by WellbeingSettingsStore.pausedApps.asState()
    val reminderEnabled by WellbeingSettingsStore.reminderEnabled.asState()
    val reminderInterval by WellbeingSettingsStore.reminderIntervalMinutes.asState()
    val reminderMode by WellbeingSettingsStore.reminderMode.asState()
    val returnToLauncherEnabled by WellbeingSettingsStore.returnToLauncherEnabled.asState()

    /* ───────────── Store pending package to launch after pause ───────────── */
    var pendingPackageToLaunch by remember { mutableStateOf<String?>(null) }
    var pendingUserIdToLaunch by remember { mutableStateOf<Int?>(null) }
    var pendingAppName by remember { mutableStateOf<String?>(null) }

    val digitalPauseLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (pendingPackageToLaunch != null) {
            val packageName = pendingPackageToLaunch!!

            logD(APP_LAUNCH_TAG) { "result: $result" }

            if (result.resultCode == DigitalPauseActivity.RESULT_PROCEED) {
                try {
                    // Start reminder-only timer if enabled (no time limit)
                    if (reminderEnabled) {
                        AppTimerService.start(
                            ctx = ctx,
                            packageName = packageName,
                            appName = pendingAppName ?: packageName,
                            reminderEnabled = true,
                            reminderIntervalMinutes = reminderInterval,
                            reminderMode = reminderMode
                        )
                    }

                    launchAppDirectly(
                        appsViewModel,
                        ctx,
                        packageName,
                        pendingUserIdToLaunch!!
                    )
                } catch (e: Exception) {
                    logE(TAG, e) { "Failed to launch after pause" }
                }
            } else if (result.resultCode == DigitalPauseActivity.RESULT_PROCEED_WITH_TIMER) {
                try {
                    val data = result.data
                    val timeLimitMin =
                        data?.getIntExtra(DigitalPauseActivity.RESULT_EXTRA_TIME_LIMIT, 10) ?: 10
                    val hasReminder =
                        data?.getBooleanExtra(DigitalPauseActivity.EXTRA_REMINDER_ENABLED, false)
                            ?: false
                    val remInterval =
                        data?.getIntExtra(DigitalPauseActivity.EXTRA_REMINDER_INTERVAL, 5) ?: 5
                    val remMode = try {
                        data?.getStringExtra(DigitalPauseActivity.EXTRA_REMINDER_MODE)?.let { ReminderMode.valueOf(it) } ?: ReminderMode.Overlay
                    } catch (_: Exception) {
                        null
                    } ?: ReminderMode.Overlay

                    AppTimerService.start(
                        ctx = ctx,
                        packageName = packageName,
                        appName = pendingAppName ?: packageName,
                        reminderEnabled = hasReminder,
                        reminderIntervalMinutes = remInterval,
                        reminderMode = remMode,
                        timeLimitEnabled = true,
                        timeLimitMinutes = timeLimitMin
                    )

                    launchAppDirectly(
                        appsViewModel,
                        ctx,
                        packageName,
                        pendingUserIdToLaunch!!
                    )
                } catch (e: Exception) {
                    logE(APP_LAUNCH_TAG, e) {
                        "Failed to launch after pause with timer"
                    }
                }
            }
        }
        pendingUserIdToLaunch = null
        pendingPackageToLaunch = null
        pendingAppName = null
    }


    @SuppressLint("LocalContextGetResourceValueCall")
    fun NavBackStack<NavKey>.navigate(screen: NavigationRoute) {

        fun go() {
            remove(screen)
            add(screen)
        }

        if (!isLocked) {
            go()
            return
        }

        if (screen in NavigationRoute.settingsRoutes) {
            val activity = ctx.findFragmentActivity()
            lockScreenViewModel.requestUnlock(activity, screen) {
                go()
            }
        } else {
            go()
        }
    }

    fun popBackMainScreen() {
        backStack.clear()
        backStack.add(NavigationRoute.Main)
    }


    val lastInteraction by appLifecycleViewModel.lastInteraction.collectAsState()
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val offScreenUserTimeout = lastInteraction.takeIf { it != -1L }

                if (offScreenUserTimeout != null) {
                    val isInIgnoredRoutes = currentRoute.isInIgnoredReturnScreen()

                    val userHasExceededTimeout = appLifecycleViewModel.isTimeoutExceeded(offScreenUserTimeout)

                    if (!isInIgnoredRoutes && userHasExceededTimeout) {
                        popBackMainScreen()
                    }
                }

            } else if (event == Lifecycle.Event.ON_PAUSE) {
                appLifecycleViewModel.updateLastInteraction()
            }
        }

        // Add the observer to the lifecycle
        lifecycleOwner.lifecycle.addObserver(observer)

        // When the noAnimComposable leaves the screen, remove the observer
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }




    fun launchWidgetsPicker(nestId: Int) {
        showWidgetPicker = nestId
    }


    fun runShisukuCommandNotEmpty(command: SwipeActionSerializable.RunAdbCommand) {
        if (!Shizuku.pingBinder()) {
            logD(SHIZUKU_TAG) { "Shizuku is not running, opening it..." }
            shizukuViewModel.setUnavailable()
            return
        }

        if (!hasShizukuPermission) {
            logD(SHIZUKU_TAG) { "Shizuku his not allowed" }

            shizukuViewModel.requestShizukuPermission()
        } else {
            logD(SHIZUKU_TAG) { "Shizuku tries to run the command: $command" }
            if (command.toast == true) {
                ctx.showToast("Running: $command")
            }
            shizukuViewModel.executeShizukuCommand(command.command)
        }
    }

    fun launchAction(point: SwipePointSerializable) {
        // Store package for potential pause callback
        val action = point.action

        // Store package for potential pause callback
        if (action is SwipeActionSerializable.LaunchApp) {
            pendingPackageToLaunch = action.packageName
            pendingUserIdToLaunch = action.userId ?: 0
            pendingAppName = point.customName ?: try {
                ctx.packageManager.getApplicationLabel(
                    ctx.packageManager.getApplicationInfo(action.packageName, 0)
                ).toString()
            } catch (_: Exception) {
                action.packageName
            }
        }

        appLifecycleViewModel.blockHomeActionsTemporarily()

        try {
            launchSwipeAction(
                ctx = ctx,
                appsViewModel = appsViewModel,
                action = action,
                useAccessibilityInsteadOfContextToExpandActionPanel = useAccessibilityInsteadOfContextToExpandActionPanel,
                pausedApps = pausedApps,
                socialMediaPauseEnabled = socialMediaPauseEnabled,
                guiltModeEnabled = guiltModeEnabled,
                pauseDuration = pauseDuration,
                reminderEnabled = reminderEnabled,
                reminderIntervalMinutes = reminderInterval,
                reminderMode = reminderMode,
                returnToLauncherEnabled = returnToLauncherEnabled,
                appName = pendingAppName ?: "",
                digitalPauseLauncher = digitalPauseLauncher,
                onOpenPrivateSpaceApp = { action ->
                    if (action !is SwipeActionSerializable.LaunchApp) return@launchSwipeAction

                    if (privateSpaceState.value.isLocked) {
                        privateSpaceViewModel.onUnlockPrivateSpace()
                    }

                    scope.launch {

                        logD(APP_LAUNCH_TAG) { "Waiting for private space to unlock before launch" }

                        val unlocked = withTimeoutOrNull(10_000L) {
                            privateSpaceState
                                .filter { !it.isLocked }
                                .first()
                        }

                        if (unlocked != null) {
                            logD(APP_LAUNCH_TAG) { "Private space unlocked, launching" }
                            launchAction(dummySwipePoint(action.copy(isPrivateSpace = false)))
                        } else {
                            logW(APP_LAUNCH_TAG) { "Timeout expired for private space unlock" }
                        }
                    }
                },
                onReloadApps = { scope.launch { appsViewModel.reloadApps() } },
                onReselectFile = { showFilePicker = point },
                onAppSettings = backStack::navigate,
                onAppDrawer = { workspaceId ->
                    if (workspaceId != null) {
                        appsViewModel.selectWorkspace(workspaceId)
                    }
                    backStack.navigate(NavigationRoute.Drawer)
                },
                onShizukuCommand = { command ->
                    logD(SHIZUKU_TAG) { "Got shizuku command: $command" }

                    if (command.command.trim().isEmpty()) {
                        showShizukuCommandPromter = command
                    }

                    runShisukuCommandNotEmpty(command)
                }
            )
        } catch (e: AppLaunchException) {
            logE(TAG, e) { "Failed to launch action" }
        } catch (e: Exception) {
            logE(TAG, e) { "Unknow error while launching action" }
        }
    }

    fun launchAction(action: SwipeActionSerializable) {
        launchAction(
            dummySwipePoint(action)
        )
    }


    // Drawer home action receiver
    var drawerHomeHandler by remember { mutableStateOf<(() -> Unit)?>(null) }

    LaunchedEffect(Unit) {
        appLifecycleViewModel.homeEvents.collect {

            logD(TAG) { "Got home event, launching home action, currentRoute: $currentRoute" }
            when (currentRoute) {
                NavigationRoute.Drawer -> {
                    drawerHomeHandler?.invoke()
                }

                NavigationRoute.Main -> {
                    launchAction(homeAction)
                }

                NavigationRoute.Welcome -> {
                    // Do nothing when in welcome screen
                }

                // Return to home screen in case of any home action in the settings
                else -> {
                    popBackMainScreen()
                }
            }
        }
    }

    val containerColor by animateColorAsState(
        if (currentRoute.isInTransparentScreen()) Color.Transparent
        else MaterialTheme.colorScheme.background
    )

    val colorTestMode by ColorModesSettingsStore.colorTestMode.asState()


    val hasSeenWelcome by PrivateSettingsStore.hasSeenWelcome.asStateNull()
    LaunchedEffect(hasSeenWelcome) {
        if (hasSeenWelcome == false) {
            backStack.navigate(NavigationRoute.Welcome)
        }
    }

    ProvideGlobalCompositionLocals {
        OverlayHost(
            modifier = Modifier
                .background(Color.Transparent)
                .fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            Scaffold(
                floatingActionButton = {
                    if (colorTestMode) {
                        AnimatedFab(
                            onClick = { backStack.navigate(NavigationRoute.Colors) },
                            icon = R.drawable.edit_rounded
                        )
                    }
                },
                snackbarHost = {
                    LauncherSnackbarHost()
                },
                contentWindowInsets = WindowInsets(),
                containerColor = containerColor,
            ) { paddingValues ->

                NavDisplay(
                    backStack = backStack,
                    modifier = Modifier.padding(paddingValues),
                    onBack = { backStack.navigateBack() },
                    entryDecorators = listOf(
                        rememberSaveableStateHolderNavEntryDecorator(),
                        rememberViewModelStoreNavEntryDecorator()
                    ),
                    predictivePopTransitionSpec = {
                        ContentTransform(
                            fadeIn(),
                            slideOutHorizontally { it },
                        )
                    },
                    popTransitionSpec = {
                        ContentTransform(
                            fadeIn(),
                            slideOutHorizontally { it },
                        )
                    },
                    entryProvider = entryProvider {

                        entry<NavigationRoute.Main>(metadata = verticalMetadata) {
                            MainScreen(backStack::navigate, ::launchAction)
                        }

                        entry<NavigationRoute.Drawer>(
                            metadata = metadata {
                                put(NavDisplay.TransitionKey) {
                                    // Slide new content up, keeping the old content in place underneath
                                    slideInVertically(
                                        initialOffsetY = { it },
                                        animationSpec = tween(250)
                                    ) togetherWith ExitTransition.KeepUntilTransitionsFinished
                                }
                                put(NavDisplay.PopTransitionKey) {
                                    // Slide old content down, revealing the new content in place underneath
                                    EnterTransition.None togetherWith
                                            slideOutVertically(
                                                targetOffsetY = { it },
                                                animationSpec = tween(250)
                                            )
                                }
                                put(NavDisplay.PredictivePopTransitionKey) {
                                    // Slide old content down, revealing the new content in place underneath
                                    EnterTransition.None togetherWith
                                            slideOutVertically(
                                                targetOffsetY = { it },
                                                animationSpec = tween(250)
                                            )
                                }
                            }
                        ) {
                            AppDrawerScreen(
                                autoShowKeyboard = autoShowKeyboardOnDrawer,
                                onRegisterHomeHandler = { handler ->
                                    drawerHomeHandler = handler
                                },
                                drawerToolbarsOrder = selectedToolbarItems,
                                leftAction = leftDrawerAction,
                                leftWeight = leftDrawerWidth,
                                rightAction = rightDrawerAction,
                                rightWeight = rightDrawerWidth,
                                onLaunchAction = {
                                    launchAction(it)
                                    backStack.navigateBack()
                                },
                                onNavigate = backStack::navigate,
                                onClose = backStack::navigateBack
                            )
                        }

                        entry<NavigationRoute.Welcome>(metadata = horizontalMetadata) {
                            WelcomeScreen(
                                onEnterSettings = { backStack.navigate(NavigationRoute.PointsSettings) },
                                onEnterApp = backStack::navigateBack
                            )
                        }

                        entry<NavigationRoute.PointsSettings>(metadata = horizontalMetadata) {
                            PointsSettingsScreen(
                                onAdvSettings = { backStack.navigate(NavigationRoute.Settings) },
                                onNestEdit = {
                                    backStack.navigate(NavigationRoute.NestEdit(it))
                                },
                                onBack = backStack::navigateBack
                            )
                        }

                        entry<NavigationRoute.Settings>(metadata = horizontalMetadata) {
                            AdvancedSettingsScreen(
                                backStack::navigate,
                                backStack::navigateBack
                            )
                        }
                        entry<NavigationRoute.Appearance>(metadata = horizontalMetadata) {
                            AppearanceTab(
                                backStack::navigate,
                                backStack::navigateBack
                            )
                        }
                        entry<NavigationRoute.Behavior>(metadata = horizontalMetadata) { BehaviorTab(backStack::navigateBack) }
                        entry<NavigationRoute.DrawerSettings>(metadata = horizontalMetadata) { DrawerTab(backStack::navigateBack) }
                        entry<NavigationRoute.Backup>(metadata = horizontalMetadata) { BackupTab(backStack::navigateBack) }
                        entry<NavigationRoute.Changelogs>(metadata = horizontalMetadata) { ChangelogsScreen(backStack::navigateBack) }
                        entry<NavigationRoute.Extensions>(metadata = horizontalMetadata) { ExtensionsTab(backStack::navigateBack) }
                        entry<NavigationRoute.Wellbeing>(metadata = horizontalMetadata) { WellbeingTab(backStack::navigateBack) }
                        entry<NavigationRoute.Debug>(metadata = horizontalMetadata) { DebugTab(backStack::navigate, backStack::navigateBack) }
                        entry<NavigationRoute.Logs>(metadata = horizontalMetadata) { LogsTab(backStack::navigate, backStack::navigateBack) }
                        entry<NavigationRoute.SettingsJson>(metadata = horizontalMetadata) { SettingsDebugTab(backStack::navigateBack) }

                        // All the appearance sub-settings
                        entry<NavigationRoute.AppDisplay>(metadata = horizontalMetadata) { AppDisplayTab(backStack::navigateBack) }
                        entry<NavigationRoute.Colors>(metadata = horizontalMetadata) { ColorSelectorTab(backStack::navigateBack) }
                        entry<NavigationRoute.Theme>(metadata = horizontalMetadata) { ThemesTab(backStack::navigateBack) }
                        entry<NavigationRoute.Wallpaper>(metadata = horizontalMetadata) { WallpaperTab(backStack::navigateBack) }
                        entry<NavigationRoute.IconPack>(metadata = horizontalMetadata) { IconPackTab(backStack::navigateBack) }
                        entry<NavigationRoute.StatusBar>(metadata = horizontalMetadata) { StatusBarTab(backStack::navigateBack) }
                        entry<NavigationRoute.Fonts>(metadata = horizontalMetadata) { FontTab(backStack::navigateBack) }
                        entry<NavigationRoute.AngleLineEdit>(metadata = horizontalMetadata) { AngleLineTab(backStack::navigateBack) }
                        entry<NavigationRoute.HoldToActivateArc>(metadata = horizontalMetadata) { HoldToActivateArcTab(backStack::navigateBack) }
                        entry<NavigationRoute.MainScreenLayers>(metadata = horizontalMetadata) { MainScreeLayersTab(backStack::navigateBack) }

                        entry<NavigationRoute.LogsViewer>(metadata = horizontalMetadata) { key ->
                            LogsViewerScreen(
                                filename = key.filename,
                                onBack = backStack::navigateBack
                            )
                        }

                        entry<NavigationRoute.NestEdit>(metadata = horizontalMetadata) { key ->
                            NestEditingScreen(
                                nestId = key.nestId,
                                onBack = backStack::navigateBack
                            )
                        }

                        entry<NavigationRoute.Widgets>(metadata = horizontalMetadata) { key ->
                            WidgetsTab(
                                onBack = backStack::navigateBack,
                                onLaunchSystemWidgetPicker = ::launchWidgetsPicker,
                                onResetWidgetSize = onResetWidgetSize,
                                onRemoveWidget = onRemoveFloatingApp,
                                initialNestId = key.nestId
                            )
                        }

                        entry<NavigationRoute.Workspace>(metadata = horizontalMetadata) {
                            WorkspaceListScreen(
                                onOpenWorkspace = { id ->
                                    backStack.navigate(NavigationRoute.WorkspaceDetail(id))
                                },
                                onBack = backStack::navigateBack
                            )
                        }

                        entry<NavigationRoute.WorkspaceDetail>(metadata = horizontalMetadata) { key ->

                            WorkspaceDetailScreen(
                                workspaceId = key.workspaceId,
                                onBack = backStack::navigateBack,
                                onLaunchAction = ::launchAction
                            )
                        }

//                        entry<NavigationRoute.PinUnlock>(metadata = horizontalMetadata) { key ->
//                            PinUnlock(
//                                onDismiss = backStack::navigateBack,
//                                onValidate = {
//                                    backStack.remove<Any>(NavigationRoute.PinUnlock)
//                                    backStack.navigate(key.screenToGo) }
//                            )
//                        }
                    }
                )
            }
        }

        if (showFilePicker != null) {
            val currentPoint = showFilePicker!!
            val points = LocalPoints.current

            FilePickerDialog(
                onDismiss = { showFilePicker = null },
                onFileSelected = { newAction ->

                    // Build the updated point
                    val updatedPoint = currentPoint.copy(action = newAction)

                    // Replace only this point
                    val finalList = points.map { p ->
                        if (p.id == currentPoint.id) updatedPoint else p
                    }

                    scope.launch {
                        SwipeSettingsStore.savePoints(ctx, finalList)
                    }

                    showFilePicker = null
                }
            )
        }


        if (showWidgetPicker != null) {
            val nestToBind = showWidgetPicker!!
            WidgetPickerDialog(
                onBindCustomWidget = { id, info ->
                    onBindCustomWidget(id, info, nestToBind)
                }
            ) { showWidgetPicker = null }
        }


        if (showShizukuCommandPromter != null) {
            AdbCommandInputDialog(
                onDismiss = { showShizukuCommandPromter = null },
                showLeaveEmptyNotice = false
            ) {
                if (it.command.trim().isNotEmpty()) {
                    runShisukuCommandNotEmpty(it)
                }
            }
        }

        if (showShizukuUnavailableDialog) {
            ShizukuUnavailableDialog(
                onDismiss = {
                    shizukuViewModel.dismissUnavailableDialog()
                },
                onConfirm = {
                    if (isShizukuInstalled) launchAction(SwipeActionSerializable.LaunchApp(SHIZUKU_PACKAGE_NAME, false, 0))
                    else ctx.openUrl(
                        url = URL_SHIZUKU_SITE
                    )
                }
            )
        }
        BackupReminder { backStack.navigate(NavigationRoute.Backup) }

        BottomBanners(currentRoute)
        ShizukuOutputDialog()
        FpsCounterGraph()
        WhatsNewBottomSheet()
        BackupResultDialog()
        GoogleLockingWarningDialog()
        PrivateSpaceStateDebugDialog()
        DebugViewModel()


//        DragonColumnGroup {
//            Text("isLocked: $isLocked; screenToUnlock: $screenToUnlock")
//        }

        if (isLocked && screenToUnlock != null) {
            PinUnlock(
                onDismiss = {
                    lockScreenViewModel.cancelPinUnlock()
                },
                onValidate = {
                    lockScreenViewModel.unlock()
                    backStack.remove(screenToUnlock!!)
                    backStack.add(screenToUnlock!!)
                }
            )
        }
    }
}

@Composable
private fun BottomBanners(currentRoute: NavKey) {
    val ctx = LocalContext.current

    val showSetDefaultLauncherBanner by PrivateSettingsStore.showSetDefaultLauncherBanner.asStateNull()
    val isDefaultLauncher = rememberIsDefaultLauncher()

    val autoBackupEnabled by BackupSettingsStore.autoBackupEnabled.asState()
    val autoBackupUriString by BackupSettingsStore.autoBackupUri.asStateNull()
    val autoBackupUri = autoBackupUriString?.toUri()


    val showSetAsDefaultBanner = (showSetDefaultLauncherBanner == true) &&
            !isDefaultLauncher &&
            currentRoute != NavigationRoute.Welcome


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
                currentRoute != NavigationRoute.Welcome



    if (showSetAsDefaultBanner || showReselectAutoBackupFile) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Spacer()
            AnimatedVisibility(showSetAsDefaultBanner) {
                SetDefaultLauncherBanner()
            }
            AnimatedVisibility(showReselectAutoBackupFile) {
                ReselectAutoBackupBanner()
            }
        }
    }
}

private fun NavBackStack<NavKey>.navigateBack() {
    // Popping the only screen will crash so this avoids it
    if (size == 1) return
    removeLastOrNull()
}
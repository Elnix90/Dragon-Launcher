@file:Suppress("AssignedValueIsNeverRead")

package org.elnix.dragonlauncher.ui

import android.annotation.SuppressLint
import android.content.ComponentName
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import org.elnix.dragonlauncher.base.model.serializables.Widget
import org.elnix.dragonlauncher.models.AppLaunchViewModel
import org.elnix.dragonlauncher.models.AppLifecycleViewModel
import org.elnix.dragonlauncher.models.LockScreenViewModel
import org.elnix.dragonlauncher.models.PointViewModel
import org.elnix.dragonlauncher.models.ShizukuViewModel
import org.elnix.dragonlauncher.ui.base.activityViewModel


@SuppressLint("LocalContextGetResourceValueCall")
@Composable
fun MainAppUi(
    appLifecycleViewModel: AppLifecycleViewModel = activityViewModel(),
//    drawerViewModel: DrawerViewModel = activityViewModel(),
    lockScreenViewModel: LockScreenViewModel = activityViewModel(),
    appLaunchViewModel: AppLaunchViewModel = activityViewModel(),
    shizukuViewModel: ShizukuViewModel = activityViewModel(),
    pointViewModel: PointViewModel = activityViewModel(),
    onBindCustomWidget: (Int, ComponentName, nestId: Int) -> Unit,
    onResetWidgetSize: (id: Int, widgetId: Int) -> Unit,
    onRemoveWidget: (Widget) -> Unit
) {
    Button({}) {
        Text("Clickme")
    }

//    val ctx = LocalContext.current
//
//    var showWidgetPicker by remember { mutableStateOf<Int?>(null) }
//    var showFilePicker: Point? by remember { mutableStateOf(null) }
//
//    var showShizukuCommandPromter by remember { mutableStateOf<Action.RunAdbCommand?>(null) }
//    val showShizukuUnavailableDialog by shizukuViewModel.showUnavailable.collectAsState()
//    val hasShizukuPermission by shizukuViewModel.shizukuPermissionState().collectAsState()
//    val isShizukuInstalled by drawerViewModel.isAppInstalled(SHIZUKU_PACKAGE_NAME).collectAsState()
//
//
//    val homeAction by BehaviorSettingsStore.homeAction.asState()
//    val useAccessibilityInsteadOfContextToExpandActionPanel by DebugSettingsStore.useAccessibilityInsteadOfContextToExpandActionPanel.asState()
//
//
//    val lifecycleOwner = LocalLifecycleOwner.current
//
//    val startScreen = NavigationRoute.Main
//    val backStack = rememberNavBackStack(startScreen)
//    val currentRoute by remember {
//        derivedStateOf { backStack.lastOrNull() ?: NavigationRoute.Main }
//    }
//
//    val securityService = lockScreenViewModel.securityService
//    val isLocked by lockScreenViewModel.isLocked.collectAsState()
//    val screenToUnlock by lockScreenViewModel.screenToUnlock.collectAsState()
//    val lockMethod by lockScreenViewModel.lockMethod.collectAsState()
//
//
//    LaunchedEffect(currentRoute) {
//        lockScreenViewModel.onEnterNewRoute(currentRoute)
//    }
//
//
//
//
//    @SuppressLint("LocalContextGetResourceValueCall")
//    fun NavBackStack<NavKey>.navigate(screen: NavigationRoute) {
//
//        fun go() {
//            remove(screen)
//            add(screen)
//        }
//
//        if (!isLocked) {
//            go()
//            return
//        }
//
//        if (screen in NavigationRoute.settingsRoutes && lockMethod != LockMethod.NONE) {
//            lockScreenViewModel.requestUnlock(screen)
//        } else {
//            go()
//        }
//    }
//
//    fun popBackMainScreen() {
//        backStack.clear()
//        backStack.add(NavigationRoute.Main)
//    }
//
//    logWtf { "Got here 2" }
//
//    val lastInteraction by appLifecycleViewModel.lastInteraction.collectAsState()
//    DisposableEffect(lifecycleOwner) {
//        val observer = LifecycleEventObserver { _, event ->
//            if (event == Lifecycle.Event.ON_RESUME) {
//                val offScreenUserTimeout = lastInteraction.takeIf { it != -1L }
//
//                if (offScreenUserTimeout != null) {
//                    val isInIgnoredRoutes = currentRoute.isInIgnoredReturnScreen()
//
//                    val userHasExceededTimeout = appLifecycleViewModel.isTimeoutExceeded(offScreenUserTimeout)
//
//                    if (!isInIgnoredRoutes && userHasExceededTimeout) {
//                        popBackMainScreen()
//                    }
//                }
//
//            } else if (event == Lifecycle.Event.ON_PAUSE) {
//                appLifecycleViewModel.updateLastInteraction()
//            }
//        }
//
//        // Add the observer to the lifecycle
//        lifecycleOwner.lifecycle.addObserver(observer)
//
//        // When the noAnimComposable leaves the screen, remove the observer
//        onDispose {
//            lifecycleOwner.lifecycle.removeObserver(observer)
//        }
//    }
//
//
//    val launcherReceiver = object : BroadcastReceiver() {
//        override fun onReceive(context: Context?, intent: Intent?) {
//            if (intent?.action == SHOW_LAUNCHER) {
//                val appName = intent.getStringExtra(EXTRA_APP_NAME)
//                backStack.navigate(NavigationRoute.TimerExceeded(appName ?: "Unknown App"))
//            }
//        }
//    }
//    LaunchedEffect(Unit) {
//        registerReceiver(
//            ctx,
//            launcherReceiver,
//
//            IntentFilter("com.elnix.dragonlauncher.SHOW_LAUNCHER"), ContextCompat.RECEIVER_NOT_EXPORTED
//        )
//    }
//
//
//    fun launchWidgetsPicker(nestId: Int) {
//        showWidgetPicker = nestId
//    }
//
//
//    fun runShisukuCommandNotEmpty(command: Action.RunAdbCommand) {
//        if (!Shizuku.pingBinder()) {
//            logD(SHIZUKU_TAG) { "Shizuku is not running, opening it..." }
//            shizukuViewModel.setUnavailable()
//            return
//        }
//
//        if (!hasShizukuPermission) {
//            logD(SHIZUKU_TAG) { "Shizuku his not allowed" }
//
//            shizukuViewModel.requestShizukuPermission()
//        } else {
//            logD(SHIZUKU_TAG) { "Shizuku tries to run the command: $command" }
//            if (command.toast == true) {
//                ctx.showToast("Running: $command")
//            }
//            shizukuViewModel.executeShizukuCommand(command.command)
//        }
//    }
//
//    fun launchAction(point: Point) {
//        val action = point.action
//
//        if (action is Action.LaunchApp) {
//            pendingPackageToLaunch = action.packageName
//            pendingUserIdToLaunch = action.userId ?: 0
//            pendingAppName = point.customName ?: try {
//                ctx.packageManager.getApplicationLabel(
//                    ctx.packageManager.getApplicationInfo(action.packageName, 0)
//                ).toString()
//            } catch (_: Exception) {
//                action.packageName
//            }
//        }

//        appLifecycleViewModel.blockHomeActionsTemporarily()
//
//        try {
//            launchAction(
//                ctx = ctx,
//                appLaunchViewModel = appLaunchViewModel,
//                drawerViewModel = drawerViewModel,
//                action = action,
////                pausedApps = pausedApps,
////                socialMediaPauseEnabled = socialMediaPauseEnabled,
////                guiltModeEnabled = guiltModeEnabled,
////                pauseDuration = pauseDuration,
////                reminderEnabled = reminderEnabled,
////                reminderIntervalMinutes = reminderInterval,
////                reminderMode = reminderMode,
////                returnToLauncherEnabled = returnToLauncherEnabled,
////                appName = pendingAppName ?: "",
////                onOpenPrivateSpaceApp = { action ->
////                    if (action !is Action.LaunchApp) return@launchAction
////
////                    if (privateSpaceState.value.isLocked) {
////                        profilesViewModel.onUnlockPrivateSpace()
////                    }
////
////                    scope.launch {
////
////                        logD(APP_LAUNCH_TAG) { "Waiting for private space to unlock before launch" }
////
////                        val unlocked = withTimeoutOrNull(10_000L) {
////                            privateSpaceState
////                                .filter { !it.isLocked }
////                                .first()
////                        }
////
////                        if (unlocked != null) {
////                            logD(APP_LAUNCH_TAG) { "Private space unlocked, launching" }
////                            launchAction(dummySwipePoint(action.copy(isPrivateSpace = false)))
////                        } else {
////                            logW(APP_LAUNCH_TAG) { "Timeout expired for private space unlock" }
////                        }
////                    }
////                },
//                useAccessibilityInsteadOfContextToExpandActionPanel = useAccessibilityInsteadOfContextToExpandActionPanel,
////                digitalPauseLauncher = digitalPauseLauncher,
////                onReloadApps = { scope.launch { TODO()/*appsViewModel.reloadApps() */} },
//                onReselectFile = { showFilePicker = point },
//                onAppSettings = backStack::navigate,
//                onAppDrawer = { workspaceId ->
//                    if (workspaceId != null) { drawerViewModel.selectWorkspace(workspaceId) }
//                    backStack.navigate(NavigationRoute.Drawer)
//                }
//            ) { command ->
//                if (command.command.trim().isEmpty()) {
//                    showShizukuCommandPromter = command
//                }
//                runShisukuCommandNotEmpty(command)
//            }
//        } catch (e: AppLaunchException) {
//            logE(TAG, e) { "Failed to launch action" }
//        } catch (e: Exception) {
//            logE(TAG, e) { "Unknow error while launching action" }
//        }
//    }
//
//    fun launchAction(action: Action) {
//        launchAction(
//            dummySwipePoint(action)
//        )
//    }
//
//
//    // Drawer home action receiver
//    var drawerHomeHandler by remember { mutableStateOf<(() -> Unit)?>(null) }
//
//    LaunchedEffect(Unit) {
//        appLifecycleViewModel.homeEvents.collect {
//
//            logD(TAG) { "Got home event, launching home action, currentRoute: $currentRoute" }
//            when (currentRoute) {
//                NavigationRoute.Drawer -> {
//                    drawerHomeHandler?.invoke()
//                }
//
//                NavigationRoute.Main -> {
//                    launchAction(homeAction)
//                }
//
//                NavigationRoute.Welcome -> {
//                    // Do nothing when in welcome screen
//                }
//
//                // Return to home screen in case of any home action in the settings
//                else -> {
//                    popBackMainScreen()
//                }
//            }
//        }
//    }
//
//    val containerColor by animateColorAsState(
//        if (currentRoute.isInTransparentScreen()) Color.Transparent
//        else MaterialTheme.colorScheme.background
//    )
//
//    val colorTestMode by ColorModesSettingsStore.colorTestMode.asState()
//
//
//    val hasSeenWelcome by PrivateSettingsStore.hasSeenWelcome.asStateNull()
//    LaunchedEffect(hasSeenWelcome) {
//        if (hasSeenWelcome == false) {
//            backStack.navigate(NavigationRoute.Welcome)
//        }
//    }
//
//    ProvideGlobalCompositionLocals {
//        Scaffold(
//            floatingActionButton = {
//                if (colorTestMode) {
//                    AnimatedFab(
//                        onClick = { backStack.navigate(NavigationRoute.Colors) },
//                        icon = R.drawable.edit_rounded
//                    )
//                }
//            },
//            snackbarHost = {
//                LauncherSnackbarHost()
//            },
//            contentWindowInsets = WindowInsets(),
//            containerColor = containerColor,
//        ) { paddingValues ->
//
//            NavDisplay(
//                backStack = backStack,
//                modifier = Modifier.padding(paddingValues),
//                onBack = { backStack.navigateBack() },
//                entryDecorators = listOf(
//                    rememberSaveableStateHolderNavEntryDecorator(),
//                    rememberViewModelStoreNavEntryDecorator()
//                ),
//                predictivePopTransitionSpec = {
//                    ContentTransform(
//                        fadeIn(),
//                        slideOutHorizontally { it },
//                    )
//                },
//                popTransitionSpec = {
//                    ContentTransform(
//                        fadeIn(),
//                        slideOutHorizontally { it },
//                    )
//                },
//                entryProvider = entryProvider {
//
//                    entry<NavigationRoute.Main>(metadata = verticalMetadata) {
//                        MainScreen(backStack::navigate, ::launchAction)
//                    }
//
//                    entry<NavigationRoute.Drawer>(metadata = drawerMetadata) {
//                        AppDrawerScreen(
//                            onRegisterHomeHandler = { handler ->
//                                drawerHomeHandler = handler
//                            },
//                            onLaunchAction = {
//                                launchAction(it)
//                                backStack.navigateBack()
//                            },
//                            onNavigate = backStack::navigate,
//                            onClose = backStack::navigateBack
//                        )
//                    }
//
//                    entry<NavigationRoute.Welcome>(metadata = horizontalMetadata) {
//                        WelcomeScreen(
//                            onEnterSettings = {
//                                popBackMainScreen()
//                                backStack.navigate(NavigationRoute.PointsSettings(0))
//                            },
//                            onEnterApp = ::popBackMainScreen
//                        )
//                    }
//
//                    entry<NavigationRoute.PointsSettings>(metadata = horizontalMetadata) {
//                        PointsSettingsScreen(
//                            onAdvSettings = {
//                                pointViewModel.pointsService.persist()
//                                backStack.navigate(NavigationRoute.Settings)
//                            },
//                            onNestEdit = {
//                                pointViewModel.pointsService.persist()
//                                backStack.navigate(NavigationRoute.NestEdit(it))
//                            },
//                            onBack = {
//                                pointViewModel.pointsService.persist()
//                                backStack.navigateBack()
//                            }
//                        )
//                    }
//
//                    entry<NavigationRoute.Settings>(metadata = horizontalMetadata) {
//                        SettingsScreen(
//                            backStack::navigate,
//                            backStack::navigateBack
//                        )
//                    }
//                    entry<NavigationRoute.Appearance>(metadata = horizontalMetadata) {
//                        AppearanceTab(
//                            backStack::navigate,
//                            backStack::navigateBack
//                        )
//                    }
//                    entry<NavigationRoute.Behavior>(metadata = horizontalMetadata) { BehaviorTab(backStack::navigateBack) }
//                    entry<NavigationRoute.DrawerSettings>(metadata = horizontalMetadata) { DrawerTab(backStack::navigateBack) }
//                    entry<NavigationRoute.Backup>(metadata = horizontalMetadata) { BackupTab(backStack::navigateBack) }
//                    entry<NavigationRoute.Changelogs>(metadata = horizontalMetadata) { ChangelogsScreen(backStack::navigateBack) }
//                    entry<NavigationRoute.Extensions>(metadata = horizontalMetadata) { ExtensionsTab(backStack::navigateBack) }
//                    entry<NavigationRoute.Wellbeing>(metadata = horizontalMetadata) { WellbeingTab(backStack::navigateBack) }
//                    entry<NavigationRoute.Debug>(metadata = horizontalMetadata) { DebugTab(backStack::navigate, backStack::navigateBack) }
//                    entry<NavigationRoute.Logs>(metadata = horizontalMetadata) { LogsTab(backStack::navigate, backStack::navigateBack) }
//                    entry<NavigationRoute.SettingsJson>(metadata = horizontalMetadata) { SettingsDebugTab(backStack::navigateBack) }
//
//                    // All the appearance sub-settings
//                    entry<NavigationRoute.AppDisplay>(metadata = horizontalMetadata) { AppDisplayTab(backStack::navigateBack) }
//                    entry<NavigationRoute.Colors>(metadata = horizontalMetadata) { ColorSelectorTab(backStack::navigateBack) }
//                    entry<NavigationRoute.Theme>(metadata = horizontalMetadata) { ThemesTab(backStack::navigateBack) }
//                    entry<NavigationRoute.Wallpaper>(metadata = horizontalMetadata) { WallpaperTab(backStack::navigateBack) }
//                    entry<NavigationRoute.IconPack>(metadata = horizontalMetadata) { IconPackTab(backStack::navigateBack) }
//                    entry<NavigationRoute.StatusBar>(metadata = horizontalMetadata) { StatusBarTab(backStack::navigateBack) }
//                    entry<NavigationRoute.Fonts>(metadata = horizontalMetadata) { FontTab(backStack::navigateBack) }
//                    entry<NavigationRoute.AngleLineEdit>(metadata = horizontalMetadata) { AngleLineTab(backStack::navigateBack) }
//                    entry<NavigationRoute.HoldToActivateArc>(metadata = horizontalMetadata) { HoldToActivateArcTab(backStack::navigateBack) }
//                    entry<NavigationRoute.MainScreenLayers>(metadata = horizontalMetadata) { MainScreeLayersTab(backStack::navigateBack) }
//
//                    entry<NavigationRoute.LogsViewer>(metadata = horizontalMetadata) { key ->
//                        LogsViewerScreen(
//                            filename = key.filename,
//                            onBack = backStack::navigateBack
//                        )
//                    }
//
//                    entry<NavigationRoute.NestEdit>(metadata = horizontalMetadata) { key ->
//                        NestEditingScreen(
//                            nestId = key.nestId,
//                            onBack = {
//                                backStack.navigateBack()
//                                pointViewModel.pointsService.persist()
//                            }
//                        )
//                    }
//
//                    entry<NavigationRoute.Widgets>(metadata = horizontalMetadata) { key ->
//                        WidgetsTab(
//                            onBack = backStack::navigateBack,
//                            onLaunchSystemWidgetPicker = ::launchWidgetsPicker,
//                            onResetWidgetSize = onResetWidgetSize,
//                            onRemoveWidget = onRemoveWidget,
//                            initialNestId = key.nestId
//                        )
//                    }
//
//                    entry<NavigationRoute.Workspace>(metadata = horizontalMetadata) {
//                        WorkspaceListScreen(
//                            onOpenWorkspace = { id ->
//                                backStack.navigate(NavigationRoute.WorkspaceDetail(id))
//                            },
//                            onBack = backStack::navigateBack
//                        )
//                    }
//
//                    entry<NavigationRoute.WorkspaceDetail>(metadata = horizontalMetadata) { key ->
//
//                        WorkspaceDetailScreen(
//                            workspaceId = key.workspaceId,
//                            onBack = backStack::navigateBack
//                        )
//                    }
//
//                    entry<NavigationRoute.TimerExceeded> { key ->
//                        TimeLimitExceededScreen(key.appName, backStack::navigateBack)
//                    }
//                }
//            )
//        }
//
//
//        if (showFilePicker != null) {
//            val currentPoint = showFilePicker!!
//
//            FilePickerDialog(
//                onDismiss = { showFilePicker = null },
//                onFileSelected = { newAction ->
//                    val updatedPoint = currentPoint.copy(action = newAction)
//                    pointViewModel.pointsService.editPoint(currentPoint.id) { updatedPoint }
//                    showFilePicker = null
//                    launchAction(updatedPoint)
//                }
//            )
//        }
//
//
//        if (showWidgetPicker != null) {
//            val nestToBind = showWidgetPicker!!
//            WidgetPickerDialog(
//                onBindCustomWidget = { id, info ->
//                    onBindCustomWidget(id, info, nestToBind)
//                }
//            ) { showWidgetPicker = null }
//        }
//
//
//        if (showShizukuCommandPromter != null) {
//            AdbCommandInputDialog(
//                onDismiss = { showShizukuCommandPromter = null },
//                showLeaveEmptyNotice = false
//            ) {
//                if (it.command.trim().isNotEmpty()) {
//                    runShisukuCommandNotEmpty(it)
//                }
//            }
//        }
//
//        if (showShizukuUnavailableDialog) {
//            ShizukuUnavailableDialog(
//                onDismiss = {
//                    shizukuViewModel.dismissUnavailableDialog()
//                },
//                onConfirm = {
//                    if (isShizukuInstalled) launchAction(Action.LaunchApp(SHIZUKU_PACKAGE_NAME, Profile.dummy()))
//                    else ctx.openUrl(
//                        url = URL_SHIZUKU_SITE
//                    )
//                }
//            )
//        }
//
//        val pendingAppToLaunch by appLaunchViewModel.pendingAppLaunch.collectAsState(null)
//
//        if (pendingAppToLaunch != null) {
//            val pendingApp = pendingAppToLaunch!!
//            DigitalPauseScreen(
//                application = pendingApp,
////                                    onProceedWithTimer = { timeLimitMinutes ->
////                                        val data = Intent().apply {
////                                            putExtra(RESULT_EXTRA_TIME_LIMIT, timeLimitMinutes)
////                                            putExtra(EXTRA_REMINDER_ENABLED, reminderEnabled)
////                                            putExtra(EXTRA_REMINDER_INTERVAL, reminderInterval)
////                                            putExtra(EXTRA_REMINDER_MODE, reminderMode)
////                                        }
////                                        setResult(RESULT_PROCEED_WITH_TIMER, data)
////                                        finish()
////                                    },
//
//                onCancel = backStack::navigateBack
//            )
//        }
//
//        BottomBanners(currentRoute)
//        ShizukuOutputDialog()
//        FpsCounterGraph()
//        WhatsNewBottomSheet()
//        BackupResultDialog()
//        GoogleLockingWarningDialog()
//
//
//        if (screenToUnlock != null && lockMethod == LockMethod.PIN) {
//            PinUnlock(
//                onDismiss = {
//                    lockScreenViewModel.cancelPinUnlock()
//                },
//                onValidate = {
//                    lockScreenViewModel.unlock()
//                    backStack.remove(screenToUnlock!!)
//                    backStack.add(screenToUnlock!!)
//                }
//            )
//        }
//
//        if (screenToUnlock != null && lockMethod == LockMethod.DEVICE_UNLOCK) {
//            LaunchedEffect(screenToUnlock) {
//                val activity = ctx.findFragmentActivity()
//                if (activity != null && securityService.isDeviceUnlockAvailable(ctx)) {
//                    securityService.showDeviceUnlockPrompt(
//                        activity = activity,
//                        onSuccess = {
//                            lockScreenViewModel.unlock()
//                            backStack.navigate(screenToUnlock!!)
//                            lockScreenViewModel.cancelPinUnlock()
//                        },
//                        onError = { msg ->
//                            ctx.showToast(ctx.getString(R.string.authentication_error, msg))
//                            lockScreenViewModel.cancelPinUnlock()
//                        },
//                        onFailed = {
//                            ctx.showToast(ctx.getString(R.string.authentication_failed))
//                            lockScreenViewModel.cancelPinUnlock()
//                        }
//                    )
//                }
//            }
//        }
//    }

//    Text("Hello", color = Color.White)
}


private fun NavBackStack<NavKey>.navigateBack() {
    // Popping the only screen will crash so this avoids it
    if (size == 1) return
    removeLastOrNull()
}
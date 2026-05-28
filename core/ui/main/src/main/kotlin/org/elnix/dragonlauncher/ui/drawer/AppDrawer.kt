package org.elnix.dragonlauncher.ui.drawer

import android.annotation.SuppressLint
import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import org.elnix.dragonlauncher.base.profiles.Profile.Type.Personal
import org.elnix.dragonlauncher.base.profiles.Profile.Type.Private
import org.elnix.dragonlauncher.base.profiles.Profile.Type.Work
import org.elnix.dragonlauncher.common.search.Application
import org.elnix.dragonlauncher.common.messyfolder.Constants
import org.elnix.dragonlauncher.common.messyfolder.openSearch
import org.elnix.dragonlauncher.common.navigaton.NavigationRoute
import org.elnix.dragonlauncher.common.serializables.SwipeAction
import org.elnix.dragonlauncher.common.serializables.WorkspaceType.PRIVATE
import org.elnix.dragonlauncher.common.serializables.WorkspaceType.WORK
import org.elnix.dragonlauncher.enumsui.toggle.DrawerActions
import org.elnix.dragonlauncher.enumsui.toggle.DrawerActions.CLEAR
import org.elnix.dragonlauncher.enumsui.toggle.DrawerActions.CLOSE
import org.elnix.dragonlauncher.enumsui.toggle.DrawerActions.CLOSE_KB
import org.elnix.dragonlauncher.enumsui.toggle.DrawerActions.DISABLED
import org.elnix.dragonlauncher.enumsui.toggle.DrawerActions.NONE
import org.elnix.dragonlauncher.enumsui.toggle.DrawerActions.OPEN_FIRST_APP
import org.elnix.dragonlauncher.enumsui.toggle.DrawerActions.OPEN_KB
import org.elnix.dragonlauncher.enumsui.toggle.DrawerActions.SEARCH_WEB
import org.elnix.dragonlauncher.enumsui.toggle.DrawerActions.TOGGLE_KB
import org.elnix.dragonlauncher.enumsui.toggle.DrawerToolbar
import org.elnix.dragonlauncher.enumsui.toggle.DrawerToolbar.RecentlyUsed
import org.elnix.dragonlauncher.enumsui.toggle.DrawerToolbar.SearchBar
import org.elnix.dragonlauncher.enumsui.toggle.DrawerToolbar.Spacer
import org.elnix.dragonlauncher.enumsui.toggle.isUsed
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.ktx.px
import org.elnix.dragonlauncher.ktx.toDp
import org.elnix.dragonlauncher.models.AppsViewModel
import org.elnix.dragonlauncher.models.ProfilesVM
import org.elnix.dragonlauncher.settings.stores.DrawerSettingsStore
import org.elnix.dragonlauncher.settings.stores.UiSettingsStore
import org.elnix.dragonlauncher.ui.base.activityViewModel
import org.elnix.dragonlauncher.ui.base.asState
import org.elnix.dragonlauncher.ui.base.modifiers.conditional
import org.elnix.dragonlauncher.ui.base.modifiers.settingsGroup
import org.elnix.dragonlauncher.ui.base.modifiers.shapedClickable
import org.elnix.dragonlauncher.ui.components.burger.BurgerListAction
import org.elnix.dragonlauncher.ui.components.burger.MoreOptions
import org.elnix.dragonlauncher.ui.dialogs.AppAliasesDialog
import org.elnix.dragonlauncher.ui.dialogs.AppIconEditor
import org.elnix.dragonlauncher.ui.dialogs.TextEditorDialog
import org.elnix.dragonlauncher.ui.helpers.AppDrawerSearch
import org.elnix.dragonlauncher.ui.helpers.AppGrid
import org.elnix.dragonlauncher.ui.helpers.WallpaperDim
import kotlin.math.abs
import kotlin.math.pow

@SuppressLint("LocalContextGetResourceValueCall")
@Suppress("AssignedValueIsNeverRead")
@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AppDrawerScreen(
    appsViewModel: AppsViewModel = activityViewModel(),
    profilesVM: ProfilesVM = activityViewModel(),
    autoShowKeyboard: Boolean,
    drawerToolbarsOrder: List<DrawerToolbar>,
    leftAction: DrawerActions,
    leftWeight: Float,
    rightAction: DrawerActions,
    rightWeight: Float,
    onRegisterHomeHandler: ((() -> Unit)?) -> Unit,
    onNavigate: (NavigationRoute) -> Unit,
    onLaunchAction: (SwipeAction) -> Unit,
    onClose: () -> Unit
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()


    val workspacesManager = appsViewModel.workspaceManager
    val workspaceState by workspacesManager.workspacesState.collectAsState()
    val visibleWorkspaces = workspaceState.workspaces

    val appOverridesManager = appsViewModel.appOverrideManager
    val appOverrideState by appOverridesManager.appOverrideState.collectAsState()
    val appOverrides = appOverrideState.appOverrides

    val profiles by profilesVM.profiles.collectAsState(emptyList())
    val profileStates by profilesVM.profileStates.collectAsState(emptyList())
    val hasProfilesPermission by profilesVM.hasProfilesPermission.collectAsState(false)


    val selectedWorkspaceId by appsViewModel.selectedWorkspaceId.collectAsState()
    val initialIndex = visibleWorkspaces.indexOfFirst { it.id == selectedWorkspaceId }
    val pagerState = rememberPagerState(
        initialPage = initialIndex.coerceIn(0, (visibleWorkspaces.size - 1).coerceAtLeast(0)),
        pageCount = { visibleWorkspaces.size }
    )

    val autoLaunchSingleMatch by DrawerSettingsStore.autoOpenSingleMatch.asState()
    val disableAutoLaunchOnSpaceFirstChar by DrawerSettingsStore.disableAutoLaunchOnSpaceFirstChar.asState()

    val tapEmptySpaceToRaiseKeyboard by DrawerSettingsStore.tapEmptySpaceAction.asState()
    val drawerEnterAction by DrawerSettingsStore.drawerEnterAction.asState()
    val drawerBackAction by DrawerSettingsStore.backDrawerAction.asState()
    val drawerHomeAction by DrawerSettingsStore.drawerHomeAction.asState()
    val drawerScrollDownAction by DrawerSettingsStore.scrollDownDrawerAction.asState()
    val drawerScrollUpAction by DrawerSettingsStore.scrollUpDrawerAction.asState()


    val showSearchBar by DrawerSettingsStore.showSearchBar.asState()


    val showRecentlyUsedApps by DrawerSettingsStore.showRecentlyUsedApps.asState()
    val recentlyUsedAppsCount by DrawerSettingsStore.recentlyUsedAppsCount.asState()
    val recentApps by appsViewModel.getRecentApps(recentlyUsedAppsCount)
        .collectAsStateWithLifecycle(emptyList())


    var haveToLaunchFirstApp by remember { mutableStateOf(false) }

    var searchQuery by remember { mutableStateOf("") }

    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    var isSearchFocused by remember { mutableStateOf(false) }

    var renameAppTarget by remember { mutableStateOf<Application?>(null) }
    var showAliasDialog by remember { mutableStateOf<Application?>(null) }

    var workspaceId by remember { mutableStateOf<String?>(null) }


    var appTarget by remember { mutableStateOf<Application?>(null) }
    var showMoreMenu by remember { mutableStateOf(false) }



    LaunchedEffect(autoShowKeyboard) {
        if (autoShowKeyboard) {
            yield()
            focusRequester.requestFocus()
        }
    }


    /**
     * Updates the visible workspace
     */
    LaunchedEffect(visibleWorkspaces, selectedWorkspaceId) {
        if (visibleWorkspaces.isEmpty()) return@LaunchedEffect

        val selectedVisible = visibleWorkspaces.any { it.id == selectedWorkspaceId }
        val targetId = if (selectedVisible) selectedWorkspaceId else visibleWorkspaces.first().id
        val targetIndex = visibleWorkspaces.indexOfFirst { it.id == targetId }

        if (!selectedVisible) {
            appsViewModel.selectWorkspace(targetId)
        }

        if (targetIndex >= 0 && pagerState.currentPage != targetIndex) {
            pagerState.scrollToPage(targetIndex)
        }
    }

    // TODO
//    /**
//     * Fires on workspace state change
//     * launch the private space unlocking prompt if workspace type if private space
//     */
//    LaunchedEffect(pagerState.currentPage) {
//        val newWorkspace =
//            visibleWorkspaces.getOrNull(pagerState.currentPage) ?: return@LaunchedEffect
//        val newWorkspaceId = newWorkspace.id
//
//        // Check if switching to Private Space (Android 15+)
//        if (PrivateSpaceUtils.isPrivateSpaceSupported() &&
//            newWorkspace.type == WorkspaceType.PRIVATE &&
//            privateSpaceState.isLocked
//        ) {
//            profilesVM.onUnlockPrivateSpace()
//        }
//
//        workspaceId = newWorkspaceId
//        appsViewModel.selectWorkspace(newWorkspaceId)
//    }


    fun closeKeyboard() {
        focusManager.clearFocus()
        keyboardController?.hide()
    }

    fun openKeyboard() {
        focusRequester.requestFocus()
        keyboardController?.show()
    }

    fun toggleKeyboard() {
        if (isSearchFocused) {
            closeKeyboard()
        } else {
            openKeyboard()
        }
    }


    fun launchDrawerAction(action: DrawerActions) {
        when (action) {
            CLOSE -> onClose()
            TOGGLE_KB -> toggleKeyboard()
            CLOSE_KB -> closeKeyboard()
            OPEN_KB -> openKeyboard()

            CLEAR -> searchQuery = ""
            SEARCH_WEB -> {
                if (searchQuery.isNotBlank()) ctx.openSearch(searchQuery)
            }

            OPEN_FIRST_APP -> haveToLaunchFirstApp = true
            NONE, DISABLED -> {}
        }
    }

    // Used to correctly handle the home action when in drawer (otherwise the action is consumed by the nav host and not made here)
    DisposableEffect(Unit) {

        val handler = {
            launchDrawerAction(drawerHomeAction)
        }

        onRegisterHomeHandler(handler)

        onDispose {
            onRegisterHomeHandler(null)
        }
    }

    BackHandler {
        launchDrawerAction(drawerBackAction)
    }


    val filteredToolbarsOrder by remember(drawerToolbarsOrder, showSearchBar, showRecentlyUsedApps) {
        derivedStateOf {
            drawerToolbarsOrder.filter { item ->
                when (item) {
                    RecentlyUsed -> showRecentlyUsedApps
                    SearchBar -> showSearchBar
                    else -> true
                }
            }
        }
    }

    // Computes the position of the spacer in the toolbars list, and deduce 2 lists:
    // one with the elements that come before, and one with those that come after
    val spacerIndex = remember(filteredToolbarsOrder) {
        filteredToolbarsOrder.indexOf(Spacer).takeIf { it != -1 } ?: 0
    }
    val beforeSpacer = remember(filteredToolbarsOrder, spacerIndex) {
        filteredToolbarsOrder.subList(0, spacerIndex)
    }
    val afterSpacer = remember(filteredToolbarsOrder, spacerIndex) {
        filteredToolbarsOrder.subList(spacerIndex + 1, filteredToolbarsOrder.size)
    }

    var searchBarHeightPx by remember { mutableIntStateOf(0) }
    var recentAppsHeightPx by remember { mutableIntStateOf(0) }


    val density = LocalDensity.current

    val appsContentPadding = remember(filteredToolbarsOrder, searchBarHeightPx, recentAppsHeightPx) {
        PaddingValues(
            top = with(density) {
                beforeSpacer.sumOf {
                    when (it) {
                        Spacer -> 0
                        RecentlyUsed -> recentAppsHeightPx
                        SearchBar -> searchBarHeightPx
                    }
                }.toDp() + 5.dp
            },
            bottom = with(density) {
                afterSpacer.sumOf {
                    when (it) {
                        Spacer -> 0
                        RecentlyUsed -> recentAppsHeightPx
                        SearchBar -> searchBarHeightPx
                    }
                }.toDp() + 5.dp
            }
        )
    }

//    logD(DRAWER_TAG) { "toolbar order: $drawerToolbarsOrder, filtered: $filteredToolbarsOrder SpacerIndex: $spacerIndex, beforeSpacer: $beforeSpacer, after: $afterSpacer\ntopPadding: $topPadding, bottomPadding: $bottomPadding" }

    /* ───────────── Pull Down System ───────────── */

    val pullDownAnimations by DrawerSettingsStore.pullDownAnimations.asState()
    val pullDownScaleIn by DrawerSettingsStore.pullDownScaleIn.asState()
//    val pullDownIconFade by DrawerSettingsStore.pullDownIconFade.asState()

    var atTop by remember { mutableStateOf(true) }

    val haptic = LocalHapticFeedback.current

    val thresholdPx = Constants.Drawer.DRAWER_DRAG_DOWN_THRESHOLD.dp.px
    val maxDragDownOffset = Constants.Drawer.DRAWER_MAX_DRAG_DOWN.dp.px

    var pullOffset by remember { mutableFloatStateOf(0f) }

    /**
     * `Of..1f`, used for animations
     * `1f` is at the threshold
     */
    val pullProgress = 1 - (pullOffset / thresholdPx).coerceAtMost(1f)

    /**
     * If the haptic feedback has already been executed, to avoid repeating it indefinitely
     */
    var hasHapticed by remember { mutableStateOf(false) }

    /**
     * The scroll state basically, defines what happen on vertical scrolls, the horizontal being handled by the pager
     * Responsible for the drag up/down actions, and the top padding of the drawer on down drag
     */
    val nestedConnection = remember {

        object : NestedScrollConnection {

            override fun onPreScroll(
                available: Offset,
                source: NestedScrollSource
            ): Offset {

                if (source != NestedScrollSource.UserInput)
                    return Offset.Zero

                // ignore horizontal gestures
                if (abs(available.y) <= abs(available.x))
                    return Offset.Zero

                // Down Drag (pull-to-trigger)
                if (available.y > 0f && atTop) {

                    // Linear curve for clean output
                    val newPullOffset = pullOffset + available.y * (1f - (pullOffset / thresholdPx))
                        .coerceAtLeast(0.2f)

                    // Block when max offset is reached (constant)
                    pullOffset = newPullOffset.coerceAtMost(maxDragDownOffset)

                    val thresholdReachedNow = pullOffset > thresholdPx

                    // Haptic feedback
                    if (thresholdReachedNow && !hasHapticed) {
                        hasHapticed = true
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                    if (!thresholdReachedNow && hasHapticed) hasHapticed = false

                    // consume only what we used
                    return Offset(0f, available.y)
                }

                // UP DRAG while stretching (reversible)
                if (available.y < 0f && pullOffset > 0f) {

                    pullOffset = (pullOffset + available.y).coerceAtLeast(0f)


                    if (!(pullOffset > thresholdPx) && hasHapticed) hasHapticed = false
                    return Offset(0f, available.y)
                }


                // Launch Up action on any up scroll large enough
                if (available.y < -15) {
                    launchDrawerAction(drawerScrollUpAction)
                }

                return Offset.Zero
            }

            override suspend fun onPostFling(
                consumed: Velocity,
                available: Velocity
            ): Velocity {

                // No need to enclave in if statement as values aren't changing if !pullDownAnimations

                // DOWN action
                if (pullOffset > thresholdPx) {
                    launchDrawerAction(drawerScrollDownAction)
                }

                // reset
                pullOffset = 0f
                hasHapticed = false

                return Velocity.Zero
            }
        }
    }


    val animatedScale by animateFloatAsState(
        targetValue = if (pullDownScaleIn) (pullProgress.pow(0.9f)).coerceIn(0.95f, 1f)
        else 1f
    )


    val pullDownPadding = if (pullDownAnimations) pullOffset else 0f
    val animatedPadding by animateDpAsState(targetValue = pullDownPadding.toDp)

    @Composable
    fun AppLongPressRow(app: Application) {
        val cacheKey = app.key

        AppLongPressRow(
            app = app,
            onOpen = { onLaunchAction(app.action) },
            onSettings = if (!app.isPrivate && !app.isWork) {
                {
                    ctx.startActivity(
                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = "package:${app.packageName}".toUri()
                        }
                    )
                    onClose()
                }
            } else null,
            onUninstall = if (!app.isPrivate && !app.isWork) {
                {
                    ctx.startActivity(
                        Intent(Intent.ACTION_DELETE).apply {
                            data = "package:${app.packageName}".toUri()
                        }
                    )
                    onClose()
                }
            } else null,
            onRemoveFromWorkspace = if (!app.isPrivate) {
                {
                    workspaceId?.let { wsId ->
                        scope.launch {
                            workspacesManager.removeAppFromWorkspace(
                                id = wsId,
                                cacheKey = cacheKey
                            )
                        }
                    }
                }
            } else null,
            onRenameApp = { renameAppTarget = app },
            onChangeAppIcon = { appTarget = app },
            onAliases = { showAliasDialog = app }
        )
    }


    /* ───────────── Dim wallpaper system ───────────── */
    val wallpaperDimDrawerScreen by UiSettingsStore.wallpaperDimDrawerScreen.asState()
    val pullDownWallPaperDimFadeEnabled by DrawerSettingsStore.pullDownWallPaperDimFade.asState()

    val animatedDim by animateFloatAsState(targetValue = pullProgress)
    // Dims the wallpaper, when the user starts pulling down,
    // the dim amount is reduced proportionally to the drag amount
    val dimAmount = wallpaperDimDrawerScreen *
            if (pullDownWallPaperDimFadeEnabled) animatedDim
            else 1f

    WallpaperDim(dimAmount)


    /* ───────────── Main Content ───────────── */
    Box(
        modifier = Modifier
            .windowInsetsPadding(WindowInsets.safeDrawing.exclude(WindowInsets.ime))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(nestedConnection)
                .padding(top = animatedPadding)
                .conditional(pullDownScaleIn) {
                    graphicsLayer {
                        scaleX = animatedScale
                        scaleY = animatedScale
                    }
                }
                .clickable(
                    enabled = tapEmptySpaceToRaiseKeyboard.isUsed(),
                    indication = null,
                    interactionSource = null
                ) {
                    toggleKeyboard()
                }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f)
            ) {

                if (leftAction != DISABLED) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(leftWeight.coerceIn(0.001f, 1f))
                            .clickable(
                                indication = null,
                                interactionSource = null
                            ) { launchDrawerAction(leftAction) }
                    )
                }

                Column(modifier = Modifier.weight(1f)) {

                    HorizontalPager(
                        state = pagerState,
                        key = { it.hashCode() }
                    ) { pageIndex ->

                        val workspace = visibleWorkspaces[pageIndex]

                        val workspaceProfileType = when (workspace.type) {
                            WORK -> Work
                            PRIVATE -> Private
                            else -> Personal
                        }

                        val workspaceProfile = when (workspaceProfileType) {
                            Personal -> profiles[0]
                            Work -> profiles[1]
                            Private -> profiles[2]
                        }

                        val workspaceLocked = when (workspaceProfileType) {
                            Work -> profileStates[1]!!.locked
                            Private -> profileStates[2]!!.locked
                            Personal -> false
                        }

                        val gridState = remember(workspace.id) {
                            LazyGridState()
                        }

                        val categoryGridState = remember(workspace.id) {
                            LazyGridState()
                        }

                        val listState = remember(workspace.id) {
                            LazyListState()
                        }

                        val apps by appsViewModel
                            .appsForWorkspace(workspace)
                            .collectAsStateWithLifecycle(emptyList())

                        val filteredApps by remember(searchQuery, apps) {
                            derivedStateOf {
                                val trimmedSearchQuery = searchQuery.trim()

                                val base = if (trimmedSearchQuery.isBlank()) apps
                                else apps.filter { app ->
                                    app.label.contains(trimmedSearchQuery, ignoreCase = true) ||

                                            // Also search for aliases
                                            appOverrides[app.key]?.aliases?.any {
                                                it.contains(
                                                    trimmedSearchQuery,
                                                    ignoreCase = true
                                                )
                                            } ?: false
                                }

                                base.sortedBy { it.label.lowercase() }
                            }
                        }

                        LaunchedEffect(haveToLaunchFirstApp, filteredApps) {

                            val autoLaunch =
                                autoLaunchSingleMatch &&
                                        filteredApps.size == 1 &&
                                        searchQuery.isNotEmpty() &&
                                        !(disableAutoLaunchOnSpaceFirstChar && searchQuery.first() == ' ')

                            if (haveToLaunchFirstApp || autoLaunch && filteredApps.isNotEmpty()) {
                                onLaunchAction(filteredApps.first().action)
                            }
                        }


                        if (workspaceProfileType != Personal) {
                            if (workspaceLocked) {
                                Column(
                                    modifier = Modifier
                                        .padding(12.dp)
                                        .fillMaxWidth()
                                        .border(
                                            1.dp,
                                            MaterialTheme.colorScheme.outlineVariant,
                                            MaterialTheme.shapes.small
                                        )
                                        .background(
                                            MaterialTheme.colorScheme.surfaceContainer,
                                            MaterialTheme.shapes.small
                                        )
                                        .padding(vertical = 64.dp),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                ) {
                                    Icon(
                                        painterResource(if (workspaceProfileType == Work) R.drawable.enterprise_off else R.drawable.lock),
                                        contentDescription = null,
                                        modifier = Modifier.size(48.dp),
                                        tint = MaterialTheme.colorScheme.secondary,
                                    )
                                    Text(
                                        stringResource(
                                            if (workspaceProfileType == Work) R.string.profile_work_profile_state_locked
                                            else R.string.profile_private_profile_state_locked
                                        ),
                                        modifier = Modifier.padding(top = 8.dp),
                                        color = MaterialTheme.colorScheme.secondary,
                                        style = MaterialTheme.typography.titleSmall,
                                    )
                                    if (hasProfilesPermission) {
                                        Button(
                                            modifier = Modifier.padding(top = 32.dp),
                                            onClick = {
                                                appsViewModel.setProfileLock(workspaceProfile, false)
                                            },
                                            contentPadding = ButtonDefaults.TextButtonWithIconContentPadding,
                                        ) {
                                            Icon(
                                                painterResource(if (workspaceProfileType == Work) R.drawable.enterprise else R.drawable.lock_open),
                                                contentDescription = null,
                                                modifier = Modifier
                                                    .padding(end = ButtonDefaults.IconSpacing)
                                                    .size(ButtonDefaults.IconSize)
                                            )
                                            Text(
                                                stringResource(
                                                    if (workspaceProfileType == Work) R.string.profile_work_profile_action_unlock
                                                    else R.string.profile_private_profile_action_unlock
                                                )
                                            )
                                        }
                                    }
                                }
                            } else if (hasProfilesPermission) {
                                FilledTonalButton(
                                    modifier = Modifier
                                        .padding(12.dp)
                                        .fillMaxWidth(),
                                    onClick = {
                                        appsViewModel.setProfileLock(workspaceProfile, true)
                                    },
                                    contentPadding = ButtonDefaults.TextButtonWithIconContentPadding,
                                ) {
                                    Icon(
                                        painterResource(if (workspaceProfileType == Work) R.drawable.enterprise_off else R.drawable.lock),
                                        contentDescription = null,
                                        modifier = Modifier
                                            .padding(end = ButtonDefaults.IconSpacing)
                                            .size(ButtonDefaults.IconSize)
                                    )
                                    Text(
                                        stringResource(
                                            if (workspaceProfileType == Work) R.string.profile_work_profile_action_lock
                                            else R.string.profile_private_profile_action_lock
                                        )
                                    )
                                }
                            }
                        } else {

                            AppGrid(
                                apps = filteredApps,
                                gridState = gridState,
                                paddingValues = appsContentPadding,
                                categoryGridState = categoryGridState,
                                listState = listState,
                                onTopStateChange = { atTop = it },
//                                onReload = {
//                                    scope.launch {
//                                        if (workspace.type == WorkspaceType.PRIVATE) appsViewModel.unlockAndReloadPrivateSpace()
//                                        else appsViewModel.reloadApps()
//                                    }
//                                },
                                longPressPopup = { app -> AppLongPressRow(app) }
                            ) {
                                onLaunchAction(it.action)
                            }
                        }
                    }
                }

                if (rightAction != DISABLED) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(rightWeight.coerceIn(0.001f, 1f))
                            .clickable(
                                indication = null,
                                interactionSource = null
                            ) { launchDrawerAction(rightAction) }
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
        ) {

            drawerToolbarsOrder.forEach { toolbar ->
                when (toolbar) {
                    Spacer -> Spacer(Modifier.weight(1f))

                    RecentlyUsed -> {
                        /* ───────────── Recently Used Apps section ───────────── */
                        AnimatedVisibility(
                            visible = showRecentlyUsedApps && searchQuery.isBlank() && recentApps.isNotEmpty(),
                            modifier = Modifier.onGloballyPositioned {
                                recentAppsHeightPx = it.size.height
                            }
                        ) {

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(5.dp)
                                    .settingsGroup()
                            ) {
                                AppGrid(
                                    apps = recentApps,
                                    fillMaxSize = false,
                                    longPressPopup = { app -> AppLongPressRow(app) }
                                ) {
                                    onLaunchAction(it.action)
                                }
                            }
                        }
                    }

                    SearchBar -> {
                        AnimatedVisibility(
                            visible = showSearchBar,
                            modifier = Modifier.onGloballyPositioned {
                                searchBarHeightPx = it.size.height
                            }
                        ) {
                            AppDrawerSearch(
                                searchQuery = searchQuery,
                                trailingIcon = {
                                    Box {
                                        Icon(
                                            painter = painterResource(R.drawable.more_vert),
                                            contentDescription = stringResource(R.string.more),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.shapedClickable { showMoreMenu = true }
                                        )

                                        BurgerListAction(
                                            actions = listOf(
                                                MoreOptions(
                                                    onClick = { onNavigate(NavigationRoute.DrawerSettings) },
                                                    icon = R.drawable.ic_action_drawer,
                                                    text = { stringResource(R.string.drawer_settings) }
                                                )
                                            ),
                                            isExpanded = showMoreMenu,
                                            onDismissRequest = { showMoreMenu = false }
                                        )
                                    }
                                },
                                onSearchChanged = { searchQuery = it },
                                modifier = Modifier.focusRequester(focusRequester),
                                onEnterPressed = { launchDrawerAction(drawerEnterAction) },
                                onFocusStateChanged = { isSearchFocused = it }
                            )
                        }
                    }
                }
            }
        }
    }



    if (renameAppTarget != null) {
        val app = renameAppTarget!!
        val cacheKey = app.key

        TextEditorDialog(
            title = { stringResource(R.string.rename) },
            placeHolder = { app.label },
            onDismiss = { renameAppTarget = null },
            initialText = app.label
        ) {
            if (it != "") {
                appOverridesManager.renameApp(
                    cacheKey = cacheKey,
                    customName = it
                )
            } else {
                appOverridesManager.renameApp(cacheKey, null)
            }
            renameAppTarget = null
        }
    }

    if (appTarget != null) {

        val app = appTarget!!
        val cacheKey = app.key

        val iconService = appsViewModel.iconsService

        AppIconEditor(
            app = app,
            onReset = {
                // Reload
                @Suppress("UnusedFlow")
                iconService.getAppIcon(app, true)
            },
            onDismiss = { appTarget = null }
        ) { customIcon ->

            scope.launch {
                if (customIcon != null) {
                    appOverridesManager.setAppIcon(
                        cacheKey = cacheKey,
                        customIcon = customIcon
                    )
                } else {
                    appOverridesManager.setAppIcon(cacheKey, null)
                }

                // Reload
                @Suppress("UnusedFlow")
                iconService.getAppIcon(app, true)
                appTarget = null
            }
        }
    }

    if (showAliasDialog != null) {
        val app = showAliasDialog!!

        AppAliasesDialog(
            app = app,
            onDismiss = { showAliasDialog = null }
        )
    }
}

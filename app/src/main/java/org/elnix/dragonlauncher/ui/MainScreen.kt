package org.elnix.dragonlauncher.ui

import android.annotation.SuppressLint
import android.graphics.Bitmap
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.changedToDown
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.R
import org.elnix.dragonlauncher.data.dummySwipePoint
import org.elnix.dragonlauncher.data.helpers.FloatingAppObject
import org.elnix.dragonlauncher.data.helpers.SwipePointSerializable
import org.elnix.dragonlauncher.data.stores.BehaviorSettingsStore
import org.elnix.dragonlauncher.data.stores.DebugSettingsStore
import org.elnix.dragonlauncher.data.stores.PrivateSettingsStore
import org.elnix.dragonlauncher.data.stores.StatusBarSettingsStore
import org.elnix.dragonlauncher.data.stores.SwipeSettingsStore
import org.elnix.dragonlauncher.data.stores.UiSettingsStore
import org.elnix.dragonlauncher.ui.components.FloatingAppsHostView
import org.elnix.dragonlauncher.ui.components.dialogs.FilePickerDialog
import org.elnix.dragonlauncher.ui.components.dialogs.UserValidation
import org.elnix.dragonlauncher.ui.helpers.HoldToActivateArc
import org.elnix.dragonlauncher.ui.helpers.rememberHoldToOpenSettings
import org.elnix.dragonlauncher.ui.statusbar.StatusBar
import org.elnix.dragonlauncher.utils.TAG
import org.elnix.dragonlauncher.utils.WIDGET_TAG
import org.elnix.dragonlauncher.utils.actions.AppLaunchException
import org.elnix.dragonlauncher.utils.actions.launchSwipeAction
import org.elnix.dragonlauncher.utils.circles.rememberNestNavigation
import org.elnix.dragonlauncher.utils.logs.logE
import org.elnix.dragonlauncher.utils.models.AppsViewModel
import org.elnix.dragonlauncher.utils.models.FloatingAppsViewModel

@SuppressLint("LocalContextResourcesRead")
@Suppress("AssignedValueIsNeverRead")
@Composable
fun MainScreen(
    appsViewModel: AppsViewModel,
    floatingAppsViewModel: FloatingAppsViewModel,
    wallpaper: Bitmap?,
    useWallpaper: Boolean,
    onAppDrawer: () -> Unit,
    onGoWelcome: () -> Unit,
    onLongPress3Sec: () -> Unit
) {
    val ctx = LocalContext.current
//    val extraColors = LocalExtraColors.current
    val scope = rememberCoroutineScope()

    var showFilePicker: SwipePointSerializable? by remember { mutableStateOf(null) }
    var showMethodDialog by remember { mutableStateOf(false) }
    var lastClickTime by remember { mutableLongStateOf(0L) }

    val floatingAppObjects by floatingAppsViewModel.floatingApps.collectAsState()

    LaunchedEffect(floatingAppObjects) {
        logE(WIDGET_TAG, floatingAppObjects.toString())
    }

//    val showMethodAsking by PrivateSettingsStore.getShowMethodAsking(ctx)
//        .collectAsState(initial = false)
    // Removed hacky popup on notifications, use debug on my phone to open quick settings
    val showMethodAsking = false

    val doubleClickAction by BehaviorSettingsStore.getDoubleClickAction(ctx)
        .collectAsState(initial = null)

    val backAction by BehaviorSettingsStore.getBackAction(ctx)
        .collectAsState(initial = null)

    val leftPadding by BehaviorSettingsStore.getLeftPadding(ctx)
        .collectAsState(initial = 0)

    val rightPadding by BehaviorSettingsStore.getRightPadding(ctx)
        .collectAsState(initial = 0)

    val upPadding by BehaviorSettingsStore.getUpPadding(ctx)
        .collectAsState(initial = 0)

    val downPadding by BehaviorSettingsStore.getDownPadding(ctx)
        .collectAsState(initial = 0)


    val icons by appsViewModel.icons.collectAsState()
    val pointIcons by appsViewModel.pointIcons.collectAsState()

    var start by remember { mutableStateOf<Offset?>(null) }
    var current by remember { mutableStateOf<Offset?>(null) }
    var isDragging by remember { mutableStateOf(false) }
    var size by remember { mutableStateOf(IntSize.Zero) }

    val hold = rememberHoldToOpenSettings(
        onSettings = onLongPress3Sec
    )

    val defaultColor = Color.Red
    val rgbLoading by UiSettingsStore.getRGBLoading(ctx)
        .collectAsState(initial = true)

    val hasSeenWelcome by PrivateSettingsStore.getHasSeenWelcome(ctx)
        .collectAsState(initial = true)

    val useAccessibilityInsteadOfContextToExpandActionPanel by DebugSettingsStore
        .getUseAccessibilityInsteadOfContextToExpandActionPanel(ctx)
        .collectAsState(initial = true)


    /* ───────────── status bar things ───────────── */

    val showStatusBar by StatusBarSettingsStore.getShowStatusBar(ctx)
        .collectAsState(initial = false)

    val systemInsets = WindowInsets.systemBars.asPaddingValues()

    val isRealFullscreen = systemInsets.calculateTopPadding() == 0.dp

    /* ────────────────────────────────────────────── */



    LaunchedEffect(hasSeenWelcome) {
        if (!hasSeenWelcome) onGoWelcome()
    }

    LaunchedEffect(Unit) { lastClickTime = 0 }

    val points by SwipeSettingsStore.getPointsFlow(ctx).collectAsState(emptyList())
    val nests by SwipeSettingsStore.getNestsFlow(ctx).collectAsState(emptyList())

    val nestNavigation = rememberNestNavigation(nests)
    val nestId = nestNavigation.nestId


    val dm = ctx.resources.displayMetrics
    val density = LocalDensity.current
    val cellSizePx = floatingAppsViewModel.cellSizePx

    LaunchedEffect(points, nestId) {
        appsViewModel.preloadPointIcons(
            points = points.filter { it.nestId == nestId }
        )
    }



    fun launchAction(point: SwipePointSerializable?) {
        isDragging = false
        nestNavigation.goToNest(0)
        start = null
        current = null
        lastClickTime = 0

        try {
            launchSwipeAction(
                ctx = ctx,
                action = point?.action,
                useAccessibilityInsteadOfContextToExpandActionPanel = useAccessibilityInsteadOfContextToExpandActionPanel,
                onAskWhatMethodToUseToOpenQuickActions = { showMethodDialog = true },
                onReloadApps = { scope.launch { appsViewModel.reloadApps() } },
                onReselectFile = { showFilePicker = point },
                onAppSettings = onLongPress3Sec,
                onAppDrawer = onAppDrawer,
                onOpenNestCircle = { nestNavigation.goToNest(it) },
                onParentNest = { nestNavigation.goBack() }
            )
        } catch (e: AppLaunchException) {
            ctx.logE(TAG, e.message!!) // Lol if it crashes when logging for an exception
        } catch (e: Exception) {
            ctx.logE(TAG, e.message?:"")
        }
    }

    /**
     * 1. Tests if the current nest is the main, if not, go back one nest
     * 2. Activate the back actions
     */
    BackHandler {
        if (nestId != 0) {
            nestNavigation.goBack()
        } else if (backAction != null) {
            launchAction(
                dummySwipePoint(backAction)
            )
        }
    }


    // I should store a plain wallpaper color, and print it for performances and optimisations issues but nobody's using a plain color as wallpaper anyways
    if (useWallpaper) {
        wallpaper?.let { bmp ->
            Image(
                bitmap = bmp.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .onSizeChanged { size = it },
                contentScale = ContentScale.Crop
            )
        }
    } else {
        // Moved the background drawing here for more clarity
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .onSizeChanged { size = it }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent(PointerEventPass.Initial)

                        val down = event.changes.firstOrNull { it.changedToDown() } ?: continue
                        val pos = down.position

                        val allowed = isInsideActiveZone(
                            pos = pos,
                            size = size,
                            left = leftPadding,
                            right = rightPadding,
                            top = upPadding,
                            bottom = downPadding
                        )

                        if (!allowed) {
                            continue
                        }

                        if (isInsideForegroundWidget(
                                pos = pos,
                                floatingAppObjects = floatingAppObjects, // 👈 Pass this
                                dm = dm,
                                density = density, // 👈 Pass this
                                cellSizePx = cellSizePx
                            )
                        ) {
                            // Let widget handle scroll - do NOT consume or process
                            continue
                        }

                        start = down.position
                        current = down.position
                        isDragging = true

                        val pointerId = down.id

                        val currentTime = System.currentTimeMillis()
                        val diff = currentTime - lastClickTime
                        if (diff < 500) {
                            doubleClickAction?.let { action ->
                                launchAction(
                                    dummySwipePoint(action)
                                )
                                isDragging = false
                                continue
                            }
                        }
                        lastClickTime = currentTime

                        while (true) {
                            val event = awaitPointerEvent(PointerEventPass.Initial)
                            val change = event.changes.firstOrNull { it.id == pointerId }

                            if (change != null) {
                                if (change.pressed) {
                                    change.consume()
                                    current = change.position
                                } else {
                                    isDragging = false
                                    start = null
                                    current = null
                                    break
                                }
                            } else {
                                isDragging = false
                                start = null
                                current = null
                                break
                            }
                        }
                    }
                }
            }
            .onSizeChanged { size = it }
            .then(hold.pointerModifier)
    ) {

        floatingAppObjects.forEach { floatingAppObject ->
            FloatingAppsHostView(
                floatingAppObject = floatingAppObject,
                icons = icons,
                cellSizePx = cellSizePx,
                modifier = Modifier
                    .offset {
                        IntOffset(
                            x = (floatingAppObject.x * dm.widthPixels).toInt(),
                            y = (floatingAppObject.y * dm.heightPixels).toInt()
                        )
                    }
                    .size(
                        width = with(density) { (floatingAppObject.spanX * cellSizePx).toDp() },
                        height = with(density) { (floatingAppObject.spanY * cellSizePx).toDp() }
                    ),
                onLaunchAction = {
                    launchAction(
                        dummySwipePoint(
                            action = floatingAppObject.action
                        )
                    )
                },
                blockTouches = floatingAppObject.ghosted == true
            )
        }

        if (showStatusBar && isRealFullscreen) {
            StatusBar(
                onClockAction = { launchAction(dummySwipePoint(it)) },
                onDateAction = { launchAction( dummySwipePoint(it)) }
            )
        }

        MainScreenOverlay(
            start = start,
            current = current,
            nestId = nestId,
            isDragging = isDragging,
            surface = size,
            points = points,
            pointIcons = pointIcons,
            nests = nests,
            onLaunch = { launchAction(it) }
        )

        HoldToActivateArc(
            center = hold.centerProvider(),
            progress = hold.progressProvider(),
            defaultColor = defaultColor,
            rgbLoading = rgbLoading
        )
    }

    if (showFilePicker != null) {
        val currentPoint = showFilePicker!!

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


    if (showMethodDialog and showMethodAsking) {
        UserValidation(
            title = stringResource(R.string.what_method_to_open_quick_actions),
            message = stringResource(R.string.did_the_notif_or_quick_actions),
            validateText = stringResource(R.string.quick_actions),
            cancelText = stringResource(R.string.notifications),
            canDismissByOuterClick = false,
            doNotRemindMeAgain = {
                scope.launch {
                    PrivateSettingsStore.setShowMethodAsking(ctx, false)
                }
            },
            onCancel = {
                // The simple ctx method didn't work, so forced to use the accessibility method, that doesn't work well on my phone
                scope.launch {
                    DebugSettingsStore.setUseAccessibilityInsteadOfContextToExpandActionPanel(ctx, false)
                }
                showMethodDialog = false
            },
            onAgree = {
                // The simple ctx method worked, keep it
                scope.launch {
                    DebugSettingsStore.setUseAccessibilityInsteadOfContextToExpandActionPanel(ctx, true)
                }
                showMethodDialog = false
            }
        )
    }
}


/**
 * Determines whether a pointer position lies within the allowed interaction zone.
 *
 * The active zone is defined as the rectangular area of the screen obtained by
 * excluding padding margins from each edge. Any position inside this rectangle
 * is considered valid for gesture handling.
 *
 * @param pos Pointer position in screen coordinates.
 * @param size Full size of the available surface.
 * @param left Excluded distance from the left edge.
 * @param right Excluded distance from the right edge.
 * @param top Excluded distance from the top edge.
 * @param bottom Excluded distance from the bottom edge.
 *
 * @return `true` if the position is inside the active zone, `false` otherwise.
 */
private fun isInsideActiveZone(
    pos: Offset,
    size: IntSize,
    left: Int,
    right: Int,
    top: Int,
    bottom: Int
): Boolean {
    return pos.x >= left &&
            pos.x <= size.width - right &&
            pos.y >= top &&
            pos.y <= size.height - bottom
}



/**
 * Checks if pointer position is inside any foreground widget bounds.
 */
private fun isInsideForegroundWidget(
    pos: Offset,
    floatingAppObjects: List<FloatingAppObject>, // 👈 You'll need to pass this
    dm: android.util.DisplayMetrics,
    density: androidx.compose.ui.unit.Density,
    cellSizePx: Float
): Boolean {
    return floatingAppObjects.any { widget ->
        // Skip if not foreground
        if (widget.foreground == false) return@any false

        val left = (widget.x * dm.widthPixels).toInt()
        val top = (widget.y * dm.heightPixels).toInt()
        val right = left + with(density) { (widget.spanX * cellSizePx).toDp() }.value.times(density.density).toInt()
        val bottom = top + with(density) { (widget.spanY * cellSizePx).toDp() }.value.times(density.density).toInt()

        pos.x >= left && pos.x <= right &&
                pos.y >= top && pos.y <= bottom
    }
}

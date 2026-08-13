package org.elnix.dragonlauncher.ui

import android.annotation.SuppressLint
import android.util.DisplayMetrics
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.changedToDown
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import io.github.elnix90.runtime.asState
import io.github.elnix90.runtime.asStateNull
import kotlinx.coroutines.delay
import org.elnix.dragonlauncher.base.model.serializables.Action
import org.elnix.dragonlauncher.base.model.serializables.MainScreenLayer
import org.elnix.dragonlauncher.base.model.serializables.MainScreenLayer.Companion.enabled
import org.elnix.dragonlauncher.base.model.serializables.Point
import org.elnix.dragonlauncher.base.model.serializables.Point.Companion.dummySwipePoint
import org.elnix.dragonlauncher.base.model.serializables.Widget
import org.elnix.dragonlauncher.base.navigaton.NavigationRoute
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.ktx.isInsideActiveZone
import org.elnix.dragonlauncher.ktx.toDp
import org.elnix.dragonlauncher.models.PointsViewModel
import org.elnix.dragonlauncher.models.WidgetsViewModel
import org.elnix.dragonlauncher.settings.stores.map.BehaviorSettingsStore
import org.elnix.dragonlauncher.settings.stores.map.HoldToActivateArcSettingsStore
import org.elnix.dragonlauncher.settings.stores.map.UiSettingsStore
import org.elnix.dragonlauncher.ui.base.activityViewModel
import org.elnix.dragonlauncher.ui.base.asState
import org.elnix.dragonlauncher.ui.components.WidgetHostView
import org.elnix.dragonlauncher.ui.components.burger.BurgerListAction
import org.elnix.dragonlauncher.ui.components.burger.MoreOptions
import org.elnix.dragonlauncher.ui.composition.LocalHoldCustomObject
import org.elnix.dragonlauncher.ui.composition.LocalMainScreenLayers
import org.elnix.dragonlauncher.ui.compositionslocals.LocalNavigator
import org.elnix.dragonlauncher.ui.dialogs.rememberHoldMenuEntries
import org.elnix.dragonlauncher.ui.helpers.ChargingAnimation
import org.elnix.dragonlauncher.ui.helpers.HoldToActivateArc
import org.elnix.dragonlauncher.ui.helpers.wallpaper.CustomDim
import org.elnix.dragonlauncher.ui.helpers.wallpaper.WallpaperDim
import org.elnix.dragonlauncher.ui.remembers.rememberHoldToOpenSettings
import org.elnix.dragonlauncher.ui.statusbar.StatusBar
import kotlin.time.Duration.Companion.milliseconds


@SuppressLint("LocalContextResourcesRead")
@Composable
fun MainScreen(
    onLaunchAction: (Point) -> Unit,
    widgetsViewModel: WidgetsViewModel = activityViewModel(),
    pointsViewModel: PointsViewModel = activityViewModel()
) {
    val ctx = LocalContext.current
    val navigator = LocalNavigator.current
    val holdCustomObject = LocalHoldCustomObject.current
    val mainScreenLayers = LocalMainScreenLayers.current

    var lastClickTime by remember { mutableLongStateOf(0L) }

    val widgetsObjects by widgetsViewModel.widgets.asState()

    val doubleClickAction by BehaviorSettingsStore.doubleClickAction.asStateNull()
    val backAction by BehaviorSettingsStore.backAction.asStateNull()
    val leftPadding by BehaviorSettingsStore.leftPadding.asState()
    val rightPadding by BehaviorSettingsStore.rightPadding.asState()
    val topPadding by BehaviorSettingsStore.topPadding.asState()
    val bottomPadding by BehaviorSettingsStore.bottomPadding.asState()


    val holdDelayBeforeStartingLongClickSettings by HoldToActivateArcSettingsStore.holdDelayBeforeStartingLongClickSettings.asState()
    val longCLickSettingsDuration by HoldToActivateArcSettingsStore.longCLickSettingsDuration.asState()


    var start by remember { mutableStateOf<Offset?>(null) }
    var current by remember { mutableStateOf<Offset?>(null) }
    var size by remember { mutableStateOf(IntSize.Zero) }

    var showCustomDim by remember { mutableStateOf(false) }

    var holdOffset by remember { mutableStateOf<Offset?>(null) }
    var showDropDownMenuSettings by remember { mutableStateOf(false) }



    LaunchedEffect(Unit) { lastClickTime = 0 }

    val nestNavigation = pointsViewModel.nestsNavigationService
    val nestId by nestNavigation.currentNestId.collectAsState()

    val filteredWidgetObjects by remember(widgetsObjects, nestId) {
        derivedStateOf {
            widgetsObjects.filter { it.nestId == nestId }
        }
    }


    val dm = ctx.resources.displayMetrics
    val density = LocalDensity.current
    val cellSizePx by widgetsViewModel.cellSizePx.collectAsState()


    fun launchAction(point: Point) {
        start = null
        current = null
        lastClickTime = 0


        // Handle nest related actions here, and let the rest pass through
        when (val action = point.action) {
            Action.GoParentNest -> nestNavigation.goBack()
            is Action.OpenCircleNest -> nestNavigation.goToNest(action.nestId)
            else -> {
                nestNavigation.clearStack()
                onLaunchAction(point)
            }
        }
    }


    val holdMenuEntries by rememberHoldMenuEntries()

    val hold = rememberHoldToOpenSettings(
        onSettings = { offset ->

            // When the list only has 1 element, directly go to that screen, otherwise, open the menu
            // If the list is empty, do nothing
            when {
                holdMenuEntries.size > 1 -> {
                    showDropDownMenuSettings = true
                    holdOffset = offset
                }

                holdMenuEntries.size == 1 -> {
                    val routeToGo = holdMenuEntries.first()
                    navigator.navigate(routeToGo)
                }

                else -> {
                    // If list is empty, directly navigate to settings root. Never block the user out of settings
                    navigator.navigate(NavigationRoute.PointsSettings(nestId))
                }
            }

            start = null
            current = null
        },
        holdDelay = holdDelayBeforeStartingLongClickSettings.toLong(),
        loadDuration = longCLickSettingsDuration.toLong()
    )


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

    val mainDimAmount by UiSettingsStore.wallpaperDimMainScreen.asState()
    WallpaperDim(mainDimAmount)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .pointerInput(Unit, nestId) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent(PointerEventPass.Initial)

                        val down = event.changes.firstOrNull { it.changedToDown() } ?: continue
                        val pos = down.position

                        val allowed = pos.isInsideActiveZone(
                            size = size,
                            left = leftPadding,
                            right = rightPadding,
                            top = topPadding,
                            bottom = bottomPadding
                        )

                        if (!allowed) {
                            continue
                        }

                        if (pos.isInsideForegroundWidget(
                                widgets = filteredWidgetObjects,
                                dm = dm,
                                cellSizePx = cellSizePx
                            )
                        ) {
                            // Let widget handle scroll - do NOT consume or process
                            continue
                        }

                        start = down.position
                        current = down.position

                        val pointerId = down.id

                        val currentTime = System.currentTimeMillis()
                        val diff = currentTime - lastClickTime
                        if (diff < 500) {
                            doubleClickAction?.let { action ->
                                launchAction(
                                    dummySwipePoint(action)
                                )
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
                                    start = null
                                    current = null
                                    break
                                }
                            } else {
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

        mainScreenLayers.forEach { layer ->
            if (layer.enabled) {
                when (layer) {
                    is MainScreenLayer.ChargingAnimation -> {
                        ChargingAnimation(modifier = Modifier.fillMaxSize())
                    }

                    is MainScreenLayer.CustomDim -> {

                        LaunchedEffect(start) {
                            if (start != null) {
                                delay(layer.showAfterMs.milliseconds)
                                showCustomDim = true
                            } else {
                                showCustomDim = false
                            }
                        }

                        if (showCustomDim) {
                            CustomDim(layer)
                        }
                    }

                    is MainScreenLayer.DragOverlay -> {
                        MainScreenOverlay(
                            start = start,
                            current = current,
                            currentNestId = nestId,
                            onLaunch = { launchAction(it) }
                        )
                    }

                    is MainScreenLayer.HoldToActivate -> {
                        // Hold to activate
                        HoldToActivateArc(
                            center = hold.center,
                            progress = hold.progress,
                            customObject = holdCustomObject,
                        )

                        if (holdOffset != null) {

                            val actions = holdMenuEntries.map {
                                MoreOptions(
                                    onClick = {
                                        showDropDownMenuSettings = false
                                        navigator.navigate(it)
                                    },
                                    icon = R.drawable.settings,
                                    text = { stringResource(it.resId) }
                                )
                            }

                            val dpOffset = with(density) {
                                DpOffset(
                                    x = holdOffset!!.x.toDp(),
                                    y = holdOffset!!.y.toDp()
                                )
                            }

                            Box(
                                modifier = Modifier.offset(dpOffset.x, dpOffset.y)
                            ) {
                                BurgerListAction(
                                    actions = actions,
                                    isExpanded = showDropDownMenuSettings,
                                    onDismissRequest = {
                                        showDropDownMenuSettings = false
                                        holdOffset = null
                                    }
                                )
                            }
                        }
                    }

                    is MainScreenLayer.StatusBar -> {
                        StatusBar(
                            launchAction = { launchAction(dummySwipePoint(it)) },
                        )
                    }

                    is MainScreenLayer.Widgets -> {
                        filteredWidgetObjects.forEach { widgetObject ->
                            key(widgetObject.id, nestId) {
                                WidgetHostView(
                                    widget = widgetObject,
                                    cellSizePx = cellSizePx,
                                    modifier = Modifier
                                        .offset {
                                            IntOffset(
                                                x = (widgetObject.x * dm.widthPixels).toInt(),
                                                y = (widgetObject.y * dm.heightPixels).toInt()
                                            )
                                        }
                                        .size(
                                            width = (widgetObject.spanX * cellSizePx).toDp,
                                            height = (widgetObject.spanY * cellSizePx).toDp
                                        )
                                        .graphicsLayer {
                                            rotationZ = widgetObject.angle
                                            transformOrigin = TransformOrigin.Center
                                        },
                                    onLaunchAction = {
                                        launchAction(
                                            dummySwipePoint(
                                                action = widgetObject.action
                                            )
                                        )
                                    },
                                    blockTouches = widgetObject.ghosted == true
                                )
                            }

                        }
                    }
                }
            }
        }
    }
}


/**
 * Checks if pointer position is inside any foreground widget bounds.
 */
private fun Offset.isInsideForegroundWidget(
    widgets: List<Widget>,
    dm: DisplayMetrics,
    cellSizePx: Float
): Boolean = widgets.any { widget ->
    if (widget.foreground == false) return@any false

    val left = widget.x * dm.widthPixels
    val top = widget.y * dm.heightPixels

    val width = widget.spanX * cellSizePx
    val height = widget.spanY * cellSizePx

    val right = left + width
    val bottom = top + height

    x in left..right &&
            y in top..bottom
}
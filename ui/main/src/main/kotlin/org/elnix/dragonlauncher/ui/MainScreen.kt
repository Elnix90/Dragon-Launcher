package org.elnix.dragonlauncher.ui

import android.annotation.SuppressLint
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.elnix90.runtime.asState
import io.github.elnix90.runtime.asStateNull
import kotlinx.coroutines.delay
import org.elnix.dragonlauncher.base.model.serializables.GlobalDraggingMode
import org.elnix.dragonlauncher.base.model.serializables.MainScreenLayer
import org.elnix.dragonlauncher.ktx.toDp
import org.elnix.dragonlauncher.models.PointsViewModel
import org.elnix.dragonlauncher.models.SwipeViewModel
import org.elnix.dragonlauncher.models.WidgetsViewModel
import org.elnix.dragonlauncher.settings.stores.map.BehaviorSettingsStore
import org.elnix.dragonlauncher.settings.stores.map.UiSettingsStore
import org.elnix.dragonlauncher.ui.base.activityViewModel
import org.elnix.dragonlauncher.ui.base.asState
import org.elnix.dragonlauncher.ui.components.WidgetHostView
import org.elnix.dragonlauncher.ui.components.burger.BurgerListAction
import org.elnix.dragonlauncher.ui.components.burger.MoreOptions
import org.elnix.dragonlauncher.ui.helpers.ChargingAnimation
import org.elnix.dragonlauncher.ui.helpers.HoldToActivateArc
import org.elnix.dragonlauncher.ui.helpers.wallpaper.CustomDim
import org.elnix.dragonlauncher.ui.helpers.wallpaper.WallpaperDim
import org.elnix.dragonlauncher.ui.statusbar.StatusBar
import kotlin.time.Duration.Companion.milliseconds

@SuppressLint("LocalContextResourcesRead")
@Composable
fun MainScreen(
	swipeViewModel: SwipeViewModel = activityViewModel(),
	widgetsViewModel: WidgetsViewModel = activityViewModel(),
	pointsViewModel: PointsViewModel = activityViewModel()
) {
	val density = LocalDensity.current

	val swipeService = swipeViewModel.swipeService
	val widgetsService = widgetsViewModel.widgetsService
	val nestNavigationService = pointsViewModel.nestsNavigationService

	val mainScreenLayers by swipeService.mainScreenLayerOrder.asState()
	val holdObject by swipeService.holdObject.asState()

	val dm = widgetsService.dm
	val widgetsObjects by widgetsService.widgets.asState()

	val backAction by BehaviorSettingsStore.backAction.asStateNull()

	val start by swipeService.start.asState()
	val current by swipeService.current.asState()
	val globalDraggingMode by swipeService.globalDraggingMode.asState()
	val fixedOffset by swipeService.fixedOffset.asState()

	val showDropDownMenuSettings by swipeService.showDropDownMenuSettings.asState()

	val nestId by nestNavigationService.currentNestId.collectAsState()

	val filteredWidgetObjects by remember(widgetsObjects, nestId) {
		derivedStateOf {
			widgetsObjects.filter { it.nestId == nestId }
		}
	}

	val holdMenuEntries by swipeService.holdMenuEntriesString.asState()

    /*
     * 1. Tests if the current nest is the main, if not, go back one nest
     * 2. Activate the back actions
     */
	BackHandler {
		if (nestId != 0) {
			nestNavigationService.goBack()
		} else if (backAction != null) {
			swipeService.launchAction(backAction!!)
		}
	}

	val mainDimAmount by UiSettingsStore.wallpaperDimMainScreen.asState()
	WallpaperDim(mainDimAmount)

	val scope = rememberCoroutineScope()
	Box(
		modifier =
			Modifier
				.fillMaxSize()
				.background(Color.Transparent)
				.pointerInput(Unit) {
					with(swipeService) { mainDragGesture() }
				}.pointerInput(Unit) {
					with(swipeService) { holdGesture(scope.coroutineContext) }
				}
	) {
		mainScreenLayers.filter { it.enabled }.forEach { layer ->
			when (layer) {
				is MainScreenLayer.ChargingAnimation -> {
					ChargingAnimation()
				}

				is MainScreenLayer.CustomDim -> {
					var showCustomDim by remember { mutableStateOf(false) }

					LaunchedEffect(start) {
						if (start != null && globalDraggingMode is GlobalDraggingMode.Normal) {
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
					val startOffset = when (globalDraggingMode) {
						is GlobalDraggingMode.Fixed -> fixedOffset
						GlobalDraggingMode.Normal -> start
					}

					MainScreenOverlay(
						lineBeforeNests = layer.lineBeforeNests,
						start = startOffset,
						current = current,
						globalDraggingMode = globalDraggingMode,
						currentNestId = nestId
					)
				}

				is MainScreenLayer.HoldToActivate -> {
					val holdAnchor by swipeService.holdAnchor.asState()
					val holdProgress by swipeService.holdProgress

					HoldToActivateArc(
						center = holdAnchor,
						progress = holdProgress,
						customObject = holdObject,
						swipeService = swipeService
					)

					if (holdAnchor != null) {
						val actions =
							holdMenuEntries.map { route ->
								MoreOptions(
									onClick = { swipeService.setting(route) },
									icon = route.icon,
									text = { stringResource(route.resId) }
								)
							}

						val dpOffset =
							with(density) {
								DpOffset(
									x = holdAnchor!!.x.toDp(),
									y = holdAnchor!!.y.toDp()
								)
							}

						Box(
							modifier = Modifier.offset(dpOffset.x, dpOffset.y)
						) {
							BurgerListAction(
								actions = actions,
								isExpanded = showDropDownMenuSettings,
								onDismissRequest = { swipeService.dismissMenu() }
							)
						}
					}
				}

				is MainScreenLayer.StatusBar -> {
					StatusBar { action -> swipeService.launchAction(action) }
				}

				is MainScreenLayer.Widgets -> {
					val cellSizePx by widgetsViewModel.widgetsService.cellSizePx.collectAsStateWithLifecycle()

					filteredWidgetObjects.forEach { widget ->
						key(widget.id, nestId) {
							WidgetHostView(
								widget = widget,
								cellSizePx = cellSizePx,
								modifier =
									Modifier
										.offset {
											IntOffset(
												x = (widget.x * dm.widthPixels).toInt(),
												y = (widget.y * dm.heightPixels).toInt()
											)
										}.size(
											width = (widget.spanX * cellSizePx).toDp,
											height = (widget.spanY * cellSizePx).toDp
										).graphicsLayer {
											rotationZ = widget.angle
											transformOrigin = TransformOrigin.Center
										},
								onLaunchAction = {
									swipeService.launchAction(widget.action)
								},
								blockTouches = widget.ghosted == true
							)
						}
					}
				}
			}
		}
	}
}

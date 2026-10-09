package org.elnix.dragonlauncher.ui.helpers.swipe

import android.content.Context
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.rememberTextMeasurer
import io.github.elnix90.runtime.asState
import org.elnix.dragonlauncher.base.model.serializables.GlobalDraggingMode
import org.elnix.dragonlauncher.base.model.serializables.IconShape
import org.elnix.dragonlauncher.base.theme.ExtraColors
import org.elnix.dragonlauncher.base.theme.LocalExtraColors
import org.elnix.dragonlauncher.models.PointsViewModel
import org.elnix.dragonlauncher.models.SwipeViewModel
import org.elnix.dragonlauncher.points.PointsService
import org.elnix.dragonlauncher.settings.stores.map.DrawerSettingsStore
import org.elnix.dragonlauncher.settings.stores.map.UiSettingsStore
import org.elnix.dragonlauncher.ui.base.activityViewModel
import org.elnix.dragonlauncher.ui.base.asState
import org.elnix.dragonlauncher.ui.composition.LocalNestDebugOverlay
import org.elnix.dragonlauncher.ui.composition.LocalPointPreviewTitleOptions

/**
 * Aggregated drawing parameters derived from [PointsViewModel] and other reactive sources.
 *
 * Composed once per key change inside [rememberDrawParams] so that no computation
 * is duplicated inside the DrawScope drawing functions.
 */
data class DrawParams(
	val ctx: Context,
	val pointsService: PointsService,
	val extraColors: ExtraColors,
	val colorScheme: ColorScheme,
	val iconShape: IconShape,
	val maxNestsDepth: Int,
	val maxTopBarDepth: Int,
	val isDefaultEditing: Boolean,
	/** Settings Screen only */
	val eraseColor: Color,
	/** Settings Screen only */
	val preventDrawingSubNests: Boolean,
	/** Settings Screen only */
	val pointSettingsDisplay: Boolean,
	val isTopPoint: Boolean,
	val appIconOverlayScale: Float,
	/** Settings Screen only */
	val hideShapes: Boolean,
	val skipSelected: Boolean,
	val showCurrentPoint: Boolean,
	val showAllPointsInCurrentShape: Boolean,
	val showAllPointsInCurrentNest: Boolean,
	val showPointPreviewCenterStartPosition: Boolean,
	val nestDebugOverlay: Boolean,
	val showCancelZone: Boolean,
	val showShape: Boolean,
	val showAllShapesInNest: Boolean,
	val textMeasurer: TextMeasurer,
	val globalDraggingMode: GlobalDraggingMode
)

/**
 * Creates a [DrawParams] reactively observing the current [PointsViewModel] state.
 *
 * The returned instance updates whenever [PointsService.points], [PointsService.nests],
 * [PointsService.defaultPoint], or any observed UI / debug setting changes.
 *
 * [org.elnix.dragonlauncher.base.cache.PointStableCache] is maintained by
 * [org.elnix.dragonlauncher.models.PointsViewModel] - this function does not
 * drive cache synchronization.
 */
@Composable
fun rememberDrawParams(
	eraseColor: Color,
	isDefaultEditing: Boolean,
	allowShowPointCenter: Boolean,
	pointSettingsDisplay: Boolean,
	showCancelZone: Boolean,
	hideShapes: Boolean,
	skipSelected: Boolean,
	isTopPoint: Boolean,
	pointsViewModel: PointsViewModel = activityViewModel(),
	swipeViewModel: SwipeViewModel = activityViewModel()
): DrawParams {
	val ctx = LocalContext.current
	val extraColors = LocalExtraColors.current

	val appIconOverlayScale = LocalPointPreviewTitleOptions.current.appIconOverlayScale

	val colorScheme = MaterialTheme.colorScheme

	val showCurrentSelectedPoint by UiSettingsStore.showCurrentSelectedPoint.asState()
	val maxNestsDepth by UiSettingsStore.maxNestsDepth.asState()
	val maxTopBarDepth by UiSettingsStore.maxTopBarDepth.asState()

	val showAllPointsInCurrentShape by UiSettingsStore.showAllPointsInCurrentShape.asState()
	val showAllPointsInCurrentNest by UiSettingsStore.showAllPointsInCurrentNest.asState()

	val showPointPreviewCenterStartPosition by UiSettingsStore.showPointPreviewCenterStartPosition.asState()

	val showShape by UiSettingsStore.showCurrentShape.asState()
	val showAllShapesInNest by UiSettingsStore.showAllShapesInNest.asState()

	val textMeasurer = rememberTextMeasurer()
	val nestDebugOverlay = LocalNestDebugOverlay.current

	val iconShape by DrawerSettingsStore.iconShape.asState()

	val globalDraggingMode by swipeViewModel.swipeService.globalDraggingMode.asState()

	return remember(
		extraColors,
		colorScheme,
		appIconOverlayScale,
		maxNestsDepth,
		maxTopBarDepth,
		eraseColor,
		pointSettingsDisplay,
		hideShapes,
		skipSelected,
		showCurrentSelectedPoint,
		showAllPointsInCurrentShape,
		showAllPointsInCurrentNest,
		allowShowPointCenter,
		showPointPreviewCenterStartPosition,
		nestDebugOverlay,
		showCancelZone,
		showShape,
		showAllShapesInNest,
		textMeasurer,
		globalDraggingMode
	) {
		DrawParams(
			ctx = ctx,
			pointsService = pointsViewModel.pointsService,
			extraColors = extraColors,
			colorScheme = colorScheme,
			iconShape = iconShape,
			maxNestsDepth = maxNestsDepth,
			maxTopBarDepth = maxTopBarDepth,
			isDefaultEditing = isDefaultEditing,
			isTopPoint = isTopPoint,
			appIconOverlayScale = appIconOverlayScale,
			eraseColor = eraseColor,
			preventDrawingSubNests = false,
			pointSettingsDisplay = pointSettingsDisplay,
			hideShapes = hideShapes,
			skipSelected = skipSelected,
			showCurrentPoint = showCurrentSelectedPoint,
			showAllPointsInCurrentShape = showAllPointsInCurrentShape,
			showAllPointsInCurrentNest = showAllPointsInCurrentNest,
			showPointPreviewCenterStartPosition = allowShowPointCenter && showPointPreviewCenterStartPosition,
			nestDebugOverlay = nestDebugOverlay,
			showCancelZone = showCancelZone,
			showShape = showShape,
			showAllShapesInNest = showAllShapesInNest,
			textMeasurer = textMeasurer,
			globalDraggingMode = globalDraggingMode
		)
	}
}

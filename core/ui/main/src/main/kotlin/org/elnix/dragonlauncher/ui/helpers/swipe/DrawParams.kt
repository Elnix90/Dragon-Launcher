package org.elnix.dragonlauncher.ui.helpers.swipe

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.rememberTextMeasurer
import io.github.elnix90.runtime.asState
import org.elnix.dragonlauncher.base.theme.ExtraColors
import org.elnix.dragonlauncher.base.theme.LocalExtraColors
import org.elnix.dragonlauncher.models.PointsViewModel
import org.elnix.dragonlauncher.points.PointsService
import org.elnix.dragonlauncher.settings.stores.map.UiSettingsStore
import org.elnix.dragonlauncher.ui.base.activityViewModel
import org.elnix.dragonlauncher.ui.composition.LocalNestDebugOverlay
import org.elnix.dragonlauncher.ui.helpers.swipe.cache.nests.RememberPointStableCaches
import org.elnix.dragonlauncher.ui.helpers.swipe.cache.points.RememberNestsStableCaches


/**
 * Aggregated drawing parameters derived from [PointsViewModel] and other reactive sources.
 *
 * Composed once per key change inside [rememberDrawParams] so that no computation
 * is duplicated inside the DrawScope drawing functions.
 */
public data class DrawParams(
    val ctx: Context,

    val pointsService: PointsService,
    val extraColors: ExtraColors,

    val maxNestsDepth: Int,

    val preventBgErasing: Boolean,
    val allowShowPointInCenter: Boolean,
    val preventDrawingSubNests: Boolean,
    val showConfiguratorDecorations: Boolean,
    val showCurrentPoint: Boolean,
    val showAllActionsInCurrentShape: Boolean,
    val showAllActionsInCurrentNest: Boolean,
    val hideSelectedPoint: Boolean,

    val nestDebugOverlay: Boolean,
    val showCancelZone: Boolean,
    val showShape: Boolean,
    val showAllShapesInNest: Boolean,
    val textMeasurer: TextMeasurer
)

/**
 * Creates a [DrawParams] reactively observing the current [PointsViewModel] state.
 *
 * The returned instance updates whenever [PointsService.points], [PointsService.nests],
 * [PointsService.defaultPoint], or any observed UI / debug setting changes.
 *
 * As a side effect this function also drives [RememberPointStableCaches] which keeps
 * [org.elnix.dragonlauncher.ui.helpers.swipe.cache.nests.PointStableCache] synchronised with the current point set.
 *
 * @param pointsViewModel source of point/nest data
 * @param preventBgErasing when true the nest background is preserved (not cleared)
 * @param showConfiguratorDecorations when true shows cycle/hold-and-run badges
 * @param forceShowAllActionsInCurrentNest when true every point of the nest is drawn
 */
@Composable
public fun rememberDrawParams(
    preventBgErasing: Boolean,
    showConfiguratorDecorations: Boolean,
    forceShowAllActionsInCurrentNest: Boolean,
    allowShowPointCenter: Boolean,
    hideSelectedPoint: Boolean,
    showCancelZone: Boolean,
    hideShapes: Boolean,
    pointsViewModel: PointsViewModel = activityViewModel()
): DrawParams {
    val ctx = LocalContext.current
    val extraColors = LocalExtraColors.current

    val showCurrentPoint by UiSettingsStore.showPreviewPoint.asState()
    val maxNestsDepth by UiSettingsStore.maxNestsDepth.asState()
    val showPointInCenter by UiSettingsStore.showPointPreviewCenterStartPosition.asState()

    val showAllActionInCurrentShape by UiSettingsStore.showAllActionsOnCurrentShape.asState()
    val showPointPreviewCenterStartPosition by UiSettingsStore.showPointPreviewCenterStartPosition.asState()

    val showShape by UiSettingsStore.showShape.asState()
    val showAllShapesInNest by UiSettingsStore.showAllShapesInNest.asState()

    val textMeasurer = rememberTextMeasurer()
    val nestDebugOverlay = LocalNestDebugOverlay.current

    RememberPointStableCaches()
    RememberNestsStableCaches()

    return remember(
        extraColors,
        showCurrentPoint,
        maxNestsDepth,
        showPointInCenter,
        showAllActionInCurrentShape,
        showPointPreviewCenterStartPosition,
        textMeasurer,
        nestDebugOverlay,
        showConfiguratorDecorations,
        forceShowAllActionsInCurrentNest,
        allowShowPointCenter,
        hideSelectedPoint,
        showShape,
        showAllShapesInNest,
        hideShapes,
        showCancelZone
    ) {
        DrawParams(
            ctx = ctx,
            pointsService = pointsViewModel.pointsService,
            extraColors = extraColors,
            maxNestsDepth = maxNestsDepth,
            preventBgErasing = preventBgErasing,
            allowShowPointInCenter = allowShowPointCenter && showPointInCenter,
            preventDrawingSubNests = false,
            showConfiguratorDecorations = showConfiguratorDecorations,
            showCurrentPoint = showCurrentPoint,
            showAllActionsInCurrentShape = showAllActionInCurrentShape,
            showAllActionsInCurrentNest = forceShowAllActionsInCurrentNest || showPointPreviewCenterStartPosition,
            hideSelectedPoint = hideSelectedPoint,
            nestDebugOverlay = nestDebugOverlay,
            showCancelZone = showCancelZone,
            showShape = if (hideShapes) false else showShape,
            showAllShapesInNest = if (hideShapes) false else showAllActionInCurrentShape,
            textMeasurer = textMeasurer
        )
    }
}

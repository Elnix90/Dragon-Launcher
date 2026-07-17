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
import org.elnix.dragonlauncher.ui.helpers.swipe.cache.points.RememberPointStableCaches


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

    /** Settings Screen only */
    val preventBgErasing: Boolean,
    /** Settings Screen only */
    val preventDrawingSubNests: Boolean,
    /** Settings Screen only */
    val pointSettingsDisplay: Boolean,

    val showCurrentPoint: Boolean,
    val showAllPointsInCurrentShape: Boolean,
    val showAllPointsInCurrentNest: Boolean,
    val allowShowPointInCenter: Boolean,

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
 * [org.elnix.dragonlauncher.base.cache.PointStableCache] synchronised with the current point set.
 */
@Composable
public fun rememberDrawParams(
    preventBgErasing: Boolean,
    allowShowPointCenter: Boolean,
    pointSettingsDisplay: Boolean,
    showCancelZone: Boolean,
    hideShapes: Boolean,
    pointsViewModel: PointsViewModel = activityViewModel()
): DrawParams {
    val ctx = LocalContext.current
    val extraColors = LocalExtraColors.current

    val showCurrentPoint by UiSettingsStore.showPreviewPoint.asState()
    val maxNestsDepth by UiSettingsStore.maxNestsDepth.asState()
    val showPointInCenter by UiSettingsStore.showPointPreviewCenterStartPosition.asState()

    val showAllPointsInCurrentShape by UiSettingsStore.showAllPointsInCurrentShape.asState()
    val showAllPointsInCurrentNest by UiSettingsStore.showAllPointsInCurrentNest.asState()

    val showPointPreviewCenterStartPosition by UiSettingsStore.showPointPreviewCenterStartPosition.asState()

    val showShape by UiSettingsStore.showShape.asState()
    val showAllShapesInNest by UiSettingsStore.showAllShapesInNest.asState()

    val textMeasurer = rememberTextMeasurer()
    val nestDebugOverlay = LocalNestDebugOverlay.current


    return remember(
        extraColors,
        maxNestsDepth,
        preventBgErasing,
        showPointInCenter,
        allowShowPointCenter,
        showCurrentPoint,
        showAllPointsInCurrentShape,
        showPointPreviewCenterStartPosition,
        pointSettingsDisplay,
        nestDebugOverlay,
        showCancelZone,
        hideShapes,
        showShape,
        showAllShapesInNest,
        textMeasurer
    ) {
        DrawParams(
            ctx = ctx,
            pointsService = pointsViewModel.pointsService,
            extraColors = extraColors,
            maxNestsDepth = maxNestsDepth,
            preventBgErasing = preventBgErasing,
            allowShowPointInCenter = allowShowPointCenter && showPointInCenter,
            preventDrawingSubNests = false,
            showCurrentPoint = showCurrentPoint,
            showAllPointsInCurrentShape = showAllPointsInCurrentShape,
            showAllPointsInCurrentNest = showAllPointsInCurrentNest,
            pointSettingsDisplay = pointSettingsDisplay,
            nestDebugOverlay = nestDebugOverlay,
            showCancelZone = showCancelZone,
            showShape = if (hideShapes) false else showShape,
            showAllShapesInNest = if (hideShapes) false else showAllShapesInNest,
            textMeasurer = textMeasurer
        )
    }
}

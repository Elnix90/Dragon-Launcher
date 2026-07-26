package org.elnix.dragonlauncher.ui.helpers.swipe

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
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

    val maxNestsDepth: Int,

    /** Settings Screen only */
    val eraseColor: Color,
    /** Settings Screen only */
    val preventDrawingSubNests: Boolean,
    /** Settings Screen only */
    val pointSettingsDisplay: Boolean,
    /** Settings Screen only */
    val hideShapes: Boolean,
    val skipSelected: Boolean,

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
 * [org.elnix.dragonlauncher.base.cache.PointStableCache] is maintained by
 * [org.elnix.dragonlauncher.models.PointsViewModel] - this function does not
 * drive cache synchronization.
 */
@Composable
fun rememberDrawParams(
    eraseColor: Color,
    allowShowPointCenter: Boolean,
    pointSettingsDisplay: Boolean,
    showCancelZone: Boolean,
    hideShapes: Boolean,
    skipSelected: Boolean,
    pointsViewModel: PointsViewModel = activityViewModel()
): DrawParams {
    val ctx = LocalContext.current
    val extraColors = LocalExtraColors.current

    val showCurrentPoint by UiSettingsStore.showPreviewPoint.asState()
    val maxNestsDepth by UiSettingsStore.maxNestsDepth.asState()

    val showAllPointsInCurrentShape by UiSettingsStore.showAllPointsInCurrentShape.asState()
    val showAllPointsInCurrentNest by UiSettingsStore.showAllPointsInCurrentNest.asState()

    val showPointPreviewCenterStartPosition by UiSettingsStore.showPointPreviewCenterStartPosition.asState()

    val showShape by UiSettingsStore.showCurrentShape.asState()
    val showAllShapesInNest by UiSettingsStore.showAllShapesInNest.asState()

    val textMeasurer = rememberTextMeasurer()
    val nestDebugOverlay = LocalNestDebugOverlay.current


    return remember(
        extraColors,
        maxNestsDepth,
        eraseColor,
        pointSettingsDisplay,
        hideShapes,
        skipSelected,
        showCurrentPoint,
        showAllPointsInCurrentShape,
        showAllPointsInCurrentNest,
        allowShowPointCenter,
        showPointPreviewCenterStartPosition,
        nestDebugOverlay,
        showCancelZone,
        showShape,
        showAllShapesInNest,
        textMeasurer
    ) {
        DrawParams(
            ctx = ctx,
            pointsService = pointsViewModel.pointsService,
            extraColors = extraColors,
            maxNestsDepth = maxNestsDepth,
            eraseColor = eraseColor,
            preventDrawingSubNests = false,
            pointSettingsDisplay = pointSettingsDisplay,
            hideShapes = hideShapes,
            skipSelected = skipSelected,
            showCurrentPoint = showCurrentPoint,
            showAllPointsInCurrentShape = showAllPointsInCurrentShape,
            showAllPointsInCurrentNest = showAllPointsInCurrentNest,
            allowShowPointInCenter = allowShowPointCenter && showPointPreviewCenterStartPosition,
            nestDebugOverlay = nestDebugOverlay,
            showCancelZone = showCancelZone,
            showShape = showShape,
            showAllShapesInNest = showAllShapesInNest,
            textMeasurer = textMeasurer
        )
    }
}

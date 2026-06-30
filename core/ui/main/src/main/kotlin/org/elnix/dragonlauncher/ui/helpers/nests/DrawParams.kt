package org.elnix.dragonlauncher.ui.helpers.nests

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import io.github.elnix90.runtime.asState
import org.elnix.dragonlauncher.base.model.serializables.Nests
import org.elnix.dragonlauncher.base.model.serializables.Point
import org.elnix.dragonlauncher.base.model.serializables.Points
import org.elnix.dragonlauncher.base.theme.ExtraColors
import org.elnix.dragonlauncher.base.theme.LocalExtraColors
import org.elnix.dragonlauncher.models.PointsViewModel
import org.elnix.dragonlauncher.points.PointsService
import org.elnix.dragonlauncher.settings.stores.map.DebugSettingsStore
import org.elnix.dragonlauncher.settings.stores.map.UiSettingsStore
import org.elnix.dragonlauncher.ui.base.activityViewModel
import org.elnix.dragonlauncher.ui.base.asState

/**
 * Parameters forwarded to every DrawScope-level nest/point drawing function.
 *
 * @property iconBitmaps Pre-rendered action icons keyed by point id.
 */
data class DrawParams(
    val ctx: Context,
    val points: Points,
    val nests: Nests,
    val defaultPoint: Point,
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
    val nestDebugOverlay: Boolean
)

/**
 * Creates a [DrawParams] reactively observing the current [PointsViewModel] state.
 *
 * The returned instance updates whenever [PointsService.points], [PointsService.nests],
 * [PointsService.defaultPoint], or [iconBitmapsVersion] change.
 *
 * @param pointsViewModel source of point/nest data
 * @param preventBgErasing when true the nest background is preserved (not cleared)
 * @param showConfiguratorDecorations when true shows cycle/hold-and-run badges
 * @param forceShowAllActionsInCurrentNest when true every point of the nest is drawn
 * @param iconBitmaps pre-rendered action icon bitmaps
 * @param iconBitmapsVersion version counter for [iconBitmaps] content changes;
 *   pass `iconBitmaps.size` when using a [SnapshotStateMap][androidx.compose.runtime.snapshots.SnapshotStateMap]
 */
@Composable
fun rememberDrawParams(
    preventBgErasing: Boolean,
    showConfiguratorDecorations: Boolean,
    forceShowAllActionsInCurrentNest: Boolean,
    allowShowPointCenter: Boolean,
    pointsViewModel: PointsViewModel = activityViewModel()
): DrawParams {
    val ctx = LocalContext.current
    val extraColors = LocalExtraColors.current
    val pointService = pointsViewModel.pointsService

    val points by pointService.points.asState()
    val nests by pointService.nests.asState()
    val defaultPoint by pointService.defaultPoint.asState()

    val showCurrentPoint by UiSettingsStore.showPreviewPoint.asState()
    val maxNestsDepth by UiSettingsStore.maxNestsDepth.asState()
    val showPointInCenter by UiSettingsStore.showPointPreviewCenterStartPosition.asState()

    val nestDebugOverlay by DebugSettingsStore.nestDebugOverlay.asState()

    return remember(
        points,
        nests,
        defaultPoint,
    ) {
        DrawParams(
            ctx = ctx,
            points = points,
            nests = nests,
            defaultPoint = defaultPoint,
            pointsService = pointService,
            extraColors = extraColors,
            maxNestsDepth = maxNestsDepth,
            preventBgErasing = preventBgErasing,
            allowShowPointInCenter = allowShowPointCenter && showPointInCenter,
            preventDrawingSubNests = false,
            showConfiguratorDecorations = showConfiguratorDecorations,
            showCurrentPoint = showCurrentPoint,
            showAllActionsInCurrentShape = false,
            showAllActionsInCurrentNest = forceShowAllActionsInCurrentNest,
            nestDebugOverlay = nestDebugOverlay
        )
    }
}

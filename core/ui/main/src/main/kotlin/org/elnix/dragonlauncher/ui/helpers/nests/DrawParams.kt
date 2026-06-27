package org.elnix.dragonlauncher.ui.helpers.nests

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import org.elnix.dragonlauncher.base.DragonCache
import org.elnix.dragonlauncher.base.model.serializables.Nests
import org.elnix.dragonlauncher.base.model.serializables.Point
import org.elnix.dragonlauncher.base.model.serializables.Points
import org.elnix.dragonlauncher.base.theme.ExtraColors
import org.elnix.dragonlauncher.base.theme.LocalExtraColors
import org.elnix.dragonlauncher.models.PointsViewModel
import org.elnix.dragonlauncher.points.PointsService
import org.elnix.dragonlauncher.ui.base.asState

data class DrawParams(
    val ctx: Context,
    val points: Points,
    val nests: Nests,
    val defaultPoint: Point,
    val pointsService: PointsService,
    val extraColors: ExtraColors,

    val maxNestsDepth: Int,

    val preventBgErasing: Boolean,
    val allowShowIconInCenter: Boolean,
    val preventDrawingSubNests: Boolean,
    val showConfiguratorDecorations: Boolean,
    val showCurrentPoint: Boolean,
    val showAllActionsInCurrentShape: Boolean,
    val showAllActionsInCurrentNest: Boolean
)


@Composable
fun rememberDrawParams(
    pointsViewModel: PointsViewModel
): State<DrawParams> {
    val ctx = LocalContext.current
    val extraColors = LocalExtraColors.current
    val pointService = pointsViewModel.pointsService

    val points by pointService.points.asState()
    val nests by pointService.nests.asState()
    val defaultPoint by pointService.defaultPoint.asState()

    return remember {
        mutableStateOf(
            DrawParams(
                ctx = ctx,
                points = points,
                nests = nests,
                defaultPoint = defaultPoint,
                pointsService = pointService
            )
        )
    }
}
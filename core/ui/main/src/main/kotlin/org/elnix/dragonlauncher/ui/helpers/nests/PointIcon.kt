
package org.elnix.dragonlauncher.ui.helpers.nests

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import org.elnix.dragonlauncher.base.model.models.HitResult
import org.elnix.dragonlauncher.base.model.serializables.Action
import org.elnix.dragonlauncher.base.model.serializables.Point
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.models.PointsViewModel
import org.elnix.dragonlauncher.ui.actions.FinalPointIcon
import org.elnix.dragonlauncher.ui.base.activityViewModel
import org.elnix.dragonlauncher.ui.base.asState


@Suppress("FunctionName")
fun DrawScope.PointIcon(
    point: Point,
    center: Offset,
    depth: Int,
    selected: Boolean,

    drawParams: DrawParams,
    hitResult: HitResult,
) {
    val pointsService = drawParams.pointsService
    val nests = pointsService.nests.value
    val maxNestsDepth = drawParams.maxNestsDepth

    val action = point.action

    // Prevent overloading since the drawing is recursive
    if (depth < maxNestsDepth) {
        val newCenter = pointsService.computePointPosition(point, depth)

        if (action !is Action.OpenCircleNest || point.customIcon != null) {

            PointBg(
                point = point,
                center = newCenter,
                selected = selected,
                depth = depth,
                drawParams = drawParams
            ) {
                FinalPointIcon(point)
            }
        } else {
            nests
                .find { it.id == action.nestId }
                ?.takeIf { !drawParams.preventDrawingSubNests }
                ?.let { nest ->
                    NestOverlay(
                        nest = nest,
                        center = newCenter,
                        depth = depth + 1,
                        drawParams = drawParams,
                        hitResult = hitResult,
                        selectedAll = selected
                    )
                } ?: this.NestPlaceholder(
                    center = center,
                    drawParams = drawParams
                )
        }
    }
}


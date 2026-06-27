package org.elnix.dragonlauncher.ui.helpers.nests

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.DrawScope
import org.elnix.dragonlauncher.base.model.models.HitResult
import org.elnix.dragonlauncher.base.model.serializables.Nest
import org.elnix.dragonlauncher.base.model.serializables.Point
import org.elnix.dragonlauncher.ui.components.IntersectionShape

@Suppress("FunctionName")
fun DrawScope.NestOverlay(
    nest: Nest,
    center: Offset,
    depth: Int,

//    pointsViewModel: PointsViewModel = activityViewModel(),
    drawParams: DrawParams,
    hitResult: HitResult,

    selectedAll: Boolean = false,
) {

//    val extraColors = LocalExtraColors.current
//    val pointsService = pointsViewModel.pointsService
    val points= drawParams.points
    val selectedPoint = drawParams.pointsService.selectedPoint.value


//    val surfaceColorDraw = drawParams.surfaceColorDraw
//    val showAppLaunchPreview = drawParams.showAppLaunchPreview
//    val showAllActionsOnCurrentCircle = nest.showAllActionsOnCurrentCircle ?: drawParams.showAllActionsOnCurrentCircle
//    val showAllActionsOnCurrentNest = nest.showAllActionsOnCurrentNest ?: drawParams.showAllActionsOnCurrentNest
    val showAppPreviewIconCenterStartPosition = drawParams.allowShowIconInCenter


//    val eraseBg = surfaceColorDraw == Color.Transparent && !preventBgErasing
//    val maxCircleSize: UiCircle = circles.maxByOrNull { it.radius } ?: return

//    /**
//     * ## 1.
//     * If no background color provided, erases the background.
//     * The background is always erased to prevent artifacts from below to appear in the actions center / bg,
//     * or to remove the remaining circle line behind previous points (nests for instance that have their inner circle empty)
//     */
//    drawCircle(
//        color = Color.Transparent,
//        radius = maxCircleSize.radius,
//        center = center,
//        blendMode = BlendMode.Clear
//    )

//    /**
//     * ## 2.
//     * If requested to not erase the bg, draw it (this avoids the more tinted bg when using a half transparent bg color
//     */
//    if (!eraseBg) {
//        drawCircle(
//            color = surfaceColorDraw,
//            radius = maxCircleSize.radius,
//            center = center
//        )
//    }
//
//    /**
//     * ## 3.
//     * Draw all circles
//     * whether they are shown or not depends on the variable that force them to appear, `showAppCirclePreview`, and the actual [currentCircle]
//     */
    nest.intersectionShapes.forEach { shape ->
        this.IntersectionShape(
            center = center,
            shape = shape,
            drawParams = drawParams,
        )

//        val showCirclesNestLevel = nest.showCircle ?: drawParams.showAppCirclePreview
//
//        val showCircle = showCirclesNestLevel && (showAllActionsOnCurrentNest || when (currentCircle) {
//            null -> true
//            circle.id -> true
//            else -> false
//        })

//        if (showCircle) {
//            drawCircle(
//                color = extraColors.circle,
//                radius = circle.radius,
//                center = center,
//                style = Stroke(if (selectedAll) 8f else 4f)
//            )
//        }



    }

//    /**
//     *  ## 4.
//     *  Draw all needed points, they are filtered by:
//     *   - if all points are drawn in the nest, all of them
//     *   - if all points on the circle should be drawn, and that the current circle if the right one
//     *   - if the selected points should be drawn, it only picks this one
//     */
//    val filteredPoints = points.filter {
//        it.nestId == nest.id && when {
//            drawParams.showAllActionsInCurrentNest || depth > 1 -> true
//            drawParams.showAllActionsInCurrentShape && it.collidingShapeId == hitResult.targetShape?.id -> true
//            drawParams.showCurrentPoint && it.id == selectedPoint?.id -> true
//            else -> false
//        } && it.circleNumber < circles.size
//    }

    val filteredPoints = drawParams.pointsService.getPointsForNest(nest)

//    forceShowAllActionsInCurrentNest // TODO

//    val filteredPoints by remember(points.size) {
//        derivedStateOf {
//            points
//                .filter { it.nestId == nest.id }
//                .sortedBy { it.id == selectedPoint?.id }
//        }
//    }


    filteredPoints.forEach { p ->
            /**
             * Use the selectedPoint snapshot for the selected point so any staged action
             * from Cycle Actions is reflected visually (e.g. different nest or app icon).
             */
            val drawPoint: Point = selectedPoint?.takeIf { it.id ==p.id } ?: p

            PointIcon(
                center = p.offset,
                depth = depth,
                point = drawPoint,
                selected = selectedAll || (p.id == selectedPoint?.id),
                drawParams = drawParams,
                hitResult = hitResult
            )
        }

    if (showAppPreviewIconCenterStartPosition) {
        selectedPoint?.let{ selectedPoint ->
            PointIcon(
                point = selectedPoint,
                center = center,
                depth = depth,
                selected = true,
                drawParams = drawParams.copy(allowShowIconInCenter = false),
                hitResult = hitResult
            )
        }
    }
}



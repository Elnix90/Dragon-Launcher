package org.elnix.dragonlauncher.ui.helpers.nests

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import org.elnix.dragonlauncher.base.model.models.UiCircle
import org.elnix.dragonlauncher.common.circles.computePosition
import org.elnix.dragonlauncher.base.model.serializables.Point
import org.elnix.dragonlauncher.base.model.models.SwipeDrawParams

fun DrawScope.circlesSettingsOverlay(
    drawParams: SwipeDrawParams,


    center: Offset,
    depth: Int,

    circles: List<UiCircle>,
    selectedPoint: Point?,
    nestId: Int,

    /**
     * Which circle is currently selected
     *  - `null` means *Show them all*
     *  - `-1` means *no circle selected*
     */
    currentCircle: Int? = null,

    selectedAll: Boolean = false,
    preventBgErasing: Boolean = false,
    preventDrawingSubNests: Boolean = false,
    /** Cycle stack + Hold & Run bolt — only in settings / edit previews, not on the home overlay. */
    showConfiguratorDecorations: Boolean = false,
) {
    val currentNest = drawParams.nests.find { it.id == nestId } ?: return

    val points = drawParams.points
    val surfaceColorDraw = drawParams.surfaceColorDraw
    val extraColors = drawParams.extraColors
    val showAppLaunchPreview = drawParams.showAppLaunchPreview
    val showAllActionsOnCurrentCircle = currentNest.showAllActionsOnCurrentCircle ?: drawParams.showAllActionsOnCurrentCircle
    val showAllActionsOnCurrentNest = currentNest.showAllActionsOnCurrentNest ?: drawParams.showAllActionsOnCurrentNest
    val showAppPreviewIconCenterStartPosition = drawParams.showAppPreviewIconCenterStartPosition


    val eraseBg = surfaceColorDraw == Color.Transparent && !preventBgErasing
    val maxCircleSize: UiCircle = circles.maxByOrNull { it.radius } ?: return

    /**
     * ## 1.
     * If no background color provided, erases the background.
     * The background is always erased to prevent artifacts from below to appear in the actions center / bg,
     * or to remove the remaining circle line behind previous points (nests for instance that have their inner circle empty)
     */
    drawCircle(
        color = Color.Transparent,
        radius = maxCircleSize.radius,
        center = center,
        blendMode = BlendMode.Clear
    )

    /**
     * ## 2.
     * If requested to not erase the bg, draw it (this avoids the more tinted bg when using a half transparent bg color
     */
    if (!eraseBg) {
        drawCircle(
            color = surfaceColorDraw,
            radius = maxCircleSize.radius,
            center = center
        )
    }

    /**
     * ## 3.
     * Draw all circles
     * whether they are shown or not depends on the variable that force them to appear, `showAppCirclePreview`, and the actual [currentCircle]
     */
    circles.forEach { circle ->

        val showCirclesNestLevel = currentNest.showCircle ?: drawParams.showAppCirclePreview

        val showCircle = showCirclesNestLevel && (showAllActionsOnCurrentNest || when (currentCircle) {
            null -> true
            circle.id -> true
            else -> false
        })

        if (showCircle) {
            drawCircle(
                color = extraColors.circle,
                radius = circle.radius,
                center = center,
                style = Stroke(if (selectedAll) 8f else 4f)
            )
        }
    }

    /**
     *  ## 4.
     *  Draw all needed points, they are filtered by:
     *   - if all points are drawn in the nest, all of them
     *   - if all points on the circle should be drawn, and that the current circle if the right one
     *   - if the selected points should be drawn, it only picks this one
     */
    val filteredPoints = points.filter {
        it.nestId == nestId && when {
            showAllActionsOnCurrentNest || depth > 1 -> true
            showAllActionsOnCurrentCircle && it.circleNumber == currentCircle -> true
            showAppLaunchPreview && it.id == selectedPoint?.id -> true
            else -> false
        } && it.circleNumber < circles.size
    }


    filteredPoints
        .sortedBy { it.id == selectedPoint?.id }
        .forEach { p ->

            val newCenter = p.computePosition(
                circles = circles,
                center = center
            )

            /**
             * Use the selectedPoint snapshot for the selected point so any staged action
             * from Cycle Actions is reflected visually (e.g. different nest or app icon).
             */
            val drawPoint =
                if (selectedPoint != null && p.id == selectedPoint.id) {
                    selectedPoint
                } else {
                    p
                }

            actionsInCircle(
                drawParams = drawParams.copy(showAppPreviewIconCenterStartPosition = false),
                center = newCenter,
                depth = depth,
                point = drawPoint,
                selected = selectedAll || (p.id == selectedPoint?.id),
                preventBgErasing = preventBgErasing,
                preventDrawingSubNests = preventDrawingSubNests,
                showConfiguratorDecorations = showConfiguratorDecorations
            )
        }

    if (showAppPreviewIconCenterStartPosition && selectedPoint != null) {
        actionsInCircle(
            selected = true,
            point = selectedPoint,
            drawParams = drawParams.copy(showAppPreviewIconCenterStartPosition = false),
            center = center,
            depth = depth,
            showConfiguratorDecorations = showConfiguratorDecorations
        )
    }
}

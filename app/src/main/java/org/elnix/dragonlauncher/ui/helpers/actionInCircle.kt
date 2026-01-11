package org.elnix.dragonlauncher.ui.helpers

import android.content.Context
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import org.elnix.dragonlauncher.R
import org.elnix.dragonlauncher.data.CircleNest
import org.elnix.dragonlauncher.data.SwipeActionSerializable
import org.elnix.dragonlauncher.data.helpers.SwipePointSerializable
import org.elnix.dragonlauncher.utils.actions.loadDrawableResAsBitmap


fun DrawScope.actionsInCircle(
    selected: Boolean,
    point: SwipePointSerializable,
    nests: List<CircleNest>,
    px: Float,
    py: Float,
    ctx: Context,
    circleColor: Color,
    colorAction: Color,
    pointIcons: Map<String, ImageBitmap>,
    preventBgErasing: Boolean = false
) {
    val action = point.action

    val borderColor = if (selected) {
        point.borderColorSelected?.let { Color(it) }
    } else {
        point.borderColor?.let { Color(it) }
    } ?: circleColor

    val borderStroke = if (selected) {
        point.borderStrokeSelected ?: 8f
    } else {
        point.borderStroke ?: 4f
    }


    val backgroundColor = if (selected) {
        point.backgroundColorSelected?.let { Color(it) }
    } else {
        point.backgroundColor?.let { Color(it) }
    } ?: Color.Transparent

    if (action !is SwipeActionSerializable.OpenCircleNest) {
        // if no background color provided, erases the background
        val eraseBg = backgroundColor == Color.Transparent && !preventBgErasing

        // Erases the color, instead of putting it, that lets the wallpaper pass trough
        if (eraseBg) {
            drawCircle(
                color = Color.Transparent,
                radius = 44f,
                center = Offset(px, py),
                blendMode = BlendMode.Clear
            )
        } else
            drawCircle(
                color = backgroundColor,
                radius = 44f,
                center = Offset(px, py)
            )

            if (borderColor != Color.Transparent && borderStroke > 0f) {
                drawCircle(
                    color =  borderColor,
                    radius = 44f,
                    center = Offset(px, py),
                    style = Stroke(borderStroke)
                )
            }


        val icon = point.id.let { pointIcons[it] }

        if (icon != null) {
            drawImage(
                image = icon,
                dstOffset = IntOffset(px.toInt() - 28, py.toInt() - 28),
                dstSize = IntSize(56, 56),
                colorFilter = if (
                    action !is SwipeActionSerializable.LaunchApp &&
                    action !is SwipeActionSerializable.LaunchShortcut &&
                    action !is SwipeActionSerializable.OpenDragonLauncherSettings
                ) ColorFilter.tint(colorAction)
                else null
            )
        }

    } else {
        nests.find { it.id == action.nestId }?.let { nest ->


            val circlesWidthIncrement = 1f / (nest.dragDistances.size - 1)


            // Action is OpenCirclesNext (draws small the circleNests)
            nests.find { it.id == action.nestId }!!.dragDistances.filter { it.key != -1 }
                .forEach { (index, _) ->
                    val radius = 100f * circlesWidthIncrement * (index + 1)
                    drawCircle(
                        color = colorAction,
                        radius = radius,
                        center = Offset(px, py),
                        style = Stroke(if (selected) 8f else 4f)
                    )
                }
        } ?: drawImage(
            image = loadDrawableResAsBitmap(ctx, R.drawable.ic_app_default, 48, 48),
            dstOffset = IntOffset(px.toInt() - 28, py.toInt() - 28),
            dstSize = IntSize(56, 56)
        )
    }
}

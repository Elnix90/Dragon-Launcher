package org.elnix.dragonlauncher.ui.helpers

import android.provider.Settings
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import org.elnix.dragonlauncher.base.ktx.px
import org.elnix.dragonlauncher.common.serializables.CustomObjectSerializable
import org.elnix.dragonlauncher.common.messyfolder.resolveShape
import org.elnix.dragonlauncher.ui.base.UiConstants
import org.elnix.dragonlauncher.ui.helpers.customobjects.drawNeonGlowShapePath

private fun DrawScope.holdTolerance(
    center: Offset,
    tolerance: Float
) {
    drawCircle(
        color = Color.Cyan,
        center = center,
        radius = tolerance,
        style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
    )
}


@Composable
fun HoldToActivateArc(
    center: Offset?,
    progress: Float,
    rgbLoading: Boolean,
    rotationsPerSecond: Float,
    customObjectSerializable: CustomObjectSerializable,
    erase: Boolean = false,
    playAnimation: Boolean = true,
    showHoldTolerance: (() -> Float)? = null
) {
    val ctx = LocalContext.current

    if (center == null || progress <= 0f) return

    val color = if (rgbLoading) {
        Color.hsv(progress * 360f, 1f, 1f)
    } else {
        customObjectSerializable.color ?: UiConstants.defaultHoldCustomObject.color!!
    }

    val shape = customObjectSerializable.shape ?: UiConstants.defaultHoldCustomObject.shape
    val radius = (customObjectSerializable.size ?: UiConstants.defaultHoldCustomObject.size!!).dp
    val strokeWidth = (customObjectSerializable.stroke ?: UiConstants.defaultHoldCustomObject.stroke!!).dp
    val glowRadius = customObjectSerializable.glow?.radius?.dp?.px ?: 0f
    val glowColor = customObjectSerializable.glow?.color

    // Remembers for each new click the random or not rotation it applies (if -1)
    val rotationAngleStart = remember(center) {
        (customObjectSerializable.rotation ?: UiConstants.defaultHoldCustomObject.rotation!!)
            .takeIf { it != -1 }
            ?: (0..360).random()
    }


    // Remembers the shape for each new click, but keeps the same when holding
    val resolvedShape: Shape = remember(center) { shape.resolveShape() }

    val infiniteTransition = rememberInfiniteTransition(label = "infinite")

    val animationScale = Settings.Global.getFloat(
        ctx.contentResolver,
        Settings.Global.ANIMATOR_DURATION_SCALE,
        1f
    ).coerceAtLeast(0.1f) // avoid division by zero

    val rotationTween = if (rotationsPerSecond > 0f) {
        (1000f / rotationsPerSecond / animationScale).toInt()
    } else 1

    val rotationAnimation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            tween(rotationTween, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val pathMeasurer = remember { PathMeasure() }
    val destinationPath = remember { androidx.compose.ui.graphics.Path() }
    val matrix = remember { Matrix() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawWithCache {
                val diameterPx = radius.toPx() * 2

                val composePath = when (val outline = resolvedShape.createOutline(
                    size = Size(diameterPx, diameterPx),
                    layoutDirection = layoutDirection,
                    density = this
                )) {
                    is Outline.Generic -> outline.path
                    is Outline.Rounded -> androidx.compose.ui.graphics.Path().apply { addRoundRect(outline.roundRect) }
                    is Outline.Rectangle -> androidx.compose.ui.graphics.Path().apply { addRect(outline.rect) }
                }

                matrix.reset()
                matrix.translate(-diameterPx / 2f, -diameterPx / 2f)
                composePath.transform(matrix)

                pathMeasurer.setPath(composePath, false)
                val totalLength = pathMeasurer.length
                destinationPath.reset()
                pathMeasurer.getSegment(0f, totalLength * progress, destinationPath)

                onDrawBehind {
                    withTransform({
                        // Rotate to start to the angle position chosen
                        rotate(degrees = rotationAngleStart.toFloat(), pivot = center)

                        // Rotates with the animation rotation, computed above
                        if (rotationsPerSecond > 0 && playAnimation) {
                            rotate(degrees = rotationAnimation, pivot = center)
                        }
                        translate(center.x, center.y)
                    }) {
                        drawNeonGlowShapePath(
                            path = destinationPath,
                            color = color,
                            lineStrokeWidth = strokeWidth.toPx(),
                            glowRadius = glowRadius,
                            glowColor = glowColor ?: color,
                            erase = erase
                        )
                    }

                    showHoldTolerance?.let {
                        holdTolerance(center, it())
                    }
                }
            }
    )
}
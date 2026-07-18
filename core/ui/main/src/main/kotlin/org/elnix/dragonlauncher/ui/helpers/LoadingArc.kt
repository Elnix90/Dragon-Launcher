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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import io.github.elnix90.runtime.asState
import org.elnix.dragonlauncher.base.model.serializables.CustomObject
import org.elnix.dragonlauncher.base.resolveShape
import org.elnix.dragonlauncher.base.theme.LocalExtraColors
import org.elnix.dragonlauncher.ktx.toPath
import org.elnix.dragonlauncher.settings.stores.map.HoldToActivateArcSettingsStore
import org.elnix.dragonlauncher.ui.helpers.customobjects.drawPathGlow
import org.elnix.dragonlauncher.ui.helpers.customobjects.mirrorVertically

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
public fun HoldToActivateArc(
    center: Offset?,
    progress: Float,
    customObject: CustomObject,
    erase: Boolean = false,
    playAnimation: Boolean = true,
) {
    if (center == null || progress <= 0f) return

    val rotationsPerSecond by HoldToActivateArcSettingsStore.rotationsPerSecond.asState()
    val rgbLoading by HoldToActivateArcSettingsStore.holdRgbLoading.asState()
    val holdToActivateSettingsTolerance by HoldToActivateArcSettingsStore.holdToActivateSettingsTolerance.asState()
    val showToleranceOnMainScreen by HoldToActivateArcSettingsStore.showToleranceOnMainScreen.asState()


    val ctx = LocalContext.current
    val extraColors = LocalExtraColors.current

    val color = if (rgbLoading) {
        Color.hsv(progress * 360f, 1f, 1f)
    } else {
        customObject.color ?: extraColors.holdToActivate
    }

    // Remembers for each new click the random or not rotation it applies (if -1)
    val rotationAngleStart = remember(center, customObject.rotation) {
        customObject.rotation.takeIf { it != -1 } ?: (0..360).random()
    }
    // Remembers the shape for each new click, but keeps the same when holding
    val resolvedShape: Shape = remember(center) { customObject.shape.resolveShape() }


    val infiniteTransition = rememberInfiniteTransition(label = "infinite")

    val animationScale = Settings.Global.getFloat(
        ctx.contentResolver,
        Settings.Global.ANIMATOR_DURATION_SCALE,
        1f
    )

    // TODO
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
    val destinationPath = remember { Path() }
    val matrix = remember { Matrix() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawWithCache {
                val diameterPx = customObject.size.dp.toPx() * 2
                val path = resolvedShape.toPath(Size(diameterPx, diameterPx), this)

                matrix.reset()
                matrix.translate(-diameterPx / 2f, -diameterPx / 2f)
                path.transform(matrix)

                pathMeasurer.setPath(path, false)
                val totalLength = pathMeasurer.length
                destinationPath.reset()
                pathMeasurer.getSegment(0f, totalLength * progress, destinationPath)

                onDrawBehind {
                    withTransform({
                        if (customObject.mirror) mirrorVertically(center)

                        // Rotate to start to the angle position chosen
                        rotate(
                            degrees = rotationAngleStart.toFloat(),
                            pivot = center
                        )

                        // Rotates with the animation rotation, computed above
                        if (rotationsPerSecond > 0 && playAnimation) {
                            rotate(degrees = rotationAnimation, pivot = center)
                        }
                        translate(center.x, center.y)
                    }) {
                        drawPathGlow(
                            path = destinationPath,
                            color = color,
                            lineStrokeWidth = customObject.stroke,
                            glow = customObject.glow,
                            erase = erase
                        )
                    }

                    if (showToleranceOnMainScreen) {
                        holdTolerance(center, holdToActivateSettingsTolerance)
                    }
                }
            }
    )
}
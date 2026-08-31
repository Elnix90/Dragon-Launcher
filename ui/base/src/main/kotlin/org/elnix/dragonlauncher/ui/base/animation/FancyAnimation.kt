package org.elnix.dragonlauncher.ui.base.animation

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.toPath
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.RoundedPolygon

data class FancyAnimation(
    val rotation: Float,
    val outerRotation: Float,
    val scale: Float,
    val shape: Shape
)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun rememberFancyAnimations(
    isPressed: Boolean,
    normalShape: RoundedPolygon,
    pressedShape: RoundedPolygon
): FancyAnimation {
    val morph = remember { Morph(start = normalShape, end = pressedShape) }

    val outerRotation by animateFloatAsState(
        targetValue = if (isPressed) 360f else 0f,
        label = "infinite rotation",
        animationSpec =
            if (isPressed) {
                infiniteRepeatable(
                    animation = tween(10000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                )
            } else {
                tween(300)
            }
    )

    val animatedScale by animateFloatAsState(
        targetValue = if (isPressed) 0.8f else 1f,
        label = "scale",
        animationSpec = bouncySpec()
    )

    val animatedRotation by animateFloatAsState(
        targetValue = if (isPressed) 180f else 0f,
        label = "rotation",
        animationSpec = bouncySpec()
    )

    val animatedProgress by animateFloatAsState(
        targetValue = if (isPressed) 1f else 0f,
        label = "progress",
        animationSpec = bouncySpec()
    )

    val shape =
        remember(morph, animatedProgress) {
            MorphPolygonShape(morph, animatedProgress)
        }

    return FancyAnimation(
        rotation = animatedRotation,
        outerRotation = outerRotation,
        scale = animatedScale,
        shape = shape
    )
}

class MorphPolygonShape(
    private val morph: Morph,
    private val percentage: Float
) : Shape {
    private val matrix = Matrix()

    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        matrix.scale(size.width, size.height)
        val path = morph.toPath(progress = percentage)
        path.transform(matrix)
        return Outline.Generic(path)
    }
}

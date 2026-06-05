package org.elnix.dragonlauncher.base

import android.graphics.Matrix
import android.graphics.Path
import android.graphics.RectF
import android.graphics.drawable.AdaptiveIconDrawable
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asComposePath
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.RoundedPolygon
import org.elnix.dragonlauncher.base.model.serializables.IconShape
import org.elnix.dragonlauncher.material.shapes.toShape
import kotlin.math.abs
import kotlin.math.cbrt
import kotlin.math.pow
import kotlin.math.roundToInt


/**
 * A cache object for the computed shapes, in order to allow the access in [androidx.compose.ui.graphics.drawscope.DrawScope],
 *  the cache will have a fixed size of 100, which will hopefully be enough to store all user's shapes
 */
private object ShapesCache : DragonCache<IconShape, Shape>(100)

/**
 * Resolve an [IconShape] element to a [Shape] using caching to avoid over computation
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
fun IconShape?.resolveShape(default: IconShape = IconShape.PlatformDefault): Shape {
    val shapeToResolve = this ?: default

    return ShapesCache.getOrCompute(shapeToResolve) {
        when (shapeToResolve) {
            IconShape.Random -> IconShape.allShapesWithoutRandom.random().resolveShape()
            IconShape.PlatformDefault -> PlatformShape
            IconShape.Pebble -> PebbleShape
            IconShape.Squircle -> SquircleShape
            IconShape.Teardrop -> TeardropShape
            IconShape.EasterEgg -> EasterEggShape

            /** Compute first the [RoundedPolygon] and then use the [toShape] from the library  I copied to convert it to a shape. */
            else -> when (shapeToResolve) {
                IconShape.Circle -> MaterialShapes.Circle
                IconShape.Square -> MaterialShapes.Square
                IconShape.Slanted -> MaterialShapes.Slanted
                IconShape.Arch -> MaterialShapes.Arch
                IconShape.Fan -> MaterialShapes.Fan
                IconShape.Arrow -> MaterialShapes.Arrow
                IconShape.SemiCircle -> MaterialShapes.SemiCircle
                IconShape.Oval -> MaterialShapes.Oval
                IconShape.Pill -> MaterialShapes.Pill
                IconShape.Triangle -> MaterialShapes.Triangle
                IconShape.Diamond -> MaterialShapes.Diamond
                IconShape.ClamShell -> MaterialShapes.ClamShell
                IconShape.Pentagon -> MaterialShapes.Pentagon
                IconShape.Gem -> MaterialShapes.Gem
                IconShape.VerySunny -> MaterialShapes.VerySunny
                IconShape.Sunny -> MaterialShapes.Sunny
                IconShape.Cookie4Sided -> MaterialShapes.Cookie4Sided
                IconShape.Cookie6Sided -> MaterialShapes.Cookie6Sided
                IconShape.Cookie7Sided -> MaterialShapes.Cookie7Sided
                IconShape.Cookie9Sided -> MaterialShapes.Cookie9Sided
                IconShape.Cookie12Sided -> MaterialShapes.Cookie12Sided
                IconShape.Ghostish -> MaterialShapes.Ghostish
                IconShape.Clover4Leaf -> MaterialShapes.Clover4Leaf
                IconShape.Clover8Leaf -> MaterialShapes.Clover8Leaf
                IconShape.Burst -> MaterialShapes.Burst
                IconShape.SoftBurst -> MaterialShapes.SoftBurst
                IconShape.Boom -> MaterialShapes.Boom
                IconShape.SoftBoom -> MaterialShapes.SoftBoom
                IconShape.Flower -> MaterialShapes.Flower
                IconShape.Puffy -> MaterialShapes.Puffy
                IconShape.PuffyDiamond -> MaterialShapes.PuffyDiamond
                IconShape.PixelCircle -> MaterialShapes.PixelCircle
                IconShape.PixelTriangle -> MaterialShapes.PixelTriangle
                IconShape.Bun -> MaterialShapes.Bun
                IconShape.Heart -> MaterialShapes.Heart

                IconShape.Hexagon -> RoundedPolygon(
                    numVertices = 6,
                    radius = 1f,
                    centerX = 0f,
                    centerY = 0f
                )

                IconShape.RoundedSquare -> RoundedPolygon(
                    numVertices = 4,
                    radius = 1f,
                    centerX = 0f,
                    centerY = 0f,
                    rounding = CornerRounding(0.2f),
                )

                is IconShape.Custom -> RoundedPolygon(
                    numVertices = shapeToResolve.numVertices,
                    radius = shapeToResolve.radius,
                    centerX = shapeToResolve.centerX,
                    centerY = shapeToResolve.centerY,
                    rounding = shapeToResolve.rounding,
                    perVertexRounding = shapeToResolve.perVertexRounding
                )
            }.toShape()
        }
    }
}


private val PlatformShape: Shape
    get() {
        val drawable = AdaptiveIconDrawable(null, null)

        val pathBounds = RectF()
        drawable.iconMask.computeBounds(pathBounds, true)

        return GenericShape { size, _ ->
            val path = Path(drawable.iconMask)
            val transformMatrix = Matrix()
            transformMatrix.setScale(
                size.width / pathBounds.width(),
                size.height / pathBounds.height()
            )
            path.transform(transformMatrix)
            addPath(path.asComposePath())
        }
    }

private val SquircleShape: Shape
    get() = GenericShape { size, _ ->
        val radius = size.width / 2f
        val radiusToPow = radius.pow(3f).toDouble()
        moveTo(-radius, 0f)
        for (x in -radius.roundToInt()..radius.roundToInt())
            lineTo(
                x.toFloat(),
                cbrt(radiusToPow - abs(x * x * x)).toFloat()
            )
        for (x in radius.roundToInt() downTo -radius.roundToInt())
            lineTo(
                x.toFloat(),
                (-cbrt(radiusToPow - abs(x * x * x))).toFloat()
            )
        translate(Offset(size.width / 2f, size.height / 2f))
    }

private val TeardropShape: Shape
    get() = GenericShape { size, _ ->
        moveTo(0.5f * size.width, 0f)
        cubicTo(
            0.776f * size.width, 0f,
            size.width, 0.224f * size.height,
            size.width, 0.5f * size.height,
        )
        lineTo(
            size.width, 0.88f * size.height,
        )
        cubicTo(
            size.width, 0.946f * size.height,
            0.946f * size.width, size.height,
            0.88f * size.width, size.height,
        )
        lineTo(0.5f * size.width, size.height)
        cubicTo(
            0.224f * size.width, size.height,
            0f, 0.776f * size.height,
            0f, 0.5f * size.height,
        )
        cubicTo(
            0f, 0.224f * size.height,
            0.224f * size.width, 0f,
            0.5f * size.width, 0f,
        )
        close()
    }

private val PebbleShape: Shape
    get() = GenericShape { size, _ ->
        moveTo(0.55f * size.width, 0f * size.height)
        cubicTo(
            0.25f * size.width,
            0f * size.height,
            0f * size.width,
            0.25f * size.height,
            0f * size.width,
            0.5f * size.height
        )
        cubicTo(
            0f * size.width,
            0.78f * size.height,
            0.28f * size.width,
            1f * size.height,
            0.55f * size.width,
            1f * size.height
        )
        cubicTo(
            0.85f * size.width,
            1f * size.height,
            1f * size.width,
            0.85f * size.height,
            1f * size.width,
            0.58f * size.height
        )
        cubicTo(
            1f * size.width,
            0.3f * size.height,
            0.86f * size.width,
            0f * size.height,
            0.55f * size.width,
            0f * size.height
        )
        close()
    }

private val EasterEggShape: Shape
    get() = GenericShape { size, _ ->
        moveTo(
            0.5f * size.width,
            1f * size.height
        )
        lineTo(
            0.42749998f * size.width,
            0.934f * size.height
        )
        cubicTo(
            0.16999999f * size.width,
            0.7005004f * size.height,
            0f,
            0.5460004f * size.height,
            0f,
            0.3575003f * size.height
        )
        cubicTo(
            0f,
            0.2030004f * size.height,
            0.12100002f * size.width,
            0.0825004f * size.height,
            0.275f * size.width,
            0.0825004f * size.height
        )
        cubicTo(
            0.362f * size.width,
            0.0825004f * size.height,
            0.4455f * size.width,
            0.123f * size.height,
            0.5f * size.width,
            0.1865003f * size.height
        )
        cubicTo(
            0.5545f * size.width,
            0.123f * size.height,
            0.638f * size.width,
            0.0825f * size.height,
            0.725f * size.width,
            0.0825f * size.height
        )
        cubicTo(
            0.87900007f * size.width,
            0.0825004f * size.height,
            1f * size.width,
            0.2030004f * size.height,
            1f * size.width,
            0.3575003f * size.height
        )
        cubicTo(
            1f * size.width,
            0.5460004f * size.height,
            0.83f * size.width,
            0.7005004f * size.height,
            0.5725f * size.width,
            0.9340004f * size.height
        )
        close()
    }

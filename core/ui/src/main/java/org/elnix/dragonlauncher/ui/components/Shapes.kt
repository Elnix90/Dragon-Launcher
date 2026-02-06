package org.elnix.dragonlauncher.ui.components

import android.graphics.Matrix
import android.graphics.Path
import android.graphics.RectF
import android.graphics.drawable.AdaptiveIconDrawable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asComposePath
import org.elnix.dragonlauncher.common.serializables.CornerRadiusSerializable
import org.elnix.dragonlauncher.common.serializables.CustomIconShapeSerializable
import org.elnix.dragonlauncher.common.serializables.IconCornerType
import org.elnix.dragonlauncher.common.serializables.IconShape
import org.elnix.dragonlauncher.common.serializables.allShapes
import org.elnix.dragonlauncher.ui.UiConstants.DragonShape
import kotlin.math.abs
import kotlin.math.cbrt
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt


fun resolveShape(shape: IconShape?): Shape =
    when (val s = shape ?: IconShape.PlatformDefault) {

        IconShape.PlatformDefault -> PlatformShape
        IconShape.Circle -> CircleShape
        IconShape.Square -> RectangleShape
        IconShape.RoundedSquare -> RoundedCornerShape(25)
        IconShape.Dragon -> DragonShape
        IconShape.Triangle -> ReuleauxTriangleShape
        IconShape.Squircle -> SquircleShape
        IconShape.Hexagon -> HexagonShape
        IconShape.Pentagon -> PentagonShape
        IconShape.Teardrop -> TeardropShape
        IconShape.Pebble -> PebbleShape
        IconShape.EasterEgg -> EasterEggShape

        IconShape.Random ->  resolveShape(allShapes.filter { it !is IconShape.Random }.random())

        is IconShape.Custom -> customIconShape(s.shape)
        IconShape.Rhombus -> RhombusShape
        IconShape.RoundedTriangle -> RoundedTriangleShape
        IconShape.SharpTriangle -> SharpTriangleShape
    }


val SharpTriangleShape: Shape = GenericShape { size, _ ->
    moveTo(size.width / 2f, 0f)                 // Top
    lineTo(size.width, size.height)             // Bottom right
    lineTo(0f, size.height)                     // Bottom left
    close()
}

val RoundedTriangleShape: Shape = GenericShape { size, _ ->
    val radius = size.minDimension * 0.25f

    val top = Offset(size.width / 2f, 0f)
    val right = Offset(size.width, size.height)
    val left = Offset(0f, size.height)

    addRoundRect(
        RoundRect(
            rect = Rect(0f, 0f, size.width, size.height),
            topLeft = CornerRadius(radius),
            topRight = CornerRadius(radius),
            bottomLeft = CornerRadius(radius),
            bottomRight = CornerRadius(radius)
        )
    )
}

//val RoundedTriangleShape: Shape
//    get() = GenericShape { size, _ ->
//        RoundedPolygon(
//            numVertices = 3,
//            radius = size.minDimension / 2,
//            centerX = size.width / 2,
//            centerY = size.height / 2,
//            rounding = CornerRounding(
//                size.minDimension / 10f,
//                smoothing = 0.1f
//            )
//        )
//    }


val RhombusShape: Shape = GenericShape { size, _ ->
    moveTo(size.width / 2f, 0f)            // Top
    lineTo(size.width, size.height / 2f)   // Right
    lineTo(size.width / 2f, size.height)   // Bottom
    lineTo(0f, size.height / 2f)           // Left
    close()
}

val ReuleauxTriangleShape: Shape = GenericShape { size, _ ->

    val side = size.minDimension
    val height = side * (sqrt(3f) / 2f)

    // Center vertically
    val verticalOffset = (size.height - height) / 2f

    val p1 = Offset(size.width / 2f, verticalOffset)               // Top
    val p2 = Offset(0f, verticalOffset + height)                   // Bottom left
    val p3 = Offset(size.width, verticalOffset + height)           // Bottom right

    val r = side

    // Arc centered at p2 (from p1 to p3)
    arcTo(
        rect = Rect(
            p2.x - r, p2.y - r,
            p2.x + r, p2.y + r
        ),
        startAngleDegrees = -60f,
        sweepAngleDegrees = 120f,
        forceMoveTo = true
    )

    // Arc centered at p3
    arcTo(
        rect = Rect(
            p3.x - r, p3.y - r,
            p3.x + r, p3.y + r
        ),
        startAngleDegrees = 60f,
        sweepAngleDegrees = 120f,
        forceMoveTo = false
    )

    // Arc centered at p1
    arcTo(
        rect = Rect(
            p1.x - r, p1.y - r,
            p1.x + r, p1.y + r
        ),
        startAngleDegrees = 180f,
        sweepAngleDegrees = 120f,
        forceMoveTo = false
    )

    close()
}


// Shapes from https://github.com/MM2-0/Kvaesitso/blob/main/app/ui/src/main/java/de/mm20/launcher2/ui/component/ShapedLauncherIcon.kt
private val TriangleShape: Shape
    get() = GenericShape { size, _ ->
        var cx = 0f
        var cy = size.height * 0.86f
        val r = size.width
        arcTo(Rect(cx - r, cy - r, cx + r, cy + r), 300f, 60f, false)
        cx = size.width
        cy = size.height * 0.86f
        arcTo(Rect(cx - r, cy - r, cx + r, cy + r), 180f, 60f, false)
        cx = size.width * 0.5f
        cy = 0f
        arcTo(Rect(cx - r, cy - r, cx + r, cy + r), 60f, 60f, false)
        close()
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

private val HexagonShape: Shape
    get() = GenericShape { size, _ ->
        moveTo(
            size.width * 0.25f,
            size.height * 0.933f
        )
        lineTo(
            size.width * 0.75f,
            size.height * 0.933f
        )
        lineTo(
            size.width * 1.0f,
            size.height * 0.5f
        )
        lineTo(
            size.width * 0.75f,
            size.height * 0.067f
        )
        lineTo(
            size.width * 0.25f,
            size.height * 0.067f
        )
        lineTo(0f, size.height * 0.5f)
        close()
    }

private val PentagonShape: Shape
    get() = GenericShape { size, _ ->
        moveTo(
            0.49997026f * size.width,
            0.0060308f * size.height
        )
        lineTo(
            0.9999405f * size.width,
            0.3692805f * size.height
        )
        lineTo(
            0.80896884f * size.width,
            0.9570308f * size.height
        )
        lineTo(
            0.19097161f * size.width,
            0.9570308f * size.height
        )
        lineTo(
            0f,
            0.36928046f * size.height
        )
        close()
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

val PlatformShape: Shape
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

private fun corner(
    path: androidx.compose.ui.graphics.Path,
    x1: Float,
    y1: Float,
    x2: Float,
    y2: Float,
    type: IconCornerType
) {
    when (type) {
        IconCornerType.FILLET ->
            path.quadraticTo(x1, y1, x2, y2)

        IconCornerType.CHAMFER ->
            path.lineTo(x2, y2)
    }
}


fun customIconShape(shape: CustomIconShapeSerializable?): Shape =
    GenericShape { size, _ ->

        val w = size.width
        val h = size.height
        val min = minOf(w, h)

        fun CornerRadiusSerializable?.resolve(): Pair<Float, IconCornerType> {
            val r = ((this?.corner ?: 0f).coerceIn(0f, 1f)) * min * 0.5f
            val t = this?.type ?: IconCornerType.FILLET
            return r to t
        }

        val (tl, tlType) = shape?.topLeft.resolve()
        val (tr, trType) = shape?.topRight.resolve()
        val (br, brType) = shape?.bottomRight.resolve()
        val (bl, blType) = shape?.bottomLeft.resolve()

        // ─── Start top-left ───
        moveTo(tl, 0f)

        // ─── Top edge ───
        lineTo(w - tr, 0f)
        corner(this, w - tr, 0f, w, tr, trType)

        // ─── Right edge ───
        lineTo(w, h - br)
        corner(this, w, h - br, w - br, h, brType)

        // ─── Bottom edge ───
        lineTo(bl, h)
        corner(this, bl, h, 0f, h - bl, blType)

        // ─── Left edge ───
        lineTo(0f, tl)
        corner(this, 0f, tl, tl, 0f, tlType)

        close()
    }

package org.elnix.dragonlauncher.common.messyfolder

import android.graphics.Matrix
import android.graphics.Path
import android.graphics.RectF
import android.graphics.drawable.AdaptiveIconDrawable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asComposePath
import org.elnix.dragonlauncher.common.serializables.CornerRadius
import org.elnix.dragonlauncher.common.serializables.CustomIconShape
import org.elnix.dragonlauncher.common.serializables.IconCornerType
import org.elnix.dragonlauncher.common.serializables.IconShape
import org.elnix.dragonlauncher.common.serializables.allShapes
import kotlin.math.abs
import kotlin.math.cbrt
import kotlin.math.pow
import kotlin.math.roundToInt


fun IconShape?.resolveShape(default: IconShape = IconShape.PlatformDefault): Shape =
    when (val s = this ?: default) {

        IconShape.PlatformDefault -> PlatformShape
        IconShape.Circle -> CircleShape
        IconShape.Square -> RectangleShape
        IconShape.RoundedSquare, IconShape.Dragon -> RoundedCornerShape(25)
        IconShape.Triangle -> TriangleShape
        IconShape.Squircle -> SquircleShape
        IconShape.Hexagon -> HexagonShape
        IconShape.Pentagon -> PentagonShape
        IconShape.Teardrop -> TeardropShape
        IconShape.Pebble -> PebbleShape
        IconShape.EasterEgg -> EasterEggShape


        // So funny, I'm proud of it actually
        IconShape.Random ->  (allShapes.filter { it !is IconShape.Random }.random()).resolveShape()

        is IconShape.Custom -> customIconShape(s.shape)
    }


// Some shapes from https://github.com/MM2-0/Kvaesitso/blob/main/app/ui/src/main/java/de/mm20/launcher2/ui/component/ShapedLauncherIcon.kt
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
        IconCornerType.Fillet ->
            path.quadraticTo(x1, y1, x2, y2)

        IconCornerType.Chamfer ->
            path.lineTo(x2, y2)
    }
}


fun customIconShape(shape: CustomIconShape?): Shape =
    GenericShape { size, _ ->

        val w = size.width
        val h = size.height
        val min = minOf(w, h)

        fun CornerRadius?.resolve(): Pair<Float, IconCornerType> {
            val r = ((this?.corner ?: 0f).coerceIn(0f, 1f)) * min * 0.5f
            val t = this?.type ?: IconCornerType.Fillet
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

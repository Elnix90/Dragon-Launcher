package org.elnix.dragonlauncher.ktx

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import kotlin.math.abs
import kotlin.random.Random

/**
 * Returns this [Color] only if it is explicitly defined.
 *
 * If the receiver is `null` or equal to [Color.Companion.Unspecified], this returns `null`.
 * Otherwise, it returns the receiver unchanged.
 *
 * This is useful when treating [Color.Companion.Unspecified] as an absent value
 * and normalizing it to `null` for clearer nullable handling.
 *
 * @return this color if defined, or `null` if it is `null` or `Color.Unspecified`
 */
public fun Color?.definedOrNull(): Color? =
    this.takeIf { it != Color.Unspecified }


/**
 * Returns a copy of this [Color] with its alpha multiplied by [multiplier].
 *
 * The RGB components remain unchanged. The resulting alpha is computed as:
 * `currentAlpha * multiplier`.
 *
 * This can be used to uniformly increase or decrease transparency while
 * preserving the original opacity proportion.
 *
 * @param multiplier factor applied to the current alpha value
 * @return a copy of this color with the adjusted alpha
 */
public fun Color.alphaMultiplier(multiplier: Float): Color =
    copy(alpha = alpha * multiplier)

/**
 * Returns this [Color], reducing its alpha by half when [enabled] is false.
 *
 * If [enabled] is true, the color is returned unchanged.
 * If false, the resulting color keeps the same RGB components and
 * multiplies the current alpha by `0.5f`.
 *
 * @param enabled whether the color should remain fully effective
 * @return this color, or a version with its alpha halved when disabled
 */
public fun Color.semiTransparentIfDisabled(enabled: Boolean): Color =
    if (enabled) this else alphaMultiplier(0.5f)


/** Utility: convert Color -> #AARRGGBB */
public inline val Color.toHexWithAlpha: String
    get() = "#%08X".format(this.toArgb())


public fun randomColor(
    minLuminance: Float = 0f,
    maxLuminance: Float = 1f,
    alpha: Boolean = false
): Color {
    val hue = Random.nextFloat() * 360f
    val saturation = 1f
    val value = Random.nextFloat() * (maxLuminance - minLuminance) + minLuminance

    // Convert HSV to RGB
    val c = value * saturation
    val x = c * (1 - abs((hue / 60f) % 2 - 1))
    val m = value - c

    val (r1, g1, b1) = when (hue) {
        in 0f..60f -> Triple(c, x, 0f)
        in 60f..120f -> Triple(x, c, 0f)
        in 120f..180f -> Triple(0f, c, x)
        in 180f..240f -> Triple(0f, x, c)
        in 240f..300f -> Triple(x, 0f, c)
        else -> Triple(c, 0f, x)
    }

    val r = r1 + m
    val g = g1 + m
    val b = b1 + m

    return Color(r, g, b, if (alpha) Random.nextFloat() else 1f)
}
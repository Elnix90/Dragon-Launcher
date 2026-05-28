package org.elnix.dragonlauncher.base.icons

import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes

sealed interface LauncherIconLayer

data class StaticIconLayer(
    val icon: Drawable,
    val scale: Float = 1f,
) : LauncherIconLayer

data class ColorLayer(
    val color: Int = 0,
) : LauncherIconLayer

data class ClockLayer(
    val sublayers: List<ClockSublayer>,
    val defaultHour: Int = 0,
    val defaultMinute: Int = 0,
    val defaultSecond: Int = 0,
    val scale: Float,
) : LauncherIconLayer

data class ClockSublayer(
    val drawable: Drawable,
    val role: ClockSublayerRole
)

enum class ClockSublayerRole {
    Hour,
    Minute,
    Second,
    Static,
}

data class TintedIconLayer(
    val icon: Drawable,
    val scale: Float = 0.5f,
    val color: Int = 0
) : LauncherIconLayer

data class TintedClockLayer(
    val sublayers: List<ClockSublayer>,
    val defaultHour: Int = 0,
    val defaultMinute: Int = 0,
    val defaultSecond: Int = 0,
    val scale: Float,
    val color: Int = 0,
) : LauncherIconLayer

data class TextLayer(
    val text: String,
    val color: Int = 0,
) : LauncherIconLayer


data class VectorLayer(
    @param:DrawableRes val icon: Int,
    val color: Int = 0,
) : LauncherIconLayer

object TransparentLayer: LauncherIconLayer
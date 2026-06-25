package org.elnix.dragonlauncher.base.icons

import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes

sealed interface LauncherIconLayer{
    val tint: Int?
}

data class ColorLayer(
    override val tint: Int = 0,
) : LauncherIconLayer

data class StaticIconLayer(
    val icon: Drawable,
    val scale: Float,
    override val tint: Int?
) : LauncherIconLayer

data class ClockLayer(
    val sublayers: List<ClockSublayer>,
    val defaultHour: Int = 0,
    val defaultMinute: Int = 0,
    val defaultSecond: Int = 0,
    val scale: Float,
    override val tint: Int? = null,
) : LauncherIconLayer

data class TextLayer(
    val text: String,
    override val tint: Int? = null,
) : LauncherIconLayer


data class VectorLayer(
    @param:DrawableRes val icon: Int,
    override val tint: Int? = null,
) : LauncherIconLayer

object TransparentLayer: LauncherIconLayer {
    override val tint: Int = 0
}



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
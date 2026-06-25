package org.elnix.dragonlauncher.base.icons

import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes

public sealed interface LauncherIconLayer{
    public val tint: Int?
}

public data class ColorLayer(
    override val tint: Int = 0,
) : LauncherIconLayer

public data class StaticIconLayer(
    val icon: Drawable,
    val scale: Float,
    override val tint: Int?
) : LauncherIconLayer

public data class ClockLayer(
    val sublayers: List<ClockSublayer>,
    val defaultHour: Int = 0,
    val defaultMinute: Int = 0,
    val defaultSecond: Int = 0,
    val scale: Float,
    override val tint: Int? = null,
) : LauncherIconLayer

public data class TextLayer(
    val text: String,
    override val tint: Int? = null,
) : LauncherIconLayer


public data class VectorLayer(
    @param:DrawableRes val icon: Int,
    override val tint: Int? = null,
) : LauncherIconLayer

public object TransparentLayer: LauncherIconLayer {
    override val tint: Int = 0
}



public data class ClockSublayer(
    val drawable: Drawable,
    val role: ClockSublayerRole
)

public enum class ClockSublayerRole {
    Hour,
    Minute,
    Second,
    Static,
}
package org.elnix.dragonlauncher.enumsui.select

import org.elnix.dragonlauncher.enumsui.SelectButtonOption
import org.elnix.dragonlauncher.i18n.R

public enum class AngleObject(
    override val resId: Int,
    override val iconResId: Int? = null
) : SelectButtonOption {
    Line(R.string.line),
    Angle(R.string.angle),
    Start(R.string.start),
    End(R.string.end)
}

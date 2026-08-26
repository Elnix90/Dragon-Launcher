package org.elnix.dragonlauncher.base.model.enumsui.select

import org.elnix.dragonlauncher.base.model.enumsui.SelectButtonOption
import org.elnix.dragonlauncher.i18n.R

public enum class AngleObject(
    override val resId: Int,
    override val iconResId: Int? = null
) : org.elnix.dragonlauncher.base.model.enumsui.SelectButtonOption {
    Line(R.string.line),
    Angle(R.string.angle),
    Start(R.string.start),
    End(R.string.end)
}

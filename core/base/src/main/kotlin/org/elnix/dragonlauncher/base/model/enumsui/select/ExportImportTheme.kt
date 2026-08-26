package org.elnix.dragonlauncher.base.model.enumsui.select

import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.base.model.enumsui.SelectButtonOption

public enum class ExportImportTheme(
    override val resId: Int,
    override val iconResId: Int? = null
) : org.elnix.dragonlauncher.base.model.enumsui.SelectButtonOption {
    Export(R.string.export, R.drawable.share),
    Import(R.string.import_text, R.drawable.download)
}

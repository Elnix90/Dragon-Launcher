package org.elnix.dragonlauncher.base.model.enumsui.select

import org.elnix.dragonlauncher.base.model.enumsui.SelectButtonOption
import org.elnix.dragonlauncher.i18n.R

public enum class ExportImportTheme(
    override val resId: Int,
    override val iconResId: Int? = null
) : SelectButtonOption {
    Export(R.string.export, R.drawable.share),
    Import(R.string.import_text, R.drawable.download)
}

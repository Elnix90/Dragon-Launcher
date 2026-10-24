package org.elnix.dragonlauncher.base.model.enumsui.select

import io.github.elnix90.shared.ui.base.model.SelectButtonOption
import org.elnix.dragonlauncher.i18n.R

enum class ExportImportTheme(
	override val resId: Int,
	override val iconResId: Int? = null
) : SelectButtonOption {
	Export(R.string.export, R.drawable.share),
	Import(R.string.import_text, R.drawable.download)
}

package org.elnix.dragonlauncher.fonts

import android.content.Context
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import org.elnix.dragonlauncher.base.theme.GoogleSansFlex
import org.elnix.dragonlauncher.base.theme.JetBrainsMono
import java.io.File

fun fontNameToFont(name: String, ctx: Context): FontFamily {
	if (name.isBlank()) return FontFamily.Default

	val cleanName = name.substringBefore(" (")
	val base =
		when (cleanName) {
			"Serif" -> FontFamily.Serif
			"SansSerif" -> FontFamily.SansSerif
			"Monospace" -> FontFamily.Monospace
			"Cursive" -> FontFamily.Cursive
			"System" -> FontFamily.Default
			"Default" -> GoogleSansFlex
			"JetBrains Mono" -> JetBrainsMono
			else -> null
		}
	if (base != null) return base
	try {
		val extDir = File(ctx.getExternalFilesDir(null), "fonts")
		val ttf = File(extDir, "$cleanName.ttf")
		val otf = File(extDir, "$cleanName.otf")
		val fontFile =
			when {
				ttf.exists() -> ttf
				otf.exists() -> otf
				else -> null
			}
		return if (fontFile != null) {
			FontFamily(Font(fontFile))
		} else {
			GoogleSansFlex
		}
	} catch (_: Exception) {
		return GoogleSansFlex
	}
}

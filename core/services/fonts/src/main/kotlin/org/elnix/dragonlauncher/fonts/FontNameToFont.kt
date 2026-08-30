package org.elnix.dragonlauncher.fonts

import android.content.Context
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import java.io.File

public fun fontNameToFont(name: String, ctx: Context? = null): FontFamily {
    if (name.isBlank()) return FontFamily.Default

    val cleanName = name.substringBefore(" (")
    val base =
        when (cleanName) {
            "Serif" -> FontFamily.Serif
            "SansSerif" -> FontFamily.SansSerif
            "Monospace" -> FontFamily.Monospace
            "Cursive" -> FontFamily.Cursive
            "Default" -> FontFamily.Default
            else -> null
        }
    if (base != null) return base
    if (ctx != null) {
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
            if (fontFile != null) {
                return FontFamily(Font(fontFile))
            }
        } catch (_: Exception) {
        }
    }
    return FontFamily.Default
}

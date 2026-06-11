package org.elnix.dragonlauncher.fonts

import android.content.Context
import androidx.compose.ui.text.font.FontFamily
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.elnix.dragonlauncher.logging.SETTINGS_TAG
import org.elnix.dragonlauncher.logging.logWtf
import org.elnix.dragonlauncher.settings.stores.map.UiSettingsStore


interface FontService {
    val fontFamily: Flow<FontFamily>
}

internal class FontServiceImpl(
    private val ctx: Context
) : FontService {
    private val globalFontNameFlow = UiSettingsStore.globalFont.flow(ctx)

    override val fontFamily: Flow<FontFamily> = globalFontNameFlow.map { fontName ->
        logWtf(SETTINGS_TAG) { "FontName: $fontName" }
        fontNameToFont(fontName, ctx)
    }
}
package org.elnix.dragonlauncher.ui.composition

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import org.elnix.dragonlauncher.base.model.serializables.MainScreenLayer

public val LocalMainScreenLayers: ProvidableCompositionLocal<List<MainScreenLayer>> = compositionLocalOf {
    error("No LocalMainScreenLayers provided")
}
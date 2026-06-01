package org.elnix.dragonlauncher.ui.composition

import androidx.compose.runtime.compositionLocalOf
import org.elnix.dragonlauncher.base.model.serializables.MainScreenLayer

val LocalMainScreenLayers = compositionLocalOf<List<MainScreenLayer>> {
    error("No LocalMainScreenLayers provided")
}
package org.elnix.dragonlauncher.ui.composition

import androidx.compose.runtime.compositionLocalOf
import org.elnix.dragonlauncher.base.model.serializables.StatusBar

val LocalStatusBarElements = compositionLocalOf<List<StatusBar>> {
    error("No status bar elements provided")
}
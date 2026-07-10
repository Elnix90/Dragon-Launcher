package org.elnix.dragonlauncher.ui.composition

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import org.elnix.dragonlauncher.base.model.serializables.StatusBar

public val LocalStatusBarElements: ProvidableCompositionLocal<List<StatusBar>> = compositionLocalOf {
    error("No status bar elements provided")
}
package org.elnix.dragonlauncher.ui.composition

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf

val LocalNestDebugOverlay: ProvidableCompositionLocal<Boolean> = compositionLocalOf { error("no LocalNestDebugOverlay provided") }

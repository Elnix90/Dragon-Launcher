package org.elnix.dragonlauncher.ui.composition

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf

val LocalSettingsPlacementChecker: ProvidableCompositionLocal<Unit> = compositionLocalOf { error("This setting MUST be placed inside a DragonSettingGroup") }

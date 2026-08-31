package org.elnix.dragonlauncher.ui.composition

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import org.elnix.dragonlauncher.base.model.enumsui.select.ColorPickerMode

val LocalColorPickerMode: ProvidableCompositionLocal<org.elnix.dragonlauncher.base.model.enumsui.select.ColorPickerMode> =
    compositionLocalOf {
        error("No LocalColorPickerMode provided")
    }

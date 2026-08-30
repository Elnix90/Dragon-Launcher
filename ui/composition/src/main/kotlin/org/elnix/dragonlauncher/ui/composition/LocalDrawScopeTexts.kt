package org.elnix.dragonlauncher.ui.composition

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.text.TextMeasurer

val LocalTextMeasurer: ProvidableCompositionLocal<TextMeasurer> =
    compositionLocalOf {
        error("No LocalTextMeasurer provided")
    }

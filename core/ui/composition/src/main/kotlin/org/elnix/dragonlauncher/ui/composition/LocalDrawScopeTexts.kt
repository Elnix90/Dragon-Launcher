package org.elnix.dragonlauncher.ui.composition

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle

public val LocalTextMeasurer: ProvidableCompositionLocal<TextMeasurer> = compositionLocalOf {
    error("No LocalTextMeasurer provided")
}
public val LocalPointTextStyle: ProvidableCompositionLocal<TextStyle> = compositionLocalOf {
    error("No LocalTextStyle provided")
}
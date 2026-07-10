package org.elnix.dragonlauncher.ui.base.remember

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
public fun rememberInteractionSource(): MutableInteractionSource {
    return remember { MutableInteractionSource() }
}
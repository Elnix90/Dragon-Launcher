package org.elnix.dragonlauncher.ui.base.modifiers

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer

fun Modifier.semiTransparentIfDisabled(enabled: Boolean): Modifier =
    if (enabled) this else this.graphicsLayer {
        alpha *= 0.5f
    }

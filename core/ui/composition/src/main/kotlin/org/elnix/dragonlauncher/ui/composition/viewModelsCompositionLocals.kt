package org.elnix.dragonlauncher.ui.composition

import androidx.compose.runtime.compositionLocalOf
import org.elnix.dragonlauncher.models.WidgetsViewModel


val LocalWidgetsViewModel = compositionLocalOf<WidgetsViewModel> {
    error("No FloatingAppsViewModel bar provided")
}

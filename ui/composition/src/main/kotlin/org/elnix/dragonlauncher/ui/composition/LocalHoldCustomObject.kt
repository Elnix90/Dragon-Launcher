package org.elnix.dragonlauncher.ui.composition

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import org.elnix.dragonlauncher.base.model.serializables.CustomObject


val LocalHoldCustomObject: ProvidableCompositionLocal<CustomObject> = compositionLocalOf {
    error("No LocalHoldCustomObject provided")
}

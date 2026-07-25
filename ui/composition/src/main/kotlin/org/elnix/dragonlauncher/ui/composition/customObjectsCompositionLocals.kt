package org.elnix.dragonlauncher.ui.composition

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import org.elnix.dragonlauncher.base.model.serializables.CustomObject

val LocalLineObject: ProvidableCompositionLocal<CustomObject> = compositionLocalOf {
    error("No LocalLine provided")
}

val LocalAngleLineObject: ProvidableCompositionLocal<CustomObject> = compositionLocalOf {
    error("No LocalAngleLine provided")
}

val LocalStartLineObject: ProvidableCompositionLocal<CustomObject> = compositionLocalOf {
    error("No LocalStartLine provided")
}

val LocalEndLineObject: ProvidableCompositionLocal<CustomObject> = compositionLocalOf {
    error("No LocalEndLine provided")
}

val LocalHoldCustomObject: ProvidableCompositionLocal<CustomObject> = compositionLocalOf {
    error("No LocalHoldCustomObject provided")
}

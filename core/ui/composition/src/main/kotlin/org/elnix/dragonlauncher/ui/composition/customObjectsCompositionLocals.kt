package org.elnix.dragonlauncher.ui.composition

import androidx.compose.runtime.compositionLocalOf
import org.elnix.dragonlauncher.common.serializables.CustomObject

val LocalLineObject = compositionLocalOf<CustomObject> {
    error("No LocalLine provided")
}

val LocalAngleLineObject = compositionLocalOf<CustomObject> {
    error("No LocalAngleLine provided")
}

val LocalStartLineObject = compositionLocalOf<CustomObject> {
    error("No LocalStartLine provided")
}

val LocalEndLineObject = compositionLocalOf<CustomObject> {
    error("No LocalEndLine provided")
}

val LocalHoldCustomObject = compositionLocalOf<CustomObject> {
    error("No LocalHoldCustomObject provided")
}

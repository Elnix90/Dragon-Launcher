package org.elnix.dragonlauncher.ui.composition

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import org.elnix.dragonlauncher.base.model.serializables.CustomObject

public val LocalLineObject: ProvidableCompositionLocal<CustomObject> = compositionLocalOf {
    error("No LocalLine provided")
}

public val LocalAngleLineObject: ProvidableCompositionLocal<CustomObject> = compositionLocalOf {
    error("No LocalAngleLine provided")
}

public val LocalStartLineObject: ProvidableCompositionLocal<CustomObject> = compositionLocalOf {
    error("No LocalStartLine provided")
}

public val LocalEndLineObject: ProvidableCompositionLocal<CustomObject> = compositionLocalOf {
    error("No LocalEndLine provided")
}

public val LocalHoldCustomObject: ProvidableCompositionLocal<CustomObject> = compositionLocalOf {
    error("No LocalHoldCustomObject provided")
}

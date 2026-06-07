package org.elnix.dragonlauncher.ui.composition

import androidx.compose.runtime.compositionLocalOf
import org.elnix.dragonlauncher.base.model.serializables.IconShape

val LocalIconShape = compositionLocalOf<IconShape> { error("No iconShape provided") }
val LocalGridSize = compositionLocalOf<Int> { error("No grid settings provided") }
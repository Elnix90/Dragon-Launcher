package org.elnix.dragonlauncher.ui.composition

import androidx.compose.runtime.compositionLocalOf
import org.elnix.dragonlauncher.base.model.serializables.IconShape

val LocalIconShape = compositionLocalOf<IconShape> { error("No iconShape Provided") }
val LocalIconSize = compositionLocalOf<Int> { error("No IconSize provided") }
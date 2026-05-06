package org.elnix.dragonlauncher.ui.composition

import androidx.compose.runtime.compositionLocalOf
import org.elnix.dragonlauncher.common.serializables.CircleNest
import org.elnix.dragonlauncher.common.serializables.SwipePointSerializable

val LocalNests = compositionLocalOf<List<CircleNest>> { error("No nests provided") }
val LocalPoints = compositionLocalOf<List<SwipePointSerializable>> { error("No points provided") }
val LocalDefaultPoint = compositionLocalOf<SwipePointSerializable> { error("No default point provided") }


package org.elnix.dragonlauncher.ui.composition

import androidx.compose.runtime.compositionLocalOf
import org.elnix.dragonlauncher.common.serializables.Nest
import org.elnix.dragonlauncher.common.serializables.Point

val LocalNests = compositionLocalOf<List<Nest>> { error("No nests provided") }
val LocalPoints = compositionLocalOf<List<Point>> { error("No points provided") }
val LocalDefaultPoint = compositionLocalOf<Point> { error("No default point provided") }


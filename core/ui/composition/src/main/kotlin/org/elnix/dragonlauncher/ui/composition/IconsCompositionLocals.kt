package org.elnix.dragonlauncher.ui.composition

import androidx.compose.runtime.compositionLocalOf
import org.elnix.dragonlauncher.common.serializables.CacheKey
import org.elnix.dragonlauncher.common.serializables.IconShape
import org.elnix.dragonlauncher.common.messyfolder.IconsCache

val LocalDrawerIconsCache = compositionLocalOf<IconsCache<CacheKey>> { error("No drawer icons provided") }
val LocalPointIconsCache = compositionLocalOf<IconsCache<String>> { error("No points icons provided") }
val LocalIconShape = compositionLocalOf<IconShape> { error("No iconShape Provided") }
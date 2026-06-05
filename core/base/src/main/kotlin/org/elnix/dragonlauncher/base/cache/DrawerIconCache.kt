package org.elnix.dragonlauncher.base.cache

import org.elnix.dragonlauncher.base.DragonCache
import org.elnix.dragonlauncher.base.icons.LauncherIcon
import org.elnix.dragonlauncher.base.model.serializables.CacheKey

class DrawerIconCache(initialMaxSize: Int) : DragonCache<CacheKey, LauncherIcon>(initialMaxSize)

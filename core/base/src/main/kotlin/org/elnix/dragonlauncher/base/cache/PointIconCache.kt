package org.elnix.dragonlauncher.base.cache

import androidx.compose.ui.graphics.ImageBitmap
import org.elnix.dragonlauncher.base.DragonCache
import org.elnix.dragonlauncher.base.model.serializables.CacheKey

class PointIconCache(initialMaxSize: Int) : DragonCache<CacheKey, ImageBitmap>(initialMaxSize)

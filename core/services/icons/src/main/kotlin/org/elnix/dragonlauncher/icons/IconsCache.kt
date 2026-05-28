package org.elnix.dragonlauncher.icons

import androidx.compose.ui.graphics.ImageBitmap
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.elnix.dragonlauncher.base.icons.LauncherIcon
import org.elnix.dragonlauncher.common.serializables.CacheKey
import org.elnix.dragonlauncher.logging.ICONS_TAG
import org.elnix.dragonlauncher.logging.logD
import java.util.Collections
import java.util.UUID

abstract class DragonCache <K,V> (initialMaxSize: Int) {
    private var maxSize = initialMaxSize

    val cacheUUID: UUID = UUID.randomUUID()

    private val _iconsTrigger = MutableStateFlow(0)
    val iconsTrigger = _iconsTrigger.asStateFlow()

    /**
     * Updates the maximum number of cached entries.
     * Call this whenever the number of points changes to keep the cache sized to the working set.
     *
     * @param newSize The new maximum entry count.
     */
    fun updateMaxCacheSize(newSize: Int) {
        maxSize = newSize
    }

    fun incrementCacheSize(increment: Int = 1) {
        maxSize += increment
    }

    private val icons = Collections.synchronizedMap(
        object : LinkedHashMap<K, V>(
            maxSize, 0.75f, true
        ) {
            override fun removeEldestEntry(
                eldest: MutableMap.MutableEntry<K, V>
            ) = size > maxSize
        }
    )


    operator fun get(cacheKey: K): V? = icons[cacheKey]


    /**
     * Get or compute, used in the [org.elnix.dragonlauncher.common.utils.ImageUtils] object, to directly compute the icon when it is not in the cache.
     * It's the most direct method, as if the icon isn't found in the list, it is updated real time
     *
     * @param cacheKey the icon it of type [V]
     * @param compute the function block used to compute the icon, it returns [androidx.compose.ui.graphics.ImageBitmap]
     * @return [androidx.compose.ui.graphics.ImageBitmap] the actual icon, not null
     */
    fun getOrCompute(
        cacheKey: K,
        compute: () -> V
    ): V =
        icons.getOrPut(cacheKey) {
            _iconsTrigger.update { it + 1 }
            compute()
        }

    /**
     * Get or lazy compute, used in the `AppPreviewTitle` Composable, to delegate computing to the viewmodel,
     * when it can't directly load the icon, with the scope it has
     *
     * @param id the icon it of type [V]
     * @param compute the function block used to compute the icon, it returns [androidx.compose.ui.graphics.ImageBitmap]
     * @return [androidx.compose.ui.graphics.ImageBitmap] the actual icon, not null
     */
    fun getOrLazyCompute(
        cacheKey: K,
        compute: () -> Unit
    ): V? {
        val result = icons[cacheKey]
        if (result == null) {
            compute()
            logD(ICONS_TAG) { "Failed to get icon for $cacheKey. Computing it lazily\ncacheUUID: $cacheUUID\nmaxSize: $maxSize, size: $size" }
        }
        return result
    }

    /*** Compute simply a new icon, don't return it */
    fun compute(cacheKey: K, compute: () -> V) {
        _iconsTrigger.update { it + 1 }
        icons[cacheKey] = compute()
    }

    fun getRandom(): K? = if (icons.isNotEmpty()) {
        icons.keys.random()
    } else null

    fun evict(cacheKey: K) {
        icons.remove(cacheKey)
    }

    fun evictAll() {
        icons.clear()
    }

    /**
     * The current number of entries held in the cache.
     * Useful for debugging or logging cache pressure.
     */
    val size: Int = icons.size
}

class IconsCache(initialMaxSize: Int) : DragonCache<CacheKey, LauncherIcon>(initialMaxSize)
class PointIconCache(initialMaxSize: Int) : DragonCache<CacheKey, ImageBitmap>(initialMaxSize)

package org.elnix.dragonlauncher.common.utils

import androidx.compose.ui.graphics.ImageBitmap
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.elnix.dragonlauncher.common.utils.Constants.Logging.ICONS_TAG
import org.elnix.dragonlauncher.logging.logD
import java.util.Collections
import java.util.UUID


class IconsCache<T>(initialMaxSize: Int) {

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

    private val icons = Collections.synchronizedMap(
        object : LinkedHashMap<T, ImageBitmap>(
            maxSize, 0.75f, true
        ) {
            override fun removeEldestEntry(
                eldest: MutableMap.MutableEntry<T, ImageBitmap>
            ) = size > maxSize
        }
    )

    /**
     * Get or compute, used in the [ImageUtils] object, to directly compute the icon when it is not in the cache.
     * It's the most direct method, as if the icon isn't found in the list, it is updated real time
     *
     * @param id the icon it of type [T]
     * @param compute the function block used to compute the icon, it returns [ImageBitmap]
     * @return [ImageBitmap] the actual icon, not null
     */
    fun getOrCompute(
        id: T,
        compute: () -> ImageBitmap
    ): ImageBitmap =
        icons.getOrPut(id) {
            _iconsTrigger.update { it + 1 }
            compute()
        }

    /**
     * Get or lazy compute, used in the `AppPreviewTitle` Composable, to delegate computing to the viewmodel,
     * when it can't directly load the icon, with the scope it has
     *
     * @param id the icon it of type [T]
     * @param compute the function block used to compute the icon, it returns [ImageBitmap]
     * @return [ImageBitmap] the actual icon, not null
     */
    fun getOrLazyCompute(
        id: T,
        compute: () -> Unit
    ): ImageBitmap? {
        val result = icons[id]
        if (result == null) {
            compute()
            logD(ICONS_TAG) { "Failed to get icon for $id. Computing it lazily\ncacheUUID: $cacheUUID\nmaxSize: $maxSize, size: $size" }
        }
        return result
    }

    /*** Compute simply a new icon, don't return it */
    fun compute(id: T, compute: () -> ImageBitmap) {
        _iconsTrigger.update { it + 1 }
        icons[id] = compute()
    }

    fun getRandom(): T? = if (icons.isNotEmpty()) {
        icons.keys.random()
    } else null

    /**
     * The current number of entries held in the cache.
     * Useful for debugging or logging cache pressure.
     */
    val size: Int
        get() = icons.size
}
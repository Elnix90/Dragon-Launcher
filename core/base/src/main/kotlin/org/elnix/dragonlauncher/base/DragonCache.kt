package org.elnix.dragonlauncher.base

import io.github.elnix90.logging.ICONS_TAG
import io.github.elnix90.logging.logD
import java.util.Collections
import java.util.UUID

public abstract class DragonCache <K,V> (initialMaxSize: Int) {
    private var maxSize = initialMaxSize
    public val cacheUUID: UUID = UUID.randomUUID()

    public val cacheTrigger: SettingFlow<Int> = SettingFlow(0)

    /**
     * Updates the maximum number of cached entries.
     * Call this whenever the number of points changes to keep the cache sized to the working set.
     *
     * @param newSize The new maximum entry count.
     */
    public fun updateMaxCacheSize(newSize: Int) {
        maxSize = newSize
    }

    public fun incrementCacheSize(increment: Int = 1) {
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


    public operator fun get(key: K): V? = icons[key]


    /**
     * Get or compute, used in the [org.elnix.dragonlauncher.base.util.ImageUtils] object, to directly compute [V] when it is not in the cache.
     * It's the most direct method, as if the [V] isn't found in the list, it is updated real time
     *
     * @param key the object it of type [V]
     * @param compute the function block used to compute the object, it returns [androidx.compose.ui.graphics.ImageBitmap]
     * @return [androidx.compose.ui.graphics.ImageBitmap] the actual object, not null
     */
    public fun getOrCompute(
        key: K,
        compute: () -> V
    ): V =
        icons.getOrPut(key) {
            cacheTrigger.update { it + 1 }
            compute()
        }

    /**
     * Get or lazy compute, used in the `AppPreviewTitle` Composable, to delegate computing to the viewmodel,
     * when it can't directly load the object, with the scope it has
     *
     * @param compute the function block used to compute the object, it returns [androidx.compose.ui.graphics.ImageBitmap]
     * @return [androidx.compose.ui.graphics.ImageBitmap] the actual object, not null
     */
    public fun getOrLazyCompute(
        key: K,
        compute: () -> Unit
    ): V? {
        val result = icons[key]
        if (result == null) {
            compute()
            logD(ICONS_TAG) { "Failed to get object for $key. Computing it lazily\ncacheUUID: $cacheUUID\nmaxSize: $maxSize, size: $size" }
        }
        return result
    }

    /** Compute simply a new object, don't return it */
    public fun compute(key: K, compute: () -> V) {
        cacheTrigger.update { it + 1 }
        icons[key] = compute()
    }

    public fun getRandom(): K? = if (icons.isNotEmpty()) {
        icons.keys.random()
    } else null

    public fun evict(key: K) {
        icons.remove(key)
    }

    public fun evictAll() {
        icons.clear()
    }

    /**
     * The current number of entries held in the cache.
     * Useful for debugging or logging cache pressure.
     */
    public val size: Int = icons.size
}
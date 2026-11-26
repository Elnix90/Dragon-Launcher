package org.elnix.dragonlauncher.base

import io.github.elnix90.shared.base.SettingFlow

/** LRU cache bounded by [initialMaxSize] units; entries count as one unit unless [sizeOf] is supplied. */
abstract class DragonCache<K, V>(
	initialMaxSize: Int,
	private val sizeOf: (K, V) -> Int = { _, _ -> 1 }
) {
	private var maxSize = initialMaxSize.coerceAtLeast(0)
	private var currentSize = 0L

	val cacheTrigger: SettingFlow<Int> = SettingFlow(0)

	/**
	 * Updates the maximum cached size, in [sizeOf] units (entries by default).
	 *
	 * @param newSize The new maximum size.
	 */
	fun updateMaxCacheSize(newSize: Int) {
		synchronized(items) {
			maxSize = newSize.coerceAtLeast(0)
			trimToSize()
		}
	}

	private val items = LinkedHashMap<K, V>(16, 0.75f, true)

	operator fun get(key: K): V? = synchronized(items) { items[key] }

	/**
	 * Get or compute, used in the [org.elnix.dragonlauncher.base.util.ImageUtils] object, to directly compute [V] when it is not in the cache.
	 * It's the most direct method, as if the [V] isn't found in the list, it is updated real time
	 *
	 * @param key the object it of type [V]
	 * @param compute the function block used to compute the object, it returns [androidx.compose.ui.graphics.ImageBitmap]
	 * @return [androidx.compose.ui.graphics.ImageBitmap] the actual object, not null
	 */
	fun getOrCompute(
		key: K,
		compute: () -> V
	): V {
		synchronized(items) { items[key]?.let { return it } }

		val value = compute()
		synchronized(items) {
			items[key]?.let { return it }
			put(key, value)
			cacheTrigger.update { it + 1 }
		}
		return value
	}

	/** Compute simply a new object, don't return it */
	fun compute(key: K, compute: () -> V) {
		val value = compute()
		synchronized(items) {
			put(key, value)
			cacheTrigger.update { it + 1 }
		}
	}

	fun evict(key: K) {
		synchronized(items) {
			if (items.containsKey(key)) {
				currentSize -= weight(key, items.getValue(key))
				items.remove(key)
			}
		}
	}

	fun evictAll() {
		synchronized(items) {
			items.clear()
			currentSize = 0L
		}
	}

	/**
	 * The current number of entries held in the cache.
	 * Useful for debugging or logging cache pressure.
	 */
	val size: Int get() = synchronized(items) { items.size }

	private fun put(key: K, value: V) {
		if (items.containsKey(key)) currentSize -= weight(key, items.getValue(key))
		items[key] = value
		currentSize += weight(key, value)
		trimToSize()
	}

	private fun trimToSize() {
		val iterator = items.entries.iterator()
		while (currentSize > maxSize && iterator.hasNext()) {
			val entry = iterator.next()
			currentSize -= weight(entry.key, entry.value)
			iterator.remove()
		}
	}

	private fun weight(key: K, value: V): Long = sizeOf(key, value).coerceAtLeast(1).toLong()
}

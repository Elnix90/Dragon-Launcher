package org.elnix.dragonlauncher.base

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DragonCacheTest {
	private class TestCache(
		maxSize: Int
	) : DragonCache<Int, String>(maxSize)

	@Test
	fun `shrinking cache immediately evicts least recently used entries`() {
		val cache = TestCache(3)
		cache.compute(1) { "one" }
		cache.compute(2) { "two" }
		cache.compute(3) { "three" }
		cache[1]

		cache.updateMaxCacheSize(1)

		assertEquals(1, cache.size)
		assertEquals("one", cache[1])
		assertNull(cache[2])
		assertNull(cache[3])
	}

	@Test
	fun `zero capacity evicts existing and future entries`() {
		val cache = TestCache(1)
		cache.compute(1) { "one" }

		cache.updateMaxCacheSize(0)
		cache.compute(2) { "two" }

		assertEquals(0, cache.size)
		assertNull(cache[1])
		assertNull(cache[2])
	}
}

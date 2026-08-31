package org.elnix.dragonlauncher.ktx

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for [Set.getNextId].
 *
 * [getNextId] returns the smallest non-negative [Int] not present in the set.
 * This is used to generate sequential IDs for lists/collections where items
 * may be removed, creating gaps in the ID sequence.
 */
class SetTest {
    //  Empty set

    @Test
    fun `getNextId on empty set returns 0`() {
        val ids = emptySet<Int>()
        assertEquals(0, ids.getNextId())
    }

    //  Contiguous sequences starting from 0

    @Test
    fun `getNextId on set containing only 0 returns 1`() {
        val ids = setOf(0)
        assertEquals(1, ids.getNextId())
    }

    @Test
    fun `getNextId on set containing 0 and 1 returns 2`() {
        val ids = setOf(0, 1)
        assertEquals(2, ids.getNextId())
    }

    @Test
    fun `getNextId on contiguous set 0,1,2,3 returns 4`() {
        val ids = setOf(0, 1, 2, 3)
        assertEquals(4, ids.getNextId())
    }

    //  Gaps in the sequence

    @Test
    fun `getNextId fills gap when 0 is missing`() {
        val ids = setOf(1, 2, 3)
        assertEquals(0, ids.getNextId())
    }

    @Test
    fun `getNextId fills gap when 1 is missing`() {
        val ids = setOf(0, 2, 3)
        assertEquals(1, ids.getNextId())
    }

    @Test
    fun `getNextId fills gap in middle of sequence`() {
        val ids = setOf(0, 1, 3, 4)
        assertEquals(2, ids.getNextId())
    }

    @Test
    fun `getNextId fills first gap even with large numbers present`() {
        val ids = setOf(5, 6, 7, 100)
        assertEquals(0, ids.getNextId())
    }

    //  Non-contiguous / scattered values

    @Test
    fun `getNextId with only large numbers returns 0`() {
        val ids = setOf(10, 20, 30)
        assertEquals(0, ids.getNextId())
    }

    @Test
    fun `getNextId with 0 and 2 returns 1`() {
        val ids = setOf(0, 2)
        assertEquals(1, ids.getNextId())
    }

    //  Negative IDs (edge case)

    @Test
    fun `getNextId ignores negative IDs and returns 0`() {
        // The function starts at 0 and increments, so negative IDs in the set
        // should not affect the result.
        val ids = setOf(-1, -2, -3)
        assertEquals(0, ids.getNextId())
    }

    @Test
    fun `getNextId with negatives and 0 present returns 1`() {
        val ids = setOf(-5, -1, 0)
        assertEquals(1, ids.getNextId())
    }

    //  Large sets

    @Test
    fun `getNextId with 1000 contiguous IDs returns 1000`() {
        val ids = (0..999).toSet()
        assertEquals(1000, ids.getNextId())
    }
}

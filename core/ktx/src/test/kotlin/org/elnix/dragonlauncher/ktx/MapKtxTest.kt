package org.elnix.dragonlauncher.ktx

import android.util.ArraySet
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Unit tests for [groupByTo] extension on [Iterable].
 *
 * [groupByTo] groups elements by a key selector and collects them into
 * [MutableSet] values backed by [ArraySet] (Android's memory-efficient set).
 * This requires Robolectric because [ArraySet] is an Android framework class.
 *
 * This is an optimized alternative to Kotlin's stdlib [groupBy] when you
 * need sets instead of lists and want to avoid allocations from boxed integers.
 */
@RunWith(RobolectricTestRunner::class)
class MapKtxTest {
    //  Basic grouping

    @Test
    fun `groupByTo groups elements by key`() {
        val items = listOf("apple", "avocado", "banana", "blueberry", "cherry")
        val destination = mutableMapOf<Char, MutableSet<String>>()

        items.groupByTo(destination) { it.first() }

        assertEquals(setOf("apple", "avocado"), destination['a'])
        assertEquals(setOf("banana", "blueberry"), destination['b'])
        assertEquals(setOf("cherry"), destination['c'])
    }

    @Test
    fun `groupByTo all elements same key`() {
        val items = listOf(1, 2, 3, 4, 5)
        val destination = mutableMapOf<Int, MutableSet<Int>>()

        items.groupByTo(destination) { 42 }

        assertEquals(1, destination.size)
        assertEquals(setOf(1, 2, 3, 4, 5), destination[42])
    }

    //  Empty input

    @Test
    fun `groupByTo on empty list returns empty destination`() {
        val items = emptyList<String>()
        val destination = mutableMapOf<Char, MutableSet<String>>()

        items.groupByTo(destination) { it.first() }

        assertTrue(destination.isEmpty())
    }

    //  Uses ArraySet (Android-specific behavior)

    @Test
    fun `groupByTo creates sets that are ArraySet instances`() {
        val items = listOf(1, 2, 3)
        val destination = mutableMapOf<String, MutableSet<Int>>()

        items.groupByTo(destination) { "key" }

        // Verify it's an ArraySet, not a HashSet or LinkedHashSet
        val set = destination["key"]!!
        assertTrue(
            "Expected ArraySet but got ${set::class.simpleName}",
            set is ArraySet
        )
    }

    //  Duplicates within same key group

    @Test
    fun `groupByTo deduplicates within sets`() {
        // ArraySet only stores unique values
        val items = listOf(1, 1, 2, 2, 3, 3)
        val destination = mutableMapOf<String, MutableSet<Int>>()

        items.groupByTo(destination) { if (it > 1) "big" else "small" }

        assertEquals(setOf(1), destination["small"])
        assertEquals(setOf(2, 3), destination["big"])
    }

    //  Returns destination map

    @Test
    fun `groupByTo returns the destination map`() {
        val items = listOf("a" to 1, "b" to 2)
        val destination = mutableMapOf<String, MutableSet<Pair<String, Int>>>()

        val result = items.groupByTo(destination) { it.first }

        assertEquals(destination, result)
    }
}

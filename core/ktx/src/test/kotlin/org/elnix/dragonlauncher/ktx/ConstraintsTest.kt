package org.elnix.dragonlauncher.ktx

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.Constraints
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for [Constraints.getCenter].
 *
 * [getCenter] returns the center point of the constraints as an [Offset],
 * computed from [Constraints.maxWidth] and [Constraints.maxHeight].
 */
class ConstraintsTest {
    private companion object {
        const val EPSILON = 1e-3f
    }

    @Test
    fun `getCenter returns midpoint of 1080x1920`() {
        val constraints = Constraints.fixed(1080, 1920)
        val center = constraints.getCenter()
        assertEquals(540f, center.x, EPSILON)
        assertEquals(960f, center.y, EPSILON)
    }

    @Test
    fun `getCenter of square`() {
        val constraints = Constraints.fixed(100, 100)
        val center = constraints.getCenter()
        assertEquals(50f, center.x, EPSILON)
        assertEquals(50f, center.y, EPSILON)
    }

    @Test
    fun `getCenter of 1x1`() {
        val constraints = Constraints.fixed(1, 1)
        val center = constraints.getCenter()
        assertEquals(0.5f, center.x, EPSILON)
        assertEquals(0.5f, center.y, EPSILON)
    }
}

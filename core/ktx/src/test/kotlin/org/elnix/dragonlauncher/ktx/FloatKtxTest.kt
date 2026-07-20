package org.elnix.dragonlauncher.ktx

import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.math.PI

/**
 * Unit tests for Float extension functions and properties in [org.elnix.dragonlauncher.ktx].
 *
 * Covers:
 * - [Float.getToRadians]: degrees -> radians conversion
 * - [PI]: π constant as a Float
 * - [TWO_PI]: 2π constant as a Float
 */
class FloatKtxTest {

    private companion object {
        /** Tolerance for floating-point comparisons. */
        const val EPSILON = 1e-5f
    }

    //  toRadians 

    @Test
    fun `0 degrees converts to 0 radians`() {
        assertEquals(0f, 0f.radians.toFloat(), EPSILON)
    }

    @Test
    fun `90 degrees converts to PI over 2`() {
        val expected = (PI / 2.0).toFloat()
        assertEquals(expected, 90f.radians.toFloat(), EPSILON)
    }

    @Test
    fun `180 degrees converts to PI`() {
        assertEquals(PI.toFloat(), 180f.radians.toFloat(), EPSILON)
    }

    @Test
    fun `360 degrees converts to 2 PI`() {
        val expected = (2.0 * PI).toFloat()
        assertEquals(expected, 360f.radians.toFloat(), EPSILON)
    }

    @Test
    fun `45 degrees converts to PI over 4`() {
        val expected = (PI / 4.0).toFloat()
        assertEquals(expected, 45f.radians.toFloat(), EPSILON)
    }

    @Test
    fun `270 degrees converts to 3 PI over 2`() {
        val expected = (3.0 * PI / 2.0).toFloat()
        assertEquals(expected, 270f.radians.toFloat(), EPSILON)
    }
}

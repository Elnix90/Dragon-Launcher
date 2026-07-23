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


    @Test
    fun `test rounding to 0 decimals`() {
        assertEquals(3f, 3.14159f.round(0))
        assertEquals(5f, 4.5f.round(0))
        assertEquals(-2f, (-2.499f).round(0))
    }

    @Test
    fun `test rounding to 1 decimal`() {
        assertEquals(3.1f, 3.14159f.round(1))
        assertEquals(4.5f, 4.54f.round(1))
        assertEquals(2.3f, 2.25f.round(1))
    }

    @Test
    fun `test rounding to 2 decimals`() {
        assertEquals(3.14f, 3.14159f.round(2))
        assertEquals(4.55f, 4.549f.round(2))
        assertEquals(2.25f, 2.25f.round(2))
    }

    @Test
    fun `test rounding to 3 decimals`() {
        assertEquals(3.142f, 3.14159f.round(3))
        assertEquals(4.549f, 4.5485f.round(3))
        assertEquals(2.250f, 2.25f.round(3))
    }

    @Test
    fun `test rounding negative numbers`() {
        assertEquals(-3.14f, (-3.14159f).round(2))
        assertEquals(-4.55f, (-4.549f).round(2))
        assertEquals(-2.0f, (-2.001f).round(0))
    }

    @Test
    fun `test rounding with large decimals`() {
        assertEquals(3.1415927f, 3.1415927f.round(8))
        assertEquals(0.12345678f, 0.12345678f.round(8))
    }

    @Test
    fun `test rounding with zero`() {
        assertEquals(0f, 0f.round(2))
        assertEquals(0f, 0.001f.round(0))
    }

    @Test
    fun `test rounding with very small numbers`() {
        assertEquals(0.001f, 0.001499f.round(3))
        assertEquals(0.002f, 0.0015f.round(3))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `test rounding with invalid decimals (negative)`() {
        3.14f.round(-1)
    }
}

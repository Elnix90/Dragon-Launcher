package org.elnix.dragonlauncher.ktx

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for [Double.normalizeAngle].
 *
 * [normalizeAngle] maps any angle (in degrees) into the half-open range [0, 360).
 * Negative angles wrap around, and angles >= 360 are reduced modulo 360.
 */
class DoubleNormalizeAngleTest {

    private companion object {
        /** Tolerance for floating-point comparisons. */
        const val EPSILON = 1e-9
    }

    //  Values already in range [0, 360)

    @Test
    fun `0 degrees stays 0`() {
        assertEquals(0.0, 0.0.normalizeAngle(), EPSILON)
    }

    @Test
    fun `90 degrees stays 90`() {
        assertEquals(90.0, 90.0.normalizeAngle(), EPSILON)
    }

    @Test
    fun `180 degrees stays 180`() {
        assertEquals(180.0, 180.0.normalizeAngle(), EPSILON)
    }

    @Test
    fun `359 point 9 degrees stays as is`() {
        assertEquals(359.9, 359.9.normalizeAngle(), EPSILON)
    }

    //  Boundary: exactly 360 

    @Test
    fun `360 degrees wraps to 0`() {
        assertEquals(0.0, 360.0.normalizeAngle(), EPSILON)
    }

    @Test
    fun `720 degrees wraps to 0`() {
        assertEquals(0.0, 720.0.normalizeAngle(), EPSILON)
    }

    //  Angles greater than 360 

    @Test
    fun `450 degrees normalizes to 90`() {
        assertEquals(90.0, 450.0.normalizeAngle(), EPSILON)
    }

    @Test
    fun `810 degrees normalizes to 90`() {
        // 810 = 2 * 360 + 90
        assertEquals(90.0, 810.0.normalizeAngle(), EPSILON)
    }

    //  Negative angles 

    @Test
    fun `negative 90 degrees normalizes to 270`() {
        assertEquals(270.0, (-90.0).normalizeAngle(), EPSILON)
    }

    @Test
    fun `negative 180 degrees normalizes to 180`() {
        assertEquals(180.0, (-180.0).normalizeAngle(), EPSILON)
    }

    @Test
    fun `negative 360 degrees normalizes to 0`() {
        assertEquals(0.0, (-360.0).normalizeAngle(), EPSILON)
    }

    @Test
    fun `negative 1 degree normalizes to 359`() {
        assertEquals(359.0, (-1.0).normalizeAngle(), EPSILON)
    }

    @Test
    fun `large negative angle normalizes correctly`() {
        // -720 = -2 * 360, should normalize to 0
        assertEquals(0.0, (-720.0).normalizeAngle(), EPSILON)
    }

    @Test
    fun `negative 450 normalizes to 270`() {
        // -450 = -360 - 90, so mod gives -90, then +360 = 270
        assertEquals(270.0, (-450.0).normalizeAngle(), EPSILON)
    }

    //  Fractional angles 

    @Test
    fun `fractional positive angle within range`() {
        assertEquals(45.5, 45.5.normalizeAngle(), EPSILON)
    }

    @Test
    fun `fractional negative angle`() {
        assertEquals(359.5, (-0.5).normalizeAngle(), EPSILON)
    }
}

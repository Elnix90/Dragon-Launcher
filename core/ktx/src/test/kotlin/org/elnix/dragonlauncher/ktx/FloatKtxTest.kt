package org.elnix.dragonlauncher.ktx

import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.math.PI

/**
 * Unit tests for Float extension functions and properties in [org.elnix.dragonlauncher.ktx]
 */
class FloatKtxTest {
    private companion object {
        /** Tolerance for floating-point comparisons. */
        const val EPSILON: Double = 1e-5
        const val EPSILON_FLOAT: Float = 1e-5f
    }

    @Test
    fun `0 degrees converts to 0 radians`() {
        assertEquals(0.0, 0f.radians, EPSILON)
    }

    @Test
    fun `90 degrees converts to PI over 2`() {
        assertEquals((PI / 2.0), 90f.radians, EPSILON)
    }

    @Test
    fun `180 degrees converts to PI`() {
        assertEquals(PI, 180f.radians, EPSILON)
    }

    @Test
    fun `360 degrees converts to 2 PI`() {
        assertEquals((2.0 * PI), 360f.radians, EPSILON)
    }

    @Test
    fun `45 degrees converts to PI over 4`() {
        assertEquals((PI / 4.0), 45f.radians, EPSILON)
    }

    @Test
    fun `270 degrees converts to 3 PI over 2`() {
        assertEquals((3.0 * PI / 2.0), 270f.radians, EPSILON)
    }

    @Test
    fun `0 radians converts to 0 degrees`() {
        assertEquals(0.0, 0f.degrees, EPSILON)
    }

    @Test
    fun `PI over 2 radians converts to 90 degrees`() {
        assertEquals(90.0, (PI / 2.0).degrees, EPSILON)
    }

    @Test
    fun `PI radians converts to 180 degrees`() {
        assertEquals(180.0, PI.toFloat().degrees, EPSILON)
    }

    @Test
    fun `2 PI radians converts to 360 degrees`() {
        assertEquals(360.0, (2.0 * PI).degrees, EPSILON)
    }

    @Test
    fun `PI over 4 radians converts to 45 degrees`() {
        val input = (PI / 4.0).toFloat()
        assertEquals(45.0, input.degrees, EPSILON)
    }

    @Test
    fun `3 PI over 2 radians converts to 270 degrees`() {
        val input = (3.0 * PI / 2.0).toFloat()
        assertEquals(270.0, input.degrees, EPSILON)
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

    @Test
    fun `converts 0_0 to 0`() {
        assertEquals(0, 0f.to255)
    }

    @Test
    fun `converts 1_0 to 256`() {
        assertEquals(255, 1f.to255)
    }

    @Test
    fun `converts 0_5 to 128`() {
        assertEquals(128, 0.5f.to255)
    }

    @Test
    fun `converts 0_25 to 64`() {
        assertEquals(64, 0.25f.to255)
    }

    @Test
    fun `converts 0_75 to 192`() {
        assertEquals(191, 0.75f.to255)
    }

    @Test
    fun `handles fractional values 0_1`() {
        assertEquals(26, 0.1f.to255)
    }

    @Test
    fun `handles fractional values 0_3`() {
        assertEquals(77, 0.3f.to255)
    }

    @Test
    fun `handles fractional values 0_7`() {
        assertEquals(179, 0.7f.to255)
    }

    @Test
    fun `handles fractional values 0_9`() {
        assertEquals(230, 0.9f.to255)
    }

    @Test
    fun `handles precision loss gracefully`() {
        assertEquals(254, 0.99609375f.to255)
    }

    @Test
    fun `truncates decimal parts`() {
        assertEquals(26, 0.1f.to255)
        assertEquals(77, 0.3f.to255)
    }

    @Test
    fun `handles values greater than 1_0`() {
        assertEquals(255, 2f.to255)
        assertEquals(255, 3f.to255)
    }

    @Test
    fun `handles negative values`() {
        assertEquals(0, (-1f).to255)
        assertEquals(0, (-0.5f).to255)
    }

    @Test
    fun `handles very small values`() {
        assertEquals(0, 0.001f.to255)
    }

    @Test
    fun `handles very large values`() {
        assertEquals(255, 100f.to255)
    }

    @Test
    fun `behavior matches Float times and toInt chain`() {
        val testValue = 0.42f
        val expected = (testValue * 256).toInt()
        assertEquals(expected, testValue.to255)
    }

    @Test
    fun `snapToRound snaps when within threshold`() {
        assertEquals(5f, 4.9f.snapToRound(5f, 0.2f), EPSILON_FLOAT)
    }

    @Test
    fun `snapToRound does not snap when beyond threshold`() {
        assertEquals(4.5f, 4.5f.snapToRound(5f, 0.2f), EPSILON_FLOAT)
    }

    @Test
    fun `snapToRound snaps exactly at snapTo value`() {
        assertEquals(5f, 5f.snapToRound(5f, 0.1f), EPSILON_FLOAT)
    }

    @Test
    fun `snapToRound snaps at lower boundary`() {
        assertEquals(5f, 4.8f.snapToRound(5f, 0.2f), EPSILON_FLOAT)
    }

    @Test
    fun `snapToRound snaps at upper boundary`() {
        assertEquals(5f, 5.2f.snapToRound(5f, 0.2f), EPSILON_FLOAT)
    }

    @Test
    fun `snapToGrid rounds positive value up`() {
        val result = 27f.snapToGrid(10f)
        assertEquals(30f, result, EPSILON_FLOAT)
    }

    @Test
    fun `snapToGrid rounds positive value down`() {
        val result = 23f.snapToGrid(10f)
        assertEquals(20f, result, EPSILON_FLOAT)
    }

    @Test
    fun `snapToGrid rounds negative value up`() {
        val result = (-23f).snapToGrid(10f)
        assertEquals(-20f, result, EPSILON_FLOAT)
    }

    @Test
    fun `snapToGrid rounds negative value down`() {
        val result = (-27f).snapToGrid(10f)
        assertEquals(-30f, result, EPSILON_FLOAT)
    }

    @Test
    fun `snapToGrid value already aligned returns unchanged`() {
        val result = 30f.snapToGrid(10f)
        assertEquals(30f, result, EPSILON_FLOAT)
    }

    @Test
    fun `snapToGrid zero value`() {
        val result = 0f.snapToGrid(10f)
        assertEquals(0f, result, EPSILON_FLOAT)
    }

    @Test
    fun `snapToGrid small grid size`() {
        val result = 1.7f.snapToGrid(1f)
        assertEquals(2f, result, EPSILON_FLOAT)
    }

    @Test
    fun `snapToGrid fractional grid size`() {
        val result = 5.6f.snapToGrid(2.5f)
        assertEquals(5f, result, EPSILON_FLOAT)
    }

    @Test
    fun `snapToGrid very small value with large grid`() {
        val result = 2f.snapToGrid(100f)
        assertEquals(0f, result, EPSILON_FLOAT)
    }

    @Test
    fun `snapToGrid rounds to nearest at midpoint`() {
        val result = 15f.snapToGrid(10f)
        assertEquals(20f, result, EPSILON_FLOAT)
    }

    @Test
    fun `snapToGrid large negative value`() {
        val result = (-999f).snapToGrid(100f)
        assertEquals(-1000f, result, EPSILON_FLOAT)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `snapToGrid zero grid size throws`() {
        5f.snapToGrid(0f)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `snapToGrid negative grid size throws`() {
        5f.snapToGrid(-10f)
    }
}

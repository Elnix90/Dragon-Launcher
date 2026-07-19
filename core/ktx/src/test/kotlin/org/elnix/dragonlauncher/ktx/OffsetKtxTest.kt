package org.elnix.dragonlauncher.ktx

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.sqrt

/**
 * Unit tests for Offset extension functions in [org.elnix.dragonlauncher.ktx].
 *
 * These functions implement core geometric math used throughout the launcher's
 * touch-drag and transformation pipeline:
 * - Distance calculations: [distanceTo], [distanceSquaredTo]
 * - Angle calculations: [angleDeg], [angleRad], [angle360FromOffset]
 * - Rotation: [rotateBy]
 * - Full transform pipeline: [applyTransformations], [undoTransformations]
 * - Snapping: [snapToRound] (both Float and Offset variants)
 * - Zone detection: [isInsideActiveZone]
 * - Display: [cleanString]
 *
 * NOTE: Floating-point tests use a tolerance (epsilon) because trigonometric
 * operations and coordinate math inherently produce tiny rounding errors.
 */
class OffsetKtxTest {

    private companion object {
        const val EPSILON = 1e-3f
        const val DEG_EPSILON = 0.5f
    }


    //  distanceTo


    @Test
    fun `distanceTo same point is 0`() {
        val a = Offset(5f, 5f)
        assertEquals(0f, a distanceTo a, EPSILON)
    }

    @Test
    fun `distanceTo horizontal`() {
        val a = Offset(0f, 0f)
        val b = Offset(3f, 0f)
        assertEquals(3f, a distanceTo b, EPSILON)
    }

    @Test
    fun `distanceTo vertical`() {
        val a = Offset(0f, 0f)
        val b = Offset(0f, 4f)
        assertEquals(4f, a distanceTo b, EPSILON)
    }

    @Test
    fun `distanceTo 3-4-5 triangle`() {
        val a = Offset(0f, 0f)
        val b = Offset(3f, 4f)
        assertEquals(5f, a distanceTo b, EPSILON)
    }

    @Test
    fun `distanceTo is symmetric`() {
        val a = Offset(1f, 2f)
        val b = Offset(4f, 6f)
        assertEquals(a distanceTo b, b distanceTo a, EPSILON)
    }

    @Test
    fun `distanceTo with negative coordinates`() {
        val a = Offset(-3f, -4f)
        val b = Offset(0f, 0f)
        assertEquals(5f, a distanceTo b, EPSILON)
    }


    //  distanceSquaredTo


    @Test
    fun `distanceSquaredTo same point is 0`() {
        val a = Offset(5f, 5f)
        assertEquals(0f, a distanceSquaredTo a, EPSILON)
    }

    @Test
    fun `distanceSquaredTo 3-4-5 triangle returns 25`() {
        val a = Offset(0f, 0f)
        val b = Offset(3f, 4f)
        assertEquals(25f, a distanceSquaredTo b, EPSILON)
    }

    @Test
    fun `distanceSquaredTo is symmetric`() {
        val a = Offset(1f, 2f)
        val b = Offset(4f, 6f)
        assertEquals(a distanceSquaredTo b, b distanceSquaredTo a, EPSILON)
    }


    //  angleDeg


    @Test
    fun `angleDeg right (positive x) is 0`() {
        assertEquals(0f, Offset(1f, 0f).angleDeg(), DEG_EPSILON)
    }

    @Test
    fun `angleDeg up (positive y) is 90`() {
        assertEquals(90f, Offset(0f, 1f).angleDeg(), DEG_EPSILON)
    }

    @Test
    fun `angleDeg left (negative x) is 180`() {
        assertEquals(180f, Offset(-1f, 0f).angleDeg(), DEG_EPSILON)
    }

    @Test
    fun `angleDeg down (negative y) is 270`() {
        assertEquals(270f, Offset(0f, -1f).angleDeg(), DEG_EPSILON)
    }

    @Test
    fun `angleDeg origin is 0`() {
        // atan2(0, 0) = 0, so angleDeg should be 0
        assertEquals(0f, Offset(0f, 0f).angleDeg(), DEG_EPSILON)
    }


    //  angle360FromOffset


    @Test
    fun `angle360FromOffset north is 0`() {
        val center = Offset(100f, 100f)
        val offset = Offset(100f, 0f) // above center
        assertEquals(0f, angle360FromOffset(center, offset), DEG_EPSILON)
    }

    @Test
    fun `angle360FromOffset east is 90`() {
        val center = Offset(100f, 100f)
        val offset = Offset(200f, 100f) // right of center
        assertEquals(90f, angle360FromOffset(center, offset), DEG_EPSILON)
    }

    @Test
    fun `angle360FromOffset south is 180`() {
        val center = Offset(100f, 100f)
        val offset = Offset(100f, 200f) // below center
        assertEquals(180f, angle360FromOffset(center, offset), DEG_EPSILON)
    }

    @Test
    fun `angle360FromOffset west is 270`() {
        val center = Offset(100f, 100f)
        val offset = Offset(0f, 100f) // left of center
        assertEquals(270f, angle360FromOffset(center, offset), DEG_EPSILON)
    }

    @Test
    fun `angle360FromOffset same point is 0`() {
        val center = Offset(50f, 50f)
        assertEquals(0f, angle360FromOffset(center, center), DEG_EPSILON)
    }


    //  rotateBy


    @Test
    fun `rotateBy 0 returns same offset`() {
        val offset = Offset(10f, 20f)
        val rotated = offset.rotateBy(0f)
        assertEquals(offset.x, rotated.x, EPSILON)
        assertEquals(offset.y, rotated.y, EPSILON)
    }

    @Test
    fun `rotateBy 360 returns same offset`() {
        val offset = Offset(10f, 20f)
        val rotated = offset.rotateBy(360f)
        assertEquals(offset.x, rotated.x, EPSILON)
        assertEquals(offset.y, rotated.y, EPSILON)
    }

    @Test
    fun `rotateBy 90 degrees counterclockwise`() {
        // (1, 0) rotated 90° CCW -> (0, 1)
        val offset = Offset(1f, 0f)
        val rotated = offset.rotateBy(90f)
        assertEquals(0f, rotated.x, EPSILON)
        assertEquals(1f, rotated.y, EPSILON)
    }

    @Test
    fun `rotateBy -90 degrees (clockwise)`() {
        // (1, 0) rotated 90° CW -> (0, -1)
        val offset = Offset(1f, 0f)
        val rotated = offset.rotateBy(-90f)
        assertEquals(0f, rotated.x, EPSILON)
        assertEquals(-1f, rotated.y, EPSILON)
    }

    @Test
    fun `rotateBy 180 degrees flips both components`() {
        val offset = Offset(3f, 4f)
        val rotated = offset.rotateBy(180f)
        assertEquals(-3f, rotated.x, EPSILON)
        assertEquals(-4f, rotated.y, EPSILON)
    }

    @Test
    fun `rotateBy preserves magnitude`() {
        val offset = Offset(3f, 4f)
        val expectedMagnitude = sqrt(3f * 3f + 4f * 4f)
        val rotated = offset.rotateBy(45f)
        val actualMagnitude = sqrt(rotated.x * rotated.x + rotated.y * rotated.y)
        assertEquals(expectedMagnitude, actualMagnitude, EPSILON)
    }


    //  applyTransformations & undoTransformations


    @Test
    fun `applyTransformations then undoTransformations is identity`() {
        val original = Offset(100f, 200f)
        val zoom = 2f
        val offset = Offset(50f, 30f)
        val angle = 45f

        val transformed = original.applyTransformations(zoom, offset, angle)
        val restored = transformed.undoTransformations(angle, zoom, offset)

        assertEquals(original.x, restored.x, EPSILON)
        assertEquals(original.y, restored.y, EPSILON)
    }

    @Test
    fun `applyTransformations with zero zoom and offset and angle is identity`() {
        val original = Offset(10f, 20f)
        val transformed = original.applyTransformations(1f, Offset.Zero, 0f)
        assertEquals(original.x, transformed.x, EPSILON)
        assertEquals(original.y, transformed.y, EPSILON)
    }

    @Test
    fun `applyTransformations only zoom scales correctly`() {
        val original = Offset(10f, 20f)
        val transformed = original.applyTransformations(2f, Offset.Zero, 0f)
        // div(2) then plus(0) then rotateBy(0) -> original / 2
        assertEquals(5f, transformed.x, EPSILON)
        assertEquals(10f, transformed.y, EPSILON)
    }


    //  snapToRound (Float extension)


    @Test
    fun `snapToRound snaps when within threshold`() {
        assertEquals(5f, 4.9f.snapToRound(5f, 0.2f), EPSILON)
    }

    @Test
    fun `snapToRound does not snap when beyond threshold`() {
        assertEquals(4.5f, 4.5f.snapToRound(5f, 0.2f), EPSILON)
    }

    @Test
    fun `snapToRound snaps exactly at snapTo value`() {
        assertEquals(5f, 5f.snapToRound(5f, 0.1f), EPSILON)
    }

    @Test
    fun `snapToRound snaps at lower boundary`() {
        assertEquals(5f, 4.8f.snapToRound(5f, 0.2f), EPSILON)
    }

    @Test
    fun `snapToRound snaps at upper boundary`() {
        assertEquals(5f, 5.2f.snapToRound(5f, 0.2f), EPSILON)
    }


    //  snapToRound (Offset extension)


    @Test
    fun `Offset snapToRound snaps both axes`() {
        val result = Offset(4.9f, 3.1f).snapToRound(Offset(5f, 3f), 0.2f)
        assertEquals(5f, result.x, EPSILON)
        assertEquals(3f, result.y, EPSILON)
    }

    @Test
    fun `Offset snapToRound only snaps x when y is beyond threshold`() {
        val result = Offset(4.9f, 10f).snapToRound(Offset(5f, 3f), 0.2f)
        assertEquals(5f, result.x, EPSILON)
        assertEquals(10f, result.y, EPSILON)
    }


    //  isInsideActiveZone


    @Test
    fun `isInsideActiveZone center of screen is inside`() {
        val offset = Offset(540f, 960f) // center of 1080x1920
        val size = IntSize(1080, 1920)
        assertTrue(offset.isInsideActiveZone(size, left = 50, right = 50, top = 50, bottom = 50))
    }

    @Test
    fun `isInsideActiveZone point at exact left boundary is inside`() {
        // left=50 means x must be >= 50
        val offset = Offset(50f, 500f)
        val size = IntSize(1080, 1920)
        assertTrue(offset.isInsideActiveZone(size, left = 50, right = 50, top = 50, bottom = 50))
    }

    @Test
    fun `isInsideActiveZone point just inside left boundary is inside`() {
        val offset = Offset(51f, 500f)
        val size = IntSize(1080, 1920)
        assertTrue(offset.isInsideActiveZone(size, left = 50, right = 50, top = 50, bottom = 50))
    }

    @Test
    fun `isInsideActiveZone point outside left boundary is outside`() {
        val offset = Offset(49f, 500f)
        val size = IntSize(1080, 1920)
        assertFalse(offset.isInsideActiveZone(size, left = 50, right = 50, top = 50, bottom = 50))
    }

    @Test
    fun `isInsideActiveZone point at exact right boundary is inside`() {
        // right=50 means x <= 1080 - 50 = 1030
        val offset = Offset(1030f, 500f)
        val size = IntSize(1080, 1920)
        assertTrue(offset.isInsideActiveZone(size, left = 50, right = 50, top = 50, bottom = 50))
    }

    @Test
    fun `isInsideActiveZone point outside right boundary is outside`() {
        val offset = Offset(1031f, 500f)
        val size = IntSize(1080, 1920)
        assertFalse(offset.isInsideActiveZone(size, left = 50, right = 50, top = 50, bottom = 50))
    }

    @Test
    fun `isInsideActiveZone point at exact top boundary is inside`() {
        val offset = Offset(500f, 50f)
        val size = IntSize(1080, 1920)
        assertTrue(offset.isInsideActiveZone(size, left = 50, right = 50, top = 50, bottom = 50))
    }

    @Test
    fun `isInsideActiveZone point outside top boundary is outside`() {
        val offset = Offset(500f, 49f)
        val size = IntSize(1080, 1920)
        assertFalse(offset.isInsideActiveZone(size, left = 50, right = 50, top = 50, bottom = 50))
    }

    @Test
    fun `isInsideActiveZone point at exact bottom boundary is inside`() {
        // bottom=50 means y <= 1920 - 50 = 1870
        val offset = Offset(500f, 1870f)
        val size = IntSize(1080, 1920)
        assertTrue(offset.isInsideActiveZone(size, left = 50, right = 50, top = 50, bottom = 50))
    }

    @Test
    fun `isInsideActiveZone point outside bottom boundary is outside`() {
        val offset = Offset(500f, 1871f)
        val size = IntSize(1080, 1920)
        assertFalse(offset.isInsideActiveZone(size, left = 50, right = 50, top = 50, bottom = 50))
    }

    @Test
    fun `isInsideActiveZone with zero padding means entire screen is inside`() {
        val offset = Offset(0f, 0f)
        val size = IntSize(1080, 1920)
        assertTrue(offset.isInsideActiveZone(size, left = 0, right = 0, top = 0, bottom = 0))
    }

    @Test
    fun `isInsideActiveZone origin is outside with non-zero padding`() {
        val offset = Offset(0f, 0f)
        val size = IntSize(1080, 1920)
        assertFalse(offset.isInsideActiveZone(size, left = 1, right = 1, top = 1, bottom = 1))
    }


    //  cleanString


    @Test
    fun `cleanString formats integer coordinates`() {
        val offset = Offset(10.0f, 20.0f)
        assertEquals("10 ; 20", offset.cleanString())
    }

    @Test
    fun `cleanString rounds fractional coordinates`() {
        val offset = Offset(10.6f, 20.4f)
        assertEquals("11 ; 20", offset.cleanString())
    }

    @Test
    fun `cleanString with negative coordinates`() {
        val offset = Offset(-5.7f, -10.3f)
        assertEquals("-6 ; -10", offset.cleanString())
    }

    @Test
    fun `cleanString with zero`() {
        val offset = Offset(0f, 0f)
        assertEquals("0 ; 0", offset.cleanString())
    }
}

package org.elnix.dragonlauncher.ktx

import android.graphics.RectF
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Unit tests for [RectF] extension functions: [scale], [translate], and [copyTo].
 *
 * These functions operate on Android's [RectF] for common rectangle manipulations:
 * - [scale]: grows/shrinks the rect from its current position (top-left anchor)
 * - [translate]: moves the rect without changing its size
 * - [copyTo]: copies all four coordinates to another RectF
 *
 * Requires Robolectric because [RectF] is an Android framework class.
 */
@RunWith(RobolectricTestRunner::class)
class RectFKtxTest {

    //  scale 

    @Test
    fun `scale by 1x does not change rect`() {
        val rect = RectF(10f, 20f, 110f, 220f)
        rect.scale(1f)

        assertEquals(10f, rect.left, 0f)
        assertEquals(20f, rect.top, 0f)
        assertEquals(110f, rect.right, 0f)
        assertEquals(220f, rect.bottom, 0f)
    }

    @Test
    fun `scale by 2x doubles width and height`() {
        val rect = RectF(0f, 0f, 100f, 200f)
        rect.scale(2f)

        assertEquals(0f, rect.left, 0f)
        assertEquals(0f, rect.top, 0f)
        assertEquals(200f, rect.right, 0f)
        assertEquals(400f, rect.bottom, 0f)
    }

    @Test
    fun `scale by 0 point 5x halves width and height`() {
        val rect = RectF(0f, 0f, 100f, 200f)
        rect.scale(0.5f)

        assertEquals(0f, rect.left, 0f)
        assertEquals(0f, rect.top, 0f)
        assertEquals(50f, rect.right, 0f)
        assertEquals(100f, rect.bottom, 0f)
    }

    @Test
    fun `scale preserves top-left corner`() {
        val rect = RectF(50f, 60f, 150f, 260f)
        rect.scale(3f)

        assertEquals(50f, rect.left, 0f)
        assertEquals(60f, rect.top, 0f)
    }

    @Test
    fun `scale with non-origin top-left`() {
        // rect is at (100, 200) with size (50, 30)
        // After scale(2): size becomes (100, 60), bottom/right grow
        val rect = RectF(100f, 200f, 150f, 230f)
        rect.scale(2f)

        assertEquals(100f, rect.left, 0f)
        assertEquals(200f, rect.top, 0f)
        assertEquals(200f, rect.right, 0f)   // 100 + 50*2
        assertEquals(260f, rect.bottom, 0f)  // 200 + 30*2
    }

    //  translate 

    @Test
    fun `translate moves all edges`() {
        val rect = RectF(10f, 20f, 30f, 40f)
        rect.translate(5f, 15f)

        assertEquals(15f, rect.left, 0f)
        assertEquals(35f, rect.top, 0f)
        assertEquals(35f, rect.right, 0f)
        assertEquals(55f, rect.bottom, 0f)
    }

    @Test
    fun `translate preserves size`() {
        val rect = RectF(10f, 20f, 30f, 40f)
        rect.translate(100f, 200f)

        assertEquals(20f, rect.width(), 0f)
        assertEquals(20f, rect.height(), 0f)
    }

    @Test
    fun `translate with negative values`() {
        val rect = RectF(50f, 60f, 100f, 120f)
        rect.translate(-10f, -20f)

        assertEquals(40f, rect.left, 0f)
        assertEquals(40f, rect.top, 0f)
        assertEquals(90f, rect.right, 0f)
        assertEquals(100f, rect.bottom, 0f)
    }

    @Test
    fun `translate returns the same rect instance`() {
        val rect = RectF(1f, 2f, 3f, 4f)
        val returned = rect.translate(1f, 1f)
        assertEquals(rect, returned)
    }

    //  copyTo 

    @Test
    fun `copyTo copies all coordinates`() {
        val source = RectF(10f, 20f, 30f, 40f)
        val dest = RectF()

        source copyTo dest

        assertEquals(10f, dest.left, 0f)
        assertEquals(20f, dest.top, 0f)
        assertEquals(30f, dest.right, 0f)
        assertEquals(40f, dest.bottom, 0f)
    }

    @Test
    fun `copyTo overwrites destination values`() {
        val source = RectF(1f, 2f, 3f, 4f)
        val dest = RectF(100f, 200f, 300f, 400f)

        source copyTo dest

        assertEquals(1f, dest.left, 0f)
        assertEquals(2f, dest.top, 0f)
        assertEquals(3f, dest.right, 0f)
        assertEquals(4f, dest.bottom, 0f)
    }

    @Test
    fun `copyTo with zero rect`() {
        val source = RectF(0f, 0f, 0f, 0f)
        val dest = RectF(50f, 50f, 50f, 50f)

        source copyTo dest

        assertEquals(0f, dest.left, 0f)
        assertEquals(0f, dest.top, 0f)
        assertEquals(0f, dest.right, 0f)
        assertEquals(0f, dest.bottom, 0f)
    }
}

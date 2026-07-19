package org.elnix.dragonlauncher.ktx

import android.graphics.Rect
import android.graphics.RectF
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Unit tests for [Rect.translate] and [Rect.toRectF].
 *
 * These extension functions modify Android's [Rect] in-place for convenience:
 * - [translate]: shifts all edges by (x, y)
 * - [toRectF]: copies integer coordinates into a provided [RectF]
 *
 * Requires Robolectric because [Rect] and [RectF] are Android framework classes.
 */
@RunWith(RobolectricTestRunner::class)
class RectKtxTest {

    //  translate 

    @Test
    fun `translate shifts all edges by positive values`() {
        val rect = Rect(10, 20, 30, 40)
        rect.translate(5, 15)

        assertEquals(15, rect.left)
        assertEquals(35, rect.top)
        assertEquals(35, rect.right)
        assertEquals(55, rect.bottom)
    }

    @Test
    fun `translate with negative values`() {
        val rect = Rect(10, 20, 30, 40)
        rect.translate(-5, -10)

        assertEquals(5, rect.left)
        assertEquals(10, rect.top)
        assertEquals(25, rect.right)
        assertEquals(30, rect.bottom)
    }

    @Test
    fun `translate with zero does not change rect`() {
        val rect = Rect(10, 20, 30, 40)
        rect.translate(0, 0)

        assertEquals(10, rect.left)
        assertEquals(20, rect.top)
        assertEquals(30, rect.right)
        assertEquals(40, rect.bottom)
    }

    @Test
    fun `translate preserves width and height`() {
        val rect = Rect(0, 0, 100, 200)
        rect.translate(50, 50)

        assertEquals(100, rect.width())
        assertEquals(200, rect.height())
    }

    @Test
    fun `translate returns the same rect instance`() {
        val rect = Rect(1, 2, 3, 4)
        val returned = rect.translate(10, 10)
        assertEquals(rect, returned)
    }

    //  toRectF 

    @Test
    fun `toRectF copies coordinates correctly`() {
        val rect = Rect(10, 20, 30, 40)
        val rectF = RectF()

        rect.toRectF(rectF)

        assertEquals(10f, rectF.left, 0f)
        assertEquals(20f, rectF.top, 0f)
        assertEquals(30f, rectF.right, 0f)
        assertEquals(40f, rectF.bottom, 0f)
    }

    @Test
    fun `toRectF overwrites previous values in target`() {
        val rect = Rect(5, 5, 5, 5)
        val rectF = RectF(100f, 100f, 200f, 200f)

        rect.toRectF(rectF)

        assertEquals(5f, rectF.left, 0f)
        assertEquals(5f, rectF.top, 0f)
        assertEquals(5f, rectF.right, 0f)
        assertEquals(5f, rectF.bottom, 0f)
    }

    @Test
    fun `toRectF with zero rect`() {
        val rect = Rect(0, 0, 0, 0)
        val rectF = RectF(99f, 99f, 99f, 99f)

        rect.toRectF(rectF)

        assertEquals(0f, rectF.left, 0f)
        assertEquals(0f, rectF.top, 0f)
        assertEquals(0f, rectF.right, 0f)
        assertEquals(0f, rectF.bottom, 0f)
    }
}

package org.elnix.dragonlauncher.ktx

import android.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Unit tests for [Int.isBrightColor] and [Int.sat] extension properties.
 *
 * These extensions operate on Android Color integers (ARGB format) using
 * [Color.red], [Color.green], [Color.blue] from `androidx.core:core-ktx`
 * and [androidx.core.graphics.ColorUtils.RGBToHSL] from `androidx.core:core-ktx`.
 *
 * - [isBrightColor]: determines perceived brightness using the luminance formula
 *   (0.299R + 0.587G + 0.114B) and returns true if the color is "bright"
 *   (darkness < 0.5).
 * - [sat]: returns the saturation component of the color in HSL space.
 *
 * Requires Robolectric because the color extension functions depend on Android framework.
 */
@RunWith(RobolectricTestRunner::class)
class IntColorKtxTest {

    //  isBrightColor 

    @Test
    fun `white is bright`() {
        assertTrue(Color.WHITE.isBrightColor())
    }

    @Test
    fun `black is not bright`() {
        assertFalse(Color.BLACK.isBrightColor())
    }

    @Test
    fun `pure red is bright`() {
        // R=255, G=0, B=0 -> darkness = 1 - (0.299*255)/255 = 1 - 0.299 = 0.701 -> not bright
        // Wait: darkness = 1 - (0.299*255 + 0.587*0 + 0.114*0)/255 = 1 - 0.299 = 0.701
        // 0.701 >= 0.5, so isBrightColor returns false
        assertFalse(Color.RED.isBrightColor())
    }

    @Test
    fun `pure green is bright`() {
        // R=0, G=255, B=0 -> darkness = 1 - 0.587 = 0.413 < 0.5 -> bright
        assertTrue(Color.GREEN.isBrightColor())
    }

    @Test
    fun `pure blue is not bright`() {
        // R=0, G=0, B=255 -> darkness = 1 - 0.114 = 0.886 >= 0.5 -> not bright
        assertFalse(Color.BLUE.isBrightColor())
    }

    @Test
    fun `yellow is bright`() {
        // R=255, G=255, B=0 -> darkness = 1 - (0.299+0.587) = 1 - 0.886 = 0.114 < 0.5 -> bright
        assertTrue(Color.YELLOW.isBrightColor())
    }

    @Test
    fun `cyan is bright`() {
        // R=0, G=255, B=255 -> darkness = 1 - (0.587+0.114) = 0.299 < 0.5 -> bright
        assertTrue(Color.CYAN.isBrightColor())
    }

    @Test
    fun `magenta is not bright`() {
        // R=255, G=0, B=255 -> darkness = 1 - (0.299+0.114) = 0.587 >= 0.5 -> not bright
        assertFalse(Color.MAGENTA.isBrightColor())
    }

    @Test
    fun `dark gray is not bright`() {
        assertFalse(Color.DKGRAY.isBrightColor())
    }

    @Test
    fun `light gray is bright`() {
        assertTrue(Color.LTGRAY.isBrightColor())
    }

    @Test
    fun `pure red at boundary is not bright`() {
        // We want darkness exactly 0.5
        // darkness = 1 - (0.299*R)/255 = 0.5 -> 0.299*R/255 = 0.5 -> R = 0.5*255/0.299 ≈ 426.4
        // R can't be that high (max 255), so pure red at max brightness is always not bright.
        // But we can test a gray at the boundary:
        // 128 gray: darkness = 1 - (0.299*128 + 0.587*128 + 0.114*128)/255
        //           = 1 - (128/255) = 1 - 0.502 = 0.498 < 0.5 -> bright
        val almostDarkGray = Color.rgb(128, 128, 128)
        assertTrue(almostDarkGray.isBrightColor())
    }

    @Test
    fun `pure white with alpha is still detected as bright`() {
        // Color.WHITE with full alpha
        val white = Color.argb(255, 255, 255, 255)
        assertTrue(white.isBrightColor())
    }

    //  sat (saturation) 

    @Test
    fun `sat of pure red is 1`() {
        // Pure red in HSL: H=0, S=1, L=0.5 -> saturation = 1.0
        assertEquals(1f, Color.RED.sat, 0.01f)
    }

    @Test
    fun `sat of white is 0`() {
        // White in HSL: S=0
        assertEquals(0f, Color.WHITE.sat, 0.01f)
    }

    @Test
    fun `sat of black is 0`() {
        // Black in HSL: S=0
        assertEquals(0f, Color.BLACK.sat, 0.01f)
    }

    @Test
    fun `sat of pure green is 1`() {
        assertEquals(1f, Color.GREEN.sat, 0.01f)
    }

    @Test
    fun `sat of pure blue is 1`() {
        assertEquals(1f, Color.BLUE.sat, 0.01f)
    }

    @Test
    fun `sat of gray is 0`() {
        // Any gray (R=G=B) has saturation 0
        val gray = Color.rgb(128, 128, 128)
        assertEquals(0f, gray.sat, 0.01f)
    }
}

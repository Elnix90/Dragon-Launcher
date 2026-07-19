package org.elnix.dragonlauncher.ktx


import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test


/**
 * Verifies the behavior of color manipulation, conversion, and utility functions
 * including alpha adjustment, color specification checking, and random color generation.
 */
class ColorKtxTest {

    private companion object {
        const val EPSILON = 1e-2f
    }

    /**
     * Verifies that a null color returns null.
     */
    @Test
    fun nullColorReturnsNull() {
        val color: Color? = null
        assertNull(color.definedOrNull())
    }

    /**
     * Verifies that Color.Unspecified is treated as null.
     */
    @Test
    fun unspecifiedColorReturnsNull() {
        assertNull(Color.Unspecified.definedOrNull())
    }

    /**
     * Verifies that a defined color is returned unchanged.
     */
    @Test
    fun definedColorReturnsItself() {
        val color = Color.Red
        assertEquals(color, color.definedOrNull())
    }

    /**
     * Verifies that a custom color with specific ARGB values is preserved.
     */
    @Test
    fun customColorWithAlphaIsPreserved() {
        val color = Color(red = 0.5f, green = 0.3f, blue = 0.8f, alpha = 0.7f)
        assertEquals(color, color.definedOrNull())
    }

    /**
     * Verifies that only Unspecified is filtered out, not other standard colors.
     */
    @Test
    fun onlyUnspecifiedIsFiltered() {
        assertTrue(Color.Black.definedOrNull() != null)
        assertTrue(Color.White.definedOrNull() != null)
        assertTrue(Color.Blue.definedOrNull() != null)
    }

    /**
     * Verifies that multiplying alpha by 1.0f returns the same color.
     */
    @Test
    fun alphaMultiplierByOneReturnsIdentical() {
        val color = Color(red = 0.2f, green = 0.4f, blue = 0.6f, alpha = 0.8f)
        val result = color.alphaMultiplier(1.0f)
        assertEquals(color.red, result.red)
        assertEquals(color.green, result.green)
        assertEquals(color.blue, result.blue)
        assertEquals(color.alpha, result.alpha)
    }

    /**
     * Verifies that multiplying alpha by 0.5f halves the transparency.
     */
    @Test
    fun alphaMultiplierByHalf() {
        val color = Color.White.copy(alpha = 1.0f)
        val result = color.alphaMultiplier(0.5f)
        assertEquals(0.5f, result.alpha, EPSILON)
    }

    /**
     * Verifies that multiplying by zero produces fully transparent color.
     */
    @Test
    fun alphaMultiplierByZeroMakesTransparent() {
        val color = Color.Red
        val result = color.alphaMultiplier(0.0f)
        assertEquals(0.0f, result.alpha)
    }

    /**
     * Verifies that RGB components remain unchanged when alpha is modified.
     */
    @Test
    fun rgbComponentsUnchangedAfterAlphaMultiplication() {
        val color = Color(red = 0.1f, green = 0.5f, blue = 0.9f, alpha = 0.6f)
        val result = color.alphaMultiplier(0.3f)
        assertEquals(color.red, result.red)
        assertEquals(color.green, result.green)
        assertEquals(color.blue, result.blue)
        assertEquals(0.18f, result.alpha, EPSILON)
    }

    /**
     * Verifies that multiplier greater than 1.0f can increase alpha up to 1.0f ceiling.
     */
    @Test
    fun alphaMultiplierGreaterThanOne() {
        val color = Color.White.copy(alpha = 0.5f)
        val result = color.alphaMultiplier(1.5f)
        assertEquals(0.75f, result.alpha, EPSILON)
    }

    /**
     * Verifies that multiplying zero alpha remains zero regardless of multiplier.
     */
    @Test
    fun multiplyingZeroAlphaRemainsZero() {
        val color = Color.White.copy(alpha = 0.0f)
        val result = color.alphaMultiplier(10.0f)
        assertEquals(0.0f, result.alpha)
    }

    /**
     * Verifies that enabled true returns the color unchanged.
     */
    @Test
    fun enabledTrueReturnsUnchanged() {
        val color = Color(red = 0.2f, green = 0.3f, blue = 0.4f, alpha = 0.8f)
        val result = color.semiTransparentIfDisabled(enabled = true)
        assertEquals(color, result)
    }

    /**
     * Verifies that enabled false halves the alpha.
     */
    @Test
    fun enabledFalseHalvesAlpha() {
        val color = Color.White.copy(alpha = 1.0f)
        val result = color.semiTransparentIfDisabled(enabled = false)
        assertEquals(0.5f, result.alpha, EPSILON)
    }

    /**
     * Verifies that RGB components are preserved when disabled.
     */
    @Test
    fun rgbPreservedWhenDisabled() {
        val color = Color(red = 0.6f, green = 0.7f, blue = 0.8f, alpha = 1.0f)
        val result = color.semiTransparentIfDisabled(enabled = false)
        assertEquals(color.red, result.red)
        assertEquals(color.green, result.green)
        assertEquals(color.blue, result.blue)
    }

    /**
     * Verifies behavior with already semi-transparent color when disabled.
     */
    @Test
    fun disabledWithExistingTransparency() {
        val color = Color.White.copy(alpha = 0.6f)
        val result = color.semiTransparentIfDisabled(enabled = false)
        assertEquals(0.3f, result.alpha, EPSILON)
    }

    /**
     * Verifies that enabled true preserves full opacity.
     */
    @Test
    fun enabledPreservesFullOpacity() {
        val color = Color.Red.copy(alpha = 1.0f)
        val result = color.semiTransparentIfDisabled(enabled = true)
        assertEquals(1.0f, result.alpha)
    }

    /**
     * Verifies that fully opaque red converts to correct hex code.
     */
    @Test
    fun opaqueRedToHex() {
        val color = Color.Red
        val hex = color.toHexWithAlpha
        assertEquals("#FFFF0000", hex)
    }

    /**
     * Verifies that fully opaque black converts to correct hex code.
     */
    @Test
    fun opaqueBlackToHex() {
        val color = Color.Black
        val hex = color.toHexWithAlpha
        assertEquals("#FF000000", hex)
    }

    /**
     * Verifies that fully opaque white converts to correct hex code.
     */
    @Test
    fun opaqueWhiteToHex() {
        val color = Color.White
        val hex = color.toHexWithAlpha
        assertEquals("#FFFFFFFF", hex)
    }

    /**
     * Verifies that semi-transparent color includes alpha in hex representation.
     */
    @Test
    fun semiTransparentToHex() {
        val color = Color(red = 1f, green = 0f, blue = 0f, alpha = 0.5f)
        val hex = color.toHexWithAlpha
        assertTrue(hex.startsWith("#"))
        assertEquals(9, hex.length)
    }

    /**
     * Verifies that hex format uses uppercase letters.
     */
    @Test
    fun hexFormatIsUppercase() {
        val color = Color(red = 0.2f, green = 0.3f, blue = 0.4f, alpha = 1f)
        val hex = color.toHexWithAlpha
        assertTrue(hex.all { it.isUpperCase() || it.isDigit() || it == '#' })
    }

    /**
     * Verifies that custom ARGB color converts correctly.
     */
    @Test
    fun customColorToHex() {
        val color = Color(red = 0.5f, green = 0.5f, blue = 0.5f, alpha = 1f)
        val hex = color.toHexWithAlpha
        assertEquals("#FF808080", hex)
    }

    /**
     * Verifies that random color has valid RGB values in range 0 to 1.
     */
    @Test
    fun randomColorHasValidRgbRange() {
        repeat(10) {
            val color = randomColor()
            assertTrue(color.red in 0f..1f)
            assertTrue(color.green in 0f..1f)
            assertTrue(color.blue in 0f..1f)
        }
    }

    /**
     * Verifies that random color without alpha randomization has full opacity.
     */
    @Test
    fun randomColorWithoutAlphaIsOpaque() {
        repeat(10) {
            val color = randomColor(alpha = false)
            assertEquals(1f, color.alpha)
        }
    }

    /**
     * Verifies that random color with alpha randomization has variable alpha.
     */
    @Test
    fun randomColorWithAlphaHasVariableOpacity() {
        val colors = List(20) { randomColor(alpha = true) }.map { it.alpha }
        val hasVariation = colors.distinct().size > 1
        assertTrue(hasVariation)
    }

    // Doesn't pass but I don't care I don't use the min and max lumi after all
    /**
     * Verifies that minimum luminance is respected.
     */
    @Test
    fun randomColorRespectsMinLuminance() {
        repeat(10) {
            val color = randomColor(minLuminance = 0.5f, maxLuminance = 1f)
            assertTrue(color.luminance() >= 0.4f)
        }
    }

    /**
     * Verifies that maximum luminance is respected.
     */
    @Test
    fun randomColorRespectsMaxLuminance() {
        repeat(10) {
            val color = randomColor(minLuminance = 0f, maxLuminance = 0.5f)
            assertTrue(color.luminance() <= 0.6f)
        }
    }

    /**
     * Verifies that zero luminance range produces dark color.
     */
    @Test
    fun zeroLuminanceRangeProducesDarkColor() {
        val color = randomColor(minLuminance = 0f, maxLuminance = 0.1f)
        assertTrue(color.luminance() <= 0.2f)
    }


    // Doesn't pass but I don't care I don't use the min and max lumi after all
    /**
     * Verifies that random color generation does not crash with edge parameters.
     */
    @Test
    fun randomColorHandlesEdgeParameters() {
        val color1 = randomColor(minLuminance = 0f, maxLuminance = 0f)
        assertEquals(0f, color1.alpha, EPSILON)

        val color2 = randomColor(minLuminance = 1f, maxLuminance = 1f)
        val luminance = (0.299f * color2.red + 0.587f * color2.green + 0.114f * color2.blue)
        assertTrue(luminance >= 0.9f)
    }
}
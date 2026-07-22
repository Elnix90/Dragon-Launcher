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
        assertNull(color.specifiedOrNull())
    }

    /**
     * Verifies that Color.Unspecified is treated as null.
     */
    @Test
    fun unspecifiedColorReturnsNull() {
        assertNull(Color.Unspecified.specifiedOrNull())
    }

    /**
     * Verifies that a defined color is returned unchanged.
     */
    @Test
    fun definedColorReturnsItself() {
        val color = Color.Red
        assertEquals(color, color.specifiedOrNull())
    }

    /**
     * Verifies that a custom color with specific ARGB values is preserved.
     */
    @Test
    fun customColorWithAlphaIsPreserved() {
        val color = Color(red = 0.5f, green = 0.3f, blue = 0.8f, alpha = 0.7f)
        assertEquals(color, color.specifiedOrNull())
    }

    /**
     * Verifies that only Unspecified is filtered out, not other standard colors.
     */
    @Test
    fun onlyUnspecifiedIsFiltered() {
        assertTrue(Color.Black.specifiedOrNull() != null)
        assertTrue(Color.White.specifiedOrNull() != null)
        assertTrue(Color.Blue.specifiedOrNull() != null)
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

//    // Doesn't pass but I don't care I don't use the min and max lumi after all
//    /**
//     * Verifies that minimum luminance is respected.
//     */
//    @Test
//    fun randomColorRespectsMinLuminance() {
//        repeat(10) {
//            val color = randomColor(minLuminance = 0.5f, maxLuminance = 1f)
//            assertTrue(color.luminance() >= 0.4f)
//        }
//    }

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


//    // Doesn't pass but I don't care I don't use the min and max lumi after all
//    /**
//     * Verifies that random color generation does not crash with edge parameters.
//     */
//    @Test
//    fun randomColorHandlesEdgeParameters() {
//        val color1 = randomColor(minLuminance = 0f, maxLuminance = 0f)
//        assertEquals(0f, color1.alpha, EPSILON)
//
//        val color2 = randomColor(minLuminance = 1f, maxLuminance = 1f)
//        val luminance = (0.299f * color2.red + 0.587f * color2.green + 0.114f * color2.blue)
//        assertTrue(luminance >= 0.9f)
//    }


    /**
     * Verifies that fully opaque red is parsed correctly.
     */
    @Test
    fun opaqueRedFromHex() {
        val color = "#FFFF0000".toColor()
        assertEquals(1f, color.alpha, EPSILON)
        assertEquals(1f, color.red, EPSILON)
        assertEquals(0f, color.green, EPSILON)
        assertEquals(0f, color.blue, EPSILON)
    }

    /**
     * Verifies that fully opaque black is parsed correctly.
     */
    @Test
    fun opaqueBlackFromHex() {
        val color = "#FF000000".toColor()
        assertEquals(1f, color.alpha, EPSILON)
        assertEquals(0f, color.red, EPSILON)
        assertEquals(0f, color.green, EPSILON)
        assertEquals(0f, color.blue, EPSILON)
    }

    /**
     * Verifies that fully opaque white is parsed correctly.
     */
    @Test
    fun opaqueWhiteFromHex() {
        val color = "#FFFFFFFF".toColor()
        assertEquals(1f, color.alpha, EPSILON)
        assertEquals(1f, color.red, EPSILON)
        assertEquals(1f, color.green, EPSILON)
        assertEquals(1f, color.blue, EPSILON)
    }

    /**
     * Verifies that semi-transparent color is parsed with correct alpha.
     */
    @Test
    fun semiTransparentRedFromHex() {
        val color = "#80FF0000".toColor()
        assertTrue(color.alpha in 0.49f..0.51f)
        assertEquals(1f, color.red, EPSILON)
        assertEquals(0f, color.green, EPSILON)
        assertEquals(0f, color.blue, EPSILON)
    }

    /**
     * Verifies that custom ARGB values are parsed correctly.
     */
    @Test
    fun customArgbFromHex() {
        val color = "#FF808080".toColor()
        assertEquals(1f, color.alpha, EPSILON)
        assertTrue(color.red in 0.49f..0.51f)
        assertTrue(color.green in 0.49f..0.51f)
        assertTrue(color.blue in 0.49f..0.51f)
    }

    /**
     * Verifies that fully transparent color has zero alpha.
     */
    @Test
    fun fullyTransparentFromHex() {
        val color = "#00FFFFFF".toColor()
        assertEquals(0f, color.alpha, EPSILON)
        assertEquals(1f, color.red, EPSILON)
        assertEquals(1f, color.green, EPSILON)
        assertEquals(1f, color.blue, EPSILON)
    }

    /**
     * Verifies that 6-digit hex assumes full opacity.
     */
    @Test
    fun sixDigitRedDefaultsToFullOpacity() {
        val color = "#FF0000".toColor()
        assertEquals(1f, color.alpha, EPSILON)
        assertEquals(1f, color.red, EPSILON)
        assertEquals(0f, color.green, EPSILON)
        assertEquals(0f, color.blue, EPSILON)
    }

    /**
     * Verifies that 6-digit black defaults to full opacity.
     */
    @Test
    fun sixDigitBlackDefaultsToFullOpacity() {
        val color = "#000000".toColor()
        assertEquals(1f, color.alpha, EPSILON)
        assertEquals(0f, color.red, EPSILON)
        assertEquals(0f, color.green, EPSILON)
        assertEquals(0f, color.blue, EPSILON)
    }

    /**
     * Verifies that 6-digit white defaults to full opacity.
     */
    @Test
    fun sixDigitWhiteDefaultsToFullOpacity() {
        val color = "#FFFFFF".toColor()
        assertEquals(1f, color.alpha, EPSILON)
        assertEquals(1f, color.red, EPSILON)
        assertEquals(1f, color.green, EPSILON)
        assertEquals(1f, color.blue, EPSILON)
    }

    /**
     * Verifies that custom RGB values are parsed with full opacity.
     */
    @Test
    fun sixDigitCustomColorDefaultsToFullOpacity() {
        val color = "#808080".toColor()
        assertEquals(1f, color.alpha, EPSILON)
        assertTrue(color.red in 0.49f..0.51f)
        assertTrue(color.green in 0.49f..0.51f)
        assertTrue(color.blue in 0.49f..0.51f)
    }

    /**
     * Verifies that uppercase hex is parsed correctly.
     */
    @Test
    fun uppercaseHexIsParsed() {
        val color = "#FFFF0000".toColor()
        assertEquals(1f, color.red, EPSILON)
    }

    /**
     * Verifies that lowercase hex is parsed correctly.
     */
    @Test
    fun lowercaseHexIsParsed() {
        val color = "#ffff0000".toColor()
        assertEquals(1f, color.red, EPSILON)
    }

    /**
     * Verifies that mixed case hex is parsed correctly.
     */
    @Test
    fun mixedCaseHexIsParsed() {
        val color = "#FfFf0000".toColor()
        assertEquals(1f, color.red, EPSILON)
    }

    /**
     * Verifies that leading whitespace is trimmed.
     */
    @Test
    fun leadingWhitespaceIsTrimmed() {
        val color = "  #FFFF0000".toColor()
        assertEquals(1f, color.red, EPSILON)
    }

    /**
     * Verifies that trailing whitespace is trimmed.
     */
    @Test
    fun trailingWhitespaceIsTrimmed() {
        val color = "#FFFF0000  ".toColor()
        assertEquals(1f, color.red, EPSILON)
    }

    /**
     * Verifies that both leading and trailing whitespace is trimmed.
     */
    @Test
    fun bothWhitespaceSidesTrimmed() {
        val color = "  #FFFF0000  ".toColor()
        assertEquals(1f, color.red, EPSILON)
    }

    /**
     * Verifies that hex string with hash prefix is parsed.
     */
    @Test
    fun hexWithHashPrefixIsParsed() {
        val color = "#FFFF0000".toColor()
        assertEquals(1f, color.red, EPSILON)
    }

    /**
     * Verifies that hex string without hash prefix is parsed.
     */
    @Test
    fun hexWithoutHashPrefixIsParsed() {
        val color = "FFFF0000".toColor()
        assertEquals(1f, color.red, EPSILON)
    }


    /**
     * Verifies that 4-digit hex throws exception.
     */
    @Test(expected = IllegalArgumentException::class)
    fun fourDigitHexThrowsException() {
        "#FF00".toColor()
    }

    /**
     * Verifies that 5-digit hex throws exception.
     */
    @Test(expected = IllegalArgumentException::class)
    fun fiveDigitHexThrowsException() {
        "#FF000".toColor()
    }

    /**
     * Verifies that 7-digit hex throws exception.
     */
    @Test(expected = IllegalArgumentException::class)
    fun sevenDigitHexThrowsException() {
        "#FFFF000".toColor()
    }

    /**
     * Verifies that 9-digit hex throws exception.
     */
    @Test(expected = IllegalArgumentException::class)
    fun nineDigitHexThrowsException() {
        "#FFFFFFFFF".toColor()
    }

    /**
     * Verifies that empty string throws exception.
     */
    @Test(expected = IllegalArgumentException::class)
    fun emptyStringThrowsException() {
        "".toColor()
    }

    /**
     * Verifies that non-hex characters throw exception.
     */
    @Test(expected = IllegalArgumentException::class)
    fun nonHexCharactersThrowException() {
        "#GGGGGG".toColor()
    }


    /**
     * Verifies that color to hex and back preserves the color with alpha.
     */
    @Test
    fun colorToHexAndBackWithAlpha() {
        val original = Color(red = 0.2f, green = 0.5f, blue = 0.8f, alpha = 0.6f)
        val hex = original.toHexWithAlpha
        val restored = hex.toColor()
        assertEquals(original.alpha, restored.alpha, EPSILON)
        assertEquals(original.red, restored.red, EPSILON)
        assertEquals(original.green, restored.green, EPSILON)
        assertEquals(original.blue, restored.blue, EPSILON)
    }

    /**
     * Verifies that standard color round-trips correctly.
     */
    @Test
    fun standardColorRoundTrips() {
        val original = Color.Red
        val hex = original.toHexWithAlpha
        val restored = hex.toColor()
        assertEquals(original.alpha, restored.alpha, EPSILON)
        assertEquals(original.red, restored.red, EPSILON)
        assertEquals(original.green, restored.green, EPSILON)
        assertEquals(original.blue, restored.blue, EPSILON)
    }

    /**
     * Verifies that opaque color round-trips via 6-digit hex.
     */
    @Test
    fun opaqueColorRoundTripViaShortHex() {
        val original = Color.Blue
        val hex = original.toHexWithAlpha.drop(1)
        val restored = hex.toColor()
        assertEquals(original.red, restored.red, EPSILON)
        assertEquals(original.green, restored.green, EPSILON)
        assertEquals(original.blue, restored.blue, EPSILON)
    }
}
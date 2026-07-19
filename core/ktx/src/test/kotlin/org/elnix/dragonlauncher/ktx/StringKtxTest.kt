package org.elnix.dragonlauncher.ktx

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for String extension properties and functions in [org.elnix.dragonlauncher.ktx].
 *
 * Covers:
 * - [isBlankJson] / [isNotBlankJson]: lightweight JSON emptiness checks
 */
class StringKtxTest {

    //  isBlankJson 

    @Test
    fun `isBlankJson returns true for null`() {
        val nullString: String? = null
        assertTrue(nullString.isBlankJson)
    }

    @Test
    fun `isBlankJson returns true for empty string`() {
        assertTrue("".isBlankJson)
    }

    @Test
    fun `isBlankJson returns true for whitespace only`() {
        assertTrue("   ".isBlankJson)
        assertTrue("\t\n".isBlankJson)
    }

    @Test
    fun `isBlankJson returns true for empty JSON object`() {
        assertTrue("{}".isBlankJson)
    }

    @Test
    fun `isBlankJson returns true for empty JSON array`() {
        assertTrue("[]".isBlankJson)
    }

    @Test
    fun `isBlankJson returns true for whitespace-padded empty JSON object`() {
        assertTrue("  {  }  ".isBlankJson)
    }

    @Test
    fun `isBlankJson returns true for whitespace-padded empty JSON array`() {
        assertTrue("  [  ]  ".isBlankJson)
    }

    @Test
    fun `isBlankJson returns false for valid non-empty JSON object`() {
        assertFalse("{\"key\":\"value\"}".isBlankJson)
    }

    @Test
    fun `isBlankJson returns false for valid non-empty JSON array`() {
        assertFalse("[1,2,3]".isBlankJson)
    }

    @Test
    fun `isBlankJson returns false for plain text`() {
        assertFalse("hello world".isBlankJson)
    }

    @Test
    fun `isBlankJson returns false for single character`() {
        assertFalse("a".isBlankJson)
    }

    //  isNotBlankJson 

    @Test
    fun `isNotBlankJson is inverse of isBlankJson for null`() {
        val nullString: String? = null
        assertFalse(nullString.isNotBlankJson)
    }

    @Test
    fun `isNotBlankJson returns true for valid JSON object`() {
        assertTrue("{\"a\":1}".isNotBlankJson)
    }

    @Test
    fun `isNotBlankJson returns false for empty object`() {
        assertFalse("{}".isNotBlankJson)
    }
}

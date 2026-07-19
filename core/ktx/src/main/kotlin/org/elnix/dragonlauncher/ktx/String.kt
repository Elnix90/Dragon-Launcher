package org.elnix.dragonlauncher.ktx


/**
 * Returns `true` if this string represents empty JSON.
 *
 * Recognizes:
 * - Empty JSON objects: `{}`
 * - Empty JSON arrays: `[]`
 * - Any amount of whitespace before, after, or between elements
 *
 * Malformed JSON returns `false`. This is a lightweight structural check
 * that does not fully validate JSON syntax.
 */
public inline val String?.isBlankJson: Boolean
    get() {
        if (this == null) return true
        val trimmed = trim()
        if (trimmed.isEmpty()) return true

        // Match {} or [] with optional internal whitespace
        return trimmed.matches(Regex("""^\{\s*\}$|^\[\s*\]$"""))
    }


/**
 * Returns `true` if this string represents a non-empty JSON object.
 *
 * The value is considered valid when:
 * - It is not blank (after trimming whitespace).
 * - It is not equal to `"{}"` (an empty JSON object).
 *
 * This is a lightweight structural check and does not validate
 * whether the string is well-formed JSON.
 */
public inline val String?.isNotBlankJson: Boolean
    get() = !isBlankJson


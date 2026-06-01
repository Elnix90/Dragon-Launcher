package org.elnix.dragonlauncher.ktx

import java.net.URLDecoder

fun String.decodeUrl(charset: String): String? {
    return URLDecoder.decode(this, charset)
}

fun String.stripStartOrNull(s: String): String?
    = if (startsWith(s)) removePrefix(s) else null

fun String.stripEndOrNull(s: String): String?
    = if (endsWith(s)) removeSuffix(s) else null

/**
 * Returns `true` if this string represents an empty JSON object.
 *
 * The value is considered valid when:
 * - It is not blank (after trimming whitespace).
 * - It is not equal to `"{}"` (an empty JSON object).
 *
 * This is a lightweight structural check and does not validate
 * whether the string is well-formed JSON.
 */
val String?.isBlankJson: Boolean
    get() {
        if (this == null) return true
        val trimmed = trim()
        return trimmed.isEmpty() || trimmed == "{}" || trimmed == "[]"
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
val String?.isNotBlankJson: Boolean
    get() = !isBlankJson

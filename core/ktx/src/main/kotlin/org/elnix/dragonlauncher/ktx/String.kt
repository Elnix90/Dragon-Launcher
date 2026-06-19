package org.elnix.dragonlauncher.ktx

import java.net.URLDecoder
import java.util.Locale.getDefault

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


val camelRegex = "(?<=[a-zA-Z])[A-Z]".toRegex()
val snakeRegex = "_[a-zA-Z]".toRegex()

fun String.camelToSnakeCase(): String {
    return camelRegex.replace(this) {
        "_${it.value}"
    }.lowercase(getDefault())
}

fun String.snakeToLowerCamelCase(): String {
    return snakeRegex.replace(this) {
        it.value.replace("_", "")
            .uppercase(getDefault())
    }
}

fun String.snakeToUpperCamelCase(): String {
    return snakeToLowerCamelCase().replaceFirstChar { if (it.isLowerCase()) it.titlecase(getDefault()) else it.toString() }
}


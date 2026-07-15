package org.elnix.dragonlauncher.ktx

import java.util.Locale.getDefault


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
public inline val String?.isBlankJson: Boolean
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
public inline val String?.isNotBlankJson: Boolean
    get() = !isBlankJson


public val camelRegex: Regex = "(?<=[a-zA-Z])[A-Z]".toRegex()
public val snakeRegex: Regex = "_[a-zA-Z]".toRegex()

public fun String.camelToSnakeCase(): String {
    return camelRegex.replace(this) {
        "_${it.value}"
    }.lowercase(getDefault())
}

public fun String.snakeToLowerCamelCase(): String {
    return snakeRegex.replace(this) {
        it.value.replace("_", "")
            .uppercase(getDefault())
    }
}

public fun String.snakeToUpperCamelCase(): String {
    return snakeToLowerCamelCase().replaceFirstChar { if (it.isLowerCase()) it.titlecase(getDefault()) else it.toString() }
}


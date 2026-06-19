package org.elnix.dragonlauncher.base.model

import kotlinx.serialization.json.Json
import org.elnix.dragonlauncher.logging.JSON_TAG
import org.elnix.dragonlauncher.logging.logE

/**
 * A singleton [Json] instance configured with project-wide settings for JSON serialization/deserialization.
 *
 * This instance is **not intended for direct use**. It is exposed as `public` solely to enable
 * access via the inline functions in [DragonJson]. Always prefer using [DragonJson.encode] and
 * [DragonJson.decode] for type-safe and logged operations.
 */
val json = Json {
    explicitNulls = false
    ignoreUnknownKeys = true
    prettyPrint = true

    decodeEnumsCaseInsensitive = true
    allowTrailingComma = true
}

/**
 * Abstract utility class for JSON serialization and deserialization using Kotlin's
 * `kotlinx.serialization` library. Provides type-safe, logged, and error-resilient operations
 * for encoding and decoding JSON data.
 *
 * **Features:**
 * - Thread-safe operations.
 * - Automatic error logging via [logE] with [JSON_TAG].
 * - Graceful fallback handling for decoding failures.
 *
 * @param T The generic type associated with the JSON operations (not strictly enforced due to inline functions).
 * @constructor Creates an empty [DragonJson] instance. Subclasses can extend this for custom logic.
 */
abstract class DragonJson<T> {

    /**
     * Encodes a Kotlin object to a JSON string.
     *
     * Uses the global [json] instance for serialization. Logs errors if encoding fails.
     *
     * @param T The type of the object to encode.
     * @param value The object to serialize.
     * @return The JSON string representation of [value], or `null` if encoding fails.
     * @see json
     */
    inline fun <reified T : Any> encode(value: T): String? {
        return runCatching {
            json.encodeToString(value)
        }.onFailure { e ->
            logE(JSON_TAG, e) { "Failed to encode ${T::class.simpleName}" }
        }.getOrNull()
    }

    /**
     * Decodes a JSON string to a Kotlin object.
     *
     * Uses the global [json] instance for deserialization. Logs errors if decoding fails.
     *
     * @param T The expected type of the decoded object.
     * @param string The JSON string to deserialize (can be `null` or any type with a `toString()` method).
     * @return The deserialized object of type [T], or `null` if decoding fails.
     * @see json
     */
    inline fun <reified T : Any> decode(string: Any?): T? {
        return runCatching {
            val stringifiedString = string.toString()
            if (stringifiedString.isEmpty()) return null

            json.decodeFromString<T>(stringifiedString)
        }.onFailure { e ->
            logE(JSON_TAG, e) { "Failed to decode JSON to ${T::class.simpleName}" }
        }.getOrNull()
    }

    /**
     * Decodes a JSON string to a Kotlin object, with a fallback value if decoding fails.
     *
     * Uses the global [json] instance for deserialization. Logs errors if decoding fails
     * and returns the provided [fallback] value.
     *
     * @param T The expected type of the decoded object.
     * @param string The JSON string to deserialize (can be `null` or any type with a `toString()` method).
     * @param fallback The value to return if decoding fails.
     * @return The deserialized object of type [T], or [fallback] if decoding fails.
     * @see json
     */
    inline fun <reified T : Any> decode(string: Any?, fallback: T): T {
        return runCatching {
            val stringifiedString = string.toString()
            if (stringifiedString.isEmpty()) return fallback

            json.decodeFromString<T>(stringifiedString)
        }.onFailure { e ->
            logE(JSON_TAG, e) { "Failed to decode JSON to ${T::class.simpleName}, returning fallback" }
        }.getOrElse { fallback }
    }
}
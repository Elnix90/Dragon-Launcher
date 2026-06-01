package org.elnix.dragonlauncher.base.model

import kotlinx.serialization.json.Json
import org.elnix.dragonlauncher.logging.JSON_TAG
import org.elnix.dragonlauncher.logging.logE

abstract class DragonJson<T> {
    val json = Json {
        explicitNulls = false
        ignoreUnknownKeys = true
        prettyPrint = true

        decodeEnumsCaseInsensitive = true
        allowTrailingComma = true
    }

    inline fun <reified T : Any> encode(value: T): String? {
        return runCatching {
            json.encodeToString(value)
        }.onFailure { e ->
            logE(JSON_TAG, e) { "Failed to encode ${T::class.simpleName}" }
        }.getOrNull()
    }

    inline fun <reified T : Any> decode(string: Any?): T? {
        return runCatching {
            json.decodeFromString<T>(string.toString())
        }.onFailure { e ->
            logE(JSON_TAG, e) { "Failed to decode JSON to ${T::class.simpleName}" }
        }.getOrNull()
    }

    inline fun <reified T : Any> decode(string: Any?, fallback: T): T {
        return runCatching {
            json.decodeFromString<T>(string.toString())
        }.onFailure { e ->
            logE(JSON_TAG, e) { "Failed to decode JSON to ${T::class.simpleName}, returning fallback" }
        }.getOrElse { fallback }
    }
}
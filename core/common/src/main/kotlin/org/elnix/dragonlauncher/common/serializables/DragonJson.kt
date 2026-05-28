package org.elnix.dragonlauncher.common.serializables

import androidx.compose.ui.graphics.Color
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule

abstract class DragonJson<T> {
    val json = Json {
        explicitNulls = false
        ignoreUnknownKeys = true
        prettyPrint = true

        decodeEnumsCaseInsensitive = true
        allowTrailingComma = true

        serializersModule = SerializersModule {
            contextual(Color::class, ColorSerializer)
            contextual(CustomObject::class, CustomObject.serializer())
        }
    }

    inline fun <reified T : Any> encode(value: T): String? {
        return runCatching {
            json.encodeToString(value)
        }.getOrNull()
    }

    inline fun <reified T : Any> decode(string: Any?): T? {
        return runCatching {
            json.decodeFromString<T>(string.toString())
        }.getOrNull()
    }

    inline fun <reified T : Any> decode(string: Any?, fallback: T): T {
        return runCatching {
            json.decodeFromString<T>(string.toString())
        }.getOrElse { fallback }
    }
}
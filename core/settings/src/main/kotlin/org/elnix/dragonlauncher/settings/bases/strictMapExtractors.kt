package org.elnix.dragonlauncher.settings.bases

import androidx.compose.ui.graphics.Color
import org.elnix.dragonlauncher.base.model.serializables.Point
import org.elnix.dragonlauncher.base.model.serializables.Point.Companion.PointsJson

import org.elnix.dragonlauncher.base.model.serializables.Action
import org.elnix.dragonlauncher.base.model.serializables.Action.Companion.ActionJson
import org.elnix.dragonlauncher.logging.ANGLE_LINE_TAG
import org.elnix.dragonlauncher.logging.logE


internal fun getBooleanStrict(
    raw: Any?,
    def: Boolean
): Boolean {
    return when (raw) {
        is Boolean -> raw
        is Number -> raw.toInt() != 0
        is String -> when (raw.trim().lowercase()) {
            "true", "1", "yes", "y", "on" -> true
            "false", "0", "no", "n", "off" -> false
            else -> null
        }

        else -> null
    } ?: def
}

internal fun getIntStrict(
    raw: Any?,
    def: Int
): Int {
    return when (raw) {
        is Int -> raw
        is Number -> raw.toInt()
        is String -> raw.toIntOrNull()
        else -> null
    } ?: def
}

internal fun getFloatStrict(
    raw: Any?,
    def: Float
): Float {
    return when (raw) {
        is Float -> raw
        is Number -> raw.toFloat()
        is String -> raw.toFloatOrNull()
        else -> null
    } ?: def
}

internal fun getLongStrict(
    raw: Any?,
    def: Long
): Long {
    return when (raw) {
        is Long -> raw
        is Number -> raw.toLong()
        is String -> raw.toLongOrNull()
        else -> null
    } ?: def
}


internal fun getDoubleStrict(
    raw: Any?,
    def: Double
): Double {
    return when (raw) {
        is Double -> raw
        is Number -> raw.toDouble()
        is String -> raw.toDoubleOrNull()
        else -> null
    } ?: def
}

internal fun getStringStrict(
    raw: Any?,
    def: String
): String {
    return when (raw) {
        is String -> raw
        null -> def
        else -> raw.toString()
    }
}


//internal fun getJsonObjectStrict(
//    raw: Any?,
//    def: JSONObject
//): JSONObject {
//    return when (raw) {
//        is JSONObject -> raw
//        null -> def
//        is String -> try {
//            JSONObject(raw)
//        } catch (e: Exception) {
//            logE(BACKUP_TAG, e) { "Failed to decode item as JSONObject" }
//            def
//        }
//        else -> try {
//            JSONObject(raw.toString())
//        } catch (e: Exception) {
//            logE(BACKUP_TAG, e) { "Failed to decode item as JSONObject" }
//            def
//        }
//    }
//}

internal fun getActionStrict(
    raw: Any?,
    def: Action
): Action {
    return when (raw) {
        is String -> ActionJson.decode(raw)
        else -> ActionJson.decode(raw.toString())
    } ?: def
}

internal fun getPointStrict(
    raw: Any?,
    def: Point
): Point {
    return when (raw) {
        is String -> PointsJson.decode(raw)
        else -> PointsJson.decode(raw.toString())
    } ?: def
}

internal fun getStringSetStrict(
    raw: Any?,
    def: Set<String>
): Set<String> {
    return when (raw) {
        is Set<*> -> raw.flattenStrings().toSet()
        is List<*> -> raw.flattenStrings().toSet()
        is String -> {
            // Parse "[a,b,c]" → ["a","b","c"]
            try {
                // Extract content between [ ] and split by comma
                val clean = raw.trim().removeSurrounding("[", "]")
                if (clean.isBlank()) return emptySet()

                clean.split(",")
                    .map { it.trim().trim('"').trim('\'') }
                    .filter { it.isNotBlank() }
                    .toSet()
            } catch (_: Exception) {
                setOf(raw)
            }
        }

        else -> null
    } ?: def
}

internal fun getStringListStrict(
    raw: Any?,
    def: List<String>
): List<String> {
    return try {
        with(raw.toString()) {
            val clean = trim()
            if (clean.isBlank()) return emptyList()
            clean.split(",")
                .map { it.trim().trim('"').trim('\'') }
                .filter { it.isNotBlank() }
        }
    } catch (_: Exception) {
        def
    }
}


private fun Collection<*>.flattenStrings(): List<String> = flatMap { item ->
    when (item) {
        is String -> listOf(item)
        is Collection<*> -> item.flattenStrings()
        else -> emptyList()
    }
}.filter { it.isNotBlank() }

internal fun <E : Enum<E>> getEnumStrict(
    raw: Any?,
    def: E,
    enumClass: Class<E>
): E {
    return enumClass.enumConstants
        ?.firstOrNull { it.name == raw }
        ?: def
}

/**
 * Decodes a list of enum from a string, comma separated statements
 */
internal fun <E : Enum<E>> getEnumListStrict(
    raw: Any?,
    def: List<E>,
    enumClass: Class<E>
): List<E> {

    return when (raw) {
        is String ->
            try {
                raw
                    .takeIf { it.isNotEmpty() }
                    ?.split(",")
                    ?.mapNotNull { elem ->
                        enumClass.enumConstants
                            ?.firstOrNull { it.name == elem.trim() }
                    }.orEmpty()
            } catch (e: Exception) {
                logE(ANGLE_LINE_TAG, e) { "Failed to decode enumClass $enumClass object, using default value" }
                null
            }

        else -> null
    } ?: def
}


internal fun getColorStrict(
    raw: Any?,
    def: Color
): Color {
    return when (raw) {
        null -> null
        // Old storage format
        is Int -> Color(raw)
        is Number -> Color(raw.toInt())
        // New readable format, fallbacks to old format
        is String -> {
            raw.toLongOrNull(16)
                ?.let { Color(it.toInt()) }
        }

        else -> null
    } ?: def
}

internal fun MutableMap<String, Any>.putIfNonDefault(
    key: String,
    value: Any?,
    def: Any?
) {
    if (value != null && value != def) {
        put(key, value)
    }
}

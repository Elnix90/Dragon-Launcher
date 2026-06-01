package org.elnix.dragonlauncher.settings.bases

import androidx.compose.ui.graphics.Color
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import org.elnix.dragonlauncher.base.model.serializables.Action
import org.elnix.dragonlauncher.base.model.serializables.Action.Companion.ActionJson
import org.elnix.dragonlauncher.base.model.serializables.IconShape
import org.elnix.dragonlauncher.base.model.serializables.IconShape.Companion.IconShapeJson
import org.elnix.dragonlauncher.base.model.serializables.Point
import org.elnix.dragonlauncher.base.model.serializables.Point.Companion.PointsJson
import org.elnix.dragonlauncher.base.util.ColorUtils.toHexWithAlpha
import org.elnix.dragonlauncher.logging.BACKUP_TAG
import org.elnix.dragonlauncher.logging.logI
import org.elnix.dragonlauncher.settings.bases.objects.BaseSettingObject
import org.elnix.dragonlauncher.settings.bases.stores.BaseSettingsStore
import org.elnix.dragonlauncher.settings.bases.stores.MapSettingsStore


/**
 * Factory functions for creating typed [org.elnix.dragonlauncher.settings.bases.objects.BaseSettingObject] instances backed by DataStore.
 *
 * This file provides convenient functions to create strongly-typed settings without
 * manually specifying generic parameters or creating dedicated subclasses for every type.
 *
 * Supported types include:
 * - Primitive types: [Boolean], [Int], [Long], [Float], [Double], [String], [Set]
 * - Enum types: any [Enum] using its name as the stored string
 * - Complex types: [androidx.compose.ui.graphics.Color] , [Point] and [Action], with proper encode/decode handling
 *
 * Each function returns a [org.elnix.dragonlauncher.settings.bases.objects.BaseSettingObject] which can be used to get/set values, reset
 * the setting, or observe changes via flows.
 *
 * Example usage:
 * ```
 * val primaryColor = Settings.color(
 *     key = "primaryColor",
 *     default = Color.Purple
 * )
 *
 * val someAction = Settings.Action(
 *     key = "someAction",
 *     default = Action.OpenDragonLauncherSettings
 * )
 * ```
 */

/**
 * Creates Boolean [BaseSettingObject]
 * stored in the datastore using the built-in [booleanPreferencesKey]
 */
fun MapSettingsStore.boolean(
    key: String,
    default: Boolean,
    onChange: (() -> Unit)? = null
): BaseSettingObject<Boolean, Boolean> =
    BaseSettingObject(
        key = key,
        dataStoreName = dataStoreName,
        default = default,
        preferenceKey = booleanPreferencesKey(key),
        encode = { it },
        decode = { raw -> getBooleanStrict(raw, default) },
        onChanged = onChange
    )


/**
 * Creates int [BaseSettingObject]
 */
fun MapSettingsStore.int(
    key: String,
    default: Int,
    allowedRange: ClosedRange<Int>,
    onChange: (() -> Unit)? = null
): BaseSettingObject<Int, Int> =
    BaseSettingObject(
        key = key,
        dataStoreName = dataStoreName,
        default = default,
        preferenceKey = intPreferencesKey(key),
        encode = { it },
        decode = { raw -> getIntStrict(raw, default).coerceIn(allowedRange) },
        onChanged = onChange
    )


fun MapSettingsStore.float(
    key: String,
    default: Float,
    allowedRange: ClosedRange<Float>,
    onChange: (() -> Unit)? = null
): BaseSettingObject<Float, Float> =
    BaseSettingObject(
        key = key,
        dataStoreName = dataStoreName,
        default = default,
        preferenceKey = floatPreferencesKey(key),
        encode = { it },
        decode = { raw -> getFloatStrict(raw, default).coerceIn(allowedRange) },
        onChanged = onChange
    )

fun MapSettingsStore.long(
    key: String,
    default: Long,
    allowedRange: ClosedRange<Long>,
    onChange: (() -> Unit)? = null
): BaseSettingObject<Long, Long> =
    BaseSettingObject(
        key = key,
        dataStoreName = dataStoreName,
        default = default,
        preferenceKey = longPreferencesKey(key),
        encode = { it },
        decode = { raw -> getLongStrict(raw, default).coerceIn(allowedRange) },
        onChanged = onChange
    )

@Suppress("unused")
fun MapSettingsStore.double(
    key: String,
    default: Double,
    allowedRange: ClosedRange<Double>,
    onChange: (() -> Unit)? = null
): BaseSettingObject<Double, Double> =
    BaseSettingObject(
        key = key,
        dataStoreName = dataStoreName,
        default = default,
        preferenceKey = doublePreferencesKey(key),
        encode = { it },
        decode = { raw -> getDoubleStrict(raw, default).coerceIn(allowedRange) },
        onChanged = onChange
    )


fun BaseSettingsStore<*, *>.string(
    key: String,
    default: String,
    onChange: (() -> Unit)? = null
): BaseSettingObject<String, String> =
    BaseSettingObject(
        key = key,
        dataStoreName = dataStoreName,
        default = default,
        preferenceKey = stringPreferencesKey(key),
        encode = { it },
        decode = { raw -> getStringStrict(raw, default) },
        onChanged = onChange
    )

//    fun jsonObject(
//        key: String,
//        dataStoreName: DatastoreProvider,
//        default: JSONObject,
//        onChange: (() -> Unit)? = null
//    ): BaseSettingObject<JSONObject, String> =
//        BaseSettingObject(
//            key = key,
//            dataStoreName = dataStoreName,
//            default = default,
//            preferenceKey = stringPreferencesKey(key),
//            encode = { it.toString() },
//            decode = { raw -> getJsonObjectStrict(raw, default) },
//            onChanged = onChange
//        )

fun MapSettingsStore.stringSet(
    key: String,
    default: Set<String>,
    onChange: (() -> Unit)? = null
): BaseSettingObject<Set<String>, Set<String>> =
    BaseSettingObject(
        key = key,
        dataStoreName = dataStoreName,
        default = default,
        preferenceKey = stringSetPreferencesKey(key),
        encode = { it },
        decode = { raw -> getStringSetStrict(raw, default) },
        onChanged = onChange
    )

fun MapSettingsStore.stringList(
    key: String,
    default: List<String>,
    onChange: (() -> Unit)? = null
): BaseSettingObject<List<String>, String> =
    BaseSettingObject(
        key = key,
        dataStoreName = dataStoreName,
        default = default,
        preferenceKey = stringPreferencesKey(key),
        encode = { list ->

            val encoded = list.joinToString(",")
            logI(BACKUP_TAG) { "Encoded: $encoded" }
            encoded
        },
        decode = { raw -> getStringListStrict(raw, default) },
        onChanged = onChange
    )

fun <E : Enum<E>> MapSettingsStore.enum(
    key: String,
    default: E,
    enumClass: Class<E>,
    onChange: (() -> Unit)? = null
): BaseSettingObject<E, String> =
    BaseSettingObject(
        key = key,
        dataStoreName = dataStoreName,
        default = default,
        preferenceKey = stringPreferencesKey(key),
        encode = { it.name },
        decode = { raw -> getEnumStrict(raw, default, enumClass) },
        onChanged = onChange
    )

@Suppress("unused")
fun <E : Enum<E>> MapSettingsStore.enumList(
    key: String,
    default: List<E>,
    enumClass: Class<E>,
    onChange: (() -> Unit)? = null
): BaseSettingObject<List<E>, String> =
    BaseSettingObject(
        key = key,
        dataStoreName = dataStoreName,
        default = default,
        preferenceKey = stringPreferencesKey(key),
        encode = { list ->
            list.joinToString(",") { it.name }
        },
        decode = { raw -> getEnumListStrict(raw, default, enumClass) },
        onChanged = onChange
    )


fun MapSettingsStore.color(
    key: String,
    default: Color,
    onChange: (() -> Unit)? = null
): BaseSettingObject<Color, String> =
    BaseSettingObject(
        key = key,
        dataStoreName = dataStoreName,
        default = default,
        preferenceKey = stringPreferencesKey(key),
        encode = { it.toHexWithAlpha(false) },
        decode = { raw -> getColorStrict(raw, default) },
        onChanged = onChange
    )

fun MapSettingsStore.action(
    key: String,
    default: Action,
    onChange: (() -> Unit)? = null
): BaseSettingObject<Action, String> =
    BaseSettingObject(
        key = key,
        dataStoreName = dataStoreName,
        default = default,
        preferenceKey = stringPreferencesKey(key),
        encode = { raw -> ActionJson.encode(raw) },
        decode = { raw -> getActionStrict(raw, default) },
        onChanged = onChange
    )


fun MapSettingsStore.point(
    key: String,
    default: Point,
    onChange: (() -> Unit)? = null
): BaseSettingObject<Point, String> =
    BaseSettingObject(
        key = key,
        dataStoreName = dataStoreName,
        default = default,
        preferenceKey = stringPreferencesKey(key),
        encode = { raw -> PointsJson.encode(raw) },
        decode = { raw -> getPointStrict(raw, default) },
        onChanged = onChange
    )

fun MapSettingsStore.shape(
    key: String,
    default: IconShape,
    onChange: (() -> Unit)? = null
): BaseSettingObject<IconShape, String> =
    BaseSettingObject(
        key = key,
        dataStoreName = dataStoreName,
        default = default,
        preferenceKey = stringPreferencesKey(key),
        encode = { value -> IconShapeJson.encode(value) },
        decode = { raw -> IconShapeJson.decode(raw, default) },
        onChanged = onChange
    )


package org.elnix.dragonlauncher.settings.bases

import androidx.compose.ui.graphics.Color
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import org.elnix.dragonlauncher.base.ColorUtils.toHexWithAlpha
import org.elnix.dragonlauncher.common.messyfolder.Constants.Logging.BACKUP_TAG
import org.elnix.dragonlauncher.common.serializables.IconShape
import org.elnix.dragonlauncher.common.serializables.IconShapeJson
import org.elnix.dragonlauncher.common.serializables.SwipeAction
import org.elnix.dragonlauncher.common.serializables.SwipeJson
import org.elnix.dragonlauncher.logging.logI
import org.elnix.dragonlauncher.settings.bases.objects.BaseSettingObject
import org.elnix.dragonlauncher.settings.bases.stores.BaseSettingsStore

/**
 * Factory object for creating typed [org.elnix.dragonlauncher.settings.bases.objects.BaseSettingObject] instances backed by DataStore.
 *
 * This object provides convenient functions to create strongly-typed settings without
 * manually specifying generic parameters or creating dedicated subclasses for every type.
 *
 * Supported types include:
 * - Primitive types: [Boolean], [Int], [Long], [Float], [Double], [String], [Set<String>]
 * - Enum types: any [Enum] using its name as the stored string
 * - Complex types: [androidx.compose.ui.graphics.Color] and [org.elnix.dragonlauncher.common.serializables.SwipeAction], with proper encode/decode handling
 *
 * Each function returns a [org.elnix.dragonlauncher.settings.bases.objects.BaseSettingObject] which can be used to get/set values, reset
 * the setting, or observe changes via flows.
 *
 * Example usage:
 * ```
 * val primaryColor = Settings.color(
 *     key = "primary_color",
 *     dataStoreName = dataStoreName,
 *     default = AmoledDefault.Primary
 * )
 *
 * val swipeAction = Settings.swipeAction(
 *     key = "main_swipe_action",
 *     dataStoreName = dataStoreName,
 *     default = SwipeActionSerializable.OpenDragonLauncherSettings
 * )
 * ```
 */

fun BaseSettingsStore<*, *>.boolean(
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

fun BaseSettingsStore<*, *>.int(
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


fun BaseSettingsStore<*, *>.float(
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

fun BaseSettingsStore<*, *>.long(
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
fun BaseSettingsStore<*, *>.double(
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

fun BaseSettingsStore<*, *>.stringSet(
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

fun BaseSettingsStore<*, *>.stringList(
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

fun <E : Enum<E>> BaseSettingsStore<*, *>.enum(
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
fun <E : Enum<E>> BaseSettingsStore<*, *>.enumList(
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


fun BaseSettingsStore<*, *>.color(
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

fun BaseSettingsStore<*, *>.swipeAction(
    key: String,
    default: SwipeAction,
    onChange: (() -> Unit)? = null
): BaseSettingObject<SwipeAction, String> =
    BaseSettingObject(
        key = key,
        dataStoreName = dataStoreName,
        default = default,
        preferenceKey = stringPreferencesKey(key),
        encode = { raw -> SwipeJson.encodeAction(raw) },
        decode = { raw -> getSwipeActionSerializableStrict(raw, default) },
        onChanged = onChange
    )


fun BaseSettingsStore<*, *>.shape(
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


package org.elnix.dragonlauncher.settings.bases.objects

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.datastore.preferences.core.Preferences
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
import org.elnix.dragonlauncher.settings.DataStoreName
import org.elnix.dragonlauncher.settings.bases.getActionStrict
import org.elnix.dragonlauncher.settings.bases.getBooleanStrict
import org.elnix.dragonlauncher.settings.bases.getColorStrict
import org.elnix.dragonlauncher.settings.bases.getDoubleStrict
import org.elnix.dragonlauncher.settings.bases.getDpStrict
import org.elnix.dragonlauncher.settings.bases.getEnumListStrict
import org.elnix.dragonlauncher.settings.bases.getEnumStrict
import org.elnix.dragonlauncher.settings.bases.getFloatStrict
import org.elnix.dragonlauncher.settings.bases.getIntStrict
import org.elnix.dragonlauncher.settings.bases.getLongStrict
import org.elnix.dragonlauncher.settings.bases.getPointStrict
import org.elnix.dragonlauncher.settings.bases.getStringListStrict
import org.elnix.dragonlauncher.settings.bases.getStringSetStrict
import org.elnix.dragonlauncher.settings.bases.getStringStrict
import org.elnix.dragonlauncher.settings.bases.stores.BaseSettingsStore
import org.elnix.dragonlauncher.settings.bases.stores.MapSettingsStore


/**
 * Factory functions for creating typed [BaseSettingObject] instances backed by DataStore.
 *
 * This file provides convenient functions to create strongly-typed settings without
 * manually specifying generic parameters or creating dedicated subclasses for every type.
 *
 * Supported types include:
 * - Primitive types: [Boolean], [Int], [Long], [Float], [Double], [String], [Set]
 * - Enum types: any [Enum] using its name as the stored string
 * - Complex types: [Color] , [Point] and [Action], with proper encode/decode handling
 *
 * Each function returns a [BaseSettingObject] which can be used to get/set values, reset
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
data class BooleanSettingObject(
    override val key: String,
    override val default: Boolean,
    override val dataStoreName: DataStoreName,
    override var onChanged: (() -> Unit)?
) : BaseSettingObject<Boolean, Boolean>() {
    override val preferenceKey: Preferences.Key<Boolean>
        get() = booleanPreferencesKey(key)
    override val encode: (Boolean) -> Boolean?
        get() = { it }
    override val decode: (Any?) -> Boolean
        get() = { raw -> getBooleanStrict(raw, default) }

    companion object {
        fun MapSettingsStore.boolean(
            key: String,
            default: Boolean,
            onChange: (() -> Unit)? = null
        ): BooleanSettingObject =
            BooleanSettingObject(
                key = key,
                dataStoreName = dataStoreName,
                default = default,
                onChanged = onChange
            )
    }
}




data class IntSettingObject(
    override val key: String,
    override val default: Int,
    override val dataStoreName: DataStoreName,
    override var onChanged: (() -> Unit)?,
    val allowedRange: ClosedRange<Int>
) : BaseSettingObject<Int, Int>() {

    override val preferenceKey: Preferences.Key<Int>
        get() = intPreferencesKey(key)

    override val encode: (Int) -> Int?
        get() = { it }

    override val decode: (Any?) -> Int
        get() = { raw -> getIntStrict(raw, default).coerceIn(allowedRange) }

    companion object {
        fun MapSettingsStore.int(
            key: String,
            default: Int,
            allowedRange: ClosedRange<Int>,
            onChange: (() -> Unit)? = null
        ): IntSettingObject =
            IntSettingObject(
                key = key,
                dataStoreName = dataStoreName,
                default = default,
                allowedRange = allowedRange,
                onChanged = onChange
            )
    }
}

data class DpSettingObject(
    override val key: String,
    override val default: Dp,
    override val dataStoreName: DataStoreName,
    override var onChanged: (() -> Unit)?,
    val allowedRange: ClosedRange<Dp>
) : BaseSettingObject<Dp, Int>() {

    override val preferenceKey: Preferences.Key<Int>
        get() = intPreferencesKey(key)

    override val encode: (Dp) -> Int?
        get() = { it.value.toInt() }

    override val decode: (Any?) -> Dp
        get() = { raw -> getDpStrict(raw, default).coerceIn(allowedRange) }

    companion object {
        fun MapSettingsStore.dp(
            key: String,
            default: Dp,
            allowedRange: ClosedRange<Dp>,
            onChange: (() -> Unit)? = null
        ): DpSettingObject =
            DpSettingObject(
                key = key,
                dataStoreName = dataStoreName,
                default = default,
                allowedRange = allowedRange,
                onChanged = onChange
            )
    }
}


data class LongSettingObject(
    override val key: String,
    override val default: Long,
    override val dataStoreName: DataStoreName,
    override var onChanged: (() -> Unit)?,
    val allowedRange: ClosedRange<Long>
) : BaseSettingObject<Long, Long>() {
    override val preferenceKey: Preferences.Key<Long>
        get() = longPreferencesKey(key)
    override val encode: (Long) -> Long?
        get() = { it }
    override val decode: (Any?) -> Long
        get() = { raw -> getLongStrict(raw, default).coerceIn(allowedRange) }

    companion object {
        fun MapSettingsStore.long(
            key: String,
            default: Long,
            allowedRange: ClosedRange<Long>,
            onChange: (() -> Unit)? = null
        ): LongSettingObject =
            LongSettingObject(
                key = key,
                dataStoreName = dataStoreName,
                default = default,
                allowedRange = allowedRange,
                onChanged = onChange
            )
    }
}

data class FloatSettingObject(
    override val key: String,
    override val default: Float,
    override val dataStoreName: DataStoreName,
    override var onChanged: (() -> Unit)?,
    val allowedRange: ClosedRange<Float>
) : BaseSettingObject<Float, Float>() {
    override val preferenceKey: Preferences.Key<Float>
        get() = floatPreferencesKey(key)
    override val encode: (Float) -> Float?
        get() = { it }
    override val decode: (Any?) -> Float
        get() = { raw -> getFloatStrict(raw, default).coerceIn(allowedRange) }

    companion object {
        fun MapSettingsStore.float(
            key: String,
            default: Float,
            allowedRange: ClosedRange<Float>,
            onChange: (() -> Unit)? = null
        ): FloatSettingObject =
            FloatSettingObject(
                key = key,
                dataStoreName = dataStoreName,
                default = default,
                allowedRange = allowedRange,
                onChanged = onChange
            )
    }
}

data class DoubleSettingObject(
    override val key: String,
    override val default: Double,
    override val dataStoreName: DataStoreName,
    override var onChanged: (() -> Unit)?,
    val allowedRange: ClosedRange<Double>
) : BaseSettingObject<Double, Double>() {
    override val preferenceKey: Preferences.Key<Double>
        get() = doublePreferencesKey(key)
    override val encode: (Double) -> Double?
        get() = { it }
    override val decode: (Any?) -> Double
        get() = { raw -> getDoubleStrict(raw, default).coerceIn(allowedRange) }

    companion object {
        fun MapSettingsStore.double(
            key: String,
            default: Double,
            allowedRange: ClosedRange<Double>,
            onChange: (() -> Unit)? = null
        ): DoubleSettingObject =
            DoubleSettingObject(
                key = key,
                dataStoreName = dataStoreName,
                default = default,
                allowedRange = allowedRange,
                onChanged = onChange
            )
    }
}

data class StringSettingObject(
    override val key: String,
    override val default: String,
    override val dataStoreName: DataStoreName,
    override var onChanged: (() -> Unit)?
) : BaseSettingObject<String, String>() {
    override val preferenceKey: Preferences.Key<String>
        get() = stringPreferencesKey(key)
    override val encode: (String) -> String?
        get() = { it }
    override val decode: (Any?) -> String
        get() = { raw -> getStringStrict(raw, default) }

    companion object {
        fun BaseSettingsStore<*, *>.string(
            key: String,
            default: String,
            onChange: (() -> Unit)? = null
        ): StringSettingObject =
            StringSettingObject(
                key = key,
                dataStoreName = dataStoreName,
                default = default,
                onChanged = onChange
            )
    }
}

data class StringSetSettingObject(
    override val key: String,
    override val default: Set<String>,
    override val dataStoreName: DataStoreName,
    override var onChanged: (() -> Unit)?
) : BaseSettingObject<Set<String>, Set<String>>() {
    override val preferenceKey: Preferences.Key<Set<String>>
        get() = stringSetPreferencesKey(key)
    override val encode: (Set<String>) -> Set<String>?
        get() = { it }
    override val decode: (Any?) -> Set<String>
        get() = { raw -> getStringSetStrict(raw, default) }

    companion object {
        fun MapSettingsStore.stringSet(
            key: String,
            default: Set<String>,
            onChange: (() -> Unit)? = null
        ): StringSetSettingObject =
            StringSetSettingObject(
                key = key,
                dataStoreName = dataStoreName,
                default = default,
                onChanged = onChange
            )
    }
}

data class StringListSettingObject(
    override val key: String,
    override val default: List<String>,
    override val dataStoreName: DataStoreName,
    override var onChanged: (() -> Unit)?
) : BaseSettingObject<List<String>, String>() {
    override val preferenceKey: Preferences.Key<String>
        get() = stringPreferencesKey(key)
    override val encode: (List<String>) -> String?
        get() = { list -> list.joinToString(",") }
    override val decode: (Any?) -> List<String>
        get() = { raw -> getStringListStrict(raw, default) }

    companion object {
        fun MapSettingsStore.stringList(
            key: String,
            default: List<String>,
            onChange: (() -> Unit)? = null
        ): StringListSettingObject =
            StringListSettingObject(
                key = key,
                dataStoreName = dataStoreName,
                default = default,
                onChanged = onChange
            )
    }
}

data class EnumSettingObject<E : Enum<E>>(
    override val key: String,
    override val default: E,
    override val dataStoreName: DataStoreName,
    override var onChanged: (() -> Unit)?,
    val enumClass: Class<E>
) : BaseSettingObject<E, String>() {
    override val preferenceKey: Preferences.Key<String>
        get() = stringPreferencesKey(key)
    override val encode: (E) -> String?
        get() = { it.name }
    override val decode: (Any?) -> E
        get() = { raw -> getEnumStrict(raw, default, enumClass) }

    companion object {
        fun <E : Enum<E>> MapSettingsStore.enum(
            key: String,
            default: E,
            enumClass: Class<E>,
            onChange: (() -> Unit)? = null
        ): EnumSettingObject<E> =
            EnumSettingObject(
                key = key,
                dataStoreName = dataStoreName,
                default = default,
                enumClass = enumClass,
                onChanged = onChange
            )
    }
}

data class EnumListSettingObject<E : Enum<E>>(
    override val key: String,
    override val default: List<E>,
    override val dataStoreName: DataStoreName,
    override var onChanged: (() -> Unit)?,
    val enumClass: Class<E>
) : BaseSettingObject<List<E>, String>() {
    override val preferenceKey: Preferences.Key<String>
        get() = stringPreferencesKey(key)
    override val encode: (List<E>) -> String?
        get() = { list -> list.joinToString(",") { it.name } }
    override val decode: (Any?) -> List<E>
        get() = { raw -> getEnumListStrict(raw, default, enumClass) }

    companion object {
        fun <E : Enum<E>> MapSettingsStore.enumList(
            key: String,
            default: List<E>,
            enumClass: Class<E>,
            onChange: (() -> Unit)? = null
        ): EnumListSettingObject<E> =
            EnumListSettingObject(
                key = key,
                dataStoreName = dataStoreName,
                default = default,
                enumClass = enumClass,
                onChanged = onChange
            )
    }
}

data class ColorSettingObject(
    override val key: String,
    override val default: Color,
    override val dataStoreName: DataStoreName,
    override var onChanged: (() -> Unit)?
) : BaseSettingObject<Color, String>() {
    override val preferenceKey: Preferences.Key<String>
        get() = stringPreferencesKey(key)
    override val encode: (Color) -> String?
        get() = { it.toHexWithAlpha(false) }
    override val decode: (Any?) -> Color
        get() = { raw -> getColorStrict(raw, default) }

    companion object {
        fun MapSettingsStore.color(
            key: String,
            default: Color,
            onChange: (() -> Unit)? = null
        ): ColorSettingObject =
            ColorSettingObject(
                key = key,
                dataStoreName = dataStoreName,
                default = default,
                onChanged = onChange
            )
    }
}

data class ActionSettingObject(
    override val key: String,
    override val default: Action,
    override val dataStoreName: DataStoreName,
    override var onChanged: (() -> Unit)?
) : BaseSettingObject<Action, String>() {
    override val preferenceKey: Preferences.Key<String>
        get() = stringPreferencesKey(key)
    override val encode: (Action) -> String?
        get() = { raw -> ActionJson.encode(raw) }
    override val decode: (Any?) -> Action
        get() = { raw -> getActionStrict(raw, default) }

    companion object {
        fun MapSettingsStore.action(
            key: String,
            default: Action,
            onChange: (() -> Unit)? = null
        ): ActionSettingObject =
            ActionSettingObject(
                key = key,
                dataStoreName = dataStoreName,
                default = default,
                onChanged = onChange
            )
    }
}

data class PointSettingObject(
    override val key: String,
    override val default: Point,
    override val dataStoreName: DataStoreName,
    override var onChanged: (() -> Unit)?
) : BaseSettingObject<Point, String>() {
    override val preferenceKey: Preferences.Key<String>
        get() = stringPreferencesKey(key)
    override val encode: (Point) -> String?
        get() = { raw -> PointsJson.encode(raw) }
    override val decode: (Any?) -> Point
        get() = { raw -> getPointStrict(raw, default) }

    companion object {
        fun MapSettingsStore.point(
            key: String,
            default: Point,
            onChange: (() -> Unit)? = null
        ): PointSettingObject =
            PointSettingObject(
                key = key,
                dataStoreName = dataStoreName,
                default = default,
                onChanged = onChange
            )
    }
}

data class IconShapeSettingObject(
    override val key: String,
    override val default: IconShape,
    override val dataStoreName: DataStoreName,
    override var onChanged: (() -> Unit)?
) : BaseSettingObject<IconShape, String>() {
    override val preferenceKey: Preferences.Key<String>
        get() = stringPreferencesKey(key)
    override val encode: (IconShape) -> String?
        get() = { value -> IconShapeJson.encode(value) }
    override val decode: (Any?) -> IconShape
        get() = { raw -> IconShapeJson.decode(raw, default) }

    companion object {
        fun MapSettingsStore.shape(
            key: String,
            default: IconShape,
            onChange: (() -> Unit)? = null
        ): IconShapeSettingObject =
            IconShapeSettingObject(
                key = key,
                dataStoreName = dataStoreName,
                default = default,
                onChanged = onChange
            )
    }
}
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
import org.elnix.dragonlauncher.settings.bases.stores.JsonArraySettingsStore
import org.elnix.dragonlauncher.settings.bases.stores.JsonObjectSettingsStore
import org.elnix.dragonlauncher.settings.bases.stores.MapSettingsStore
import kotlin.properties.ReadOnlyProperty


data class BooleanSettingObject(
    override val key: String,
    override val default: Boolean,
    override val title: Int?,
    override val description: Int?,
    override val dataStoreName: DataStoreName,
    override var onChanged: (() -> Unit)?
) : BaseSettingObject<Boolean, Boolean>() {

    override val preferenceKey: Preferences.Key<Boolean>
        get() = booleanPreferencesKey(key)

    override fun encode(value: Boolean): Boolean = value
    override fun decode(raw: Any?): Boolean = getBooleanStrict(raw, default)

    companion object {
        inline fun <reified T> MapSettingsStore.boolean(
            title: Int?,
            description: Int?,
            default: Boolean,
            noinline onChange: (() -> Unit)? = null
        ): ReadOnlyProperty<T, BooleanSettingObject> =
            ReadOnlyProperty { _, property ->
                BooleanSettingObject(
                    key = property.name,
                    title = title,
                    description = description,
                    dataStoreName = dataStoreName,
                    default = default,
                    onChanged = onChange
                )
            }
    }
}


data class IntSettingObject(
    override val key: String,
    override val default: Int,
    override val title: Int?,
    override val description: Int?,
    override val dataStoreName: DataStoreName,
    override var onChanged: (() -> Unit)?,
    val allowedRange: IntRange
) : BaseSettingObject<Int, Int>() {

    override val preferenceKey: Preferences.Key<Int>
        get() = intPreferencesKey(key)

    override fun encode(value: Int): Int = value
    override fun decode(raw: Any?): Int = getIntStrict(raw, default).coerceIn(allowedRange)

    companion object {
        inline fun <reified T> MapSettingsStore.int(
            title: Int?,
            description: Int?,
            default: Int,
            allowedRange: IntRange,
            noinline onChange: (() -> Unit)? = null
        ): ReadOnlyProperty<T, IntSettingObject> =
            ReadOnlyProperty { _, property ->
                IntSettingObject(
                    key = property.name,
                    title = title,
                    description = description,
                    dataStoreName = dataStoreName,
                    default = default,
                    allowedRange = allowedRange,
                    onChanged = onChange
                )
            }
    }
}

data class DpSettingObject(
    override val key: String,
    override val default: Dp,
    override val title: Int?,
    override val description: Int?,
    override val dataStoreName: DataStoreName,
    override var onChanged: (() -> Unit)?,
    val allowedRange: ClosedRange<Dp>
) : BaseSettingObject<Dp, Int>() {

    override val preferenceKey: Preferences.Key<Int>
        get() = intPreferencesKey(key)

    override fun encode(value: Dp): Int = value.value.toInt()
    override fun decode(raw: Any?): Dp = getDpStrict(raw, default).coerceIn(allowedRange)

    companion object {
        inline fun <reified T> MapSettingsStore.dp(
            title: Int?,
            description: Int?,
            default: Dp,
            allowedRange: ClosedRange<Dp>,
            noinline onChange: (() -> Unit)? = null
        ): ReadOnlyProperty<T, DpSettingObject> =
            ReadOnlyProperty { _, property ->
                DpSettingObject(
                    key = property.name,
                    title = title,
                    description = description,
                    dataStoreName = dataStoreName,
                    default = default,
                    allowedRange = allowedRange,
                    onChanged = onChange
                )
            }
    }
}

data class LongSettingObject(
    override val key: String,
    override val default: Long,
    override val title: Int?,
    override val description: Int?,
    override val dataStoreName: DataStoreName,
    override var onChanged: (() -> Unit)?,
    val allowedRange: ClosedRange<Long>
) : BaseSettingObject<Long, Long>() {

    override val preferenceKey: Preferences.Key<Long>
        get() = longPreferencesKey(key)

    override fun encode(value: Long): Long = value
    override fun decode(raw: Any?): Long = getLongStrict(raw, default).coerceIn(allowedRange)

    companion object {
        inline fun <reified T> MapSettingsStore.long(
            title: Int?,
            description: Int?,
            default: Long,
            allowedRange: ClosedRange<Long>,
            noinline onChange: (() -> Unit)? = null
        ): ReadOnlyProperty<T, LongSettingObject> =
            ReadOnlyProperty { _, property ->
                LongSettingObject(
                    key = property.name,
                    title = title,
                    description = description,
                    dataStoreName = dataStoreName,
                    default = default,
                    allowedRange = allowedRange,
                    onChanged = onChange
                )
            }
    }
}

data class FloatSettingObject(
    override val key: String,
    override val default: Float,
    override val title: Int?,
    override val description: Int?,
    override val dataStoreName: DataStoreName,
    override var onChanged: (() -> Unit)?,
    val allowedRange: ClosedFloatingPointRange<Float>
) : BaseSettingObject<Float, Float>() {

    override val preferenceKey: Preferences.Key<Float>
        get() = floatPreferencesKey(key)

    override fun encode(value: Float): Float = value
    override fun decode(raw: Any?): Float = getFloatStrict(raw, default).coerceIn(allowedRange)

    companion object {
        inline fun <reified T> MapSettingsStore.float(
            title: Int?,
            description: Int?,
            default: Float,
            allowedRange: ClosedFloatingPointRange<Float>,
            noinline onChange: (() -> Unit)? = null
        ): ReadOnlyProperty<T, FloatSettingObject> =
            ReadOnlyProperty { _, property ->
                FloatSettingObject(
                    key = property.name,
                    title = title,
                    description = description,
                    dataStoreName = dataStoreName,
                    default = default,
                    allowedRange = allowedRange,
                    onChanged = onChange
                )
            }
    }
}

data class DoubleSettingObject(
    override val key: String,
    override val default: Double,
    override val title: Int?,
    override val description: Int?,
    override val dataStoreName: DataStoreName,
    override var onChanged: (() -> Unit)?,
    val allowedRange: ClosedRange<Double>
) : BaseSettingObject<Double, Double>() {

    override val preferenceKey: Preferences.Key<Double>
        get() = doublePreferencesKey(key)

    override fun encode(value: Double): Double = value
    override fun decode(raw: Any?): Double = getDoubleStrict(raw, default).coerceIn(allowedRange)

    companion object {
        inline fun <reified T> MapSettingsStore.double(
            title: Int?,
            description: Int?,
            default: Double,
            allowedRange: ClosedRange<Double>,
            noinline onChange: (() -> Unit)? = null
        ): ReadOnlyProperty<T, DoubleSettingObject> =
            ReadOnlyProperty { _, property ->
                DoubleSettingObject(
                    key = property.name,
                    title = title,
                    description = description,
                    dataStoreName = dataStoreName,
                    default = default,
                    allowedRange = allowedRange,
                    onChanged = onChange
                )
            }
    }
}

data class StringSettingObject(
    override val key: String,
    override val default: String,
    override val title: Int?,
    override val description: Int?,
    override val dataStoreName: DataStoreName,
    override var onChanged: (() -> Unit)?
) : BaseSettingObject<String, String>() {

    override val preferenceKey: Preferences.Key<String>
        get() = stringPreferencesKey(key)

    override fun encode(value: String): String = value
    override fun decode(raw: Any?): String = getStringStrict(raw, default)

    companion object {
        inline fun <reified T> MapSettingsStore.string(
            title: Int?,
            description: Int?,
            default: String,
            noinline onChange: (() -> Unit)? = null
        ): ReadOnlyProperty<T, StringSettingObject> =
            ReadOnlyProperty { _, property ->
                StringSettingObject(
                    key = property.name,
                    title = title,
                    description = description,
                    dataStoreName = dataStoreName,
                    default = default,
                    onChanged = onChange
                )
            }

        inline fun <reified T> JsonObjectSettingsStore.string(
            title: Int?,
            description: Int?,
            default: String,
            noinline onChange: (() -> Unit)? = null
        ): ReadOnlyProperty<T, StringSettingObject> =
            ReadOnlyProperty { _, property ->
                StringSettingObject(
                    key = property.name,
                    title = title,
                    description = description,
                    dataStoreName = dataStoreName,
                    default = default,
                    onChanged = onChange
                )
            }

        inline fun <reified T> JsonArraySettingsStore.string(
            title: Int?,
            description: Int?,
            default: String,
            noinline onChange: (() -> Unit)? = null
        ): ReadOnlyProperty<T, StringSettingObject> =
            ReadOnlyProperty { _, property ->
                StringSettingObject(
                    key = property.name,
                    title = title,
                    description = description,
                    dataStoreName = dataStoreName,
                    default = default,
                    onChanged = onChange
                )
            }
    }
}

data class StringSetSettingObject(
    override val key: String,
    override val default: Set<String>,
    override val title: Int?,
    override val description: Int?,
    override val dataStoreName: DataStoreName,
    override var onChanged: (() -> Unit)?
) : BaseSettingObject<Set<String>, Set<String>>() {

    override val preferenceKey: Preferences.Key<Set<String>>
        get() = stringSetPreferencesKey(key)

    override fun encode(value: Set<String>): Set<String> = value
    override fun decode(raw: Any?): Set<String> = getStringSetStrict(raw, default)

    companion object {
        inline fun <reified T> MapSettingsStore.stringSet(
            title: Int?,
            description: Int?,
            default: Set<String>,
            noinline onChange: (() -> Unit)? = null
        ): ReadOnlyProperty<T, StringSetSettingObject> =
            ReadOnlyProperty { _, property ->
                StringSetSettingObject(
                    key = property.name,
                    title = title,
                    description = description,
                    dataStoreName = dataStoreName,
                    default = default,
                    onChanged = onChange
                )
            }
    }
}

data class StringListSettingObject(
    override val key: String,
    override val default: List<String>,
    override val title: Int?,
    override val description: Int?,
    override val dataStoreName: DataStoreName,
    override var onChanged: (() -> Unit)?
) : BaseSettingObject<List<String>, String>() {

    override val preferenceKey: Preferences.Key<String>
        get() = stringPreferencesKey(key)

    override fun encode(value: List<String>): String = value.joinToString(",")
    override fun decode(raw: Any?): List<String> = getStringListStrict(raw, default)

    companion object {
        inline fun <reified T> MapSettingsStore.stringList(
            title: Int?,
            description: Int?,
            default: List<String>,
            noinline onChange: (() -> Unit)? = null
        ): ReadOnlyProperty<T, StringListSettingObject> =
            ReadOnlyProperty { _, property ->
                StringListSettingObject(
                    key = property.name,
                    title = title,
                    description = description,
                    dataStoreName = dataStoreName,
                    default = default,
                    onChanged = onChange
                )
            }
    }
}

data class EnumSettingObject<E : Enum<E>>(
    override val key: String,
    override val default: E,
    override val title: Int?,
    override val description: Int?,
    override val dataStoreName: DataStoreName,
    override var onChanged: (() -> Unit)?,
    val enumClass: Class<E>
) : BaseSettingObject<E, String>() {

    override val preferenceKey: Preferences.Key<String>
        get() = stringPreferencesKey(key)

    override fun encode(value: E): String = value.name
    override fun decode(raw: Any?): E = getEnumStrict(raw, default, enumClass)

    companion object {
        inline fun <reified T, E : Enum<E>> MapSettingsStore.enum(
            title: Int?,
            description: Int?,
            default: E,
            enumClass: Class<E>,
            noinline onChange: (() -> Unit)? = null
        ): ReadOnlyProperty<T, EnumSettingObject<E>> =
            ReadOnlyProperty { _, property ->
                EnumSettingObject(
                    key = property.name,
                    title = title,
                    description = description,
                    dataStoreName = dataStoreName,
                    default = default,
                    enumClass = enumClass,
                    onChanged = onChange
                )
            }
    }

}

data class EnumListSettingObject<E : Enum<E>>(
    override val key: String,
    override val default: List<E>,
    override val title: Int?,
    override val description: Int?,
    override val dataStoreName: DataStoreName,
    override var onChanged: (() -> Unit)?,
    val enumClass: Class<E>
) : BaseSettingObject<List<E>, String>() {
    override val preferenceKey: Preferences.Key<String>
        get() = stringPreferencesKey(key)

    override fun encode(value: List<E>): String = value.joinToString(",") { it.name }
    override fun decode(raw: Any?): List<E> = getEnumListStrict(raw, default, enumClass)

    companion object {
        inline fun <reified T, E : Enum<E>> MapSettingsStore.enumList(
            title: Int?,
            description: Int?,
            default: List<E>,
            enumClass: Class<E>,
            noinline onChange: (() -> Unit)? = null
        ): ReadOnlyProperty<T, EnumListSettingObject<E>> =
            ReadOnlyProperty { _, property ->
                EnumListSettingObject(
                    key = property.name,
                    title = title,
                    description = description,
                    dataStoreName = dataStoreName,
                    default = default,
                    enumClass = enumClass,
                    onChanged = onChange
                )
            }
    }
}

data class ColorSettingObject(
    override val key: String,
    override val default: Color,
    override val title: Int?,
    override val description: Int?,
    override val dataStoreName: DataStoreName,
    override var onChanged: (() -> Unit)?
) : BaseSettingObject<Color, String>() {

    override val preferenceKey: Preferences.Key<String>
        get() = stringPreferencesKey(key)

    override fun encode(value: Color): String = value.toHexWithAlpha(false)
    override fun decode(raw: Any?): Color = getColorStrict(raw, default)

    companion object {
        inline fun <reified T> MapSettingsStore.color(
            title: Int?,
            description: Int?,
            default: Color,
            noinline onChange: (() -> Unit)? = null
        ): ReadOnlyProperty<T, ColorSettingObject> =
            ReadOnlyProperty { _, property ->
                ColorSettingObject(
                    key = property.name,
                    title = title,
                    description = description,
                    dataStoreName = dataStoreName,
                    default = default,
                    onChanged = onChange
                )
            }
    }
}

data class ActionSettingObject(
    override val key: String,
    override val default: Action,
    override val title: Int?,
    override val description: Int?,
    override val dataStoreName: DataStoreName,
    override var onChanged: (() -> Unit)?
) : BaseSettingObject<Action, String>() {

    override val preferenceKey: Preferences.Key<String>
        get() = stringPreferencesKey(key)

    override fun encode(value: Action): String? = ActionJson.encode(value)
    override fun decode(raw: Any?): Action = getActionStrict(raw, default)

    companion object {
        inline fun <reified T> MapSettingsStore.action(
            title: Int?,
            description: Int?,
            default: Action,
            noinline onChange: (() -> Unit)? = null
        ): ReadOnlyProperty<T, ActionSettingObject> =
            ReadOnlyProperty { _, property ->
                ActionSettingObject(
                    key = property.name,
                    title = title,
                    description = description,
                    dataStoreName = dataStoreName,
                    default = default,
                    onChanged = onChange
                )
            }
    }
}

data class PointSettingObject(
    override val key: String,
    override val default: Point,
    override val title: Int?,
    override val description: Int?,
    override val dataStoreName: DataStoreName,
    override var onChanged: (() -> Unit)?
) : BaseSettingObject<Point, String>() {

    override val preferenceKey: Preferences.Key<String>
        get() = stringPreferencesKey(key)

    override fun encode(value: Point): String? = PointsJson.encode(value)
    override fun decode(raw: Any?): Point = getPointStrict(raw, default)

    companion object {
        inline fun <reified T> MapSettingsStore.point(
            title: Int?,
            description: Int?,
            default: Point,
            noinline onChange: (() -> Unit)? = null
        ): ReadOnlyProperty<T, PointSettingObject> =
            ReadOnlyProperty { _, property ->
                PointSettingObject(
                    key = property.name,
                    title = title,
                    description = description,
                    dataStoreName = dataStoreName,
                    default = default,
                    onChanged = onChange
                )
            }
    }
}

data class IconShapeSettingObject(
    override val key: String,
    override val default: IconShape,
    override val title: Int?,
    override val description: Int?,
    override val dataStoreName: DataStoreName,
    override var onChanged: (() -> Unit)?
) : BaseSettingObject<IconShape, String>() {

    override val preferenceKey: Preferences.Key<String>
        get() = stringPreferencesKey(key)

    override fun encode(value: IconShape): String? = IconShapeJson.encode(value)
    override fun decode(raw: Any?): IconShape = IconShapeJson.decode(raw, default)

    companion object {
        inline fun <reified T> MapSettingsStore.shape(
            title: Int?,
            description: Int?,
            default: IconShape,
            noinline onChange: (() -> Unit)? = null
        ): ReadOnlyProperty<T, IconShapeSettingObject> =
            ReadOnlyProperty { _, property ->
                IconShapeSettingObject(
                    key = property.name,
                    title = title,
                    description = description,
                    dataStoreName = dataStoreName,
                    default = default,
                    onChanged = onChange
                )
            }
    }
}
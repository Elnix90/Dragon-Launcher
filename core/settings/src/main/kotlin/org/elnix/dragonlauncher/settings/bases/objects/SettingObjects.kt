package org.elnix.dragonlauncher.settings.bases.objects

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
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
import org.elnix.dragonlauncher.settings.bases.getDpStrict
import org.elnix.dragonlauncher.settings.bases.getEnumListStrict
import org.elnix.dragonlauncher.settings.bases.getEnumSetStrict
import org.elnix.dragonlauncher.settings.bases.getEnumStrict
import org.elnix.dragonlauncher.settings.bases.getFloatStrict
import org.elnix.dragonlauncher.settings.bases.getIntStrict
import org.elnix.dragonlauncher.settings.bases.getLongStrict
import org.elnix.dragonlauncher.settings.bases.getPointStrict
import org.elnix.dragonlauncher.settings.bases.getStringListStrict
import org.elnix.dragonlauncher.settings.bases.getStringSetStrict
import org.elnix.dragonlauncher.settings.bases.getStringStrict
import org.elnix.dragonlauncher.settings.bases.stores.MapSettingsStore

val String.isNotBlankKey: String
    get() = this.ifEmpty { error("Key cannot be null or empty") }


data class BooleanSettingObject(
    override val key: String,
    override val default: Boolean,
    override val title: Int?,
    override val description: Int?,
    override val dataStoreName: DataStoreName,
    override var onChanged: (() -> Unit)?,
    override val backupable: Boolean
) : SettingObject<Boolean, Boolean>() {

    override val preferenceKey: Preferences.Key<Boolean> = booleanPreferencesKey(key)
    override fun encode(value: Boolean): Boolean = value
    override fun decode(raw: Any?): Boolean = getBooleanStrict(raw, default)

    companion object {
        fun MapSettingsStore.boolean(
            title: Int?,
            description: Int?,
            default: Boolean,
            key: String = "",
            onChanged: (() -> Unit)? = null,
            backupable: Boolean = true
        ) = BooleanSettingObject(
            key = key.isNotBlankKey,
            title = title,
            description = description,
            dataStoreName = dataStoreName,
            default = default,
            onChanged = onChanged,
            backupable = backupable
        )
    }
}


data class IntSettingObject(
    override val key: String,
    override val default: Int,
    override val title: Int?,
    override val description: Int?,
    override val dataStoreName: DataStoreName,
    override var onChanged: (() -> Unit)?,
    override val backupable: Boolean,
    val allowedRange: IntRange
) : SettingObject<Int, Int>() {

    override val preferenceKey: Preferences.Key<Int> = intPreferencesKey(key)
    override fun encode(value: Int): Int = value
    override fun decode(raw: Any?): Int = getIntStrict(raw, default).coerceIn(allowedRange)

    companion object {
        fun MapSettingsStore.int(
            title: Int?,
            description: Int?,
            default: Int,
            allowedRange: IntRange,
            key: String = "",
            onChanged: (() -> Unit)? = null,
            backupable: Boolean = true
        ) = IntSettingObject(
            key = key.isNotBlankKey,
            title = title,
            description = description,
            dataStoreName = dataStoreName,
            default = default,
            allowedRange = allowedRange,
            onChanged = onChanged,
            backupable = backupable
        )
    }
}

data class DpSettingObject(
    override val key: String,
    override val default: Dp,
    override val title: Int?,
    override val description: Int?,
    override val dataStoreName: DataStoreName,
    override var onChanged: (() -> Unit)?,
    override val backupable: Boolean,
    val allowedRange: ClosedRange<Dp>
) : SettingObject<Dp, Int>() {

    override val preferenceKey: Preferences.Key<Int> = intPreferencesKey(key)
    override fun encode(value: Dp): Int = value.value.toInt()
    override fun decode(raw: Any?): Dp = getDpStrict(raw, default).coerceIn(allowedRange)

    companion object {
        fun MapSettingsStore.dp(
            title: Int?,
            description: Int?,
            default: Dp,
            allowedRange: ClosedRange<Dp>,
            key: String = "",
            onChanged: (() -> Unit)? = null,
            backupable: Boolean = true
        ) = DpSettingObject(
            key = key.isNotBlankKey,
            title = title,
            description = description,
            dataStoreName = dataStoreName,
            default = default,
            allowedRange = allowedRange,
            onChanged = onChanged,
            backupable = backupable
        )
    }
}

data class LongSettingObject(
    override val key: String,
    override val default: Long,
    override val title: Int?,
    override val description: Int?,
    override val dataStoreName: DataStoreName,
    override var onChanged: (() -> Unit)?,
    override val backupable: Boolean,
    val allowedRange: ClosedRange<Long>
) : SettingObject<Long, Long>() {

    override val preferenceKey: Preferences.Key<Long> = longPreferencesKey(key)
    override fun encode(value: Long): Long = value
    override fun decode(raw: Any?): Long = getLongStrict(raw, default).coerceIn(allowedRange)

    companion object {
        fun MapSettingsStore.long(
            title: Int?,
            description: Int?,
            default: Long,
            allowedRange: ClosedRange<Long>,
            key: String = "",
            onChanged: (() -> Unit)? = null,
            backupable: Boolean = true
        ) = LongSettingObject(
            key = key.isNotBlankKey,
            title = title,
            description = description,
            dataStoreName = dataStoreName,
            default = default,
            allowedRange = allowedRange,
            onChanged = onChanged,
            backupable = backupable
        )
    }
}

data class FloatSettingObject(
    override val key: String,
    override val default: Float,
    override val title: Int?,
    override val description: Int?,
    override val dataStoreName: DataStoreName,
    override var onChanged: (() -> Unit)?,
    override val backupable: Boolean,
    val allowedRange: ClosedFloatingPointRange<Float>
) : SettingObject<Float, Float>() {

    override val preferenceKey: Preferences.Key<Float> = floatPreferencesKey(key)
    override fun encode(value: Float): Float = value
    override fun decode(raw: Any?): Float = getFloatStrict(raw, default).coerceIn(allowedRange)

    companion object {
        fun MapSettingsStore.float(
            title: Int?,
            description: Int?,
            default: Float,
            allowedRange: ClosedFloatingPointRange<Float>,
            key: String = "",
            onChanged: (() -> Unit)? = null,
            backupable: Boolean = true
        ) = FloatSettingObject(
            key = key.isNotBlankKey,
            title = title,
            description = description,
            dataStoreName = dataStoreName,
            default = default,
            allowedRange = allowedRange,
            onChanged = onChanged,
            backupable = backupable
        )
    }
}

//data class DoubleSettingObject(
//    override val key: String,
//    override val default: Double,
//    override val title: Int?,
//    override val description: Int?,
//    override val dataStoreName: DataStoreName,
//    override var onChanged: (() -> Unit)?,
//    override val backupable: Boolean,
//    val allowedRange: ClosedRange<Double>
//) : BaseSettingObject<Double, Double>() {
//
//    override val preferenceKey: Preferences.Key<Double> = doublePreferencesKey(key)
//    override fun encode(value: Double): Double = value
//    override fun decode(raw: Any?): Double = getDoubleStrict(raw, default).coerceIn(allowedRange)
//
//    companion object {
//        fun MapSettingsStore.double(
//            title: Int?,
//            description: Int?,
//            default: Double,
//            allowedRange: ClosedRange<Double>,
//            key: String = "",
//            onChanged: (() -> Unit)? = null,
//            backupable: Boolean = true
//        ) = DoubleSettingObject(
//            key = key.isNotBlankKey,
//            title = title,
//            description = description,
//            dataStoreName = dataStoreName,
//            default = default,
//            allowedRange = allowedRange,
//            onChanged = onChanged,
//            backupable = backupable
//        )
//    }
//}

data class StringSettingObject(
    override val key: String,
    override val default: String,
    override val title: Int?,
    override val description: Int?,
    override val dataStoreName: DataStoreName,
    override var onChanged: (() -> Unit)?,
    override val backupable: Boolean
) : SettingObject<String, String>() {

    override val preferenceKey: Preferences.Key<String> = stringPreferencesKey(key)
    override fun encode(value: String): String = value
    override fun decode(raw: Any?): String = getStringStrict(raw, default)

    companion object {
        fun MapSettingsStore.string(
            title: Int?,
            description: Int?,
            default: String,
            key: String = "",
            onChanged: (() -> Unit)? = null,
            backupable: Boolean = true
        ) = StringSettingObject(
            key = key.isNotBlankKey,
            title = title,
            description = description,
            dataStoreName = dataStoreName,
            default = default,
            onChanged = onChanged,
            backupable = backupable
        )
    }
}


data class StringSetSettingObject(
    override val key: String,
    override val default: Set<String>,
    override val title: Int?,
    override val description: Int?,
    override val dataStoreName: DataStoreName,
    override var onChanged: (() -> Unit)?,
    override val backupable: Boolean
) : SettingObject<Set<String>, Set<String>>() {

    override val preferenceKey: Preferences.Key<Set<String>> = stringSetPreferencesKey(key)
    override fun encode(value: Set<String>): Set<String> = value
    override fun decode(raw: Any?): Set<String> = getStringSetStrict(raw, default)

    companion object {
        fun MapSettingsStore.stringSet(
            title: Int?,
            description: Int?,
            default: Set<String>,
            key: String = "",
            onChanged: (() -> Unit)? = null,
            backupable: Boolean = true
        ) = StringSetSettingObject(
            key = key.isNotBlankKey,
            title = title,
            description = description,
            dataStoreName = dataStoreName,
            default = default,
            onChanged = onChanged,
            backupable = backupable
        )
    }
}

data class StringListSettingObject(
    override val key: String,
    override val default: List<String>,
    override val title: Int?,
    override val description: Int?,
    override val dataStoreName: DataStoreName,
    override var onChanged: (() -> Unit)?,
    override val backupable: Boolean
) : SettingObject<List<String>, String>() {

    override val preferenceKey: Preferences.Key<String> = stringPreferencesKey(key)
    override fun encode(value: List<String>): String = value.joinToString(",")
    override fun decode(raw: Any?): List<String> = getStringListStrict(raw, default)

    companion object {
        fun MapSettingsStore.stringList(
            title: Int?,
            description: Int?,
            default: List<String>,
            key: String = "",
            onChanged: (() -> Unit)? = null,
            backupable: Boolean = true
        ) = StringListSettingObject(
            key = key.isNotBlankKey,
            title = title,
            description = description,
            dataStoreName = dataStoreName,
            default = default,
            onChanged = onChanged,
            backupable = backupable
        )
    }
}


data class EnumSettingObject<E : Enum<E>>(
    override val key: String,
    override val default: E,
    override val title: Int?,
    override val description: Int?,
    override val dataStoreName: DataStoreName,
    override var onChanged: (() -> Unit)?,
    override val backupable: Boolean,
    val enumClass: Class<E>
) : SettingObject<E, String>() {

    override val preferenceKey: Preferences.Key<String> = stringPreferencesKey(key)
    override fun encode(value: E): String = value.name
    override fun decode(raw: Any?): E = getEnumStrict(raw, default, enumClass)

    companion object {
        inline fun <reified E : Enum<E>> MapSettingsStore.enum(
            title: Int?,
            description: Int?,
            default: E,
            key: String = "",
            noinline onChanged: (() -> Unit)? = null,
            backupable: Boolean = true
        ) = EnumSettingObject(
            key = key.isNotBlankKey,
            title = title,
            description = description,
            dataStoreName = dataStoreName,
            default = default,
            enumClass = E::class.java,
            onChanged = onChanged,
            backupable = backupable
        )
    }
}

data class EnumListSettingObject<E : Enum<E>>(
    override val key: String,
    override val default: List<E>,
    override val title: Int?,
    override val description: Int?,
    override val dataStoreName: DataStoreName,
    override var onChanged: (() -> Unit)?,
    override val backupable: Boolean,
    val enumClass: Class<E>
) : SettingObject<List<E>, String>() {
    override val preferenceKey: Preferences.Key<String> = stringPreferencesKey(key)
    override fun encode(value: List<E>): String = value.joinToString(",") { it.name }
    override fun decode(raw: Any?): List<E> = getEnumListStrict(raw, default, enumClass)

    companion object {
        inline fun <reified E : Enum<E>> MapSettingsStore.enumList(
            title: Int?,
            description: Int?,
            default: List<E>,
            key: String = "",
            noinline onChanged: (() -> Unit)? = null,
            backupable: Boolean = true
        ) = EnumListSettingObject(
            key = key.isNotBlankKey,
            title = title,
            description = description,
            dataStoreName = dataStoreName,
            default = default,
            enumClass = E::class.java,
            onChanged = onChanged,
            backupable = backupable
        )
    }
}

data class EnumSetSettingObject<E : Enum<E>>(
    override val key: String,
    override val default: Set<E>,
    override val title: Int?,
    override val description: Int?,
    override val dataStoreName: DataStoreName,
    override var onChanged: (() -> Unit)?,
    override val backupable: Boolean,
    val enumClass: Class<E>
) : SettingObject<Set<E>, Set<String>>() {
    override val preferenceKey: Preferences.Key<Set<String>> = stringSetPreferencesKey(key)
    override fun encode(value: Set<E>): Set<String> = value.mapTo(mutableSetOf()) { it.name }
    override fun decode(raw: Any?): Set<E> = getEnumSetStrict(raw, default, enumClass)

    companion object {
        inline fun <reified E : Enum<E>> MapSettingsStore.enumSet(
            title: Int?,
            description: Int?,
            default: Set<E>,
            key: String = "",
            noinline onChanged: (() -> Unit)? = null,
            backupable: Boolean = true
        ) = EnumSetSettingObject(
            key = key.isNotBlankKey,
            title = title,
            description = description,
            dataStoreName = dataStoreName,
            default = default,
            enumClass = E::class.java,
            onChanged = onChanged,
            backupable = backupable
        )
    }
}

data class ColorSettingObject(
    override val key: String,
    override val default: Color,
    override val title: Int?,
    override val description: Int?,
    override val dataStoreName: DataStoreName,
    override var onChanged: (() -> Unit)?,
    override val backupable: Boolean
) : SettingObject<Color, String>() {

    override val preferenceKey: Preferences.Key<String> = stringPreferencesKey(key)
    override fun encode(value: Color): String = value.toHexWithAlpha(false)
    override fun decode(raw: Any?): Color = getColorStrict(raw, default)

    companion object {
        fun MapSettingsStore.color(
            title: Int?,
            description: Int?,
            default: Color,
            key: String = "",
            onChanged: (() -> Unit)? = null,
            backupable: Boolean = true
        ) = ColorSettingObject(
            key = key.takeIf { it.isNotEmpty() } ?: error("Key must not be empty"),
            title = title,
            description = description,
            dataStoreName = dataStoreName,
            default = default,
            onChanged = onChanged,
            backupable = backupable
        )
    }
}


data class ActionSettingObject(
    override val key: String,
    override val default: Action,
    override val title: Int?,
    override val description: Int?,
    override val dataStoreName: DataStoreName,
    override var onChanged: (() -> Unit)?,
    override val backupable: Boolean
) : SettingObject<Action, String>() {

    override val preferenceKey: Preferences.Key<String> = stringPreferencesKey(key)
    override fun encode(value: Action): String? = ActionJson.encode(value)
    override fun decode(raw: Any?): Action = getActionStrict(raw, default)

    companion object {
        fun MapSettingsStore.action(
            title: Int?,
            description: Int?,
            default: Action,
            key: String = "",
            onChanged: (() -> Unit)? = null,
            backupable: Boolean = true
        ) = ActionSettingObject(
            key = key.isNotBlankKey,
            title = title,
            description = description,
            dataStoreName = dataStoreName,
            default = default,
            onChanged = onChanged,
            backupable = backupable
        )
    }
}

data class PointSettingObject(
    override val key: String,
    override val default: Point,
    override val title: Int?,
    override val description: Int?,
    override val dataStoreName: DataStoreName,
    override var onChanged: (() -> Unit)?,
    override val backupable: Boolean
) : SettingObject<Point, String>() {

    override val preferenceKey: Preferences.Key<String> = stringPreferencesKey(key)
    override fun encode(value: Point): String? = PointsJson.encode(value)
    override fun decode(raw: Any?): Point = getPointStrict(raw, default)

    companion object {
        fun MapSettingsStore.point(
            title: Int?,
            description: Int?,
            default: Point,
            key: String = "",
            onChanged: (() -> Unit)? = null,
            backupable: Boolean = true
        ) = PointSettingObject(
            key = key.isNotBlankKey,
            title = title,
            description = description,
            dataStoreName = dataStoreName,
            default = default,
            onChanged = onChanged,
            backupable = backupable
        )
    }
}

data class IconShapeSettingObject(
    override val key: String,
    override val default: IconShape,
    override val title: Int?,
    override val description: Int?,
    override val dataStoreName: DataStoreName,
    override var onChanged: (() -> Unit)?,
    override val backupable: Boolean
) : SettingObject<IconShape, String>() {

    override val preferenceKey: Preferences.Key<String> = stringPreferencesKey(key)
    override fun encode(value: IconShape): String? = IconShapeJson.encode(value)
    override fun decode(raw: Any?): IconShape = IconShapeJson.decode(raw, default)

    companion object {
        fun MapSettingsStore.shape(
            title: Int?,
            description: Int?,
            default: IconShape,
            key: String = "",
            onChanged: (() -> Unit)? = null,
            backupable: Boolean = true
        ) = IconShapeSettingObject(
            key = key.isNotBlankKey,
            title = title,
            description = description,
            dataStoreName = dataStoreName,
            default = default,
            onChanged = onChanged,
            backupable = backupable
        )
    }
}
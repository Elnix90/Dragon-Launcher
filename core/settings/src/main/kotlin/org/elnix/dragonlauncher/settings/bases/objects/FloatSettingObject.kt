package org.elnix.dragonlauncher.settings.bases.objects

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.floatPreferencesKey
import org.elnix.dragonlauncher.settings.DataStoreName
import org.elnix.dragonlauncher.settings.bases.stores.MapSettingsStore
import org.elnix.dragonlauncher.settings.util.isNotBlankKey

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
    }
}
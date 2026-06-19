package org.elnix.dragonlauncher.settings.bases.objects

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import org.elnix.dragonlauncher.settings.DataStoreName
import org.elnix.dragonlauncher.settings.bases.stores.MapSettingsStore
import org.elnix.dragonlauncher.settings.util.isNotBlankKey

data class DoubleSettingObject(
    override val key: String,
    override val default: Double,
    override val title: Int?,
    override val description: Int?,
    override val dataStoreName: DataStoreName,
    override var onChanged: (() -> Unit)?,
    override val backupable: Boolean,
    val allowedRange: ClosedRange<Double>
) : SettingObject<Double, Double>() {

    override val preferenceKey: Preferences.Key<Double> = doublePreferencesKey(key)
    override fun encode(value: Double): Double = value
    override fun decode(raw: Any?): Double = getDoubleStrict(raw, default).coerceIn(allowedRange)

    companion object {
        fun MapSettingsStore.double(
            title: Int?,
            description: Int?,
            default: Double,
            allowedRange: ClosedRange<Double>,
            key: String = "",
            onChanged: (() -> Unit)? = null,
            backupable: Boolean = true
        ) = DoubleSettingObject(
            key = key.isNotBlankKey,
            title = title,
            description = description,
            dataStoreName = dataStoreName,
            default = default,
            allowedRange = allowedRange,
            onChanged = onChanged,
            backupable = backupable
        )

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
    }
}
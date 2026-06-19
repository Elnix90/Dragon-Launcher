package org.elnix.dragonlauncher.settings.bases.objects

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import org.elnix.dragonlauncher.settings.DataStoreName
import org.elnix.dragonlauncher.settings.bases.stores.MapSettingsStore
import org.elnix.dragonlauncher.settings.util.isNotBlankKey

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
    }
}
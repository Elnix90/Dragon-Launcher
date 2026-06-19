package org.elnix.dragonlauncher.settings.bases.objects

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import org.elnix.dragonlauncher.settings.DataStoreName
import org.elnix.dragonlauncher.settings.bases.stores.MapSettingsStore
import org.elnix.dragonlauncher.settings.util.isNotBlankKey

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

        @Suppress("NOTHING_TO_INLINE")
        internal inline fun getBooleanStrict(
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
    }
}
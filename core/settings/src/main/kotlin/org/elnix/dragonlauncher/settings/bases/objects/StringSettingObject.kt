package org.elnix.dragonlauncher.settings.bases.objects

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import org.elnix.dragonlauncher.settings.DataStoreName
import org.elnix.dragonlauncher.settings.bases.stores.MapSettingsStore
import org.elnix.dragonlauncher.settings.util.isNotBlankKey

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
    }
}
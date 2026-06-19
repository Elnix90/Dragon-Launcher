package org.elnix.dragonlauncher.settings.bases.objects

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import org.elnix.dragonlauncher.settings.DataStoreName
import org.elnix.dragonlauncher.settings.bases.stores.MapSettingsStore
import org.elnix.dragonlauncher.settings.util.isNotBlankKey

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

        internal fun getStringListStrict(
            raw: Any?,
            def: List<String>
        ): List<String> {
            return try {
                with(raw.toString()) {
                    val clean = trim()
                    if (clean.isBlank()) return emptyList()
                    clean.split(",")
                        .map { it.trim().trim('"').trim('\'') }
                        .filter { it.isNotBlank() }
                }
            } catch (_: Exception) {
                def
            }
        }
    }
}
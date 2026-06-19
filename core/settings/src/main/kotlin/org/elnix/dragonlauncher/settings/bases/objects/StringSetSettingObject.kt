package org.elnix.dragonlauncher.settings.bases.objects

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringSetPreferencesKey
import org.elnix.dragonlauncher.settings.DataStoreName
import org.elnix.dragonlauncher.settings.bases.stores.MapSettingsStore
import org.elnix.dragonlauncher.settings.util.isNotBlankKey

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

        internal fun getStringSetStrict(
            raw: Any?,
            def: Set<String>
        ): Set<String> {
            return when (raw) {
                is Set<*> -> raw.flattenStrings().toSet()
                is List<*> -> raw.flattenStrings().toSet()
                is String -> {
                    // Parse "[a,b,c]" → ["a","b","c"]
                    try {
                        // Extract content between [ ] and split by comma
                        val clean = raw.trim().removeSurrounding("[", "]")
                        if (clean.isBlank()) return emptySet()

                        clean.split(",")
                            .map { it.trim().trim('"').trim('\'') }
                            .filter { it.isNotBlank() }
                            .toSet()
                    } catch (_: Exception) {
                        setOf(raw)
                    }
                }

                else -> null
            } ?: def
        }

        private fun Collection<*>.flattenStrings(): List<String> = flatMap { item ->
            when (item) {
                is String -> listOf(item)
                is Collection<*> -> item.flattenStrings()
                else -> emptyList()
            }
        }.filter { it.isNotBlank() }
    }
}

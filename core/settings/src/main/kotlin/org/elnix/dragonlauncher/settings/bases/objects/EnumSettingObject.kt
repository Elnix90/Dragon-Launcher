package org.elnix.dragonlauncher.settings.bases.objects

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import org.elnix.dragonlauncher.settings.DataStoreName
import org.elnix.dragonlauncher.settings.bases.stores.MapSettingsStore
import org.elnix.dragonlauncher.settings.util.isNotBlankKey

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

        internal fun <E : Enum<E>> getEnumStrict(
            raw: Any?,
            def: E,
            enumClass: Class<E>
        ): E {
            return enumClass.enumConstants
                ?.firstOrNull { it.name == raw }
                ?: def
        }
    }
}
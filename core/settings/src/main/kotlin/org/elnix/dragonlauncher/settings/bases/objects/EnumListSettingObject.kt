package org.elnix.dragonlauncher.settings.bases.objects

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import org.elnix.dragonlauncher.logging.ANGLE_LINE_TAG
import org.elnix.dragonlauncher.logging.logE
import org.elnix.dragonlauncher.settings.DataStoreName
import org.elnix.dragonlauncher.settings.bases.stores.MapSettingsStore
import org.elnix.dragonlauncher.settings.util.isNotBlankKey

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

        /**
         * Decodes a list of enum from a string, comma separated statements
         */
        internal fun <E : Enum<E>> getEnumListStrict(
            raw: Any?,
            def: List<E>,
            enumClass: Class<E>
        ): List<E> = when (raw) {
            is String ->
                try {
                    raw
                        .takeIf { it.isNotEmpty() }
                        ?.split(",")
                        ?.mapNotNull { elem ->
                            enumClass.enumConstants
                                ?.firstOrNull { it.name == elem.trim() }
                        }.orEmpty()
                } catch (e: Exception) {
                    logE(ANGLE_LINE_TAG, e) { "Failed to decode enumClass $enumClass object, using default value" }
                    null
                }

            else -> null
        } ?: def
    }
}
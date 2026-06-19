package org.elnix.dragonlauncher.settings.bases.objects

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringSetPreferencesKey
import org.elnix.dragonlauncher.logging.ANGLE_LINE_TAG
import org.elnix.dragonlauncher.logging.logE
import org.elnix.dragonlauncher.settings.DataStoreName
import org.elnix.dragonlauncher.settings.bases.stores.MapSettingsStore
import org.elnix.dragonlauncher.settings.util.isNotBlankKey

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

        internal fun <E : Enum<E>> getEnumSetStrict(
            raw: Any?,
            def: Set<E>,
            enumClass: Class<E>
        ): Set<E> {

            return when (raw) {
                is String ->
                    try {
                        raw
                            .takeIf { it.isNotEmpty() }
                            ?.split(",")
                            ?.mapNotNull { elem ->
                                enumClass.enumConstants
                                    ?.firstOrNull { it.name == elem.trim() }
                            }.orEmpty()
                            .toSet()
                    } catch (e: Exception) {
                        logE(ANGLE_LINE_TAG, e) { "Failed to decode enumClass $enumClass object, using default value" }
                        null
                    }

                else -> null
            } ?: def
        }
    }
}
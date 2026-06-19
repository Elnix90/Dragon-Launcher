package org.elnix.dragonlauncher.settings.bases.objects

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.longPreferencesKey
import org.elnix.dragonlauncher.settings.DataStoreName
import org.elnix.dragonlauncher.settings.bases.stores.MapSettingsStore
import org.elnix.dragonlauncher.settings.util.isNotBlankKey

data class LongSettingObject(
    override val key: String,
    override val default: Long,
    override val title: Int?,
    override val description: Int?,
    override val dataStoreName: DataStoreName,
    override var onChanged: (() -> Unit)?,
    override val backupable: Boolean,
    val allowedRange: ClosedRange<Long>
) : SettingObject<Long, Long>() {

    override val preferenceKey: Preferences.Key<Long> = longPreferencesKey(key)
    override fun encode(value: Long): Long = value
    override fun decode(raw: Any?): Long = getLongStrict(raw, default).coerceIn(allowedRange)

    companion object {
        fun MapSettingsStore.long(
            title: Int?,
            description: Int?,
            default: Long,
            allowedRange: ClosedRange<Long>,
            key: String = "",
            onChanged: (() -> Unit)? = null,
            backupable: Boolean = true
        ) = LongSettingObject(
            key = key.isNotBlankKey,
            title = title,
            description = description,
            dataStoreName = dataStoreName,
            default = default,
            allowedRange = allowedRange,
            onChanged = onChanged,
            backupable = backupable
        )

        internal fun getLongStrict(
            raw: Any?,
            def: Long
        ): Long {
            return when (raw) {
                is Long -> raw
                is Number -> raw.toLong()
                is String -> raw.toLongOrNull()
                else -> null
            } ?: def
        }
    }
}
package org.elnix.dragonlauncher.settings.bases.objects

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import org.elnix.dragonlauncher.settings.DataStoreName
import org.elnix.dragonlauncher.settings.bases.stores.MapSettingsStore
import org.elnix.dragonlauncher.settings.util.isNotBlankKey

data class DpSettingObject(
    override val key: String,
    override val default: Dp,
    override val title: Int?,
    override val description: Int?,
    override val dataStoreName: DataStoreName,
    override var onChanged: (() -> Unit)?,
    override val backupable: Boolean,
    val allowedRange: ClosedRange<Dp>
) : SettingObject<Dp, Int>() {

    override val preferenceKey: Preferences.Key<Int> = intPreferencesKey(key)
    override fun encode(value: Dp): Int = value.value.toInt()
    override fun decode(raw: Any?): Dp = getDpStrict(raw, default).coerceIn(allowedRange)

    companion object {
        fun MapSettingsStore.dp(
            title: Int?,
            description: Int?,
            default: Dp,
            allowedRange: ClosedRange<Dp>,
            key: String = "",
            onChanged: (() -> Unit)? = null,
            backupable: Boolean = true
        ) = DpSettingObject(
            key = key.isNotBlankKey,
            title = title,
            description = description,
            dataStoreName = dataStoreName,
            default = default,
            allowedRange = allowedRange,
            onChanged = onChanged,
            backupable = backupable
        )

        internal fun getDpStrict(
            raw: Any?,
            def: Dp
        ): Dp {
            return when (raw) {
                is Int -> raw.dp
                is Number -> raw.toInt().dp
                is String -> raw.toIntOrNull()?.dp
                else -> null
            } ?: def
        }
    }
}
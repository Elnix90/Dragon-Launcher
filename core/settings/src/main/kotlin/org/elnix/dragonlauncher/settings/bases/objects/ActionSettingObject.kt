package org.elnix.dragonlauncher.settings.bases.objects

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import org.elnix.dragonlauncher.base.model.serializables.Action
import org.elnix.dragonlauncher.settings.DataStoreName
import org.elnix.dragonlauncher.settings.bases.stores.MapSettingsStore
import org.elnix.dragonlauncher.settings.util.isNotBlankKey

data class ActionSettingObject(
    override val key: String,
    override val default: Action,
    override val title: Int?,
    override val description: Int?,
    override val dataStoreName: DataStoreName,
    override var onChanged: (() -> Unit)?,
    override val backupable: Boolean
) : SettingObject<Action, String>() {

    override val preferenceKey: Preferences.Key<String> = stringPreferencesKey(key)
    override fun encode(value: Action): String? = Action.Companion.ActionJson.encode(value)
    override fun decode(raw: Any?): Action = getActionStrict(raw, default)

    companion object {
        fun MapSettingsStore.action(
            title: Int?,
            description: Int?,
            default: Action,
            key: String = "",
            onChanged: (() -> Unit)? = null,
            backupable: Boolean = true
        ) = ActionSettingObject(
            key = key.isNotBlankKey,
            title = title,
            description = description,
            dataStoreName = dataStoreName,
            default = default,
            onChanged = onChanged,
            backupable = backupable
        )

        @Suppress("NOTHING_TO_INLINE")
        internal inline fun getActionStrict(
            raw: Any?,
            def: Action
        ): Action {
            return when (raw) {
                is String -> Action.Companion.ActionJson.decode(raw)
                else -> Action.Companion.ActionJson.decode(raw.toString())
            } ?: def
        }
    }
}
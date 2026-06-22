package org.elnix.dragonlauncher.settings.specialObjects

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import io.github.elnix90.core.objects.SettingObject
import io.github.elnix90.core.stores.MapSettingsStore
import io.github.elnix90.core.stores.SettingsStore
import io.github.elnix90.core.util.isNotBlankKey
import org.elnix.dragonlauncher.base.model.serializables.Action

data class ActionSettingObject(
    override val key: String,
    override val default: Action,
    override val title: Int?,
    override val description: Int?,
    override var onChanged: (() -> Unit)?,
    override val backupable: Boolean,
    override val settingsStore: SettingsStore<*, *>
) : SettingObject<Action, String>() {

    override val preferenceKey: Preferences.Key<String> = stringPreferencesKey(preferenceKeyName)
    override fun encode(value: Action): String? = Action.Companion.ActionJson.encode(value)
    override fun decode(raw: Any?): Action = Action.Companion.ActionJson.decode(raw, default)
}

fun MapSettingsStore.action(
    default: Action,
    title: Int? = null,
    description: Int? = null,
    key: String = "",
    onChanged: (() -> Unit)? = null,
    backupable: Boolean = true
) = ActionSettingObject(
    key = key.isNotBlankKey,
    title = title,
    description = description,
    default = default,
    onChanged = onChanged,
    backupable = backupable,
    settingsStore = this
)

package org.elnix.dragonlauncher.settings.specialObjects

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import io.github.elnix90.core.objects.SettingObject
import io.github.elnix90.core.stores.MapSettingsStore
import io.github.elnix90.core.stores.SettingsStore
import io.github.elnix90.core.util.isNotBlankKey
import org.elnix.dragonlauncher.base.model.serializables.IconShape

public data class IconShapeSettingObject(
    override val key: String,
    override val default: IconShape,
    override val title: Int?,
    override val description: Int?,
    override var onChanged: (() -> Unit)?,
    override val backupable: Boolean,
    override val settingsStore: SettingsStore<*, *>
) : SettingObject<IconShape, String>() {

    override val preferenceKey: Preferences.Key<String> = stringPreferencesKey(preferenceKeyName)
    override fun encode(value: IconShape): String? = IconShape.Companion.IconShapeJson.encode(value)
    override fun decode(raw: Any?): IconShape = IconShape.Companion.IconShapeJson.decode(raw, default)
}

public fun MapSettingsStore.shape(
    default: IconShape,
    title: Int? = null,
    description: Int? = null,
    key: String = "",
    onChanged: (() -> Unit)? = null,
    backupable: Boolean = true
): IconShapeSettingObject = IconShapeSettingObject(
    key = key.isNotBlankKey,
    title = title,
    description = description,
    default = default,
    onChanged = onChanged,
    backupable = backupable,
    settingsStore = this
)


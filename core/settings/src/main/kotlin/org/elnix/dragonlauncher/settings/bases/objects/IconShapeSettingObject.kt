package org.elnix.dragonlauncher.settings.bases.objects

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import org.elnix.dragonlauncher.base.model.serializables.IconShape
import org.elnix.dragonlauncher.settings.DataStoreName
import org.elnix.dragonlauncher.settings.bases.stores.MapSettingsStore
import org.elnix.dragonlauncher.settings.util.isNotBlankKey

data class IconShapeSettingObject(
    override val key: String,
    override val default: IconShape,
    override val title: Int?,
    override val description: Int?,
    override val dataStoreName: DataStoreName,
    override var onChanged: (() -> Unit)?,
    override val backupable: Boolean
) : SettingObject<IconShape, String>() {

    override val preferenceKey: Preferences.Key<String> = stringPreferencesKey(key)
    override fun encode(value: IconShape): String? = IconShape.Companion.IconShapeJson.encode(value)
    override fun decode(raw: Any?): IconShape = IconShape.Companion.IconShapeJson.decode(raw, default)

    companion object {
        fun MapSettingsStore.shape(
            title: Int?,
            description: Int?,
            default: IconShape,
            key: String = "",
            onChanged: (() -> Unit)? = null,
            backupable: Boolean = true
        ) = IconShapeSettingObject(
            key = key.isNotBlankKey,
            title = title,
            description = description,
            dataStoreName = dataStoreName,
            default = default,
            onChanged = onChanged,
            backupable = backupable
        )
    }
}
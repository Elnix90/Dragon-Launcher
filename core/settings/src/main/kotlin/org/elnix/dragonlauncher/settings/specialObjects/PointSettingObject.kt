package org.elnix.dragonlauncher.settings.specialObjects

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import io.github.elnix90.core.objects.SettingObject
import io.github.elnix90.core.stores.MapSettingsStore
import io.github.elnix90.core.stores.SettingsStore
import io.github.elnix90.core.util.isNotBlankKey
import org.elnix.dragonlauncher.base.model.serializables.Point

public data class PointSettingObject(
    override val key: String,
    override val default: Point,
    override val title: Int?,
    override val description: Int?,
    override var onChanged: (() -> Unit)?,
    override val icon: Int?,
    override val backupable: Boolean,
    override val settingsStore: SettingsStore<*, *>
) : SettingObject<Point, String>() {
    override val preferenceKey: Preferences.Key<String> = stringPreferencesKey(preferenceKeyName)

    override fun encode(value: Point): String? = Point.Companion.DefaultPointJson.encode(value)

    override fun decode(raw: Any?): Point = Point.Companion.DefaultPointJson.decode(raw, default)
}

public fun MapSettingsStore.point(
    default: Point,
    title: Int? = null,
    description: Int? = null,
    icon: Int? = null,
    key: String = "",
    onChanged: (() -> Unit)? = null,
    backupable: Boolean = true
): PointSettingObject =
    PointSettingObject(
        key = key.isNotBlankKey,
        title = title,
        icon = icon,
        description = description,
        default = default,
        onChanged = onChanged,
        backupable = backupable,
        settingsStore = this
    )

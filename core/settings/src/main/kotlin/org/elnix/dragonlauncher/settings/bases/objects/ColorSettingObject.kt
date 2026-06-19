package org.elnix.dragonlauncher.settings.bases.objects

import androidx.compose.ui.graphics.Color
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import org.elnix.dragonlauncher.base.util.ColorUtils.toHexWithAlpha
import org.elnix.dragonlauncher.settings.DataStoreName
import org.elnix.dragonlauncher.settings.bases.stores.MapSettingsStore

data class ColorSettingObject(
    override val key: String,
    override val default: Color,
    override val title: Int?,
    override val description: Int?,
    override val dataStoreName: DataStoreName,
    override var onChanged: (() -> Unit)?,
    override val backupable: Boolean
) : SettingObject<Color, String>() {

    override val preferenceKey: Preferences.Key<String> = stringPreferencesKey(key)
    override fun encode(value: Color): String = value.toHexWithAlpha(false)
    override fun decode(raw: Any?): Color = getColorStrict(raw, default)

    companion object {
        fun MapSettingsStore.color(
            title: Int?,
            description: Int?,
            default: Color,
            key: String = "",
            onChanged: (() -> Unit)? = null,
            backupable: Boolean = true
        ) = ColorSettingObject(
            key = key.takeIf { it.isNotEmpty() } ?: error("Key must not be empty"),
            title = title,
            description = description,
            dataStoreName = dataStoreName,
            default = default,
            onChanged = onChanged,
            backupable = backupable
        )

        internal fun getColorStrict(
            raw: Any?,
            def: Color
        ): Color {
            return when (raw) {
                null -> null
                // Old storage format
                is Int -> Color(raw)
                is Number -> Color(raw.toInt())
                // New readable format, fallbacks to old format
                is String -> {
                    raw.toLongOrNull(16)
                        ?.let { Color(it.toInt()) }
                }

                else -> null
            } ?: def
        }
    }
}
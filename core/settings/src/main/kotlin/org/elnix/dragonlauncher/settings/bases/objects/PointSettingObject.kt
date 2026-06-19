package org.elnix.dragonlauncher.settings.bases.objects

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import org.elnix.dragonlauncher.base.model.serializables.Point
import org.elnix.dragonlauncher.settings.DataStoreName
import org.elnix.dragonlauncher.settings.bases.stores.MapSettingsStore
import org.elnix.dragonlauncher.settings.util.isNotBlankKey

data class PointSettingObject(
    override val key: String,
    override val default: Point,
    override val title: Int?,
    override val description: Int?,
    override val dataStoreName: DataStoreName,
    override var onChanged: (() -> Unit)?,
    override val backupable: Boolean
) : SettingObject<Point, String>() {

    override val preferenceKey: Preferences.Key<String> = stringPreferencesKey(key)
    override fun encode(value: Point): String? = Point.Companion.PointsJson.encode(value)
    override fun decode(raw: Any?): Point = getPointStrict(raw, default)

    companion object {
        fun MapSettingsStore.point(
            title: Int?,
            description: Int?,
            default: Point,
            key: String = "",
            onChanged: (() -> Unit)? = null,
            backupable: Boolean = true
        ) = PointSettingObject(
            key = key.isNotBlankKey,
            title = title,
            description = description,
            dataStoreName = dataStoreName,
            default = default,
            onChanged = onChanged,
            backupable = backupable
        )

        @Suppress("NOTHING_TO_INLINE")
        internal inline fun getPointStrict(
            raw: Any?,
            def: Point
        ): Point {
            return when (raw) {
                is String -> Point.Companion.PointsJson.decode(raw)
                else -> Point.Companion.PointsJson.decode(raw.toString())
            } ?: def
        }
    }
}
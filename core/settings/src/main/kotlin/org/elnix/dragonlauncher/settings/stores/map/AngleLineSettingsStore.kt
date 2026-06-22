package org.elnix.dragonlauncher.settings.stores.map

import io.github.elnix90.annotations.SettingKey
import io.github.elnix90.annotations.SettingsStore
import io.github.elnix90.core.objects.boolean
import io.github.elnix90.core.objects.string
import io.github.elnix90.core.stores.MapSettingsStore
import org.elnix.dragonlauncher.base.model.models.AngleLineObjects
import org.elnix.dragonlauncher.i18n.R


@SettingsStore
object AngleLineSettingsStore : MapSettingsStore() {

    @SettingKey
    val showLineObjectPreview = boolean(
        title = R.string.show_app_line_preview,
        description = R.string.show_app_line_preview_description,
        default = true
    )

    @SettingKey
    val showAngleLineObjectPreview = boolean(
        title = R.string.show_app_angle_preview,
        description = R.string.show_app_angle_preview_description,
        default = false
    )

    @SettingKey
    val showStartObjectPreview = boolean(
        title = R.string.show_start_object_preview,
        description = R.string.show_start_object_preview_desc,
        default = true
    )

    @SettingKey
    val showEndObjectPreview = boolean(
        title = R.string.show_end_object_preview,
        description = R.string.show_end_object_preview_desc,
        default = true
    )

    @SettingKey
    val lineJson = string("")

    @SettingKey
    val angleLineJson = string(
        default = "",
        title = R.string.angle_object
    )

    @SettingKey
    val startLineJson = string(
        default = "",
        title = R.string.start_object
    )

    @SettingKey
    val endLineJson = string(
        default = "",
        title = R.string.end_object
    )

    @SettingKey
    val angleLineObjectsOrder = string(AngleLineObjects.entries.joinToString(",") { it.name })
}
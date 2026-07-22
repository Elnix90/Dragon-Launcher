package org.elnix.dragonlauncher.settings.stores.map

import io.github.elnix90.annotations.SettingKey
import io.github.elnix90.annotations.SettingsStore
import io.github.elnix90.core.objects.BooleanSettingObject
import io.github.elnix90.core.objects.StringSettingObject
import io.github.elnix90.core.objects.boolean
import io.github.elnix90.core.objects.string
import io.github.elnix90.core.stores.MapSettingsStore
import org.elnix.dragonlauncher.base.model.models.AngleLineObjects
import org.elnix.dragonlauncher.i18n.R


@SettingsStore
public object AngleLineSettingsStore : MapSettingsStore() {

    /** Use the computing of HSV color to produce a color that depends on the angle / progress */
    @SettingKey
    public val rgbLine: BooleanSettingObject = boolean(
        title = R.string.rgb_line_selector,
        description = R.string.rgb_line_selector_description,
        default = true
    )

    @SettingKey
    public val startAndAngleShareSameRandomAngle: BooleanSettingObject = boolean(
        title = R.string.start_ang_angle_share_same_random_angle,
        description = R.string.start_ang_angle_share_same_random_angle_desc,
        default = false
    )



    @SettingKey
    public val showLineObjectPreview: BooleanSettingObject = boolean(
        title = R.string.show_app_line_preview,
        description = R.string.show_app_line_preview_description,
        default = true
    )

    @SettingKey
    public val showAngleLineObjectPreview: BooleanSettingObject = boolean(
        title = R.string.show_angle_preview,
        description = R.string.show_app_angle_preview_description,
        default = false
    )

    @SettingKey
    public val showStartObjectPreview: BooleanSettingObject = boolean(
        title = R.string.show_start_object_preview,
        description = R.string.show_start_object_preview_desc,
        default = true
    )

    @SettingKey
    public val showEndObjectPreview: BooleanSettingObject = boolean(
        title = R.string.show_end_object_preview,
        description = R.string.show_end_object_preview_desc,
        default = true
    )

    @SettingKey
    public val angleLineObjectsOrder: StringSettingObject = string(AngleLineObjects.entries.joinToString(",") { it.name })
}
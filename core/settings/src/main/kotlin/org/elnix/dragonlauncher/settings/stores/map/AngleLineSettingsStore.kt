package org.elnix.dragonlauncher.settings.stores.map

import org.elnix.dragonlauncher.base.model.models.AngleLineObjects
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.settings.DataStoreName
import org.elnix.dragonlauncher.settings.bases.objects.BaseSettingObject
import org.elnix.dragonlauncher.settings.bases.objects.BooleanSettingObject.Companion.boolean
import org.elnix.dragonlauncher.settings.bases.objects.StringSettingObject.Companion.string
import org.elnix.dragonlauncher.settings.bases.stores.MapSettingsStore

object AngleLineSettingsStore : MapSettingsStore(DataStoreName.ANGLE_LINE) {

    override val ALL: List<BaseSettingObject<*, *>> by lazy {
        listOf(
            this.showLineObjectPreview,
            this.showAngleLineObjectPreview,
            this.showStartObjectPreview,
            this.showEndObjectPreview,

            this.lineJson,
            this.angleLineJson,
            this.startLineJson,
            this.endLineJson,

            this.angleLineObjectsOrder
        )
    }

    val showLineObjectPreview by boolean(
        title = R.string.show_app_line_preview,
        description = R.string.show_app_line_preview_description,
        default = true
    )

    val showAngleLineObjectPreview by boolean(
        title = R.string.show_app_angle_preview,
        description = R.string.show_app_angle_preview_description,
        default = false
    )

    val showStartObjectPreview by boolean(
        title = R.string.show_start_object_preview,
        description = R.string.show_start_object_preview_desc,
        default = true
    )

    val showEndObjectPreview by boolean(
        title = R.string.show_end_object_preview,
        description = R.string.show_end_object_preview_desc,
        default = true
    )

    val lineJson by string(
        title = null,
        description = null,
        default = ""
    )

    val angleLineJson by string(
        title = R.string.angle_object,
        description = null,
        default = ""
    )

    val startLineJson by string(
        title = R.string.start_object,
        description = null,
        default = ""
    )

    val endLineJson by string(
        title = R.string.end_object,
        description = null,
        default = ""
    )

    val angleLineObjectsOrder by string(
        title = null,
        description = null,
        default = AngleLineObjects.entries.joinToString(",") { it.name }
    )
}
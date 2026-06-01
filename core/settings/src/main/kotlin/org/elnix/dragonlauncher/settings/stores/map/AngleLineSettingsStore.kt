package org.elnix.dragonlauncher.settings.stores.map

import org.elnix.dragonlauncher.base.model.models.AngleLineObjects
import org.elnix.dragonlauncher.settings.DataStoreName
import org.elnix.dragonlauncher.settings.bases.boolean
import org.elnix.dragonlauncher.settings.bases.objects.BaseSettingObject
import org.elnix.dragonlauncher.settings.bases.stores.MapSettingsStore
import org.elnix.dragonlauncher.settings.bases.string

object AngleLineSettingsStore : MapSettingsStore(DataStoreName.ANGLE_LINE) {

    override val ALL: List<BaseSettingObject<*, *>>
        get() = listOf(
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

    val showLineObjectPreview = boolean(
        key = "showLineObjectPreview",
        default = true
    )

    val showAngleLineObjectPreview = boolean(
        key = "showAngleLineObjectPreview",
        default = false
    )

    val showStartObjectPreview = boolean(
        key = "showStartObjectPreview",
        default = true
    )

    val showEndObjectPreview = boolean(
        key = "showEndObjectPreview",
        default = true
    )

    val lineJson = string(
        key = "lineJson",
        default = ""
    )

    val angleLineJson = string(
        key = "angleLineJson",
        default = ""
    )

    val startLineJson = string(
        key = "startLineJson",
        default = ""
    )

    val endLineJson = string(
        key = "endLineJson",
        default = ""
    )

    val angleLineObjectsOrder = string(
        key = "angleLineObjectsOrder",
        default = AngleLineObjects.entries.joinToString(",") { it.name }
    )
}
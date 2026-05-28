package org.elnix.dragonlauncher.settings.stores

import org.elnix.dragonlauncher.enumsui.other.AngleLineObjects
import org.elnix.dragonlauncher.settings.DataStoreName
import org.elnix.dragonlauncher.settings.bases.boolean
import org.elnix.dragonlauncher.settings.bases.objects.BaseSettingObject
import org.elnix.dragonlauncher.settings.bases.stores.MapSettingsStore
import org.elnix.dragonlauncher.settings.bases.string

object AngleLineSettingsStore : MapSettingsStore(DataStoreName.ANGLE_LINE) {

    /*  ─────────────  Main toggler for showing or not the line objects  ─────────────  */
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


    /*  ───────────── Custom line objects stored as JSON using kotlin serializer ─────────────  */
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

    override val ALL: List<BaseSettingObject<*,*>> = listOf(
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

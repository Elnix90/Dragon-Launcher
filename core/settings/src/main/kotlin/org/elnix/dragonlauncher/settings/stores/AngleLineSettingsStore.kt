package org.elnix.dragonlauncher.settings.stores

import org.elnix.dragonlauncher.enumsui.other.AngleLineObjects
import org.elnix.dragonlauncher.settings.DataStoreName
import org.elnix.dragonlauncher.settings.bases.Settings
import org.elnix.dragonlauncher.settings.bases.BaseSettingObject
import org.elnix.dragonlauncher.settings.bases.MapSettingsStore

object AngleLineSettingsStore : MapSettingsStore() {

    override val name: String = "Angle line"
    override val dataStoreName: DataStoreName = DataStoreName.ANGLE_LINE


    /*  ─────────────  Main toggler for showing or not the line objects  ─────────────  */
    val showLineObjectPreview = Settings.boolean(
        key = "showLineObjectPreview",
        dataStoreName = dataStoreName,
        default = true
    )

    val showAngleLineObjectPreview = Settings.boolean(
        key = "showAngleLineObjectPreview",
        dataStoreName = dataStoreName,
        default = false
    )

    val showStartObjectPreview = Settings.boolean(
        key = "showStartObjectPreview",
        dataStoreName = dataStoreName,
        default = true
    )

    val showEndObjectPreview = Settings.boolean(
        key = "showEndObjectPreview",
        dataStoreName = dataStoreName,
        default = true
    )


    /*  ───────────── Custom line objects stored as JSON using kotlin serializer ─────────────  */
    val lineJson = Settings.string(
        key = "lineJson",
        dataStoreName = dataStoreName,
        default = ""
    )

    val angleLineJson = Settings.string(
        key = "angleLineJson",
        dataStoreName = dataStoreName,
        default = ""
    )

    val startLineJson = Settings.string(
        key = "startLineJson",
        dataStoreName = dataStoreName,
        default = ""
    )

    val endLineJson = Settings.string(
        key = "endLineJson",
        dataStoreName = dataStoreName,
        default = ""
    )


    val angleLineObjectsOrder = Settings.string(
        key = "angleLineObjectsOrder",
        dataStoreName = dataStoreName,
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

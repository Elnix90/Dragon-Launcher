package org.elnix.dragonlauncher.settings.stores.map

import org.elnix.dragonlauncher.base.model.serializables.Action
import org.elnix.dragonlauncher.settings.DataStoreName
import org.elnix.dragonlauncher.settings.bases.objects.ActionSettingObject.Companion.action
import org.elnix.dragonlauncher.settings.bases.objects.BaseSettingObject
import org.elnix.dragonlauncher.settings.bases.objects.BooleanSettingObject.Companion.boolean
import org.elnix.dragonlauncher.settings.bases.objects.IntSettingObject.Companion.int
import org.elnix.dragonlauncher.settings.bases.stores.MapSettingsStore

object BehaviorSettingsStore : MapSettingsStore(DataStoreName.BEHAVIOR) {
    override val ALL: List<BaseSettingObject<*, *>>
        get() = listOf(
            this.backAction,
            this.doubleClickAction,
            this.homeAction,
            this.keepScreenOn,
            this.leftPadding,
            this.rightPadding,
            this.topPadding,
            this.bottomPadding,
            this.disableHapticFeedbackGlobally,
            this.pointsActionSnapsToOuterCircle,
            this.superWarningMode,
            this.superWarningModeSound,
            this.metalPipesSound,
            this.alarmSound,
            this.vibrateOnError,
            this.offScreenTimeout,
            this.createLiveNestByDefaultWhenCreatingOpenCircleNestPoint
        )

    val backAction = action(
        key = "backAction",
        default = Action.None
    )

    val doubleClickAction = action(
        key = "doubleClickAction",
        default = Action.OpenAppDrawer()
    )

    val homeAction = action(
        key = "homeAction",
        default = Action.OpenDragonLauncherSettings()
    )

    val keepScreenOn = boolean(
        key = "keepScreenOn",
        default = false
    )

    val leftPadding = int(
        key = "leftPadding",
        default = 60,
        allowedRange = 0..300
    )

    val rightPadding = int(
        key = "rightPadding",
        default = 60,
        allowedRange = 0..300
    )

    val topPadding = int(
        key = "upPadding",
        default = 80,
        allowedRange = 0..300
    )

    val bottomPadding = int(
        key = "downPadding",
        default = 100,
        allowedRange = 0..300
    )

    val disableHapticFeedbackGlobally = boolean(
        key = "disableHapticFeedbackGlobally",
        default = false
    )

    val pointsActionSnapsToOuterCircle = boolean(
        key = "pointsActionSnapsToOuterCircle",
        default = true
    )

    val superWarningMode = boolean(
        key = "superWarningMode",
        default = false
    )


    val vibrateOnError = boolean(
        key = "vibrateOnError",
        default = false
    )

    val alarmSound = boolean(
        key = "alarmSound",
        default = false
    )

    val metalPipesSound = boolean(
        key = "metalPipesSound",
        default = false
    )

    val superWarningModeSound = int(
        key = "superWarningModeSound",
        default = 100,
        allowedRange = 0..100
    )

    val promptForShortcutsWhenAddingApp = boolean(
        key = "promptForShortcutsWhenAddingApp",
        default = false
    )

    val offScreenTimeout = int(
        key = "offScreenTimeout",
        default = 10,
        allowedRange = -1..60
    )

    val createLiveNestByDefaultWhenCreatingOpenCircleNestPoint = boolean(
        key = "createLiveNestByDefaultWhenCreatingOpenCircleNestPoint",
        default = true
    )
}
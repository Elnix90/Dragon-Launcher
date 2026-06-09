package org.elnix.dragonlauncher.settings.stores

import org.elnix.dragonlauncher.common.serializables.SwipeActionSerializable
import org.elnix.dragonlauncher.settings.DataStoreName
import org.elnix.dragonlauncher.settings.bases.BaseSettingObject
import org.elnix.dragonlauncher.settings.bases.MapSettingsStore
import org.elnix.dragonlauncher.settings.bases.Settings

object BehaviorSettingsStore : MapSettingsStore() {

    override val name: String = "Behavior"

    override val dataStoreName = DataStoreName.BEHAVIOR

    override val ALL: List<BaseSettingObject <*, *> >
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
            this.useDifferentialLoadingForPrivateSpace,
            this.superWarningMode,
            this.superWarningModeSound,
            this.metalPipesSound,
            this.alarmSound,
            this.vibrateOnError,
            this.offScreenTimeout,
            this.createLiveNestByDefaultWhenCreatingOpenCircleNestPoint,
            this.openRootNestEachTime
        )

    val backAction = Settings.swipeAction(
        key = "backAction",
        dataStoreName = dataStoreName,
        default = SwipeActionSerializable.None
    )

    val doubleClickAction = Settings.swipeAction(
        key = "doubleClickAction",
        dataStoreName = dataStoreName,
        default = SwipeActionSerializable.OpenAppDrawer()
    )

    val homeAction = Settings.swipeAction(
        key = "homeAction",
        dataStoreName = dataStoreName,
        default = SwipeActionSerializable.OpenDragonLauncherSettings()
    )

    val keepScreenOn = Settings.boolean(
        key = "keepScreenOn",
        dataStoreName = dataStoreName,
        default = false
    )

    val leftPadding = Settings.int(
        key = "leftPadding",
        dataStoreName = dataStoreName,
        default = 60,
        allowedRange = 0..300
    )

    val rightPadding = Settings.int(
        key = "rightPadding",
        dataStoreName = dataStoreName,
        default = 60,
        allowedRange = 0..300
    )

    val topPadding = Settings.int(
        key = "upPadding",
        dataStoreName = dataStoreName,
        default = 80,
        allowedRange = 0..300
    )

    val bottomPadding = Settings.int(
        key = "downPadding",
        dataStoreName = dataStoreName,
        default = 100,
        allowedRange = 0..300
    )

    /*  ─────────────  Other Useful Settings  ─────────────  */

    val disableHapticFeedbackGlobally = Settings.boolean(
        key = "disableHapticFeedbackGlobally",
        dataStoreName = dataStoreName,
        default = false
    )

    val pointsActionSnapsToOuterCircle = Settings.boolean(
        key = "pointsActionSnapsToOuterCircle",
        dataStoreName = dataStoreName,
        default = true
    )


    /*  ─────────────  Super Warning mode  ─────────────  */

    val superWarningMode = Settings.boolean(
        key = "superWarningMode",
        dataStoreName = dataStoreName,
        default = false
    )


    val vibrateOnError = Settings.boolean(
        key = "vibrateOnError",
        dataStoreName = dataStoreName,
        default = false
    )


    val alarmSound = Settings.boolean(
        key = "alarmSound",
        dataStoreName = dataStoreName,
        default = false
    )

    val metalPipesSound = Settings.boolean(
        key = "metalPipesSound",
        dataStoreName = dataStoreName,
        default = false
    )

    val superWarningModeSound = Settings.int(
        key = "superWarningModeSound",
        dataStoreName = dataStoreName,
        default = 100,
        allowedRange = 0..100
    )


    /*  ─────────────  Internal used settings concerning apps loading ───────────── */
    val useDifferentialLoadingForPrivateSpace = Settings.boolean(
        key = "useDifferentialLoadingForPrivateSpace",
        dataStoreName = dataStoreName,
        default = false
    )

    val promptForShortcutsWhenAddingApp = Settings.boolean(
        key = "promptForShortcutsWhenAddingApp",
        dataStoreName = dataStoreName,
        default = false
    )


    val offScreenTimeout = Settings.int(
        key = "offScreenTimeout",
        dataStoreName = dataStoreName,
        default = 10,
        allowedRange = -1..60
    )


    val createLiveNestByDefaultWhenCreatingOpenCircleNestPoint = Settings.boolean(
        key = "createLiveNestByDefaultWhenCreatingOpenCircleNestPoint",
        dataStoreName = UiSettingsStore.dataStoreName,
        default = true
    )

    val openRootNestEachTime = Settings.boolean(
        key = "openRootNestEachTime",
        dataStoreName = dataStoreName,
        default = false
    )
}

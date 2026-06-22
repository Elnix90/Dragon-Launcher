package org.elnix.dragonlauncher.settings.stores.map

import io.github.elnix90.annotations.SettingKey
import io.github.elnix90.annotations.SettingsStore
import io.github.elnix90.core.objects.boolean
import io.github.elnix90.core.objects.int
import io.github.elnix90.core.stores.MapSettingsStore
import org.elnix.dragonlauncher.base.model.serializables.Action
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.settings.specialObjects.action

@SettingsStore
object BehaviorSettingsStore : MapSettingsStore() {

    @SettingKey
    val backAction = action(
        title = R.string.back_action,
        description = R.string.back_action_desc,
        default = Action.None
    )

    @SettingKey
    val doubleClickAction = action(
        title = R.string.double_click_action,
        description = R.string.double_click_action_desc,
        default = Action.OpenAppDrawer()
    )

    @SettingKey
    val homeAction = action(
        title = R.string.home_action,
        description = R.string.home_action_desc,
        default = Action.OpenDragonLauncherSettings()
    )

    @SettingKey
    val keepScreenOn = boolean(
        title = R.string.keep_screen_on,
        description = R.string.keep_screen_on_desc,
        default = false
    )

    @SettingKey
    val leftPadding = int(
        default = 60,
        title = R.string.left_padding,
        description = R.string.left_padding_desc,
        allowedRange = 0..300
    )

    @SettingKey
    val rightPadding = int(
        default = 60,
        title = R.string.right_padding,
        description = R.string.right_padding_desc,
        allowedRange = 0..300
    )

    @SettingKey
    val topPadding = int(
        default = 80,
        title = R.string.top_padding,
        description = R.string.top_padding_desc,
        allowedRange = 0..300
    )

    @SettingKey
    val bottomPadding = int(
        default = 100,
        title = R.string.bottom_padding,
        description = R.string.bottom_padding_desc,
        allowedRange = 0..300
    )

    @SettingKey
    val disableHapticFeedbackGlobally = boolean(
        title = R.string.disable_haptic_globally,
        description = R.string.disable_haptic_globally_desc,
        default = false
    )

    @SettingKey
    val pointsActionSnapsToOuterCircle = boolean(
        title = R.string.point_action_snaps_to_outer_circle,
        description = R.string.point_action_snaps_to_outer_circle_desc,
        default = true
    )

    @SettingKey
    val superWarningMode = boolean(
        title = R.string.super_warning_mode,
        description = R.string.super_warning_mode_desc,
        default = false
    )


    @SettingKey
    val vibrateOnError = boolean(
        title = R.string.vibrate_on_error,
        description = R.string.vibrate_on_error_desc,
        default = false,
    )

    @SettingKey
    val alarmSound = boolean(
        title = R.string.alarm_sound,
        description = R.string.super_warning_mode_desc,
        default = false
    )

    @SettingKey
    val metalPipesSound = boolean(
        title = R.string.metal_pipes_sound,
        description = R.string.metal_pipes_sound_desc,
        default = false
    )

    @SettingKey
    val superWarningModeSound = int(
        default = 100,
        title = R.string.super_warning_mode_sound,
        description = R.string.super_warning_mode_sound_desc,
        allowedRange = 0..100
    )

    @SettingKey
    val promptForShortcutsWhenAddingApp = boolean(
        title = R.string.prompt_shortcuts_when_adding_app,
        description = R.string.prompt_shortcuts_when_adding_app_desc,
        default = false
    )

    @SettingKey
    val offScreenTimeout = int(
        default = 10,
        title = R.string.off_screen_timeout,
        description = R.string.off_screen_timeout_desc,
        allowedRange = -1..60
    )

    @SettingKey
    val createLiveNestByDefaultWhenCreatingOpenCircleNestPoint = boolean(
        title = R.string.create_live_nest_by_default,
        description = R.string.create_live_nest_by_default_desc,
        default = true
    )
}
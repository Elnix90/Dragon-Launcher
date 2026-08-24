package org.elnix.dragonlauncher.settings.stores.map

import io.github.elnix90.annotations.SettingKey
import io.github.elnix90.annotations.SettingsStore
import io.github.elnix90.core.objects.BooleanSettingObject
import io.github.elnix90.core.objects.IntSettingObject
import io.github.elnix90.core.objects.boolean
import io.github.elnix90.core.objects.int
import io.github.elnix90.core.stores.MapSettingsStore
import org.elnix.dragonlauncher.base.model.serializables.Action
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.settings.specialObjects.ActionSettingObject
import org.elnix.dragonlauncher.settings.specialObjects.action

@SettingsStore
public object BehaviorSettingsStore : MapSettingsStore() {

    @SettingKey
    public val backAction: ActionSettingObject = action(
        title = R.string.back_action,
        description = R.string.back_action_desc,
        icon = R.drawable.back,
        default = Action.None
    )

    @SettingKey
    public val doubleClickAction: ActionSettingObject = action(
        title = R.string.double_click_action,
        description = R.string.double_click_action_desc,
        default = Action.OpenAppDrawer()
    )

    @SettingKey
    public val homeAction: ActionSettingObject = action(
        title = R.string.home_action,
        description = R.string.home_action_desc,
        default = Action.OpenDragonLauncherSettings()
    )

    @SettingKey
    public val keepScreenOn: BooleanSettingObject = boolean(
        title = R.string.keep_screen_on,
        description = R.string.keep_screen_on_desc,
        default = false
    )

    @SettingKey
    public val leftPadding: IntSettingObject = int(
        default = 60,
        title = R.string.left_padding,
        description = R.string.left_padding_desc,
        allowedRange = 0..300
    )

    @SettingKey
    public val rightPadding: IntSettingObject = int(
        default = 60,
        title = R.string.right_padding,
        description = R.string.right_padding_desc,
        allowedRange = 0..300
    )

    @SettingKey
    public val topPadding: IntSettingObject = int(
        default = 80,
        title = R.string.top_padding,
        description = R.string.top_padding_desc,
        allowedRange = 0..300
    )

    @SettingKey
    public val bottomPadding: IntSettingObject = int(
        default = 100,
        title = R.string.bottom_padding,
        description = R.string.bottom_padding_desc,
        allowedRange = 0..300
    )

    @SettingKey
    public val disableHapticFeedbackGlobally: BooleanSettingObject = boolean(
        title = R.string.disable_haptic_globally,
        description = R.string.disable_haptic_globally_desc,
        default = false
    )

    @SettingKey
    public val superWarningMode: BooleanSettingObject = boolean(
        title = R.string.super_warning_mode,
        description = R.string.super_warning_mode_desc,
        default = false
    )


    @SettingKey
    public val vibrateOnError: BooleanSettingObject = boolean(
        title = R.string.vibrate_on_error,
        description = R.string.vibrate_on_error_desc,
        default = false,
    )

    @SettingKey
    public val alarmSound: BooleanSettingObject = boolean(
        title = R.string.alarm_sound,
        description = R.string.super_warning_mode_desc,
        default = false
    )

    @SettingKey
    public val metalPipesSound: BooleanSettingObject = boolean(
        title = R.string.metal_pipes_sound,
        description = R.string.metal_pipes_sound_desc,
        default = false
    )

    @SettingKey
    public val superWarningModeSound: IntSettingObject = int(
        default = 100,
        title = R.string.super_warning_mode_sound,
        description = R.string.super_warning_mode_sound_desc,
        allowedRange = 0..100
    )

    @SettingKey
    public val promptForShortcutsWhenAddingApp: BooleanSettingObject = boolean(
        title = R.string.prompt_shortcuts_when_adding_app,
        description = R.string.prompt_shortcuts_when_adding_app_desc,
        default = false
    )

    @SettingKey
    public val offScreenTimeout: IntSettingObject = int(
        default = 10,
        title = R.string.off_screen_timeout,
        description = R.string.off_screen_timeout_desc,
        allowedRange = -1..60
    )

    @SettingKey
    public val createLiveNestByDefaultWhenCreatingOpenCircleNestPoint: BooleanSettingObject = boolean(
        title = R.string.create_live_nest_by_default,
        description = R.string.create_live_nest_by_default_desc,
        default = true
    )

    @SettingKey
    public val openRootNestEachTime: BooleanSettingObject = boolean(
        title = R.string.open_root_nest_each_time,
        description = R.string.open_root_nest_each_time_desc,
        default = false
    )
}
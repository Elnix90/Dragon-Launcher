package org.elnix.dragonlauncher.settings.stores.map

import io.github.elnix90.annotations.SettingKey
import io.github.elnix90.annotations.SettingsStore
import io.github.elnix90.core.objects.boolean
import io.github.elnix90.core.objects.float
import io.github.elnix90.core.objects.int
import io.github.elnix90.core.objects.string
import io.github.elnix90.core.objects.stringSet
import io.github.elnix90.core.stores.MapSettingsStore
import org.elnix.dragonlauncher.i18n.R

@SettingsStore
object UiSettingsStore : MapSettingsStore() {

    /** Use the computing of HSV color to produce a color that depends on the angle / progress */
    @SettingKey
    val rgbLoading = boolean(
        title = R.string.rgb_loading_settings,
        description = R.string.rgb_loading_description,
        default = true
    )

    /** Use the computing of HSV color to produce a color that depends on the angle / progress */
    @SettingKey
    val rgbLine = boolean(
        title = R.string.rgb_line_selector,
        description = R.string.rgb_line_selector_description,
        default = true
    )

    /** Overlay on top of the screen */
    @SettingKey
    val showLaunchingAppLabel = boolean(
        title = R.string.show_launching_app_label,
        description = R.string.show_launching_app_label_description,
        default = true,
    )

    @SettingKey
    val showLaunchingAppIcon = boolean(
        title = R.string.show_launching_app_icon,
        description = R.string.show_launching_app_icon_description,
        default = true
    )

    @SettingKey
    val appLabelIconOverlayTopPadding = int(
        title = R.string.app_label_icon_overlay_top_padding,
        default = 20,
        allowedRange = 0..1000
    )

    @SettingKey
    val appLabelOverlaySize = int(
        title = R.string.app_label_overlay_size,
        default = 18,
        allowedRange = 0..100
    )

    @SettingKey
    val appIconOverlaySize = int(
        title = R.string.app_icon_overlay_size,
        default = 22,
        allowedRange = 0..400
    )

    @SettingKey
    val fullScreen = boolean(
        title = R.string.fullscreen_app,
        description = R.string.fullscreen_description,
        default = false
    )

    /** Whether if the points are separated automatically to avoid overlapping when moving them in the circle */
    @SettingKey
    val autoSeparatePoints = boolean(
        title = R.string.auto_separate,
        default = true
    )

    @SettingKey
    val snapPoints = boolean(
        title = R.string.snap_points,
        default = true
    )

    @SettingKey
    val freeMoveDraggedPoint = boolean(
        title = R.string.free_move_dragged_point,
        default = true
    )

    /** Global setting, can be changed individually for each nest */
    @SettingKey
    val showCirclePreview = boolean(
        title = R.string.show_app_circle_preview,
        description = R.string.show_app_circle_preview_description,
        default = true
    )

    @SettingKey
    val showAppPreviewIconCenterStartPosition = boolean(
        title = R.string.show_app_icon_start_drag_position,
        description = R.string.show_app_icon_start_drag_position_description,
        default = false
    )

    @SettingKey
    val linePreviewSnapToAction = boolean(
        title = R.string.line_preview_snap_to_action,
        description = R.string.line_preview_snap_to_action_description,
        default = false
    )

    /** Show the current selected app on drag in the main screen / show them all on the circle */
    @SettingKey
    val showAppLaunchingPreview = boolean(
        title = R.string.show_app_launch_preview,
        description = R.string.show_app_launch_preview_description,
        default = true
    )

    @SettingKey
    val showAllActionsOnCurrentCircle = boolean(
        title = R.string.show_all_actions_on_current_circle,
        description = R.string.show_all_actions_on_current_circle_description,
        default = true
    )

    @SettingKey
    val showAllActionsOnCurrentNest = boolean(
        title = R.string.show_all_actions_on_current_nest,
        description = R.string.show_all_actions_on_current_nest_desc,
        default = false
    )

    @SettingKey
    val wallpaperDimMainScreen = float(
        title = R.string.wallpaper_dim_amount,
        default = 0f,
        allowedRange = 0f..1f
    )

    @SettingKey
    val wallpaperDimDrawerScreen = float(
        title = R.string.wallpaper_dim_amount,
        default = 0f,
        allowedRange = 0f..1f
    )

    @SettingKey
    val globalFont = string("Default")

    /** How far the points drawing system `actionsInCircle` draws the points */
    @SettingKey
    val maxNestsDepth = int(
        title = R.string.depth,
        description = R.string.depth_desc,
        default = 2,
        allowedRange = 1..10
    )

    /** How many sub live nests can be drawn at once */
    @SettingKey
    val maxLiveNestsDepth = int(
        title = R.string.live_nest_depth,
        description = R.string.live_nests_depth_desc,
        default = 5,
        allowedRange = 1..10
    )

    @SettingKey
    val mainScreenLayers = string(
        title = R.string.main_screen_layers,
        default = ""
    )

    @SettingKey
    val cellSizeDp = int(
        title = R.string.cell_size,
        description = R.string.cell_size_help,
        default = 30,
        allowedRange = 1..100
    )

    @SettingKey
    val showTooltipsOnAddPointDialog = boolean(
        default = true,
    )

    @SettingKey
    val userThemes = stringSet(emptySet())

    @SettingKey
    val multiplyOrSubtractOpacityInLiveNests = boolean(
        title = R.string.multiply_or_subtract_opacity_in_live_nests,
        description = R.string.multiply_or_subtract_opacity_in_live_nests_desc,
        default = true
    )

    @SettingKey
    val doNotRemindMeAgainPinLockWarning = boolean(
        title = R.string.do_not_remind_me_again_pin_lock,
        description = R.string.do_not_remind_me_again_pin_lock_desc,
        default = false
    )
}
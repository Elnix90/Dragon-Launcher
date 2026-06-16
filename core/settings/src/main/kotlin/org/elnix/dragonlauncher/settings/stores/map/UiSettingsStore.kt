package org.elnix.dragonlauncher.settings.stores.map

import io.github.elnix90.settings.SettingKey
import io.github.elnix90.settings.SettingStore
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.settings.DataStoreName
import org.elnix.dragonlauncher.settings.bases.objects.BooleanSettingObject.Companion.boolean
import org.elnix.dragonlauncher.settings.bases.objects.FloatSettingObject.Companion.float
import org.elnix.dragonlauncher.settings.bases.objects.IntSettingObject.Companion.int
import org.elnix.dragonlauncher.settings.bases.objects.StringSetSettingObject.Companion.stringSet
import org.elnix.dragonlauncher.settings.bases.objects.StringSettingObject.Companion.string
import org.elnix.dragonlauncher.settings.bases.stores.MapSettingsStore

@SettingStore
object UiSettingsStore : MapSettingsStore(DataStoreName.UI) {

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
        description = null,
        default = 20,
        allowedRange = 0..1000
    )

    @SettingKey
    val appLabelOverlaySize = int(
        title = R.string.app_label_overlay_size,
        description = null,
        default = 18,
        allowedRange = 0..100
    )

    @SettingKey
    val appIconOverlaySize = int(
        title = R.string.app_icon_overlay_size,
        description = null,
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
        description = null,
        default = true
    )

    @SettingKey
    val snapPoints = boolean(
        title = R.string.snap_points,
        description = null,
        default = true
    )

    @SettingKey
    val freeMoveDraggedPoint = boolean(
        title = R.string.free_move_dragged_point,
        description = null,
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
        description = null,
        default = 0f,
        allowedRange = 0f..1f
    )

    @SettingKey
    val wallpaperDimDrawerScreen = float(
        title = R.string.wallpaper_dim_amount,
        description = null,
        default = 0f,
        allowedRange = 0f..1f
    )

    @SettingKey
    val globalFont = string(
        title = null,
        description = null,
        default = "Default"
    )

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

    /**
     * Whether to use my custom-made color schemes for objects, or the default Android colors schemes.
     * For ex: my switch uses no borders, and other colors channels than the default one, while the android one has borders
     * */
    @SettingKey
    val useCustomColorChannels = boolean(
        title = R.string.use_custom_color_channels,
        description = R.string.use_custom_color_channels_desc,
        default = true
    )

    @SettingKey
    val mainScreenLayers = string(
        title = R.string.main_screen_layers,
        description = null,
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
        title = null,
        description = null,
        default = true,
    )

    @SettingKey
    val userThemes = stringSet(
        title = null,
        description = null,
        default = emptySet()
    )

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
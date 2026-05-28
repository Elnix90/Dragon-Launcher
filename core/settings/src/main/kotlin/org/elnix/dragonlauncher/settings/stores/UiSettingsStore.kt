package org.elnix.dragonlauncher.settings.stores

import org.elnix.dragonlauncher.settings.DataStoreName
import org.elnix.dragonlauncher.settings.bases.boolean
import org.elnix.dragonlauncher.settings.bases.float
import org.elnix.dragonlauncher.settings.bases.int
import org.elnix.dragonlauncher.settings.bases.objects.BaseSettingObject
import org.elnix.dragonlauncher.settings.bases.stores.MapSettingsStore
import org.elnix.dragonlauncher.settings.bases.string
import org.elnix.dragonlauncher.settings.bases.stringSet

object UiSettingsStore : MapSettingsStore(DataStoreName.UI) {

    /*  ─────────────  Use the computing of HSV color to produce a color that depends on the angle / progress  ─────────────  */
    val rgbLoading = boolean(
        key = "rgbLoading",
        default = true
    )

    val rgbLine = boolean(
        key = "rgbLine",
        default = true
    )


    /*  ─────────────  Overlay on top of the screen  ─────────────  */
    val showLaunchingAppLabel = boolean(
        key = "showLaunchingAppLabel",
        default = true,
    )

    val showLaunchingAppIcon = boolean(
        key = "showLaunchingAppIcon",
        default = true
    )
    val appLabelIconOverlayTopPadding = int(
        key = "appLabelIconOverlayTopPadding",
        default = 20,
        allowedRange = 0..1000
    )

    val appLabelOverlaySize = int(
        key = "appLabelOverlaySize",
        default = 18,
        allowedRange = 0..100
    )

    val appIconOverlaySize = int(
        key = "appIconOverlaySize",
        default = 22,
        allowedRange = 0..400
    )

    val fullScreen = boolean(
        key = "fullscreen",
        default = false
    )


    /* Used in settings screen internally to remember the 2 toggleable button */
    val autoSeparatePoints = boolean(
        key = "autoSeparatePoints",
        default = true
    )
    val snapPoints = boolean(
        key = "snapPoints",
        default = true
    )
    val freeMoveDraggedPoint = boolean(
        key = "freeMoveDraggedPoint",
        default = true
    )



    /*  ───────────── Advanced line preview customization  ─────────────  */
    val showCirclePreview = boolean(
        key = "showCirclePreview",
        default = true
    )
    val showAppPreviewIconCenterStartPosition = boolean(
        key = "showAppPreviewIconCenterStartPosition",
        default = false
    )
    val linePreviewSnapToAction = boolean(
        key = "linePreviewSnapToAction",
        default = false
    )


    /* Show the current selected app on drag in the main screen / show them all on the circle */
    val showAppLaunchingPreview = boolean(
        key = "showAppLaunchPreview",
        default = true
    )
    val showAllActionsOnCurrentCircle = boolean(
        key = "showAllActionsOnCurrentCircle",
        default = true
    )
    val showAllActionsOnCurrentNest = boolean(
        key = "showAllActionsOnCurrentNest",
        default = false
    )

    /*  ───────────── Wallpaper Things ─────────────  */
    val wallpaperDimMainScreen = float(
        key = "wallpaperDimMainScreen",
        default = 0f,
        allowedRange = 0f..1f
    )

    val wallpaperDimDrawerScreen = float(
        key = "wallpaperDimDrawerScreen",
        default = 0f,
        allowedRange = 0f..1f
    )

    val globalFont = string(
        key = "globalFont",
        default = "Default"
    )

    /** How far the points drawing system `actionsInCircle` draws the points */
    val maxNestsDepth = int(
        key = "maxNestsDepth",
        default = 2,
        allowedRange = 1..10
    )


    /** How many sub live nests can be drawn at once */
    val maxLiveNestsDepth = int(
        key = "maxLiveNestsDepth",
        default = 5,
        allowedRange = 1..10
    )

    /**
     * Whether to use my custom-made color schemes for objects, or the default Android colors schemes.
     * For ex: my switch uses no borders, and other colors channels than the default one, while the android one has borders
     * */
    val useCustomColorChannels = boolean(
        key = "useCustomColorChannels",
        default = true
    )

    val mainScreenLayers = string(
        key = "mainScreenLayers",
        default = ""
    )

    val cellSizeDp = int(
        key = "cellSizeDp",
        default = 30,
        allowedRange = 1..100
    )

    val showTooltipsOnAddPointDialog = boolean(
        key = "showTooltipsOnAddPointDialog",
        default = true,
    )

    val userThemes = stringSet(
        key = "userThemes",
        default = emptySet()
    )

    val multiplyOrSubtractOpacityInLiveNests = boolean(
        key = "multiplyOrSubtractOpacityInLiveNests",
        default = true
    )

    val doNotRemindMeAgainPinLockWarning = boolean(
        key = "doNotRemindMeAgainPinLockWarning",
        default = false
    )

    // unsing explicit this to avoid other stores that have the same name keys to be imported by mistake
    override val ALL: List<BaseSettingObject<*, *>> = listOf(
        this.rgbLoading,
        this.rgbLine,
        this.showLaunchingAppLabel,
        this.showLaunchingAppIcon,
        this.showAppLaunchingPreview,
        this.fullScreen,
        this.showCirclePreview,
        this.snapPoints,
        this.autoSeparatePoints,
        this.freeMoveDraggedPoint,
        this.showAppPreviewIconCenterStartPosition,
        this.linePreviewSnapToAction,
        this.showAllActionsOnCurrentCircle,
        this.showAllActionsOnCurrentNest,
        this.appLabelIconOverlayTopPadding,
        this.appLabelOverlaySize,
        this.appIconOverlaySize,
        this.wallpaperDimMainScreen,
        this.wallpaperDimDrawerScreen,
        this.globalFont,
        this.maxNestsDepth,
        this.maxLiveNestsDepth,
        this.useCustomColorChannels,
        this.mainScreenLayers,
        this.cellSizeDp,
        this.showTooltipsOnAddPointDialog,
        this.userThemes,
        this.multiplyOrSubtractOpacityInLiveNests,
        this.doNotRemindMeAgainPinLockWarning
    )
}

package org.elnix.dragonlauncher.settings.stores

import org.elnix.dragonlauncher.common.serializables.IconShape
import org.elnix.dragonlauncher.enumsui.toggle.DrawerActions
import org.elnix.dragonlauncher.enumsui.toggle.DrawerToolbar
import org.elnix.dragonlauncher.enumsui.toggle.HorizontalAlignment
import org.elnix.dragonlauncher.settings.DataStoreName
import org.elnix.dragonlauncher.settings.bases.BaseSettingObject
import org.elnix.dragonlauncher.settings.bases.MapSettingsStore
import org.elnix.dragonlauncher.settings.bases.Settings.boolean
import org.elnix.dragonlauncher.settings.bases.Settings.enum
import org.elnix.dragonlauncher.settings.bases.Settings.float
import org.elnix.dragonlauncher.settings.bases.Settings.int
import org.elnix.dragonlauncher.settings.bases.Settings.shape
import org.elnix.dragonlauncher.settings.bases.Settings.string
import org.elnix.dragonlauncher.settings.bases.Settings.stringSet


object DrawerSettingsStore : MapSettingsStore() {
    override val name: String = "Drawer"
    override val dataStoreName = DataStoreName.DRAWER

    val autoOpenSingleMatch = boolean(
        key = "autoOpenSingleMatch",
        dataStoreName = dataStoreName,
        default = true
    )
    val disableAutoLaunchOnSpaceFirstChar = boolean(
        key = "disableAutoLaunchOnSpaceFirstChar",
        dataStoreName = dataStoreName,
        default = true
    )


    val showAppIconsInDrawer = boolean(
        key = "showAppIconsInDrawer",
        dataStoreName = dataStoreName,
        default = true
    )

    val showAppLabelInDrawer = boolean(
        key = "showAppLabelInDrawer",
        dataStoreName = dataStoreName,
        default = true
    )

    val autoShowKeyboardOnDrawer = boolean(
        key = "autoShowKeyboardOnDrawer",
        dataStoreName = dataStoreName,
        default = true
    )

    val tapEmptySpaceAction = enum(
        key = "tabEmptySpaceToRaiseKeyboard",
        dataStoreName = dataStoreName,
        default = DrawerActions.CLOSE,
        enumClass = DrawerActions::class.java
    )

    val gridSize = int(
        key = "gridSize",
        dataStoreName = dataStoreName,
        default = 6,
        allowedRange = 1..15
    )

    val horizontalAlignment = enum(
        key = "horizontalAlignment",
        dataStoreName = dataStoreName,
        default = HorizontalAlignment.Start,
        enumClass = HorizontalAlignment::class.java
    )

    val lastWorkspaceUsed = string(
        key = "lastWorkspaceUsed",
        dataStoreName = dataStoreName,
        default = "",
    )

    val leftDrawerAction = enum(
        key = "leftDrawerAction",
        dataStoreName = dataStoreName,
        default = DrawerActions.DISABLED,
        enumClass = DrawerActions::class.java
    )

    val rightDrawerAction = enum(
        key = "rightDrawerAction",
        dataStoreName = dataStoreName,
        default = DrawerActions.DISABLED,
        enumClass = DrawerActions::class.java
    )

    val leftDrawerWidth = float(
        key = "leftDrawerWidth",
        dataStoreName = dataStoreName,
        default = 0f,
        allowedRange = 0f..1f
    )

    val rightDrawerWidth = float(
        key = "rightDrawerWidth",
        dataStoreName = dataStoreName,
        default = 0f,
        allowedRange = 0f..1f
    )

    val drawerEnterAction = enum(
        key = "drawerEnterAction",
        dataStoreName = dataStoreName,
        default = DrawerActions.CLEAR,
        enumClass = DrawerActions::class.java
    )

    val drawerHomeAction = enum(
        key = "drawerHomeAction",
        dataStoreName = dataStoreName,
        default = DrawerActions.CLOSE,
        enumClass = DrawerActions::class.java
    )

    val scrollDownDrawerAction = enum(
        key = "scrollDownDrawerAction",
        dataStoreName = dataStoreName,
        default = DrawerActions.CLOSE,
        enumClass = DrawerActions::class.java
    )

    val scrollUpDrawerAction = enum(
        key = "scrollUpDrawerAction",
        dataStoreName = dataStoreName,
        default = DrawerActions.CLOSE_KB,
        enumClass = DrawerActions::class.java
    )


    val backDrawerAction = enum(
        key = "backDrawerAction",
        dataStoreName = dataStoreName,
        default = DrawerActions.CLOSE,
        enumClass = DrawerActions::class.java
    )

    val iconsShape = shape(
        key = "iconsShape",
        dataStoreName = dataStoreName,
        default = IconShape.PlatformDefault
    )


    val iconsSpacingHorizontal = int(
        key = "iconsSpacingHorizontal²",
        dataStoreName = dataStoreName,
        default = 8,
        allowedRange = 0..50
    )


    val iconsSpacingVertical = int(
        key = "iconsSpacingVertical",
        dataStoreName = dataStoreName,
        default = 8,
        allowedRange = 0..50
    )
    val maxIconSize = int(
        key = "maxIconSize",
        dataStoreName = dataStoreName,
        default = 96,
        allowedRange = 0..200
    )

    val useCategory = boolean(
        key = "useCategory",
        dataStoreName = dataStoreName,
        default = false
    )

    val categoryGridWidth = int(
        key = "categoryGridWidth",
        dataStoreName = dataStoreName,
        default = 3,
        allowedRange = 1..4
    )


    val categoryGridCells = int(
        key = "categoryGridCells",
        dataStoreName = dataStoreName,
        default = 3,
        allowedRange = 2..5
    )

    val showCategoryName = boolean(
        key = "showCategoryName",
        dataStoreName = dataStoreName,
        default = true
    )

    val showSearchBar = boolean(
        key = "showSearchBar",
        dataStoreName = dataStoreName,
        default = true
    )


    /* ───────────── Recently Used Apps ───────────── */

    val showRecentlyUsedApps = boolean(
        key = "showRecentlyUsedApps",
        dataStoreName = dataStoreName,
        default = false
    )

    val recentlyUsedAppsCount = int(
        key = "recentlyUsedAppsCount",
        dataStoreName = dataStoreName,
        default = 5,
        allowedRange = 1..20
    )


    val recentlyUsedPackages = stringSet(
        key = "recentlyUsedPackagesSet",
        dataStoreName = dataStoreName,
        default = emptySet()
    )


    /*  ─────────────  Drawer pull down settings  ─────────────  */

    val pullDownAnimations = boolean(
        key = "pullDownAnimations",
        dataStoreName = dataStoreName,
        default = true
    )

    val pullDownWallPaperDimFade = boolean(
        key = "pullDownWallPaperDim",
        dataStoreName = dataStoreName,
        default = true
    )

    val pullDownIconFade = boolean(
        key = "pullDownIconFade",
        dataStoreName = dataStoreName,
        default = true
    )

    val pullDownScaleIn = boolean(
        key = "pullDownScaleIn",
        dataStoreName = dataStoreName,
        default = true
    )

    /**
     * The order of the search bar / recently used in drawer
     */
    val toolbarsOrder = string(
        key = "toolbarsOrder",
        dataStoreName = dataStoreName,
        default = DrawerToolbar.entries.joinToString(",") { it.toString() }
    )


    override val ALL: List<BaseSettingObject<*, *>>
        get() = listOf(
            this.autoOpenSingleMatch,
            this.disableAutoLaunchOnSpaceFirstChar,
            this.showAppIconsInDrawer,
            this.showAppLabelInDrawer,
            this.autoShowKeyboardOnDrawer,
            this.tapEmptySpaceAction,
            this.gridSize,
            this.horizontalAlignment,
            this.lastWorkspaceUsed,
            this.leftDrawerAction,
            this.rightDrawerAction,
            this.leftDrawerWidth,
            this.rightDrawerWidth,
            this.drawerEnterAction,
            this.drawerHomeAction,
            this.scrollDownDrawerAction,
            this.scrollUpDrawerAction,
            this.iconsShape,
            this.iconsSpacingVertical,
            this.iconsSpacingHorizontal,
            this.maxIconSize,
            this.useCategory,
            this.showSearchBar,
            this.showRecentlyUsedApps,
            this.recentlyUsedAppsCount,
//            this.recentlyUsedPackages, // Don't put it in the ALL value, this way it won't be backup, and the onSetting changed wont trigger on new app launch
            this.categoryGridWidth,
            this.categoryGridCells,
            this.showCategoryName,
            this.backDrawerAction,
            this.pullDownAnimations,
            this.pullDownWallPaperDimFade,
            this.pullDownIconFade,
            this.pullDownScaleIn,
            this.toolbarsOrder
        )
}

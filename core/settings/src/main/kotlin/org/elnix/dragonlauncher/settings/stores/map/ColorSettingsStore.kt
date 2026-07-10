package org.elnix.dragonlauncher.settings.stores.map

import android.content.Context
import androidx.compose.ui.graphics.Color
import io.github.elnix90.annotations.SettingKey
import io.github.elnix90.annotations.SettingsStore
import io.github.elnix90.core.objects.ColorSettingObject
import io.github.elnix90.core.objects.color
import io.github.elnix90.core.stores.MapSettingsStore
import org.elnix.dragonlauncher.base.theme.AmoledDragonColorScheme
import org.elnix.dragonlauncher.base.theme.DefaultExtraColors
import org.elnix.dragonlauncher.base.util.ColorUtils
import org.elnix.dragonlauncher.i18n.R

@SettingsStore
public object ColorSettingsStore : MapSettingsStore() {

    @SettingKey
    public val primaryColor: ColorSettingObject = color(
        title = R.string.primary_color,
        default = AmoledDragonColorScheme.primary
    )

    @SettingKey
    public val onPrimaryColor: ColorSettingObject = color(
        title = R.string.on_primary_color,
        default = AmoledDragonColorScheme.onPrimary
    )

    @SettingKey
    public val secondaryColor: ColorSettingObject = color(
        title = R.string.secondary_color,
        default = AmoledDragonColorScheme.secondary
    )

    @SettingKey
    public val onSecondaryColor: ColorSettingObject = color(
        title = R.string.on_secondary_color,
        default = AmoledDragonColorScheme.onSecondary
    )

    @SettingKey
    public val tertiaryColor: ColorSettingObject = color(
        title = R.string.tertiary_color,
        default = AmoledDragonColorScheme.tertiary
    )

    @SettingKey
    public val onTertiaryColor: ColorSettingObject = color(
        title = R.string.on_tertiary_color,
        default = AmoledDragonColorScheme.onTertiary
    )

    @SettingKey
    public val backgroundColor: ColorSettingObject = color(
        title = R.string.background_color,
        default = AmoledDragonColorScheme.background
    )

    @SettingKey
    public val onBackgroundColor: ColorSettingObject = color(
        title = R.string.on_background_color,
        default = AmoledDragonColorScheme.onBackground
    )

    @SettingKey
    public val surfaceColor: ColorSettingObject = color(
        title = R.string.surface_color,
        default = AmoledDragonColorScheme.surface
    )

    @SettingKey
    public val onSurfaceColor: ColorSettingObject = color(
        title = R.string.on_surface_color,
        default = AmoledDragonColorScheme.onSecondary
    )

    @SettingKey
    public val errorColor: ColorSettingObject = color(
        title = R.string.error_color,
        default = AmoledDragonColorScheme.error
    )

    @SettingKey
    public val onErrorColor: ColorSettingObject = color(
        title = R.string.on_error_color,
        default = AmoledDragonColorScheme.onError
    )

    @SettingKey
    public val outlineColor: ColorSettingObject = color(
        title = R.string.outline_color,
        default = AmoledDragonColorScheme.outline
    )

    @SettingKey
    public val primaryContainerColor: ColorSettingObject = color(
        title = R.string.primary_container_color,
        default = AmoledDragonColorScheme.primaryContainer
    )

    @SettingKey
    public val onPrimaryContainerColor: ColorSettingObject = color(
        title = R.string.on_primary_container_color,
        default = AmoledDragonColorScheme.onPrimaryContainer
    )

    @SettingKey
    public val inversePrimaryColor: ColorSettingObject = color(
        title = R.string.inverse_primary_color,
        default = AmoledDragonColorScheme.inversePrimary
    )

    @SettingKey
    public val secondaryContainerColor: ColorSettingObject = color(
        title = R.string.secondary_container_color,
        default = AmoledDragonColorScheme.secondaryContainer
    )

    @SettingKey
    public val onSecondaryContainerColor: ColorSettingObject = color(
        title = R.string.on_secondary_container_color,
        default = AmoledDragonColorScheme.onSecondaryContainer
    )

    @SettingKey
    public val tertiaryContainerColor: ColorSettingObject = color(
        title = R.string.tertiary_container_color,
        default = AmoledDragonColorScheme.tertiaryContainer
    )

    @SettingKey
    public val onTertiaryContainerColor: ColorSettingObject = color(
        title = R.string.on_tertiary_container_color,
        default = AmoledDragonColorScheme.onTertiaryContainer
    )

    @SettingKey
    public val surfaceVariantColor: ColorSettingObject = color(
        title = R.string.surface_variant_color,
        default = AmoledDragonColorScheme.surfaceVariant
    )

    @SettingKey
    public val onSurfaceVariantColor: ColorSettingObject = color(
        title = R.string.on_surface_variant_color,
        default = AmoledDragonColorScheme.onSurfaceVariant
    )

    @SettingKey
    public val surfaceTintColor: ColorSettingObject = color(
        title = R.string.surface_tint_color,
        default = AmoledDragonColorScheme.surfaceTint
    )

    @SettingKey
    public val inverseSurfaceColor: ColorSettingObject = color(
        title = R.string.inverse_surface_color,
        default = AmoledDragonColorScheme.inverseSurface
    )

    @SettingKey
    public val inverseOnSurfaceColor: ColorSettingObject = color(
        title = R.string.inverse_on_surface_color,
        default = AmoledDragonColorScheme.inverseSurface
    )

    @SettingKey
    public val errorContainerColor: ColorSettingObject = color(
        title = R.string.error_container_color,
        default = AmoledDragonColorScheme.errorContainer
    )

    @SettingKey
    public val onErrorContainerColor: ColorSettingObject = color(
        title = R.string.on_error_container_color,
        default = AmoledDragonColorScheme.onErrorContainer
    )

    @SettingKey
    public val outlineVariantColor: ColorSettingObject = color(
        title = R.string.outline_variant_color,
        default = AmoledDragonColorScheme.outlineVariant
    )

    @SettingKey
    public val scrimColor: ColorSettingObject = color(
        title = R.string.scrim_color,
        default = AmoledDragonColorScheme.scrim
    )

    @SettingKey
    public val surfaceBrightColor: ColorSettingObject = color(
        title = R.string.surface_bright_color,
        default = AmoledDragonColorScheme.surfaceBright
    )

    @SettingKey
    public val surfaceContainerColor: ColorSettingObject = color(
        title = R.string.surface_container_color,
        default = AmoledDragonColorScheme.surfaceContainer
    )

    @SettingKey
    public val surfaceContainerHighColor: ColorSettingObject = color(
        title = R.string.surface_container_high_color,
        default = AmoledDragonColorScheme.surfaceContainerHigh
    )

    @SettingKey
    public val surfaceContainerHighestColor: ColorSettingObject = color(
        title = R.string.surface_container_highest_color,
        default = AmoledDragonColorScheme.surfaceContainerHighest
    )

    @SettingKey
    public val surfaceContainerLowColor: ColorSettingObject = color(
        title = R.string.surface_container_low_color,
        default = AmoledDragonColorScheme.surfaceContainerLow
    )

    @SettingKey
    public val surfaceContainerLowestColor: ColorSettingObject = color(
        title = R.string.surface_container_lowest_color,
        default = AmoledDragonColorScheme.surfaceContainerLowest
    )

    @SettingKey
    public val surfaceDimColor: ColorSettingObject = color(
        title = R.string.surface_dim_color,
        default = AmoledDragonColorScheme.surfaceDim
    )

    @SettingKey
    public val primaryFixedColor: ColorSettingObject = color(
        title = R.string.primary_fixed_color,
        default = AmoledDragonColorScheme.primaryFixed
    )

    @SettingKey
    public val primaryFixedDimColor: ColorSettingObject = color(
        title = R.string.primary_fixed_dim_color,
        default = AmoledDragonColorScheme.primaryFixedDim
    )

    @SettingKey
    public val onPrimaryFixedColor: ColorSettingObject = color(
        title = R.string.on_primary_fixed_color,
        default = AmoledDragonColorScheme.onPrimaryFixed
    )

    @SettingKey
    public val onPrimaryFixedVariantColor: ColorSettingObject = color(
        title = R.string.on_primary_fixed_variant_color,
        default = AmoledDragonColorScheme.onPrimaryFixed
    )

    @SettingKey
    public val secondaryFixedColor: ColorSettingObject = color(
        title = R.string.secondary_fixed_color,
        default = AmoledDragonColorScheme.secondaryFixed
    )

    @SettingKey
    public val secondaryFixedDimColor: ColorSettingObject = color(
        title = R.string.secondary_fixed_dim_color,
        default = AmoledDragonColorScheme.secondaryFixedDim
    )

    @SettingKey
    public val onSecondaryFixedColor: ColorSettingObject = color(
        title = R.string.on_secondary_fixed_color,
        default = AmoledDragonColorScheme.onSecondaryFixed
    )

    @SettingKey
    public val onSecondaryFixedVariantColor: ColorSettingObject = color(
        title = R.string.on_secondary_fixed_variant_color,
        default = AmoledDragonColorScheme.onSurfaceVariant
    )

    @SettingKey
    public val tertiaryFixedColor: ColorSettingObject = color(
        title = R.string.tertiary_color,
        default = AmoledDragonColorScheme.tertiaryFixed
    )

    @SettingKey
    public val tertiaryFixedDimColor: ColorSettingObject = color(
        title = R.string.tertiary_color,
        default = AmoledDragonColorScheme.tertiaryFixedDim
    )

    @SettingKey
    public val onTertiaryFixedColor: ColorSettingObject = color(
        title = R.string.on_tertiary_fixed_color,
        default = AmoledDragonColorScheme.onTertiaryFixed
    )

    @SettingKey
    public val onTertiaryFixedVariantColor: ColorSettingObject = color(
        title = R.string.on_tertiary_fixed_variant_color,
        default = AmoledDragonColorScheme.onTertiaryFixedVariant
    )

    
    // Custom Colors

    @SettingKey
    public val angleLineColor: ColorSettingObject = color(
        title = R.string.angle_line_color,
        default = DefaultExtraColors.angleLine
    )

    @SettingKey
    public val holdToActivateColor: ColorSettingObject = color(
        title = R.string.hold_to_activate_color,
        default = DefaultExtraColors.holdToActivate
    )


    @SettingKey
    public val shapesColor: ColorSettingObject = color(
        title = R.string.shapes_color,
        default = DefaultExtraColors.shapes
    )

    @SettingKey
    public val launchAppColor: ColorSettingObject = color(
        title = R.string.launch_app_color,
        default = DefaultExtraColors.launchApp
    )

    @SettingKey
    public val openUrlColor: ColorSettingObject = color(
        title = R.string.open_url_color,
        default = DefaultExtraColors.openUrl
    )

    @SettingKey
    public val notificationShadeColor: ColorSettingObject = color(
        title = R.string.notification_shade_color,
        default = DefaultExtraColors.notificationShade
    )

    @SettingKey
    public val controlPanelColor: ColorSettingObject = color(
        title = R.string.control_panel_color,
        default = DefaultExtraColors.controlPanel
    )

    @SettingKey
    public val openAppDrawerColor: ColorSettingObject = color(
        title = R.string.open_app_drawer_color,
        default = DefaultExtraColors.openAppDrawer
    )

    @SettingKey
    public val launcherSettingsColor: ColorSettingObject = color(
        title = R.string.launcher_settings_color,
        default = DefaultExtraColors.launcherSettings
    )

    @SettingKey
    public val lockColor: ColorSettingObject = color(
        title = R.string.lock_color,
        default = DefaultExtraColors.lock
    )

    @SettingKey
    public val openFileColor: ColorSettingObject = color(
        title = R.string.open_file_color,
        default = DefaultExtraColors.openFile
    )

    @SettingKey
    public val reloadColor: ColorSettingObject = color(
        title = R.string.reload_color,
        default = DefaultExtraColors.reload
    )

    @SettingKey
    public val openRecentAppsColor: ColorSettingObject = color(
        title = R.string.open_recent_apps_color,
        default = DefaultExtraColors.openRecentApps
    )

    @SettingKey
    public val openCircleNestColor: ColorSettingObject = color(
        title = R.string.open_circle_nest_color,
        default = DefaultExtraColors.openCircleNest
    )

    @SettingKey
    public val goParentNestColor: ColorSettingObject = color(
        title = R.string.go_parent_nest_color,
        default = DefaultExtraColors.goParentNest
    )

    @SettingKey
    public val toggleWifi: ColorSettingObject = color(
        title = R.string.toggle_wifi,
        default = DefaultExtraColors.toggleWifi
    )

    @SettingKey
    public val toggleBluetooth: ColorSettingObject = color(
        title = R.string.toggle_bluetooth,
        default = DefaultExtraColors.toggleBluetooth
    )

    @SettingKey
    public val toggleData: ColorSettingObject = color(
        title = R.string.toggle_mobile_data,
        default = DefaultExtraColors.toggleData
    )

    @SettingKey
    public val runAdbCommand: ColorSettingObject = color(
        title = R.string.run_adb_command,
        default = DefaultExtraColors.runAdbCommand
    )


    public suspend fun setAllRandomColors(ctx: Context) {
        setAllColors(ctx) { ColorUtils.randomColor() }
    }

    public suspend fun setAllSameColors(ctx: Context, color: Color) {
        setAllColors(ctx) { color }
    }

    public suspend fun setAllColors(ctx: Context, color: () -> Color) {
        ALL.forEach { (it as ColorSettingObject).set(ctx, color()) }
    }

    // For test mode backup
    private val backupColorsMap = mutableMapOf<String, Color?>()

    public suspend fun backupColors(ctx: Context) {
        backupColorsMap.clear()
        ALL.forEach { setting ->
            setting as ColorSettingObject

            backupColorsMap[setting.key] = setting.getOrNull(ctx)
        }
    }

    public suspend fun restoreColors(ctx: Context) {
        backupColorsMap.forEach { (key, color) ->
            (ALL.find { it.key == key } as? ColorSettingObject)?.set(ctx, color)
        }
        backupColorsMap.clear()
    }
}

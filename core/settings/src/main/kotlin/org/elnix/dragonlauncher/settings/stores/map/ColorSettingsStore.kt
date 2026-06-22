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
object ColorSettingsStore : MapSettingsStore() {

    @SettingKey
    val primaryColor = color(
        title = R.string.primary_color,
        default = AmoledDragonColorScheme.primary
    )

    @SettingKey
    val onPrimaryColor = color(
        title = R.string.on_primary_color,
        default = AmoledDragonColorScheme.onPrimary
    )

    @SettingKey
    val secondaryColor = color(
        title = R.string.secondary_color,
        default = AmoledDragonColorScheme.secondary
    )

    @SettingKey
    val onSecondaryColor = color(
        title = R.string.on_secondary_color,
        default = AmoledDragonColorScheme.onSecondary
    )

    @SettingKey
    val tertiaryColor = color(
        title = R.string.tertiary_color,
        default = AmoledDragonColorScheme.tertiary
    )

    @SettingKey
    val onTertiaryColor = color(
        title = R.string.on_tertiary_color,
        default = AmoledDragonColorScheme.onTertiary
    )

    @SettingKey
    val backgroundColor = color(
        title = R.string.background_color,
        default = AmoledDragonColorScheme.background
    )

    @SettingKey
    val onBackgroundColor = color(
        title = R.string.on_background_color,
        default = AmoledDragonColorScheme.onBackground
    )

    @SettingKey
    val surfaceColor = color(
        title = R.string.surface_color,
        default = AmoledDragonColorScheme.surface
    )

    @SettingKey
    val onSurfaceColor = color(
        title = R.string.on_surface_color,
        default = AmoledDragonColorScheme.onSecondary
    )

    @SettingKey
    val errorColor = color(
        title = R.string.error_color,
        default = AmoledDragonColorScheme.error
    )

    @SettingKey
    val onErrorColor = color(
        title = R.string.on_error_color,
        default = AmoledDragonColorScheme.onError
    )

    @SettingKey
    val outlineColor = color(
        title = R.string.outline_color,
        default = AmoledDragonColorScheme.outline
    )

    @SettingKey
    val primaryContainerColor = color(
        title = R.string.primary_container_color,
        default = AmoledDragonColorScheme.primaryContainer
    )

    @SettingKey
    val onPrimaryContainerColor = color(
        title = R.string.on_primary_container_color,
        default = AmoledDragonColorScheme.onPrimaryContainer
    )

    @SettingKey
    val inversePrimaryColor = color(
        title = R.string.inverse_primary_color,
        default = AmoledDragonColorScheme.inversePrimary
    )

    @SettingKey
    val secondaryContainerColor = color(
        title = R.string.secondary_container_color,
        default = AmoledDragonColorScheme.secondaryContainer
    )

    @SettingKey
    val onSecondaryContainerColor = color(
        title = R.string.on_secondary_container_color,
        default = AmoledDragonColorScheme.onSecondaryContainer
    )

    @SettingKey
    val tertiaryContainerColor = color(
        title = R.string.tertiary_container_color,
        default = AmoledDragonColorScheme.tertiaryContainer
    )

    @SettingKey
    val onTertiaryContainerColor = color(
        title = R.string.on_tertiary_container_color,
        default = AmoledDragonColorScheme.onTertiaryContainer
    )

    @SettingKey
    val surfaceVariantColor = color(
        title = R.string.surface_variant_color,
        default = AmoledDragonColorScheme.surfaceVariant
    )

    @SettingKey
    val onSurfaceVariantColor = color(
        title = R.string.on_surface_variant_color,
        default = AmoledDragonColorScheme.onSurfaceVariant
    )

    @SettingKey
    val surfaceTintColor = color(
        title = R.string.surface_tint_color,
        default = AmoledDragonColorScheme.surfaceTint
    )

    @SettingKey
    val inverseSurfaceColor = color(
        title = R.string.inverse_surface_color,
        default = AmoledDragonColorScheme.inverseSurface
    )

    @SettingKey
    val inverseOnSurfaceColor = color(
        title = R.string.inverse_on_surface_color,
        default = AmoledDragonColorScheme.inverseSurface
    )

    @SettingKey
    val errorContainerColor = color(
        title = R.string.error_container_color,
        default = AmoledDragonColorScheme.errorContainer
    )

    @SettingKey
    val onErrorContainerColor = color(
        title = R.string.on_error_container_color,
        default = AmoledDragonColorScheme.onErrorContainer
    )

    @SettingKey
    val outlineVariantColor = color(
        title = R.string.outline_variant_color,
        default = AmoledDragonColorScheme.outlineVariant
    )

    @SettingKey
    val scrimColor = color(
        title = R.string.scrim_color,
        default = AmoledDragonColorScheme.scrim
    )

    @SettingKey
    val surfaceBrightColor = color(
        title = R.string.surface_bright_color,
        default = AmoledDragonColorScheme.surfaceBright
    )

    @SettingKey
    val surfaceContainerColor = color(
        title = R.string.surface_container_color,
        default = AmoledDragonColorScheme.surfaceContainer
    )

    @SettingKey
    val surfaceContainerHighColor = color(
        title = R.string.surface_container_high_color,
        default = AmoledDragonColorScheme.surfaceContainerHigh
    )

    @SettingKey
    val surfaceContainerHighestColor = color(
        title = R.string.surface_container_highest_color,
        default = AmoledDragonColorScheme.surfaceContainerHighest
    )

    @SettingKey
    val surfaceContainerLowColor = color(
        title = R.string.surface_container_low_color,
        default = AmoledDragonColorScheme.surfaceContainerLow
    )

    @SettingKey
    val surfaceContainerLowestColor = color(
        title = R.string.surface_container_lowest_color,
        default = AmoledDragonColorScheme.surfaceContainerLowest
    )

    @SettingKey
    val surfaceDimColor = color(
        title = R.string.surface_dim_color,
        default = AmoledDragonColorScheme.surfaceDim
    )

    @SettingKey
    val primaryFixedColor = color(
        title = R.string.primary_fixed_color,
        default = AmoledDragonColorScheme.primaryFixed
    )

    @SettingKey
    val primaryFixedDimColor = color(
        title = R.string.primary_fixed_dim_color,
        default = AmoledDragonColorScheme.primaryFixedDim
    )

    @SettingKey
    val onPrimaryFixedColor = color(
        title = R.string.on_primary_fixed_color,
        default = AmoledDragonColorScheme.onPrimaryFixed
    )

    @SettingKey
    val onPrimaryFixedVariantColor = color(
        title = R.string.on_primary_fixed_variant_color,
        default = AmoledDragonColorScheme.onPrimaryFixed
    )

    @SettingKey
    val secondaryFixedColor = color(
        title = R.string.secondary_fixed_color,
        default = AmoledDragonColorScheme.secondaryFixed
    )

    @SettingKey
    val secondaryFixedDimColor = color(
        title = R.string.secondary_fixed_dim_color,
        default = AmoledDragonColorScheme.secondaryFixedDim
    )

    @SettingKey
    val onSecondaryFixedColor = color(
        title = R.string.on_secondary_fixed_color,
        default = AmoledDragonColorScheme.onSecondaryFixed
    )

    @SettingKey
    val onSecondaryFixedVariantColor = color(
        title = R.string.on_secondary_fixed_variant_color,
        default = AmoledDragonColorScheme.onSurfaceVariant
    )

    @SettingKey
    val tertiaryFixedColor = color(
        title = R.string.tertiary_color,
        default = AmoledDragonColorScheme.tertiaryFixed
    )

    @SettingKey
    val tertiaryFixedDimColor = color(
        title = R.string.tertiary_color,
        default = AmoledDragonColorScheme.tertiaryFixedDim
    )

    @SettingKey
    val onTertiaryFixedColor = color(
        title = R.string.on_tertiary_fixed_color,
        default = AmoledDragonColorScheme.onTertiaryFixed
    )

    @SettingKey
    val onTertiaryFixedVariantColor = color(
        title = R.string.on_tertiary_fixed_variant_color,
        default = AmoledDragonColorScheme.onTertiaryFixedVariant
    )

    
    // Custom Colors

    @SettingKey
    val angleLineColor = color(
        title = R.string.angle_line_color,
        default = DefaultExtraColors.angleLine
    )

    @Deprecated("Have to change that system to a per-circle selection")
    @SettingKey
    val circleColor = color(
        title = R.string.circle_color,
        default = DefaultExtraColors.circle
    )

    @SettingKey
    val launchAppColor = color(
        title = R.string.launch_app_color,
        default = DefaultExtraColors.launchApp
    )

    @SettingKey
    val openUrlColor = color(
        title = R.string.open_url_color,
        default = DefaultExtraColors.openUrl
    )

    @SettingKey
    val notificationShadeColor = color(
        title = R.string.notification_shade_color,
        default = DefaultExtraColors.notificationShade
    )

    @SettingKey
    val controlPanelColor = color(
        title = R.string.control_panel_color,
        default = DefaultExtraColors.controlPanel
    )

    @SettingKey
    val openAppDrawerColor = color(
        title = R.string.open_app_drawer_color,
        default = DefaultExtraColors.openAppDrawer
    )

    @SettingKey
    val launcherSettingsColor = color(
        title = R.string.launcher_settings_color,
        default = DefaultExtraColors.launcherSettings
    )

    @SettingKey
    val lockColor = color(
        title = R.string.lock_color,
        default = DefaultExtraColors.lock
    )

    @SettingKey
    val openFileColor = color(
        title = R.string.open_file_color,
        default = DefaultExtraColors.openFile
    )

    @SettingKey
    val reloadColor = color(
        title = R.string.reload_color,
        default = DefaultExtraColors.reload
    )

    @SettingKey
    val openRecentAppsColor = color(
        title = R.string.open_recent_apps_color,
        default = DefaultExtraColors.openRecentApps
    )

    @SettingKey
    val openCircleNestColor = color(
        title = R.string.open_circle_nest_color,
        default = DefaultExtraColors.openCircleNest
    )

    @SettingKey
    val goParentNestColor = color(
        title = R.string.go_parent_nest_color,
        default = DefaultExtraColors.goParentNest
    )

    @SettingKey
    val toggleWifi = color(
        title = R.string.toggle_wifi,
        default = DefaultExtraColors.toggleWifi
    )

    @SettingKey
    val toggleBluetooth = color(
        title = R.string.toggle_bluetooth,
        default = DefaultExtraColors.toggleBluetooth
    )

    @SettingKey
    val toggleData = color(
        title = R.string.toggle_mobile_data,
        default = DefaultExtraColors.toggleData
    )

    @SettingKey
    val runAdbCommand = color(
        title = R.string.run_adb_command,
        default = DefaultExtraColors.runAdbCommand
    )


    suspend fun setAllRandomColors(ctx: Context) {
        setAllColors(ctx) { ColorUtils.randomColor() }
    }

    suspend fun setAllSameColors(ctx: Context, color: Color) {
        setAllColors(ctx) { color }
    }

    suspend fun setAllColors(ctx: Context, color: () -> Color) {
        ALL.forEach { (it as ColorSettingObject).set(ctx, color()) }
    }

    // For test mode backup
    private val backupColorsMap = mutableMapOf<String, Color?>()

    suspend fun backupColors(ctx: Context) {
        backupColorsMap.clear()
        ALL.forEach { setting ->
            setting as ColorSettingObject

            backupColorsMap[setting.key] = setting.getOrNull(ctx)
        }
    }

    suspend fun restoreColors(ctx: Context) {
        backupColorsMap.forEach { (key, color) ->
            (ALL.find { it.key == key } as? ColorSettingObject)?.set(ctx, color)
        }
        backupColorsMap.clear()
    }
}

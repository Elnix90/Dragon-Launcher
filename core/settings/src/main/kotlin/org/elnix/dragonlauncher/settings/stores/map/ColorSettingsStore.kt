package org.elnix.dragonlauncher.settings.stores.map

import android.content.Context
import androidx.compose.ui.graphics.Color
import org.elnix.dragonlauncher.base.util.ColorUtils
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.settings.DataStoreName
import org.elnix.dragonlauncher.settings.bases.objects.BaseSettingObject
import org.elnix.dragonlauncher.settings.bases.objects.ColorSettingObject.Companion.color
import org.elnix.dragonlauncher.settings.bases.stores.MapSettingsStore

object ColorSettingsStore : MapSettingsStore(DataStoreName.COLOR) {

    override val ALL: List<BaseSettingObject<Color, String>>
        get() = listOf(
            this.primaryColor,
            this.onPrimaryColor,
            this.primaryContainerColor,
            this.onPrimaryContainerColor,
            this.inversePrimaryColor,
            this.secondaryColor,
            this.onSecondaryColor,
            this.secondaryContainerColor,
            this.onSecondaryContainerColor,
            this.tertiaryColor,
            this.onTertiaryColor,
            this.tertiaryContainerColor,
            this.onTertiaryContainerColor,
            this.backgroundColor,
            this.onBackgroundColor,
            this.surfaceColor,
            this.onSurfaceColor,
            this.surfaceVariantColor,
            this.onSurfaceVariantColor,
            this.surfaceTintColor,
            this.inverseSurfaceColor,
            this.inverseOnSurfaceColor,
            this.errorColor,
            this.onErrorColor,
            this.errorContainerColor,
            this.onErrorContainerColor,
            this.outlineColor,
            this.outlineVariantColor,
            this.scrimColor,
            this.surfaceBrightColor,
            this.surfaceContainerColor,
            this.surfaceContainerHighColor,
            this.surfaceContainerHighestColor,
            this.surfaceContainerLowColor,
            this.surfaceContainerLowestColor,
            this.surfaceDimColor,
            this.primaryFixedColor,
            this.primaryFixedDimColor,
            this.onPrimaryFixedColor,
            this.onPrimaryFixedVariantColor,
            this.secondaryFixedColor,
            this.secondaryFixedDimColor,
            this.onSecondaryFixedColor,
            this.onSecondaryFixedVariantColor,
            this.tertiaryFixedColor,
            this.tertiaryFixedDimColor,
            this.onTertiaryFixedColor,
            this.onTertiaryFixedVariantColor,
            this.angleLineColor,
            this.circleColor,
            this.launchAppColor,
            this.openUrlColor,
            this.notificationShadeColor,
            this.controlPanelColor,
            this.openAppDrawerColor,
            this.launcherSettingsColor,
            this.lockColor,
            this.openFileColor,
            this.reloadColor,
            this.openRecentAppsColor,
            this.openCircleNestColor,
            this.goParentNestColor,
            this.toggleWifi,
            this.toggleData,
            this.toggleBluetooth,
            this.runAdbCommand
        )

    val primaryColor by color(
        title = R.string.primary_color,
        description = null,
        default = Color.Unspecified
    )

    val onPrimaryColor by color(
        title = R.string.on_primary_color,
        description = null,
        default = Color.Unspecified
    )

    val secondaryColor by color(
        title = R.string.secondary_color,
        description = null,
        default = Color.Unspecified
    )

    val onSecondaryColor by color(
        title = R.string.on_secondary_color,
        description = null,
        default = Color.Unspecified
    )

    val tertiaryColor by color(
        title = R.string.tertiary_color,
        description = null,
        default = Color.Unspecified
    )

    val onTertiaryColor by color(
        title = R.string.on_tertiary_color,
        description = null,
        default = Color.Unspecified
    )

    val backgroundColor by color(
        title = R.string.background_color,
        description = null,
        default = Color.Unspecified
    )

    val onBackgroundColor by color(
        title = R.string.on_background_color,
        description = null,
        default = Color.Unspecified
    )

    val surfaceColor by color(
        title = R.string.surface_color,
        description = null,
        default = Color.Unspecified
    )

    val onSurfaceColor by color(
        title = R.string.on_surface_color,
        description = null,
        default = Color.Unspecified
    )

    val errorColor by color(
        title = R.string.error_color,
        description = null,
        default = Color.Unspecified
    )

    val onErrorColor by color(
        title = R.string.on_error_color,
        description = null,
        default = Color.Unspecified
    )

    val outlineColor by color(
        title = R.string.outline_color,
        description = null,
        default = Color.Unspecified
    )

    val angleLineColor by color(
        title = R.string.angle_line_color,
        description = null,
        default = Color.Unspecified
    )

    val circleColor by color(
        title = R.string.circle_color,
        description = null,
        default = Color.Unspecified
    )

    val primaryContainerColor by color(
        title = R.string.primary_container_color,
        description = null,
        default = Color.Unspecified
    )

    val onPrimaryContainerColor by color(
        title = R.string.on_primary_container_color,
        description = null,
        default = Color.Unspecified
    )

    val inversePrimaryColor by color(
        title = R.string.inverse_primary_color,
        description = null,
        default = Color.Unspecified
    )

    val secondaryContainerColor by color(
        title = R.string.secondary_container_color,
        description = null,
        default = Color.Unspecified
    )

    val onSecondaryContainerColor by color(
        title = R.string.on_secondary_container_color,
        description = null,
        default = Color.Unspecified
    )

    val tertiaryContainerColor by color(
        title = R.string.tertiary_container_color,
        description = null,
        default = Color.Unspecified
    )

    val onTertiaryContainerColor by color(
        title = R.string.on_tertiary_container_color,
        description = null,
        default = Color.Unspecified
    )

    val surfaceVariantColor by color(
        title = R.string.surface_variant_color,
        description = null,
        default = Color.Unspecified
    )

    val onSurfaceVariantColor by color(
        title = R.string.on_surface_variant_color,
        description = null,
        default = Color.Unspecified
    )

    val surfaceTintColor by color(
        title = R.string.surface_tint_color,
        description = null,
        default = Color.Unspecified
    )

    val inverseSurfaceColor by color(
        title = R.string.inverse_surface_color,
        description = null,
        default = Color.Unspecified
    )

    val inverseOnSurfaceColor by color(
        title = R.string.inverse_on_surface_color,
        description = null,
        default = Color.Unspecified
    )

    val errorContainerColor by color(
        title = R.string.error_container_color,
        description = null,
        default = Color.Unspecified
    )

    val onErrorContainerColor by color(
        title = R.string.on_error_container_color,
        description = null,
        default = Color.Unspecified
    )

    val outlineVariantColor by color(
        title = R.string.outline_variant_color,
        description = null,
        default = Color.Unspecified
    )

    val scrimColor by color(
        title = R.string.scrim_color,
        description = null,
        default = Color.Unspecified
    )

    val surfaceBrightColor by color(
        title = R.string.surface_bright_color,
        description = null,
        default = Color.Unspecified
    )

    val surfaceContainerColor by color(
        title = R.string.surface_container_color,
        description = null,
        default = Color.Unspecified
    )

    val surfaceContainerHighColor by color(
        title = R.string.surface_container_high_color,
        description = null,
        default = Color.Unspecified
    )

    val surfaceContainerHighestColor by color(
        title = R.string.surface_container_highest_color,
        description = null,
        default = Color.Unspecified
    )

    val surfaceContainerLowColor by color(
        title = R.string.surface_container_low_color,
        description = null,
        default = Color.Unspecified
    )

    val surfaceContainerLowestColor by color(
        title = R.string.surface_container_lowest_color,
        description = null,
        default = Color.Unspecified
    )

    val surfaceDimColor by color(
        title = R.string.surface_dim_color,
        description = null,
        default = Color.Unspecified
    )

    val primaryFixedColor by color(
        title = R.string.primary_fixed_color,
        description = null,
        default = Color.Unspecified
    )

    val primaryFixedDimColor by color(
        title = R.string.primary_fixed_dim_color,
        description = null,
        default = Color.Unspecified
    )

    val onPrimaryFixedColor by color(
        title = R.string.on_primary_fixed_color,
        description = null,
        default = Color.Unspecified
    )

    val onPrimaryFixedVariantColor by color(
        title = R.string.on_primary_fixed_variant_color,
        description = null,
        default = Color.Unspecified
    )

    val secondaryFixedColor by color(
        title = R.string.secondary_fixed_color,
        description = null,
        default = Color.Unspecified
    )

    val secondaryFixedDimColor by color(
        title = R.string.secondary_fixed_dim_color,
        description = null,
        default = Color.Unspecified
    )

    val onSecondaryFixedColor by color(
        title = R.string.on_secondary_fixed_color,
        description = null,
        default = Color.Unspecified
    )

    val onSecondaryFixedVariantColor by color(
        title = R.string.on_secondary_fixed_variant_color,
        description = null,
        default = Color.Unspecified
    )

    val tertiaryFixedColor by color(
        title = R.string.tertiary_color,
        description = null,
        default = Color.Unspecified
    )

    val tertiaryFixedDimColor by color(
        title = R.string.tertiary_color,
        description = null,
        default = Color.Unspecified
    )

    val onTertiaryFixedColor by color(
        title = R.string.on_tertiary_fixed_color,
        description = null,
        default = Color.Unspecified
    )

    val onTertiaryFixedVariantColor by color(
        title = R.string.on_tertiary_fixed_variant_color,
        description = null,
        default = Color.Unspecified
    )


    val launchAppColor by color(
        title = R.string.launch_app_color,
        description = null,
        default = Color.Unspecified
    )

    val openUrlColor by color(
        title = R.string.open_url_color,
        description = null,
        default = Color.Unspecified
    )

    val notificationShadeColor by color(
        title = R.string.notification_shade_color,
        description = null,
        default = Color.Unspecified
    )

    val controlPanelColor by color(
        title = R.string.control_panel_color,
        description = null,
        default = Color.Unspecified
    )

    val openAppDrawerColor by color(
        title = R.string.open_app_drawer_color,
        description = null,
        default = Color.Unspecified
    )

    val launcherSettingsColor by color(
        title = R.string.launcher_settings_color,
        description = null,
        default = Color.Unspecified
    )

    val lockColor by color(
        title = R.string.lock_color,
        description = null,
        default = Color.Unspecified
    )

    val openFileColor by color(
        title = R.string.open_file_color,
        description = null,
        default = Color.Unspecified
    )

    val reloadColor by color(
        title = R.string.reload_color,
        description = null,
        default = Color.Unspecified
    )

    val openRecentAppsColor by color(
        title = R.string.open_recent_apps_color,
        description = null,
        default = Color.Unspecified
    )

    val openCircleNestColor by color(
        title = R.string.open_circle_nest_color,
        description = null,
        default = Color.Unspecified
    )

    val goParentNestColor by color(
        title = R.string.go_parent_nest_color,
        description = null,
        default = Color.Unspecified
    )

    val toggleWifi by color(
        title = R.string.toggle_wifi,
        description = null,
        default = Color.Unspecified
    )

    val toggleBluetooth by color(
        title = R.string.toggle_bluetooth,
        description = null,
        default = Color.Unspecified
    )
    val toggleData by color(
        title = R.string.toggle_mobile_data,
        description = null,
        default = Color.Unspecified
    )

    val runAdbCommand by color(
        title = R.string.run_adb_command,
        description = null,
        default = Color.Unspecified
    )


    suspend fun setAllRandomColors(ctx: Context) {
        setAllColors(ctx) { ColorUtils.randomColor() }
    }

    suspend fun setAllSameColors(ctx: Context, color: Color) {
        setAllColors(ctx) { color }
    }

    suspend fun setAllColors(ctx: Context, color: () -> Color) {
        ALL.forEach { it.set(ctx, color()) }
    }

    // For test mode backup
    private val backupColorsMap = mutableMapOf<String, Color?>()

    suspend fun backupColors(ctx: Context) {
        backupColorsMap.clear()
        ALL.forEach { setting ->
            backupColorsMap[setting.key] = setting.getOrNull(ctx)
        }
    }

    suspend fun restoreColors(ctx: Context) {
        backupColorsMap.forEach { (key, color) ->
            ALL.find { it.key == key }?.set(ctx, color)
        }
        backupColorsMap.clear()
    }
}
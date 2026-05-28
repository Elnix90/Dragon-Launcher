package org.elnix.dragonlauncher.settings.stores

import android.content.Context
import androidx.compose.ui.graphics.Color
import org.elnix.dragonlauncher.base.ColorUtils.randomColor
import org.elnix.dragonlauncher.settings.DataStoreName
import org.elnix.dragonlauncher.settings.bases.color
import org.elnix.dragonlauncher.settings.bases.objects.BaseSettingObject
import org.elnix.dragonlauncher.settings.bases.stores.MapSettingsStore


object ColorSettingsStore : MapSettingsStore(DataStoreName.COLOR) {

    val primaryColor = color(
        key = "primary_color",
        default = Color.Unspecified
    )

    val onPrimaryColor = color(
        key = "on_primary_color",
        default = Color.Unspecified
    )

    val secondaryColor = color(
        key = "secondary_color",
        default = Color.Unspecified
    )

    val onSecondaryColor = color(
        key = "on_secondary_color",
        default = Color.Unspecified
    )

    val tertiaryColor = color(
        key = "tertiary_color",
        default = Color.Unspecified
    )

    val onTertiaryColor = color(
        key = "on_tertiary_color",
        default = Color.Unspecified
    )

    val backgroundColor = color(
        key = "background_color",
        default = Color.Unspecified
    )

    val onBackgroundColor = color(
        key = "on_background_color",
        default = Color.Unspecified
    )

    val surfaceColor = color(
        key = "surface_color",
        default = Color.Unspecified
    )

    val onSurfaceColor = color(
        key = "on_surface_color",
        default = Color.Unspecified
    )

    val errorColor = color(
        key = "error_color",
        default = Color.Unspecified
    )

    val onErrorColor = color(
        key = "on_error_color",
        default = Color.Unspecified
    )

    val outlineColor = color(
        key = "outline_color",
        default = Color.Unspecified
    )

    val angleLineColor = color(
        key = "angle_line_color",
        default = Color.Unspecified
    )

    val circleColor = color(
        key = "circle_color",
        default = Color.Unspecified
    )

    val primaryContainerColor = color(
        key = "primary_container_color",
        default = Color.Unspecified
    )

    val onPrimaryContainerColor = color(
        key = "on_primary_container_color",
        default = Color.Unspecified
    )

    val inversePrimaryColor = color(
        key = "inverse_primary_color",
        default = Color.Unspecified
    )

    val secondaryContainerColor = color(
        key = "secondary_container_color",
        default = Color.Unspecified
    )

    val onSecondaryContainerColor = color(
        key = "on_secondary_container_color",
        default = Color.Unspecified
    )

    val tertiaryContainerColor = color(
        key = "tertiary_container_color",
        default = Color.Unspecified
    )

    val onTertiaryContainerColor = color(
        key = "on_tertiary_container_color",
        default = Color.Unspecified
    )

    val surfaceVariantColor = color(
        key = "surface_variant_color",
        default = Color.Unspecified
    )

    val onSurfaceVariantColor = color(
        key = "on_surface_variant_color",
        default = Color.Unspecified
    )

    val surfaceTintColor = color(
        key = "surface_tint_color",
        default = Color.Unspecified
    )

    val inverseSurfaceColor = color(
        key = "inverse_surface_color",
        default = Color.Unspecified
    )

    val inverseOnSurfaceColor = color(
        key = "inverse_on_surface_color",
        default = Color.Unspecified
    )

    val errorContainerColor = color(
        key = "error_container_color",
        default = Color.Unspecified
    )

    val onErrorContainerColor = color(
        key = "on_error_container_color",
        default = Color.Unspecified
    )

    val outlineVariantColor = color(
        key = "outline_variant_color",
        default = Color.Unspecified
    )

    val scrimColor = color(
        key = "scrim_color",
        default = Color.Unspecified
    )

    val surfaceBrightColor = color(
        key = "surface_bright_color",
        default = Color.Unspecified
    )

    val surfaceContainerColor = color(
        key = "surface_container_color",
        default = Color.Unspecified
    )

    val surfaceContainerHighColor = color(
        key = "surface_container_high_color",
        default = Color.Unspecified
    )

    val surfaceContainerHighestColor = color(
        key = "surface_container_highest_color",
        default = Color.Unspecified
    )

    val surfaceContainerLowColor = color(
        key = "surface_container_low_color",
        default = Color.Unspecified
    )

    val surfaceContainerLowestColor = color(
        key = "surface_container_lowest_color",
        default = Color.Unspecified
    )

    val surfaceDimColor = color(
        key = "surface_dim_color",
        default = Color.Unspecified
    )

    val primaryFixedColor = color(
        key = "primary_fixed_color",
        default = Color.Unspecified
    )

    val primaryFixedDimColor = color(
        key = "primary_fixed_dim_color",
        default = Color.Unspecified
    )

    val onPrimaryFixedColor = color(
        key = "on_primary_fixed_color",
        default = Color.Unspecified
    )

    val onPrimaryFixedVariantColor = color(
        key = "on_primary_fixed_variant_color",
        default = Color.Unspecified
    )

    val secondaryFixedColor = color(
        key = "secondary_fixed_color",
        default = Color.Unspecified
    )

    val secondaryFixedDimColor = color(
        key = "secondary_fixed_dim_color",
        default = Color.Unspecified
    )

    val onSecondaryFixedColor = color(
        key = "on_secondary_fixed_color",
        default = Color.Unspecified
    )

    val onSecondaryFixedVariantColor = color(
        key = "on_secondary_fixed_variant_color",
        default = Color.Unspecified
    )

    val tertiaryFixedColor = color(
        key = "tertiary_fixed_color",
        default = Color.Unspecified
    )

    val tertiaryFixedDimColor = color(
        key = "tertiary_fixed_dim_color",
        default = Color.Unspecified
    )

    val onTertiaryFixedColor = color(
        key = "on_tertiary_fixed_color",
        default = Color.Unspecified
    )

    val onTertiaryFixedVariantColor = color(
        key = "on_tertiary_fixed_variant_color",
        default = Color.Unspecified
    )


    /* ───────────── Action colors ───────────── */

    val launchAppColor = color(
        key = "launch_app_color",
        default = Color.Unspecified
    )

    val openUrlColor = color(
        key = "open_url_color",
        default = Color.Unspecified
    )

    val notificationShadeColor = color(
        key = "notification_shade_color",
        default = Color.Unspecified
    )

    val controlPanelColor = color(
        key = "control_panel_color",
        default = Color.Unspecified
    )

    val openAppDrawerColor = color(
        key = "open_app_drawer_color",
        default = Color.Unspecified
    )

    val launcherSettingsColor = color(
        key = "launcher_settings_color",
        default = Color.Unspecified
    )

    val lockColor = color(
        key = "lock_color",
        default = Color.Unspecified
    )

    val openFileColor = color(
        key = "open_file_color",
        default = Color.Unspecified
    )

    val reloadColor = color(
        key = "reload_color",
        default = Color.Unspecified
    )

    val openRecentAppsColor = color(
        key = "open_recent_apps",
        default = Color.Unspecified
    )

    val openCircleNestColor = color(
        key = "open_circle_nest",
        default = Color.Unspecified
    )

    val goParentNestColor = color(
        key = "go_parent_nest",
        default = Color.Unspecified
    )

    val toggleWifi = color(
        key = "toggleWifi",
        default = Color.Unspecified
    )

    val toggleBluetooth = color(
        key = "toggleBluetooth",
        default = Color.Unspecified
    )
    val toggleData = color(
        key = "toggleData",
        default = Color.Unspecified
    )

    val runAdbCommand = color(
        key = "runAdbCommand",
        default = Color.Unspecified
    )


    /* ───────────── Registry ───────────── */

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

    suspend fun setAllRandomColors(ctx: Context) {
        setAllColors(ctx) { randomColor() }
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

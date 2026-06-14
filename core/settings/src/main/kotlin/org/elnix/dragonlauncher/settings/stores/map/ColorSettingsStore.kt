package org.elnix.dragonlauncher.settings.stores.map

import android.content.Context
import androidx.compose.ui.graphics.Color
import org.elnix.dragonlauncher.base.util.ColorUtils
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.settings.DataStoreName
import org.elnix.dragonlauncher.settings.bases.objects.BaseSettingObject
import org.elnix.dragonlauncher.settings.bases.objects.ColorSettingObject.Companion.color
import org.elnix.dragonlauncher.settings.bases.stores.MapSettingsStore
import org.elnix.settings.SettingKey
import org.elnix.settings.SettingStore

@SettingStore
object ColorSettingsStore : MapSettingsStore(DataStoreName.COLOR) {

    override val ALL: List<BaseSettingObject<Color, String>> by lazy {
        listOf(
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
    }

    @SettingKey
    val primaryColor = color(
        title = R.string.primary_color,
        description = null,
        default = Color.Unspecified
    )

    @SettingKey
    val onPrimaryColor = color(
        title = R.string.on_primary_color,
        description = null,
        default = Color.Unspecified
    )

    @SettingKey
    val secondaryColor = color(
        title = R.string.secondary_color,
        description = null,
        default = Color.Unspecified
    )

    @SettingKey
    val onSecondaryColor = color(
        title = R.string.on_secondary_color,
        description = null,
        default = Color.Unspecified
    )

    @SettingKey
    val tertiaryColor = color(
        title = R.string.tertiary_color,
        description = null,
        default = Color.Unspecified
    )

    @SettingKey
    val onTertiaryColor = color(
        title = R.string.on_tertiary_color,
        description = null,
        default = Color.Unspecified
    )

    @SettingKey
    val backgroundColor = color(
        title = R.string.background_color,
        description = null,
        default = Color.Unspecified
    )

    @SettingKey
    val onBackgroundColor = color(
        title = R.string.on_background_color,
        description = null,
        default = Color.Unspecified
    )

    @SettingKey
    val surfaceColor = color(
        title = R.string.surface_color,
        description = null,
        default = Color.Unspecified
    )

    @SettingKey
    val onSurfaceColor = color(
        title = R.string.on_surface_color,
        description = null,
        default = Color.Unspecified
    )

    @SettingKey
    val errorColor = color(
        title = R.string.error_color,
        description = null,
        default = Color.Unspecified
    )

    @SettingKey
    val onErrorColor = color(
        title = R.string.on_error_color,
        description = null,
        default = Color.Unspecified
    )

    @SettingKey
    val outlineColor = color(
        title = R.string.outline_color,
        description = null,
        default = Color.Unspecified
    )

    @SettingKey
    val angleLineColor = color(
        title = R.string.angle_line_color,
        description = null,
        default = Color.Unspecified
    )

    @SettingKey
    val circleColor = color(
        title = R.string.circle_color,
        description = null,
        default = Color.Unspecified
    )

    @SettingKey
    val primaryContainerColor = color(
        title = R.string.primary_container_color,
        description = null,
        default = Color.Unspecified
    )

    @SettingKey
    val onPrimaryContainerColor = color(
        title = R.string.on_primary_container_color,
        description = null,
        default = Color.Unspecified
    )

    @SettingKey
    val inversePrimaryColor = color(
        title = R.string.inverse_primary_color,
        description = null,
        default = Color.Unspecified
    )

    @SettingKey
    val secondaryContainerColor = color(
        title = R.string.secondary_container_color,
        description = null,
        default = Color.Unspecified
    )

    @SettingKey
    val onSecondaryContainerColor = color(
        title = R.string.on_secondary_container_color,
        description = null,
        default = Color.Unspecified
    )

    @SettingKey
    val tertiaryContainerColor = color(
        title = R.string.tertiary_container_color,
        description = null,
        default = Color.Unspecified
    )

    @SettingKey
    val onTertiaryContainerColor = color(
        title = R.string.on_tertiary_container_color,
        description = null,
        default = Color.Unspecified
    )

    @SettingKey
    val surfaceVariantColor = color(
        title = R.string.surface_variant_color,
        description = null,
        default = Color.Unspecified
    )

    @SettingKey
    val onSurfaceVariantColor = color(
        title = R.string.on_surface_variant_color,
        description = null,
        default = Color.Unspecified
    )

    @SettingKey
    val surfaceTintColor = color(
        title = R.string.surface_tint_color,
        description = null,
        default = Color.Unspecified
    )

    @SettingKey
    val inverseSurfaceColor = color(
        title = R.string.inverse_surface_color,
        description = null,
        default = Color.Unspecified
    )

    @SettingKey
    val inverseOnSurfaceColor = color(
        title = R.string.inverse_on_surface_color,
        description = null,
        default = Color.Unspecified
    )

    @SettingKey
    val errorContainerColor = color(
        title = R.string.error_container_color,
        description = null,
        default = Color.Unspecified
    )

    @SettingKey
    val onErrorContainerColor = color(
        title = R.string.on_error_container_color,
        description = null,
        default = Color.Unspecified
    )

    @SettingKey
    val outlineVariantColor = color(
        title = R.string.outline_variant_color,
        description = null,
        default = Color.Unspecified
    )

    @SettingKey
    val scrimColor = color(
        title = R.string.scrim_color,
        description = null,
        default = Color.Unspecified
    )

    @SettingKey
    val surfaceBrightColor = color(
        title = R.string.surface_bright_color,
        description = null,
        default = Color.Unspecified
    )

    @SettingKey
    val surfaceContainerColor = color(
        title = R.string.surface_container_color,
        description = null,
        default = Color.Unspecified
    )

    @SettingKey
    val surfaceContainerHighColor = color(
        title = R.string.surface_container_high_color,
        description = null,
        default = Color.Unspecified
    )

    @SettingKey
    val surfaceContainerHighestColor = color(
        title = R.string.surface_container_highest_color,
        description = null,
        default = Color.Unspecified
    )

    @SettingKey
    val surfaceContainerLowColor = color(
        title = R.string.surface_container_low_color,
        description = null,
        default = Color.Unspecified
    )

    @SettingKey
    val surfaceContainerLowestColor = color(
        title = R.string.surface_container_lowest_color,
        description = null,
        default = Color.Unspecified
    )

    @SettingKey
    val surfaceDimColor = color(
        title = R.string.surface_dim_color,
        description = null,
        default = Color.Unspecified
    )

    @SettingKey
    val primaryFixedColor = color(
        title = R.string.primary_fixed_color,
        description = null,
        default = Color.Unspecified
    )

    @SettingKey
    val primaryFixedDimColor = color(
        title = R.string.primary_fixed_dim_color,
        description = null,
        default = Color.Unspecified
    )

    @SettingKey
    val onPrimaryFixedColor = color(
        title = R.string.on_primary_fixed_color,
        description = null,
        default = Color.Unspecified
    )

    @SettingKey
    val onPrimaryFixedVariantColor = color(
        title = R.string.on_primary_fixed_variant_color,
        description = null,
        default = Color.Unspecified
    )

    @SettingKey
    val secondaryFixedColor = color(
        title = R.string.secondary_fixed_color,
        description = null,
        default = Color.Unspecified
    )

    @SettingKey
    val secondaryFixedDimColor = color(
        title = R.string.secondary_fixed_dim_color,
        description = null,
        default = Color.Unspecified
    )

    @SettingKey
    val onSecondaryFixedColor = color(
        title = R.string.on_secondary_fixed_color,
        description = null,
        default = Color.Unspecified
    )

    @SettingKey
    val onSecondaryFixedVariantColor = color(
        title = R.string.on_secondary_fixed_variant_color,
        description = null,
        default = Color.Unspecified
    )

    @SettingKey
    val tertiaryFixedColor = color(
        title = R.string.tertiary_color,
        description = null,
        default = Color.Unspecified
    )

    @SettingKey
    val tertiaryFixedDimColor = color(
        title = R.string.tertiary_color,
        description = null,
        default = Color.Unspecified
    )

    @SettingKey
    val onTertiaryFixedColor = color(
        title = R.string.on_tertiary_fixed_color,
        description = null,
        default = Color.Unspecified
    )

    @SettingKey
    val onTertiaryFixedVariantColor = color(
        title = R.string.on_tertiary_fixed_variant_color,
        description = null,
        default = Color.Unspecified
    )


    @SettingKey
    val launchAppColor = color(
        title = R.string.launch_app_color,
        description = null,
        default = Color.Unspecified
    )

    @SettingKey
    val openUrlColor = color(
        title = R.string.open_url_color,
        description = null,
        default = Color.Unspecified
    )

    @SettingKey
    val notificationShadeColor = color(
        title = R.string.notification_shade_color,
        description = null,
        default = Color.Unspecified
    )

    @SettingKey
    val controlPanelColor = color(
        title = R.string.control_panel_color,
        description = null,
        default = Color.Unspecified
    )

    @SettingKey
    val openAppDrawerColor = color(
        title = R.string.open_app_drawer_color,
        description = null,
        default = Color.Unspecified
    )

    @SettingKey
    val launcherSettingsColor = color(
        title = R.string.launcher_settings_color,
        description = null,
        default = Color.Unspecified
    )

    @SettingKey
    val lockColor = color(
        title = R.string.lock_color,
        description = null,
        default = Color.Unspecified
    )

    @SettingKey
    val openFileColor = color(
        title = R.string.open_file_color,
        description = null,
        default = Color.Unspecified
    )

    @SettingKey
    val reloadColor = color(
        title = R.string.reload_color,
        description = null,
        default = Color.Unspecified
    )

    @SettingKey
    val openRecentAppsColor = color(
        title = R.string.open_recent_apps_color,
        description = null,
        default = Color.Unspecified
    )

    @SettingKey
    val openCircleNestColor = color(
        title = R.string.open_circle_nest_color,
        description = null,
        default = Color.Unspecified
    )

    @SettingKey
    val goParentNestColor = color(
        title = R.string.go_parent_nest_color,
        description = null,
        default = Color.Unspecified
    )

    @SettingKey
    val toggleWifi = color(
        title = R.string.toggle_wifi,
        description = null,
        default = Color.Unspecified
    )

    @SettingKey
    val toggleBluetooth = color(
        title = R.string.toggle_bluetooth,
        description = null,
        default = Color.Unspecified
    )

    @SettingKey
    val toggleData = color(
        title = R.string.toggle_mobile_data,
        description = null,
        default = Color.Unspecified
    )

    @SettingKey
    val runAdbCommand = color(
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
package org.elnix.dragonlauncher.colors

import android.content.Context
import androidx.compose.material3.ColorScheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import org.elnix.dragonlauncher.base.theme.ExtraColors
import org.elnix.dragonlauncher.settings.stores.map.ColorSettingsStore

interface ColorService {
    val extraColors: Flow<ExtraColors>
    val colors: Flow<ColorScheme>
}

internal class ColorServiceImpl(
    ctx: Context
) : ColorService {

    private val angleLineColor = ColorSettingsStore.angleLineColor.flow(ctx)
    private val circleColor = ColorSettingsStore.circleColor.flow(ctx)
    private val launchAppColor = ColorSettingsStore.launchAppColor.flow(ctx)
    private val openUrlColor = ColorSettingsStore.openUrlColor.flow(ctx)
    private val notificationShadeColor = ColorSettingsStore.notificationShadeColor.flow(ctx)
    private val controlPanelColor = ColorSettingsStore.controlPanelColor.flow(ctx)
    private val openAppDrawerColor = ColorSettingsStore.openAppDrawerColor.flow(ctx)
    private val launcherSettingsColor = ColorSettingsStore.launcherSettingsColor.flow(ctx)
    private val lockColor = ColorSettingsStore.lockColor.flow(ctx)
    private val openFileColor = ColorSettingsStore.openFileColor.flow(ctx)
    private val reloadAppsColor = ColorSettingsStore.reloadColor.flow(ctx)
    private val openRecentAppsColor = ColorSettingsStore.openRecentAppsColor.flow(ctx)
    private val openCircleNestColor = ColorSettingsStore.openCircleNestColor.flow(ctx)
    private val goParentNestColor = ColorSettingsStore.goParentNestColor.flow(ctx)
    private val toggleBluetooth = ColorSettingsStore.toggleBluetooth.flow(ctx)
    private val toggleData = ColorSettingsStore.toggleData.flow(ctx)
    private val toggleWifi = ColorSettingsStore.toggleWifi.flow(ctx)
    private val runAdbCommand = ColorSettingsStore.runAdbCommand.flow(ctx)

    override val extraColors: Flow<ExtraColors> = combine(
        angleLineColor,
        circleColor,
        launchAppColor,
        openUrlColor,
        notificationShadeColor,
        controlPanelColor,
        openAppDrawerColor,
        launcherSettingsColor,
        lockColor,
        openFileColor,
        reloadAppsColor,
        openRecentAppsColor,
        openCircleNestColor,
        goParentNestColor,
        toggleBluetooth,
        toggleData,
        toggleWifi,
        runAdbCommand
    ) { extraColors ->
        ExtraColors(
            angleLine = extraColors[0],
            circle = extraColors[1],
            launchApp = extraColors[2],
            openUrl = extraColors[3],
            notificationShade = extraColors[4],
            controlPanel = extraColors[5],
            openAppDrawer = extraColors[6],
            launcherSettings = extraColors[7],
            lock = extraColors[8],
            openFile = extraColors[9],
            reload = extraColors[10],
            openRecentApps = extraColors[11],
            openCircleNest = extraColors[12],
            goParentNest = extraColors[13],
            toggleBluetooth = extraColors[14],
            toggleData = extraColors[15],
            toggleWifi = extraColors[16],
            runAdbCommand = extraColors[17]
        )
    }


    private val primary = ColorSettingsStore.primaryColor.flow(ctx)
    private val onPrimary = ColorSettingsStore.onPrimaryColor.flow(ctx)
    private val primaryContainer = ColorSettingsStore.primaryContainerColor.flow(ctx)
    private val onPrimaryContainer = ColorSettingsStore.onPrimaryContainerColor.flow(ctx)
    private val inversePrimary = ColorSettingsStore.inversePrimaryColor.flow(ctx)
    private val primaryFixed = ColorSettingsStore.primaryFixedColor.flow(ctx)
    private val primaryFixedDim = ColorSettingsStore.primaryFixedDimColor.flow(ctx)
    private val onPrimaryFixed = ColorSettingsStore.onPrimaryFixedColor.flow(ctx)
    private val onPrimaryFixedVariant = ColorSettingsStore.onPrimaryFixedVariantColor.flow(ctx)

    private val secondary = ColorSettingsStore.secondaryColor.flow(ctx)
    private val onSecondary = ColorSettingsStore.onSecondaryColor.flow(ctx)
    private val secondaryContainer = ColorSettingsStore.secondaryContainerColor.flow(ctx)
    private val onSecondaryContainer = ColorSettingsStore.onSecondaryContainerColor.flow(ctx)
    private val secondaryFixed = ColorSettingsStore.secondaryFixedColor.flow(ctx)
    private val secondaryFixedDim = ColorSettingsStore.secondaryFixedDimColor.flow(ctx)
    private val onSecondaryFixed = ColorSettingsStore.onSecondaryFixedColor.flow(ctx)
    private val onSecondaryFixedVariant = ColorSettingsStore.onSecondaryFixedVariantColor.flow(ctx)

    private val tertiary = ColorSettingsStore.tertiaryColor.flow(ctx)
    private val onTertiary = ColorSettingsStore.onTertiaryColor.flow(ctx)
    private val tertiaryContainer = ColorSettingsStore.tertiaryContainerColor.flow(ctx)
    private val onTertiaryContainer = ColorSettingsStore.onTertiaryContainerColor.flow(ctx)
    private val tertiaryFixed = ColorSettingsStore.tertiaryFixedColor.flow(ctx)
    private val tertiaryFixedDim = ColorSettingsStore.tertiaryFixedDimColor.flow(ctx)
    private val onTertiaryFixed = ColorSettingsStore.onTertiaryFixedColor.flow(ctx)
    private val onTertiaryFixedVariant = ColorSettingsStore.onTertiaryFixedVariantColor.flow(ctx)

    private val background = ColorSettingsStore.backgroundColor.flow(ctx)
    private val onBackground = ColorSettingsStore.onBackgroundColor.flow(ctx)
    private val surface = ColorSettingsStore.surfaceColor.flow(ctx)
    private val onSurface = ColorSettingsStore.onSurfaceColor.flow(ctx)
    private val surfaceVariant = ColorSettingsStore.surfaceVariantColor.flow(ctx)
    private val onSurfaceVariant = ColorSettingsStore.onSurfaceVariantColor.flow(ctx)
    private val surfaceTint = ColorSettingsStore.surfaceTintColor.flow(ctx)
    private val inverseSurface = ColorSettingsStore.inverseSurfaceColor.flow(ctx)
    private val inverseOnSurface = ColorSettingsStore.inverseOnSurfaceColor.flow(ctx)

    private val surfaceBright = ColorSettingsStore.surfaceBrightColor.flow(ctx)
    private val surfaceDim = ColorSettingsStore.surfaceDimColor.flow(ctx)
    private val surfaceContainer = ColorSettingsStore.surfaceContainerColor.flow(ctx)
    private val surfaceContainerLow = ColorSettingsStore.surfaceContainerLowColor.flow(ctx)
    private val surfaceContainerLowest = ColorSettingsStore.surfaceContainerLowestColor.flow(ctx)
    private val surfaceContainerHigh = ColorSettingsStore.surfaceContainerHighColor.flow(ctx)
    private val surfaceContainerHighest = ColorSettingsStore.surfaceContainerHighestColor.flow(ctx)

    private val error = ColorSettingsStore.errorColor.flow(ctx)
    private val onError = ColorSettingsStore.onErrorColor.flow(ctx)
    private val errorContainer = ColorSettingsStore.errorContainerColor.flow(ctx)
    private val onErrorContainer = ColorSettingsStore.onErrorContainerColor.flow(ctx)

    private val outline = ColorSettingsStore.outlineColor.flow(ctx)
    private val outlineVariant = ColorSettingsStore.outlineVariantColor.flow(ctx)
    private val scrim = ColorSettingsStore.scrimColor.flow(ctx)

    override val colors: Flow<ColorScheme> = combine(
        primary,
        onPrimary,
        primaryContainer,
        onPrimaryContainer,
        inversePrimary,
        primaryFixed,
        primaryFixedDim,
        onPrimaryFixed,
        onPrimaryFixedVariant,

        secondary,
        onSecondary,
        secondaryContainer,
        onSecondaryContainer,
        secondaryFixed,
        secondaryFixedDim,
        onSecondaryFixed,
        onSecondaryFixedVariant,

        tertiary,
        onTertiary,
        tertiaryContainer,
        onTertiaryContainer,
        tertiaryFixed,
        tertiaryFixedDim,
        onTertiaryFixed,
        onTertiaryFixedVariant,

        background,
        onBackground,
        surface,
        onSurface,
        surfaceVariant,
        onSurfaceVariant,
        surfaceTint,
        inverseSurface,
        inverseOnSurface,

        surfaceBright,
        surfaceDim,
        surfaceContainer,
        surfaceContainerLow,
        surfaceContainerLowest,
        surfaceContainerHigh,
        surfaceContainerHighest,

        error,
        onError,
        errorContainer,
        onErrorContainer,

        outline,
        outlineVariant,
        scrim
    ) { colors ->
        ColorScheme(
            primary = colors[0],
            onPrimary = colors[1],
            primaryContainer = colors[2],
            onPrimaryContainer = colors[3],
            inversePrimary = colors[4],
            primaryFixed = colors[5],
            primaryFixedDim = colors[6],
            onPrimaryFixed = colors[7],
            onPrimaryFixedVariant = colors[8],

            secondary = colors[9],
            onSecondary = colors[10],
            secondaryContainer = colors[11],
            onSecondaryContainer = colors[12],
            secondaryFixed = colors[13],
            secondaryFixedDim = colors[14],
            onSecondaryFixed = colors[15],
            onSecondaryFixedVariant = colors[16],

            tertiary = colors[17],
            onTertiary = colors[18],
            tertiaryContainer = colors[19],
            onTertiaryContainer = colors[20],
            tertiaryFixed = colors[21],
            tertiaryFixedDim = colors[22],
            onTertiaryFixed = colors[23],
            onTertiaryFixedVariant = colors[24],

            background = colors[25],
            onBackground = colors[26],
            surface = colors[27],
            onSurface = colors[28],
            surfaceVariant = colors[29],
            onSurfaceVariant = colors[30],
            surfaceTint = colors[31],
            inverseSurface = colors[32],
            inverseOnSurface = colors[33],

            surfaceBright = colors[34],
            surfaceDim = colors[35],
            surfaceContainer = colors[36],
            surfaceContainerLow = colors[37],
            surfaceContainerLowest = colors[38],
            surfaceContainerHigh = colors[39],
            surfaceContainerHighest = colors[40],

            error = colors[41],
            onError = colors[42],
            errorContainer = colors[43],
            onErrorContainer = colors[44],

            outline = colors[45],
            outlineVariant = colors[46],
            scrim = colors[47]
        )
    }
}
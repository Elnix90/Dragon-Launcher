@file:Suppress("AssignedValueIsNeverRead")

package org.elnix.dragonlauncher.ui.settings.customization


import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.base.theme.DefaultExtraColors
import org.elnix.dragonlauncher.base.util.ColorUtils.alphaMultiplier
import org.elnix.dragonlauncher.base.util.ColorUtils.definedOrNull
import org.elnix.dragonlauncher.enumsui.select.ColorSelectorModes
import org.elnix.dragonlauncher.enumsui.toggle.DefaultThemes
import org.elnix.dragonlauncher.enumsui.toggle.DefaultThemes.AMOLED
import org.elnix.dragonlauncher.enumsui.toggle.DefaultThemes.CUSTOM
import org.elnix.dragonlauncher.enumsui.toggle.DefaultThemes.DARK
import org.elnix.dragonlauncher.enumsui.toggle.DefaultThemes.LIGHT
import org.elnix.dragonlauncher.enumsui.toggle.DefaultThemes.SYSTEM
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.settings.bases.objects.BaseSettingObject
import org.elnix.dragonlauncher.settings.stores.map.ColorModesSettingsStore
import org.elnix.dragonlauncher.settings.stores.map.ColorSettingsStore
import org.elnix.dragonlauncher.settings.stores.map.UiSettingsStore
import org.elnix.dragonlauncher.theme.AppObjectsColors
import org.elnix.dragonlauncher.theme.getSystemColorScheme
import org.elnix.dragonlauncher.ui.base.UiConstants.DragonShape
import org.elnix.dragonlauncher.ui.base.animation.bouncySpec
import org.elnix.dragonlauncher.ui.base.asState
import org.elnix.dragonlauncher.ui.base.asStateNull
import org.elnix.dragonlauncher.ui.base.components.Spacer
import org.elnix.dragonlauncher.ui.base.modifiers.conditional
import org.elnix.dragonlauncher.ui.components.burger.BurgerListAction
import org.elnix.dragonlauncher.ui.components.burger.MoreOptions
import org.elnix.dragonlauncher.ui.dragon.colors.ColorPickerRow
import org.elnix.dragonlauncher.ui.dragon.components.DragonButton
import org.elnix.dragonlauncher.ui.dragon.components.DragonIconButton
import org.elnix.dragonlauncher.ui.dragon.components.SwitchRow
import org.elnix.dragonlauncher.ui.dragon.dialogs.UserValidation
import org.elnix.dragonlauncher.ui.dragon.expandable.ExpandableSection
import org.elnix.dragonlauncher.ui.dragon.expandable.ExpandableSectionState
import org.elnix.dragonlauncher.ui.dragon.expandable.rememberExpandableSection
import org.elnix.dragonlauncher.ui.dragon.generic.SingleSelectConnectedButtonRow
import org.elnix.dragonlauncher.ui.dragon.settings.SettingsColorPicker
import org.elnix.dragonlauncher.ui.dragon.settings.SettingsSwitchRow
import org.elnix.dragonlauncher.ui.helpers.settings.SettingsScaffold

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ColorSelectorTab(
    onBack: (() -> Unit)
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    val dynamicColors by ColorModesSettingsStore.dynamicColor.asState()
    val defaultTheme by ColorModesSettingsStore.defaultTheme.asState()

    val defaultColorScheme = getSystemColorScheme(defaultTheme, dynamicColors)

    val primaryColors = listOf(
        ColorEdit(ColorSettingsStore.primaryColor, stringResource(R.string.primary_color), defaultColorScheme.primary),
        ColorEdit(
            ColorSettingsStore.onPrimaryColor,
            stringResource(R.string.on_primary_color),
            defaultColorScheme.onPrimary
        ),
        ColorEdit(
            ColorSettingsStore.primaryContainerColor,
            stringResource(R.string.primary_container_color),
            defaultColorScheme.primaryContainer
        ),
        ColorEdit(
            ColorSettingsStore.onPrimaryContainerColor,
            stringResource(R.string.on_primary_container_color),
            defaultColorScheme.onPrimaryContainer
        ),
        ColorEdit(
            ColorSettingsStore.inversePrimaryColor,
            stringResource(R.string.inverse_primary_color),
            defaultColorScheme.inversePrimary
        )
    )
    val primarySectionTitle = stringResource(R.string.primary_colors_section)

    val secondaryColors = listOf(
        ColorEdit(
            ColorSettingsStore.secondaryColor,
            stringResource(R.string.secondary_color),
            defaultColorScheme.secondary
        ),
        ColorEdit(
            ColorSettingsStore.onSecondaryColor,
            stringResource(R.string.on_secondary_color),
            defaultColorScheme.onSecondary
        ),
        ColorEdit(
            ColorSettingsStore.secondaryContainerColor,
            stringResource(R.string.secondary_container_color),
            defaultColorScheme.secondaryContainer
        ),
        ColorEdit(
            ColorSettingsStore.onSecondaryContainerColor,
            stringResource(R.string.on_secondary_container_color),
            defaultColorScheme.onSecondaryContainer
        )
    )
    val secondarySectionTitle = stringResource(R.string.secondary_colors_section)

    val tertiaryColors = listOf(
        ColorEdit(
            ColorSettingsStore.tertiaryColor,
            stringResource(R.string.tertiary_color),
            defaultColorScheme.tertiary
        ),
        ColorEdit(
            ColorSettingsStore.onTertiaryColor,
            stringResource(R.string.on_tertiary_color),
            defaultColorScheme.onTertiary
        ),
        ColorEdit(
            ColorSettingsStore.tertiaryContainerColor,
            stringResource(R.string.tertiary_container_color),
            defaultColorScheme.tertiaryContainer
        ),
        ColorEdit(
            ColorSettingsStore.onTertiaryContainerColor,
            stringResource(R.string.on_tertiary_container_color),
            defaultColorScheme.onTertiaryContainer
        )
    )
    val tertiarySectionTitle = stringResource(R.string.tertiary_colors_section)

    val backgroundColors = listOf(
        ColorEdit(
            ColorSettingsStore.backgroundColor,
            stringResource(R.string.background_color),
            defaultColorScheme.background
        ),
        ColorEdit(
            ColorSettingsStore.onBackgroundColor,
            stringResource(R.string.on_background_color),
            defaultColorScheme.onBackground
        ),
        ColorEdit(ColorSettingsStore.surfaceColor, stringResource(R.string.surface_color), defaultColorScheme.surface),
        ColorEdit(
            ColorSettingsStore.onSurfaceColor,
            stringResource(R.string.on_surface_color),
            defaultColorScheme.onSurface
        ),
        ColorEdit(
            ColorSettingsStore.surfaceVariantColor,
            stringResource(R.string.surface_variant_color),
            defaultColorScheme.surfaceVariant
        ),
        ColorEdit(
            ColorSettingsStore.onSurfaceVariantColor,
            stringResource(R.string.on_surface_variant_color),
            defaultColorScheme.onSurfaceVariant
        ),
        ColorEdit(
            ColorSettingsStore.surfaceTintColor,
            stringResource(R.string.surface_tint_color),
            defaultColorScheme.surfaceTint
        ),
        ColorEdit(
            ColorSettingsStore.inverseSurfaceColor,
            stringResource(R.string.inverse_surface_color),
            defaultColorScheme.inverseSurface
        ),
        ColorEdit(
            ColorSettingsStore.inverseOnSurfaceColor,
            stringResource(R.string.inverse_on_surface_color),
            defaultColorScheme.inverseOnSurface
        )
    )
    val backgroundSectionTitle = stringResource(R.string.background_surface_colors_section)

    val errorColors = listOf(
        ColorEdit(ColorSettingsStore.errorColor, stringResource(R.string.error_color), defaultColorScheme.error),
        ColorEdit(ColorSettingsStore.onErrorColor, stringResource(R.string.on_error_color), defaultColorScheme.onError),
        ColorEdit(
            ColorSettingsStore.errorContainerColor,
            stringResource(R.string.error_container_color),
            defaultColorScheme.errorContainer
        ),
        ColorEdit(
            ColorSettingsStore.onErrorContainerColor,
            stringResource(R.string.on_error_container_color),
            defaultColorScheme.onErrorContainer
        )
    )
    val errorSectionTitle = stringResource(R.string.error_colors_section)

    val outlineColors = listOf(
        ColorEdit(ColorSettingsStore.outlineColor, stringResource(R.string.outline_color), defaultColorScheme.outline),
        ColorEdit(
            ColorSettingsStore.outlineVariantColor,
            stringResource(R.string.outline_variant_color),
            defaultColorScheme.outlineVariant
        ),
        ColorEdit(ColorSettingsStore.scrimColor, stringResource(R.string.scrim_color), defaultColorScheme.scrim)
    )
    val outlineSectionTitle = stringResource(R.string.outline_colors_section)

    val surfaceContainerColors = listOf(
        ColorEdit(
            ColorSettingsStore.surfaceBrightColor,
            stringResource(R.string.surface_bright_color),
            defaultColorScheme.surfaceBright
        ),
        ColorEdit(
            ColorSettingsStore.surfaceContainerColor,
            stringResource(R.string.surface_container_color),
            defaultColorScheme.surfaceContainer
        ),
        ColorEdit(
            ColorSettingsStore.surfaceContainerHighColor,
            stringResource(R.string.surface_container_high_color),
            defaultColorScheme.surfaceContainerHigh
        ),
        ColorEdit(
            ColorSettingsStore.surfaceContainerHighestColor,
            stringResource(R.string.surface_container_highest_color),
            defaultColorScheme.surfaceContainerHighest
        ),
        ColorEdit(
            ColorSettingsStore.surfaceContainerLowColor,
            stringResource(R.string.surface_container_low_color),
            defaultColorScheme.surfaceContainerLow
        ),
        ColorEdit(
            ColorSettingsStore.surfaceContainerLowestColor,
            stringResource(R.string.surface_container_lowest_color),
            defaultColorScheme.surfaceContainerLowest
        ),
        ColorEdit(
            ColorSettingsStore.surfaceDimColor,
            stringResource(R.string.surface_dim_color),
            defaultColorScheme.surfaceDim
        )
    )
    val surfaceContainerSectionTitle = stringResource(R.string.surface_container_colors_section)

    val fixedColors = listOf(
        ColorEdit(
            ColorSettingsStore.primaryFixedColor,
            stringResource(R.string.primary_fixed_color),
            defaultColorScheme.primaryFixed
        ),
        ColorEdit(
            ColorSettingsStore.primaryFixedDimColor,
            stringResource(R.string.primary_fixed_dim_color),
            defaultColorScheme.primaryFixedDim
        ),
        ColorEdit(
            ColorSettingsStore.onPrimaryFixedColor,
            stringResource(R.string.on_primary_fixed_color),
            defaultColorScheme.onPrimaryFixed
        ),
        ColorEdit(
            ColorSettingsStore.onPrimaryFixedVariantColor,
            stringResource(R.string.on_primary_fixed_variant_color),
            defaultColorScheme.onPrimaryFixedVariant
        ),
        ColorEdit(
            ColorSettingsStore.secondaryFixedColor,
            stringResource(R.string.secondary_fixed_color),
            defaultColorScheme.secondaryFixed
        ),
        ColorEdit(
            ColorSettingsStore.secondaryFixedDimColor,
            stringResource(R.string.secondary_fixed_dim_color),
            defaultColorScheme.secondaryFixedDim
        ),
        ColorEdit(
            ColorSettingsStore.onSecondaryFixedColor,
            stringResource(R.string.on_secondary_fixed_color),
            defaultColorScheme.onSecondaryFixed
        ),
        ColorEdit(
            ColorSettingsStore.onSecondaryFixedVariantColor,
            stringResource(R.string.on_secondary_fixed_variant_color),
            defaultColorScheme.onSecondaryFixedVariant
        ),
        ColorEdit(
            ColorSettingsStore.tertiaryFixedColor,
            stringResource(R.string.tertiary_fixed_color),
            defaultColorScheme.tertiaryFixed
        ),
        ColorEdit(
            ColorSettingsStore.tertiaryFixedDimColor,
            stringResource(R.string.tertiary_fixed_dim_color),
            defaultColorScheme.tertiaryFixedDim
        ),
        ColorEdit(
            ColorSettingsStore.onTertiaryFixedColor,
            stringResource(R.string.on_tertiary_fixed_color),
            defaultColorScheme.onTertiaryFixed
        ),
        ColorEdit(
            ColorSettingsStore.onTertiaryFixedVariantColor,
            stringResource(R.string.on_tertiary_fixed_variant_color),
            defaultColorScheme.onTertiaryFixedVariant
        )
    )
    val fixedSectionTitle = stringResource(R.string.fixed_colors_section)


    val primarySectionState =
        rememberExpandableSection(primarySectionTitle)

    val secondarySectionState =
        rememberExpandableSection(secondarySectionTitle)

    val tertiarySectionState =
        rememberExpandableSection(tertiarySectionTitle)

    val backgroundSectionState =
        rememberExpandableSection(backgroundSectionTitle)

    val errorSectionState =
        rememberExpandableSection(errorSectionTitle)

    val outlineSectionState =
        rememberExpandableSection(outlineSectionTitle)

    val surfaceContainerSectionState =
        rememberExpandableSection(surfaceContainerSectionTitle)

    val fixedSectionState =
        rememberExpandableSection(fixedSectionTitle)


    val angleLineColor by ColorSettingsStore.angleLineColor.asStateNull()
    val circleColor by ColorSettingsStore.circleColor.asStateNull()

    val launchAppColor by ColorSettingsStore.launchAppColor.asStateNull()
    val openUrlColor by ColorSettingsStore.openUrlColor.asStateNull()
    val notificationShadeColor by ColorSettingsStore.notificationShadeColor.asStateNull()
    val controlPanelColor by ColorSettingsStore.controlPanelColor.asStateNull()
    val openAppDrawerColor by ColorSettingsStore.openAppDrawerColor.asStateNull()
    val launcherSettingsColor by ColorSettingsStore.launcherSettingsColor.asStateNull()
    val lockColor by ColorSettingsStore.lockColor.asStateNull()
    val openFileColor by ColorSettingsStore.openFileColor.asStateNull()
    val reloadColor by ColorSettingsStore.reloadColor.asStateNull()
    val openRecentAppsColor by ColorSettingsStore.openRecentAppsColor.asStateNull()
    val openCircleNest by ColorSettingsStore.openCircleNestColor.asStateNull()
    val goParentCircle by ColorSettingsStore.goParentNestColor.asStateNull()
    val toggleBluetooth by ColorSettingsStore.toggleBluetooth.asStateNull()
    val toggleData by ColorSettingsStore.toggleData.asStateNull()
    val toggleWifi by ColorSettingsStore.toggleWifi.asStateNull()
    val runAdbCommand by ColorSettingsStore.runAdbCommand.asStateNull()

    val selectedDefaultTheme by ColorModesSettingsStore.defaultTheme.asState()


    var showResetValidation by remember { mutableStateOf(false) }

    var showBurgerMenu by remember { mutableStateOf(false) }

    var selectedCustomView by remember { mutableStateOf(ColorSelectorModes.NORMAL) }

    var showRandomColorsValidation by remember { mutableStateOf(false) }
    var showAllColorsValidation by remember { mutableStateOf(false) }

    val colorTestMode by ColorModesSettingsStore.colorTestMode.asState()

    var showExitTestValidation by remember { mutableStateOf(false) }

    SettingsScaffold(
        title = stringResource(R.string.color_selector),
        onBack = onBack,
        helpText = stringResource(R.string.color_selector_text),
        onReset = {
            scope.launch {
                ColorSettingsStore.resetAll(ctx)
                ColorModesSettingsStore.resetAll(ctx)
            }
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max)
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = DragonShape
                )
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            DefaultThemes.entries.filter { it != AMOLED }.forEach {
                val selected = it == defaultTheme || (it == DARK && defaultTheme == AMOLED)

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(DragonShape)
                        .conditional(selected) {
                            background(MaterialTheme.colorScheme.surfaceDim)
                        }
                        .clickable {
                            scope.launch {
                                ColorModesSettingsStore.defaultTheme.set(ctx, it)
                            }
                        }
                        .padding(5.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    val background = when (it) {
                        AMOLED -> null

                        DARK -> Color.DarkGray
                        LIGHT -> Color.White
                        SYSTEM -> Brush.horizontalGradient(
                            colors = listOf(
                                Color.White,
                                Color.Black
                            )
                        )

                        CUSTOM -> Brush.linearGradient(
                            colors = listOf(
                                Color.Red,
                                Color.Yellow,
                                Color.Green,
                                Color.Cyan,
                                Color.Blue,
                                Color.Magenta
                            )
                        )
                    }


                    // I like this simple animation I made, I think I've changed my mind about animations
                    val shapeCorners by animateIntAsState(
                        targetValue = if (selected) 12 else 50,
                        animationSpec = bouncySpec(),
                        label = "box_shape"
                    )

                    val boxShape = RoundedCornerShape(shapeCorners)

                    if (background != null) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(boxShape)
                                .then(
                                    when (background) {
                                        is Color -> Modifier.background(background)
                                        is Brush -> Modifier.background(background)
                                        else -> Modifier
                                    }
                                )
                                .border(
                                    1.dp,
                                    MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                    boxShape
                                )
                        )
                    }


                    Spacer(5.dp)

                    Text(
                        text = stringResource(it.resId),
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.labelSmall,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        HorizontalDivider()

        SettingsSwitchRow(
            setting = UiSettingsStore.useCustomColorChannels,
            title = stringResource(R.string.use_custom_color_channels),
            description = stringResource(R.string.use_custom_color_channels_desc)
        )

        AnimatedVisibility(selectedDefaultTheme == DARK || selectedDefaultTheme == AMOLED) {
            SwitchRow(
                state = selectedDefaultTheme == AMOLED,
                title = stringResource(R.string.amoled_theme),
                description = stringResource(R.string.use_pure_black_background)
            ) {
                scope.launch {
                    ColorModesSettingsStore.defaultTheme.set(ctx, if (it) AMOLED else DARK)
                }
            }
        }

        // Only show the dynamic colors switch when in SYSTEM view
        AnimatedVisibility(selectedDefaultTheme == SYSTEM) {
            SettingsSwitchRow(
                setting = ColorModesSettingsStore.dynamicColor,
                title = stringResource(R.string.dynamic_colors),
                description = stringResource(R.string.dynamic_colors_desc)
            )
        }

        AnimatedVisibility(colorTestMode) {
            DragonButton(
                onClick = { showExitTestValidation = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.exit_test_mode))
            }
        }


        AnimatedVisibility(defaultTheme == CUSTOM) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                @Suppress("DEPRECATION")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    SingleSelectConnectedButtonRow(
                        entries = ColorSelectorModes.entries,
                        modifier = Modifier.weight(1f),
                        checked = { it == selectedCustomView }
                    ) { selectedCustomView = it }

                    Box {
                        DragonIconButton(
                            colors = AppObjectsColors.iconButtonColors(),
                            icon = R.drawable.more_vert,
                            contentDescription = stringResource(R.string.open_burger_menu)
                        ) { showBurgerMenu = true }


                        BurgerListAction(
                            actions = listOf(
                                MoreOptions(
                                    onClick = {
                                        showRandomColorsValidation = true
                                        showBurgerMenu = false
                                    },
                                    icon = R.drawable.shuffle,
                                    text = { stringResource(R.string.make_every_colors_random) }
                                ),
                                MoreOptions(
                                    onClick = {
                                        showAllColorsValidation = true
                                        showBurgerMenu = false
                                    },
                                    icon = R.drawable.select_all,
                                    text = { stringResource(R.string.make_all_colors_identical) }
                                ),
                                MoreOptions(
                                    onClick = {
                                        scope.launch {
                                            ColorSettingsStore.backupColors(ctx)
                                            ColorModesSettingsStore.colorTestMode.set(ctx, true)
                                            onBack()
                                        }
                                    },
                                    icon = R.drawable.colorize,
                                    text = { stringResource(R.string.test_colors) }
                                )
                            ),
                            isExpanded = showBurgerMenu,
                            onDismissRequest = { showBurgerMenu = false }
                        )
                    }
                }


                AnimatedContent(selectedCustomView) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        when (it) {
                            ColorSelectorModes.NORMAL -> {

                                ColorsGroup(
                                    expandableSectionState = primarySectionState,
                                    colors = primaryColors
                                )

                                ColorsGroup(
                                    expandableSectionState = secondarySectionState,
                                    colors = secondaryColors
                                )

                                ColorsGroup(
                                    expandableSectionState = tertiarySectionState,
                                    colors = tertiaryColors
                                )

                                ColorsGroup(
                                    expandableSectionState = backgroundSectionState,
                                    colors = backgroundColors
                                )

                                ColorsGroup(
                                    expandableSectionState = errorSectionState,
                                    colors = errorColors
                                )

                                ColorsGroup(
                                    expandableSectionState = outlineSectionState,
                                    colors = outlineColors
                                )

                                ColorsGroup(
                                    expandableSectionState = surfaceContainerSectionState,
                                    colors = surfaceContainerColors
                                )

                                ColorsGroup(
                                    expandableSectionState = fixedSectionState,
                                    colors = fixedColors
                                )
                            }


                            ColorSelectorModes.CUSTOM -> {


                                SettingsColorPicker(
                                    settingObject = ColorSettingsStore.angleLineColor,
                                    defaultColor = angleLineColor.definedOrNull() ?: DefaultExtraColors.angleLine,
                                    label = stringResource(R.string.angle_line_color)
                                )

                                SettingsColorPicker(
                                    settingObject = ColorSettingsStore.circleColor,
                                    defaultColor = circleColor.definedOrNull() ?: DefaultExtraColors.circle,
                                    label = stringResource(R.string.circle_color)
                                )

                                SettingsColorPicker(
                                    settingObject = ColorSettingsStore.launchAppColor,
                                    defaultColor = launchAppColor.definedOrNull() ?: DefaultExtraColors.launchApp,
                                    label = stringResource(R.string.launch_app_color)
                                )

                                SettingsColorPicker(
                                    settingObject = ColorSettingsStore.openUrlColor,
                                    defaultColor = openUrlColor.definedOrNull() ?: DefaultExtraColors.openUrl,
                                    label = stringResource(R.string.open_url_color)
                                )

                                SettingsColorPicker(
                                    settingObject = ColorSettingsStore.notificationShadeColor,
                                    defaultColor = notificationShadeColor.definedOrNull() ?: DefaultExtraColors.notificationShade,
                                    label = stringResource(R.string.notification_shade_color)
                                )

                                SettingsColorPicker(
                                    settingObject = ColorSettingsStore.controlPanelColor,
                                    defaultColor = controlPanelColor.definedOrNull() ?: DefaultExtraColors.controlPanel,
                                    label = stringResource(R.string.control_panel_color)
                                )

                                SettingsColorPicker(
                                    settingObject = ColorSettingsStore.openAppDrawerColor,
                                    defaultColor = openAppDrawerColor.definedOrNull() ?: DefaultExtraColors.openAppDrawer,
                                    label = stringResource(R.string.open_app_drawer_color)
                                )

                                SettingsColorPicker(
                                    settingObject = ColorSettingsStore.launcherSettingsColor,
                                    defaultColor = launcherSettingsColor.definedOrNull() ?: DefaultExtraColors.launcherSettings,
                                    label = stringResource(R.string.launcher_settings_color)
                                )

                                SettingsColorPicker(
                                    settingObject = ColorSettingsStore.lockColor,
                                    defaultColor = lockColor.definedOrNull() ?: DefaultExtraColors.lock,
                                    label = stringResource(R.string.lock_color)
                                )

                                SettingsColorPicker(
                                    settingObject = ColorSettingsStore.openFileColor,
                                    defaultColor = openFileColor.definedOrNull() ?: DefaultExtraColors.openFile,
                                    label = stringResource(R.string.open_file_color)
                                )

                                SettingsColorPicker(
                                    settingObject = ColorSettingsStore.reloadColor,
                                    defaultColor = reloadColor.definedOrNull() ?: DefaultExtraColors.reload,
                                    label = stringResource(R.string.reload_color)
                                )

                                SettingsColorPicker(
                                    settingObject = ColorSettingsStore.openRecentAppsColor,
                                    defaultColor = openRecentAppsColor.definedOrNull() ?: DefaultExtraColors.openRecentApps,
                                    label = stringResource(R.string.open_recent_apps_color)
                                )

                                SettingsColorPicker(
                                    settingObject = ColorSettingsStore.openCircleNestColor,
                                    defaultColor = openCircleNest.definedOrNull() ?: DefaultExtraColors.openCircleNest,
                                    label = stringResource(R.string.open_circle_nest_color)
                                )

                                SettingsColorPicker(
                                    settingObject = ColorSettingsStore.goParentNestColor,
                                    defaultColor = goParentCircle.definedOrNull() ?: DefaultExtraColors.goParentNest,
                                    label = stringResource(R.string.go_parent_nest_color)
                                )

                                SettingsColorPicker(
                                    settingObject = ColorSettingsStore.toggleWifi,
                                    defaultColor = toggleWifi.definedOrNull() ?: DefaultExtraColors.toggleWifi,
                                    label = stringResource(R.string.toggle_wifi)
                                )

                                SettingsColorPicker(
                                    settingObject = ColorSettingsStore.toggleBluetooth,
                                    defaultColor = toggleBluetooth.definedOrNull() ?: DefaultExtraColors.toggleBluetooth,
                                    label = stringResource(R.string.toggle_bluetooth)
                                )


                                SettingsColorPicker(
                                    settingObject = ColorSettingsStore.toggleData,
                                    defaultColor = toggleData.definedOrNull() ?: DefaultExtraColors.toggleData,
                                    label = stringResource(R.string.toggle_mobile_data)
                                )


                                SettingsColorPicker(
                                    settingObject = ColorSettingsStore.runAdbCommand,
                                    defaultColor = runAdbCommand.definedOrNull() ?: DefaultExtraColors.runAdbCommand,
                                    label = stringResource(R.string.run_adb_command)
                                )
                            }
                        }
                    }
                }
            }
        }
    }


    if (showResetValidation) {
        UserValidation(
            title = stringResource(R.string.reset_to_default_colors),
            message = stringResource(R.string.reset_to_default_colors_explanation),
            onDismiss = { showResetValidation = false }
        ) {
            scope.launch {
                ColorSettingsStore.resetAll(ctx)
                showResetValidation = false
            }
        }
    }
    if (showRandomColorsValidation) {
        UserValidation(
            title = stringResource(R.string.make_every_colors_random),
            message = stringResource(R.string.make_every_colors_random_explanation),
            onDismiss = { showRandomColorsValidation = false }
        ) {
            scope.launch {
                ColorSettingsStore.setAllRandomColors(ctx)
                showRandomColorsValidation = false
            }
        }
    }


    if (showAllColorsValidation) {
        var applyColor by remember { mutableStateOf(Color.Black) }
        AlertDialog(
            onDismissRequest = { showAllColorsValidation = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            ColorSettingsStore.setAllSameColors(ctx, applyColor)
                            showAllColorsValidation = false
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(CircleShape)
                        .padding(5.dp)
                ) {
                    Text(
                        text = stringResource(R.string.apply),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            title = {
                ColorPickerRow(
                    currentColor = applyColor,
                    label = stringResource(R.string.color_mode_all),
                    backgroundColor = MaterialTheme.colorScheme.surface.alphaMultiplier(0.7f)
                ) { applyColor = it ?: Color.Black }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = DragonShape
        )
    }

    if (showExitTestValidation) {
        UserValidation(
            title = stringResource(R.string.exit_test_mode),
            message = stringResource(R.string.exit_test_mode_message),
            validateText = stringResource(R.string.test_mode_validate),
            cancelText = stringResource(R.string.test_mode_cancel),
            properties = DialogProperties(
                dismissOnClickOutside = false
            ),
            onDismiss = {
                scope.launch {
                    ColorSettingsStore.restoreColors(ctx)
                    ColorModesSettingsStore.colorTestMode.set(ctx, false)
                    showExitTestValidation = false
                }
            },
            onValidate = {
                scope.launch {
                    ColorModesSettingsStore.colorTestMode.set(ctx, false)
                    showExitTestValidation = false
                }
            }
        )
    }
}

private data class ColorEdit(
    val setting: BaseSettingObject<Color, String>,
    val label: String,
    val defaultColor: Color
)


@Composable
private fun ColorsGroup(
    expandableSectionState: ExpandableSectionState,
    colors: List<ColorEdit>,
    examples: @Composable (ColumnScope.() -> Unit)? = null
) {
    ExpandableSection(expandableSectionState) {
        examples?.let { it() }
        colors.forEach {
            SettingsColorPicker(
                settingObject = it.setting,
                defaultColor = it.defaultColor,
                label = it.label
            )
        }
    }
}

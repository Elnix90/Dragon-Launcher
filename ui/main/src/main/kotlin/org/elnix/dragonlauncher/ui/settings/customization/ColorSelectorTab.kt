package org.elnix.dragonlauncher.ui.settings.customization


import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import io.github.elnix90.core.objects.ColorSettingObject
import io.github.elnix90.runtime.asState
import io.github.elnix90.runtime.asStateNull
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.base.model.enumsui.select.ColorSelectorModes
import org.elnix.dragonlauncher.base.model.enumsui.toggle.DefaultThemes
import org.elnix.dragonlauncher.base.model.enumsui.toggle.DefaultThemes.Amoled
import org.elnix.dragonlauncher.base.model.enumsui.toggle.DefaultThemes.Custom
import org.elnix.dragonlauncher.base.model.enumsui.toggle.DefaultThemes.Dark
import org.elnix.dragonlauncher.base.model.enumsui.toggle.DefaultThemes.Light
import org.elnix.dragonlauncher.base.model.enumsui.toggle.DefaultThemes.System
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.settings.stores.map.ColorModesSettingsStore
import org.elnix.dragonlauncher.settings.stores.map.ColorSettingsStore
import org.elnix.dragonlauncher.ui.base.animation.bouncySpec
import org.elnix.dragonlauncher.ui.base.components.AnimatedFab
import org.elnix.dragonlauncher.ui.base.components.Spacer
import org.elnix.dragonlauncher.ui.base.modifiers.conditional
import org.elnix.dragonlauncher.ui.components.burger.BurgerListAction
import org.elnix.dragonlauncher.ui.components.burger.MoreOptions
import org.elnix.dragonlauncher.ui.compositionslocals.LocalNavigator
import org.elnix.dragonlauncher.ui.dragon.colors.ColorPickerRow
import org.elnix.dragonlauncher.ui.dragon.components.DragonButton
import org.elnix.dragonlauncher.ui.dragon.components.DragonGroupScope
import org.elnix.dragonlauncher.ui.dragon.components.DragonSettingsGroup
import org.elnix.dragonlauncher.ui.dragon.components.SwitchRow
import org.elnix.dragonlauncher.ui.dragon.components.ValidateCancelButtons
import org.elnix.dragonlauncher.ui.dragon.dialogs.UserValidation
import org.elnix.dragonlauncher.ui.dragon.expandable.ExpandableSection
import org.elnix.dragonlauncher.ui.dragon.expandable.rememberExpandableSection
import org.elnix.dragonlauncher.ui.dragon.generic.SingleSelectConnectedButtonRow
import org.elnix.dragonlauncher.ui.dragon.model.ExpandableSectionState
import org.elnix.dragonlauncher.ui.dragon.settings.Setting
import org.elnix.dragonlauncher.ui.helpers.settings.SettingsScaffold

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ColorSelectorTab() {
    val ctx = LocalContext.current
    val navigator = LocalNavigator.current
    val scope = rememberCoroutineScope()

    val defaultTheme by ColorModesSettingsStore.defaultTheme.asStateNull()
    val colorTestMode by ColorModesSettingsStore.colorTestMode.asState()

    val primarySectionState = rememberExpandableSection(
        title = R.string.primary_colors_section,
        description = R.string.primary_colors_section_desc,
        customLeadingContent = {
            with(MaterialTheme.colorScheme) {
                PalettePreview(primary, onPrimary, primaryContainer, onPrimaryContainer)
            }
        },
        icon = null
    )

    val secondarySectionState = rememberExpandableSection(
        title = R.string.secondary_colors_section,
        description = R.string.secondary_colors_section_desc,
        customLeadingContent = {
            with(MaterialTheme.colorScheme) {
                PalettePreview(secondary, onSecondary, secondaryContainer, onSecondaryContainer)
            }
        },
        icon = null
    )

    val tertiarySectionState = rememberExpandableSection(
        title = R.string.tertiary_colors_section,
        description = R.string.tertiary_colors_section_desc,
        customLeadingContent = {
            with(MaterialTheme.colorScheme) {
                PalettePreview(tertiary, onTertiary, tertiaryContainer, onTertiaryContainer)
            }
        },
        icon = null
    )

    val backgroundSectionState = rememberExpandableSection(
        title = R.string.background_surface_colors_section,
        description = R.string.background_surface_colors_section_desc,
        customLeadingContent = {
            with(MaterialTheme.colorScheme) {
                PalettePreview(background, onBackground, surface, onSecondary)
            }
        },
        icon = null
    )
    val errorSectionState = rememberExpandableSection(
        title = R.string.error_colors_section,
        description = R.string.error_colors_section_desc,
        customLeadingContent = {
            with(MaterialTheme.colorScheme) {
                PalettePreview(error, onError, errorContainer, onErrorContainer)
            }
        },
        icon = null
    )
    val outlineSectionState = rememberExpandableSection(
        title = R.string.outline_colors_section,
        description = R.string.outline_colors_section_desc,
        customLeadingContent = {
            with(MaterialTheme.colorScheme) {
                PalettePreview(outline, outlineVariant, scrim, Color.Transparent)
            }
        },
        icon = null
    )
    val surfaceContainerSectionState = rememberExpandableSection(
        title = R.string.surface_container_colors_section,
        description = R.string.surface_container_colors_section_desc,
        customLeadingContent = {
            with(MaterialTheme.colorScheme) {
                PalettePreview(surfaceContainer, surfaceContainerHigh, surfaceContainerHighest, surfaceContainerLow)
            }
        },
        icon = null
    )

    var showResetValidation by remember { mutableStateOf(false) }
    var showBurgerMenu by remember { mutableStateOf(false) }
    var selectedCustomView by remember { mutableStateOf(ColorSelectorModes.Normal) }
    var showRandomColorsValidation by remember { mutableStateOf(false) }
    var showAllColorsValidation by remember { mutableStateOf(false) }
    var showExitTestValidation by remember { mutableStateOf(false) }

    SettingsScaffold(
        title = stringResource(R.string.color_selector),
        helpText = stringResource(R.string.color_selector_text),
        resetText = stringResource(R.string.reset_colors_tab),
        onReset = {
            scope.launch {
                ColorSettingsStore.resetAll(ctx)
                ColorModesSettingsStore.resetAll(ctx)
            }
        },
        specialSettingsTitleContent = {
            Box {
                AnimatedFab(
                    icon = R.drawable.more_vert,
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
                                    navigator.onBack()
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
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max)
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = MaterialTheme.shapes.large
                )
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            DefaultThemes.entries.filter { it != Amoled }.forEach {
                val selected = it == defaultTheme || (it == Dark && defaultTheme == Amoled)

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(MaterialTheme.shapes.large)
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
                        Amoled -> null

                        Dark -> Color.DarkGray
                        Light -> Color.White
                        System -> Brush.horizontalGradient(
                            colors = listOf(
                                Color.White,
                                Color.Black
                            )
                        )

                        Custom -> Brush.linearGradient(
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
                        animationSpec = bouncySpec()
                    )

                    val scale by animateFloatAsState(
                        targetValue = if (selected) 1.2f else 1f,
                        animationSpec = bouncySpec()
                    )

                    val boxShape = RoundedCornerShape(shapeCorners)

                    if (background != null) {
                        Box((Modifier.scale(scale))) {
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
                                        1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), boxShape
                                    )
                            )
                        }
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

        DragonSettingsGroup(R.string.special_options) {
            Setting(ColorModesSettingsStore.useCustomColorChannels)

            AnimatedVisibility(defaultTheme == Dark || defaultTheme == Amoled) {
                SwitchRow(
                    state = defaultTheme == Amoled,
                    title = R.string.amoled_theme,
                    description = R.string.use_pure_black_background,
                    icon = R.drawable.opacity
                ) {
                    scope.launch {
                        ColorModesSettingsStore.defaultTheme.set(ctx, if (it) Amoled else Dark)
                    }
                }
            }

            // Only show the dynamic colors switch when in SYSTEM view
            AnimatedVisibility(defaultTheme == System) {
                Setting(ColorModesSettingsStore.dynamicColors)
            }

            AnimatedVisibility(colorTestMode) {
                DragonButton(onClick = { showExitTestValidation = true }) {
                    Text(stringResource(R.string.exit_test_mode))
                }
            }
        }

        AnimatedVisibility(defaultTheme == Custom) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                SingleSelectConnectedButtonRow(
                    entries = ColorSelectorModes.entries,
                    modifier = Modifier.fillMaxWidth(),
                    checked = { it == selectedCustomView }
                ) { selectedCustomView = it }


                AnimatedContent(selectedCustomView) {
                    DragonSettingsGroup {
                        when (it) {
                            ColorSelectorModes.Normal -> {
                                ColorsGroup(
                                    expandableSectionState = primarySectionState,
                                    colors = listOf(
                                        ColorSettingsStore.primaryColor,
                                        ColorSettingsStore.onPrimaryColor,
                                        ColorSettingsStore.primaryContainerColor,
                                        ColorSettingsStore.onPrimaryContainerColor,
                                        ColorSettingsStore.inversePrimaryColor
                                    )
                                )

                                ColorsGroup(
                                    expandableSectionState = secondarySectionState,
                                    colors = listOf(
                                        ColorSettingsStore.secondaryColor,
                                        ColorSettingsStore.onSecondaryColor,
                                        ColorSettingsStore.secondaryContainerColor,
                                        ColorSettingsStore.onSecondaryContainerColor
                                    )
                                )

                                ColorsGroup(
                                    expandableSectionState = tertiarySectionState,
                                    colors = listOf(
                                        ColorSettingsStore.tertiaryColor,
                                        ColorSettingsStore.onTertiaryColor,
                                        ColorSettingsStore.tertiaryContainerColor,
                                        ColorSettingsStore.onTertiaryContainerColor
                                    )

                                )

                                ColorsGroup(
                                    expandableSectionState = backgroundSectionState,
                                    colors = listOf(
                                        ColorSettingsStore.backgroundColor,
                                        ColorSettingsStore.onBackgroundColor,
                                        ColorSettingsStore.surfaceColor,
                                        ColorSettingsStore.onSurfaceColor,
                                        ColorSettingsStore.surfaceVariantColor,
                                        ColorSettingsStore.onSurfaceVariantColor,
                                        ColorSettingsStore.surfaceTintColor,
                                        ColorSettingsStore.inverseSurfaceColor,
                                        ColorSettingsStore.inverseOnSurfaceColor
                                    )
                                )

                                ColorsGroup(
                                    expandableSectionState = errorSectionState,
                                    colors = listOf(
                                        ColorSettingsStore.errorColor,
                                        ColorSettingsStore.onErrorColor,
                                        ColorSettingsStore.errorContainerColor,
                                        ColorSettingsStore.onErrorContainerColor
                                    )
                                )

                                ColorsGroup(
                                    expandableSectionState = outlineSectionState,
                                    colors = listOf(
                                        ColorSettingsStore.outlineColor,
                                        ColorSettingsStore.outlineVariantColor,
                                        ColorSettingsStore.scrimColor
                                    )
                                )

                                ColorsGroup(
                                    expandableSectionState = surfaceContainerSectionState,
                                    colors = listOf(
                                        ColorSettingsStore.surfaceBrightColor,
                                        ColorSettingsStore.surfaceContainerColor,
                                        ColorSettingsStore.surfaceContainerHighColor,
                                        ColorSettingsStore.surfaceContainerHighestColor,
                                        ColorSettingsStore.surfaceContainerLowColor,
                                        ColorSettingsStore.surfaceContainerLowestColor,
                                        ColorSettingsStore.surfaceDimColor
                                    )
                                )


                                // Removed the fixed colors as I don't use them personally, but I may add them in the future
//                                ColorsGroup(
//                                    expandableSectionState = fixedSectionState,
//                                    colors = listOf(
//                                        ColorSettingsStore.primaryFixedColor,
//                                        ColorSettingsStore.primaryFixedDimColor,
//                                        ColorSettingsStore.onPrimaryFixedColor,
//                                        ColorSettingsStore.onPrimaryFixedVariantColor,
//                                        ColorSettingsStore.secondaryFixedColor,
//                                        ColorSettingsStore.secondaryFixedDimColor,
//                                        ColorSettingsStore.onSecondaryFixedColor,
//                                        ColorSettingsStore.onSecondaryFixedVariantColor,
//                                        ColorSettingsStore.tertiaryFixedColor,
//                                        ColorSettingsStore.tertiaryFixedDimColor,
//                                        ColorSettingsStore.onTertiaryFixedColor,
//                                        ColorSettingsStore.onTertiaryFixedVariantColor
//                                    )
//                                )
                            }

                            ColorSelectorModes.Custom -> {
                                Setting(ColorSettingsStore.holdToActivateColor)
                                Setting(ColorSettingsStore.angleLineColor)
                                Setting(ColorSettingsStore.shapesColor)
                                Setting(ColorSettingsStore.launchAppColor)
                                Setting(ColorSettingsStore.openUrlColor)
                                Setting(ColorSettingsStore.notificationShadeColor)
                                Setting(ColorSettingsStore.controlPanelColor)
                                Setting(ColorSettingsStore.openAppDrawerColor)
                                Setting(ColorSettingsStore.launcherSettingsColor)
                                Setting(ColorSettingsStore.lockColor)
                                Setting(ColorSettingsStore.openFileColor)
                                Setting(ColorSettingsStore.reloadColor)
                                Setting(ColorSettingsStore.openRecentAppsColor)
                                Setting(ColorSettingsStore.openCircleNestColor)
                                Setting(ColorSettingsStore.goParentNestColor)
                                Setting(ColorSettingsStore.toggleWifi)
                                Setting(ColorSettingsStore.toggleBluetooth)
                                Setting(ColorSettingsStore.toggleData)
                                Setting(ColorSettingsStore.runAdbCommand)
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
                ValidateCancelButtons(
                    validateText = stringResource(R.string.apply),
                    onCancel = { showAllColorsValidation = false }
                ) {
                    scope.launch {
                        ColorSettingsStore.setAllSameColors(ctx, applyColor)
                        showAllColorsValidation = false
                    }
                }
            },
            title = {
                DragonSettingsGroup {
                    ColorPickerRow(
                        description = null,
                        title = stringResource(R.string.color_mode_all),
                        currentColor = applyColor,
                        defaultColor = null
                    ) {
                        if (it != null) applyColor = it
                    }
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = MaterialTheme.shapes.large
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


@Composable
private fun DragonGroupScope.ColorsGroup(
    expandableSectionState: ExpandableSectionState,
    colors: List<ColorSettingObject>,
    examples: @Composable (ColumnScope.() -> Unit)? = null
) {
    ExpandableSection(expandableSectionState) {
        examples?.let { it() }
        DragonSettingsGroup(expandableSectionState.title) {
            colors.forEach {
                this@ColorsGroup.Setting(it)
            }
        }
    }
}


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun PalettePreview(
    color1: Color,
    color2: Color,
    color3: Color,
    color4: Color
) {
    Box(
        modifier = Modifier
            .size(50.dp)
            .clip(MaterialShapes.Clover4Leaf.toShape())
    ) {
        Column {
            Row(Modifier.weight(1f)) {
                PaletteColor(color1)
                PaletteColor(color2)
            }
            Row(Modifier.weight(1f)) {
                PaletteColor(color3)
                PaletteColor(color4)
            }
        }
    }
}

@Composable
private fun RowScope.PaletteColor(color: Color) {
    Box(
        modifier = Modifier
            .weight(1f)
            .fillMaxSize()
            .background(color)
    )
}
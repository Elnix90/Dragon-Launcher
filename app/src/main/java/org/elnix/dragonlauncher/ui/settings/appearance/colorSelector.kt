package org.elnix.dragonlauncher.ui.settings.appearance


import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AllInclusive
import androidx.compose.material.icons.filled.InvertColors
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.R
import org.elnix.dragonlauncher.data.ColorCustomisationMode
import org.elnix.dragonlauncher.data.DefaultThemes
import org.elnix.dragonlauncher.data.applyDefaultThemeColors
import org.elnix.dragonlauncher.data.colorCustomizationModeName
import org.elnix.dragonlauncher.data.defaultThemeName
import org.elnix.dragonlauncher.data.stores.ColorModesSettingsStore
import org.elnix.dragonlauncher.data.stores.ColorSettingsStore
import org.elnix.dragonlauncher.ui.colors.ColorPickerRow
import org.elnix.dragonlauncher.ui.helpers.TextDivider
import org.elnix.dragonlauncher.ui.helpers.UserValidation
import org.elnix.dragonlauncher.ui.helpers.settings.SettingsLazyHeader
import org.elnix.dragonlauncher.ui.theme.AmoledDefault
import org.elnix.dragonlauncher.ui.theme.LocalExtraColors
import org.elnix.dragonlauncher.utils.colors.AppObjectsColors
import org.elnix.dragonlauncher.utils.colors.adjustBrightness
import org.elnix.dragonlauncher.utils.colors.blendWith

@Suppress("AssignedValueIsNeverRead")
@Composable
fun ColorSelectorTab(
    onBack: (() -> Unit)
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    // === Collect all theme color states ===
    val primary by ColorSettingsStore.getPrimary(ctx).collectAsState(initial = null)
    val onPrimary by ColorSettingsStore.getOnPrimary(ctx).collectAsState(initial = null)

    val secondary by ColorSettingsStore.getSecondary(ctx).collectAsState(initial = null)
    val onSecondary by ColorSettingsStore.getOnSecondary(ctx).collectAsState(initial = null)

    val tertiary by ColorSettingsStore.getTertiary(ctx).collectAsState(initial = null)
    val onTertiary by ColorSettingsStore.getOnTertiary(ctx).collectAsState(initial = null)

    val background by ColorSettingsStore.getBackground(ctx).collectAsState(initial = null)
    val onBackground by ColorSettingsStore.getOnBackground(ctx).collectAsState(initial = null)

    val surface by ColorSettingsStore.getSurface(ctx).collectAsState(initial = null)
    val onSurface by ColorSettingsStore.getOnSurface(ctx).collectAsState(initial = null)

    val error by ColorSettingsStore.getError(ctx).collectAsState(initial = null)
    val onError by ColorSettingsStore.getOnError(ctx).collectAsState(initial = null)

    val outline by ColorSettingsStore.getOutline(ctx).collectAsState(initial = null)

    val angleLineColor by ColorSettingsStore.getAngleLineColor(ctx).collectAsState(initial = null)
    val circleColor by ColorSettingsStore.getCircleColor(ctx).collectAsState(initial = null)
//    val completeColor by ColorSettingsStore.getComplete(ctx).collectAsState(initial = null)
//
//    val customSelect by ColorSettingsStore.getSelect(ctx).collectAsState(initial = null)
//    val customNoteTypeText by ColorSettingsStore.getNoteTypeText(ctx).collectAsState(initial = null)
//    val customNoteTypeChecklist by ColorSettingsStore.getNoteTypeChecklist(ctx).collectAsState(initial = null)
//    val customNoteTypeDrawing by ColorSettingsStore.getNoteTypeDrawing(ctx).collectAsState(initial = null)


    val colorCustomisationMode by ColorModesSettingsStore.getColorCustomisationMode(ctx).collectAsState(initial = ColorCustomisationMode.DEFAULT)
    val selectedDefaultTheme by ColorModesSettingsStore.getDefaultTheme(ctx).collectAsState(initial = DefaultThemes.DARK)

    var showResetValidation by remember { mutableStateOf(false) }
    var showRandomColorsValidation by remember { mutableStateOf(false) }

    SettingsLazyHeader(
        title = stringResource(R.string.color_selector),
        onBack = onBack,
        helpText = stringResource(R.string.color_selector_text),
        onReset = {
            scope.launch {
                ColorSettingsStore.resetAll(ctx)
            }
        }
    ) {

        item {
            TextDivider(stringResource(R.string.color_custom_mode))

            Spacer(Modifier.height(15.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ColorCustomisationMode.entries.forEach {
                    Column(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                scope.launch {
                                    ColorModesSettingsStore.setColorCustomisationMode(ctx, it)
                                }
                            }
                            .padding(5.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        when (it) {
                            ColorCustomisationMode.DEFAULT -> {
                                Icon(
                                    imageVector = Icons.Default.InvertColors,
                                    contentDescription = stringResource(R.string.color_mode_default),
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            ColorCustomisationMode.NORMAL -> {
                                Icon(
                                    imageVector = Icons.Default.Palette,
                                    contentDescription = stringResource(R.string.color_mode_normal),
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            ColorCustomisationMode.ALL -> {
                                Icon(
                                    imageVector = Icons.Default.AllInclusive,
                                    contentDescription = stringResource(R.string.color_mode_all),
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        Spacer(Modifier.height(5.dp))

                        Text(
                            text = colorCustomizationModeName(it),
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.labelSmall
                        )

                        RadioButton(
                            selected = colorCustomisationMode == it,
                            onClick = {
                                scope.launch {
                                    ColorModesSettingsStore.setColorCustomisationMode(ctx, it)
                                }
                            },
                            colors = AppObjectsColors.radioButtonColors()
                        )
                    }
                }
            }
        }

        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { showResetValidation = true },
                    modifier = Modifier.weight(1f),
                    colors = AppObjectsColors.buttonColors()
                ) {
                    Icon(
                        imageVector = Icons.Default.Restore,
                        contentDescription = stringResource(R.string.reset),
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier
                            .clip(CircleShape)
                            .padding(5.dp)
                    )

                    Text(
                        text = stringResource(R.string.reset_to_default_colors),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }

                if (colorCustomisationMode == ColorCustomisationMode.ALL){
                    IconButton(
                        onClick = { showRandomColorsValidation = true },
                        colors = AppObjectsColors.iconButtonColors(
                            backgroundColor = MaterialTheme.colorScheme.primary.copy(0.5f)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shuffle,
                            contentDescription = stringResource(R.string.make_every_colors_random),
                            modifier = Modifier
                                .clip(CircleShape)
                                .padding(5.dp)
                        )
                    }
                }
            }
        }

        item { HorizontalDivider(color = MaterialTheme.colorScheme.outline) }

        when (colorCustomisationMode) {
            ColorCustomisationMode.ALL -> {

                item {
                    ColorPickerRow(
                        label = stringResource(R.string.primary_color),
                        defaultColor = AmoledDefault.Primary,
                        currentColor = primary ?: MaterialTheme.colorScheme.primary
                    ) { scope.launch { ColorSettingsStore.setPrimary(ctx, it) } }
                }

                item {
                    ColorPickerRow(
                        label = stringResource(R.string.on_primary_color),
                        defaultColor = AmoledDefault.OnPrimary,
                        currentColor = onPrimary ?: MaterialTheme.colorScheme.onPrimary
                    ) { scope.launch { ColorSettingsStore.setOnPrimary(ctx, it) } }
                }

                item {
                    ColorPickerRow(
                        label = stringResource(R.string.secondary_color),
                        defaultColor = AmoledDefault.Secondary,
                        currentColor = secondary ?: MaterialTheme.colorScheme.secondary
                    ) { scope.launch { ColorSettingsStore.setSecondary(ctx, it) } }
                }

                item {
                    ColorPickerRow(
                        label = stringResource(R.string.on_secondary_color),
                        defaultColor = AmoledDefault.OnSecondary,
                        currentColor = onSecondary
                            ?: MaterialTheme.colorScheme.onSecondary
                    ) { scope.launch { ColorSettingsStore.setOnSecondary(ctx, it) } }
                }

                item {
                    ColorPickerRow(
                        label = stringResource(R.string.tertiary_color),
                        defaultColor = AmoledDefault.Tertiary,
                        currentColor = tertiary ?: MaterialTheme.colorScheme.tertiary
                    ) { scope.launch { ColorSettingsStore.setTertiary(ctx, it) } }
                }

                item {
                    ColorPickerRow(
                        label = stringResource(R.string.on_tertiary_color),
                        defaultColor = AmoledDefault.OnTertiary,
                        currentColor = onTertiary
                            ?: MaterialTheme.colorScheme.onTertiary
                    ) { scope.launch { ColorSettingsStore.setOnTertiary(ctx, it) } }
                }

                item {
                    ColorPickerRow(
                        label = stringResource(R.string.background_color),
                        defaultColor = AmoledDefault.Background,
                        currentColor = background
                            ?: MaterialTheme.colorScheme.background
                    ) { scope.launch { ColorSettingsStore.setBackground(ctx, it) } }
                }

                item {
                    ColorPickerRow(
                        label = stringResource(R.string.on_background_color),
                        defaultColor = AmoledDefault.OnBackground,
                        currentColor = onBackground
                            ?: MaterialTheme.colorScheme.onBackground
                    ) { scope.launch { ColorSettingsStore.setOnBackground(ctx, it) } }
                }

                item {
                    ColorPickerRow(
                        label = stringResource(R.string.surface_color),
                        defaultColor = AmoledDefault.Surface,
                        currentColor = surface ?: MaterialTheme.colorScheme.surface
                    ) { scope.launch { ColorSettingsStore.setSurface(ctx, it) } }
                }

                item {
                    ColorPickerRow(
                        label = stringResource(R.string.on_surface_color),
                        defaultColor = AmoledDefault.OnSurface,
                        currentColor = onSurface ?: MaterialTheme.colorScheme.onSurface
                    ) { scope.launch { ColorSettingsStore.setOnSurface(ctx, it) } }
                }

                item {
                    ColorPickerRow(
                        label = stringResource(R.string.error_color),
                        defaultColor = AmoledDefault.Error,
                        currentColor = error ?: MaterialTheme.colorScheme.error
                    ) { scope.launch { ColorSettingsStore.setError(ctx, it) } }
                }

                item {
                    ColorPickerRow(
                        label = stringResource(R.string.on_error_color),
                        defaultColor = AmoledDefault.OnError,
                        currentColor = onError ?: MaterialTheme.colorScheme.onError
                    ) { scope.launch { ColorSettingsStore.setOnError(ctx, it) } }
                }

                item {
                    ColorPickerRow(
                        label = stringResource(R.string.outline_color),
                        defaultColor = AmoledDefault.Outline,
                        currentColor = outline ?: MaterialTheme.colorScheme.outline
                    ) { scope.launch { ColorSettingsStore.setOutline(ctx, it) } }
                }

                // === Extra custom action colors ===
                item {
                    ColorPickerRow(
                        label = stringResource(R.string.angle_line_color),
                        defaultColor = AmoledDefault.AngleLineColor,
                        currentColor = angleLineColor ?: LocalExtraColors.current.angleLine
                    ) { scope.launch { ColorSettingsStore.setAngleLineColor(ctx, it) } }
                }

                item {
                    ColorPickerRow(
                        label = stringResource(R.string.circle_color),
                        defaultColor = AmoledDefault.CircleColor,
                        currentColor = circleColor ?: LocalExtraColors.current.circle
                    ) { scope.launch { ColorSettingsStore.setCircleColor(ctx, it) } }
                }

//                item {
//                    ColorPickerRow(
//                        label = stringResource(R.string.complete_color),
//                        defaultColor = AmoledDefault.Complete,
//                        currentColor = complete ?: LocalExtraColors.current.complete
//                    ) { scope.launch { ColorSettingsStore.setComplete(ctx, it) } }
//                }
//
//                item {
//                    ColorPickerRow(
//                        label = stringResource(R.string.select_color),
//                        defaultColor = AmoledDefault.Select,
//                        currentColor = select ?: LocalExtraColors.current.select
//                    ) { scope.launch { ColorSettingsStore.setSelect(ctx, it) } }
//                }
//
//                item {
//                    ColorPickerRow(
//                        label = stringResource(R.string.note_type_text),
//                        defaultColor = AmoledDefault.NoteTypeText,
//                        currentColor = noteTypeText
//                            ?: LocalExtraColors.current.noteTypeText
//                    ) { scope.launch { ColorSettingsStore.setNoteTypeText(ctx, it) } }
//                }
//
//                item {
//                    ColorPickerRow(
//                        label = stringResource(R.string.note_type_checklist),
//                        defaultColor = AmoledDefault.NoteTypeChecklist,
//                        currentColor = noteTypeChecklist
//                            ?: LocalExtraColors.current.noteTypeChecklist
//                    ) { scope.launch { ColorSettingsStore.setNoteTypeChecklist(ctx, it) } }
//                }
//
//                item {
//                    ColorPickerRow(
//                        label = stringResource(R.string.note_type_drawing),
//                        defaultColor = AmoledDefault.NoteTypeDrawing,
//                        currentColor = noteTypeDrawing
//                            ?: LocalExtraColors.current.noteTypeDrawing
//                    ) { scope.launch { ColorSettingsStore.setNoteTypeDrawing(ctx, it) } }
//                }
            }

            ColorCustomisationMode.NORMAL -> {
                item {
                    val bgColorFromTheme = MaterialTheme.colorScheme.background
                    ColorPickerRow(
                        label = stringResource(R.string.primary_color),
                        defaultColor = AmoledDefault.Primary,
                        currentColor = primary ?: MaterialTheme.colorScheme.primary
                    ) { newColor ->

                        val backgroundColor = background ?: bgColorFromTheme

                        val secondaryColor = newColor.adjustBrightness(1.2f)
                        val tertiaryColor = secondaryColor.adjustBrightness(1.2f)
                        val surfaceColor = newColor.blendWith(backgroundColor, 0.7f)

                        scope.launch {
                            ColorSettingsStore.setPrimary(ctx, newColor)
                            ColorSettingsStore.setSecondary(ctx, secondaryColor)
                            ColorSettingsStore.setTertiary(ctx, tertiaryColor)
                            ColorSettingsStore.setSurface(ctx, surfaceColor)
                        }
                    }
                }

                item {
                    ColorPickerRow(
                        label = stringResource(R.string.background_color),
                        defaultColor = AmoledDefault.Background,
                        currentColor = background
                            ?: MaterialTheme.colorScheme.background
                    ) {
                        scope.launch {
                            ColorSettingsStore.setBackground(ctx, it)
                        }
                    }
                }

                item {
                    ColorPickerRow(
                        label = stringResource(R.string.text_color),
                        defaultColor = AmoledDefault.OnPrimary,
                        currentColor = onPrimary ?: MaterialTheme.colorScheme.onPrimary
                    ) {
                        scope.launch {
                            ColorSettingsStore.setOnPrimary(ctx, it)
                            ColorSettingsStore.setOnSecondary(ctx, it)
                            ColorSettingsStore.setOnTertiary(ctx, it)
                            ColorSettingsStore.setOnSurface(ctx, it)
                            ColorSettingsStore.setOnBackground(ctx, it)
                            ColorSettingsStore.setOutline(ctx, it)
                            ColorSettingsStore.setOnError(ctx, it)
                        }
                    }
                }
            }

            ColorCustomisationMode.DEFAULT -> {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = MaterialTheme.colorScheme.surface,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        DefaultThemes.entries.forEach {
                            Column(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        scope.launch {
                                            ColorModesSettingsStore.setDefaultTheme(ctx, it)
                                            applyDefaultThemeColors(ctx, it)
                                        }
                                    }
                                    .padding(5.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {

                                when (it) {
                                    DefaultThemes.AMOLED -> Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(Color.Black)
                                            .border(
                                                1.dp,
                                                MaterialTheme.colorScheme.outline.copy(0.5f),
                                                CircleShape
                                            )
                                    )
                                    DefaultThemes.DARK -> Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(Color.DarkGray)
                                            .border(
                                                1.dp,
                                                MaterialTheme.colorScheme.outline.copy(0.5f),
                                                CircleShape
                                            )
                                    )
                                    DefaultThemes.LIGHT -> Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(Color.White)
                                            .border(
                                                1.dp,
                                                MaterialTheme.colorScheme.outline.copy(0.5f),
                                                CircleShape
                                            )
                                    )
                                    DefaultThemes.SYSTEM -> Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primary)
                                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(0.5f), CircleShape)
                                    )
                                }

                                Spacer(Modifier.height(5.dp))

                                Text(
                                    text = defaultThemeName(it),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    style = MaterialTheme.typography.labelSmall
                                )

                                RadioButton(
                                    selected = selectedDefaultTheme == it,
                                    onClick = {
                                        scope.launch {
                                            ColorModesSettingsStore.setDefaultTheme(ctx, it)
                                            applyDefaultThemeColors(ctx, it)
                                        }
                                    },
                                    colors = AppObjectsColors.radioButtonColors()
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if(showResetValidation){
        UserValidation(
            title = stringResource(R.string.reset_to_default_colors),
            message = stringResource(R.string.reset_to_default_colors_explanation),
            onCancel = { showResetValidation = false }
        ) {
            scope.launch {
                ColorSettingsStore.resetColors(
                    ctx,
                    colorCustomisationMode,
                    selectedDefaultTheme
                )
                showResetValidation = false
            }
        }
    }
    if(showRandomColorsValidation){
        UserValidation(
            title = stringResource(R.string.make_every_colors_random),
            message = stringResource(R.string.make_every_colors_random_explanation),
            onCancel = { showRandomColorsValidation = false }
        ) {
            scope.launch {
                ColorSettingsStore.setAllRandomColors(ctx)
                showRandomColorsValidation = false
            }
        }
    }
}

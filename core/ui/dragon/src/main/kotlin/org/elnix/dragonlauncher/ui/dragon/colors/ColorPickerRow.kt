@file:Suppress("AssignedValueIsNeverRead")

package org.elnix.dragonlauncher.ui.dragon.colors

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.base.util.ColorUtils.randomColor
import org.elnix.dragonlauncher.base.util.ColorUtils.semiTransparentIfDisabled
import org.elnix.dragonlauncher.base.util.ColorUtils.toHexWithAlpha
import org.elnix.dragonlauncher.common.utils.CopyPasteUtils.copyToClipboard
import org.elnix.dragonlauncher.common.utils.CopyPasteUtils.pasteClipboard
import org.elnix.dragonlauncher.enumsui.select.ColorPickerMode
import org.elnix.dragonlauncher.enumsui.toggle.ColorActions
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.ktx.showToast
import org.elnix.dragonlauncher.settings.stores.map.ColorModesSettingsStore
import org.elnix.dragonlauncher.theme.AppObjectsColors
import io.github.elnix90.runtime.asState
import org.elnix.dragonlauncher.ui.base.components.Spacer
import org.elnix.dragonlauncher.ui.dragon.components.DragonIconButton
import org.elnix.dragonlauncher.ui.dragon.components.DragonModalBottomSheet
import org.elnix.dragonlauncher.ui.dragon.components.DragonRow
import org.elnix.dragonlauncher.ui.dragon.components.SliderWithLabel
import org.elnix.dragonlauncher.ui.dragon.components.ValidateCancelButtons
import org.elnix.dragonlauncher.ui.dragon.generic.MultiSelectConnectedButtonRow
import org.elnix.dragonlauncher.ui.dragon.generic.SingleSelectConnectedButtonRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColorPickerRow(
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    currentColor: Color,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    onColorPicked: (Color?) -> Unit
) {
    var showPicker by remember { mutableStateOf(false) }
    var actualColor by remember(currentColor) { mutableStateOf(currentColor) }

    val savedMode by ColorModesSettingsStore.colorPickerMode.asState()
    val initialPage = remember(savedMode) { ColorPickerMode.entries.indexOf(savedMode) }

    DragonRow(
        modifier = modifier,
        enabled = enabled,
        onClick = { showPicker = true }
    ) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurface.semiTransparentIfDisabled(enabled),
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.weight(1f),
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {

            ColorPickerButtonOne(
                currentColor = currentColor,
                onReset = { onColorPicked(null) },
                backgroundColor = backgroundColor,
                onColorPicked = onColorPicked
            )

            ColorPickerButtonTwo(
                currentColor = currentColor,
                onReset = { onColorPicked(null) },
                backgroundColor = backgroundColor,
                onColorPicked = onColorPicked
            )

            Spacer(12.dp)

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(currentColor, shape = CircleShape)
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.outline,
                        CircleShape
                    )
            )
        }
    }

    if (showPicker) {
        DragonModalBottomSheet(
            sheetState = rememberModalBottomSheetState(true),
            onDismissRequest = { showPicker = false }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = label,
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleMediumEmphasized
                    )

                    Spacer()

                    MultiSelectConnectedButtonRow(
                        entries = ColorActions.entries
                    ) {
                        when (it) {
                            ColorActions.Reset -> onColorPicked(null)
                            ColorActions.Random -> actualColor = randomColor()
                        }
                    }
                }

                ColorPicker(
                    color = actualColor,
                    initialPage = initialPage,
                    onColorSelected = { actualColor = it }
                )

                ValidateCancelButtons(
                    onCancel = { showPicker = false }
                ) {
                    onColorPicked(actualColor)
                    showPicker = false
                }
            }
        }
    }
}


@Composable
private fun ColorPicker(
    color: Color,
    initialPage: Int,
    onColorSelected: (Color) -> Unit
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    val pickerModes = ColorPickerMode.entries
    // Synchronize pager state with stored mode
    val pagerState = rememberPagerState(initialPage = initialPage) { pickerModes.size }

    var hexText by remember { mutableStateOf(color.toHexWithAlpha()) }

    LaunchedEffect(color) {
        hexText = color.toHexWithAlpha()
    }

    val currentMode = pickerModes[pagerState.currentPage]
    // Save the current page as mode whenever changed
    LaunchedEffect(currentMode) {
        ColorModesSettingsStore.colorPickerMode.set(ctx, currentMode)
    }

    Column(modifier = Modifier.fillMaxWidth()) {

        SingleSelectConnectedButtonRow(
            entries = pickerModes,
            checked = { currentMode == it },
            modifier = Modifier.fillMaxWidth()
        ) {
            scope.launch { pagerState.animateScrollToPage(it.ordinal) }
        }

        Spacer(5.dp)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .background(color)
                .border(1.dp, MaterialTheme.colorScheme.outline),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                val textBoxColor = if (color.luminance() > 0.4) Color.Black else Color.White

                TextField(
                    value = hexText,
                    onValueChange = {
                        if (it.length <= 9) hexText = it
                        runCatching {
                            if (it.startsWith("#") && it.length == 9) {
                                onColorSelected(Color(it.toColorInt()))
                            }
                        }
                    },
                    label = {
                        Text(
                            text = "HEX - AARRGGBB",
                            color = textBoxColor
                        )
                    },
                    colors = AppObjectsColors.outlinedTextFieldColors(
                        backgroundColor = Color.Transparent,
                        onBackgroundColor = textBoxColor,
                        removeBorder = true
                    ),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )

                Spacer(50.dp)

                DragonIconButton(
                    onClick = {
                        ctx.copyToClipboard(hexText)
                    },
                    colors = IconButtonDefaults.iconButtonColors(containerColor = color, contentColor = textBoxColor),
                    icon = R.drawable.copy,
                    contentDescription = R.string.copy
                )

                DragonIconButton(
                    onClick = {
                        val newColor = pasteColorHexFromClipboard(ctx)
                        newColor?.let { pasted ->
                            hexText = pasted.toHexWithAlpha()
                            onColorSelected(pasted)
                        }
                    },
                    colors = IconButtonDefaults.iconButtonColors(containerColor = color, contentColor = textBoxColor),
                    icon = R.drawable.paste,
                    contentDescription = "Paste HEX"
                )
            }
        }

        Spacer(15.dp)

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.height(380.dp)
        ) { page ->
            when (pickerModes[page]) {
                ColorPickerMode.Default -> DefaultColorPicker(
                    initialColor = color,
                    onColorSelected = onColorSelected
                )

                ColorPickerMode.Slider -> SliderColorPicker(
                    actualColor = color,
                    onColorSelected = onColorSelected
                )

                ColorPickerMode.Gradient -> GradientColorPicker(
                    initialColor = color,
                    onColorSelected = onColorSelected
                )
            }
        }

        Spacer(12.dp)

        SliderWithLabel(
            label = stringResource(R.string.transparency),
            value = color.alpha,
            color = MaterialTheme.colorScheme.primary,
            backgroundColor = MaterialTheme.colorScheme.surface,
            valueRange = 0f..1f
        ) { alpha -> onColorSelected(color.copy(alpha = alpha)) }
    }
}


fun pasteColorHexFromClipboard(ctx: Context): Color? {
    ctx.pasteClipboard()?.let { pasted ->
        try {
            if (pasted.startsWith("#") && pasted.length == 9) {
                return Color(pasted.toColorInt())
            }
        } catch (e: Exception) {
            e.printStackTrace()
            ctx.showToast("Error while parsing clipboard color")
            return null
        }
    }
    return null
}

package org.elnix.dragonlauncher.ui.dragon.colors

import android.content.Context
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import org.elnix.dragonlauncher.base.utils.CopyPasteUtils.copyToClipboard
import org.elnix.dragonlauncher.base.utils.CopyPasteUtils.pasteClipboard
import org.elnix.dragonlauncher.enumsui.select.ColorPickerMode
import org.elnix.dragonlauncher.enumsui.toggle.ColorActions
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.ktx.randomColor
import org.elnix.dragonlauncher.ktx.showToast
import org.elnix.dragonlauncher.ktx.toHexWithAlpha
import org.elnix.dragonlauncher.settings.stores.map.ColorModesSettingsStore
import org.elnix.dragonlauncher.theme.AppObjectsColors
import org.elnix.dragonlauncher.ui.base.components.Spacer
import org.elnix.dragonlauncher.ui.composition.LocalColorPickerMode
import org.elnix.dragonlauncher.ui.dragon.components.DragonIconButton
import org.elnix.dragonlauncher.ui.dragon.components.DragonModalBottomSheet
import org.elnix.dragonlauncher.ui.dragon.components.SliderWithLabel
import org.elnix.dragonlauncher.ui.dragon.components.rememberBottomSheetState
import org.elnix.dragonlauncher.ui.dragon.generic.MultiSelectConnectedButtonRow
import org.elnix.dragonlauncher.ui.dragon.generic.SingleSelectConnectedButtonRow
import org.elnix.dragonlauncher.ui.dragon.text.DialogTitle
import org.elnix.dragonlauncher.ui.dragon.text.TextWithDescription

/**
 * Color picker row
 *
 * Uses internally a Compose state derived by the current color to mutate it inside the color picker sheet.
 * When the sheet is dismissed, the [onColorPicked] is called
 *
 * @param currentColor the current color saved in settings
 * @param defaultColor the default color
 * @param onColorPicked when the user saves a color and validate
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColorPickerRow(
    title: String,
    description: String?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    currentColor: Color?,
    defaultColor: Color?,
    onColorPicked: (Color?) -> Unit
) {
    var showPicker by remember { mutableStateOf(false) }

    val currentColorNotNull = currentColor ?: Color.Unspecified
    var actualColor by remember(currentColorNotNull) { mutableStateOf(currentColorNotNull) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled) { showPicker = true }
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {

        TextWithDescription(
            text = title,
            description = description,
            modifier = Modifier.weight(1f),
            enabled = enabled
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {

            ColorPickerButton(
                button = ColorModesSettingsStore.colorPickerButtonOne,
                enabled = enabled,
                currentColor = currentColor,
                defaultColor = defaultColor,
                onColorPicked = onColorPicked
            )

            ColorPickerButton(
                button = ColorModesSettingsStore.colorPickerButtonTwo,
                enabled = enabled,
                currentColor = currentColor,
                defaultColor = defaultColor,
                onColorPicked = onColorPicked
            )

            Spacer(12.dp)

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(currentColorNotNull, shape = CircleShape)
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
            sheetState = rememberBottomSheetState(true),
            onDismissRequest = {
                onColorPicked(actualColor)
                showPicker = false
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 16.dp)
            ) {
                DialogTitle(
                    text = title,
                    trailingIcon = {
                        MultiSelectConnectedButtonRow(
                            entries = ColorActions.entries,
                            enabled = {
                                when (it) {
                                    ColorActions.Reset -> actualColor != defaultColor
                                    ColorActions.Random -> true
                                }
                            }
                        ) {
                            actualColor = when (it) {
                                ColorActions.Reset -> defaultColor ?: Color.Unspecified
                                ColorActions.Random -> randomColor()
                            }
                        }
                    }
                )

                ColorPicker(
                    initialColor = currentColorNotNull,
                    color = actualColor,
                    onColorSelected = { actualColor = it }
                )
            }
        }
    }
}


@Composable
private fun ColorPicker(
    initialColor: Color,
    color: Color,
    onColorSelected: (Color) -> Unit
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    val colorPickerMode = LocalColorPickerMode.current
    val initialPage = remember { ColorPickerMode.entries.indexOf(colorPickerMode) }
    val pagerState = rememberPagerState(initialPage = initialPage) { ColorPickerMode.entries.size }

    var hexText by remember { mutableStateOf(color.toHexWithAlpha) }

    LaunchedEffect(color) {
        hexText = color.toHexWithAlpha
    }

    val currentMode = ColorPickerMode.entries[pagerState.currentPage]
    // Save the current page as mode whenever changed
    LaunchedEffect(currentMode) {
        ColorModesSettingsStore.colorPickerMode.set(ctx, currentMode)
    }

    Column(modifier = Modifier.fillMaxWidth()) {

        SingleSelectConnectedButtonRow(
            entries = ColorPickerMode.entries,
            checked = { currentMode == it },
            modifier = Modifier.fillMaxWidth()
        ) {
            scope.launch { pagerState.animateScrollToPage(it.ordinal) }
        }

        Spacer(5.dp)

        val displayedColor by animateColorAsState(
            targetValue = color,
            animationSpec = tween(durationMillis = 200)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .background(
                    color = displayedColor,
                    shape = MaterialTheme.shapes.medium
                )
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline,
                    shape = MaterialTheme.shapes.medium
                ),
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
                            hexText = pasted.toHexWithAlpha
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
            when (ColorPickerMode.entries[page]) {
                ColorPickerMode.Default -> DefaultColorPicker(
                    selectedColor = color,
                    onColorSelected = onColorSelected
                )

                ColorPickerMode.Slider -> SliderColorPicker(
                    actualColor = color,
                    initialColor = initialColor,
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
            valueRange = 0f..1f,
            resetEnabled = color.alpha != initialColor.alpha,
            onReset = {
                onColorSelected(color.copy(alpha = color.alpha))
            }
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

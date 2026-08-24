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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import io.github.elnix90.runtime.asMutableState
import kotlinx.coroutines.delay
import org.elnix.dragonlauncher.base.model.serializables.IconShape
import org.elnix.dragonlauncher.base.resolveShape
import org.elnix.dragonlauncher.base.utils.CopyPasteUtils.copyToClipboard
import org.elnix.dragonlauncher.base.utils.CopyPasteUtils.pasteClipboard
import org.elnix.dragonlauncher.enumsui.select.ColorPickerMode
import org.elnix.dragonlauncher.enumsui.toggle.ColorActions
import org.elnix.dragonlauncher.enumsui.toggle.ColorActions.Copy
import org.elnix.dragonlauncher.enumsui.toggle.ColorActions.Paste
import org.elnix.dragonlauncher.enumsui.toggle.ColorActions.Random
import org.elnix.dragonlauncher.enumsui.toggle.ColorActions.Reset
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.ktx.randomColor
import org.elnix.dragonlauncher.ktx.rect
import org.elnix.dragonlauncher.ktx.showToast
import org.elnix.dragonlauncher.ktx.toHexWithAlpha
import org.elnix.dragonlauncher.settings.stores.map.ColorModesSettingsStore
import org.elnix.dragonlauncher.theme.AppObjectsColors
import org.elnix.dragonlauncher.ui.base.components.Spacer
import org.elnix.dragonlauncher.ui.dragon.components.DragonGroupScope
import org.elnix.dragonlauncher.ui.dragon.components.DragonModalBottomSheet
import org.elnix.dragonlauncher.ui.dragon.components.DragonSettingsGroup
import org.elnix.dragonlauncher.ui.dragon.components.SliderWithLabel
import org.elnix.dragonlauncher.ui.dragon.components.rememberBottomSheetState
import org.elnix.dragonlauncher.ui.dragon.generic.MultiSelectConnectedButtonRow
import org.elnix.dragonlauncher.ui.dragon.generic.SingleSelectConnectedButtonRow
import org.elnix.dragonlauncher.ui.dragon.text.DialogTitle
import org.elnix.dragonlauncher.ui.dragon.text.TextWithDescription
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.milliseconds

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
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DragonGroupScope.ColorPickerRow(
    title: String,
    description: String?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    currentColor: Color?,
    defaultColor: Color?,
    onColorPicked: (Color?) -> Unit
) {
    val ctx = LocalContext.current

    val initialColorNotNull = currentColor ?: Color.Unspecified

    var currentMode by ColorModesSettingsStore.colorPickerMode.asMutableState()
    var actualColor by remember(initialColorNotNull) { mutableStateOf(initialColorNotNull) }
    var previewBoxShape by remember { mutableStateOf(IconShape.Random.resolveShape()) }

    var showPicker by remember { mutableStateOf(false) }
    LaunchedEffect(showPicker) {
        if (showPicker) {
            delay(50.milliseconds)
            previewBoxShape = IconShape.Random.resolveShape()
        }
    }

    var hexText by remember { mutableStateOf(actualColor.toHexWithAlpha) }
    LaunchedEffect(actualColor) {
        hexText = actualColor.toHexWithAlpha
    }

    Row(
        modifier = modifier
            .dragonSettingGroup(enabled = enabled) {
                clickable(enabled) { showPicker = true }
            },
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

            ColorCirclePreview(initialColorNotNull, previewBoxShape)
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
                DialogTitle(title)

                MultiSelectConnectedButtonRow(
                    entries = ColorActions.entries,
                    enabled = {
                        when (it) {
                            Reset -> actualColor != defaultColor
                            Random, Copy, Paste -> true
                        }
                    }
                ) {
                    when (it) {
                        Reset -> actualColor = defaultColor ?: Color.Unspecified
                        Random -> actualColor = randomColor()
                        Copy -> ctx.copyToClipboard(hexText)
                        Paste -> {
                            val newColor = pasteColorHexFromClipboard(ctx)
                            newColor?.let { pasted ->
                                hexText = pasted.toHexWithAlpha
                                actualColor = pasted
                            }
                        }
                    }
                }


                SingleSelectConnectedButtonRow(
                    entries = ColorPickerMode.entries,
                    checked = { currentMode == it },
                    modifier = Modifier.fillMaxWidth()
                ) { currentMode = it }

                Spacer(5.dp)

                val displayedColor by animateColorAsState(
                    targetValue = actualColor,
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
                        val textBoxColor = if (actualColor.luminance() > 0.4) Color.Black else Color.White

                        TextField(
                            value = hexText,
                            onValueChange = {
                                if (it.length <= 9) hexText = it
                                runCatching {
                                    if (it.startsWith("#") && it.length == 9) {
                                        actualColor = Color(it.toColorInt())
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
                    }
                }

                Spacer(15.dp)

                when (currentMode) {
                    ColorPickerMode.Default -> DefaultColorPicker(
                        selectedColor = actualColor,
                        onColorSelected = { actualColor = it }
                    )

                    ColorPickerMode.Slider -> SliderColorPicker(
                        actualColor = actualColor,
                        initialColor = initialColorNotNull,
                        onColorSelected = { actualColor = it }
                    )

                    ColorPickerMode.Gradient -> GradientColorPicker(
                        initialColor = actualColor,
                        onColorSelected = { actualColor = it }
                    )
                }

                Spacer(12.dp)

                DragonSettingsGroup {
                    SliderWithLabel(
                        label = stringResource(R.string.transparency),
                        value = actualColor.alpha,
                        valueRange = 0f..1f,
                        resetEnabled = actualColor.alpha != initialColorNotNull.alpha,
                        onReset = {
                            actualColor = actualColor.copy(alpha = actualColor.alpha)
                        }
                    ) { alpha -> actualColor = (actualColor.copy(alpha = alpha)) }
                }
            }
        }
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


@Composable
fun ColorCirclePreview(
    color: Color,
    shape: Shape
) {
    Box(
        modifier = Modifier
            .size(50.dp)
            .clip(shape)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = shape
            )
            .drawBehind {
                pngBackgroundTexture(color)
            }
    )
}

private fun DrawScope.pngBackgroundTexture(
    color: Color,
    gridSize: Dp = 8.dp
) {
    val cellSizePx = gridSize.toPx()

    val size = (this.size.width / cellSizePx).roundToInt()
    var count = 0

    repeat(size) { y ->
        repeat(size) { x ->
            drawRect(
                color = if (count % 2 == 0) Color.White else Color.Gray,
                size = Size.rect(cellSizePx),
                blendMode = BlendMode.Src,
                topLeft = Offset(
                    x = cellSizePx * x,
                    y = cellSizePx * y
                )
            )
            count++
        }
        if (size % 2 == 0) count++
    }

    drawRect(color)
}
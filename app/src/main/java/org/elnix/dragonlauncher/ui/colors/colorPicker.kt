@file:Suppress("AssignedValueIsNeverRead")

package org.elnix.dragonlauncher.ui.colors

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.R
import org.elnix.dragonlauncher.data.helpers.ColorPickerMode
import org.elnix.dragonlauncher.data.helpers.colorPickerText
import org.elnix.dragonlauncher.data.stores.ColorModesSettingsStore.getColorPickerMode
import org.elnix.dragonlauncher.data.stores.ColorModesSettingsStore.setColorPickerMode
import org.elnix.dragonlauncher.ui.components.ValidateCancelButtons
import org.elnix.dragonlauncher.ui.components.dialogs.CustomAlertDialog
import org.elnix.dragonlauncher.ui.helpers.SliderWithLabel
import org.elnix.dragonlauncher.utils.colors.AppObjectsColors
import org.elnix.dragonlauncher.utils.colors.adjustBrightness
import org.elnix.dragonlauncher.utils.colors.randomColor
import org.elnix.dragonlauncher.utils.colors.toHexWithAlpha
import org.elnix.dragonlauncher.utils.copyToClipboard
import org.elnix.dragonlauncher.utils.pasteClipboard

@Composable
fun ColorPickerRow(
    label: String,
    showLabel: Boolean = true,
    enabled: Boolean = true,
    defaultColor: Color,
    currentColor: Color,
    randomColorButton: Boolean = true,
    resetButton: Boolean = true,
    maxLuminance: Float = 1f,
    onColorPicked: (Color) -> Unit
) {
    val ctx = LocalContext.current

    var showPicker by remember { mutableStateOf(false) }
    var actualColor by remember { mutableStateOf(currentColor) }

    val modifier = if (showLabel) Modifier.fillMaxWidth() else Modifier.wrapContentWidth()

    val savedMode by getColorPickerMode(ctx).collectAsState(initial = ColorPickerMode.SLIDERS)
    val initialPage = remember(savedMode) { ColorPickerMode.entries.indexOf(savedMode) }


    Row(
       modifier = modifier
           .clickable(enabled) { showPicker = true }
           .background(
               color = MaterialTheme.colorScheme.surface.copy(if (enabled) 1f else 0.5f),
               shape = RoundedCornerShape(12.dp)
           )
           .padding(horizontal = 16.dp, vertical = 14.dp),

        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if(showLabel){
            Text(
                text = label,
                color = MaterialTheme.colorScheme.onSurface.copy(if (enabled) 1f else 0.5f),
                modifier = Modifier.weight(1f),
                maxLines = Int.MAX_VALUE,
                softWrap = true
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            if (randomColorButton) {
                Icon(
                    imageVector = Icons.Default.Shuffle,
                    contentDescription = "Random color",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface.adjustBrightness(0.8f))
                        .padding(5.dp)
                        .clickable(enabled) { onColorPicked(randomColor(minLuminance = 0.2f, maxLuminance = maxLuminance)) }
                )
            }

            Spacer(Modifier.width(8.dp))

            if (resetButton) {
                Icon(
                    imageVector = Icons.Default.Restore,
                    contentDescription = "Reset color",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface.adjustBrightness(0.8f))
                        .padding(5.dp)
                        .clickable(enabled) { onColorPicked(defaultColor) }
                )
            }

            Spacer(Modifier.width(12.dp))

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
        CustomAlertDialog(
            modifier = modifier.padding(15.dp),
            onDismissRequest = { showPicker = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Restore,
                    contentDescription = "Reset Color",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .clip(CircleShape)
                        .padding(8.dp)
                        .clickable { actualColor = defaultColor }
                )
            },
            title = {
                Text(
                    text = label,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                ColorPicker(
                    color = actualColor,
                    initialPage = initialPage,
                    onColorSelected = { actualColor = it }
                )
            },
            confirmButton = {
                ValidateCancelButtons(
                    onCancel = { showPicker = false}
                ) {
                    onColorPicked(actualColor)
                    showPicker = false
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            alignment = Alignment.Center
        )
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

    var hexText by remember { mutableStateOf(toHexWithAlpha(color)) }

    LaunchedEffect(color) {
        hexText = toHexWithAlpha(color)
    }

    // Save the current page as mode whenever changed
    LaunchedEffect(pagerState.currentPage) {
        val currentMode = pickerModes[pagerState.currentPage]
        setColorPickerMode(ctx, currentMode)
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        // Tabs to indicate and jump
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            pickerModes.forEachIndexed { idx, mode ->
                Text(
                    text = colorPickerText(mode),
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { scope.launch { pagerState.scrollToPage(idx) } }
                        .background(
                            if (pagerState.currentPage == idx) MaterialTheme.colorScheme.secondary
                            else MaterialTheme.colorScheme.surface
                        )
                        .padding(12.dp),
                    color = if (pagerState.currentPage == idx) MaterialTheme.colorScheme.onSecondary
                    else MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(Modifier.height(5.dp))

        // --- Preview box ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .background(color)
                .border(1.dp, MaterialTheme.colorScheme.outline),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = toHexWithAlpha(color),
                color = if (color.luminance() > 0.4) Color.Black else Color.White
            )
        }

        Spacer(Modifier.height(15.dp))

        HorizontalPager(state = pagerState) { page ->
            when (pickerModes[page]) {
                ColorPickerMode.SLIDERS -> SliderColorPicker(
                    actualColor = color,
                    onColorSelected = onColorSelected
                )
                ColorPickerMode.GRADIENT -> GradientColorPicker(
                    initialColor = color,
                    onColorSelected = onColorSelected
                )
                ColorPickerMode.DEFAULTS -> DefaultColorPicker(
                    initialColor = color,
                    onColorSelected = onColorSelected
                )
            }
        }


        Spacer(Modifier.height(12.dp))


        SliderWithLabel(
            label = stringResource(R.string.transparency),
            showValue = false,
            value = color.alpha,
            color = MaterialTheme.colorScheme.primary,
        ) { alpha -> onColorSelected(color.copy(alpha = alpha)) }

        Spacer(Modifier.height(15.dp))

        // --- HEX entry ---
        val context = LocalContext.current

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = hexText,
                onValueChange = {
                    hexText = it
                    runCatching {
                        if (it.startsWith("#")) {
                            onColorSelected(Color(it.toColorInt()))
                        }
                    }
                },
                label = { Text("HEX") },
                colors = AppObjectsColors.outlinedTextFieldColors(
                    backgroundColor = MaterialTheme.colorScheme.surface
                ),
                singleLine = true,
                modifier = Modifier.weight(1f)
            )

            IconButton(
                onClick = {
                    context.copyToClipboard(hexText)
                },
                colors = AppObjectsColors.iconButtonColors(
                    backgroundColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy HEX"
                )
            }

            IconButton(
                onClick = {
                    context.pasteClipboard()?.let { pasted ->
                        hexText = pasted
                        runCatching {
                            if (pasted.startsWith("#")) {
                                onColorSelected(Color(pasted.toColorInt()))
                            }
                        }
                    }
                },
                colors = AppObjectsColors.iconButtonColors(
                    backgroundColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Icon(
                    imageVector = Icons.Default.ContentPaste,
                    contentDescription = "Paste HEX"
                )
            }
        }

    }
}

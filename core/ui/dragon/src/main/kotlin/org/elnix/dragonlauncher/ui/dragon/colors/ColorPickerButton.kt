package org.elnix.dragonlauncher.ui.dragon.colors

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.base.ColorUtils.alphaMultiplier
import org.elnix.dragonlauncher.base.ColorUtils.randomColor
import org.elnix.dragonlauncher.base.ColorUtils.toHexWithAlpha
import org.elnix.dragonlauncher.common.utils.CopyPasteUtils.copyToClipboard
import org.elnix.dragonlauncher.enumsui.toggle.ColorPickerButtonAction
import org.elnix.dragonlauncher.enumsui.toggle.ColorPickerButtonAction.COPY
import org.elnix.dragonlauncher.enumsui.toggle.ColorPickerButtonAction.PASTE
import org.elnix.dragonlauncher.enumsui.toggle.ColorPickerButtonAction.RANDOM
import org.elnix.dragonlauncher.enumsui.toggle.ColorPickerButtonAction.RESET
import org.elnix.dragonlauncher.settings.stores.ColorModesSettingsStore
import org.elnix.dragonlauncher.ui.base.asState


@Composable
private fun ColorPickerButton(
    button: ColorPickerButtonAction,
    currentColor: Color,
    onReset: () -> Unit,
    backgroundColor: Color,
    onModeChanged: (ColorPickerButtonAction) -> Unit,
    onColorPicked: (Color) -> Unit
) {
    val ctx = LocalContext.current


    var showSelector by remember { mutableStateOf(false) }

    Box {
        Icon(
            painter = painterResource(button.iconEnabled),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .clip(CircleShape)
                .background(backgroundColor.alphaMultiplier(0.8f))
                .combinedClickable(
                    onLongClick = { showSelector = true }
                ) {
                    when (button) {
                        RANDOM -> onColorPicked(randomColor(minLuminance = 0.2f))
                        RESET -> { onReset() }
                        COPY -> ctx.copyToClipboard(currentColor.toHexWithAlpha())
                        PASTE -> {
                            val newColor = pasteColorHexFromClipboard(ctx)
                            newColor?.let { pasted ->
                                onColorPicked(pasted)
                            }
                        }
                    }
                }
                .padding(5.dp)
        )


        DropdownMenu(
            expanded = showSelector,
            onDismissRequest = { showSelector = false },
            containerColor = MaterialTheme.colorScheme.background,
            shape = CircleShape,
            modifier = Modifier.padding(5.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ColorPickerButtonAction.entries.forEach {
                    Icon(
                        painter = painterResource(it.iconEnabled),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(backgroundColor.alphaMultiplier(0.8f))
                            .clickable {
                                onModeChanged(it)
                                showSelector = false
                            }
                            .padding(5.dp)
                    )
                }
            }
        }
    }
}


@Composable
fun ColorPickerButtonOne(
    currentColor: Color,
    onReset: () -> Unit,

    backgroundColor: Color,
    onColorPicked: (Color) -> Unit
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    val button by ColorModesSettingsStore.colorPickerButtonOne.asState()

    ColorPickerButton(
        button = button,
        currentColor = currentColor,
        backgroundColor = backgroundColor,
        onReset = onReset,
        onModeChanged = {
            scope.launch {
                ColorModesSettingsStore.colorPickerButtonOne.set(ctx, it)
            }
        },
        onColorPicked = onColorPicked
    )
}


@Composable
fun ColorPickerButtonTwo(
    currentColor: Color,
    onReset: () -> Unit,
    backgroundColor: Color,
    onColorPicked: (Color) -> Unit
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    val button by ColorModesSettingsStore.colorPickerButtonTwo.asState()

    ColorPickerButton(
        button = button,
        currentColor = currentColor,
        backgroundColor = backgroundColor,
        onReset = onReset,
        onModeChanged = {
            scope.launch {
                ColorModesSettingsStore.colorPickerButtonTwo.set(ctx, it)
            }
        },
        onColorPicked = onColorPicked
    )
}

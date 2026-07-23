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
import androidx.compose.runtime.LaunchedEffect
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
import io.github.elnix90.runtime.asState
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.common.utils.CopyPasteUtils.copyToClipboard
import org.elnix.dragonlauncher.enumsui.toggle.ColorPickerButtonAction
import org.elnix.dragonlauncher.enumsui.toggle.ColorPickerButtonAction.Copy
import org.elnix.dragonlauncher.enumsui.toggle.ColorPickerButtonAction.Paste
import org.elnix.dragonlauncher.enumsui.toggle.ColorPickerButtonAction.Random
import org.elnix.dragonlauncher.enumsui.toggle.ColorPickerButtonAction.Reset
import org.elnix.dragonlauncher.ktx.randomColor
import org.elnix.dragonlauncher.ktx.semiTransparentIfDisabled
import org.elnix.dragonlauncher.ktx.toHexWithAlpha
import org.elnix.dragonlauncher.settings.stores.map.ColorModesSettingsStore


@Composable
private fun ColorPickerButtonInternal(
    button: ColorPickerButtonAction,
    enabled: Boolean,
    currentColor: Color,
    onReset: () -> Unit,
    onModeChanged: (ColorPickerButtonAction) -> Unit,
    onColorPicked: (Color) -> Unit
) {
    val ctx = LocalContext.current

    var showSelector by remember { mutableStateOf(false) }
    LaunchedEffect(enabled) {
        if (!enabled) {
            showSelector = false
        }
    }

    Box {
        Icon(
            painter = painterResource(button.iconEnabled),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface.semiTransparentIfDisabled(enabled),
            modifier = Modifier
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh.semiTransparentIfDisabled(enabled))
                .combinedClickable(
                    enabled = enabled,
                    onLongClick = { showSelector = true }
                ) {
                    when (button) {
                        Random -> onColorPicked(randomColor(minLuminance = 0.2f))
                        Reset -> {
                            onReset()
                        }

                        Copy -> ctx.copyToClipboard(currentColor.toHexWithAlpha)
                        Paste -> {
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
public fun ColorPickerButtonOne(
    currentColor: Color,
    enabled: Boolean,
    onReset: () -> Unit,
    onColorPicked: (Color) -> Unit
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    val button by ColorModesSettingsStore.colorPickerButtonOne.asState()

    val buttonEnabled = when (button) {
        Reset -> currentColor != Color.Unspecified
        Random, Copy,Paste -> true
    }

    ColorPickerButtonInternal(
        button = button,
        enabled = enabled && buttonEnabled,
        currentColor = currentColor,
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
public fun ColorPickerButtonTwo(
    currentColor: Color,
    enabled: Boolean,
    onReset: () -> Unit,
    onColorPicked: (Color) -> Unit
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    val button by ColorModesSettingsStore.colorPickerButtonTwo.asState()

    val buttonEnabled = when (button) {
        Reset -> currentColor != Color.Unspecified
        Random, Copy,Paste -> true
    }

    ColorPickerButtonInternal(
        button = button,
        enabled = enabled && buttonEnabled,
        currentColor = currentColor,
        onReset = onReset,
        onModeChanged = {
            scope.launch {
                ColorModesSettingsStore.colorPickerButtonTwo.set(ctx, it)
            }
        },
        onColorPicked = onColorPicked
    )
}

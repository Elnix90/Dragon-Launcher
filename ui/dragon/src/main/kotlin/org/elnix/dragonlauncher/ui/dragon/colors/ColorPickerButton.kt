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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import io.github.elnix90.core.objects.EnumSettingObject
import io.github.elnix90.runtime.asMutableState
import org.elnix.dragonlauncher.common.utils.CopyPasteUtils.copyToClipboard
import org.elnix.dragonlauncher.enumsui.toggle.ColorPickerButtonAction
import org.elnix.dragonlauncher.enumsui.toggle.ColorPickerButtonAction.Copy
import org.elnix.dragonlauncher.enumsui.toggle.ColorPickerButtonAction.Paste
import org.elnix.dragonlauncher.enumsui.toggle.ColorPickerButtonAction.Random
import org.elnix.dragonlauncher.enumsui.toggle.ColorPickerButtonAction.Reset
import org.elnix.dragonlauncher.ktx.randomColor
import org.elnix.dragonlauncher.ktx.semiTransparentIfDisabled
import org.elnix.dragonlauncher.ktx.toHexWithAlpha


@Composable
fun ColorPickerButton(
    button: EnumSettingObject<ColorPickerButtonAction>,
    enabled: Boolean,
    currentColor: Color,
    onColorPicked: (Color?) -> Unit
) {
    val ctx = LocalContext.current
    var button by button.asMutableState()

    var showSelector by remember { mutableStateOf(false) }
    LaunchedEffect(enabled) {
        if (!enabled) {
            showSelector = false
        }
    }

    val buttonEnabled = when (button) {
        Reset -> currentColor != Color.Unspecified
        Random, Copy,Paste -> true
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
                    enabled = enabled && buttonEnabled,
                    onLongClick = { showSelector = true }
                ) {
                    when (button) {
                        Random -> onColorPicked(randomColor(minLuminance = 0.2f))
                        Reset -> {
                            onColorPicked(null)
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
                                button = it
                                showSelector = false
                            }
                            .padding(5.dp)
                    )
                }
            }
        }
    }
}

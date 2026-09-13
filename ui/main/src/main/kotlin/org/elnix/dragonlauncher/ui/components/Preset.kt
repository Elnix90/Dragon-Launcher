package org.elnix.dragonlauncher.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import org.elnix.dragonlauncher.base.utils.CopyPasteUtils.copyToClipboard
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.ui.base.remember.rememberInteractionSource
import org.elnix.dragonlauncher.ui.dragon.components.DragonIconButton
import org.elnix.dragonlauncher.ui.dragon.components.DragonSettingsGroup
import kotlin.time.Duration.Companion.milliseconds

interface Preset {
    val name: String
}

@Composable
fun <T : Preset> PresetRow(
    presets: List<T>,
    get: () -> T,
    set: (T) -> Unit
) {
    val ctx = LocalContext.current
    val hapticFeedback = LocalHapticFeedback.current

    DragonSettingsGroup(R.string.presets) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            DragonIconButton(
                icon = R.drawable.share,
                contentDescription = R.string.copy,
                modifier = with(this@DragonSettingsGroup) {
                    Modifier
                        .width(50.dp)
                        .dragonSettingGroup()
                }
            ) {
                val preset = get()

                // This has a max size of 0 so it will ALWAYS suggest to share a file
                ctx.copyToClipboard(preset.toString(), maxSize = 0)
            }
            LazyRow(
                modifier = with(this@DragonSettingsGroup) { Modifier.dragonSettingGroup() },
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                itemsIndexed(presets) { idx, preset ->
                    val interactionSource = rememberInteractionSource()
                    val isPressed by interactionSource.collectIsPressedAsState()

                    var canDelete by remember { mutableStateOf(false) }
                    LaunchedEffect(isPressed) {
                        if (isPressed && idx > 0) {
                            delay(250.milliseconds)
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.Confirm)
                            canDelete = true
                        } else {
                            canDelete = false
                        }
                    }

                    val containerColor by animateColorAsState(
                        if (canDelete) {
                            MaterialTheme.colorScheme.errorContainer
                        } else {
                            MaterialTheme.colorScheme.primaryContainer
                        }
                    )

                    Button(
                        onClick = { set(preset) },
                        interactionSource = interactionSource,
                        shapes = ButtonDefaults.shapes(),
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = containerColor,
                                contentColor = contentColorFor(containerColor)
                            )
                    ) {
                        Text(preset.name)
                    }
                }
            }
        }
    }
}

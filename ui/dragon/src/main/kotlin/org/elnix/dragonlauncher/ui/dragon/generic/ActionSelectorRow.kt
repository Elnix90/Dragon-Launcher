package org.elnix.dragonlauncher.ui.dragon.generic

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.elnix.dragonlauncher.ktx.alphaMultiplier
import org.elnix.dragonlauncher.ktx.semiTransparentIfDisabled
import org.elnix.dragonlauncher.theme.AppObjectsColors
import org.elnix.dragonlauncher.ui.base.modifiers.conditional
import org.elnix.dragonlauncher.ui.base.remember.rememberInteractionSource
import org.elnix.dragonlauncher.ui.dragon.components.DragonGroupScope
import org.elnix.dragonlauncher.ui.dragon.components.DragonModalBottomSheet
import org.elnix.dragonlauncher.ui.dragon.components.DragonSettingsGroup
import org.elnix.dragonlauncher.ui.dragon.components.ResetIcon
import org.elnix.dragonlauncher.ui.dragon.components.rememberBottomSheetState
import org.elnix.dragonlauncher.ui.dragon.text.TextWithDescription


@Composable
fun <T> DragonGroupScope.ActionSelectorRow(
    options: List<T>,
    selected: T,
    switchEnabled: Boolean = true,
    label: String,
    optionLabel: @Composable (T) -> String = { it.toString() },
    toggled: Boolean? = null,
    enabled: Boolean = true,
    resetEnabled: Boolean,
    onReset: () -> Unit,
    onSelected: (T?) -> Unit
) {
    var showSheet by remember { mutableStateOf(false) }

    val switchInteractionSource = rememberInteractionSource()
    val globalInteractionSource = rememberInteractionSource()

    Row(
        modifier = Modifier
            .dragonSettingGroup(enabled) {
                clickable(
                    enabled = enabled,
                    interactionSource = if (toggled == true) globalInteractionSource else switchInteractionSource
                ) { showSheet = true }
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextWithDescription(
            text = label,
            description = optionLabel(selected),
            modifier = Modifier.weight(1f),
            enabled = enabled
        )

        if (toggled != null) {
            VerticalDivider(
                modifier = Modifier
                    .height(50.dp)
                    .padding(horizontal = 8.dp),
                color = MaterialTheme.colorScheme.outline.alphaMultiplier(0.7f).semiTransparentIfDisabled(enabled),
                thickness = 1.dp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Switch(
                checked = toggled,
                interactionSource = switchInteractionSource,
                enabled = enabled && switchEnabled,
                onCheckedChange = if (toggled) {
                    { onSelected(null) }
                } else null,
                colors = AppObjectsColors.switchColors(),
            )
        }
        ResetIcon(enabled && resetEnabled, onReset)
    }

    // Options dialog
    if (showSheet) {
        ActionSelector(
            label = label,
            options = options,
            optionLabel = optionLabel,
            selected = selected,
            onSelected = onSelected,
            onDismiss = { showSheet = false }
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> ActionSelector(
    label: String?,
    options: List<T>,
    optionLabel: @Composable (T) -> String = { it.toString() },
    selected: T?,
    onSelected: (T) -> Unit,
    onDismiss: () -> Unit,
) {
    val textColor = MaterialTheme.colorScheme.onSurface

    DragonModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberBottomSheetState(true)
    ) {
        DragonSettingsGroup(label) {
            options.forEach { option ->
                val isSelected = selected == option
                val primaryContainer = MaterialTheme.colorScheme.primaryContainer
                val interactionSource = rememberInteractionSource()
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .dragonSettingGroup {
                            clickable(
                                interactionSource = interactionSource
                            ) {
                                onSelected(option)
                                onDismiss()
                            }
                                .conditional(isSelected) {
                                    background(primaryContainer)
                                }
                        }
                        .padding(10.dp)

                ) {
                    RadioButton(
                        selected = (isSelected),
                        onClick = null,
                        interactionSource = interactionSource
                    )

                    Text(
                        text = optionLabel(option),
                        color = textColor,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

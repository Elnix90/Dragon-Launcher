@file:Suppress("AssignedValueIsNeverRead")

package org.elnix.dragonlauncher.ui.dragon.generic

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.elnix.dragonlauncher.theme.AppObjectsColors
import org.elnix.dragonlauncher.ui.base.modifiers.conditional
import org.elnix.dragonlauncher.ui.base.modifiers.shapedClickable
import org.elnix.dragonlauncher.ui.base.remember.rememberInteractionSource
import org.elnix.dragonlauncher.ui.dragon.components.DragonModalBottomSheet
import org.elnix.dragonlauncher.ui.dragon.components.DragonRow
import org.elnix.dragonlauncher.ui.dragon.components.ResetIcon
import org.elnix.dragonlauncher.ui.dragon.text.TextWithDescription


@Composable
public fun <T> ActionSelectorRow(
    options: List<T>,
    selected: T,
    enabled: Boolean = true,
    switchEnabled: Boolean = true,
    label: String,
    optionLabel: @Composable (T) -> String = { it.toString() },
    toggled: Boolean? = null,
    resetEnabled: Boolean,
    onReset: () -> Unit,
    onSelected: (T?) -> Unit
) {
    var showSheet by remember { mutableStateOf(false) }

    val switchInteractionSource = rememberInteractionSource()
    val globalInteractionSource = rememberInteractionSource()


    DragonRow(
        onClick = { showSheet = true},
        interactionSource = if (toggled != null) globalInteractionSource else switchInteractionSource,
        modifier = Modifier
            .height(IntrinsicSize.Min)
            .fillMaxWidth()
    ) {
        TextWithDescription(
            text = label,
            description = optionLabel(selected),
        )

        // Right side toggle + divider wrapped in a clickable container
        if (toggled != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .conditional(enabled && toggled) {
                        clickable(
                            interactionSource = switchInteractionSource
                        ) {
                            // Disables, selects nothing
                            onSelected(null)
                        }
                    }
                    .fillMaxHeight()
                    .padding(top = 10.dp, bottom = 10.dp, end = 10.dp)
            ) {
                VerticalDivider(
                    modifier = Modifier
                        .height(50.dp)
                        .padding(horizontal = 8.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f),
                    thickness = 1.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Switch(
                    checked = toggled,
                    interactionSource = switchInteractionSource,
                    enabled = switchEnabled,
                    onCheckedChange = null,
                    colors = AppObjectsColors.switchColors(),
                )
            }
        }
        ResetIcon(enabled, onReset)
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
public fun <T> ActionSelector(
    label: String?,
    options: List<T>,
    optionLabel: @Composable (T) -> String = { it.toString() },
    selected: T?,
    onSelected: (T) -> Unit,
    onDismiss: () -> Unit,
) {
    val textColor = MaterialTheme.colorScheme.onSurface

    DragonModalBottomSheet(
        onDismissRequest = onDismiss
    ) {

        if (label != null) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                color = textColor,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().align(Alignment.CenterHorizontally)
            )
        }
        options.forEach { option ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.large)
                    .shapedClickable {
                        onSelected(option)
                        onDismiss()
                    }
                    .padding(15.dp)
            ) {
                Text(
                    text = optionLabel(option),
                    color = textColor,
                    style = MaterialTheme.typography.bodyMedium
                )

                RadioButton(
                    selected = (selected == option),
                    onClick = {
                        onSelected(option)
                        onDismiss()
                    },
                    colors = AppObjectsColors.radioButtonColors(),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

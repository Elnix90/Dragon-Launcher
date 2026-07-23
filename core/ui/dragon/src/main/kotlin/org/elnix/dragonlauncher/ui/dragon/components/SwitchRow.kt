package org.elnix.dragonlauncher.ui.dragon.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.elnix.dragonlauncher.theme.AppObjectsColors
import org.elnix.dragonlauncher.ui.base.remember.rememberInteractionSource
import org.elnix.dragonlauncher.ui.dragon.text.TextWithDescription

@Composable
public fun SwitchRow(
    state: Boolean?,
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    enabled: Boolean = true,
    resetEnabled: Boolean = true,
    defaultValue: Boolean = false,
    onReset: (() -> Unit)? = null,
    onCheck: (Boolean) -> Unit
) {
    val checked = state ?: defaultValue

    val interactionSource = rememberInteractionSource()
    Row(
        modifier = modifier
            .clickable(
                enabled = enabled,
                onClick = { onCheck(!checked) },
                interactionSource = interactionSource
            )
            .padding(10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TextWithDescription(
            text = title,
            description = description,
            modifier = Modifier.weight(1f),
            enabled = enabled
        )

        Switch(
            checked = checked,
            enabled = enabled,
            interactionSource = interactionSource,
            onCheckedChange = null,
            colors = AppObjectsColors.switchColors()
        )

        if (onReset != null) {
            ResetIcon(
                enabled = enabled && resetEnabled,
                onReset = onReset
            )
        }
    }
}

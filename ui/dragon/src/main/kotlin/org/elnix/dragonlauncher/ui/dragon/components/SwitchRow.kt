package org.elnix.dragonlauncher.ui.dragon.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.elnix.dragonlauncher.theme.AppObjectsColors
import org.elnix.dragonlauncher.ui.base.components.BoxedIcon
import org.elnix.dragonlauncher.ui.base.components.Spacer
import org.elnix.dragonlauncher.ui.base.remember.rememberInteractionSource
import org.elnix.dragonlauncher.ui.dragon.text.TextWithDescription

@Composable
fun DragonGroupScope.SwitchRow(
    state: Boolean?,
    title: Int,
    description: Int? = null,
    icon: Int? = null,
    enabled: Boolean = true,
    resetEnabled: Boolean = true,
    defaultValue: Boolean = false,
    onReset: (() -> Unit)? = null,
    onCheck: (Boolean) -> Unit
) {
    val checked = state ?: defaultValue

    val interactionSource = rememberInteractionSource()
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .dragonSettingGroup(enabled = enabled) {
                clickable(
                    enabled = enabled,
                    onClick = { onCheck(!checked) },
                    interactionSource = interactionSource
                )
            }
    ) {
        if (icon != null) {
            BoxedIcon(icon, enabled)
            Spacer(8.dp)
        }

        TextWithDescription(
            text = stringResource(title),
            description = description?.let { stringResource(description) },
            modifier = Modifier.weight(1f),
            enabled = enabled
        )

        Spacer(5.dp)
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

package org.elnix.dragonlauncher.ui.dragon.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.elnix.dragonlauncher.base.util.ColorUtils.semiTransparentIfDisabled
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.theme.AppObjectsColors
import org.elnix.dragonlauncher.ui.dragon.text.TextWithDescription

@Composable
fun SwitchRow(
    state: Boolean?,
    title: String,
    description: String? = null,
    enabled: Boolean = true,
    defaultValue: Boolean = false,
    onToggle: ((Boolean) -> Unit)? = null,
    onReset: (() -> Unit)? = null,
    onCheck: (Boolean) -> Unit
) {
    val checked = state ?: defaultValue

    DragonRow (
        enabled = enabled,
        onClick = { onCheck(!checked) }
    ) {

        CompositionLocalProvider(
            LocalContentColor provides MaterialTheme.colorScheme.onSurface.semiTransparentIfDisabled(enabled)
        ) {
            TextWithDescription(
                text = title,
                description = description,
                modifier = Modifier.weight(1f)
            )
        }

        if (onToggle != null) {
            VerticalDivider(
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .align(Alignment.CenterVertically),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)
            )
        } else {
            Spacer(modifier = Modifier.width(12.dp))
        }

        Switch(
            checked = checked,
            enabled = enabled,
            onCheckedChange = { if (onToggle != null) onToggle(it) else onCheck(it) },
            colors = AppObjectsColors.switchColors()
        )

        if (onReset != null) {
            DragonIconButton(
                icon = R.drawable.reset,
                contentDescription = stringResource(R.string.reset),
                onClick = onReset
            )
        }
    }
}

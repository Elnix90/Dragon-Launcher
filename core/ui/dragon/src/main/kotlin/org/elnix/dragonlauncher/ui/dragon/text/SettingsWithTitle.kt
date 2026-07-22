package org.elnix.dragonlauncher.ui.dragon.text

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

@Composable
public fun SettingsWithTitle(
    title: Int?,
    trailingIcon: (@Composable RowScope.() -> Unit)?,
    content: @Composable () -> Unit
) {

    Column {
        if (title != null) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(id = title),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleMediumEmphasized
                )

                if (trailingIcon != null) {
                    trailingIcon()
                }
            }
        }
    }
    content()
}
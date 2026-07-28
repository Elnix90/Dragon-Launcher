package org.elnix.dragonlauncher.ui.dragon.text

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.elnix.dragonlauncher.ui.base.components.Spacer
import org.elnix.dragonlauncher.ui.dragon.components.ResetIcon

@Composable
fun DialogTitle(
    text: String,
    modifier: Modifier = Modifier,
    trailingIcon: (@Composable RowScope.() -> Unit)? = null,
    resetEnabled: Boolean = true,
    onReset: (() -> Unit)? = null
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        AutoResizeableText(
            modifier = modifier,
            text = text,
            style = MaterialTheme.typography.headlineSmallEmphasized,
            color = MaterialTheme.colorScheme.onSurface
        )

        if (onReset != null) {
            ResetIcon(enabled = resetEnabled, onReset = onReset)
        }

        if (trailingIcon != null) {
            Spacer(5.dp)
            trailingIcon()
        }
    }
}

@Composable
fun DialogSubTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        modifier = modifier,
        text = text,
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onSurface
    )
}

@Composable
fun DialogDescription(text: String, modifier: Modifier = Modifier) {
    Text(
        modifier = modifier,
        text = text,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurface
    )
}
package org.elnix.dragonlauncher.ui.helpers.settings

import android.R.attr.description
import android.R.attr.enabled
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.elnix.dragonlauncher.R
import org.elnix.dragonlauncher.utils.colors.adjustBrightness

@Composable
fun SettingsItem(
    title: String,
    description: String? = null,
    enabled: Boolean = true,
    comingSoon: Boolean = false,
    icon: ImageVector? = null,
    leadIcon: ImageVector? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled) { onClick() }
            .background(
                color = MaterialTheme.colorScheme.surface.adjustBrightness(if (enabled) 1f else 0.5f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(15.dp),
            modifier = Modifier.weight(1f)
        ) {

            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary.adjustBrightness(if (enabled) 1f else 0.5f)
                )
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.adjustBrightness(if (enabled) 1f else 0.5f)
                )

                if (description != null) {
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.adjustBrightness(if (enabled) 0.8f else 0.4f)
                    )
                }
            }
            if (leadIcon != null) {
                Icon(
                    imageVector = leadIcon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary.adjustBrightness(if (enabled) 1f else 0.5f)
                )
            }
        }

        if (comingSoon) {
            Text(
                text = stringResource(R.string.coming_soon),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.adjustBrightness(0.5f)
            )
        }
    }
}

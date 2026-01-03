package org.elnix.dragonlauncher.ui.helpers.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.elnix.dragonlauncher.R
import org.elnix.dragonlauncher.ui.helpers.IconC
import org.elnix.dragonlauncher.utils.colors.AppObjectsColors
import org.elnix.dragonlauncher.utils.colors.adjustBrightness

@Composable
fun SettingsItem(
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    enabled: Boolean = true,
    comingSoon: Boolean = false,
    icon: Any? = null,
    leadIcon: Any? = null,
    onLongClick: (() -> Unit)? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .combinedClickable(
                enabled,
                onLongClick = onLongClick,
                onClick = onClick
            )
            .background(MaterialTheme.colorScheme.surface.copy(if (enabled) 1f else 0.5f))
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
                IconC(
                    icon = icon,
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
                        color = MaterialTheme.colorScheme.onSurface.adjustBrightness(if (enabled) 0.8f else 0.4f),
                        modifier = Modifier.sizeIn(maxHeight = 30.dp)
                    )
                }
            }
            if (leadIcon != null) {
                IconC(
                    icon = leadIcon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary.adjustBrightness(if (enabled) 1f else 0.5f),
                    modifier = Modifier.sizeIn(maxHeight = 30.dp)
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



@Composable
fun SettingItemWithExternalOpen(
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    enabled: Boolean = true,
    comingSoon: Boolean = false,
    icon: Any? = null,
    leadIcon: Any? = null,
    extIcon: Any = Icons.AutoMirrored.Filled.OpenInNew,
    onLongClick: (() -> Unit)? = null,
    onExtClick: () -> Unit,
    onClick: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically
    ){
        SettingsItem(
            title = title,
            modifier = modifier.weight(1f),
            description = description,
            enabled = enabled,
            comingSoon = comingSoon,
            icon = icon,
            leadIcon = leadIcon,
            onLongClick = onLongClick,
            onClick = onClick
        )

        IconButton(
            onClick = onExtClick,
            colors = AppObjectsColors.iconButtonColors(
                backgroundColor = MaterialTheme.colorScheme.surface
            ),
            modifier = Modifier
                .size(52.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            IconC(
                icon = extIcon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.sizeIn(maxHeight = 30.dp)
            )
        }
    }
}

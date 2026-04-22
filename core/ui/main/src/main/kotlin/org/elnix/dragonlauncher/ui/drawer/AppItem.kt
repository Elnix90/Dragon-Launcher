package org.elnix.dragonlauncher.ui.drawer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.elnix.dragonlauncher.common.serializables.AppModel
import org.elnix.dragonlauncher.enumsui.HorizontalAlignment
import org.elnix.dragonlauncher.ui.actions.AppIcon
import org.elnix.dragonlauncher.ui.base.UiConstants.DragonShape
import org.elnix.dragonlauncher.ui.base.components.Spacer
import org.elnix.dragonlauncher.ui.base.compositionslocals.LocalAppItemSettings
import org.elnix.dragonlauncher.ui.base.modifiers.conditional
import org.elnix.dragonlauncher.ui.dragon.components.DragonDropDownMenu


@Composable
private fun CheckIcon() {
    Icon(
        imageVector = Icons.Default.CheckCircle,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.primary,
        modifier = Modifier.background(MaterialTheme.colorScheme.surface, CircleShape)
    )
}

@Composable
fun AppItemHorizontal(
    app: AppModel,
    selected: Boolean,
    onLongClick: ((AppModel) -> Unit)?,
    longPressPopup: @Composable ((AppModel) -> Unit)?,
    onClick: ((AppModel) -> Unit)?
) {

    require(!((onLongClick != null) and (longPressPopup != null))) {
        "Long press action, or popup, or neither, but not both!"
    }

    val appItemSettings = LocalAppItemSettings.current

    var showLongPressPopup by remember { mutableStateOf(false) }

    BadgedBox(
        badge = {
            if (selected) {
                CheckIcon()
            }
        }
    ) {

        val alignment = when(appItemSettings.horizontalAlignment) {
            HorizontalAlignment.Start -> Arrangement.Start
            HorizontalAlignment.Center -> Arrangement.Center
            HorizontalAlignment.End -> Arrangement.End
        }

        Row(
            horizontalArrangement = alignment,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clip(DragonShape)
                .conditional(selected) {
                    background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                }
                .combinedClickable(
                    onLongClick = {
                        if (longPressPopup != null) showLongPressPopup = true
                        else onLongClick?.invoke(app)
                    },
                    onClick = { onClick?.invoke(app) }
                )
                .padding(horizontal = 6.dp)
        ) {

            if (appItemSettings.showIcons) {
                AppIcon(app, appItemSettings.maxIconSize)
            }

            if (appItemSettings.showLabels) {
                Spacer(appItemSettings.iconSpacingHorizontal)
                Text(
                    text = app.name,
                    color = appItemSettings.txtColor
                )
            }
        }
        DragonDropDownMenu(
            expanded = showLongPressPopup,
            onDismissRequest = { showLongPressPopup = false }
        ) {
            longPressPopup!!(app)
        }
    }
}

@Composable
fun AppItemGrid(
    app: AppModel,
    selected: Boolean,
    onLongClick: ((AppModel) -> Unit)?,
    longPressPopup: @Composable ((AppModel) -> Unit)?,
    onClick: ((AppModel) -> Unit)?
) {
    require(!((onLongClick != null) and (longPressPopup != null))) {
        "Long press action, or popup, or neither, but not both!"
    }
    val appItemSettings = LocalAppItemSettings.current

    var showLongPressPopup by remember { mutableStateOf(false) }

    BadgedBox(
        badge = {
            if (selected) {
                CheckIcon()
            }
        },
        modifier = Modifier
            .clip(DragonShape)
            .conditional(selected) {
                background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                    .border(
                        2.dp,
                        MaterialTheme.colorScheme.primary,
                        DragonShape
                    )
            }
            .combinedClickable(
                onLongClick = {
                    if (longPressPopup != null) showLongPressPopup = true
                    else onLongClick?.invoke(app)
                },
                onClick = { onClick?.invoke(app) }
            )
            .padding(5.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(appItemSettings.iconSpacingVertical)
        ) {
            if (appItemSettings.showIcons) {
                AppIcon(app, appItemSettings.maxIconSize)
            }

            if (appItemSettings.showLabels) {
                Text(
                    text = app.name,
                    color = appItemSettings.txtColor,
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        DragonDropDownMenu(
            expanded = showLongPressPopup,
            onDismissRequest = { showLongPressPopup = false }
        ) {
            longPressPopup!!(app)
        }
    }
}

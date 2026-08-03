package org.elnix.dragonlauncher.ui.drawer

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import org.elnix.dragonlauncher.base.model.models.Application
import org.elnix.dragonlauncher.enumsui.toggle.HorizontalAlignment
import org.elnix.dragonlauncher.ui.actions.AppIcon
import org.elnix.dragonlauncher.ui.base.components.Spacer
import org.elnix.dragonlauncher.ui.base.modifiers.conditional
import org.elnix.dragonlauncher.ui.compositionslocals.LocalDrawerSettings
import org.elnix.dragonlauncher.ui.dialogs.AppLongPressPopup
import org.elnix.dragonlauncher.ui.dragon.components.DragonDropDownMenu


@Composable
fun AppItemHorizontal(
    app: Application,
    selected: Boolean,
    onLongClick: ((Application) -> Unit)?,
    longPressPopup: Boolean,
    onClick: ((Application) -> Unit)?
) {
    require(!((onLongClick != null) && (longPressPopup))) {
        "Long press action, or popup, or neither, but not both!"
    }

    val drawerSettings = LocalDrawerSettings.current

    var showLongPressPopup by remember { mutableStateOf(false) }


    val alignment = when (drawerSettings.horizontalAlignment) {
        HorizontalAlignment.Start -> Arrangement.Start
        HorizontalAlignment.Center -> Arrangement.Center
        HorizontalAlignment.End -> Arrangement.End
    }

    Row(
        horizontalArrangement = alignment,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .conditional(selected) {
                background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
            }
            .combinedClickable(
                onLongClick = {
                    if (longPressPopup) showLongPressPopup = true
                    else onLongClick?.invoke(app)
                },
                onClick = { onClick?.invoke(app) }
            )
            .padding(5.dp)
    ) {

        if (drawerSettings.showAppIconsInDrawer) {
            AppIcon(app, drawerSettings.maxIconSize)
        }

        if (drawerSettings.showAppLabelsInDrawer) {
            Spacer(drawerSettings.iconsSpacingHorizontal)
            Text(
                text = app.label,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
    DragonDropDownMenu(
        expanded = showLongPressPopup,
        onDismissRequest = { showLongPressPopup = false }
    ) {
        AppLongPressPopup(app)
    }
}

@Composable
fun AppItemGrid(
    app: Application,
    selected: Boolean,
    onLongClick: ((Application) -> Unit)?,
    longPressPopup: Boolean,
    onClick: ((Application) -> Unit)?
) {
    require(!((onLongClick != null) && (longPressPopup))) {
        "Long press action, or popup, or neither, but not both!"
    }

    val drawerSettings = LocalDrawerSettings.current

    var showLongPressPopup by remember { mutableStateOf(false) }


    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(drawerSettings.iconsSpacingVertical),
        modifier = Modifier
            .clip(MaterialTheme.shapes.large)
            .conditional(selected) {
                background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
            }
            .combinedClickable(
                onLongClick = {
                    if (longPressPopup) showLongPressPopup = true
                    else onLongClick?.invoke(app)
                },
                onClick = { onClick?.invoke(app) }
            )
            .padding(5.dp)
    ) {
        if (drawerSettings.showAppIconsInDrawer) {
            AppIcon(app, drawerSettings.maxIconSize)
        }

        if (drawerSettings.showAppLabelsInDrawer) {
            Text(
                text = app.label,
                color = MaterialTheme.colorScheme.onBackground,
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
        AppLongPressPopup(app)
    }
}

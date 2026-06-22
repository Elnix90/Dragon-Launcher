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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.elnix.dragonlauncher.base.model.models.Application
import org.elnix.dragonlauncher.enumsui.toggle.HorizontalAlignment
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.settings.stores.map.DrawerSettingsStore
import org.elnix.dragonlauncher.ui.actions.AppIcon
import org.elnix.dragonlauncher.ui.base.UiConstants.DragonShape
import io.github.elnix90.runtime.asState
import org.elnix.dragonlauncher.ui.base.components.Spacer
import org.elnix.dragonlauncher.ui.base.modifiers.conditional
import org.elnix.dragonlauncher.ui.dialogs.AppLongPressRow
import org.elnix.dragonlauncher.ui.dragon.components.DragonDropDownMenu


@Composable
private fun CheckIcon() {
    Icon(
        painter = painterResource(R.drawable.check_circle),
        contentDescription = null,
        tint = MaterialTheme.colorScheme.primary,
        modifier = Modifier.background(MaterialTheme.colorScheme.surface, CircleShape)
    )
}

@Composable
fun AppItemHorizontal(
    app: Application,
    selected: Boolean,
    onLongClick: ((Application) -> Unit)?,
    longPressPopup: Boolean,
    onClick: ((Application) -> Unit)?
) {
    require(!((onLongClick != null) and (longPressPopup))) {
        "Long press action, or popup, or neither, but not both!"
    }

    val showAppIconsInDrawer by DrawerSettingsStore.showAppIconsInDrawer.asState()
    val showAppLabelsInDrawer by DrawerSettingsStore.showAppLabelInDrawer.asState()
    val horizontalAlignment by DrawerSettingsStore.horizontalAlignment.asState()
    val iconsSpacingHorizontal by DrawerSettingsStore.iconsSpacingHorizontal.asState()

    var showLongPressPopup by remember { mutableStateOf(false) }

    BadgedBox(
        badge = {
            if (selected) {
                CheckIcon()
            }
        }
    ) {

        val alignment = when(horizontalAlignment) {
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
                        if (longPressPopup) showLongPressPopup = true
                        else onLongClick?.invoke(app)
                    },
                    onClick = { onClick?.invoke(app) }
                )
                .padding(horizontal = 6.dp)
        ) {

            if (showAppIconsInDrawer) {
                AppIcon(app)
            }

            if (showAppLabelsInDrawer) {
                Spacer(iconsSpacingHorizontal)
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
            AppLongPressRow(app)
        }
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
    require(!((onLongClick != null) and (longPressPopup))) {
        "Long press action, or popup, or neither, but not both!"
    }

    val showAppIconsInDrawer by DrawerSettingsStore.showAppIconsInDrawer.asState()
    val iconsSpacingVertical by DrawerSettingsStore.iconsSpacingVertical.asState()


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
                    if (longPressPopup) showLongPressPopup = true
                    else onLongClick?.invoke(app)
                },
                onClick = { onClick?.invoke(app) }
            )
            .padding(5.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(iconsSpacingVertical)
        ) {
            if (showAppIconsInDrawer) {
                AppIcon(app)
            }

            if (showAppIconsInDrawer) {
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
            AppLongPressRow(app)
        }
    }
}

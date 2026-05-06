@file:Suppress("AssignedValueIsNeverRead")

package org.elnix.dragonlauncher.ui.dialogs

import androidx.compose.foundation.Image
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ButtonGroupScope
import androidx.compose.material3.DropdownMenuGroup
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MenuDefaults
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachIndexed
import org.elnix.dragonlauncher.common.R
import org.elnix.dragonlauncher.common.messyfolder.resolveShape
import org.elnix.dragonlauncher.common.serializables.AppModel
import org.elnix.dragonlauncher.ui.actions.appIcon
import org.elnix.dragonlauncher.ui.components.burger.MoreOptions
import org.elnix.dragonlauncher.ui.composition.LocalIconShape

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AppLongPressRow(
    app: AppModel,
    onOpen: () -> Unit,
    onRenameApp: () -> Unit,
    onChangeAppIcon: () -> Unit,
    onAliases: () -> Unit,
    onSettings: (() -> Unit)? = null,
    onUninstall: (() -> Unit)? = null,
    onRemoveFromWorkspace: (() -> Unit)? = null,
    onAddToWorkspace: (() -> Unit)? = null
) {

//    val iconsShape = LocalIconShape.current

    var showDetailedAppInfoDialog by remember { mutableStateOf(false) }

    val entries = buildList {
//        add(
//            MoreOptions(
//                text = { app.name },
//                icon = R.drawable.open_in_new,
//                onClick = onOpen
//            )
//        )
//        onUninstall?.let {
//            add(
//                MoreOptions(
//                    text = { stringResource(R.string.uninstall) },
//                    icon = R.drawable.delete_forever,
//                    onClick = it
//                )
//            )
//        }
        add(
            MoreOptions(
                text = { stringResource(R.string.rename) },
                icon = R.drawable.edit_rounded,
                onClick = onRenameApp
            )
        )
        add(
            MoreOptions(
                text = { stringResource(R.string.change_app_icon) },
                icon = R.drawable.image,
                onClick = onChangeAppIcon
            )
        )
        add(
            MoreOptions(
                text = { stringResource(R.string.app_aliases) },
                icon = R.drawable.alternate_email,
                onClick = onAliases
            )
        )
        onAddToWorkspace?.let {
            add(
                MoreOptions(
                    text = { stringResource(R.string.add_to_workspace) },
                    icon = R.drawable.add_circle,
                    onClick = it
                )
            )
        }
        onRemoveFromWorkspace?.let {
            add(
                MoreOptions(
                    text = { stringResource(R.string.remove_from_workspace) },
                    icon = R.drawable.remove_circle,
                    onClick = it
                )
            )
        }
    }

    Column {
        @Suppress("DEPRECATION")
        ButtonGroup(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(MenuDefaults.GroupSpacing, Alignment.CenterHorizontally),
        ) {

            onSettings?.let {
                Button(onClick = it, icon = R.drawable.settings)
            }

            Button(onClick = { showDetailedAppInfoDialog = true }, icon = R.drawable.info)

            onUninstall?.let {
                Button(onClick = it, icon = R.drawable.delete_forever)
            }
        }

        Spacer(Modifier.height(MenuDefaults.GroupSpacing))


        DropdownMenuGroup(
            shapes = MenuDefaults.groupShapes()
        ) {
            DropdownMenuItem(
                onClick = onOpen,
                shape = MenuDefaults.leadingItemShape,
                text = { Text(app.name) },
                leadingIcon = {
                    Image(
                        painter = appIcon(app),
                        contentDescription = "App icon",
                        modifier = Modifier
                            .size(32.dp)
                            .clip(LocalIconShape.current.resolveShape())
                    )
                }
            )

            entries.fastForEachIndexed { index, option ->
                DropdownMenuItem(
                    onClick = option.onClick,
                    enabled = option.enabled,
                    shape = if (index == entries.lastIndex) {
                        MenuDefaults.trailingItemShape
                    } else {
                        MenuDefaults.middleItemShape
                    },
                    text = { Text(option.text()) },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(option.icon),
                            contentDescription = null
                        )
                    }
                )
            }
        }


        if (showDetailedAppInfoDialog) {
            AppModelInfoDialog(app) { showDetailedAppInfoDialog = false }
        }
    }

}

@Composable
fun rememberInteractionSource(): MutableInteractionSource {
    return remember { MutableInteractionSource() }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ButtonGroupScope.Button(
    onClick: () -> Unit,
    icon: Int
) {
    val interactionSource = rememberInteractionSource()

    Button(
        onClick = onClick,
        interactionSource = interactionSource,
        shapes = ButtonDefaults.shapes(),
        modifier = Modifier
            .weight(1f)
            .animateWidth(interactionSource),
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
        )
    }
}
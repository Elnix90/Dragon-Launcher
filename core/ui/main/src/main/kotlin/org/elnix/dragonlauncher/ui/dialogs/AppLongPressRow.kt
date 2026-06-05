@file:Suppress("AssignedValueIsNeverRead")

package org.elnix.dragonlauncher.ui.dialogs

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.util.fastForEachIndexed
import org.elnix.dragonlauncher.base.model.models.Application
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.models.AppLaunchViewModel
import org.elnix.dragonlauncher.models.AppsViewModel
import org.elnix.dragonlauncher.ui.actions.AppIcon
import org.elnix.dragonlauncher.ui.base.activityViewModel
import org.elnix.dragonlauncher.ui.base.components.Spacer
import org.elnix.dragonlauncher.ui.components.burger.MoreOptions

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AppLongPressRow(
    app: Application,
    appLaunchViewModel: AppLaunchViewModel = activityViewModel(),
    appsViewModel: AppsViewModel = activityViewModel(),
) {
    val ctx = LocalContext.current
    val appOverridesManager = appsViewModel.appOverrideManager
    val workspacesManager = appsViewModel.workspaceManager
    val selectedWorkspaceId by appsViewModel.selectedWorkspaceId.collectAsState()


    var showDetailedAppInfoDialog by remember { mutableStateOf(false) }

    var showRenameDialog by remember { mutableStateOf(false) }
    var showAliasDialog by remember { mutableStateOf(false) }
    var showIconDialog by remember { mutableStateOf(false) }


    val entries = buildList {
        add(
            MoreOptions(
                text = { stringResource(R.string.rename) },
                icon = R.drawable.edit_rounded,
                onClick = { showRenameDialog = true }
            )
        )
        add(
            MoreOptions(
                text = { stringResource(R.string.change_app_icon) },
                icon = R.drawable.image,
                onClick = { showIconDialog = true }
            )
        )
        add(
            MoreOptions(
                text = { stringResource(R.string.app_aliases) },
                icon = R.drawable.alternate_email,
                onClick = { showAliasDialog = true }
            )
        )

        add(
            MoreOptions(
                text = { stringResource(R.string.add_to_workspace) },
                icon = R.drawable.add_circle,
                onClick = {
                    workspacesManager.removeAppFromWorkspace(
                        id = selectedWorkspaceId,
                        cacheKey = app.key
                    )
                }
            )
        )

        add(
            MoreOptions(
                text = { stringResource(R.string.remove_from_workspace) },
                icon = R.drawable.remove_circle,
                onClick = {
                    workspacesManager.addAppToWorkspace(
                        id = selectedWorkspaceId,
                        cacheKey = app.key
                    )
                }
            )
        )
    }

    Column {
        @Suppress("DEPRECATION")
        ButtonGroup(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(MenuDefaults.GroupSpacing, Alignment.CenterHorizontally),
        ) {

            Button(
                onClick = { app.openAppDetails(ctx) },
                icon = R.drawable.settings
            )

            Button(
                onClick = { showDetailedAppInfoDialog = true },
                icon = R.drawable.info
            )

            if (!app.isPrivate) {
                Button(
                    onClick = { app.uninstall(ctx) },
                    icon = R.drawable.delete_forever
                )
            }
        }

        Spacer(MenuDefaults.GroupSpacing)


        DropdownMenuGroup(
            shapes = MenuDefaults.groupShapes()
        ) {
            DropdownMenuItem(
                onClick = { appLaunchViewModel.requestAppLaunch(app) },//{ app.launch(ctx, null) },
                shape = MenuDefaults.leadingItemShape,
                text = { Text(app.label) },
                leadingIcon = { AppIcon(app) }
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
            ApplicationInfoDialog(app) { showDetailedAppInfoDialog = false }
        }
    }



    if (showRenameDialog) {
        val cacheKey = app.key

        TextEditorDialog(
            title = { stringResource(R.string.rename) },
            placeHolder = { app.label },
            onDismiss = { showRenameDialog = false },
            initialText = app.label
        ) {
            if (it != "") {
                appOverridesManager.renameApp(
                    cacheKey = cacheKey,
                    customName = it
                )
            } else {
                appOverridesManager.renameApp(cacheKey, null)
            }
            showRenameDialog = false
        }
    }

    if (showIconDialog) {
        val cacheKey = app.key

        val iconService = appsViewModel.iconsService

        AppIconEditor(
            app = app,
            onReset = { iconService.reloadAppIcon(app) },
            onDismiss = { showIconDialog = false }
        ) { customIcon ->

            if (customIcon != null) {
                appOverridesManager.setAppIcon(
                    cacheKey = cacheKey,
                    customIcon = customIcon
                )
            } else {
                appOverridesManager.setAppIcon(cacheKey, null)
            }

            showIconDialog = false
           iconService.reloadAppIcon(app)
        }
    }

    if (showAliasDialog) {
        AppAliasesDialog(
            app = app,
            onDismiss = { showAliasDialog = false }
        )
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
@file:Suppress("AssignedValueIsNeverRead")

package org.elnix.dragonlauncher.ui.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ButtonGroupDefaults
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachIndexed
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.base.model.models.Application
import org.elnix.dragonlauncher.enumsui.select.LocalWorkspaceViewMode
import org.elnix.dragonlauncher.enumsui.select.WorkspaceViewMode
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.ktx.showToast
import org.elnix.dragonlauncher.models.AppLaunchViewModel
import org.elnix.dragonlauncher.models.DrawerViewModel
import org.elnix.dragonlauncher.ui.actions.AppIcon
import org.elnix.dragonlauncher.ui.base.activityViewModel
import org.elnix.dragonlauncher.ui.base.asState
import org.elnix.dragonlauncher.ui.base.components.Spacer
import org.elnix.dragonlauncher.ui.base.remember.rememberInteractionSource
import org.elnix.dragonlauncher.ui.components.burger.MoreOptions
import org.elnix.dragonlauncher.ui.dialogs.editors.AppIconEditor

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AppLongPressPopup(
    app: Application,
    appLaunchViewModel: AppLaunchViewModel = activityViewModel(),
    drawerViewModel: DrawerViewModel = activityViewModel(),
    close: () -> Unit
) {
    val ctx = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val workspaceViewMode = LocalWorkspaceViewMode.current

    val scope = rememberCoroutineScope()

    val appOverridesManager = drawerViewModel.appOverrideManager
    val appOverrides by appOverridesManager.appOverrides.asState()
    val workspacesManager = drawerViewModel.workspaceManager
    val selectedWorkspaceId by drawerViewModel.selectedWorkspaceId.collectAsState()


    var showDetailedAppInfoDialog by remember { mutableStateOf(false) }

    var showRenameDialog by remember { mutableStateOf(false) }
    var showAliasDialog by remember { mutableStateOf(false) }
    var showIconDialog by remember { mutableStateOf(false) }

    val installerStoreLink = remember { app.getStoreDetails(ctx) }


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

        if (workspaceViewMode == WorkspaceViewMode.Removed) {
            add(
                MoreOptions(
                    text = { stringResource(R.string.add_to_workspace) },
                    icon = R.drawable.add_circle,
                    onClick = {
                        workspacesManager.addAppToWorkspace(
                            id = selectedWorkspaceId,
                            cacheKey = app.key
                        )
                        close()
                    }
                )
            )
        } else {
            add(
                MoreOptions(
                    text = { stringResource(R.string.remove_from_workspace) },
                    icon = R.drawable.remove_circle,
                    onClick = {
                        workspacesManager.removeAppFromWorkspace(
                            id = selectedWorkspaceId,
                            cacheKey = app.key
                        )
                        close()
                    }
                )
            )
        }

        add(
            MoreOptions(
                text = { stringResource(R.string.export_apk) },
                icon = R.drawable.share,
                onClick = {
                    scope.launch {
                        app.shareApkFile(ctx)
                        close()
                    }
                }
            )
        )

        installerStoreLink?.let { link ->
            add(
                MoreOptions(
                    text = { link.label },
                    icon = link.icon,
                    onClick = {
                        scope.launch {
                            uriHandler.openUri(link.url)
                            close()
                        }
                    }
                )
            )
        }

        add(
            MoreOptions(
                text = { stringResource(R.string.detailed_info) },
                icon = R.drawable.info,
                onClick = {
                    showDetailedAppInfoDialog = true
                }
            )
        )
    }

    val cannotMessage = stringResource(R.string.cannot_directly_uninstall_from_other_profiles)

    Column {
        ButtonGroup(
            overflowIndicator = { menuState -> ButtonGroupDefaults.OverflowIndicator(menuState) },
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(MenuDefaults.GroupSpacing, Alignment.CenterHorizontally),
        ) {

            customItem(
                buttonGroupContent = {
                    Button(
                        onClick = { appLaunchViewModel.requestAppLaunch(app) },
                        icon = R.drawable.open_in_new
                    )
                },
                menuContent = {}
            )
            customItem(
                buttonGroupContent = {
                    Button(
                        onClick = { app.openAppDetails(ctx) },
                        icon = R.drawable.settings
                    )
                },
                menuContent = {}
            )
            customItem(
                buttonGroupContent = {
                    Button(
                        onClick = {
                            if (!app.isPrivate) {
                                app.uninstall(ctx)
                            } else {
                                ctx.showToast(cannotMessage)
                                app.openAppDetails(ctx)
                            }
                        },
                        icon = R.drawable.delete_forever
                    )
                },
                menuContent = { }
            )
        }

        Spacer(MenuDefaults.GroupSpacing)


        DropdownMenuGroup(
            shapes = MenuDefaults.groupShapes()
        ) {
            DropdownMenuItem(
                onClick = { appLaunchViewModel.requestAppLaunch(app) },
                shape = MenuDefaults.leadingItemShape,
                text = { Text(app.label) },
                leadingIcon = { AppIcon(app, maxSize = 35.dp) }
            )

            entries.fastForEachIndexed { index, option ->
                DropdownMenuItem(
                    onClick = option.onClick,
                    enabled = option.enabled,
                    shape = if (index == entries.lastIndex && installerStoreLink == null) {
                        MenuDefaults.trailingItemShape
                    } else {
                        MenuDefaults.middleItemShape
                    },
                    text = { Text(option.text()) },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(option.icon),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
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
            defaultText = app.defaultLabel,
            initialText = app.label
        ) { newName ->
            appOverridesManager.renameApp(
                cacheKey = cacheKey,
                customName = newName
            )
            showRenameDialog = false
        }
    }

    if (showIconDialog) {
        AppIconEditor(app) { showIconDialog = false }
    }

    if (showAliasDialog) {
        AppAliasesDialog(app) { showAliasDialog = false }
    }
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
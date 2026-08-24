package org.elnix.dragonlauncher.ui.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.elnix.dragonlauncher.base.model.serializables.Action
import org.elnix.dragonlauncher.base.model.serializables.Workspace
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.models.DrawerViewModel
import org.elnix.dragonlauncher.ui.base.activityViewModel
import org.elnix.dragonlauncher.ui.base.components.LazyColumnWithScrollIndicator
import org.elnix.dragonlauncher.ui.base.components.Spacer
import org.elnix.dragonlauncher.ui.dragon.components.DragonGroupScope
import org.elnix.dragonlauncher.ui.dragon.components.DragonModalBottomSheet
import org.elnix.dragonlauncher.ui.dragon.components.DragonSettingsGroup
import org.elnix.dragonlauncher.ui.dragon.text.DialogDescription
import org.elnix.dragonlauncher.ui.dragon.text.DialogTitle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkspacePickerDialog(
    drawerViewModel: DrawerViewModel = activityViewModel(),
    onDismiss: () -> Unit,
    onActionPicked: (Action.OpenAppDrawer) -> Unit
) {
    val activeWorkspaces by drawerViewModel.activeWorkspaces.collectAsState()
    val workspacesDisplayed = remember(activeWorkspaces) {
        mutableListOf<Workspace?>(null).apply {
            addAll(activeWorkspaces)
        }
    }

    DragonModalBottomSheet(onDismissRequest = onDismiss) {
        DialogTitle(stringResource(R.string.select_default_workspace))
        DialogDescription(stringResource(R.string.select_workspace_hint))

        Spacer(10.dp)

        DragonSettingsGroup {
            LazyColumnWithScrollIndicator(
                items = workspacesDisplayed,
                modifier = Modifier.heightIn(max = 500.dp)
            ) { workspace ->
                WorkspaceCard(workspace) {
                    onActionPicked(Action.OpenAppDrawer(workspace?.id))
                }
            }
        }
    }
}

@Composable
private fun DragonGroupScope.WorkspaceCard(
    workspace: Workspace?,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .dragonSettingGroup {
                clickable(onClick = onClick)
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Icon(
            painter = painterResource(workspace?.type?.icon ?: R.drawable.account_tree),
            contentDescription = null
        )

        Text(
            text = workspace?.id ?: stringResource(R.string.default_text),
            style = MaterialTheme.typography.labelMedium
        )
    }
}
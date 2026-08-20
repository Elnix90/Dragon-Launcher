package org.elnix.dragonlauncher.ui.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
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
import org.elnix.dragonlauncher.ui.dragon.dialogs.CustomAlertDialog
import org.elnix.dragonlauncher.ui.dragon.text.DialogDescription
import org.elnix.dragonlauncher.ui.dragon.text.DialogTitle

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

    CustomAlertDialog(
        modifier = Modifier.padding(40.dp),
        onDismissRequest = onDismiss,
        alignment = Alignment.Center,
        scroll = false,
        title = {
            Column(
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                DialogTitle(stringResource(R.string.select_default_workspace))
                DialogDescription(stringResource(R.string.select_workspace_hint))
            }
        },
        text = {
            LazyColumnWithScrollIndicator(
                items = workspacesDisplayed,
                modifier = Modifier.heightIn(max = 500.dp)
            ) { workspace ->
                WorkspaceCard(workspace) {
                    onActionPicked(Action.OpenAppDrawer(workspace?.id))
                }
            }
        }
    )
}

@Composable
private fun WorkspaceCard(
    workspace: Workspace?,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 5.dp),
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

}
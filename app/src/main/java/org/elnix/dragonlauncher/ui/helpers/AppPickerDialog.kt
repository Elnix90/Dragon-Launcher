package org.elnix.dragonlauncher.ui.helpers

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.elnix.dragonlauncher.data.SwipeActionSerializable
import org.elnix.dragonlauncher.utils.AppDrawerViewModel
import org.elnix.dragonlauncher.utils.actions.actionIcon

@Composable
fun AppPickerDialog(
    viewModel: AppDrawerViewModel,
    onDismiss: () -> Unit,
    onAppSelected: (SwipeActionSerializable.LaunchApp) -> Unit
) {
    val apps by viewModel.userApps.collectAsState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select App") },
        text = {
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier.height(350.dp),
                content = {
                    items(apps.size) { index ->
                        val app = apps[index]

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    onAppSelected(SwipeActionSerializable.LaunchApp(app.packageName))
                                    onDismiss()
                                }
                                .padding(8.dp)
                        ) {
                            Image(
                                painter = actionIcon(app.action),
                                contentDescription = app.name,
                                modifier = Modifier.size(48.dp)
                            )

                            Spacer(Modifier.height(6.dp))

                            Text(
                                text = app.name,
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            )
        },
        confirmButton = {},
        containerColor = MaterialTheme.colorScheme.surface
    )
}



package org.elnix.dragonlauncher.ui.dialogs

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import org.elnix.dragonlauncher.common.serializables.IconPackInfo
import org.elnix.dragonlauncher.models.AppsViewModel
import org.elnix.dragonlauncher.ui.activityViewModel
import org.elnix.dragonlauncher.ui.helpers.IconPackListContent

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun IconPackPickerDialog(
    appsViewModel: AppsViewModel = activityViewModel(),
    onDismiss: () -> Unit,
    onIconPicked: (drawableName: String, packName: String) -> Unit
) {
    var showIconPickerDialog by remember { mutableStateOf<IconPackInfo?>(null) }

    val packs by appsViewModel.iconPacksList.collectAsState()

    LaunchedEffect(Unit) {
        appsViewModel.loadIconPacks()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                IconPackListContent(
                    packs = packs,
                    selectedPackPackage = null,
                    showClearOption = false,
                    onReloadPacks = {
                        appsViewModel.loadIconPacks()
                    },
                    onPackClick = { pack ->
                        appsViewModel.loadAllIconsMappingsFromPack(pack)
                        showIconPickerDialog = pack
                    },
                    onClearClick = {}
                )

            }
        },
        confirmButton = {},
        containerColor = MaterialTheme.colorScheme.surface
    )

    if (showIconPickerDialog != null) {
        val pack = showIconPickerDialog!!
        IconPickerListDialog(
            pack = pack,
            onDismiss = onDismiss,
            onIconSelected = {
                name ->
                onIconPicked(name, pack.packageName)
            }
        )
    }
}

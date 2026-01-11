@file:Suppress("AssignedValueIsNeverRead")

package org.elnix.dragonlauncher.ui.components.dialogs

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.dp
import org.elnix.dragonlauncher.ui.drawer.IconPackInfo
import org.elnix.dragonlauncher.ui.helpers.iconPackListContent
import org.elnix.dragonlauncher.utils.models.AppsViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun IconPackPickerDialog(
    appsViewModel: AppsViewModel,
    onDismiss: () -> Unit,
    onIconPicked: (ImageBitmap) -> Unit
) {
    var showIconPickerDialog by remember { mutableStateOf<IconPackInfo?>(null) }

    val icons by appsViewModel.icons.collectAsState()
    val packs by appsViewModel.iconPacksList.collectAsState()

    LaunchedEffect(Unit) {
        appsViewModel.loadIconsPacks()
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                iconPackListContent(
                    packs = packs,
                    icons = icons,
                    selectedPackPackage = null,
                    showClearOption = false,
                    onReloadPacks = {
                        appsViewModel.loadIconsPacks()
                    },
                    onPackClick = { pack ->
                        appsViewModel.loadAllIconsFromPack(pack)
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
            appsViewModel = appsViewModel,
            pack = pack,
            onDismiss = onDismiss,
            onIconSelected = {
                _, iconBitmap ->
                onIconPicked(iconBitmap)
            }
        )
    }
}

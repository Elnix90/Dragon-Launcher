package org.elnix.dragonlauncher.ui.components.dialogs

import android.graphics.Bitmap
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.elnix.dragonlauncher.R
import org.elnix.dragonlauncher.ui.drawer.IconPackInfo
import org.elnix.dragonlauncher.utils.actions.loadDrawableAsBitmap
import org.elnix.dragonlauncher.utils.colors.AppObjectsColors
import org.elnix.dragonlauncher.utils.colors.adjustBrightness
import org.elnix.dragonlauncher.utils.models.AppsViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun IconPickerListDialog(
    appsViewModel: AppsViewModel,
    pack: IconPackInfo,
    onDismiss: () -> Unit,
    onIconSelected: (iconName: String, icon: ImageBitmap) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val drawableNames by appsViewModel.packIcons.collectAsState()

    val filteredDrawables = remember(searchQuery, drawableNames) {
        if (searchQuery.isBlank()) drawableNames
        else drawableNames.filter {
            it.contains(searchQuery, ignoreCase = true)
        }
    }

    CustomAlertDialog(
        imePadding = false,
        scroll = false,
        alignment = Alignment.Center,
        modifier = Modifier
            .padding(32.dp)
            .height(500.dp),
        onDismissRequest = onDismiss,
        title = {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    modifier = Modifier
                        .height(75.dp)
                ) {
                    Text(
                        text = "Select Icon",
                        modifier = Modifier.wrapContentWidth(),
                        style = MaterialTheme.typography.titleMedium,
                    )


                    TextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        singleLine = true,
                        trailingIcon = {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = null,
                                modifier = Modifier.clickable {
                                    searchQuery = ""
                                }
                            )
                        },
                        placeholder = { Text("Search drawableNames") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(CircleShape),
                        colors = AppObjectsColors.outlinedTextFieldColors(
                            removeBorder = true,
                            backgroundColor = MaterialTheme.colorScheme.surface.adjustBrightness(
                                0.8f
                            )
                        )
                    )
                }
            }
        },
        text = {
            if (filteredDrawables.isNotEmpty()) {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(72.dp),
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredDrawables) { filteredDrawable ->
                        IconCell(
                            appsViewModel = appsViewModel,
                            pack = pack,
                            drawableName = filteredDrawable,
                            onClick = { bitmap ->
                                onIconSelected(filteredDrawable, bitmap.asImageBitmap())
                                onDismiss()
                            }
                        )
                    }
                }
            } else if (drawableNames.isNotEmpty()) {

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(stringResource(R.string.no_search_match))

                }
            } else {

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        },
        confirmButton = {},
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(12.dp)
    )
}


@Composable
private fun IconCell(
    appsViewModel: AppsViewModel,
    pack: IconPackInfo,
    drawableName: String,
    onClick: (Bitmap) -> Unit
) {

    val bitmap by produceState<ImageBitmap?>(null, drawableName) {
        value = withContext(Dispatchers.IO) {
            appsViewModel.loadIconFromPack(pack.packageName, drawableName)
                ?.let { loadDrawableAsBitmap(it, 96, 96) }
        }
    }

    bitmap?.let {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(4.dp)
        ){
            Icon(
                bitmap = it,
                contentDescription = null,
                modifier = Modifier.clickable {
                    onClick(it.asAndroidBitmap())
                }
            )
            Text(
                text = drawableName,
                style = MaterialTheme.typography.bodySmall,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(top = 4.dp)
                    .fillMaxWidth(),
                maxLines = 1
            )
        }

    } ?: run {
        Box(
            modifier = Modifier
                .height(48.dp)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}

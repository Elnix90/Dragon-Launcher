package org.elnix.dragonlauncher.ui.dialogs

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.base.ICONS_TAG
import org.elnix.dragonlauncher.common.serializables.IconPackInfo
import org.elnix.dragonlauncher.base.util.ImageUtils.loadDrawableAsBitmap
import org.elnix.dragonlauncher.logging.logW
import org.elnix.dragonlauncher.models.AppsViewModel
import org.elnix.dragonlauncher.ui.activityViewModel
import org.elnix.dragonlauncher.ui.base.UiConstants.DragonShape
import org.elnix.dragonlauncher.ui.base.modifiers.shapedClickable
import org.elnix.dragonlauncher.ui.dragon.dialogs.CustomAlertDialog
import org.elnix.dragonlauncher.ui.helpers.AppDrawerSearch

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun IconPickerListDialog(
    appsViewModel: AppsViewModel = activityViewModel(),
    pack: IconPackInfo,
    onDismiss: () -> Unit,
    onIconSelected: (iconName: String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val drawableNames by appsViewModel.packIcons.collectAsState()

    val filteredDrawables by remember(searchQuery, drawableNames) {
        derivedStateOf {
            val drawables = if (searchQuery.isBlank()) drawableNames
            else drawableNames.filter {
                it.contains(searchQuery, ignoreCase = true)
            }
            drawables.sorted()
        }
    }

    val iconPackTint by appsViewModel.packTint.collectAsState()

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

                AppDrawerSearch(
                    searchQuery = searchQuery,
                    placeholderText = stringResource(R.string.select_icon),
                    trailingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.close),
                            contentDescription = null,
                            modifier = Modifier.shapedClickable {
                                searchQuery = ""
                            }
                        )
                    },
                    onSearchChanged = { searchQuery = it }
                )
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
                            pack = pack,
                            drawableName = filteredDrawable,
                            packTint = iconPackTint,
                            onClick = {
                                onIconSelected(filteredDrawable)
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
                    LoadingIndicator()
                }
            }
        }
    )
}


@Composable
private fun IconCell(
    appsViewModel: AppsViewModel = activityViewModel(),
    pack: IconPackInfo,
    drawableName: String,
    packTint: Int?,
    onClick: () -> Unit
) {
    val bitmap by produceState<ImageBitmap?>(null, drawableName) {
        value = withContext(Dispatchers.IO) {
            appsViewModel.loadIconFromPack(pack.packageName, drawableName, "")
                ?.let {
                    logW(ICONS_TAG) { " Failed to load icon pack.packageName = ${pack.packageName}, drawableName = $drawableName the normal way, trying to resolve it via loadDrawableAsBitmap" }
                    loadDrawableAsBitmap(it, 96, 96, packTint)
                }
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(DragonShape)
            .clickable(onClick = onClick)
            .padding(4.dp)
    ) {
        bitmap?.let {
            Icon(
                bitmap = it,
                contentDescription = null,
                tint = Color.Unspecified
            )
        } ?: run {
            // Empty box, as the loading is fast and don't need to display an indicator everytime
            Box(
                modifier = Modifier
                    .height(48.dp)
                    .fillMaxWidth()
            )
        }

        Text(
            text = drawableName,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(top = 4.dp)
                .fillMaxWidth(),
            maxLines = 1
        )
    }
}

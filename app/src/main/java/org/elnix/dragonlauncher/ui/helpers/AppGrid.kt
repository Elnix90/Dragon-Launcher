package org.elnix.dragonlauncher.ui.helpers

import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.elnix.dragonlauncher.ui.drawer.AppItem
import org.elnix.dragonlauncher.ui.drawer.AppModel
import org.elnix.dragonlauncher.utils.actions.appIcon

@Composable
fun AppGrid(
    apps: List<AppModel>,
    icons: Map<String, ImageBitmap>,
    gridSize: Int,
    txtColor: Color,
    showIcons: Boolean,
    showLabels: Boolean,
    onLongClick: ((AppModel) -> Unit)? = null,
    onClick: (AppModel) -> Unit
) {
    if (gridSize == 1) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectVerticalDragGestures { _, _ -> }
                }
        ) {
            items(apps, key = { it.packageName }) { app ->
                AppItem(
                    app = app,
                    showIcons = showIcons,
                    showLabels = showLabels,
                    icons = icons,
                    onClick = { onClick(app) },
                    onLongClick = if (onLongClick != null) { { onLongClick(app) } } else null
                )
            }
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(gridSize),
            modifier = Modifier.fillMaxSize()
        ) {
            items(apps.size, ) { index ->
                val app = apps[index]

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .combinedClickable(
                            onLongClick = if (onLongClick != null) { { onLongClick(app)} } else null
                        ) { onClick(app) }
                        .padding(8.dp)
                ) {
                    if (showIcons) {
                        Image(
                            painter = appIcon(app.packageName, icons),
                            contentDescription = app.name,
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f),
                            contentScale = ContentScale.Crop

                        )
                    }

                    if (showLabels) {
                        Spacer(Modifier.height(6.dp))

                        Text(
                            text = app.name,
                            color = txtColor,
                            style = MaterialTheme.typography.labelSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}
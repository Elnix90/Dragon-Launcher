@file:Suppress("AssignedValueIsNeverRead", "DEPRECATION")

package org.elnix.dragonlauncher.ui.components.dialogs

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView
import org.elnix.dragonlauncher.data.helpers.CustomIconSerializable
import org.elnix.dragonlauncher.data.helpers.IconType
import org.elnix.dragonlauncher.utils.models.AppsViewModel

@Composable
fun IconPickerDialog(
    appsViewModel: AppsViewModel,
    onDismiss: () -> Unit,
    onPicked: (CustomIconSerializable?) -> Unit
) {
    val ctx = LocalContext.current

    val iconPacks by appsViewModel.selectedIconPack.collectAsState()
    val icons by appsViewModel.icons.collectAsState()

    var selectedIcon by remember { mutableStateOf<CustomIconSerializable?>(null) }
    var textValue by remember { mutableStateOf("") }

    val imagePicker = rememberLauncherForActivityResult(
        CropImageContract()
    ) { result ->
        if (result.isSuccessful) {
            val uri = result.uriContent
            if (uri != null) {
                selectedIcon = CustomIconSerializable(
                    type = IconType.BITMAP,
                    source = uri.toString()
                )
            }
        }
    }

    CustomAlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Icon Picker",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { onPicked(null) }) {
                    Icon(Icons.Default.Close, null)
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = selectedIcon != null,
                onClick = { onPicked(selectedIcon) }
            ) {
                Text("Apply")
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(520.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                /* IMAGE PICKER */
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable {
                            imagePicker.launch(
                                CropImageContractOptions(
                                    uri = null,
                                    cropImageOptions = CropImageOptions(
                                        cropShape = CropImageView.CropShape.RECTANGLE,
                                        fixAspectRatio = true,
                                        aspectRatioX = 1,
                                        aspectRatioY = 1
                                    )
                                )
                            )
                        }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Image, null)
                    Spacer(Modifier.width(12.dp))
                    Text("Pick image (crop)")
                }

                /* TEXT / EMOJI */
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(12.dp)
                ) {
                    Text("Text / Emoji", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    TextField(
                        value = textValue,
                        onValueChange = {
                            textValue = it
                            selectedIcon =
                                if (it.isNotBlank())
                                    CustomIconSerializable(
                                        type = IconType.TEXT,
                                        source = it
                                    )
                                else null
                        },
                        placeholder = { Text("😀  A  ★") },
                        singleLine = true
                    )
                }

                /* ICON PACK / APP ICONS */
                if (icons.isNotEmpty()) {
                    Text(
                        text = "Available Icons",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )

                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(56.dp),
                        modifier = Modifier.height(260.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(icons.entries.toList()) { entry ->
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (selectedIcon?.source == entry.key)
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                        else Color.Transparent
                                    )
                                    .clickable {
                                        selectedIcon = CustomIconSerializable(
                                            type = IconType.VECTOR,
                                            source = entry.key
                                        )
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = BitmapPainter(entry.value),
                                    contentDescription = null,
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.surface
    )
}

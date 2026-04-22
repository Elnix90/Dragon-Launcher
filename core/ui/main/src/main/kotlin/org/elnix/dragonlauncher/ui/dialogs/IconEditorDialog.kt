@file:Suppress("AssignedValueIsNeverRead", "DEPRECATION")

package org.elnix.dragonlauncher.ui.dialogs

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.base.ColorUtils.alphaMultiplier
import org.elnix.dragonlauncher.base.ColorUtils.definedOrNull
import org.elnix.dragonlauncher.common.R
import org.elnix.dragonlauncher.common.serializables.AppModel
import org.elnix.dragonlauncher.common.serializables.CustomIconSerializable
import org.elnix.dragonlauncher.common.serializables.IconType
import org.elnix.dragonlauncher.common.serializables.SwipePointSerializable
import org.elnix.dragonlauncher.common.utils.ImageUtils.uriToBase64
import org.elnix.dragonlauncher.theme.AppObjectsColors
import org.elnix.dragonlauncher.ui.actions.AppIcon
import org.elnix.dragonlauncher.ui.base.UiConstants.DragonShape
import org.elnix.dragonlauncher.ui.components.PointPreviewCanvas
import org.elnix.dragonlauncher.ui.composition.LocalAppsViewModel
import org.elnix.dragonlauncher.ui.composition.LocalDefaultPoint
import org.elnix.dragonlauncher.ui.composition.LocalIconShape
import org.elnix.dragonlauncher.ui.dragon.colors.ColorPickerRow
import org.elnix.dragonlauncher.ui.dragon.components.DragonColumnGroup
import org.elnix.dragonlauncher.ui.dragon.components.DragonIconButton
import org.elnix.dragonlauncher.ui.dragon.components.SliderWithLabel
import org.elnix.dragonlauncher.ui.dragon.components.ValidateCancelButtons
import org.elnix.dragonlauncher.ui.dragon.dialogs.CustomAlertDialog
import org.elnix.dragonlauncher.ui.dragon.text.TextDivider
import org.elnix.dragonlauncher.ui.helpers.ShapeRow


@Composable
fun PointIconEditorDialog(
    point: SwipePointSerializable,
    onReset: (() -> Unit)? = null,
    onDismiss: () -> Unit,
    onPicked: (CustomIconSerializable?) -> Unit
) {
    val defaultPoint = LocalDefaultPoint.current
    val appsViewModel = LocalAppsViewModel.current

    var selectedIcon by remember { mutableStateOf(point.customIcon) }


    val previewPoint = point.copy(customIcon = selectedIcon)


    IconEditorDialog(
        customIcon = point.customIcon,
        onDismiss = onDismiss,
        onReset = onReset,
        preview = {
                PointPreviewCanvas(
                    editPoint = previewPoint,
                    defaultPoint = defaultPoint,
                    backgroundSurfaceColor = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.weight(1f)
                )
        },
        onUpdate = {
            selectedIcon = it
            appsViewModel.reloadPointIcon(point.copy(customIcon = selectedIcon))
        },
        onPicked = onPicked
    )
}

@Composable
fun AppIconEditorDialog(
    app: AppModel,
    onReset: (() -> Unit)? = null,
    onDismiss: () -> Unit,
    onPicked: (CustomIconSerializable?) -> Unit
) {
    val appsViewModel = LocalAppsViewModel.current

    val workspaceState by appsViewModel.state.collectAsState()
    val appOverrides = workspaceState.appOverrides

    val customIcon = appOverrides[app.iconCacheKey]?.customIcon

    var selectedIcon by remember { mutableStateOf(customIcon) }


    IconEditorDialog(
        customIcon = customIcon,
        onDismiss = onDismiss,
        onReset = onReset,
        preview = {
            AppIcon(app, 50.dp)
        },
        onUpdate = {
            selectedIcon = it
            appsViewModel.reloadAppIcon(app, selectedIcon)
        },
        onPicked = onPicked
    )
}

@Composable
private fun IconEditorDialog(
    customIcon: CustomIconSerializable?,
    onReset: (() -> Unit)? = null,
    onDismiss: () -> Unit,
    preview: @Composable RowScope.() -> Unit,
    onUpdate: (CustomIconSerializable?) -> Unit,
    onPicked: (CustomIconSerializable?) -> Unit
) {
    val ctx = LocalContext.current
    val iconShapes = LocalIconShape.current
    val scope = rememberCoroutineScope()

    var selectedIcon by remember { mutableStateOf(customIcon) }


    fun updateSelectedIcon(newIcon: CustomIconSerializable?) {
        onUpdate(newIcon)
        selectedIcon = newIcon
    }

    var textValue by remember { mutableStateOf("") }
    val source = selectedIcon?.source

    LaunchedEffect(Unit) {
        if (selectedIcon?.type == IconType.TEXT) {
            textValue = source ?: ""
        }
    }


    var showIconPackPicker by remember { mutableStateOf(false) }
    var showShapePickerDialog by remember { mutableStateOf(false) }


    val cropLauncher = rememberLauncherForActivityResult(
        CropImageContract()
    ) { result ->
        val uri = result.uriContent ?: return@rememberLauncherForActivityResult

        scope.launch {
            val base64 = uriToBase64(ctx, uri)
            updateSelectedIcon(
                (selectedIcon ?: CustomIconSerializable()).copy(
                    type = IconType.BITMAP,
                    source = base64
                )
            )
        }
    }

    val imagePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri ?: return@rememberLauncherForActivityResult

        cropLauncher.launch(
            CropImageContractOptions(
                uri,
                cropImageOptions = CropImageOptions(
                    cropShape = CropImageView.CropShape.RECTANGLE,
                    fixAspectRatio = true,
                    aspectRatioX = 1,
                    aspectRatioY = 1,
                    guidelines = CropImageView.Guidelines.ON
                )
            )
        )
    }

    CustomAlertDialog(
        modifier = Modifier
            .padding(24.dp),
        onDismissRequest = onDismiss,
        imePadding = false,
        alignment = Alignment.Center,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.icon_editor),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleLarge
                )

                preview()


                DragonIconButton(
                    onClick = {
                        updateSelectedIcon(null)
                        onReset?.invoke()
                        textValue = ""
                    },
                    colors = AppObjectsColors.iconButtonColors(),
                    imageVector = Icons.Default.Restore,
                    contentDescription = stringResource(R.string.reset)
                )
            }
        },
        confirmButton = {
            ValidateCancelButtons(
                onCancel = onDismiss,
            ) { onPicked(selectedIcon) }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextDivider(stringResource(R.string.source))

                DragonColumnGroup {
                    Row(horizontalArrangement = Arrangement.SpaceEvenly) {
                        Column(
                            verticalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth(0.5f) // Hacky lol
                        ) {
                            SelectableCard(
                                selected = selectedIcon?.type == IconType.BITMAP && source != null,
                                onClick = {
                                    imagePicker.launch(arrayOf("image/*"))
                                    textValue = ""
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Image,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    text = stringResource(R.string.pick_image),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }


                            SelectableCard(
                                selected = selectedIcon?.type == IconType.ICON_PACK && source != null,
                                onClick = {
                                    showIconPackPicker = true
                                    textValue = ""
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Palette,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    text = stringResource(R.string.pick_from_icon_pack),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        SelectableCard(
                            modifier = Modifier.fillMaxWidth(),
                            selected = selectedIcon?.type == IconType.TEXT && source != null,
                            onClick = { }
                        ) {
                            Column(
                                modifier = Modifier
                                    .clip(DragonShape)
                                    .background(MaterialTheme.colorScheme.surface.alphaMultiplier(0.7f))
                                    .padding(12.dp)
                            ) {
                                Text(
                                    stringResource(R.string.text_emoji),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(Modifier.height(8.dp))
                                TextField(
                                    value = textValue,
                                    onValueChange = {
                                        textValue = it
                                        updateSelectedIcon(
                                            if (it.isNotBlank()) {
                                                (selectedIcon ?: CustomIconSerializable()).copy(
                                                    type = IconType.TEXT,
                                                    source = it
                                                )
                                            } else {
                                                null
                                            }
                                        )
                                    },
                                    placeholder = { Text("😀  A  ★") },
                                    singleLine = true,
                                    colors = AppObjectsColors.outlinedTextFieldColors(
                                        removeBorder = true,
                                        backgroundColor = MaterialTheme.colorScheme.surface
                                    )
                                )
                            }
                        }
                    }

                    SelectableCard(
                        selected = selectedIcon?.type == IconType.PLAIN_COLOR && source != null,
                        onClick = {}
                    ) {
                        val currentColor = run {
                            source
                                ?.takeIf { selectedIcon?.type == IconType.PLAIN_COLOR }
                                ?.let { Color(it.toInt()) }
                        } ?: Color.Black

                        ColorPickerRow(
                            label = stringResource(R.string.plain_color),
                            currentColor = currentColor
                        ) { newColor ->
                            newColor?.let {
                                updateSelectedIcon(
                                    (selectedIcon ?: CustomIconSerializable()).copy(
                                        type = IconType.PLAIN_COLOR,
                                        source = it.toArgb().toString()
                                    )
                                )
                            } ?: run {
                                updateSelectedIcon(
                                    (selectedIcon ?: CustomIconSerializable()).copy(
                                        type = null,
                                        source = null
                                    )
                                )
                            }
                        }
                    }

                    SelectableCard(
                        selected = selectedIcon?.type == null || source == null,
                        onClick = {
                            updateSelectedIcon(
                                selectedIcon?.copy(
                                    type = null,
                                    source = null
                                )
                            )
                            textValue = ""
                        }
                    ) {
                        Icon(
                            Icons.Default.Close,
                            null,
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = stringResource(R.string.no_custom_icon),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }


                TextDivider(stringResource(R.string.appearance))


                DragonColumnGroup {

                    // Opacity
                    SliderWithLabel(
                        label = stringResource(R.string.opacity),
                        value = selectedIcon?.opacity ?: 1f,
                        valueRange = 0f..1f,
                        color = MaterialTheme.colorScheme.primary,
                        onReset = {
                            updateSelectedIcon(selectedIcon?.copy(opacity = null))
                        }
                    ) {
                        updateSelectedIcon((selectedIcon ?: CustomIconSerializable()).copy(opacity = it))
                    }

                    // Rotation
                    SliderWithLabel(
                        label = stringResource(R.string.rotation),
                        value = selectedIcon?.rotationDeg ?: 0f,
                        valueRange = -180f..180f,
                        color = MaterialTheme.colorScheme.primary,
                        onReset = {
                            updateSelectedIcon(selectedIcon?.copy(rotationDeg = null))
                        }
                    ) {
                        updateSelectedIcon((selectedIcon ?: CustomIconSerializable()).copy(rotationDeg = it))
                    }

                    // Scale X
                    SliderWithLabel(
                        label = stringResource(R.string.scale_x),
                        value = selectedIcon?.scaleX ?: 1f,
                        valueRange = 0.2f..3f,
                        color = MaterialTheme.colorScheme.primary,
                        onReset = {
                            updateSelectedIcon(selectedIcon?.copy(scaleX = null))
                        }
                    ) {
                        updateSelectedIcon((selectedIcon ?: CustomIconSerializable()).copy(scaleX = it))
                    }

                    // Scale Y
                    SliderWithLabel(
                        label = stringResource(R.string.scale_y),
                        value = selectedIcon?.scaleY ?: 1f,
                        valueRange = 0.2f..3f,
                        color = MaterialTheme.colorScheme.primary,
                        onReset = {
                            updateSelectedIcon(selectedIcon?.copy(scaleY = null))
                        }
                    ) {
                        updateSelectedIcon((selectedIcon ?: CustomIconSerializable()).copy(scaleY = it))
                    }
                }

                DragonColumnGroup {
                    ColorPickerRow(
                        label = stringResource(R.string.tint),
                        currentColor = selectedIcon?.tint?.let { Color(it) } ?: Color.Unspecified
                    ) {
                        val tintColor = it.definedOrNull()?.toArgb()
                        updateSelectedIcon(
                            (selectedIcon ?: CustomIconSerializable()).copy(
                                tint = tintColor
                            )
                        )
                    }

                    ShapeRow(
                        selected = selectedIcon?.shape ?: iconShapes,
                        onReset = {
                            updateSelectedIcon(
                                (selectedIcon ?: CustomIconSerializable()).copy(
                                    shape = null
                                )
                            )
                        }
                    ) { showShapePickerDialog = true }
                }
            }
        }
    )

    if (showIconPackPicker) {
        IconPackPickerDialog(
            onDismiss = { showIconPackPicker = false },
            onIconPicked = { name, packName ->
                // Now stores the name of the drawable, to avoid storing big bitmaps,
                // renders at runtime, as equally efficient since rendering bitmap also consumes lots
                // Comma separated with the name of the drawable and the pack name
                updateSelectedIcon(
                    (selectedIcon ?: CustomIconSerializable()).copy(
                        type = IconType.ICON_PACK,
                        source = "$name,$packName"
                    )
                )
                showIconPackPicker = false
            }
        )
    }

    if (showShapePickerDialog) {
        ShapePickerDialog(
            selected = selectedIcon?.shape ?: iconShapes,
            onDismiss = { showShapePickerDialog = false }
        ) {
            updateSelectedIcon(
                (selectedIcon ?: CustomIconSerializable()).copy(
                    shape = it
                )
            )

            showShapePickerDialog = false
        }
    }
}

@Composable
private fun SelectableCard(
    modifier: Modifier = Modifier,
    selected: Boolean,
    onClick: () -> Unit,
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = modifier
            .padding(3.dp)
            .clip(DragonShape)
            .background(MaterialTheme.colorScheme.surface.alphaMultiplier(0.7f))
            .clickable { onClick() }
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {

        Row(
            verticalAlignment = Alignment.CenterVertically,
            content = content,
            modifier = Modifier.weight(1f)
        )

        if (selected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

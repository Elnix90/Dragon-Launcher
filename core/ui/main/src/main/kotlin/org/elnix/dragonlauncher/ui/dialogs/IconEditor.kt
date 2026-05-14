@file:Suppress("AssignedValueIsNeverRead", "DEPRECATION")

package org.elnix.dragonlauncher.ui.dialogs

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView
import kotlinx.coroutines.launch
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
import org.elnix.dragonlauncher.ui.base.components.Spacer
import org.elnix.dragonlauncher.ui.base.components.VerticalScrollIndicator
import org.elnix.dragonlauncher.ui.base.modifiers.conditional
import org.elnix.dragonlauncher.ui.base.modifiers.settingsGroupHorizontalPadding
import org.elnix.dragonlauncher.ui.components.PointPreviewCanvas
import org.elnix.dragonlauncher.ui.composition.LocalAppsViewModel
import org.elnix.dragonlauncher.ui.composition.LocalDefaultPoint
import org.elnix.dragonlauncher.ui.composition.LocalIconShape
import org.elnix.dragonlauncher.ui.dragon.colors.ColorPickerRow
import org.elnix.dragonlauncher.ui.dragon.components.DragonIconButton
import org.elnix.dragonlauncher.ui.dragon.components.DragonModalBottomSheet
import org.elnix.dragonlauncher.ui.dragon.components.DragonSettingsGroup
import org.elnix.dragonlauncher.ui.dragon.components.SliderWithLabel
import org.elnix.dragonlauncher.ui.dragon.components.ValidateCancelButtons
import org.elnix.dragonlauncher.ui.helpers.ShapeRow


@Composable
fun PointIconEditor(
    point: SwipePointSerializable,
    onReset: (() -> Unit)? = null,
    onDismiss: () -> Unit,
    onPicked: (CustomIconSerializable?) -> Unit
) {
    val defaultPoint = LocalDefaultPoint.current
    val appsViewModel = LocalAppsViewModel.current

    var selectedIcon by remember { mutableStateOf(point.customIcon) }


    val previewPoint = point.copy(customIcon = selectedIcon)


    IconEditorImpl(
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
fun AppIconEditor(
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


    IconEditorImpl(
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IconEditorImpl(
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

    val columnScrollState = rememberScrollState()

    DragonModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(true),
        content = {
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
                    colors = AppObjectsColors.iconButtonColors(),
                    icon = R.drawable.reset,
                    contentDescription = stringResource(R.string.reset)
                ){
                    updateSelectedIcon(null)
                    onReset?.invoke()
                    textValue = ""
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.verticalScroll(columnScrollState)
                ) {
                    DragonSettingsGroup(R.string.source) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                            modifier = Modifier.height(IntrinsicSize.Min)
                        ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(5.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                SelectableCard(
                                    selected = selectedIcon?.type == IconType.BITMAP && source != null,
                                    onClick = {
                                        imagePicker.launch(arrayOf("image/*"))
                                        textValue = ""
                                    }
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.image),
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = stringResource(R.string.pick_image),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
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
                                        painter = painterResource(R.drawable.palette),
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = stringResource(R.string.pick_from_icon_pack),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            SelectableCard(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight(),
                                selected = selectedIcon?.type == IconType.TEXT && source != null,
                                onClick = null
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxHeight(),
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        stringResource(R.string.text_emoji),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(8.dp)
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
                            onClick = null
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
                                painter = painterResource(R.drawable.close),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = stringResource(R.string.no_custom_icon),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    DragonSettingsGroup(R.string.appearance) {
                        // Opacity
                        SliderWithLabel(
                            modifier = Modifier.settingsGroupHorizontalPadding(),
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
                            modifier = Modifier.settingsGroupHorizontalPadding(),
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
                            modifier = Modifier.settingsGroupHorizontalPadding(),
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
                            modifier = Modifier.settingsGroupHorizontalPadding(),
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

                        Spacer(8.dp)
                    }

                    DragonSettingsGroup(R.string.advanced) {
                        ColorPickerRow(
                            label = stringResource(R.string.tint),
                            currentColor = selectedIcon?.tint?.let { Color(it) } ?: Color.Unspecified,
                            modifier = Modifier.settingsGroupHorizontalPadding(),
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
                            modifier = Modifier.settingsGroupHorizontalPadding(),
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
                VerticalScrollIndicator(columnScrollState.canScrollForward)
            }

            ValidateCancelButtons(
                onCancel = onDismiss,
            ) { onPicked(selectedIcon) }
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
        }
    }
}

@Composable
private fun SelectableCard(
    modifier: Modifier = Modifier,
    selected: Boolean,
    onClick: (() -> Unit)? = null,
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = modifier
            .clip(DragonShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .conditional(onClick != null) {
                clickable(onClick = onClick!!)
            }
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            content = content,
            modifier = Modifier.weight(1f)
        )

        AnimatedVisibility(selected) {
            Icon(
                painter = painterResource(R.drawable.check),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

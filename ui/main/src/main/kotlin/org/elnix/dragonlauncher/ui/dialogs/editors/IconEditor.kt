@file:Suppress("AssignedValueIsNeverRead", "DEPRECATION")

package org.elnix.dragonlauncher.ui.dialogs.editors

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.elnix.dragonlauncher.base.model.models.Application
import org.elnix.dragonlauncher.base.model.models.PointApp
import org.elnix.dragonlauncher.base.model.serializables.CustomIcon
import org.elnix.dragonlauncher.base.model.serializables.CustomIcon.Companion.getProperties
import org.elnix.dragonlauncher.base.model.serializables.CustomIcon.Companion.setProperties
import org.elnix.dragonlauncher.base.model.serializables.CustomIconProperties
import org.elnix.dragonlauncher.base.model.serializables.Point
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.models.DrawerViewModel
import org.elnix.dragonlauncher.models.IconsViewModel
import org.elnix.dragonlauncher.ui.actions.AppIcon
import org.elnix.dragonlauncher.ui.base.activityViewModel
import org.elnix.dragonlauncher.ui.base.asState
import org.elnix.dragonlauncher.ui.components.PointPreviewCanvas
import org.elnix.dragonlauncher.ui.components.iconeditor.IconPicker
import org.elnix.dragonlauncher.ui.compositionslocals.LocalDrawerSettings
import org.elnix.dragonlauncher.ui.dialogs.ShapePickerDialog
import org.elnix.dragonlauncher.ui.dragon.colors.ColorPickerRow
import org.elnix.dragonlauncher.ui.dragon.components.DragonModalBottomSheet
import org.elnix.dragonlauncher.ui.dragon.components.DragonSettingsGroup
import org.elnix.dragonlauncher.ui.dragon.components.SliderWithLabel
import org.elnix.dragonlauncher.ui.dragon.components.rememberBottomSheetState
import org.elnix.dragonlauncher.ui.dragon.text.DialogTitle
import org.elnix.dragonlauncher.ui.helpers.DebugZone
import org.elnix.dragonlauncher.ui.helpers.ShapeRow


@Composable
fun PointIconEditor(
    point: Point,
    iconsViewModel: IconsViewModel = activityViewModel(),
    onPicked: (CustomIcon?) -> Unit
) {

    var editCustomIcon by remember(point.customIcon) { mutableStateOf(point.customIcon) }
    val previewPoint = point.copy(customIcon = editCustomIcon)

    val pointApp = remember(point.key) { PointApp(point) }

    IconEditorImpl(
        application = pointApp,
        customIcon = editCustomIcon,
        preview = {
            PointPreviewCanvas(
                editPoint = previewPoint,
                isDefaultEditing = false,
                backgroundColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.weight(1f)
            )
        },
        onUpdate = {
            editCustomIcon = it
            iconsViewModel.reloadIcon(point.copy(customIcon = editCustomIcon))
        }
    ) {
        onPicked(editCustomIcon)
    }
}

@Composable
fun AppIconEditor(
    app: Application,
    drawerViewModel: DrawerViewModel = activityViewModel(),
    iconViewModel: IconsViewModel = activityViewModel(),
    onDismiss: () -> Unit
) {

    val appOverrideManager = drawerViewModel.appOverrideManager
    val appOverrides by appOverrideManager.appOverrides.asState()
    val initialCustomIcon = appOverrides[app.key]?.customIcon

    var editCustomIcon by remember(initialCustomIcon) { mutableStateOf(initialCustomIcon) }

    IconEditorImpl(
        application = app,
        customIcon = editCustomIcon,
        preview = {
            AppIcon(app, 56.dp)
        },
        onUpdate = {
            editCustomIcon = it
            appOverrideManager.setAppIcon(app.key, editCustomIcon)
            iconViewModel.reloadIcon(app)
        }
    ) {
        appOverrideManager.setAppIcon(app.key, editCustomIcon)
        onDismiss()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IconEditorImpl(
    application: Application,
    customIcon: CustomIcon?,
    preview: @Composable RowScope.() -> Unit,
    onUpdate: (CustomIcon?) -> Unit,
    onDismiss: (CustomIcon?) -> Unit
) {
    var editIcon by remember { mutableStateOf(customIcon) }
    var editProperties by remember { mutableStateOf(customIcon?.getProperties() ?: CustomIconProperties()) }

    val defaultIconShape = LocalDrawerSettings.current.iconShape
    var showShapePickerDialog by remember { mutableStateOf(false) }

    DragonModalBottomSheet(
        onDismissRequest = { onDismiss(editIcon?.setProperties(editProperties)) },
        sheetState = rememberBottomSheetState(true),
        content = {
            DialogTitle(stringResource(R.string.icon_editor), resetEnabled = editIcon == null && editProperties.isNotEmpty) {
                onUpdate(null)
                editIcon = null
                editProperties = CustomIconProperties()
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                content = preview
            )

            DebugZone(true) {
                Text(editProperties.toString())
                Text(editIcon.toString())
            }

            Column(
                modifier = Modifier
                    .heightIn(max = 800.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                DragonSettingsGroup(R.string.appearance) {
                    SliderWithLabel(
                        label = stringResource(R.string.opacity),
                        value = editProperties.opacity ?: CustomIconProperties.defaultOpacity,
                        valueRange = 0f..1f,
                        resetEnabled = editProperties.opacity != null,
                        onReset = {
                            editProperties = editProperties.copy(opacity = null)
                        }
                    ) {
                        editProperties = editProperties.copy(opacity = it)
                    }

                    SliderWithLabel(
                        label = stringResource(R.string.rotation),
                        value = editProperties.rotationDeg ?: CustomIconProperties.defaultRotationDeg,
                        valueRange = -180..180,
                        resetEnabled = editProperties.rotationDeg != null,
                        onReset = {
                            editProperties = editProperties.copy(rotationDeg = null)
                        }
                    ) {
                        editProperties = editProperties.copy(rotationDeg = it)
                    }

                    SliderWithLabel(
                        label = stringResource(R.string.scale_x),
                        value = editProperties.scaleX ?: CustomIconProperties.defaultScaleX,
                        valueRange = 0.2f..3f,
                        resetEnabled = editProperties.scaleX != null,
                        onReset = {
                            editProperties = editProperties.copy(scaleX = null)
                        }
                    ) {
                        editProperties = editProperties.copy(scaleX = it)
                    }

                    SliderWithLabel(
                        label = stringResource(R.string.scale_y),
                        value = editProperties.scaleY ?: CustomIconProperties.defaultScaleY,
                        valueRange = 0.2f..3f,
                        resetEnabled = editProperties.scaleY != null,
                        onReset = {
                            editProperties = editProperties.copy(scaleY = null)
                        }
                    ) {
                        editProperties = editProperties.copy(scaleY = it)
                    }
                }

                DragonSettingsGroup(R.string.advanced) {
                    ColorPickerRow(
                        title = stringResource(R.string.tint),
                        description = null,
                        currentColor = editProperties.tint ?: Color.Unspecified
                    ) {
                        editProperties = editProperties.copy(tint = it)
                    }

                    ShapeRow(
                        selected = editProperties.shape ?: defaultIconShape,
                        resetEnabled = editProperties.shape != null,
                        onReset = { editProperties = editProperties.copy(shape = null) }
                    ) { showShapePickerDialog = true }
                }

                if (showShapePickerDialog) {
                    ShapePickerDialog(
                        selected = editProperties.shape ?: defaultIconShape,
                        onDismiss = { showShapePickerDialog = false }
                    ) {
                        editProperties = editProperties.copy(shape = it)
                    }
                }

                IconPicker(application) {
                    editIcon = (it?.setProperties(editProperties))
                    onUpdate(editIcon?.setProperties(editProperties))
                }
            }
        }
    )
}

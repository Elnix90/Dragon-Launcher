@file:Suppress("AssignedValueIsNeverRead", "DEPRECATION")

package org.elnix.dragonlauncher.ui.dialogs.editors

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.elnix90.runtime.asState
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
import org.elnix.dragonlauncher.settings.stores.map.DrawerSettingsStore
import org.elnix.dragonlauncher.ui.actions.AppIcon
import org.elnix.dragonlauncher.ui.base.activityViewModel
import org.elnix.dragonlauncher.ui.base.asState
import org.elnix.dragonlauncher.ui.base.modifiers.conditional
import org.elnix.dragonlauncher.ui.components.PointPreviewCanvas
import org.elnix.dragonlauncher.ui.components.iconeditor.IconPicker
import org.elnix.dragonlauncher.ui.dialogs.ShapePickerDialog
import org.elnix.dragonlauncher.ui.dragon.colors.ColorPickerRow
import org.elnix.dragonlauncher.ui.dragon.components.DragonModalBottomSheet
import org.elnix.dragonlauncher.ui.dragon.components.DragonSettingsGroup
import org.elnix.dragonlauncher.ui.dragon.components.ResetIcon
import org.elnix.dragonlauncher.ui.dragon.components.SliderWithLabel
import org.elnix.dragonlauncher.ui.dragon.components.ValidateCancelButtons
import org.elnix.dragonlauncher.ui.dragon.components.rememberBottomSheetState
import org.elnix.dragonlauncher.ui.helpers.ShapeRow


@Composable
fun PointIconEditor(
    iconsViewModel: IconsViewModel = activityViewModel(),
    point: Point,
    onReset: (() -> Unit)? = null,
    onDismiss: () -> Unit,
    onPicked: (CustomIcon?) -> Unit
) {

    var editCustomIcon by remember(point.customIcon) { mutableStateOf(point.customIcon) }
    val previewPoint = point.copy(customIcon = editCustomIcon)

    val pointApp = remember(point.key) { PointApp(point) }

    IconEditorImpl(
        application = pointApp,
        customIcon = editCustomIcon,
        onDismiss = onDismiss,
        onReset = onReset,
        preview = {
            PointPreviewCanvas(
                editPoint = previewPoint,
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
    val appOverrides by appOverrideManager.appOverridesState.asState()
    val initialCustomIcon = appOverrides[app.key]?.customIcon

    var editCustomIcon by remember(initialCustomIcon) { mutableStateOf(initialCustomIcon) }

    IconEditorImpl(
        application = app,
        customIcon = editCustomIcon,
        onDismiss = onDismiss,
        onReset = {
            iconViewModel.reloadIcon(app)
            appOverrideManager.setAppIcon(app.key, null)
        },
        preview = {
            AppIcon(app, 56.dp)
        },
        onUpdate = {
            editCustomIcon = it
            iconViewModel.reloadIcon(app)
        }
    ) {
        iconViewModel.reloadIcon(app)
        appOverrideManager.setAppIcon(app.key, editCustomIcon)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IconEditorImpl(
    application: Application,
    customIcon: CustomIcon?,
    onReset: (() -> Unit)? = null,
    onDismiss: () -> Unit,
    preview: @Composable RowScope.() -> Unit,
    onUpdate: (CustomIcon?) -> Unit,
    onPicked: () -> Unit
) {

    val properties = remember { customIcon?.getProperties() ?: CustomIconProperties() }

    DragonModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberBottomSheetState(true),
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

                ResetIcon {
                    onUpdate(null)
                    onReset?.invoke()
                }
            }

            IconPicker(application) {
                onUpdate(it?.setProperties(properties))
            }

            CustomIconPropertiesEditor(properties) { newProperties ->
                onUpdate(customIcon?.setProperties(newProperties))
            }

            ValidateCancelButtons(
                onCancel = onDismiss,
            ) { onPicked() }
        }
    )

//    if (showIconPackPicker) {
//        IconPackPickerDialog(
//            onDismiss = { showIconPackPicker = false },
//            onIconPicked = { name, packName ->
//                // Now stores the name of the drawable, to avoid storing big bitmaps,
//                // renders at runtime, as equally efficient since rendering bitmap also consumes lots
//                // Comma separated with the name of the drawable and the pack name
//                onUpdate(
//                    (properties ?: CustomIcon()).copy(
//                        type = IconType.ICON_PACK,
//                        source = "$name,$packName"
//                    )
//                )
//                showIconPackPicker = false
//            }
//        )
//    }


}


@Composable
private fun CustomIconPropertiesEditor(
    properties: CustomIconProperties,
    onUpdate: (CustomIconProperties) -> Unit,
) {
    val defaultIconShape by DrawerSettingsStore.iconShape.asState()
    var showShapePickerDialog by remember { mutableStateOf(false) }

    DragonSettingsGroup(R.string.appearance) {
        SliderWithLabel(
            label = stringResource(R.string.opacity),
            value = properties.opacity,
            valueRange = 0f..1f,
            resetEnabled = properties.opacity != CustomIconProperties.defaultOpacity,
            onReset = {
                onUpdate(properties.copy(opacity = CustomIconProperties.defaultOpacity))
            }
        ) {
            onUpdate(properties.copy(opacity = it))
        }

        SliderWithLabel(
            label = stringResource(R.string.rotation),
            value = properties.rotationDeg,
            valueRange = -180..180,
            resetEnabled = properties.rotationDeg != CustomIconProperties.defaultRotationDeg,
            onReset = {
                onUpdate(properties.copy(rotationDeg = CustomIconProperties.defaultRotationDeg))
            }
        ) {
            onUpdate(properties.copy(rotationDeg = it))
        }

        SliderWithLabel(
            label = stringResource(R.string.scale_x),
            value = properties.scaleX,
            valueRange = 0.2f..3f,
            resetEnabled = properties.scaleX != CustomIconProperties.defaultScaleX,
            onReset = {
                onUpdate(properties.copy(scaleX = CustomIconProperties.defaultScaleX))
            }
        ) {
            onUpdate(properties.copy(scaleX = it))
        }

        SliderWithLabel(
            label = stringResource(R.string.scale_y),
            value = properties.scaleY,
            valueRange = 0.2f..3f,
            resetEnabled = properties.scaleY != CustomIconProperties.defaultScaleY,
            onReset = {
                onUpdate(properties.copy(scaleY = CustomIconProperties.defaultOpacity))
            }
        ) {
            onUpdate(properties.copy(scaleY = it))
        }
    }

    DragonSettingsGroup(R.string.advanced) {
        ColorPickerRow(
            title = stringResource(R.string.tint),
            description = null,
            currentColor = properties.tint ?: Color.Unspecified
        ) {
            onUpdate(properties.copy(tint = it))
        }

        ShapeRow(
            selected = properties.shape ?: defaultIconShape,
            resetEnabled = properties.shape != null,
            onReset = {
                onUpdate(
                    properties.copy(
                        shape = null
                    )
                )
            }
        ) { showShapePickerDialog = true }
    }

    if (showShapePickerDialog) {
        ShapePickerDialog(
            selected = properties.shape ?: defaultIconShape,
            onDismiss = { showShapePickerDialog = false }
        ) {
            onUpdate(
                properties.copy(
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
            .clip(MaterialTheme.shapes.large)
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

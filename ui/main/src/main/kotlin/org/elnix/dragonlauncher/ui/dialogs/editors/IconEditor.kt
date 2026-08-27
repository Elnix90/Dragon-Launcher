@file:Suppress("DEPRECATION")

package org.elnix.dragonlauncher.ui.dialogs.editors

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.elnix.dragonlauncher.base.icons.LauncherIcon
import org.elnix.dragonlauncher.base.model.models.Application
import org.elnix.dragonlauncher.base.model.models.PointApp
import org.elnix.dragonlauncher.base.model.serializables.CustomIcon
import org.elnix.dragonlauncher.base.model.serializables.CustomIconProperties
import org.elnix.dragonlauncher.base.model.serializables.Point
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.models.DrawerViewModel
import org.elnix.dragonlauncher.models.IconsViewModel
import org.elnix.dragonlauncher.settings.stores.map.DebugSettingsStore
import org.elnix.dragonlauncher.ui.base.activityViewModel
import org.elnix.dragonlauncher.ui.base.asState
import org.elnix.dragonlauncher.ui.base.components.Spacer
import org.elnix.dragonlauncher.ui.components.ShapedLauncherIcon
import org.elnix.dragonlauncher.ui.components.iconeditor.IconPicker
import org.elnix.dragonlauncher.ui.compositionslocals.LocalDrawerSettings
import org.elnix.dragonlauncher.ui.dialogs.ShapePickerDialog
import org.elnix.dragonlauncher.ui.dragon.colors.ColorPickerRow
import org.elnix.dragonlauncher.ui.dragon.components.DragonModalBottomSheet
import org.elnix.dragonlauncher.ui.dragon.components.DragonSettingsGroup
import org.elnix.dragonlauncher.ui.dragon.components.SliderWithLabel
import org.elnix.dragonlauncher.ui.dragon.text.DialogTitle
import org.elnix.dragonlauncher.ui.helpers.DebugZone
import org.elnix.dragonlauncher.ui.helpers.ShapeRow
import org.elnix.dragonlauncher.ui.helpers.settings.SettingsItem


@Composable
fun PointIconEditor(
    point: Point,
    iconsViewModel: IconsViewModel = activityViewModel(),
    onPicked: (CustomIcon?, CustomIconProperties?) -> Unit
) {
    val pointApp = remember(point.key) { PointApp(point) }

    IconEditorImpl(
        application = pointApp,
        customIcon = point.customIcon,
        initialProperties = point.iconProperties ?: CustomIconProperties(),
        resolvePreview = { sizePx, icon, properties ->
            iconsViewModel.getPointIconOnce(point, sizePx, icon, properties)
        },
        onDismiss = onPicked
    )
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
    val initialProperties = appOverrides[app.key]?.iconProperties ?: CustomIconProperties()

    IconEditorImpl(
        application = app,
        customIcon = initialCustomIcon,
        initialProperties = initialProperties,
        resolvePreview = { sizePx, icon, properties ->
            iconViewModel.getAppIconOnce(app, sizePx, icon, properties)
        },
        onDismiss = { newIcon, newProperties ->
            appOverrideManager.setAppCustomization(
                app.key,
                newIcon,
                newProperties.takeIf { it.isNotEmpty }
            )
            onDismiss()
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IconEditorImpl(
    application: Application,
    customIcon: CustomIcon?,
    initialProperties: CustomIconProperties,
    resolvePreview: suspend (sizePx: Int, icon: CustomIcon?, properties: CustomIconProperties) -> LauncherIcon?,
    onDismiss: (CustomIcon?, CustomIconProperties) -> Unit
) {
    val iconSize = LocalDrawerSettings.current.iconSize
    val defaultIconShape = LocalDrawerSettings.current.iconShape

    var editIcon by remember(customIcon, initialProperties) { mutableStateOf(customIcon) }
    var editProperties by remember(customIcon, initialProperties) { mutableStateOf(initialProperties) }

    var showAdvancedSettings by remember { mutableStateOf(false) }
    var previewIcon by remember(application) { mutableStateOf<LauncherIcon?>(null) }
    val previewSizePx = with(LocalDensity.current) { iconSize.toPx() }.toInt()

    LaunchedEffect(editIcon, editProperties) {
        previewIcon = resolvePreview(previewSizePx, editIcon, editProperties)
    }

    // TODO when picking unmodified system icon, it doesn't work and bug out completely I don't want to bother with that anymore

    DragonModalBottomSheet(
        onDismissRequest = { onDismiss(editIcon, editProperties) },
        skipPartiallyExpanded = true,
        content = {
            DialogTitle(stringResource(R.string.icon_editor), resetEnabled = editIcon != null || editProperties.isNotEmpty) {
                editIcon = null
                editProperties = CustomIconProperties()
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                content = {
                    ShapedLauncherIcon(
                        size = iconSize,
                        icon = { previewIcon }
                    )
                }
            )

            DebugZone(DebugSettingsStore.settingsDebugInfo) {
                Text(editProperties.toString())
                Text(editIcon.toString())
            }

            DragonSettingsGroup {
                SettingsItem(
                    title = stringResource(R.string.advanced),
                    icon = R.drawable.eyeglasses_3
                ) { showAdvancedSettings = true }
            }

            Spacer(15.dp)

            IconPicker(application) {
                editIcon = it
            }
        }
    )

    if (showAdvancedSettings) {
        DragonModalBottomSheet({showAdvancedSettings = false}) {
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


                var showShapePickerDialog by remember { mutableStateOf(false) }

                DragonSettingsGroup(R.string.advanced) {
                    ColorPickerRow(
                        title = stringResource(R.string.tint),
                        description = null,
                        currentColor = editProperties.tint,
                        defaultColor = null
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
            }
        }
    }
}

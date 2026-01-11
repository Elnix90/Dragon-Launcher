@file:Suppress("AssignedValueIsNeverRead")

package org.elnix.dragonlauncher.ui.components.dialogs

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.elnix.dragonlauncher.R
import org.elnix.dragonlauncher.data.defaultSwipePointsValues
import org.elnix.dragonlauncher.data.helpers.SwipePointSerializable
import org.elnix.dragonlauncher.data.stores.ColorSettingsStore
import org.elnix.dragonlauncher.ui.colors.ColorPickerRow
import org.elnix.dragonlauncher.ui.components.ValidateCancelButtons
import org.elnix.dragonlauncher.ui.helpers.SliderWithLabel
import org.elnix.dragonlauncher.ui.helpers.actionsInCircle
import org.elnix.dragonlauncher.ui.theme.AmoledDefault
import org.elnix.dragonlauncher.ui.theme.LocalExtraColors
import org.elnix.dragonlauncher.utils.actions.actionColor
import org.elnix.dragonlauncher.utils.actions.actionLabel
import org.elnix.dragonlauncher.utils.colors.AppObjectsColors
import org.elnix.dragonlauncher.utils.colors.adjustBrightness
import org.elnix.dragonlauncher.utils.models.AppsViewModel

@Composable
fun EditPointDialog(
    appsViewModel: AppsViewModel,
    point: SwipePointSerializable,
    onDismiss: () -> Unit,
    onConfirm: (SwipePointSerializable) -> Unit
) {

    val ctx = LocalContext.current
    val extraColors = LocalExtraColors.current

    var editPoint by remember { mutableStateOf(point) }
    var showEditIconDialog by remember { mutableStateOf(false) }
    var showEditActionDialog by remember { mutableStateOf(false) }

    val circleColor by ColorSettingsStore.getCircleColor(ctx)
        .collectAsState(initial = AmoledDefault.CircleColor)

    val pointIcons by appsViewModel.pointIcons.collectAsState()

    val backgroundSurfaceColor = MaterialTheme.colorScheme.surface.adjustBrightness(0.7f)

    val currentActionColor = actionColor(editPoint.action, extraColors)

    val label = actionLabel(editPoint.action, editPoint.customName)
    val actionColor = actionColor(editPoint.action, extraColors, editPoint.customActionColor?.let{ Color(it) })

    LaunchedEffect(
        editPoint.action,
        editPoint.customIcon,
        editPoint.customActionColor
    ) {
        appsViewModel.reloadPointIcon(editPoint)
    }


    CustomAlertDialog(
        modifier = Modifier
            .padding(16.dp)
            .height(700.dp),
        onDismissRequest = onDismiss,
        imePadding = false,
        alignment = Alignment.Center,
        confirmButton = {
            ValidateCancelButtons(
                onCancel = onDismiss
            ) {
                onConfirm(editPoint)
            }
        },
        title = {
            Column(
                verticalArrangement = Arrangement.spacedBy(5.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Spacer(Modifier.weight(1f))

                    Text(
                        text = stringResource(R.string.edit_point),
                        style = MaterialTheme.typography.titleLarge,
                    )

                    Spacer(Modifier.weight(1f))
                    IconButton(
                        onClick = {
                            editPoint = SwipePointSerializable(
                                circleNumber = editPoint.circleNumber,
                                angleDeg = editPoint.angleDeg,
                                nestId = editPoint.nestId,
                                action = editPoint.action,
                                id = editPoint.id
                            )
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Restore,
                            contentDescription = stringResource(R.string.reset)
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(backgroundSurfaceColor)
                        .padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = stringResource(R.string.unselected_action),
                            style = MaterialTheme.typography.labelSmall
                        )

                        Spacer(Modifier.width(95.dp))
                        Text(
                            text = stringResource(R.string.selected_action),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }


                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        val center = size.center
                        val actionSpacing = 240f

                        // Left action
                        actionsInCircle(
                            selected = false,
                            point = editPoint,
                            nests = emptyList(),
                            px = center.x - actionSpacing,
                            py = center.y,
                            ctx = ctx,
                            circleColor = circleColor,
                            colorAction = actionColor(editPoint.action, extraColors),
                            pointIcons = pointIcons,
                            preventBgErasing = true
                        )

                        // Right action
                        actionsInCircle(
                            selected = true,
                            point = editPoint,
                            nests = emptyList(),
                            px = center.x + actionSpacing,
                            py = center.y,
                            ctx = ctx,
                            circleColor = circleColor,
                            colorAction = actionColor(editPoint.action, extraColors),
                            pointIcons = pointIcons,
                            preventBgErasing = true
                        )
                    }
                }
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(5.dp),
            ) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(15.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(backgroundSurfaceColor)
                            .clickable {
                                showEditActionDialog = true
                            }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = label,
                            color = actionColor,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(Modifier.weight(1f))
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = stringResource(R.string.edit_action),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(backgroundSurfaceColor)
                            .clickable {
                                showEditIconDialog = true
                            }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.edit_icon),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.weight(1f))

                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = stringResource(R.string.edit_action),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                }

                TextField(
                    value = editPoint.customName ?: "",
                    onValueChange = {
                        editPoint = editPoint.copy(customName = it)
                    },
                    label = { Text(stringResource(R.string.custom_name)) },
                    trailingIcon = {
                        if (editPoint.customName != null && editPoint.customName!!.isNotEmpty()) {
                            IconButton(
                                onClick = {
                                    editPoint = editPoint.copy(customName = null)
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Restore,
                                    contentDescription = stringResource(R.string.reset)
                                )
                            }
                        }
                    },
                    modifier = Modifier.fillMaxSize(),
                    colors = AppObjectsColors.outlinedTextFieldColors(removeBorder = true, backgroundColor = backgroundSurfaceColor)
                )

                ColorPickerRow(
                    label = stringResource(R.string.custom_action_color),
                    defaultColor = currentActionColor,
                    currentColor = editPoint.customActionColor?.let { Color(it) }
                        ?: currentActionColor,
                    backgroundColor = backgroundSurfaceColor
                ) { selectedColor ->
                    editPoint = editPoint.copy(customActionColor = selectedColor.toArgb())
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(backgroundSurfaceColor)
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ){
                    SliderWithLabel(
                        label = stringResource(R.string.border_stroke),
                        value = editPoint.borderStroke ?: defaultSwipePointsValues.borderStroke!!,
                        valueRange = 0f..50f,
                        color = MaterialTheme.colorScheme.primary,
                        onReset = {
                            editPoint =
                                editPoint.copy(borderStroke = defaultSwipePointsValues.borderStroke!!)
                        }
                    ) {
                        editPoint = editPoint.copy(borderStroke = it)
                    }

                    ColorPickerRow(
                        label = stringResource(R.string.border_color),
                        defaultColor = extraColors.circle,
                        currentColor = editPoint.borderColor?.let { Color(it) }
                            ?: Color.Unspecified
                    ) { selectedColor ->
                        editPoint = editPoint.copy(borderColor = selectedColor.toArgb())
                    }

                    ColorPickerRow(
                        label = stringResource(R.string.background_color),
                        defaultColor = Color.Unspecified,
                        currentColor = editPoint.backgroundColor?.let { Color(it) }
                            ?: Color.Unspecified
                    ) { selectedColor ->
                        editPoint = editPoint.copy(backgroundColor = selectedColor.toArgb())
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(backgroundSurfaceColor)
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ){
                    SliderWithLabel(
                        label = stringResource(R.string.border_stroke_selected),
                        value = editPoint.borderStrokeSelected
                            ?: defaultSwipePointsValues.borderStrokeSelected!!,
                        valueRange = 0f..50f,
                        color = MaterialTheme.colorScheme.primary,
                        onReset = {
                            editPoint =
                                editPoint.copy(borderStrokeSelected = defaultSwipePointsValues.borderStrokeSelected!!)
                        }
                    ) {
                        editPoint = editPoint.copy(borderStrokeSelected = it)
                    }


                    ColorPickerRow(
                        label = stringResource(R.string.border_color_selected),
                        defaultColor = extraColors.circle,
                        currentColor = editPoint.borderColorSelected?.let { Color(it) }
                            ?: Color.Unspecified
                    ) { selectedColor ->
                        editPoint = editPoint.copy(borderColorSelected = selectedColor.toArgb())
                    }


                    ColorPickerRow(
                        label = stringResource(R.string.border_background_selected),
                        defaultColor = Color.Unspecified,
                        currentColor = editPoint.backgroundColorSelected?.let { Color(it) }
                            ?: Color.Unspecified
                    ) { selectedColor ->
                        editPoint = editPoint.copy(backgroundColorSelected = selectedColor.toArgb())
                    }
                }


                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(false) {
                            if (editPoint.haptic == null) editPoint.copy(haptic = true)
                            else editPoint = editPoint.copy(haptic = editPoint.haptic)

                        }
                        .padding(8.dp)
                ) {
                    Checkbox(
                        enabled = false,
                        checked = editPoint.haptic
                            ?: defaultSwipePointsValues.haptic!!,
                        onCheckedChange = {
                            if (editPoint.haptic == null) editPoint.copy(haptic = true)
                            else editPoint = editPoint.copy(haptic = editPoint.haptic)
                        },
                        colors = AppObjectsColors.checkboxColors()
                    )

                    Text(
                        text = stringResource(R.string.haptic_feedback),
                        color = MaterialTheme.colorScheme.onSurface.adjustBrightness(0.7f)
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.surface
    )

    if (showEditIconDialog) {
        IconEditorDialog(
            point = editPoint,
            appsViewModel = appsViewModel,
            onDismiss = { showEditIconDialog = false }
        ) { newIcon ->

            val previewPoint = point.copy(customIcon = newIcon)

            appsViewModel.reloadPointIcon(previewPoint)

            showEditIconDialog = false
            editPoint = editPoint.copy(customIcon = newIcon)
        }
    }
    if (showEditActionDialog) {
        AddPointDialog(
            appsViewModel = appsViewModel,
            onDismiss = { showEditActionDialog = false },
        ) { selectedAction ->
            editPoint = editPoint.copy(action = selectedAction)
            showEditActionDialog = false
        }
    }
}

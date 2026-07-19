@file:Suppress("AssignedValueIsNeverRead")

package org.elnix.dragonlauncher.ui.helpers

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.elnix90.runtime.asState
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.base.model.serializables.Action
import org.elnix.dragonlauncher.base.model.serializables.Action.Companion.actionColor
import org.elnix.dragonlauncher.base.theme.LocalExtraColors
import org.elnix.dragonlauncher.ktx.semiTransparentIfDisabled
import org.elnix.dragonlauncher.models.DrawerViewModel
import org.elnix.dragonlauncher.settings.specialObjects.ActionSettingObject
import org.elnix.dragonlauncher.theme.AppObjectsColors
import org.elnix.dragonlauncher.ui.actions.ActionIcon
import org.elnix.dragonlauncher.ui.actions.AppIcon
import org.elnix.dragonlauncher.ui.actions.actionLabel
import org.elnix.dragonlauncher.ui.base.activityViewModel
import org.elnix.dragonlauncher.ui.base.components.Spacer
import org.elnix.dragonlauncher.ui.dialogs.AddPointDialog
import org.elnix.dragonlauncher.ui.dragon.components.DragonRow

@Composable
private fun ActionSelectorImpl(
    label: String,
    currentAction: Action?,
    nullText: String? = null,
    enabled: Boolean = true,
    switchEnabled: Boolean = true,
    onToggle: () -> Unit,
    onSelected: (Action) -> Unit,
    drawerViewModel: DrawerViewModel = activityViewModel()
) {
    val extraColors = LocalExtraColors.current

    val textColor = MaterialTheme.colorScheme.onSurface.semiTransparentIfDisabled(enabled)

    var showDialog by remember { mutableStateOf(false) }

    val toggled = currentAction != null && currentAction != Action.None
    val actionColor = currentAction.actionColor(extraColors).semiTransparentIfDisabled(enabled)

    DragonRow(
        onClick = { showDialog = true },
        enabled = enabled
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(5.dp),
            modifier = Modifier
                .weight(1f)
        ) {
            Text(
                text = label,
                color = textColor,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1
            )

            AnimatedVisibility(toggled || nullText != null) {
                Row(
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (toggled) {
                        when (currentAction) {
                            is Action.LaunchApp -> {
                                val app by drawerViewModel.findOne(currentAction.packageName, currentAction.profile.userHandle).collectAsState(null)

                                app?.let {
                                    AppIcon(
                                        app = it,
                                        size = 30.dp
                                    )
                                }
                            }

                            // TODO shortcuts annoy me sooo much
//                            is Action.LaunchShortcut -> {
//                                val app by drawerViewModel.findOne(currentAction.packageName, currentAction.profile.userHandle).collectAsState(null)
//
//                                app?.let{
//                                    AppIcon(
//                                        app = it,
//                                        size = 30.dp
//                                    )
//                                }
//                            }
                            else -> {
                                ActionIcon(
                                    action = currentAction,
                                    size = 30.dp
                                )
                            }
                        }
                        Spacer(5.dp)

                        Text(
                            text = actionLabel(currentAction),
                            color = actionColor,
                            style = MaterialTheme.typography.labelMediumEmphasized
                        )
                    } else if (nullText != null) {
                        Text(
                            text = nullText,
                            color = textColor.copy(0.7f),
                            fontSize = 14.sp,
                            textAlign = TextAlign.Right
                        )
                    }
                }
            }
        }

        VerticalDivider(
            modifier = Modifier
                .height(50.dp)
                .padding(horizontal = 8.dp),
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f),
            thickness = 1.dp
        )
        Spacer(modifier = Modifier.width(8.dp))
        Switch(
            checked = toggled,
            enabled = switchEnabled,
            onCheckedChange = {
                if (it) showDialog = true
                else onToggle()
            },
            colors = AppObjectsColors.switchColors()
        )
    }

    if (showDialog) {
        AddPointDialog(
            onDismiss = { showDialog = false },
            onActionSelected = {
                onSelected(it)
                showDialog = false
            }
        )
    }
}

@Composable
public fun CustomActionSelector(
    label: String,
    currentAction: Action?,
    nullText: String? = null,
    enabled: Boolean = true,
    switchEnabled: Boolean = true,
    onToggle: () -> Unit,
    onSelected: (Action) -> Unit
) {
    ActionSelectorImpl(
        label = label,
        currentAction = currentAction,
        nullText = nullText,
        enabled = enabled,
        switchEnabled = switchEnabled,
        onToggle = onToggle,
        onSelected = onSelected
    )
}

@Composable
public fun SettingActionSelector(setting: ActionSettingObject) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    val currentAction by setting.asState()

    ActionSelectorImpl(
        label = stringResource(setting.title!!),
        currentAction = currentAction,
        nullText = null,
        enabled = true,
        switchEnabled = true,
        onToggle = {
            scope.launch {
                setting.set(ctx, Action.None)
            }
        },
        onSelected = {
            scope.launch {
                setting.set(ctx, it)
            }
        }
    )
}

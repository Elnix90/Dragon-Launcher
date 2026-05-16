package org.elnix.dragonlauncher.ui.dragon.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import org.elnix.dragonlauncher.common.R
import org.elnix.dragonlauncher.common.utils.CopyPasteUtils.copyToClipboard
import org.elnix.dragonlauncher.theme.AppObjectsColors
import org.elnix.dragonlauncher.ui.base.UiConstants.DragonShape
import org.elnix.dragonlauncher.ui.dragon.components.DragonIconButton
import org.elnix.dragonlauncher.ui.dragon.components.ValidateCancelButtons


@Composable
fun UserValidation(
    title: String? = null,
    message: String?,
    validateText: String = stringResource(R.string.ok),
    cancelText: String = stringResource(R.string.cancel),
    doNotRemindMeAgain: (() -> Unit)? = null,
    titleIcon: Int = R.drawable.warning,
    titleColor: Color = MaterialTheme.colorScheme.error,
    copy: Boolean = false,
    properties: DialogProperties = DialogProperties(),
    onDismiss: (() -> Unit)? = null,
    onValidate: () -> Unit
) {
    val ctx = LocalContext.current
    var doNotRemindMeAgainChecked by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss ?: onValidate,
        confirmButton = {
            ValidateCancelButtons(
                validateText = validateText,
                cancelText = cancelText,
                onCancel = onDismiss,
                onConfirm = onValidate
            )
        },
        properties = properties,
        title = {
            if (title != null) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(bottom = 4.dp)
                        .clip(DragonShape)
                        .background(MaterialTheme.colorScheme.surface.copy(0.5f))
                        .padding(8.dp)

                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = titleColor
                        )
                    )
                }
            }
        },
        text = {
            if (message != null) {
                Column {
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    if (doNotRemindMeAgain != null || copy) {
                        Spacer(Modifier.height(15.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .clip(DragonShape)
                                .clickable {
                                    doNotRemindMeAgainChecked = !doNotRemindMeAgainChecked
                                }
                        ) {
                            if (doNotRemindMeAgain != null) {
                                Checkbox(
                                    checked = doNotRemindMeAgainChecked,
                                    onCheckedChange = {
                                        doNotRemindMeAgainChecked = !doNotRemindMeAgainChecked
                                    },
                                    colors = AppObjectsColors.checkboxColors()
                                )
                                Text(
                                    text = stringResource(R.string.do_not_remind_me_again),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                            }

                            Spacer(Modifier.weight(1f))

                            if (copy) {
                                DragonIconButton(
                                    icon = R.drawable.copy,
                                    contentDescription = "Copy",
                                    modifier = Modifier
                                        .padding(8.dp)
                                ) { ctx.copyToClipboard(message) }
                            }
                        }
                    }
                }
            }
        },
        icon = {
            Icon(
                painter = painterResource(titleIcon),
                contentDescription = "Warning",
                tint = titleColor
            )
        },
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp,
        shape = DragonShape
    )
}

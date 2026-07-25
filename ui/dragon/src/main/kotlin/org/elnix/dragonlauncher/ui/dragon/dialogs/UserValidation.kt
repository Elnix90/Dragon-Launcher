package org.elnix.dragonlauncher.ui.dragon.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
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
import org.elnix.dragonlauncher.common.utils.CopyPasteUtils.copyToClipboard
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.material.shapes.toShape
import org.elnix.dragonlauncher.theme.AppObjectsColors
import org.elnix.dragonlauncher.ui.base.components.Spacer
import org.elnix.dragonlauncher.ui.dragon.components.DragonIconButton
import org.elnix.dragonlauncher.ui.dragon.components.ValidateCancelButtons
import org.elnix.dragonlauncher.ui.dragon.text.DialogDescription
import org.elnix.dragonlauncher.ui.dragon.text.DialogTitle


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
public fun UserValidation(
    title: String? = null,
    message: String?,
    validateText: String = stringResource(R.string.ok),
    cancelText: String = stringResource(R.string.cancel),
    doNotRemindMeAgain: (() -> Unit)? = null,
    titleIcon: Int = R.drawable.warning,
    titleColor: Color = MaterialTheme.colorScheme.onErrorContainer,
    titleBgColor: Color = MaterialTheme.colorScheme.errorContainer,
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
        icon = {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = titleBgColor,
                        shape = MaterialShapes.Pill.toShape()
                    )
            ) {
                Icon(
                    painter = painterResource(titleIcon),
                    contentDescription = null,
                    tint = titleColor
                )
            }
        },
        title = {
            if (title != null) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(bottom = 4.dp)
                        .clip(MaterialTheme.shapes.large)
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(8.dp)

                ) {
                    DialogTitle(title)
                }
            }
        },
        text = {
            if (message != null) {
                Column {
                    DialogDescription(
                        text = message,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    if (doNotRemindMeAgain != null || copy) {
                        Spacer(15.dp)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .clip(MaterialTheme.shapes.large)
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
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp,
        shape = MaterialTheme.shapes.large
    )
}

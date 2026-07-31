package org.elnix.dragonlauncher.ui.dragon.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import org.elnix.dragonlauncher.common.utils.CopyPasteUtils.copyToClipboard
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.material.shapes.toShape
import org.elnix.dragonlauncher.theme.AppObjectsColors
import org.elnix.dragonlauncher.ui.base.animation.Icon
import org.elnix.dragonlauncher.ui.base.animation.rememberAnimatedIcon
import org.elnix.dragonlauncher.ui.base.components.Spacer
import org.elnix.dragonlauncher.ui.dragon.components.DragonIconButton
import org.elnix.dragonlauncher.ui.dragon.components.ValidateCancelButtons
import org.elnix.dragonlauncher.ui.dragon.text.DialogDescription
import org.elnix.dragonlauncher.ui.dragon.text.DialogTitle


private val useLessicons = setOf(
    R.drawable.shapes,
    R.drawable.casino,
    R.drawable.shape_line,
    R.drawable.visibility,
    R.drawable.account_circle,
    R.drawable.android,
    R.drawable.alternate_email,
    R.drawable.crop_free,
    R.drawable.discord_symbol_blurple,
    R.drawable.check
)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun UserValidation(
    title: String? = null,
    message: String?,
    validateText: String = stringResource(R.string.ok),
    cancelText: String = stringResource(R.string.cancel),
    doNotRemindMeAgain: ((Boolean) -> Unit)? = null,
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

    val uselessAnimation = rememberAnimatedIcon()

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
                var recompose by remember { mutableIntStateOf(0) }
                val successIcon = remember(recompose) { useLessicons.random() }
                uselessAnimation.Icon(
                    defaultIcon = titleIcon,
                    successIcon = successIcon,
                    defaultColor = titleColor,
                    successColor = titleColor,
                    modifier = Modifier.size(32.dp)
                ) {
                    recompose++
                    uselessAnimation.setSuccess()
                }
            }
        },
        title = {
            if (title != null) {
                DialogTitle(
                    text = title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp)
                        .clip(MaterialTheme.shapes.large)
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(8.dp)
                )
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
                            modifier = Modifier.clip(MaterialTheme.shapes.large)
                        ) {
                            if (doNotRemindMeAgain != null) {
                                Checkbox(
                                    checked = doNotRemindMeAgainChecked,
                                    onCheckedChange = {
                                        doNotRemindMeAgainChecked = !doNotRemindMeAgainChecked
                                        doNotRemindMeAgain(doNotRemindMeAgainChecked)
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

                            if (copy) {
                                Spacer(Modifier.weight(1f))
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

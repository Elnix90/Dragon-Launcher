package org.elnix.dragonlauncher.ui.helpers

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.elnix.dragonlauncher.R
import org.elnix.dragonlauncher.utils.colors.AppObjectsColors
import org.elnix.dragonlauncher.utils.copyToClipboard


@Composable
fun UserValidation(
    title: String? = null,
    message: String,
    validateText: String = stringResource(R.string.ok),
    cancelText: String? = stringResource(R.string.cancel),
    doNotRemindMeAgain: (() -> Unit)? = null,
    titleIcon: ImageVector = Icons.Default.Warning,
    titleColor: Color = MaterialTheme.colorScheme.error,
    copy: Boolean = false,
    canDismissByOuterClick: Boolean = true,
    onCancel: () -> Unit,
    onAgree: () -> Unit
) {
    val ctx = LocalContext.current
    var doNotRemindMeAgainChecked by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = if (canDismissByOuterClick) { { onCancel() } } else { {} },
        confirmButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                if (cancelText != null) {
                    TextButton(
                        onClick = onCancel,
                        colors = AppObjectsColors.cancelButtonColors()
                    ) {
                        Text(
                            cancelText,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
                Button(
                    onClick = { onAgree(); if (doNotRemindMeAgain != null && doNotRemindMeAgainChecked) doNotRemindMeAgain() },
                    colors = AppObjectsColors.buttonColors()
                ) {
                    Text(
                        text = validateText,
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        },
        title = {
            if (title != null) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(bottom = 4.dp)
                        .clip(RoundedCornerShape(12.dp))
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
            Column{
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
                            .clip(RoundedCornerShape(12.dp))
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
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy",
                                modifier = Modifier
                                    .padding(8.dp)
                                    .clickable { ctx.copyToClipboard(message) },
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        },
        icon = {
            Icon(
                imageVector = titleIcon,
                contentDescription = "Warning",
                tint = titleColor
            )
        },
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp,
        shape = RoundedCornerShape(20.dp)
    )
}

package org.elnix.dragonlauncher.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import org.elnix.dragonlauncher.base.model.models.ADBCommands
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.ui.base.components.Spacer
import org.elnix.dragonlauncher.ui.base.remember.rememberInteractionSource
import org.elnix.dragonlauncher.ui.dragon.components.DragonModalBottomSheet
import org.elnix.dragonlauncher.ui.dragon.components.DragonSettingsGroup
import org.elnix.dragonlauncher.ui.dragon.text.DialogTitle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T : ADBCommands> AdbCommandPickerDialog(
    label: String,
    options: List<T>,
    selected: () -> T,
    onDismiss: () -> Unit,
    onSelected: (T, Boolean) -> Unit
) {
    var selected by remember { mutableStateOf(selected()) }
    var toast by remember { mutableStateOf(false) }

    DragonModalBottomSheet(
        onDismissRequest = onDismiss,
        skipPartiallyExpanded = true
    ) {
        DialogTitle(label)

        DragonSettingsGroup {
            options.forEach { option ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier =
                        Modifier
                            .dragonSettingGroup {
                                clickable {
                                    onSelected(selected, toast)
                                }
                            },
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Text(
                            text = option.commandEnable,
                            style = MaterialTheme.typography.bodyMedium,
                            fontFamily = FontFamily.Monospace,
                            modifier =
                                Modifier
                                    .clip(MaterialTheme.shapes.medium)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .border(1.dp, MaterialTheme.colorScheme.secondary, MaterialTheme.shapes.medium)
                                    .padding(5.dp)
                        )
                        Text(
                            text = stringResource(option.resId),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }

        Spacer(10.dp)

        val interactionSource = rememberInteractionSource()

        DragonSettingsGroup {
            Row(
                modifier =
                    Modifier
                        .dragonSettingGroup {
                            clickable(interactionSource = interactionSource) { toast = !toast }
                        },
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Text(stringResource(R.string.show_toast))
                Checkbox(
                    checked = toast,
                    onCheckedChange = null,
                    interactionSource = interactionSource
                )
            }
        }
    }
}

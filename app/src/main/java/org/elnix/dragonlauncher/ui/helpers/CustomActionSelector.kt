@file:Suppress("AssignedValueIsNeverRead")

package org.elnix.dragonlauncher.ui.helpers

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.elnix.dragonlauncher.data.SwipeActionSerializable
import org.elnix.dragonlauncher.ui.components.dialogs.AddPointDialog
import org.elnix.dragonlauncher.ui.theme.LocalExtraColors
import org.elnix.dragonlauncher.utils.actions.ActionIcon
import org.elnix.dragonlauncher.utils.actions.actionColor
import org.elnix.dragonlauncher.utils.actions.actionLabel
import org.elnix.dragonlauncher.utils.colors.AppObjectsColors
import org.elnix.dragonlauncher.utils.models.AppsViewModel


@Composable
fun CustomActionSelector(
    appsViewModel: AppsViewModel,
    currentAction: SwipeActionSerializable?,
    nullText: String? = null,
    enabled: Boolean = true,
    switchEnabled: Boolean = true,
    label: String? = null,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    onToggle: (Boolean) -> Unit,
    onSelected: (SwipeActionSerializable) -> Unit
) {
    val extraColors = LocalExtraColors.current

    val pointIcons by appsViewModel.pointIcons.collectAsState()

    var showDialog by remember { mutableStateOf(false) }

    val baseModifier = if (label != null) Modifier.fillMaxWidth() else Modifier.wrapContentWidth()

    val toggled = currentAction != null

    val actionColor = actionColor(currentAction, extraColors).copy(if (enabled) 1f else 0.5f)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = if (label != null) Arrangement.SpaceBetween else Arrangement.Center,
        modifier = baseModifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                color = backgroundColor.copy(if (enabled) 1f else 0.5f),
            )
            .clickable(enabled) { showDialog = true }
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        if (label != null) {
            Text(
                text = label,
                color = textColor.copy(if (enabled) 1f else 0.5f),
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1
            )
        }

        if (toggled) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp)
            ) {
//                Icon(
//                    bitmap = actionIconBitmap(
//                        icons = icons,
//                        action = currentAction,
//                        ctx = ctx,
//                        tintColor = actionColor
//                    ),
//                    contentDescription = actionLabel(currentAction),
//                    tint = Color.Unspecified,
//                    modifier = Modifier.size(22.dp)
//                )

                ActionIcon(
                    action = currentAction,
                    icons = pointIcons,
                    modifier = Modifier.size(30.dp),
                    showLaunchAppVectorGrid = true
                )

                Text(
                    text = actionLabel(currentAction),
                    color = actionColor,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        } else if (nullText != null) {
            Text(
                text = nullText,
                color = textColor.copy(0.7f),
                fontSize = 14.sp,
                textAlign = TextAlign.Right,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp)
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clickable(switchEnabled) {
                    if (toggled) showDialog = true
                    else onToggle(false)
                }
        ) {
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
                    else onToggle(false)
                },
                colors = AppObjectsColors.switchColors()
            )
        }
    }

    if (showDialog) {
        AddPointDialog(
            appsViewModel = appsViewModel,
            onDismiss = { showDialog = false }
        ) {
            onSelected(it)
            showDialog = false
        }
    }
}

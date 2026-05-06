package org.elnix.dragonlauncher.ui.helpers

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.elnix.dragonlauncher.enumsui.toggle.HorizontalAlignment
import org.elnix.dragonlauncher.ui.base.UiConstants.DragonShape
import org.elnix.dragonlauncher.ui.base.modifiers.shapedClickable

@Composable
fun HorizontalAlignmentPicker(
    selected: (HorizontalAlignment) -> Boolean,
    onClick: (HorizontalAlignment) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly,
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, MaterialTheme.colorScheme.primary, DragonShape)
            .padding(5.dp)
    ) {
        HorizontalAlignment.entries.forEach { alignment ->
            HorizontalAlignmentItem(
                alignment = alignment,
                selected = { selected(alignment) }
            ) {
                onClick(alignment)
            }
        }
    }
}


@Composable
private fun HorizontalAlignmentItem(
    alignment: HorizontalAlignment,
    selected: () -> Boolean,
    onClick: () -> Unit
) {
    val selected = selected()

    val tint by animateColorAsState(
        if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
    )

    val background by animateColorAsState(
        if (selected) MaterialTheme.colorScheme.primary else Color.Transparent
    )

    Icon(
        painter = painterResource(alignment.iconEnabled),
        contentDescription = stringResource(alignment.resId!!),
        tint = tint,
        modifier = Modifier
            .size(25.dp)
            .shapedClickable(onClick = onClick)
            .background(background)
    )
}
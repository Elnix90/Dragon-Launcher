package org.elnix.dragonlauncher.ui.helpers

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun UpDownButton(
    upIcon: ImageVector,
    downIcon: ImageVector,
    color: Color,
    upEnabled: Boolean = true,
    downEnabled: Boolean = true,
    upClickable: Boolean = true,
    downClickable: Boolean = true,
    padding: Dp = 20.dp,
    onClickUp: (() -> Unit)?,
    onClickDown: (() -> Unit)?
) {
    val upTint = color.copy(alpha = if (upEnabled) 1f else 0.5f)
    val downTint = color.copy(alpha = if (downEnabled) 1f else 0.5f)

    val upBackground = color.copy(alpha = if (upEnabled) 0.2f else 0f)
    val downBackground = color.copy(alpha = if (downEnabled) 0.2f else 0f)

    Column(
        modifier = Modifier
            .clip(CircleShape)
            .border(1.dp, color.copy(alpha = 0.5f), CircleShape)
            .width(56.dp)
    ) {

        Icon(
            imageVector = upIcon,
            contentDescription = null,
            tint = upTint,
            modifier = Modifier
                .fillMaxWidth()
                .background(upBackground)
                .then(
                    if (upClickable)
                        Modifier.clickable { onClickUp?.invoke() }
                    else Modifier
                )
                .padding(padding)
        )

        Icon(
            imageVector = downIcon,
            contentDescription = null,
            tint = downTint,
            modifier = Modifier
                .fillMaxWidth()
                .background(downBackground)
                .then(
                    if (downClickable)
                        Modifier.clickable { onClickDown?.invoke() }
                    else Modifier
                )
                .padding(padding)
        )
    }
}

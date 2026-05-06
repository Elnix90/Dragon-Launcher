package org.elnix.dragonlauncher.ui.base.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import org.elnix.dragonlauncher.ui.base.animation.AnimatedIcon
import org.elnix.dragonlauncher.ui.base.animation.barsContentTransform
import org.elnix.dragonlauncher.ui.base.animation.icon



/**
 * Composable animated icon with state transitions.
 *
 * Displays icon that animates between [AnimatedIconStatus.Default], [AnimatedIconStatus.Success], and [AnimatedIconStatus.Error] states.
 * Icon is clickable and resets to default after animation completes.
 *
 * @param defaultIcon Resource ID of the default icon to display
 * @param onClick Callback when icon is clicked
 */
@Composable
fun AnimatedIcon.Icon(
    defaultIcon: Int,
    onClick: () -> Unit
) {
    val status by this.status.collectAsState()

    AnimatedContent(
        targetState = status,
        transitionSpec = { barsContentTransform }
    ) { status ->
        val painter = status.icon(defaultIcon)

        Icon(
            painter = painter,
            contentDescription = null,
            modifier = Modifier
                .clip(RoundedCornerShape(5.dp))
                .clickable(onClick = onClick)
                .padding(5.dp)
        )
    }
}

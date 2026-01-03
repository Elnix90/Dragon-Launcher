package org.elnix.dragonlauncher.ui.welcome

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

@Composable
fun AnimatedPagerIndicator(
    currentPage: Int,
    total: Int
) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        repeat(total) { index ->
            val isCurrent = index == currentPage
            val animatedWidth by animateFloatAsState(
                targetValue = if (isCurrent) 36f else 10f,
                animationSpec = tween(200),
                label = ""
            )
            val animatedColor by animateFloatAsState(
                targetValue = if (isCurrent) 1f else 0.4f,
                animationSpec = tween(200),
                label = ""
            )

            Box(
                modifier = Modifier
                    .height(8.dp)
                    .width(animatedWidth.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = animatedColor))
            )

            if (index < total - 1) {
                Spacer(Modifier.width(8.dp))
            }
        }
    }
}

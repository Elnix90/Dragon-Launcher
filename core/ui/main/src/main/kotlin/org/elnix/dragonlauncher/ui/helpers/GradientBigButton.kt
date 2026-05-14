package org.elnix.dragonlauncher.ui.helpers

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.elnix.dragonlauncher.ui.base.UiConstants.DragonShape

@Composable
fun GradientBigButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    onClick: () -> Unit
) {

    val colors = if (enabled) {
        listOf(Color(0xFFF62FEA), Color(0xFFDC0000))
    } else {
        listOf(
            Color(0xFFF62FEA).copy(alpha = 0.4f),
            Color(0xFFDC0000).copy(alpha = 0.4f)
        )
    }

    val gradient = Brush.sweepGradient(
        colors = colors,
        center = Offset.Infinite
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .height(70.dp)
            .clip(DragonShape)
            .border(
                width = 3.dp,
                brush = gradient,
                shape = DragonShape
            )
            .background(
                if (enabled) MaterialTheme.colorScheme.background
                else MaterialTheme.colorScheme.background.copy(alpha = 0.3f)
            )
            .clickable(enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (leadingIcon != null) {
                Box(
                    modifier = Modifier.padding(end = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    leadingIcon()
                }
            }

            Text(
                text = text,
                modifier = Modifier.weight(1f),
                color = if (enabled) Color.White else Color.White.copy(alpha = 0.6f),
                fontSize = 20.sp,
                textAlign = TextAlign.Center
            )

            if (trailingIcon != null) {
                Box(
                    modifier = Modifier.padding(start = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    trailingIcon()
                }
            }
        }
    }
}

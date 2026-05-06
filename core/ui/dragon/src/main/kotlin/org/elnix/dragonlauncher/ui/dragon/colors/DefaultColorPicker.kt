package org.elnix.dragonlauncher.ui.dragon.colors

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import org.elnix.dragonlauncher.common.R
import org.elnix.dragonlauncher.ui.base.modifiers.shapedClickable

@Composable
fun DefaultColorPicker(
    initialColor: Color,
    onColorSelected: (Color) -> Unit
) {
    var selectedColor by remember { mutableStateOf(initialColor) }


    val defaultColors = listOf(
        Color.Red,
        Color.Blue,
        Color.Green,
        Color.Yellow,
        Color(0xFFEF5350), // Red
        Color(0xFFFF7043), // Deep orange
        Color(0xFFFFCA28), // Amber
        Color(0xFF66BB6A), // Green
        Color(0xFF26A69A), // Teal
        Color(0xFF42A5F5), // Blue
        Color(0xFF5C6BC0), // Indigo
        Color(0xFFB63AC7), // Purple
        Color(0xFFEC407A), // Pink
        Color(0xFF8D6E63), // Brown
        Color(0xFF78909C), // Blue Gray
        Color(0xFF9CCC65), // Light Green
        Color(0xFF26C6DA), // Cyan
        Color(0xFFD4E157), // Lime
        Color(0xFFFFB74D), // Orange
        Color(0xFFBA68C8), // Violet
        Color(0xFFFFFFFF), // White
        Color(0xFFBDBDBD), // Light Gray
        Color(0xFF616161), // Dark Gray
        Color(0xFF000000)  // Black
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxSize()
            .padding(5.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Display colors in 4x4 grid
            defaultColors.chunked(4).forEach { rowColors ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    rowColors.forEach { color ->
                        val isSelected = color == selectedColor

                        val backgroundColor by animateColorAsState(
                            targetValue = with(MaterialTheme.colorScheme) {
                                if (isSelected) primary
                                else surface
                            }
                        )

                        val scale by animateFloatAsState(
                            targetValue = if (isSelected) 1f else 0f,
                            animationSpec = tween(durationMillis = 300),
                            label = "Check Scale Animation"
                        )


                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .shapedClickable(hapticFeedback = true) {
                                        selectedColor = color
                                        onColorSelected(color)
                                    }
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .border(
                                        width = if (isSelected) 3.dp else 1.dp,
                                        color = if (isSelected)
                                            MaterialTheme.colorScheme.onBackground
                                        else
                                            MaterialTheme.colorScheme.outline,
                                        shape = CircleShape
                                    )
                            )


                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .scale(scale)
                                    .clip(CircleShape)
                                    .background(color = MaterialTheme.colorScheme.primaryContainer)
                            ) {
                                Icon(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .align(Alignment.Center),
                                    painter = painterResource(R.drawable.check),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

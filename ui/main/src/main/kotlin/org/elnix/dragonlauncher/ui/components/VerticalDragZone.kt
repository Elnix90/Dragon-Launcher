package org.elnix.dragonlauncher.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

@Composable
fun VerticalDragZone(onDrag: (amount: Float) -> Unit) {
    var isDragging by remember { mutableStateOf(false) }
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(40.dp)
                .pointerInput(Unit) {
                    detectVerticalDragGestures(
                        onDragStart = { isDragging = true },
                        onDragEnd = { isDragging = false },
                        onDragCancel = { isDragging = false },
                        onVerticalDrag = { _, dragAmount ->
                            onDrag(dragAmount)
                        }
                    )
                },
        contentAlignment = Alignment.Center
    ) {
        val animatedHeight by animateDpAsState(if (isDragging) 2.dp else 4.dp)
        Box(
            Modifier
                .size(width = 32.dp, height = animatedHeight)
                .clip(MaterialTheme.shapes.extraLarge)
                .background(MaterialTheme.colorScheme.outline)
        )
    }
}

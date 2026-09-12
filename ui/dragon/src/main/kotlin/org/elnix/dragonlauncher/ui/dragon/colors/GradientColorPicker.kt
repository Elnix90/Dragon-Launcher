package org.elnix.dragonlauncher.ui.dragon.colors

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import android.graphics.Color as AndroidColor

@Composable
fun GradientColorPicker(
    actualColor: Color,
    onColorSelected: (Color) -> Unit
) {
    val initialHsv = remember {
        FloatArray(3).apply {
            AndroidColor.colorToHSV(actualColor.toArgb(), this)
        }
    }

    var hue by remember { mutableFloatStateOf(initialHsv[0]) }
    var sat by remember { mutableFloatStateOf(initialHsv[1]) }
    var value by remember { mutableFloatStateOf(initialHsv[2]) }

    /**
     * Selected color, based on [actualColor] and only updated when I change the color.
     * Without this thing, the displayed color in the gradient as well as the pointer position can be moved by the  external color change
     *
     * I tried without, and also by removing it, but it ain't working without,
     * # DO NOT REMOVE THIS
     */
    var selectedColor by remember { mutableStateOf(actualColor) }

    /**
     * Colors I last emitted myself. Lets me tell apart changes that come
     * from my own drags vs real external changes, so I never re-derive the
     * picker position from our own output (that round-trip corrupts hue/sat
     * for grays/black).
     */
    var lastSyncedColor by remember { mutableStateOf(actualColor) }

    LaunchedEffect(actualColor) {
        if (actualColor != lastSyncedColor) {
            val hsvArray =
                FloatArray(3).apply {
                    AndroidColor.colorToHSV(actualColor.toArgb(), this)
                }
            hue = hsvArray[0]
            sat = hsvArray[1]
            value = hsvArray[2]

            selectedColor = actualColor
            lastSyncedColor = actualColor
        }
    }

    fun emitColor(color: Color) {
        selectedColor = color
        lastSyncedColor = color

        onColorSelected(color)
    }

    val hueColor = remember(hue) { Color.hsv(hue, 1f, 1f) }

    var isDraggingHue by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        fun PointerInputScope.pickColorFromPos(pos: Offset) {
            sat = (pos.x / size.width).coerceIn(0f, 1f)
            value = 1f - (pos.y / size.height).coerceIn(0f, 1f)
            emitColor(Color.hsv(hue, sat, value).copy(alpha = actualColor.alpha))
        }

        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(MaterialTheme.shapes.large)
                    .drawWithContent {
                        drawContent()
                        drawRect(
                            brush = Brush.horizontalGradient(listOf(Color.White, hueColor))
                        )
                        drawRect(
                            brush = Brush.verticalGradient(listOf(Color.Transparent, Color.Black)),
                            blendMode = BlendMode.Multiply
                        )

                        // Draw selector circle
                        val x = sat * size.width
                        val y = (1f - value) * size.height
                        drawCircle(
                            color = if (selectedColor.luminance() > 0.5) Color.Black else Color.White,
                            radius = 10.dp.toPx(),
                            center = Offset(x, y),
                            style = Stroke(width = 2.dp.toPx())
                        )
                    }.pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = {
                                pickColorFromPos(it)
                            },
                            onDrag = { change, _ -> pickColorFromPos(change.position) }
                        )
                    }.pointerInput(Unit) {
                        detectTapGestures(
                            onTap = {
                                pickColorFromPos(it)
                            },
                            onLongPress = {
                                pickColorFromPos(it)
                            }
                        )
                    }
        )

        fun PointerInputScope.pickColorFromPosHorizontal(pos: Offset) {
            val ratio = (pos.x / size.width).coerceIn(0f, 1f)
            hue = ((1f - ratio) * 360f).coerceIn(0f, 360f)
            emitColor(Color.hsv(hue, sat, value).copy(alpha = actualColor.alpha))
        }

        Box {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(25.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = (360 downTo 0 step 30).map { Color.hsv(it.toFloat(), 1f, 1f) }
                            )
                        ).drawWithContent {
                            drawContent()
                            val x = (1 - hue / 360f) * size.width
                            drawLine(
                                color = Color.White,
                                start = Offset(x, 0f),
                                end = Offset(x, size.height),
                                strokeWidth = 3.dp.toPx()
                            )
                        }.pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = {
                                    pickColorFromPosHorizontal(it)
                                    isDraggingHue = true
                                },
                                onDrag = { change, _ ->
                                    pickColorFromPosHorizontal(change.position)
                                },
                                onDragEnd = {
                                    isDraggingHue = false
                                }
                            )
                        }.pointerInput(Unit) {
                            detectTapGestures(
                                onTap = {
                                    pickColorFromPosHorizontal(it)
                                },
                                onLongPress = {
                                    pickColorFromPosHorizontal(it)
                                }
                            )
                        }
            )

            Canvas(Modifier.fillMaxSize()) {
                if (isDraggingHue) {
                    val x = (1 - hue / 360f) * size.width
                    val center = Offset(x, size.height - 50.dp.toPx())

                    val color = Color.hsv(hue, 1f, 1f)
                    colorPreview(color, center)
                }
            }
        }
    }
}

private val colorPinPreviewSize = 30.dp

private fun DrawScope.colorPreview(
    color: Color,
    center: Offset
) {
    drawCircle(
        color = Color.White,
        radius = colorPinPreviewSize.toPx(),
        center = center,
        style = Stroke(width = 2.dp.toPx())
    )
    drawCircle(
        color = color,
        radius = (colorPinPreviewSize - 1.dp).toPx(),
        center = center,
        style = Fill
    )
}

package org.elnix.dragonlauncher.ui.remembers

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.retain.retain
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Constraints
import org.elnix.dragonlauncher.ui.composition.LocalTextMeasurer
import org.elnix.dragonlauncher.ui.helpers.swipe.cache.nests.DrawScopeText

@Composable
public fun rememberCustomText(
    text: String,
    sizePx: Float = 50f
): DrawScopeText {
    val textMeasurer: TextMeasurer = LocalTextMeasurer.current
    val textStyle = MaterialTheme.typography.labelSmall

    return retain(text, sizePx) {
        getTopLeftAndTextMeasure(
            text = text,
            sizePx = sizePx,
            textStyle = textStyle,
            textMeasurer = textMeasurer
        )
    }
}

private fun getTopLeftAndTextMeasure(
    text: String,
    sizePx: Float,
    textStyle: TextStyle,
    textMeasurer: TextMeasurer
): DrawScopeText {
    val textLayoutResult = textMeasurer.measure(
        text = AnnotatedString(text),
        constraints = Constraints(maxWidth = Int.MAX_VALUE),
        style = textStyle
    )

    val textWidth = textLayoutResult.size.width
    val textHeight = textLayoutResult.size.height

    return DrawScopeText(
        offsetTextLayoutResult = textLayoutResult,
        topLeft = Offset(
            x = textWidth / 2f,
            y = textHeight + sizePx
        )
    )
}
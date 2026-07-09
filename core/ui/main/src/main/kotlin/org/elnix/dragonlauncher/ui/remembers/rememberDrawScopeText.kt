package org.elnix.dragonlauncher.ui.remembers

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.util.fastRoundToInt
import org.elnix.dragonlauncher.base.model.serializables.Point
import org.elnix.dragonlauncher.ui.composition.LocalNestDebugOverlay
import org.elnix.dragonlauncher.ui.helpers.swipe.cache.nests.DrawScopeText

@Composable
public fun rememberDrawScopeText(
    point: Point,
    sizePx: Float
): CustomTexts {
    val nestDebugOverlay = LocalNestDebugOverlay.current
    val textStyle = rememberPointTextStyle()
    val textMeasurer = rememberTextMeasurer()

    return remember(
        nestDebugOverlay,
        textStyle,
        textMeasurer,
        point.offset
    ) {
        if (nestDebugOverlay) {
            val pointOffset = point.offset
            val x = pointOffset.x.fastRoundToInt()
            val y = pointOffset.y.fastRoundToInt()
            val offsetText = "$x ; $y"

            val idText = point.id.toString()

            val offsetDsText = geTopLeftAndTM(offsetText, textStyle, sizePx, textMeasurer)
            val idDsText = geTopLeftAndTM(idText, textStyle, -sizePx * 1.5f, textMeasurer)

            offsetDsText to idDsText
        } else null
    }
}

public typealias CustomTexts = Pair<DrawScopeText, DrawScopeText>?

public fun geTopLeftAndTM(
    text: String,
    textStyle: TextStyle,
    sizePx: Float,
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
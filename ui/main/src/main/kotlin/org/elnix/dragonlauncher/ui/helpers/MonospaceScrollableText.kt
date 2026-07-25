package org.elnix.dragonlauncher.ui.helpers

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.elnix90.logging.allLetters
import io.github.elnix90.logging.logLevel
import io.github.elnix90.logging.logLevelColor
import my.nanihadesuka.compose.LazyColumnScrollbar
import my.nanihadesuka.compose.ScrollbarSelectionMode
import my.nanihadesuka.compose.ScrollbarSettings
import org.elnix.dragonlauncher.ktx.alphaMultiplier

@Composable
fun MonospaceScrollableText(
    lines: List<String>,
    modifier: Modifier = Modifier,
    useDragonLogsColoration: Boolean = false
) {
    val lazyListState = rememberLazyListState()
    val horizontalScrollState = rememberScrollState()

    val thumbColor = MaterialTheme.colorScheme.primary

    val scrollBar = remember(thumbColor) {
        ScrollbarSettings(
            alwaysShowScrollbar = true,
            thumbThickness = 10.dp,
            thumbUnselectedColor = thumbColor.alphaMultiplier(0.5f),
            thumbSelectedColor = thumbColor,
            selectionMode = ScrollbarSelectionMode.Full
        )
    }


    fun lineColor(idx: Int): Color? {
        if (idx !in lines.indices) return null

        for (i in idx downTo 0) {
            val line = lines[i]

            if (
                line.length > 27 &&
                line.startsWith("[") &&
                line[26].toString() in allLetters
            ) {
                return line[26].toString().logLevel!!.logLevelColor
            }
        }
        return null
    }

    Box(modifier = modifier.fillMaxWidth()) {
        SelectionContainer {
            LazyColumnScrollbar(
                state = lazyListState,
                settings = scrollBar
            ) {
                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(horizontalScrollState)
                ) {
                    itemsIndexed(lines) { idx, line ->

                        val color = if (useDragonLogsColoration) {
                            lineColor(idx)
                        } else null

                        Text(
                            text = line,
                            color = color ?: Color.Unspecified,
                            softWrap = false,
                            fontSize = 10.sp,
                            lineHeight = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}

package org.elnix.dragonlauncher.ui.helpers.nests

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import org.elnix.dragonlauncher.base.theme.LocalExtraColors

@Composable
fun NestPlaceholder() {
    Box(
        modifier = Modifier
            .size(30.dp)
            .border(2.dp, LocalExtraColors.current.circle, CircleShape)
    )
}
// TODO
@Suppress("FunctionName")
fun DrawScope.NestPlaceholder(
    center: Offset,
    drawParams: DrawParams
) {

}
package org.elnix.dragonlauncher.ui.remembers

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import org.elnix.dragonlauncher.base.model.serializables.Point
import org.elnix.dragonlauncher.ktx.cleanString
import org.elnix.dragonlauncher.ktx.px
import org.elnix.dragonlauncher.ui.composition.LocalNestDebugOverlay
import org.elnix.dragonlauncher.base.cache.DrawScopeText

@Composable
fun rememberDrawScopeText(
    point: Point,
    sizePx: Float,
    defaultPoint: Point
): Pair<DrawScopeText?, DrawScopeText?>? {
    if (!LocalNestDebugOverlay.current) return null
    val pointSize = (point.getSize(defaultPoint) + 5.dp).px
    return rememberCustomText(point.offset.cleanString(), pointSize) to rememberCustomText(point.id.toString(), -pointSize)
}

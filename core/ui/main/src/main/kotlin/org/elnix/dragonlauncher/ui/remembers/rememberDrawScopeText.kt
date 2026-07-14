package org.elnix.dragonlauncher.ui.remembers

import androidx.compose.runtime.Composable
import org.elnix.dragonlauncher.base.model.serializables.Point
import org.elnix.dragonlauncher.ktx.cleanString
import org.elnix.dragonlauncher.ui.composition.LocalNestDebugOverlay
import org.elnix.dragonlauncher.ui.helpers.swipe.cache.nests.DrawScopeText

@Composable
public fun rememberDrawScopeText(
    point: Point,
    sizePx: Float
): CustomTexts {
    if (!LocalNestDebugOverlay.current) return null
    return rememberCustomText(point.offset.cleanString()) to rememberCustomText(point.id.toString())
}

public typealias CustomTexts = Pair<DrawScopeText, DrawScopeText>?

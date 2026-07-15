package org.elnix.dragonlauncher.ui.components

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.elnix.dragonlauncher.base.model.serializables.Action
import org.elnix.dragonlauncher.base.model.serializables.Point
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.ktx.px
import org.elnix.dragonlauncher.models.PointsViewModel
import org.elnix.dragonlauncher.ui.base.activityViewModel
import org.elnix.dragonlauncher.ui.base.asState
import org.elnix.dragonlauncher.ui.helpers.swipe.PointIcon
import org.elnix.dragonlauncher.ui.helpers.swipe.cache.nests.DrawScopeText
import org.elnix.dragonlauncher.ui.remembers.rememberCustomText

@Composable
public fun PointPreviewCanvas(
    editPoint: Point,
    modifier: Modifier = Modifier,
    pointsViewModel: PointsViewModel = activityViewModel()
) {
    val pointsService = pointsViewModel.pointsService
    val defaultPoint by pointsService.defaultPoint.asState()

    val height =
        when (editPoint.action) {
            is Action.OpenCircleNest -> 100
            else -> (editPoint.size ?: defaultPoint.size ?: Point.defaultSize) +
                    (editPoint.innerPadding ?: defaultPoint.innerPadding ?: Point.defaultInnerPadding) * 2

        }

    val pointSize = editPoint.getSize(defaultPoint).px

    BoxWithConstraints(
        modifier = modifier
            .height(height.dp)
    ) {
        val width = this.maxWidth
        val height = this.maxHeight

        val centerY = (height / 2f).px
        val leftX = (width * 0.25f).px
        val rightX = (width * 0.75f).px

        val selected = rememberCustomText(stringResource(R.string.selected_text),  pointSize)
        val unselected = rememberCustomText(stringResource(R.string.unselected),  pointSize)

        val customTextsSelected: Pair<DrawScopeText?, DrawScopeText?> = selected to null
        val customTextsUnselected: Pair<DrawScopeText?, DrawScopeText?> = unselected to null

        // Left action
        PointIcon(
            selected = false,
            point = editPoint,
            depth = Int.MAX_VALUE,
            center = Offset(leftX, centerY),
            preventBgErasing = true,
            showConfiguratorDecorations = true,
            customText = customTextsUnselected
        )

        // Right action
        PointIcon(
            selected = true,
            point = editPoint,
            depth = Int.MAX_VALUE,
            center = Offset(rightX, centerY),
            preventBgErasing = true,
            showConfiguratorDecorations = true,
            customText = customTextsSelected
        )
    }
}
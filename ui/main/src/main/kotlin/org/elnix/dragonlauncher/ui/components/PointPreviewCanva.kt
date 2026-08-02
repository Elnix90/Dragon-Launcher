package org.elnix.dragonlauncher.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.drawText
import androidx.compose.ui.unit.dp
import org.elnix.dragonlauncher.base.model.serializables.Action
import org.elnix.dragonlauncher.base.model.serializables.Point
import org.elnix.dragonlauncher.enumsui.select.SelectedUnselectedViewMode
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.ktx.px
import org.elnix.dragonlauncher.models.PointsViewModel
import org.elnix.dragonlauncher.ui.base.activityViewModel
import org.elnix.dragonlauncher.ui.base.asState
import org.elnix.dragonlauncher.ui.helpers.swipe.PointIcon
import org.elnix.dragonlauncher.ui.remembers.rememberCustomText

@Composable
fun PointPreviewCanvas(
    editPoint: Point,
    modifier: Modifier = Modifier,
    backgroundColor: Color,
    pointsViewModel: PointsViewModel = activityViewModel(),
    onClick: ((SelectedUnselectedViewMode) -> Unit)? = null
) {
    val pointsService = pointsViewModel.pointsService
    val defaultPoint by pointsService.defaultPoint.asState()

    val height =
        when (editPoint.action) {
            is Action.OpenCircleNest -> 100.dp
            else -> editPoint.getSize(defaultPoint) + editPoint.getInnerPadding(defaultPoint) * 2
        }

    val pointSize = editPoint.getSize(defaultPoint).px

    BoxWithConstraints(
        modifier = modifier
            .height(height + 40.dp)
    ) {
        val width = this.maxWidth
        val height = this.maxHeight

        val centerY = (height / 2f).px
        val leftX = (width * 0.25f).px
        val rightX = (width * 0.75f).px

        val leftCenter = Offset(leftX, centerY)
        val rightCenter = Offset(rightX, centerY)

        PointIcon(
            selected = false,
            point = editPoint,
            depth = Int.MAX_VALUE,
            center = leftCenter,
            eraseColor = backgroundColor,
            pointSettingsDisplay = false
        )

        PointIcon(
            selected = true,
            point = editPoint,
            depth = Int.MAX_VALUE,
            center = rightCenter,
            eraseColor = backgroundColor,
            pointSettingsDisplay = false
        )


        val selected = rememberCustomText(stringResource(R.string.selected_text), pointSize)
        val unselected = rememberCustomText(stringResource(R.string.unselected), pointSize)
        val textColor = MaterialTheme.colorScheme.onSurface

        Canvas(Modifier.fillMaxSize()) {
            drawText(
                textLayoutResult = unselected.offsetTextLayoutResult,
                color = textColor,
                topLeft = leftCenter - unselected.topLeft
            )
            drawText(
                textLayoutResult = selected.offsetTextLayoutResult,
                color = textColor,
                topLeft = rightCenter - selected.topLeft
            )
        }

        if (onClick != null) {
            Row(Modifier.fillMaxSize()) {
                Box(
                    Modifier
                        .fillMaxHeight()
                        .weight(1f)
                        .clickable { onClick(SelectedUnselectedViewMode.Unselected) }
                )
                Box(
                    Modifier
                        .fillMaxHeight()
                        .weight(1f)
                        .clickable { onClick(SelectedUnselectedViewMode.Selected) }
                )
            }
        }
    }
}
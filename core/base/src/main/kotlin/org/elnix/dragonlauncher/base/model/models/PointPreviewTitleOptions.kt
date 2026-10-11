package org.elnix.dragonlauncher.base.model.models

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit

data class PointPreviewTitleOptions(
	val showLabel: Boolean,
	val showIcon: Boolean,
	val pointPreviewMode: PointPreviewMode,
	val appIconOverlaySize: Dp,
	val appIconOverlayScale: Float,
	val appLabelOverlaySize: TextUnit,
	val topPadding: Dp
)

enum class PointPreviewMode {
	New,
	Legacy
}

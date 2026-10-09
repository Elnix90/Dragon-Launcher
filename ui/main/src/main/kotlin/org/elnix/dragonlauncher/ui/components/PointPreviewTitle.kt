package org.elnix.dragonlauncher.ui.components

import android.annotation.SuppressLint
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.elnix.dragonlauncher.base.model.models.PointPreviewMode
import org.elnix.dragonlauncher.base.model.serializables.Action.Companion.actionColor
import org.elnix.dragonlauncher.base.model.serializables.Point
import org.elnix.dragonlauncher.base.theme.LocalExtraColors
import org.elnix.dragonlauncher.ktx.getCenter
import org.elnix.dragonlauncher.ui.actions.FinalPointIcon
import org.elnix.dragonlauncher.ui.actions.actionLabel
import org.elnix.dragonlauncher.ui.composition.LocalPointPreviewTitleOptions
import org.elnix.dragonlauncher.ui.helpers.swipe.PointIcon

@SuppressLint("UseOfNonLambdaOffsetOverload")
@Composable
fun PointPreviewTitle(point: Point?) {
	if (point == null) return

	val options = LocalPointPreviewTitleOptions.current
	if (!(options.showIcon || options.showLabel)) return

	val offsetY = remember { Animatable(initialValue = -20f) }

	LaunchedEffect(point.id) {
		offsetY.snapTo(-20f)
		offsetY.animateTo(
			targetValue = 0f,
			animationSpec = tween(150)
		)
	}

	Box(
		Modifier
			.fillMaxWidth()
			.offset(y = offsetY.value.dp)
			.padding(top = options.topPadding),
		contentAlignment = Alignment.TopCenter
	) {
		PointPreview(point)
	}
}

@Composable
fun PointPreview(
	point: Point
) {
	val extraColors = LocalExtraColors.current
	val options = LocalPointPreviewTitleOptions.current

	val label = point.customName ?: actionLabel(point.action)

	Row(
		horizontalArrangement = Arrangement.spacedBy(5.dp),
		verticalAlignment = Alignment.CenterVertically
	) {
		if (options.showIcon) {
			when (options.pointPreviewMode) {
				PointPreviewMode.New -> {
					// TODO change the size to the scale
					BoxWithConstraints(Modifier.requiredSize(options.appIconOverlaySize)) {
						val center = constraints.getCenter()
						PointIcon(
							selected = false,
							point = point,
							center = center,
							isTopPoint = true,
							eraseColor = Color.Transparent
						)
					}
				}

				PointPreviewMode.Legacy -> {
					FinalPointIcon(point, size = options.appIconOverlaySize)
				}
			}
		}

		if (options.showLabel) {
			Text(
				text = label,
				style =
					TextStyle(
						color = point.action.actionColor(extraColors, point.customActionColor),
						fontSize = options.appLabelOverlaySize,
						fontWeight = FontWeight.Bold,
						shadow =
							Shadow(
								color = Color.Black.copy(alpha = 0.48f),
								offset = Offset(0f, 1f),
								blurRadius = 5f
							)
					)
			)
		}
	}
}

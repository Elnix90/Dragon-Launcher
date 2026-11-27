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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.elnix90.shared.ui.base.asState
import org.elnix.dragonlauncher.base.model.models.PointPreviewMode
import org.elnix.dragonlauncher.base.model.serializables.Action.Companion.actionColor
import org.elnix.dragonlauncher.base.model.serializables.Point
import org.elnix.dragonlauncher.base.theme.LocalExtraColors
import org.elnix.dragonlauncher.ktx.getCenter
import org.elnix.dragonlauncher.models.PointsViewModel
import org.elnix.dragonlauncher.ui.actions.FinalPointIcon
import org.elnix.dragonlauncher.ui.actions.actionLabel
import org.elnix.dragonlauncher.ui.base.activityViewModel
import org.elnix.dragonlauncher.ui.composition.LocalPointPreviewTitleOptions
import org.elnix.dragonlauncher.ui.helpers.swipe.PointIcon

@SuppressLint("UseOfNonLambdaOffsetOverload")
@Composable
fun PointPreviewTitle(
	point: Point?,
	useActionIcon: Boolean = false
) {
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
		PointPreview(point, useActionIcon)
	}
}

/**
 * @param useActionIcon Resolve the icon from [Point.action] instead of the point-id keyed
 *   [org.elnix.dragonlauncher.base.cache.PointStableCache]. A staged Cycle Actions stage reuses the
 *   point's id with a different action, so only an action-derived icon can reflect it live.
 */
@Composable
fun PointPreview(
	point: Point,
	useActionIcon: Boolean = false
) {
	val extraColors = LocalExtraColors.current
	val options = LocalPointPreviewTitleOptions.current
	val density = LocalDensity.current
	val pointsViewModel: PointsViewModel = activityViewModel()
	val defaultPoint by pointsViewModel.pointsService.defaultPoint.asState()

	val label = point.customName ?: actionLabel(point.action)

	Row(
		horizontalArrangement = Arrangement.spacedBy(5.dp),
		verticalAlignment = Alignment.CenterVertically
	) {
		if (options.showIcon) {
			when {
				useActionIcon || options.pointPreviewMode == PointPreviewMode.Legacy -> {
					FinalPointIcon(point, size = options.appIconOverlaySize)
				}

				else -> {
					val previewSize = options.appIconOverlaySize.coerceAtLeast(1.dp)
					val contentSize = maxOf(previewSize, point.getSize(defaultPoint, false))
					val contentScale =
						with(density) {
							previewSize.toPx() / contentSize.toPx()
						}
					Box(
						Modifier.requiredSize(previewSize),
						contentAlignment = Alignment.Center
					) {
						BoxWithConstraints(
							Modifier
								.requiredSize(contentSize)
								.graphicsLayer {
									scaleX = contentScale
									scaleY = contentScale
								},
							contentAlignment = Alignment.Center
						) {
							PointIcon(
								selected = false,
								point = point,
								center = constraints.getCenter(),
								isTopPoint = true,
								eraseColor = Color.Transparent
							)
						}
					}
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

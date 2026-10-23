package org.elnix.dragonlauncher.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.elnix.dragonlauncher.base.model.models.PointPreviewMode
import org.elnix.dragonlauncher.base.model.models.PointPreviewTitleOptions
import org.elnix.dragonlauncher.base.model.serializables.Point
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.ui.composition.LocalPointPreviewTitleOptions

@Composable
fun ActionPlacementTopBar(
	remaining: Int,
	currentPoint: Point
) {
	var oldCount by remember { mutableIntStateOf(remaining) }

	LaunchedEffect(remaining) {
		oldCount = remaining
	}

	Box(
		modifier =
			Modifier
				.fillMaxWidth()
				.padding(WindowInsets.safeDrawing.asPaddingValues())
				.padding(horizontal = 16.dp, vertical = 8.dp),
		contentAlignment = Alignment.TopCenter
	) {
		Column(
			horizontalAlignment = Alignment.CenterHorizontally,
			modifier =
				Modifier
					.background(
						MaterialTheme.colorScheme.primaryContainer,
						shape = MaterialTheme.shapes.medium
					).padding(horizontal = 16.dp, vertical = 12.dp)
		) {
			Text(
				text = stringResource(R.string.place_app_where),
				color = MaterialTheme.colorScheme.onPrimaryContainer,
				fontWeight = FontWeight.Bold,
				textAlign = TextAlign.Center,
				fontSize = 14.sp
			)

			CompositionLocalProvider(
				LocalPointPreviewTitleOptions provides PointPreviewTitleOptions(
					showIcon = true,
					showLabel = true,
					pointPreviewMode = PointPreviewMode.Legacy,
					appIconOverlaySize = 30.dp,
					appLabelOverlaySize = 18.sp,
					appIconOverlayScale = 1f,
					topPadding = Dp.Unspecified
				)
			) {
				PointPreview(currentPoint)
			}

			Row(
				verticalAlignment = Alignment.CenterVertically
			) {
				AnimatedContent(
					targetState = remaining,
					transitionSpec = {
						if (oldCount < remaining) {
							slideInVertically { it } togetherWith slideOutVertically { -it }
						} else {
							slideInVertically { -it } togetherWith slideOutVertically { it }
						}
					}
				) {
					Text(
						text = it.toString(),
						color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
						modifier = Modifier.padding(3.dp)
					)
				}
				Text(
					text = stringResource(R.string.multi_select_left_text),
					color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
					fontSize = 12.sp
				)
			}
		}
	}
}

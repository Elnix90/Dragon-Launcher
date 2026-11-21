package org.elnix.dragonlauncher.ui.welcome

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.elnix90.shared.ui.base.components.Spacer
import kotlinx.coroutines.delay
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.ui.components.BuildTypeChip
import org.elnix.dragonlauncher.ui.components.CodeNameChip
import org.elnix.dragonlauncher.ui.components.VersionCodeChip
import org.elnix.dragonlauncher.ui.components.VersionNumberChip
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun WelcomePageIntro(
	isVisible: Boolean
) {
	val headlinesAlpha =
		remember(isVisible) {
			List(3) { Animatable(initialValue = 0f) }
		}

	LaunchedEffect(isVisible) {
		delay(500.milliseconds)
		for (i in 0..2) {
			headlinesAlpha[i].animateTo(
				targetValue = 1f,
				animationSpec = tween(750)
			)
		}
	}

	Column(
		modifier = Modifier.fillMaxSize(),
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.Center
	) {
		Image(
			painter = painterResource(R.drawable.dragon_launcher_foreground),
			contentDescription = stringResource(R.string.app_name),
			modifier = Modifier.size(400.dp)
		)

		Spacer(32.dp)

		Text(
			stringResource(id = R.string.welcome_to_dragon_launcher),
			color = MaterialTheme.colorScheme.onBackground,
			style = MaterialTheme.typography.bodyLargeEmphasized,
			fontSize = 26.sp,
			textAlign = TextAlign.Center
		)

		Spacer(12.dp)

		Text(
			stringResource(id = R.string.app_tagline),
			color = MaterialTheme.colorScheme.secondary,
			style = MaterialTheme.typography.bodyLargeEmphasized,
			textAlign = TextAlign.Center
		)

		Spacer(20.dp)

		Row(
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.spacedBy(15.dp)
		) {
			VersionNumberChip()
			CodeNameChip()
			BuildTypeChip()
			VersionCodeChip()
		}

		Spacer(15.dp)

		repeat(3) { i ->
			val text =
				stringResource(
					when (i) {
						0 -> R.string.fast
						1 -> R.string.powerful_gestures
						else -> R.string.infinite_custom
					}
				)

			Text(
				text = text,
				color = MaterialTheme.colorScheme.tertiary.copy(alpha = headlinesAlpha[i].value),
				textAlign = TextAlign.Center,
				fontSize = 18.sp
			)
		}
	}
}

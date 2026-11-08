package org.elnix.dragonlauncher.ui.welcome

import android.annotation.SuppressLint
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalGridApi
import androidx.compose.foundation.layout.Grid
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.elnix90.shared.ui.animation.bouncySpec
import io.github.elnix90.shared.ui.base.components.Spacer
import io.github.elnix90.shared.ui.base.modifiers.conditional
import io.github.elnix90.shared.ui.base.remember.rememberInteractionSource
import org.elnix.dragonlauncher.base.Constants.URLs.DISCORD_INVITE_LINK
import org.elnix.dragonlauncher.i18n.R

@OptIn(ExperimentalGridApi::class)
@Composable
fun WelcomePageTutorial() {
	val uriHandler = LocalUriHandler.current

	WelcomePagerHeader {
		Grid(
			config = {
				column(1.fr)
				column(1.fr)
				row(1.fr)
				row(1.fr)
				row(1.fr)

				gap(10.dp)
			}
		) {
			TutorialEntry(R.mipmap.long_click_to_access_settings, R.string.long_click_to_access_settings)
			TutorialEntry(R.mipmap.configure_apps, R.string.configure_your_apps)
			TutorialEntry(R.mipmap.swipe_to_open_app, R.string.swipe_to_open_app)
			TutorialEntry(R.mipmap.customize_hold_settings, R.string.customize_hold_settings)
			TutorialEntry(R.mipmap.customize_angle_line, R.string.customize_angle_line)
			TutorialEntry(
				R.drawable.discord_symbol_blurple,
				R.string.share_your_config_in_the_discord,
				MaterialTheme.colorScheme.surfaceContainerHighest,
				70.dp
			) {
				uriHandler.openUri(DISCORD_INVITE_LINK)
			}
		}
	}
}

@SuppressLint("LocalContextGetResourceValueCall")
@Composable
private fun TutorialEntry(
	painterResId: Int,
	titleResId: Int,
	backgroundColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
	imageMaxSize: Dp = 150.dp,
	onCLick: (() -> Unit)? = null
) {
	val interactionSource = rememberInteractionSource()
	val isPressed by interactionSource.collectIsPressedAsState()

	val scale by animateFloatAsState(if (isPressed) 0.97f else 1f, bouncySpec(), label = "scale")

	Column(
		horizontalAlignment = Alignment.CenterHorizontally,
		modifier = Modifier
			.fillMaxSize()
			.scale(scale)
			.clip(MaterialTheme.shapes.extraLarge)
			.background(backgroundColor)
			.conditional(onCLick) {
				clickable(
					interactionSource = interactionSource,
					onClick = it
				)
			}.padding(top = 20.dp)
	) {
		Box(
			contentAlignment = Alignment.Center,
			modifier = Modifier
				.fillMaxWidth()
				.aspectRatio(1f)
		) {
			Image(
				painter = painterResource(painterResId),
				contentDescription = stringResource(titleResId),
				modifier = Modifier
					.clip(MaterialTheme.shapes.large)
					.size(imageMaxSize)
			)
		}

		Spacer(12.dp)

		Text(
			text = stringResource(titleResId),
			style = MaterialTheme.typography.labelMediumEmphasized,
			color = contentColorFor(backgroundColor),
			textAlign = TextAlign.Center,
			modifier = Modifier.padding(5.dp)
		)
	}
}

@Composable
@Preview
private fun WelcomePageTutorialPreview() {
	WelcomePagerHeader {
		WelcomePageTutorial()
	}
}

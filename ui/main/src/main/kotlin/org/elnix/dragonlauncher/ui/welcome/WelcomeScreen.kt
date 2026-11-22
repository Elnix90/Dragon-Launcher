package org.elnix.dragonlauncher.ui.welcome

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import `in`.hridayan.shapeindicators.ShapeIndicatorColumn
import `in`.hridayan.shapeindicators.ShapeIndicatorDefaults
import io.github.elnix90.shared.ui.base.animation.slideInHorizontalBouncy
import io.github.elnix90.shared.ui.base.animation.slideOutHorizontalBouncy
import io.github.elnix90.shared.ui.base.components.Spacer
import io.github.elnix90.shared.ui.base.components.VerticalScrollIndicator
import io.github.elnix90.shared.ui.dragon.components.DragonButton
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.base.navigation.NavigationRoute
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.ui.compositionslocals.LocalNavigator
import org.elnix.dragonlauncher.ui.settings.backup.ImportBackupButton
import org.elnix.dragonlauncher.ui.wellbeing.FloatingParticles
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/** Finger travel needed to arm the launch, in dp. */
private const val PULL_THRESHOLD_DP = 100

/** Log damping scale: the displayed pull eases toward 2x the threshold instead of stopping hard. */
private const val PULL_SOFT_CAP_DP = 320

/** Sheet height visible at rest, in dp. */
private const val SHEET_PEEK_DP = 50

private const val EXIT_DURATION_MS = 400

@SuppressLint("LocalContextGetResourceValueCall", "FrequentlyChangingValue")
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun WelcomeScreen(viewModel: WelcomeViewModel) {
	val navigator = LocalNavigator.current

	val pagerState = viewModel.pagerState
	val scope = rememberCoroutineScope()
	val haptic = LocalHapticFeedback.current
	val density = LocalDensity.current
	val screenHeightPx = LocalView.current.height.toFloat()

	val thresholdPx = with(density) { PULL_THRESHOLD_DP.dp.toPx() }
	val capPx = with(density) { PULL_SOFT_CAP_DP.dp.toPx() }
	val peekPx = with(density) { SHEET_PEEK_DP.dp.toPx() }
	val cancelTickFloorPx = with(density) { 8.dp.toPx() }

	LaunchedEffect(Unit) {
		delay(2.seconds)
		viewModel.showShapesScrollBar = true
	}

	val currentPage = pagerState.currentPage

	LaunchedEffect(currentPage) {
		// When the page is the first one
		if (currentPage == 0) {
			launch {
				delay(1.seconds)
				viewModel.showScrollIndicator = true
				delay(100.milliseconds)
				viewModel.showScrollIndicatorTwo = true
			}
		} else {
			viewModel.showScrollIndicator = false
			viewModel.showScrollIndicatorTwo = false
		}
	}

	// Prevent the user to quit
	BackHandler { }

	val travel by animateFloatAsState(
		targetValue = if (viewModel.canceling) 0f else viewModel.pullTarget,
		animationSpec = spring(
			dampingRatio = if (viewModel.canceling) 0.55f else 1f,
			stiffness = if (viewModel.canceling) Spring.StiffnessMediumLow else Spring.StiffnessHigh
		),
		label = "pullTravel"
	)

	// log damping: eases off instead of hitting a wall
	val displayOffset = travel / (1f + travel / capPx)
	val thresholdDisplay = thresholdPx / (1f + thresholdPx / capPx)
	val pullProgress = (displayOffset / thresholdDisplay).coerceIn(0f, 1f)
	val excess = ((displayOffset - thresholdDisplay) / (capPx - thresholdDisplay)).coerceIn(0f, 1f)
	val armed = travel >= thresholdPx

	LaunchedEffect(armed) {
		if (viewModel.hapticReady) haptic.performHapticFeedback(HapticFeedbackType.Confirm)
		viewModel.hapticReady = true
	}

	val bgAlpha = 1f - 0.5f * pullProgress - 0.5f * excess
	val contentScale = 1f - 0.05f * pullProgress - 0.05f * excess
	val contentAlpha = 1f - 0.4f * pullProgress - 0.2f * excess

	// 0 before the swipe midpoint, 1 once the last page is settled
	val pagePosition = pagerState.currentPage + pagerState.currentPageOffsetFraction
	val pageProgress = ((pagePosition - 1.5f) * 2f).coerceIn(0f, 1f)

	fun launchWelcomeExit() {
		if (viewModel.launching) return
		viewModel.launching = true
		haptic.performHapticFeedback(HapticFeedbackType.LongPress)
		viewModel.setAsSeen()
		scope.launch {
			viewModel.exitAnim.animateTo(1f, tween(EXIT_DURATION_MS))
			navigator.popBackMainScreen()
			navigator.go(NavigationRoute.PointsSettings)
		}
	}

	fun cancelPull() {
		if (travel > cancelTickFloorPx) haptic.performHapticFeedback(HapticFeedbackType.Confirm)
		viewModel.canceling = true
	}

	val nestedScrollConnection =
		remember {
			object : NestedScrollConnection {
				override fun onPreScroll(
					available: Offset,
					source: NestedScrollSource
				): Offset {
					if (source != NestedScrollSource.UserInput || viewModel.launching) return Offset.Zero

					// finger up = negative y, same convention AppDrawer relies on
					if (available.y < 0f) {
						val eligible =
							pagerState.currentPage == PAGES_NUMBER - 1 &&
								pagerState.currentPageOffsetFraction == 0f
						if (!eligible) return Offset.Zero

						viewModel.canceling = false
						// rebase on the displayed value so a re-grab mid spring-back stays continuous
						viewModel.pullTarget = (travel - available.y).coerceAtLeast(0f)
						return Offset(0f, available.y)
					}

					// finger down drains the pull first, the rest scrolls the pager
					if (available.y > 0f && travel > 0f) {
						viewModel.canceling = false
						val consumed = minOf(available.y, travel)
						viewModel.pullTarget = (travel - consumed).coerceAtLeast(0f)
						return Offset(0f, consumed)
					}

					return Offset.Zero
				}

				override suspend fun onPreFling(available: Velocity): Velocity {
					if (viewModel.launching) return available
					if (viewModel.canceling || travel <= 0f) return Velocity.Zero

					// shortcut: release is decided on travel alone; add a velocity guard only if accidental exits show up
					if (travel >= thresholdPx) launchWelcomeExit() else cancelPull()

					// consume the fling so momentum can't continue the gesture
					return available
				}
			}
		}

	Box(
		modifier =
			Modifier
				.fillMaxSize()
				.graphicsLayer { alpha = 1f - viewModel.exitAnim.value }
				.background(MaterialTheme.colorScheme.background.copy(bgAlpha))
	) {
		Box(
			modifier = Modifier
				.fillMaxSize()
				.padding(WindowInsets.safeDrawing.asPaddingValues())
		) {
			FloatingParticles()

			VerticalPager(
				state = pagerState,
				modifier = Modifier
					.fillMaxSize()
					.padding(horizontal = 24.dp)
					.nestedScroll(nestedScrollConnection)
			) { displayPage ->
				when (displayPage) {
					0 -> {
						WelcomePageIntro(currentPage < 2)
					}

					1 -> {
						WelcomePageTutorial()
					}

					2 -> {
						Box(
							modifier = Modifier
								.fillMaxSize()
								.graphicsLayer {
									scaleX = contentScale
									scaleY = contentScale
									alpha = contentAlpha
								}
						) {
							WelcomePageSettings()
						}
					}
				}
			}

			AnimatedVisibility(
				visible = viewModel.showShapesScrollBar && travel <= 0f,
				enter = slideInHorizontalBouncy,
				exit = slideOutHorizontalBouncy,
				modifier = Modifier
					.align(Alignment.CenterEnd)
					.padding(end = 12.dp)
			) {
				ShapeIndicatorColumn(
					pagerState = pagerState,
					verticalArrangement = Arrangement.spacedBy(10.dp),
					shuffleShapes = true,
					overflow = ShapeIndicatorDefaults.overflow(maxVisibleItems = PAGES_NUMBER)
				)
			}

			AnimatedVisibility(
				visible = currentPage == 0,
				modifier = Modifier
					.align(Alignment.TopEnd)
					.padding(12.dp)
			) {
				ImportBackupButton(
					onConfirm = {
						viewModel.setAsSeen()
						// Here I do not check the initialization of the launcher, as th user imports it's settings, and therefore, it is initialized!
						navigator.onBack()
					}
				) {
					DragonButton(
						onClick = it
					) {
						Icon(painterResource(R.drawable.download), null)
						Spacer(5.dp)
						Text(stringResource(R.string.import_settings))
					}
				}
			}

			VerticalScrollIndicator(
				visible = viewModel.showScrollIndicator,
				modifier = Modifier.padding(bottom = 30.dp)
			)

			VerticalScrollIndicator(
				visible = viewModel.showScrollIndicatorTwo,
				modifier = Modifier.padding(bottom = 25.dp)
			)
		}

		if (pageProgress > 0f) {
			StartSheet(
				modifier = Modifier.align(Alignment.BottomCenter),
				pageProgress = pageProgress,
				pullProgress = pullProgress,
				armed = armed,
				exitAnim = viewModel.exitAnim,
				peekPx = peekPx,
				screenHeightPx = screenHeightPx
			)
		}
	}
}

@Composable
private fun StartSheet(
	modifier: Modifier,
	pageProgress: Float,
	pullProgress: Float,
	armed: Boolean,
	exitAnim: Animatable<Float, AnimationVector1D>,
	peekPx: Float,
	screenHeightPx: Float
) {
	val popSpring = spring<Float>(dampingRatio = 0.55f, stiffness = Spring.StiffnessMediumLow)

	val cardColor by animateColorAsState(
		if (armed) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.surfaceContainer,
		spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessMediumLow)
	)
	val textColor by animateColorAsState(
		if (armed) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onSurface,
		spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessMediumLow)
	)
	val weight by animateFloatAsState(if (armed) 1000f else 650f, popSpring, label = "weight")
	val width by animateFloatAsState(if (armed) 125f else 100f, popSpring, label = "width")
	val scale by animateFloatAsState(if (armed) 1.05f else 1f, popSpring, label = "scale")

	val family = remember(weight, width) {
		FontFamily(
			Font(
				R.font.google_sans_flex,
				variationSettings = FontVariation.Settings(
					FontVariation.Setting("ROND", 100f),
					FontVariation.weight(weight.toInt().coerceIn(100, 1000)),
					FontVariation.width(width.coerceIn(25f, 151f))
				)
			)
		)
	}

	val safeBottom = WindowInsets.safeDrawing.asPaddingValues().calculateBottomPadding()
	val shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)

	Column(
		modifier = modifier
			.fillMaxWidth()
			.graphicsLayer {
				// hidden until the page arrives, peeking at rest, flush when pulled, off the top on launch
				val restY = size.height - peekPx * pageProgress
				translationY = restY * (1f - pullProgress) - exitAnim.value * (screenHeightPx + size.height)
			}.clip(shape)
			.background(cardColor)
			.padding(horizontal = 24.dp)
			.padding(top = 8.dp)
			.padding(bottom = safeBottom + 24.dp),
		horizontalAlignment = Alignment.CenterHorizontally
	) {
		Box(
			modifier = Modifier
				.padding(bottom = 4.dp)
				.size(width = 36.dp, height = 4.dp)
				.background(textColor.copy(alpha = 0.4f), RoundedCornerShape(2.dp))
		)

		Column(
			modifier = Modifier.graphicsLayer {
				scaleX = scale
				scaleY = scale
			},
			horizontalAlignment = Alignment.CenterHorizontally
		) {
			Text(
				text = stringResource(R.string.start_your),
				style = MaterialTheme.typography.displayLarge,
				fontFamily = family,
				overflow = TextOverflow.Visible,
				textAlign = TextAlign.Center,
				maxLines = 1,
				fontSize = 50.sp,
				lineHeight = 68.sp,
				color = textColor
			)
			Text(
				text = stringResource(R.string.journey),
				style = MaterialTheme.typography.displayLarge,
				fontFamily = family,
				fontSize = 70.sp,
				lineHeight = 80.sp,
				overflow = TextOverflow.Visible,
				maxLines = 1,
				textAlign = TextAlign.Center,
				color = textColor
			)
		}
	}
}

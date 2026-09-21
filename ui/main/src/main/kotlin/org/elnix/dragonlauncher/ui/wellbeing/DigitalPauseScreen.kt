package org.elnix.dragonlauncher.ui.wellbeing

import android.Manifest
import android.annotation.SuppressLint
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresPermission
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import io.github.elnix90.runtime.asState
import kotlinx.coroutines.delay
import org.elnix.dragonlauncher.base.model.models.Application
import org.elnix.dragonlauncher.base.utils.DateUtils.formatDuration
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.models.AppLaunchViewModel
import org.elnix.dragonlauncher.settings.stores.map.WellbeingSettingsStore
import org.elnix.dragonlauncher.ui.base.activityViewModel
import org.elnix.dragonlauncher.ui.base.components.Spacer
import org.elnix.dragonlauncher.ui.dragon.components.DragonButton
import java.util.Calendar
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds

private val ZenPurple = Color(0xFF6C5CE7)
private val ZenTeal = Color(0xFF00CEC9)
private val DeepBgTop = Color(0xFF0F2027)
private val DeepBgBottom = Color(0xFF203A43)
private val TextWhite = Color(0xFFEEEEEE)
private val TextSecondary = Color(0xFFB2BEC3)

@SuppressLint("MissingPermission")
@Composable
fun DigitalPauseScreen(
	application: Application,
	appLaunchViewModel: AppLaunchViewModel = activityViewModel(),
	onCancel: () -> Unit
) {
	val ctx = LocalContext.current
	val packageName = application.packageName

	val returnToLauncherEnabled by WellbeingSettingsStore.returnToLauncherEnabled.asState()
	val guiltModeEnabled by WellbeingSettingsStore.guiltModeEnabled.asState()
	val pauseDurationSeconds by WellbeingSettingsStore.pauseDurationSeconds.asState()

	var countdown by remember(pauseDurationSeconds) { mutableIntStateOf(pauseDurationSeconds) }
	var showChoice by remember { mutableStateOf(false) }
	var showTimePicker by remember { mutableStateOf(false) }
	var countdownFinished by remember { mutableStateOf(false) }
	var currentPhraseIndex by remember { mutableIntStateOf(0) }

	val hasUsageStatsPermission by appLaunchViewModel.hasUsageStatsPermission.collectAsState()
	val scrollState = rememberScrollState()
	// Shrink the lotus once the choice is shown so the action buttons
	// ("No, I'll pass" / "Yes, open anyway") stay visible on small screens,
	// especially when the guilt stats card grows (yearly line).
	val lotusSize = if (showChoice || showTimePicker) 120.dp else 220.dp

	// List of sentence to make user feel bad
	val breathingPhrases =
		listOf(
			stringResource(R.string.pause_breathe_1),
			stringResource(R.string.pause_breathe_2),
			stringResource(R.string.pause_breathe_3),
			stringResource(R.string.pause_breathe_4),
			stringResource(R.string.pause_breathe_5)
		)

	val usageStats =
		remember(packageName, guiltModeEnabled, hasUsageStatsPermission) {
			if (guiltModeEnabled && hasUsageStatsPermission) getUsageStats(ctx, packageName) else null
		}

	LaunchedEffect(pauseDurationSeconds) {
		while (countdown > 0) {
			delay(1.seconds)
			countdown--
			if (countdown % 3 == 0 && countdown > 0) {
				currentPhraseIndex = (currentPhraseIndex + 1) % breathingPhrases.size
			}
		}
		countdownFinished = true
		showChoice = true
	}

	BackHandler(onBack = onCancel)

	Surface(
		modifier = Modifier.fillMaxSize(),
		color = Color(0xFF0F111A)
	) {
		Box(modifier = Modifier.fillMaxSize()) {
			AuroraBackground()
			FloatingParticles()

			Column(
				horizontalAlignment = Alignment.CenterHorizontally,
				verticalArrangement = Arrangement.Center,
				modifier =
					Modifier
						.fillMaxSize()
						.verticalScroll(scrollState)
						.systemBarsPadding()
						.padding(24.dp)
			) {
				Text(
					text = application.label.uppercase(),
					style = MaterialTheme.typography.labelMedium,
					color = ZenTeal.copy(alpha = 0.9f),
					letterSpacing = 3.sp,
					textAlign = TextAlign.Center,
					maxLines = 1,
					overflow = TextOverflow.Ellipsis,
					modifier = Modifier.padding(horizontal = 16.dp)
				)

				Spacer(16.dp)

				LotusBloom(flowerSize = lotusSize, startIdle = showChoice)

				Spacer(32.dp)

				AnimatedVisibility(
					visible = !countdownFinished,
					enter = fadeIn() + expandVertically(),
					exit = fadeOut() + shrinkVertically()
				) {
					Column(
						horizontalAlignment = Alignment.CenterHorizontally,
						modifier = Modifier.height(180.dp)
					) {
						Text(
							text = countdown.toString(),
							style =
								MaterialTheme.typography.displayLarge.copy(
									fontWeight = FontWeight.ExtraLight,
									color = Color.White.copy(alpha = 0.9f)
								)
						)

						Spacer(24.dp)

						AnimatedContent(
							targetState = breathingPhrases[currentPhraseIndex],
							transitionSpec = {
								fadeIn(tween(1000)) togetherWith fadeOut(tween(500))
							},
							label = "text_fade"
						) { targetText ->
							Text(
								text = targetText,
								style =
									MaterialTheme.typography.headlineSmall.copy(
										fontStyle = FontStyle.Italic,
										fontWeight = FontWeight.Light,
										lineHeight = 34.sp
									),
								color = TextWhite,
								textAlign = TextAlign.Center,
								maxLines = 2,
								overflow = TextOverflow.Ellipsis,
								modifier = Modifier.padding(horizontal = 24.dp)
							)
						}
					}
				}

				AnimatedVisibility(
					visible = showChoice,
					enter = fadeIn(tween(600)) + slideInVertically { it / 4 },
					exit = fadeOut()
				) {
					Column(
						horizontalAlignment = Alignment.CenterHorizontally,
						modifier = Modifier.fillMaxWidth()
					) {
						Text(
							text = stringResource(R.string.pause_question),
							style = MaterialTheme.typography.headlineMedium,
							color = TextWhite,
							textAlign = TextAlign.Center,
							modifier = Modifier.padding(bottom = 8.dp)
						)

						Text(
							text = application.label.uppercase(),
							style = MaterialTheme.typography.labelMedium,
							color = ZenTeal.copy(alpha = 0.9f),
							letterSpacing = 3.sp,
							textAlign = TextAlign.Center,
							maxLines = 1,
							overflow = TextOverflow.Ellipsis,
							modifier = Modifier.padding(bottom = 28.dp)
						)

						if (guiltModeEnabled) {
							GlassCard(modifier = Modifier.fillMaxWidth()) {
								if (hasUsageStatsPermission && usageStats != null) {
									UsageStatsDisplay(usageStats)
								} else if (!hasUsageStatsPermission) {
									PermissionNeededContent(ctx)
								}
							}
							Spacer(32.dp)
						}

						// Cancel Button
						DragonButton(
							onClick = onCancel,
							modifier =
								Modifier
									.fillMaxWidth()
									.height(60.dp)
						) {
							Text(
								text = stringResource(R.string.pause_no_thanks).uppercase(),
								style = MaterialTheme.typography.labelLarge,
								letterSpacing = 1.sp
							)
						}

						Spacer(16.dp)

						// Button Continue
						TextButton(
							onClick = {
								if (returnToLauncherEnabled) {
									showChoice = false
									showTimePicker = true
								} else {
									appLaunchViewModel.onAppTimerServiceStarted(null)
									onCancel()
								}
							},
							colors = ButtonDefaults.textButtonColors(contentColor = TextSecondary)
						) {
							Text(
								text = stringResource(R.string.pause_yes_open),
								style = MaterialTheme.typography.labelLarge
							)
						}
					}
				}

				AnimatedVisibility(
					visible = showTimePicker,
					enter = fadeIn(tween(600)) + slideInVertically { it / 4 },
					exit = fadeOut()
				) {
					TimeLimitPickerUI(
						onConfirm = {
							appLaunchViewModel.onAppTimerServiceStarted(it)
							onCancel()
						},
						onCancel = onCancel
					)
				}
			}
		}
	}
}

@Composable
private fun TimeLimitPickerUI(
	onConfirm: (Int) -> Unit,
	onCancel: () -> Unit
) {
	val timeOptions = listOf(5, 10, 15, 20, 30, 45, 60)
	var selectedMinutes by remember { mutableIntStateOf(10) }

	val encouragementText =
		when {
			selectedMinutes <= 10 -> stringResource(R.string.time_limit_encourage_short)
			selectedMinutes <= 20 -> stringResource(R.string.time_limit_encourage_medium)
			selectedMinutes <= 30 -> stringResource(R.string.time_limit_encourage_long)
			else -> stringResource(R.string.time_limit_encourage_very_long)
		}

	val encourageColor =
		when {
			selectedMinutes <= 10 -> ZenTeal
			selectedMinutes <= 20 -> Color(0xFFFDCB6E)
			selectedMinutes <= 30 -> Color(0xFFFAB1A0)
			else -> Color(0xFFFF7675)
		}

	Column(
		horizontalAlignment = Alignment.CenterHorizontally,
		modifier = Modifier.fillMaxWidth()
	) {
		Text(
			text = stringResource(R.string.time_limit_picker_title),
			style = MaterialTheme.typography.headlineSmall,
			color = TextWhite,
			textAlign = TextAlign.Center,
			modifier = Modifier.padding(bottom = 8.dp)
		)

		Text(
			text = stringResource(R.string.time_limit_picker_subtitle),
			style = MaterialTheme.typography.bodyMedium,
			color = TextSecondary,
			textAlign = TextAlign.Center,
			modifier = Modifier.padding(bottom = 28.dp)
		)

		FlowRow(
			horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
			verticalArrangement = Arrangement.spacedBy(10.dp),
			modifier = Modifier.fillMaxWidth()
		) {
			timeOptions.forEach { minutes ->
				val isSelected = minutes == selectedMinutes
				val chipColor = if (isSelected) ZenTeal else Color.White.copy(alpha = 0.1f)
				val textColor = if (isSelected) Color.Black else TextWhite
				val borderColor = if (isSelected) ZenTeal else Color.White.copy(alpha = 0.2f)

				Box(
					contentAlignment = Alignment.Center,
					modifier =
						Modifier
							.clip(RoundedCornerShape(20.dp))
							.clickable { selectedMinutes = minutes }
							.background(chipColor)
							.border(1.dp, borderColor, RoundedCornerShape(20.dp))
							.padding(horizontal = 20.dp, vertical = 12.dp)
				) {
					Text(
						text = stringResource(R.string.time_limit_minutes, minutes),
						style =
							MaterialTheme.typography.labelLarge.copy(
								fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
							),
						color = textColor
					)
				}
			}
		}

		Spacer(24.dp)

		AnimatedContent(
			targetState = encouragementText,
			transitionSpec = {
				fadeIn(tween(400)) togetherWith fadeOut(tween(200))
			},
			label = "encourage"
		) { text ->
			Text(
				text = text,
				style =
					MaterialTheme.typography.bodyMedium.copy(
						fontStyle = FontStyle.Italic
					),
				color = encourageColor,
				textAlign = TextAlign.Center,
				modifier = Modifier.padding(horizontal = 16.dp)
			)
		}

		Spacer(32.dp)

		DragonButton(
			onClick = { onConfirm(selectedMinutes) },
			modifier =
				Modifier
					.fillMaxWidth()
					.height(56.dp)
		) {
			Text(
				text =
					stringResource(R.string.time_limit_start) + " · " +
						stringResource(R.string.time_limit_minutes, selectedMinutes),
				style = MaterialTheme.typography.labelLarge
			)
		}

		Spacer(12.dp)

		TextButton(
			onClick = onCancel,
			colors = ButtonDefaults.textButtonColors(contentColor = TextSecondary)
		) {
			Text(
				text = stringResource(R.string.time_limit_cancel),
				style = MaterialTheme.typography.labelLarge
			)
		}
	}
}

private enum class LotusPhase {
	Reveal,
	Idle
}

@Composable
private fun LotusBloom(flowerSize: Dp, startIdle: Boolean) {
	// Phase 1: reveal played once, held briefly once fully displayed.
	// Phase 2: idle animation, but only once the next screen (choice) is
	// shown; replayed in a loop, 3s after each end.
	val revealComposition by rememberLottieComposition(LottieCompositionSpec.Asset("lotus_reveal.json"))
	val idleComposition by rememberLottieComposition(LottieCompositionSpec.Asset("lotus_bloom.json"))

	var phase by remember { mutableStateOf(LotusPhase.Reveal) }
	var idleRun by remember { mutableIntStateOf(0) }

	val revealState = animateLottieCompositionAsState(
		composition = revealComposition,
		iterations = 1,
		isPlaying = phase == LotusPhase.Reveal
	)
	LaunchedEffect(revealState.isAtEnd, startIdle) {
		if (revealState.isAtEnd && phase == LotusPhase.Reveal && startIdle) {
			delay(1200)
			phase = LotusPhase.Idle
		}
	}

	Box(contentAlignment = Alignment.Center, modifier = Modifier.size(flowerSize)) {
		// Violet halo so the animation sits in the night scene
		Canvas(modifier = Modifier.fillMaxSize()) {
			drawCircle(
				brush =
					Brush.radialGradient(
						colors =
							listOf(
								ZenPurple.copy(alpha = 0.45f),
								Color(0xFF8E6BE8).copy(alpha = 0.15f),
								Color.Transparent
							),
						center = center,
						radius = size.minDimension / 2
					),
				radius = size.minDimension / 2
			)
		}

		AnimatedContent(
			targetState = phase,
			transitionSpec = {
				fadeIn(tween(500)) togetherWith fadeOut(tween(500))
			},
			label = "lotus_phase"
		) { currentPhase ->
			if (currentPhase == LotusPhase.Reveal) {
				LottieAnimation(
					composition = revealComposition,
					progress = { revealState.progress },
					modifier = Modifier.fillMaxSize()
				)
			} else {
				key(idleRun) {
					val idleState = animateLottieCompositionAsState(
						composition = idleComposition,
						iterations = 1,
						isPlaying = true
					)
					LottieAnimation(
						composition = idleComposition,
						progress = { idleState.progress },
						modifier = Modifier.fillMaxSize()
					)
					LaunchedEffect(idleState.isAtEnd) {
						if (idleState.isAtEnd) {
							delay(3000)
							idleRun++
						}
					}
				}
			}
		}
	}
}

@Composable
private fun AuroraBackground() {
	val infiniteTransition = rememberInfiniteTransition(label = "aurora")
	val colorShift by infiniteTransition.animateColor(
		initialValue = DeepBgTop,
		targetValue = Color(0xFF1A1A2E),
		animationSpec =
			infiniteRepeatable(
				animation = tween(10000, easing = LinearEasing),
				repeatMode = RepeatMode.Reverse
			),
		label = "bg_color"
	)

	Box(
		modifier =
			Modifier
				.fillMaxSize()
				.background(
					brush =
						Brush.verticalGradient(
							colors = listOf(colorShift, DeepBgBottom)
						)
				)
	)
}

@SuppressLint("UseOfNonLambdaOffsetOverload")
@Composable
fun FloatingParticles() {
	val particles =
		remember {
			List(15) {
				ParticleData(
					x = Random.nextFloat(),
					y = Random.nextFloat(),
					size = Random.nextInt(2, 5).dp,
					speed = Random.nextLong(4000, 9000)
				)
			}
		}

	Box(modifier = Modifier.fillMaxSize()) {
		particles.forEach { particle ->
			val infiniteTransition = rememberInfiniteTransition(label = "particle")
			val yOffset by infiniteTransition.animateFloat(
				initialValue = 0f,
				targetValue = -150f,
				animationSpec =
					infiniteRepeatable(
						animation = tween(particle.speed.toInt(), easing = LinearEasing),
						repeatMode = RepeatMode.Restart
					),
				label = "y"
			)
			val alpha by infiniteTransition.animateFloat(
				initialValue = 0f,
				targetValue = 0.5f,
				animationSpec =
					infiniteRepeatable(
						animation =
							keyframes {
								durationMillis = particle.speed.toInt()
								0f at 0
								0.5f at durationMillis / 2
								0f at durationMillis
							},
						repeatMode = RepeatMode.Restart
					),
				label = "alpha"
			)

			Box(
				modifier =
					Modifier
						.offset(
							x = (particle.x * 1000).dp,
							y = (particle.y * 2000).dp + yOffset.dp
						).size(particle.size)
						.alpha(alpha)
						.background(Color.White, CircleShape)
			)
		}
	}
}

private data class ParticleData(
	val x: Float,
	val y: Float,
	val size: Dp,
	val speed: Long
)

@Composable
private fun GlassCard(
	modifier: Modifier = Modifier,
	content: @Composable ColumnScope.() -> Unit
) {
	Column(
		modifier =
			modifier
				.clip(RoundedCornerShape(24.dp))
				.background(Color.White.copy(alpha = 0.08f))
				.border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(24.dp))
				.padding(24.dp),
		horizontalAlignment = Alignment.CenterHorizontally,
		content = content
	)
}

@Composable
private fun UsageStatsDisplay(stats: AppUsageStats) {
	Column(
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.spacedBy(12.dp)
	) {
		if (stats.yesterdayMinutes > 0) {
			Text(
				text = stringResource(R.string.usage_yesterday, stats.yesterdayMinutes.formatDuration()),
				style = MaterialTheme.typography.bodyLarge,
				color = TextSecondary,
				textAlign = TextAlign.Center
			)
			val yearlyHours = (stats.yesterdayMinutes * 365) / 60
			if (yearlyHours > 24) {
				val yearlyDays = yearlyHours / 24
				Spacer(4.dp)
				Text(
					text = stringResource(R.string.usage_guilt_yearly, "$yearlyDays days"),
					style =
						MaterialTheme.typography.bodyMedium.copy(
							fontWeight = FontWeight.Bold
						),
					color = Color(0xFFFF7675),
					textAlign = TextAlign.Center
				)
			}
		}

		HorizontalDivider(color = Color.White.copy(alpha = 0.1f), thickness = 1.dp)

		Text(
			text =
				if (stats.todayMinutes > 0) {
					stringResource(R.string.usage_today, stats.todayMinutes.formatDuration())
				} else {
					stringResource(R.string.not_used_yet)
				},
			style = MaterialTheme.typography.bodyMedium,
			color = TextWhite.copy(alpha = 0.8f),
			textAlign = TextAlign.Center
		)
	}
}

@Composable
private fun PermissionNeededContent(ctx: Context) {
	Column(horizontalAlignment = Alignment.CenterHorizontally) {
		Text(
			text = stringResource(R.string.usage_permission_needed),
			style = MaterialTheme.typography.bodyMedium,
			color = TextSecondary,
			textAlign = TextAlign.Center
		)
		Spacer(12.dp)
		OutlinedButton(
			onClick = {
				val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
				ctx.startActivity(intent)
			},
			border = BorderStroke(1.dp, ZenTeal),
			colors = ButtonDefaults.outlinedButtonColors(contentColor = ZenTeal)
		) {
			Text(stringResource(R.string.grant_usage_permission))
		}
	}
}

data class AppUsageStats(
	val yesterdayMinutes: Long,
	val todayMinutes: Long
)

@RequiresPermission(Manifest.permission.PACKAGE_USAGE_STATS)
private fun getUsageStats(ctx: Context, packageName: String): AppUsageStats? =
	try {
		val usageStatsManager = ctx.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
		val now = System.currentTimeMillis()
		val todayCal =
			Calendar.getInstance().apply {
				set(Calendar.HOUR_OF_DAY, 0)
				set(Calendar.MINUTE, 0)
				set(Calendar.SECOND, 0)
				set(Calendar.MILLISECOND, 0)
			}
		val todayStart = todayCal.timeInMillis
		val todayStats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, todayStart, now)
		val todayMinutes = todayStats.filter { it.packageName == packageName }.sumOf { it.totalTimeInForeground } / 60000
		val yesterdayStart = (todayCal.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, -1) }.timeInMillis
		val yesterdayStats =
			usageStatsManager.queryUsageStats(
				UsageStatsManager.INTERVAL_DAILY,
				yesterdayStart,
				todayStart
			)
		val yesterdayMinutes = yesterdayStats.filter { it.packageName == packageName }.sumOf { it.totalTimeInForeground } / 60000
		AppUsageStats(yesterdayMinutes, todayMinutes)
	} catch (e: Exception) {
		e.printStackTrace()
		null
	}

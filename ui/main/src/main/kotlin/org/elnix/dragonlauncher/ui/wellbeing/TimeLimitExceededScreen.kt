package org.elnix.dragonlauncher.ui.wellbeing

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.ui.base.components.Spacer
import org.elnix.dragonlauncher.ui.compositionslocals.LocalNavigator
import org.elnix.dragonlauncher.ui.dragon.components.DragonButton
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds

private val AccentTeal = Color(0xFF00CEC9)
private val AccentPurple = Color(0xFF6C5CE7)
private val WarmOrange = Color(0xFFFAB1A0)
private val TextWhite = Color(0xFFEEEEEE)
private val TextMuted = Color(0xFFB2BEC3)
private val BgTop = Color(0xFF0A0E21)
private val BgBottom = Color(0xFF1A1A2E)

@Composable
fun TimeLimitExceededScreen(appName: String) {
    var showContent by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(300.milliseconds)
        showContent = true
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GradientBackground()

        SoftParticles(modifier = Modifier.fillMaxSize())

        AnimatedVisibility(
            visible = showContent,
            enter =
                fadeIn(tween(800)) +
                    slideInVertically(
                        initialOffsetY = { it / 6 },
                        animationSpec = tween(800, easing = FastOutSlowInEasing)
                    )
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier =
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .systemBarsPadding()
                        .padding(horizontal = 32.dp, vertical = 24.dp)
            ) {
                PulsingHourglass(modifier = Modifier.size(120.dp))

                Spacer(32.dp)

                Text(
                    text = stringResource(R.string.time_exceeded_header),
                    style =
                        MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                    color = AccentTeal,
                    letterSpacing = 3.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(12.dp)

                Text(
                    text = stringResource(R.string.time_exceeded_title),
                    style = MaterialTheme.typography.displaySmall,
                    color = TextWhite,
                    textAlign = TextAlign.Center
                )

                Spacer(16.dp)

                Text(
                    text = stringResource(R.string.time_exceeded_message, appName),
                    style = MaterialTheme.typography.titleMedium,
                    color = WarmOrange,
                    textAlign = TextAlign.Center
                )

                Spacer(8.dp)

                Text(
                    text = appName.uppercase(),
                    style =
                        MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                    color = TextMuted,
                    letterSpacing = 2.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(12.dp)

                Text(
                    text = stringResource(R.string.time_exceeded_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(40.dp)

                GlassEncouragementCard()

                Spacer(32.dp)

                DragonButton(
                    onClick = LocalNavigator.current::onBack,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                ) {
                    Text(
                        text = stringResource(R.string.time_exceeded_ok),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}

@Composable
private fun GradientBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "bg")
    val shift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(8000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
        label = "bg_shift"
    )

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors =
                            listOf(
                                BgTop,
                                Color.lerp(BgBottom, AccentPurple.copy(alpha = 0.15f), shift)
                            )
                    )
                )
    )
}

private fun Color.Companion.lerp(a: Color, b: Color, t: Float): Color =
    Color(
        red = a.red + (b.red - a.red) * t,
        green = a.green + (b.green - a.green) * t,
        blue = a.blue + (b.blue - a.blue) * t,
        alpha = a.alpha + (b.alpha - a.alpha) * t
    )

@Composable
private fun PulsingHourglass(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "hourglass")

    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(3000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
        label = "scale"
    )

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(2000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
        label = "glow"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier =
            modifier.graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
    ) {
        Canvas(modifier = Modifier.size(120.dp)) {
            drawCircle(
                brush =
                    Brush.radialGradient(
                        colors =
                            listOf(
                                WarmOrange.copy(alpha = glowAlpha * 0.5f),
                                AccentPurple.copy(alpha = glowAlpha * 0.3f),
                                Color.Transparent
                            ),
                        center = center,
                        radius = size.minDimension / 2
                    )
            )
        }

        Text(
            text = "⏳",
            fontSize = 56.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun GlassEncouragementCard() {
    val messages =
        listOf(
            stringResource(R.string.encouragement_walk),
            stringResource(R.string.encouragement_water),
            stringResource(R.string.encouragement_stretch),
            stringResource(R.string.encouragement_window),
            stringResource(R.string.encouragement_text_someone)
        )
    val message = remember { messages.random() }

    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White.copy(alpha = 0.06f))
                .border(
                    1.dp,
                    Brush.linearGradient(
                        colors =
                            listOf(
                                AccentTeal.copy(alpha = 0.3f),
                                AccentPurple.copy(alpha = 0.2f),
                                Color.Transparent
                            )
                    ),
                    RoundedCornerShape(20.dp)
                ).padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style =
                MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium
                ),
            color = TextWhite.copy(alpha = 0.8f),
            textAlign = TextAlign.Center
        )
    }
}

@SuppressLint("UseOfNonLambdaOffsetOverload")
@Composable
private fun SoftParticles(modifier: Modifier = Modifier) {
    data class Particle(
        val x: Float,
        val y: Float,
        val size: Dp,
        val speed: Long
    )

    val particles =
        remember {
            List(10) {
                Particle(
                    x = Random.nextFloat(),
                    y = Random.nextFloat(),
                    size = Random.nextInt(2, 4).dp,
                    speed = Random.nextLong(5000, 10000)
                )
            }
        }

    Box(modifier = modifier) {
        particles.forEach { particle ->
            val infiniteTransition = rememberInfiniteTransition(label = "p")
            val yOffset by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = -100f,
                animationSpec =
                    infiniteRepeatable(
                        animation = tween(particle.speed.toInt(), easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    ),
                label = "y"
            )
            val alpha by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 0.4f,
                animationSpec =
                    infiniteRepeatable(
                        animation = tween(particle.speed.toInt()),
                        repeatMode = RepeatMode.Reverse
                    ),
                label = "a"
            )

            Box(
                modifier =
                    Modifier
                        .offset(
                            x = (particle.x * 400).dp,
                            y = (particle.y * 800).dp + yOffset.dp
                        ).size(particle.size)
                        .alpha(alpha)
                        .background(Color.White, CircleShape)
            )
        }
    }
}

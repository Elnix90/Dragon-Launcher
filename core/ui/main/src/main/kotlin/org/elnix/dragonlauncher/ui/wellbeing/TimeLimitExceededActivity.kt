package org.elnix.dragonlauncher.ui.wellbeing

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import kotlinx.coroutines.delay
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.theme.DragonLauncherTheme
import kotlin.random.Random

/**
 * Beautiful full-screen activity shown when the user's time limit on a paused app
 * has been exceeded. Brings them back to Dragon Launcher with a calming message.
 */
class TimeLimitExceededActivity : ComponentActivity() {

    companion object {
        const val EXTRA_APP_NAME = "extra_app_name"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val appName = intent.getStringExtra(EXTRA_APP_NAME)
            ?: applicationInfo.loadLabel(packageManager).toString()

        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        setContent {
            DragonLauncherTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF0A0E21)
                ) {
                    TimeLimitExceededScreen(
                        appName = appName,
                        onDismiss = { finish() }
                    )
                }
            }
        }
    }

    @SuppressLint("MissingSuperCall")
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        finish()
    }
}


// ─────────── Colors ───────────

private val AccentTeal = Color(0xFF00CEC9)
private val AccentPurple = Color(0xFF6C5CE7)
private val WarmOrange = Color(0xFFFAB1A0)
private val TextWhite = Color(0xFFEEEEEE)
private val TextMuted = Color(0xFFB2BEC3)
private val BgTop = Color(0xFF0A0E21)
private val BgBottom = Color(0xFF1A1A2E)


@Composable
private fun TimeLimitExceededScreen(
    appName: String,
    onDismiss: () -> Unit
) {
    var showContent by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(300)
        showContent = true
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Animated gradient background
        GradientBackground()

        // Floating particles for ambiance
        SoftParticles(modifier = Modifier.fillMaxSize())

        // Main content
        AnimatedVisibility(
            visible = showContent,
            enter = fadeIn(tween(800)) + slideInVertically(
                initialOffsetY = { it / 6 },
                animationSpec = tween(800, easing = FastOutSlowInEasing)
            )
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding()
                    .padding(32.dp)
            ) {

                // ── Pulsing Hourglass ──
                PulsingHourglass(modifier = Modifier.size(120.dp))

                Spacer(modifier = Modifier.height(40.dp))

                // ── Header ──
                Text(
                    text = stringResource(R.string.time_exceeded_header),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = FontFamily.SansSerif,
                    color = AccentTeal,
                    letterSpacing = 3.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                // ── Main Title ──
                Text(
                    text = stringResource(R.string.time_exceeded_title),
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif,
                    color = TextWhite,
                    textAlign = TextAlign.Center,
                    lineHeight = 44.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                // ── Message ──
                Text(
                    text = stringResource(R.string.time_exceeded_message, appName),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Normal,
                    color = WarmOrange,
                    textAlign = TextAlign.Center,
                    lineHeight = 26.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = stringResource(R.string.time_exceeded_subtitle),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Light,
                    color = TextMuted,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(48.dp))

                // ── Glass Card with encouragement ──
                GlassEncouragementCard()

                Spacer(modifier = Modifier.height(40.dp))

                // ── Action Button ──
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    shape = RoundedCornerShape(30.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentTeal),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.time_exceeded_ok),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        letterSpacing = 0.5.sp
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
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bg_shift"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        BgTop,
                        Color.lerp(BgBottom, AccentPurple.copy(alpha = 0.15f), shift)
                    )
                )
            )
    )
}

private fun Color.Companion.lerp(a: Color, b: Color, t: Float): Color {
    return Color(
        red = a.red + (b.red - a.red) * t,
        green = a.green + (b.green - a.green) * t,
        blue = a.blue + (b.blue - a.blue) * t,
        alpha = a.alpha + (b.alpha - a.alpha) * t
    )
}


@Composable
private fun PulsingHourglass(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "hourglass")

    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
    ) {
        // Glow ring
        Canvas(modifier = Modifier.size(120.dp)) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
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
    val messages = listOf(
        stringResource(R.string.encouragement_walk),
        stringResource(R.string.encouragement_water),
        stringResource(R.string.encouragement_stretch),
        stringResource(R.string.encouragement_window),
        stringResource(R.string.encouragement_text_someone)
    )
    val message = remember { messages.random() }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = 0.06f))
            .border(
                1.dp,
                Brush.linearGradient(
                    colors = listOf(
                        AccentTeal.copy(alpha = 0.3f),
                        AccentPurple.copy(alpha = 0.2f),
                        Color.Transparent
                    )
                ),
                RoundedCornerShape(20.dp)
            )
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = TextWhite.copy(alpha = 0.8f),
            textAlign = TextAlign.Center
        )
    }
}

@SuppressLint("UseOfNonLambdaOffsetOverload")
@Composable
private fun SoftParticles(modifier: Modifier = Modifier) {
    data class Particle(val x: Float, val y: Float, val size: Dp, val speed: Long)

    val particles = remember {
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
                animationSpec = infiniteRepeatable(
                    animation = tween(particle.speed.toInt(), easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "y"
            )
            val alpha by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 0.4f,
                animationSpec = infiniteRepeatable(
                    animation = tween(particle.speed.toInt()),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "a"
            )

            Box(
                modifier = Modifier
                    .offset(
                        x = (particle.x * 400).dp,
                        y = (particle.y * 800).dp + yOffset.dp
                    )
                    .size(particle.size)
                    .alpha(alpha)
                    .background(Color.White, CircleShape)
            )
        }
    }
}

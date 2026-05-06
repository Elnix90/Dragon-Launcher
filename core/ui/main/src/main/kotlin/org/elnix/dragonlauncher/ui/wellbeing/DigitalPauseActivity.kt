package org.elnix.dragonlauncher.ui.wellbeing

import android.annotation.SuppressLint
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import kotlinx.coroutines.delay
import org.elnix.dragonlauncher.common.R
import org.elnix.dragonlauncher.common.utils.DateUtils.formatDuration
import org.elnix.dragonlauncher.common.utils.PermissionsUtils.hasUsageStatsPermission
import org.elnix.dragonlauncher.theme.DragonLauncherTheme
import java.util.Calendar
import kotlin.random.Random

class DigitalPauseActivity : ComponentActivity() {

    companion object {
        const val EXTRA_PACKAGE_NAME = "extra_package_name"
        const val EXTRA_APP_NAME = "extra_app_name"
        const val EXTRA_PAUSE_DURATION = "extra_pause_duration"
        const val EXTRA_GUILT_MODE = "extra_guilt_mode"

        // New: Reminder mode
        const val EXTRA_REMINDER_ENABLED = "extra_reminder_enabled"
        const val EXTRA_REMINDER_INTERVAL = "extra_reminder_interval"
        const val EXTRA_REMINDER_MODE = "extra_reminder_mode"

        // New: Return-to-launcher mode
        const val EXTRA_RETURN_TO_LAUNCHER = "extra_return_to_launcher"

        const val RESULT_PROCEED = 1
        const val RESULT_PROCEED_WITH_TIMER = 2
        const val RESULT_CANCEL = 0

        const val RESULT_EXTRA_TIME_LIMIT = "result_time_limit_minutes"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val packageName = intent.getStringExtra(EXTRA_PACKAGE_NAME) ?: run {
            finish()
            return
        }
        val pauseDuration = intent.getIntExtra(EXTRA_PAUSE_DURATION, 10)
        val guiltMode = intent.getBooleanExtra(EXTRA_GUILT_MODE, false)
        val returnToLauncher = intent.getBooleanExtra(EXTRA_RETURN_TO_LAUNCHER, false)
        val reminderEnabled = intent.getBooleanExtra(EXTRA_REMINDER_ENABLED, false)
        val reminderInterval = intent.getIntExtra(EXTRA_REMINDER_INTERVAL, 5)
        val reminderMode = intent.getStringExtra(EXTRA_REMINDER_MODE) ?: "overlay"

        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        setContent {
            DragonLauncherTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF0F111A)
                ) {
                    DigitalPauseScreen(
                        packageName = packageName,
                        pauseDuration = pauseDuration,
                        guiltMode = guiltMode,
                        returnToLauncherEnabled = returnToLauncher,
                        onProceed = {
                            setResult(RESULT_PROCEED)
                            finish()
                        },
                        onProceedWithTimer = { timeLimitMinutes ->
                            val data = Intent().apply {
                                putExtra(RESULT_EXTRA_TIME_LIMIT, timeLimitMinutes)
                                putExtra(EXTRA_REMINDER_ENABLED, reminderEnabled)
                                putExtra(EXTRA_REMINDER_INTERVAL, reminderInterval)
                                putExtra(EXTRA_REMINDER_MODE, reminderMode)
                            }
                            setResult(RESULT_PROCEED_WITH_TIMER, data)
                            finish()
                        },
                        onCancel = {
                            setResult(RESULT_CANCEL)
                            finish()
                        }
                    )
                }
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        setResult(RESULT_CANCEL)
        @Suppress("DEPRECATION")
        super.onBackPressed()
    }
}

// ─── Couleurs et Styles ───
private val ZenPurple = Color(0xFF6C5CE7)
private val ZenTeal = Color(0xFF00CEC9)
private val DeepBgTop = Color(0xFF0F2027)
private val DeepBgBottom = Color(0xFF203A43)
private val TextWhite = Color(0xFFEEEEEE)
private val TextSecondary = Color(0xFFB2BEC3)

@Composable
fun DigitalPauseScreen(
    packageName: String,
    pauseDuration: Int,
    guiltMode: Boolean,
    returnToLauncherEnabled: Boolean = false,
    onProceed: () -> Unit,
    onProceedWithTimer: (Int) -> Unit = {},
    onCancel: () -> Unit
) {
    val ctx = LocalContext.current
    var countdown by remember { mutableIntStateOf(pauseDuration) }
    var showChoice by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var countdownFinished by remember { mutableStateOf(false) }
    var currentPhraseIndex by remember { mutableIntStateOf(0) }


    // List of sentence to make user feel bad
    val breathingPhrases = listOf(
        stringResource(R.string.pause_breathe_1),
        stringResource(R.string.pause_breathe_2),
        stringResource(R.string.pause_breathe_3),
        stringResource(R.string.pause_breathe_4),
        stringResource(R.string.pause_breathe_5)
    )

    val usageStats = remember(packageName, guiltMode) {
        if (guiltMode && hasUsageStatsPermission(ctx)) getUsageStats(ctx, packageName) else null
    }
    val hasPermission = remember { hasUsageStatsPermission(ctx) }

    // Logic Timer
    LaunchedEffect(Unit) {
        while (countdown > 0) {
            delay(1000)
            countdown--
            if (countdown % 3 == 0 && countdown > 0) {
                currentPhraseIndex = (currentPhraseIndex + 1) % breathingPhrases.size
            }
        }
        countdownFinished = true
        showChoice = true
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AuroraBackground()
        FloatingParticles(modifier = Modifier.fillMaxSize())

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(24.dp)
        ) {

            // ─── LOTUS ───
            AnimatedLotus(
                modifier = Modifier.size(220.dp),
                isPulsing = !countdownFinished
            )

            // ─── TIMER ───
            Spacer(modifier = Modifier.height(32.dp))

            AnimatedVisibility(
                visible = !countdownFinished,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = countdown.toString(),
                        fontSize = 64.sp, // Très grand et fin
                        fontWeight = FontWeight.ExtraLight,
                        fontFamily = FontFamily.SansSerif,
                        color = Color.White.copy(alpha = 0.9f)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Phrase de respiration
                    BreathingText(text = breathingPhrases[currentPhraseIndex])
                }
            }

            // ─── FINAL CHOICE ───
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
                        fontSize = 26.sp,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Medium,
                        color = TextWhite,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 32.dp)
                    )

                    if (guiltMode) {
                        GlassCard(modifier = Modifier.fillMaxWidth()) {
                            if (hasPermission && usageStats != null) {
                                UsageStatsDisplay(usageStats)
                            } else if (!hasPermission) {
                                PermissionNeededContent(ctx)
                            }
                        }
                        Spacer(modifier = Modifier.height(32.dp))
                    }

                    // Cancel Button
                    Button(
                        onClick = onCancel,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        shape = RoundedCornerShape(30.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ZenTeal),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.pause_no_thanks).uppercase(),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            letterSpacing = 1.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Button Continue
                    TextButton(
                        onClick = {
                            if (returnToLauncherEnabled) {
                                showChoice = false
                                showTimePicker = true
                            } else {
                                onProceed()
                            }
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = TextSecondary)
                    ) {
                        Text(
                            text = stringResource(R.string.pause_yes_open),
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // ── TIME LIMIT PICKER ──
            AnimatedVisibility(
                visible = showTimePicker,
                enter = fadeIn(tween(600)) + slideInVertically { it / 4 },
                exit = fadeOut()
            ) {
                TimeLimitPickerUI(
                    onConfirm = { minutes -> onProceedWithTimer(minutes) },
                    onCancel = onCancel
                )
            }
        }
    }
}


// ─────────── Time Limit Picker UI ───────────

@Composable
private fun TimeLimitPickerUI(
    onConfirm: (Int) -> Unit,
    onCancel: () -> Unit
) {
    val timeOptions = listOf(5, 10, 15, 20, 30, 45, 60)
    var selectedMinutes by remember { mutableIntStateOf(10) }

    val encouragementText = when {
        selectedMinutes <= 10 -> stringResource(R.string.time_limit_encourage_short)
        selectedMinutes <= 20 -> stringResource(R.string.time_limit_encourage_medium)
        selectedMinutes <= 30 -> stringResource(R.string.time_limit_encourage_long)
        else -> stringResource(R.string.time_limit_encourage_very_long)
    }

    val encourageColor = when {
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
            fontSize = 24.sp,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Medium,
            color = TextWhite,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = stringResource(R.string.time_limit_picker_subtitle),
            fontSize = 14.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 28.dp)
        )

        // ── Time chips ──
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
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { selectedMinutes = minutes }
                        .background(chipColor)
                        .border(1.dp, borderColor, RoundedCornerShape(20.dp))
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.time_limit_minutes, minutes),
                        fontSize = 15.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = textColor
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ── Encouragement message ──
        AnimatedContent(
            targetState = encouragementText,
            transitionSpec = {
                fadeIn(tween(400)) togetherWith fadeOut(tween(200))
            },
            label = "encourage"
        ) { text ->
            Text(
                text = text,
                fontSize = 14.sp,
                fontStyle = FontStyle.Italic,
                color = encourageColor,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // ── Start button ──
        Button(
            onClick = { onConfirm(selectedMinutes) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ZenTeal),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
        ) {
            Text(
                text = stringResource(R.string.time_limit_start) + " · " +
                        stringResource(R.string.time_limit_minutes, selectedMinutes),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                letterSpacing = 0.5.sp
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        TextButton(
            onClick = onCancel,
            colors = ButtonDefaults.textButtonColors(contentColor = TextSecondary)
        ) {
            Text(
                text = stringResource(R.string.time_limit_cancel),
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun AnimatedLotus(
    modifier: Modifier = Modifier,
    isPulsing: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "lotus")

    // Slow rotation for ZEN effect
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(60000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    // Breathing (Scale)
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isPulsing) 1.1f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Canvas(modifier = modifier.graphicsLayer {
        scaleX = pulseScale
        scaleY = pulseScale
        rotationZ = rotation
    }) {
        val centerX = size.width / 2
        val centerY = size.height / 2
        val radius = size.minDimension / 2.2f

        // 1. Glow global derrière
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(ZenPurple.copy(alpha = 0.4f), Color.Transparent),
                center = center,
                radius = radius * 1.5f
            ),
            radius = radius * 1.5f
        )

        // Local function that draw a petal layer
        fun drawPetalLayer(count: Int, scale: Float, alphaMult: Float, colorOffset: Int) {
            for (i in 0 until count) {
                val angle = (360f / count) * i
                // uses HSV/HSL via color copy to vary
                val baseColor = if ((i + colorOffset) % 2 == 0) Color(0xFFE056FD) else Color(0xFF686DE0)

                rotate(angle, pivot = center) {
                    val path = Path().apply {
                        val r = radius * scale
                        moveTo(centerX, centerY)
                        // Petal larger at root (cubicTo adjusted)
                        // Left
                        cubicTo(
                            centerX + r * 0.35f, centerY - r * 0.4f, // Control 1 (larger)
                            centerX + r * 0.15f, centerY - r * 0.95f, // Control 2 (tip)
                            centerX, centerY - r                      // Summit
                        )
                        // Right
                        cubicTo(
                            centerX - r * 0.15f, centerY - r * 0.95f,
                            centerX - r * 0.35f, centerY - r * 0.4f,
                            centerX, centerY
                        )
                        close()
                    }

                    drawPath(
                        path = path,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                baseColor.copy(alpha = 0.9f * alphaMult),
                                baseColor.copy(alpha = 0.3f * alphaMult)
                            ),
                            start = Offset(centerX, centerY),
                            end = Offset(centerX, centerY - radius * scale)
                        )
                    )
                    // Thin outline
                    androidx.compose.ui.graphics.drawscope.Stroke(
                        width = 1.dp.toPx(),
                        pathEffect = null
                    ).let { stroke ->
                        drawPath(path, Color.White.copy(alpha = 0.3f * alphaMult), style = stroke)
                    }
                }
            }
        }

        // LAYER 1: More petals
        drawPetalLayer(count = 12, scale = 1.0f, alphaMult = 0.7f, colorOffset = 0)

        // Layer 2, a little offset, less petals
        rotate(15f, pivot = center) { // angular offset to fill gaps
            drawPetalLayer(count = 8, scale = 0.75f, alphaMult = 1.0f, colorOffset = 1)
        }

        // LOTUS HEART (light)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color.White, Color(0xFFFFD700).copy(alpha = 0.5f), Color.Transparent),
                center = center,
                radius = radius * 0.2f
            ),
            radius = radius * 0.2f
        )
    }
}

@Composable
private fun AuroraBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "aurora")
    val colorShift by infiniteTransition.animateColor(
        initialValue = DeepBgTop,
        targetValue = Color(0xFF1A1A2E),
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bg_color"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(colorShift, DeepBgBottom)
                )
            )
    )
}

@SuppressLint("UseOfNonLambdaOffsetOverload")
@Composable
private fun FloatingParticles(modifier: Modifier = Modifier) {
    val particles = remember {
        List(15) {
            ParticleData(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                size = Random.nextInt(2, 5).dp,
                speed = Random.nextLong(4000, 9000)
            )
        }
    }

    Box(modifier = modifier) {
        particles.forEach { particle ->
            val infiniteTransition = rememberInfiniteTransition(label = "particle")
            val yOffset by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = -150f,
                animationSpec = infiniteRepeatable(
                    animation = tween(particle.speed.toInt(), easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "y"
            )
            val alpha by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 0.5f,
                animationSpec = infiniteRepeatable(
                    animation = keyframes {
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
                modifier = Modifier
                    .offset(
                        x = (particle.x * 1000).dp,
                        y = (particle.y * 2000).dp + yOffset.dp
                    )
                    .size(particle.size)
                    .alpha(alpha)
                    .background(Color.White, CircleShape)
            )
        }
    }
}

data class ParticleData(val x: Float, val y: Float, val size: Dp, val speed: Long)

@Composable
private fun GlassCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White.copy(alpha = 0.08f))
            .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(24.dp))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        content = content
    )
}

@Composable
private fun BreathingText(text: String) {
    AnimatedContent(
        targetState = text,
        transitionSpec = {
            fadeIn(tween(1000)) togetherWith fadeOut(tween(500))
        }, label = "text_fade"
    ) { targetText ->
        Text(
            text = targetText,
            fontSize = 28.sp,
            fontFamily = FontFamily.Serif,
            fontStyle = FontStyle.Italic,
            fontWeight = FontWeight.Light,
            color = TextWhite,
            textAlign = TextAlign.Center,
            lineHeight = 36.sp,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
    }
}


@Composable
private fun UsageStatsDisplay(stats: AppUsageStats) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        if (stats.yesterdayMinutes > 0) {
            Text(
                text = stringResource(R.string.usage_yesterday, stats.yesterdayMinutes.formatDuration()),
                fontSize = 16.sp,
                color = TextSecondary
            )
            val yearlyHours = (stats.yesterdayMinutes * 365) / 60
            if (yearlyHours > 24) {
                val yearlyDays = yearlyHours / 24
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.usage_guilt_yearly, "$yearlyDays days"),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFF7675)
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        HorizontalDivider(color = Color.White.copy(alpha=0.1f), thickness = 1.dp)
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = if (stats.todayMinutes > 0)
                stringResource(R.string.usage_today, stats.todayMinutes.formatDuration())
            else stringResource(R.string.not_used_yet),
            fontSize = 14.sp,
            color = TextWhite.copy(alpha = 0.8f)
        )
    }
}

@Composable
private fun PermissionNeededContent(ctx: Context) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = stringResource(R.string.usage_permission_needed),
            fontSize = 14.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedButton(
            onClick = {
                val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                ctx.startActivity(intent)
            },
            border = androidx.compose.foundation.BorderStroke(1.dp, ZenTeal),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = ZenTeal)
        ) {
            Text(stringResource(R.string.grant_usage_permission))
        }
    }
}

data class AppUsageStats(val yesterdayMinutes: Long, val todayMinutes: Long)


private fun getUsageStats(ctx: Context, packageName: String): AppUsageStats? {
    return try {
        val usageStatsManager = ctx.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val calendar = Calendar.getInstance()
        val todayStart = calendar.apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }.timeInMillis
        val now = System.currentTimeMillis()
        val todayStats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, todayStart, now)
        val todayMinutes = todayStats.filter { it.packageName == packageName }.sumOf { it.totalTimeInForeground } / 60000
        val yesterdayStart = calendar.apply { add(Calendar.DAY_OF_YEAR, -1) }.timeInMillis
        val yesterdayStats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, yesterdayStart,
            todayStart
        )
        val yesterdayMinutes = yesterdayStats.filter { it.packageName == packageName }.sumOf { it.totalTimeInForeground } / 60000
        AppUsageStats(yesterdayMinutes, todayMinutes)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

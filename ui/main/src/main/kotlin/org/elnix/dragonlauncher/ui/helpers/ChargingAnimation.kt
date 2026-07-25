package org.elnix.dragonlauncher.ui.helpers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.compose.animation.core.withInfiniteAnimationFrameMillis
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.milliseconds


// Copied from Kvastisto
// https://github.com/MM2-0/Kvaesitso/blob/07e3e9669f8990e76d9d4062492dc741793e49a5/app/ui/src/main/java/de/mm20/launcher2/ui/component/NavBarEffects.kt

@Composable
fun ChargingAnimation(
    modifier: Modifier = Modifier
) {
    val ctx = LocalContext.current

    var intensity by remember { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope()

    DisposableEffect(null) {

        suspend fun update(intent: Intent?, retryOnZeroCurrent: Boolean = false) {
            if (intent == null) return
            val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
            val charging =
                status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
            if (charging) {
                val bm = ctx.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
                val current = bm.getLongProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
                if (current <= 0) {
                    intensity = 5
                    //Workaround for delayed current updates
                    if (retryOnZeroCurrent) {
                        delay(1000.milliseconds)
                        update(intent)
                    }
                    return
                }
                intensity = (current / 100000f).roundToInt().takeIf { it > 0 } ?: 1
            } else {
                intensity = 0
            }
        }

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                scope.launch {
                    update(intent, false)
                }
            }
        }

        val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)

        val intent = ctx.registerReceiver(receiver, intentFilter)
        scope.launch {
            update(intent, true)
        }

        onDispose {
            ctx.unregisterReceiver(receiver)
        }
    }

    var bubbles by remember { mutableStateOf(arrayOf<Bubble>()) }

    val dp = LocalDensity.current.density

    LaunchedEffect(intensity) {
        bubbles = Array(intensity) {
            FloatArray(6)
        }
        if (intensity == 0) return@LaunchedEffect
        while (isActive) {
            val newBubbles = Array(intensity) { FloatArray(6) }
            withInfiniteAnimationFrameMillis {}
            for (i in 0 until intensity) {
                val bubble = newBubbles[i]
                val oldBubble = bubbles[i]
                if (oldBubble.lifetime <= 0f) {
                    bubble.posX = (Math.random() * 48 - 24).toFloat() * dp
                    bubble.posY = 0f
                    bubble.deltaX = (Math.random() - 0.5).toFloat() * 1f * dp
                    bubble.deltaY = Math.random().toFloat() * 3f * dp
                    bubble.radius = (Math.random() * 2 + 2).toFloat() * dp
                    bubble.lifetime = (Math.random() * 80 + 40).toInt().toFloat()
                } else {
                    bubble.posX = oldBubble.deltaX + oldBubble.posX
                    bubble.posY = oldBubble.deltaY + oldBubble.posY
                    bubble.lifetime = oldBubble.lifetime - 1
                    bubble.deltaX = oldBubble.deltaX
                    bubble.deltaY = oldBubble.deltaY
                    bubble.radius = oldBubble.radius
                }
            }
            bubbles = newBubbles
        }
    }

    Canvas(modifier = modifier) {
        for (bubble in bubbles) {
            val x = size.width / 2 + bubble.posX
            val y = size.height - bubble.posY
            drawCircle(
                color = Color.White,
                radius = bubble.radius,
                alpha = bubble.lifetime / 255,
                center = Offset(x, y)
            )
        }
    }
}

typealias Bubble = FloatArray

inline var Bubble.posX: Float
    get() = this[0]
    set(value) {
        this[0] = value
    }

inline var Bubble.posY: Float
    get() = this[1]
    set(value) {
        this[1] = value
    }

inline var Bubble.deltaX: Float
    get() = this[2]
    set(value) {
        this[2] = value
    }

inline var Bubble.deltaY: Float
    get() = this[3]
    set(value) {
        this[3] = value
    }

inline var Bubble.radius: Float
    get() = this[4]
    set(value) {
        this[4] = value
    }

inline var Bubble.lifetime: Float
    get() = this[5]
    set(value) {
        this[5] = value
    }
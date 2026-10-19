package org.elnix.dragonlauncher.ui.helpers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.view.Surface
import androidx.compose.animation.core.withInfiniteAnimationFrameMillis
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import io.github.elnix90.runtime.asState
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.settings.stores.map.ChargingAnimationSettingsStore
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.milliseconds

// Copied from Kvastisto
// https://github.com/MM2-0/Kvaesitso/blob/07e3e9669f8990e76d9d4062492dc741793e49a5/app/ui/src/main/java/de/mm20/launcher2/ui/component/NavBarEffects.kt

@Composable
fun ChargingAnimation() {
	val ctx = LocalContext.current
	val density = LocalDensity.current.density
	val scope = rememberCoroutineScope()

	val sizeMultiplier by ChargingAnimationSettingsStore.sizeMultiplier.asState()

	val amountMultiplier by ChargingAnimationSettingsStore.amountMultiplier.asState()
	val amountDerived by rememberUpdatedState(amountMultiplier)

	val color by ChargingAnimationSettingsStore.color.asState()

	var intensity by remember { mutableIntStateOf(0) }

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
					// Workaround for delayed current updates
					if (retryOnZeroCurrent) {
						delay(1000.milliseconds)
						update(intent)
					}
					return
				}
				intensity = ((current / 100000f) * amountDerived).roundToInt().takeIf { it > 0 } ?: 1
			} else {
				intensity = 0
			}
		}

		val receiver =
			object : BroadcastReceiver() {
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

	LaunchedEffect(intensity, amountDerived) {
		val currentIntensity = intensity * amountDerived

		bubbles =
			Array(currentIntensity) {
				FloatArray(6)
			}
		if (currentIntensity == 0) return@LaunchedEffect
		while (isActive) {
			val newBubbles = Array(currentIntensity) { FloatArray(6) }
			withInfiniteAnimationFrameMillis {}
			for (i in 0 until currentIntensity) {
				val bubble = newBubbles[i]
				val oldBubble = bubbles[i]
				if (oldBubble.lifetime <= 0f) {
					bubble.posX = (Math.random() * 48 - 24).toFloat() * density
					bubble.posY = 0f
					bubble.deltaX = (Math.random() - 0.5).toFloat() * 1f * density
					bubble.deltaY = Math.random().toFloat() * 3f * density
					bubble.radius = (Math.random() * 2 + 2).toFloat() * density
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

	val view = LocalView.current
	LocalConfiguration.current // read it so we recompose on rotation
	val degrees = when (view.display?.rotation) {
		Surface.ROTATION_90 -> -90f
		Surface.ROTATION_180 -> 180f
		Surface.ROTATION_270 -> 90f
		else -> 0f
	}

	Canvas(modifier = Modifier.fillMaxSize()) {
		// In landscape, the "height" of the virtual portrait space is the canvas width
		val h = if (degrees == 90f || degrees == -90f) size.width else size.height

		rotate(degrees, pivot = center) {
			for (bubble in bubbles) {
				val x = center.x + bubble.posX
				val y = center.y + h / 2 - bubble.posY

				drawCircle(
					color = color,
					radius = bubble.radius * sizeMultiplier,
					alpha = bubble.lifetime / 255f,
					center = Offset(x, y)
				)
			}
		}
	}
}

private typealias Bubble = FloatArray

private inline var Bubble.posX: Float
	get() = this[0]
	set(value) {
		this[0] = value
	}

private inline var Bubble.posY: Float
	get() = this[1]
	set(value) {
		this[1] = value
	}

private inline var Bubble.deltaX: Float
	get() = this[2]
	set(value) {
		this[2] = value
	}

private inline var Bubble.deltaY: Float
	get() = this[3]
	set(value) {
		this[3] = value
	}

private inline var Bubble.radius: Float
	get() = this[4]
	set(value) {
		this[4] = value
	}

private inline var Bubble.lifetime: Float
	get() = this[5]
	set(value) {
		this[5] = value
	}

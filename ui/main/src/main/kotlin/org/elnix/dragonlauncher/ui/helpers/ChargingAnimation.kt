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
import androidx.compose.runtime.neverEqualPolicy
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.settings.stores.map.ChargingAnimationSettingsStore
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.milliseconds

// Copied from Kvaesitso
// https://github.com/MM2-0/Kvaesitso/blob/07e3e9669f8990e76d9d4062492dc741793e49a5/app/ui/src/main/java/de/mm20/launcher2/ui/component/NavBarEffects.kt

/**
 * Hard cap on simultaneously drawn bubbles so a fast charger (or a misbehaving current
 * sensor reporting an absurd value) can't spawn thousands of circles per frame.
 */
private const val MAX_BUBBLES = 300

/** The physics below were tuned for 60 fps; one reference frame is ~16.67 ms. */
private const val REFERENCE_FRAME_MS = 1000f / 60f

/**
 * Longest single step we ever apply, so a frame stall (GC pause, screen off, window
 * switch, ...) doesn't teleport every bubble across the screen.
 */
private const val MAX_FRAME_MS = REFERENCE_FRAME_MS * 3f

private val EMPTY_BUBBLES = arrayOf<Bubble>()

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
				intensity = (current / 100000f).roundToInt().takeIf { it > 0 } ?: 1
			} else {
				intensity = 0
			}
		}

		// BATTERY_PROPERTY_CURRENT_NOW is a binder call into the battery HAL that can block
		// for several milliseconds on low-end devices. Run every update on a single
		// off-main lane: broadcasts stay serialized (no stale overwrites) and the main
		// thread never stalls. Only the latest broadcast matters, so a pending/retrying
		// update is canceled when a new one arrives.
		val updateDispatcher = Dispatchers.IO.limitedParallelism(1)
		var updateJob: Job? = null

		val receiver =
			object : BroadcastReceiver() {
				override fun onReceive(ctx: Context?, intent: Intent?) {
					updateJob?.cancel()
					updateJob = scope.launch(updateDispatcher) {
						update(intent, false)
					}
				}
			}

		val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)

		val intent = ctx.registerReceiver(receiver, intentFilter)
		updateJob = scope.launch(updateDispatcher) {
			update(intent, true)
		}

		onDispose {
			updateJob.cancel()
			ctx.unregisterReceiver(receiver)
		}
	}

	// The pool is mutated in place every frame. neverEqualPolicy lets writing the same
	// array instance still invalidate the draw pass, so the animation loop never has to
	// allocate a fresh array per frame just to trigger a redraw.
	var bubbles by remember { mutableStateOf(EMPTY_BUBBLES, neverEqualPolicy()) }

	// Only restart the loop when charging starts/stops. The intensity and the amount
	// setting are read on every frame instead, so a fluctuating battery current no longer
	// restarts (and re-randomizes) the whole animation on every battery broadcast.
	val isCharging = intensity > 0
	LaunchedEffect(isCharging) {
		if (!isCharging) {
			bubbles = EMPTY_BUBBLES
			return@LaunchedEffect
		}

		var lastFrameMs = -1L
		while (isActive) {
			// Frame time of the current vsync. Reading it via the return value keeps the
			// frame callback capture-free (and therefore allocation-free).
			val nowMs = withInfiniteAnimationFrameMillis { it }

			val frameMs =
				if (lastFrameMs < 0L) {
					REFERENCE_FRAME_MS
				} else {
					(nowMs - lastFrameMs).coerceIn(0L, MAX_FRAME_MS.toLong()).toFloat()
				}
			lastFrameMs = nowMs

			// Advance by elapsed time instead of one fixed step per frame, so the animation
			// keeps a constant speed on 90/120 Hz panels and while frames are being dropped
			val frameScale = frameMs / REFERENCE_FRAME_MS

			val targetCount =
				(intensity.toLong() * amountDerived).coerceIn(0L, MAX_BUBBLES.toLong()).toInt()

			if (targetCount <= 0) {
				if (bubbles.isNotEmpty()) bubbles = EMPTY_BUBBLES
				continue
			}

			var pool = bubbles
			if (pool.size != targetCount) {
				// Resize only when the count actually changes, and keep already-living
				// bubbles alive so intensity fluctuations don't reset the animation
				val previous = pool
				pool = Array(targetCount) { i -> if (i < previous.size) previous[i] else FloatArray(6) }
				bubbles = pool
			}

			for (i in pool.indices) {
				val bubble = pool[i]
				if (bubble.lifetime <= 0f) {
					bubble.posX = (Math.random() * 48 - 24).toFloat() * density
					bubble.posY = 0f
					bubble.deltaX = (Math.random() - 0.5).toFloat() * 1f * density
					bubble.deltaY = Math.random().toFloat() * 3f * density
					bubble.radius = (Math.random() * 2 + 2).toFloat() * density
					bubble.lifetime = (Math.random() * 80 + 40).toInt().toFloat()
				} else {
					bubble.posX += bubble.deltaX * frameScale
					bubble.posY += bubble.deltaY * frameScale
					bubble.lifetime -= frameScale
				}
			}

			// The same instance is written back on purpose: neverEqualPolicy turns this
			// into a plain draw-phase invalidation without allocating anything
			bubbles = pool
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

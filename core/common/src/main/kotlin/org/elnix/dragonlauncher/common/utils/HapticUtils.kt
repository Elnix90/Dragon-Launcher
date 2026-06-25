package org.elnix.dragonlauncher.common.utils

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.delay
import org.elnix.dragonlauncher.base.model.serializables.CustomHapticFeedback

public object HapticUtils {
    /**
     * Vibrates the device for the given duration using the appropriate API for the current SDK level.
     *
     * @param ctx The context used to retrieve the [Vibrator] or [VibratorManager] system service.
     * @param milliseconds Duration of the vibration in milliseconds.
     */
    public fun vibrate(ctx: Context, milliseconds: Long) {
        val vibrator = if (Build.VERSION.SDK_INT >= 31) {
            val manager = ctx.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            manager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            ctx.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        vibrator.vibrate(VibrationEffect.createOneShot(milliseconds, VibrationEffect.DEFAULT_AMPLITUDE))
    }


    /**
     * Plays a custom haptic feedback pattern by sequentially resolving each step in [customHaptic].
     *
     * Each step is either a vibration or a silent delay, determined by its boolean key:
     * - `true` → vibrate for the step's duration
     * - `false` → wait silently for the step's duration
     *
     * Must be called from a coroutine as it suspends between steps.
     *
     * @param ctx The context used to trigger vibrations.
     * @param customHaptic The haptic pattern to play, or `null` to do nothing.
     */
    public suspend fun performCustomHaptic(ctx: Context, customHaptic: CustomHapticFeedback?) {
        customHaptic?.haptics?.forEach { (vibrationOrSilent, duration) ->
            if (vibrationOrSilent) {
                vibrate(ctx, duration.toLong())
            }
            delay(duration.toLong())
        }
    }
}
package org.elnix.dragonlauncher.base.util

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

public object HapticUtils {
    /**
     * Vibrates the device for the given duration using the appropriate API for the current SDK level.
     *
     * @param this@vibrate The context used to retrieve the [Vibrator] or [VibratorManager] system service.
     * @param milliseconds Duration of the vibration in milliseconds.
     */
    public fun Context.vibrate(milliseconds: Long) {
        val vibrator = if (Build.VERSION.SDK_INT >= 31) {
            val manager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            manager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        vibrator.vibrate(VibrationEffect.createOneShot(milliseconds, VibrationEffect.DEFAULT_AMPLITUDE))
    }
}
@file:Suppress("ConstPropertyName")

package org.elnix.dragonlauncher.base.model.serializables

import android.content.Context
import androidx.compose.runtime.Immutable
import kotlinx.coroutines.delay
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.ktx.vibrate
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds

public class CustomHapticBuilder {
    private val sequence = mutableListOf<HapticEntry>()

    public fun haptic(durationMs: Int) {
        sequence.add(
            HapticEntry(
                isVibration = true,
                durationMs = durationMs
            )
        )
    }

    public fun pause(durationMs: Int) {
        sequence.add(
            HapticEntry(
                isVibration = false,
                durationMs = durationMs
            )
        )
    }

    internal fun build(): List<HapticEntry> = sequence.toList()
}

@JvmInline
@Immutable
@Serializable
@SerialName("CustomHapticFeedback")
public value class CustomHapticFeedback(
    /**
     * The custom haptics, they are resolved one by one when performing the haptic feedback,
     * when the key is `true`, a vibration is performed, when it's `false` a delay is applied.
     * This way, the user can customize infinitely the haptic it wants for every situation
     * The value is the duration of the vibration/delay
     */
    @SerialName("haptics")
    public val haptics: List<HapticEntry>
) {
    /**
     * Plays a custom haptic feedback pattern by sequentially resolving each step in [haptics].
     *
     * Each step is either a vibration or a silent delay, determined by its boolean key:
     * - `true` -> vibrate for the step's duration
     * - `false` -> wait silently for the step's duration
     *
     * Must be called from a coroutine as it suspends between steps.
     */
    public suspend fun perform(ctx: Context) {
        haptics.forEach { (vibrationOrSilent, duration) ->
            if (vibrationOrSilent) {
                // I need to use the custom vibrate because its handled anyway by the DisableHapticFeedbackGlobally
                @Suppress("DEPRECATION")
                ctx.vibrate(duration.toLong())
            }
            delay(duration.milliseconds)
        }
    }

    public companion object {
        public fun build(builder: CustomHapticBuilder.() -> Unit): CustomHapticFeedback {
            val sequence = CustomHapticBuilder().apply(builder).build()
            return CustomHapticFeedback(sequence)
        }

        /** Like a subtle confirmation */
        public val singleTap: CustomHapticFeedback =
            build {
                haptic(20)
            }

        /** Vibrate, pause, vibrate */
        public val doubleTap: CustomHapticFeedback =
            build {
                haptic(20)
                pause(80)
                haptic(20)
            }

        /** Three quick pulses */
        public val tripleTap: CustomHapticFeedback =
            build {
                haptic(20)
                pause(60)
                haptic(20)
                pause(60)
                haptic(20)
            }

        /** One long strong buzz */
        public val heavy: CustomHapticFeedback =
            build {
                haptic(80)
            }

        /** Short, pause, long */
        public val heartbeat: CustomHapticFeedback =
            build {
                haptic(20)
                pause(40)
                haptic(60)
            }

        /** Three uneven jolts */
        public val error: CustomHapticFeedback =
            build {
                haptic(50)
                pause(30)
                haptic(50)
                pause(30)
                haptic(100)
            }

        /** Barely-there nudge */
        public val tick: CustomHapticFeedback =
            build {
                haptic(8)
            }

        /** Short then long, like a reward */
        public val success: CustomHapticFeedback =
            build {
                haptic(15)
                pause(50)
                haptic(40)
            }

        /** Rapid-fire tiny pulses */
        public val buzzRoll: CustomHapticFeedback =
            build {
                haptic(10)
                pause(20)
                haptic(10)
                pause(20)
                haptic(10)
                pause(20)
                haptic(10)
            }

        /** Slow build, short finish */
        public val longPress: CustomHapticFeedback =
            build {
                haptic(60)
                pause(100)
                haptic(20)
            }

        /** Lists all default presets and map them to their i18n name stringRessource */
        public val allPresets: List<Pair<Int, CustomHapticFeedback>> =
            listOf(
                R.string.haptic_preset_single to singleTap,
                R.string.haptic_preset_double to doubleTap,
                R.string.haptic_preset_triple to tripleTap,
                R.string.haptic_preset_heavy to heavy,
                R.string.haptic_preset_heartbeat to heartbeat,
                R.string.haptic_preset_error to error,
                R.string.haptic_preset_tick to tick,
                R.string.haptic_preset_success to success,
                R.string.haptic_preset_buzz_roll to buzzRoll,
                R.string.haptic_preset_long_press to longPress
            )
    }
}

/**
 * Haptic entry, used in compose in the haptic feedback editor to manage the different haptic
 *
 * @property isVibration
 * @property durationMs
 * @constructor Create empty Haptic entry
 */
@Serializable
public data class HapticEntry(
    val isVibration: Boolean,
    val durationMs: Int
) {
    val id: Long = System.nanoTime() + Random.nextLong()

    public val resetEnabled: Boolean
        get() = if (isVibration) durationMs != defaultVibrationDuration else durationMs != defaultHapticDuration

    public companion object {
        public const val defaultHapticDuration: Int = 100
        public const val defaultVibrationDuration: Int = 50
    }
}

package org.elnix.dragonlauncher.base.model.serializables

import androidx.compose.runtime.Immutable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.elnix.dragonlauncher.i18n.R


@Immutable
@Serializable
@SerialName("CustomHapticFeedback")
public data class CustomHapticFeedback(

    /**
     * The custom haptics, they are resolved one by one when performing the haptic feedback,
     * when the key is `true`, a vibration is performed, when it's `false` a delay is applied.
     * This way, the user can customize infinitely the haptic it wants for every situation
     * The value is the duration of the vibration/delay
     */

    @SerialName("haptics")
    val haptics: List<Pair<Boolean, Int>>
) {
    public companion object {

        /** Like a subtle confirmation */
        public val singleTap: CustomHapticFeedback = CustomHapticFeedback(
            listOf(
                true to 20
            )
        )

        /** Vibrate, pause, vibrate */
        public val doubleTap: CustomHapticFeedback = CustomHapticFeedback(
            listOf(
                true to 20,
                false to 80,
                true to 20
            )
        )

        /** Three quick pulses */
        public val tripleTap: CustomHapticFeedback = CustomHapticFeedback(
            listOf(
                true to 20,
                false to 60,
                true to 20,
                false to 60,
                true to 20
            )
        )

        /** One long strong buzz */
        public val heavy: CustomHapticFeedback = CustomHapticFeedback(
            listOf(
                true to 80
            )
        )

        /** Short, pause, long */
        public val heartbeat: CustomHapticFeedback = CustomHapticFeedback(
            listOf(
                true to 20,
                false to 40,
                true to 60
            )
        )

        /** Three uneven jolts */
        public val error: CustomHapticFeedback = CustomHapticFeedback(
            listOf(
                true to 50,
                false to 30,
                true to 50,
                false to 30,
                true to 100
            )
        )

        /** Barely-there nudge */
        public val tick: CustomHapticFeedback = CustomHapticFeedback(
            listOf(
                true to 8
            )
        )

        /** Short then long, like a reward */
        public val success: CustomHapticFeedback = CustomHapticFeedback(
            listOf(
                true to 15,
                false to 50,
                true to 40
            )
        )

         /** Rapid-fire tiny pulses */
         public val buzzRoll: CustomHapticFeedback = CustomHapticFeedback(
            listOf(
                true to 10,
                false to 20,
                true to 10,
                false to 20,
                true to 10,
                false to 20,
                true to 10
            )
        )

        /** Slow build, short finish */
        public val longPress: CustomHapticFeedback = CustomHapticFeedback(
            listOf(
                true to 60,
                false to 100,
                true to 20
            )
        )

        /** Lists all default presets and map them to their i18n name stringRessource */
        public val allPresets: List<Pair<Int, CustomHapticFeedback>> = listOf(
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

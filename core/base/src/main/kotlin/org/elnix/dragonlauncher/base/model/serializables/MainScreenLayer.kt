@file:Suppress("ConstPropertyName")

package org.elnix.dragonlauncher.base.model.serializables

import androidx.annotation.FloatRange
import androidx.annotation.IntRange
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.res.stringResource
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.elnix.dragonlauncher.base.model.DragonJson
import org.elnix.dragonlauncher.i18n.R

@Immutable
@Serializable
@SerialName("MainScreenLayer")
public sealed class MainScreenLayer {
    public abstract val enabled: Boolean

    @Immutable
    @Serializable
    @SerialName("ChargingAnimation")
    public data class ChargingAnimation(
        override val enabled: Boolean = true
    ) : MainScreenLayer()

    @Immutable
    @Serializable
    @SerialName("Widgets")
    public data class Widgets(
        override val enabled: Boolean = true
    ) : MainScreenLayer()

    @Immutable
    @Serializable
    @SerialName("StatusBar")
    public data class StatusBar(
        override val enabled: Boolean = false
    ) : MainScreenLayer()

    @Immutable
    @Serializable
    @SerialName("DragOverlay")
    public data class DragOverlay(
        override val enabled: Boolean = true,
        val lineBeforeNests: Boolean = defaultLineBeforeNests
    ) : MainScreenLayer() {
        public companion object {
            public const val defaultLineBeforeNests: Boolean = false
        }
    }

    @Immutable
    @Serializable
    @SerialName("HoldToActivate")
    public data class HoldToActivate(
        override val enabled: Boolean = true
    ) : MainScreenLayer()

    @Immutable
    @Serializable
    @SerialName("CustomDim")
    public data class CustomDim(
        override val enabled: Boolean = true,
        /** How powerful the fim is */
        @FloatRange(from = 0.0, to = 1.0)
        val dimAmount: Float = defaultDimAmount,
        /** After how long to hold the overlay shows up*/
        @IntRange(from = 0, to = 5000)
        val showAfterMs: Int = defaultShowAfterMs
    ) : MainScreenLayer() {
        public companion object {
            public const val defaultDimAmount: Float = .5f
            public const val defaultShowAfterMs: Int = 1000
        }
    }

    public companion object {
        public val defaultMainScreenLayers: List<MainScreenLayer> =
            listOf(
                ChargingAnimation(),
                StatusBar(),
                Widgets(),
                CustomDim(),
                DragOverlay(),
                HoldToActivate()
            )

        public val MainScreenLayer.label: String
            @Composable
            get() =
                stringResource(
                    when (this) {
                        is ChargingAnimation -> R.string.charging_animation
                        is Widgets -> R.string.widgets
                        is StatusBar -> R.string.status_bar
                        is DragOverlay -> R.string.drag_overlay
                        is HoldToActivate -> R.string.hold_to_activate
                        is CustomDim -> R.string.custom_dim
                    }
                )

        public val MainScreenLayer.enabled: Boolean
            get() =
                when (this) {
                    is ChargingAnimation -> enabled
                    is DragOverlay -> enabled
                    is HoldToActivate -> enabled
                    is StatusBar -> enabled
                    is Widgets -> enabled
                    is CustomDim -> enabled
                }

        public fun MainScreenLayer.copyWithEnabled(enabled: Boolean): MainScreenLayer =
            when (this) {
                is ChargingAnimation -> copy(enabled = enabled)
                is DragOverlay -> copy(enabled = enabled)
                is HoldToActivate -> copy(enabled = enabled)
                is StatusBar -> copy(enabled = enabled)
                is Widgets -> copy(enabled = enabled)
                is CustomDim -> copy(enabled = enabled)
            }

        public object MainScreenLayerJson : DragonJson<List<MainScreenLayer>>()
    }
}

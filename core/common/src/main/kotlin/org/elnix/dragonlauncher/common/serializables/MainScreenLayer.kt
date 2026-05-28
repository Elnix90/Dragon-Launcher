package org.elnix.dragonlauncher.common.serializables

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.elnix.dragonlauncher.i18n.R


@Serializable
@SerialName("MainScreenLayer")
sealed class MainScreenLayer {
    @Serializable
    @SerialName("ChargingAnimation")
    data class ChargingAnimation(
        val enabled: Boolean = true
    ) : MainScreenLayer()

    @Serializable
    @SerialName("Widgets")
    data class Widgets(
        val enabled: Boolean = true
    ) : MainScreenLayer()

    @Serializable
    @SerialName("StatusBar")
    data class StatusBar(
        val enabled: Boolean = true
    ) : MainScreenLayer()

    @Serializable
    @SerialName("DragOverlay")
    data class DragOverlay(
        val enabled: Boolean = true
    ) : MainScreenLayer()

    @Serializable
    @SerialName("HoldToActivate")
    data class HoldToActivate(
        val enabled: Boolean = true
    ) : MainScreenLayer()

    @Serializable
    @SerialName("CustomDim")
    data class CustomDim(
        val enabled: Boolean = true,
        /** How powerful the fim is */
        val dimAmount: Float = 0.5f,
        /** After how long to hold the overlay shows up*/
        val showAfter: Int = 1000
    ) : MainScreenLayer()

    companion object {
        val defaultMainScreenLayers: List<MainScreenLayer> = listOf(
            ChargingAnimation(),
            StatusBar(),
            Widgets(),
            CustomDim(false), // Disabled by default
            DragOverlay(),
            HoldToActivate()
        )

        val MainScreenLayer.label: String
            @Composable
            get() = stringResource(
                when (this) {
                    is ChargingAnimation -> R.string.charging_animation
                    is Widgets -> R.string.widgets
                    is StatusBar -> R.string.status_bar
                    is DragOverlay -> R.string.drag_overlay
                    is HoldToActivate -> R.string.hold_to_activate
                    is CustomDim -> R.string.custom_dim
                }
            )

        val MainScreenLayer.enabled: Boolean
            get() = when (this) {
                is ChargingAnimation -> enabled
                is DragOverlay -> enabled
                is HoldToActivate -> enabled
                is StatusBar -> enabled
                is Widgets -> enabled
                is CustomDim -> enabled
            }


        fun MainScreenLayer.copyWithEnabled(enabled: Boolean): MainScreenLayer = when (this) {
            is ChargingAnimation -> copy(enabled = enabled)
            is DragOverlay -> copy(enabled = enabled)
            is HoldToActivate -> copy(enabled = enabled)
            is StatusBar -> copy(enabled = enabled)
            is Widgets -> copy(enabled = enabled)
            is CustomDim -> copy(enabled = enabled)
        }


    }
}

object MainScreenLayerJson : DragonJson<List<MainScreenLayer>>()
package org.elnix.dragonlauncher.common.serializables

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.elnix.dragonlauncher.common.R
import org.elnix.dragonlauncher.common.messyfolder.Constants.Logging.MAIN_SCREEN_LAYERS_TAG
import org.elnix.dragonlauncher.logging.logE


@Serializable
sealed class MainScreenLayer {
    @Serializable
    data class ChargingAnimation(
        val enabled: Boolean = true
    ) : MainScreenLayer()

    @Serializable
    data class Widgets(
        val enabled: Boolean = true
    ) : MainScreenLayer()

    @Serializable
    data class StatusBar(
        val enabled: Boolean = true
    ) : MainScreenLayer()

    @Serializable
    data class DragOverlay(
        val enabled: Boolean = true
    ) : MainScreenLayer()

    @Serializable
    data class HoldToActivate(
        val enabled: Boolean = true
    ) : MainScreenLayer()

    @Serializable
    data class CustomDim(
        val enabled: Boolean = true,
        /** How powerful the fim is */
        val dimAmount: Float = 0.5f,
        /** After how long to hold the overlay shows up*/
        val showAfter: Int = 1000
    ) : MainScreenLayer()
}


val MainScreenLayer.label: String
    @Composable
    get() = stringResource(
        when (this) {
            is MainScreenLayer.ChargingAnimation -> R.string.charging_animation
            is MainScreenLayer.Widgets -> R.string.widgets
            is MainScreenLayer.StatusBar -> R.string.status_bar
            is MainScreenLayer.DragOverlay -> R.string.drag_overlay
            is MainScreenLayer.HoldToActivate -> R.string.hold_to_activate
            is MainScreenLayer.CustomDim -> R.string.custom_dim
        }
    )

val MainScreenLayer.enabled: Boolean
    get() = when (this) {
        is MainScreenLayer.ChargingAnimation -> enabled
        is MainScreenLayer.DragOverlay -> enabled
        is MainScreenLayer.HoldToActivate -> enabled
        is MainScreenLayer.StatusBar -> enabled
        is MainScreenLayer.Widgets -> enabled
        is MainScreenLayer.CustomDim -> enabled
    }


fun MainScreenLayer.copyWithEnabled(enabled: Boolean): MainScreenLayer = when (this) {
    is MainScreenLayer.ChargingAnimation -> copy(enabled = enabled)
    is MainScreenLayer.DragOverlay -> copy(enabled = enabled)
    is MainScreenLayer.HoldToActivate -> copy(enabled = enabled)
    is MainScreenLayer.StatusBar -> copy(enabled = enabled)
    is MainScreenLayer.Widgets -> copy(enabled = enabled)
    is MainScreenLayer.CustomDim -> copy(enabled = enabled)
}


val defaultMainScreenLayers: List<MainScreenLayer> = listOf(
    MainScreenLayer.ChargingAnimation(),
    MainScreenLayer.StatusBar(),
    MainScreenLayer.Widgets(),
    MainScreenLayer.CustomDim(false), // Disabled by default
    MainScreenLayer.DragOverlay(),
    MainScreenLayer.HoldToActivate()
)

object MainScreenLayerJson {
    private val jsonConfig = Json {
        explicitNulls = false
        ignoreUnknownKeys = true
    }

    fun encode(list: List<MainScreenLayer>): String =
        jsonConfig.encodeToString(list)


    fun decode(jsonString: String): List<MainScreenLayer> {
        return try {
            jsonConfig.decodeFromString<List<MainScreenLayer>>(jsonString)
        } catch (e: Exception) {
            logE(MAIN_SCREEN_LAYERS_TAG, e) { "Failed to decode main screen layers" }
            defaultMainScreenLayers
        }
    }
}
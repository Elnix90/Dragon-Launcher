package org.elnix.dragonlauncher.ui.helpers.customobjects

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import io.github.elnix90.runtime.asState
import org.elnix.dragonlauncher.base.model.serializables.CustomObject
import org.elnix.dragonlauncher.settings.stores.map.AngleLineSettingsStore

@Composable
public fun CustomObject.resolveRotation(
    isStartOrAngle: Boolean,
    sweep: Int,
    key: Any? = null
): Int {
    val startAndAngleShareSameRandomAngle by AngleLineSettingsStore.startAndAngleShareSameRandomAngle.asState()

    val baseRotation = remember(this.rotation, key, startAndAngleShareSameRandomAngle) {
        this.rotation.takeIf { it != -1 } ?: (0..360).random()
    }

    return if (this.alignsWithDragAngle) {
        sweep + baseRotation
    } else if (startAndAngleShareSameRandomAngle && isStartOrAngle) {
        remember(key) {
            (0..360).random()
        }
    } else {
        baseRotation
    }
}
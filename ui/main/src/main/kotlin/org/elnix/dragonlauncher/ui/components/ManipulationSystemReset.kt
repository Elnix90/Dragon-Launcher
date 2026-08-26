package org.elnix.dragonlauncher.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.geometry.Offset
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.base.navigation.ManipulationSystem
import org.elnix.dragonlauncher.base.model.enumsui.toggle.MoveAroundTools
import org.elnix.dragonlauncher.ui.base.animation.bouncySpec
import org.elnix.dragonlauncher.ui.dragon.generic.MultiSelectConnectedButtonRow

@Composable
fun ManipulationSystemReset(manipulationSystem: ManipulationSystem) {
    val scope = rememberCoroutineScope()
    
    val canResetOffset = manipulationSystem.offset.value != Offset.Zero
    val canResetZoom = manipulationSystem.zoom.value != 1f
    val canResetRotation = manipulationSystem.angle.value != 0f
    
    MultiSelectConnectedButtonRow(
        entries = MoveAroundTools.entries,
        enabled = {
            when (it) {
                MoveAroundTools.Center -> canResetOffset
                MoveAroundTools.ResetZoom -> canResetZoom
                MoveAroundTools.ResetRotation -> canResetRotation
            }
        },
        checked = {
            when (it) {
                MoveAroundTools.Center -> canResetOffset
                MoveAroundTools.ResetZoom -> canResetZoom
                MoveAroundTools.ResetRotation -> canResetRotation
            }
        }
    ) { entry ->
        scope.launch {
            when (entry) {
                MoveAroundTools.Center -> scope.launch {
                    manipulationSystem.offset.animateTo(Offset.Zero, bouncySpec())
                }

                MoveAroundTools.ResetZoom -> scope.launch {
                    manipulationSystem.zoom.animateTo(1f, bouncySpec())
                }

                MoveAroundTools.ResetRotation -> scope.launch {
                    manipulationSystem.angle.animateTo(0f, bouncySpec())
                }
            }
        }
    }
}
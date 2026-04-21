package org.elnix.dragonlauncher.enumsui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.TextRotationAngleup
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.ui.graphics.vector.ImageVector
import org.elnix.dragonlauncher.common.R

enum class NestEditMode(
    override val resId: Int,
    override val iconEnabled: ImageVector,
    override val iconDisabled: ImageVector? = null
): ToggleButtonOption {
    Drag(R.string.dragging_distance_selection,Icons.Default.DragIndicator),
    Haptic(R.string.haptic_feedback,Icons.Default.Vibration),
    MinAngle(R.string.min_angle_to_activate,Icons.Default.TextRotationAngleup),
    Radius(R.string.nest_radius,Icons.Default.Radar),
    Other(R.string.other,Icons.Default.MoreVert)
}

package org.elnix.dragonlauncher.utils.actions

import androidx.compose.ui.graphics.Color
import org.elnix.dragonlauncher.data.SwipeActionSerializable

fun actionColor(action: SwipeActionSerializable?): Color =
    when (action) {
        is SwipeActionSerializable.LaunchApp -> Color(0xFF55AAFF)
        is SwipeActionSerializable.OpenUrl -> Color(0xFF66DD77)
        SwipeActionSerializable.NotificationShade -> Color(0xFFFFBB44)
        SwipeActionSerializable.ControlPanel -> Color(0xFFFF6688)
        SwipeActionSerializable.OpenAppDrawer -> Color(0xFFDD55FF)
        else -> Color.Red
    }

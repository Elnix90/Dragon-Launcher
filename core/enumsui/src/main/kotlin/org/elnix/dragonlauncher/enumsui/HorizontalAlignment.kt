package org.elnix.dragonlauncher.enumsui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatAlignLeft
import androidx.compose.material.icons.automirrored.filled.FormatAlignRight
import androidx.compose.material.icons.filled.FormatAlignCenter
import androidx.compose.ui.graphics.vector.ImageVector
import org.elnix.dragonlauncher.common.R
enum class HorizontalAlignment(
    override val resId: Int?,
    override val iconEnabled: ImageVector,
    override val iconDisabled: ImageVector? = null
) : ToggleButtonOption {
    Start(R.string.align_start,Icons.AutoMirrored.Filled.FormatAlignLeft),
    Center(R.string.align_center, Icons.Default.FormatAlignCenter),
    End(R.string.align_end, Icons.AutoMirrored.Filled.FormatAlignRight)
}

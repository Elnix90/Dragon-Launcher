package org.elnix.dragonlauncher.data.helpers

import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.runtime.Composable
import org.elnix.dragonlauncher.R
import org.elnix.dragonlauncher.data.helpers.DrawerActions.CLOSE
import org.elnix.dragonlauncher.data.helpers.DrawerActions.DISABLED
import org.elnix.dragonlauncher.data.helpers.DrawerActions.NONE
import org.elnix.dragonlauncher.data.helpers.DrawerActions.TOGGLE_KB

enum class DrawerActions { CLOSE, CLEAR, TOGGLE_KB, SEARCH_WEB, OPEN_FIRST_APP, NONE, DISABLED }

@Composable
fun drawerActionIcon(action: DrawerActions) = when (action) {
    CLOSE -> Icons.Default.Close
    TOGGLE_KB -> Icons.Default.Keyboard
    NONE -> Icons.Default.RadioButtonUnchecked
    DISABLED -> Icons.Default.RadioButtonUnchecked
    DrawerActions.CLEAR -> Icons.Default.ClearAll
    DrawerActions.SEARCH_WEB -> Icons.Default.Language
    DrawerActions.OPEN_FIRST_APP -> Icons.AutoMirrored.Filled.OpenInNew
}

fun drawerActionsLabel(ctx: Context,action: DrawerActions) = when (action) {
    CLOSE -> ctx.getString(R.string.close_drawer)
    TOGGLE_KB -> ctx.getString(R.string.toggle_kb)
    NONE -> ctx.getString(R.string.none)
    DISABLED -> ""
    DrawerActions.CLEAR -> ctx.getString(R.string.drawer_action_clear)
    DrawerActions.SEARCH_WEB -> ctx.getString(R.string.drawer_action_search_web)
    DrawerActions.OPEN_FIRST_APP -> ctx.getString(R.string.drawer_action_open_first_app)
}

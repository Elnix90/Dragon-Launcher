package org.elnix.dragonlauncher.ui.compositionslocals

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import org.elnix.dragonlauncher.base.navigaton.NavigationRoute


public interface Navigator {
    public fun navigate(screen: NavigationRoute)
    public fun onBack()
    public fun popBackMainScreen()
}

public val LocalNavigator: ProvidableCompositionLocal<Navigator> = compositionLocalOf { error("No LocalNavigator provided") }
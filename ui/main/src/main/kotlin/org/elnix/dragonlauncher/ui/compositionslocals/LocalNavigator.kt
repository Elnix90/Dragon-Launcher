package org.elnix.dragonlauncher.ui.compositionslocals

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import org.elnix.dragonlauncher.base.navigaton.NavigationRoute


interface Navigator {
    fun navigate(screen: NavigationRoute)
    fun onBack()
    fun popBackMainScreen()
}

val LocalNavigator: ProvidableCompositionLocal<Navigator> = compositionLocalOf { error("No LocalNavigator provided") }
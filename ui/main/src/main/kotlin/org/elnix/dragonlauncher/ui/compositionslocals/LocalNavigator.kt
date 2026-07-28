package org.elnix.dragonlauncher.ui.compositionslocals

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import org.elnix.dragonlauncher.base.navigaton.NavigationRoute


interface Navigator {

    /**
     * Navigates directly to the given screen, bypasses any lockscreen in place. Used by... well the lock screen to navigate when unlocked
     *
     * @param screen Which screen to navigate to
     */
    fun go(screen: NavigationRoute)

    /**
     * THe correct way to navigate between screens, handles lock screen and authentication when navigating
     *
     * @param screen Which screen is requested to navigate to
     */
    fun navigate(screen: NavigationRoute)


    fun onBack()
    fun popBackMainScreen()
}

val LocalNavigator: ProvidableCompositionLocal<Navigator> = compositionLocalOf { error("No LocalNavigator provided") }
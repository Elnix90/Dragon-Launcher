package org.elnix.dragonlauncher.ui.compositionslocals

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import org.elnix.dragonlauncher.base.navigation.NavigationRoute


interface Navigator {

    /**
     * Navigates directly to the given screen, bypasses any lockscreen in place. Used by... well the lock screen to navigate when unlocked
     *
     * @param route Which screen to navigate to
     */
    fun go(route: NavigationRoute)

    /**
     * THe correct way to navigate between screens, handles lock screen and authentication when navigating
     *
     * @param route Which screen is requested to navigate to
     */
    fun navigate(route: NavigationRoute)


    /**
     * On backNavigates back the backstack
     *
     */
    fun onBack()

    /**
     * Pretty self-explanatory I guess, it removes all screens from the backstack and adds the MainScreen.
     *
     * Basically clears any route to reset it.
     */
    fun popBackMainScreen()
}

val LocalNavigator: ProvidableCompositionLocal<Navigator> = compositionLocalOf { error("No LocalNavigator provided") }
package org.elnix.dragonlauncher.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import org.elnix.dragonlauncher.data.stores.DrawerSettingsStore
import org.elnix.dragonlauncher.ui.drawer.AppDrawerScreen
import org.elnix.dragonlauncher.ui.settings.appearance.AppearanceTab
import org.elnix.dragonlauncher.ui.settings.appearance.ColorSelectorTab
import org.elnix.dragonlauncher.ui.settings.appearance.DrawerTab
import org.elnix.dragonlauncher.ui.settings.backup.BackupTab
import org.elnix.dragonlauncher.ui.settings.backup.BackupViewModel
import org.elnix.dragonlauncher.ui.settings.debug.DebugTab
import org.elnix.dragonlauncher.ui.settings.language.LanguageTab
import org.elnix.dragonlauncher.ui.welcome.WelcomeScreen
import org.elnix.dragonlauncher.utils.AppDrawerViewModel

// -------------------- SETTINGS --------------------

object SETTINGS {
    const val ROOT = "settings"
    const val ADVANCED_ROOT = "settings/advanced"
    const val APPEARANCE = "settings/advanced/appearance"
    const val COLORS = "settings/advanced/appearance/colors"
    const val DRAWER = "settings/drawer"
    const val BACKUP = "settings/advanced/backup"
    const val DEBUG = "/advanced/debug"
    const val LANGUAGE = "settings/advanced/language"
}

object ROUTES {
    const val MAIN = "main"
    const val DRAWER = "drawer"
    const val WELCOME = "welcome"
}

@Composable
fun MainAppUi(
    backupViewModel: BackupViewModel,
    appsViewModel: AppDrawerViewModel,
    navController: NavHostController
) {
    val ctx = LocalContext.current

    val showAppIconsInDrawer by DrawerSettingsStore.getShowAppIconsInDrawer(ctx)
        .collectAsState(initial = true)

    val showAppLabelsInDrawer by DrawerSettingsStore.getShowAppLabelsInDrawer(ctx)
        .collectAsState(initial = true)

    val autoShowKeyboardOnDrawer by DrawerSettingsStore.getAutoShowKeyboardOnDrawer(ctx)
        .collectAsState(initial = false)

    val gridSize by DrawerSettingsStore.getGridSize(ctx)
        .collectAsState(initial = 1)

//    val searchBarBottom by DrawerSettingsStore.getSearchBarBottom(ctx)
//        .collectAsState(initial = true)
    val searchBarBottom = false

    val initialPage by DrawerSettingsStore.getInitialPage(ctx)
        .collectAsState(initial = 0)

    fun goMainScreen() {
        navController.navigate(ROUTES.MAIN) {
            popUpTo(0) { inclusive = true }
        }
    }

    fun goSettingsRoot() =  navController.navigate(SETTINGS.ROOT)
    fun goAdvSettingsRoot() =  navController.navigate(SETTINGS.ADVANCED_ROOT)
    fun goDrawer() = navController.navigate(ROUTES.DRAWER)
    fun goWelcome() = navController.navigate(ROUTES.WELCOME)

    NavHost(
        navController = navController,
        startDestination = ROUTES.MAIN
    ) {
        // Main App (LauncherScreen + Drawer)
        composable(ROUTES.MAIN) {
            MainScreen(
                appsViewModel = appsViewModel,
                onAppDrawer = { goDrawer() },
                onGoWelcome = { goWelcome() },
                onLongPress3Sec = { goSettingsRoot() }
            )
        }

        composable(ROUTES.DRAWER) { AppDrawerScreen(
            appsViewModel = appsViewModel,
            initialPage = initialPage,
            showIcons = showAppIconsInDrawer,
            showLabels = showAppLabelsInDrawer,
            autoShowKeyboard = autoShowKeyboardOnDrawer,
            gridSize = gridSize,
            searchBarBottom = searchBarBottom
        ) { goMainScreen() } }


        // Settings + Welcome

        composable(ROUTES.WELCOME) {
            WelcomeScreen(
                backupVm =  backupViewModel,
                onEnterSettings = { goSettingsRoot() },
                onEnterApp = { goMainScreen() }
            )
        }

        composable(SETTINGS.ROOT) {
            SettingsScreen(
                appsViewModel = appsViewModel,
                onAdvSettings = { goAdvSettingsRoot() },
                onBack = { goMainScreen() }
            )
        }
        composable(SETTINGS.ADVANCED_ROOT) { AdvancedSettingsScreen(navController, onReset = { goMainScreen() } ) { goSettingsRoot() } }


        composable(SETTINGS.APPEARANCE) { AppearanceTab(navController) { goAdvSettingsRoot() } }
        composable(SETTINGS.DRAWER) { DrawerTab { goAdvSettingsRoot() } }
        composable(SETTINGS.COLORS) { ColorSelectorTab { goAdvSettingsRoot() } }
        composable(SETTINGS.DEBUG) { DebugTab(navController) { goAdvSettingsRoot() } }
        composable(SETTINGS.LANGUAGE) { LanguageTab { goAdvSettingsRoot() } }
        composable(SETTINGS.BACKUP) { BackupTab(backupViewModel) { goAdvSettingsRoot() } }
    }
}

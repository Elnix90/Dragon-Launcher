package org.elnix.dragonlauncher.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.elnix.dragonlauncher.ui.settings.appearance.AppearanceTab
import org.elnix.dragonlauncher.ui.settings.appearance.ColorSelectorTab
import org.elnix.dragonlauncher.ui.settings.backup.BackupTab
import org.elnix.dragonlauncher.ui.settings.backup.BackupViewModel
import org.elnix.dragonlauncher.ui.settings.debug.DebugTab
import org.elnix.dragonlauncher.ui.settings.language.LanguageTab

// -------------------- SETTINGS --------------------

object SETTINGS {
    const val ROOT = "settings"
    const val APPEARANCE = "settings/appearance"
    const val COLORS = "settings/appearance/colors"
//    const val CUSTOMISATION = "settings/customisation"
    const val BACKUP = "settings/backup"
    const val DEBUG = "settings/debug"
    const val LANGUAGE = "settings/language"
}

@Composable
fun AdvancedSettingsUi(backupVm: BackupViewModel, onBack : () -> Unit ) {

    val navController = rememberNavController()

    fun goRoot() =  navController.navigate(SETTINGS.ROOT)


    NavHost(
        navController = navController,
        startDestination = SETTINGS.ROOT
    ) {
        composable(SETTINGS.ROOT) { AdvancedSettingsScreen(navController) { onBack() } }
        composable(SETTINGS.APPEARANCE) { AppearanceTab(navController) { goRoot() } }
        composable(SETTINGS.COLORS) { ColorSelectorTab { goRoot() } }
        composable(SETTINGS.DEBUG) { DebugTab(navController) { goRoot() } }
        composable(SETTINGS.LANGUAGE) { LanguageTab { goRoot() } }
        composable(SETTINGS.BACKUP) { BackupTab(backupVm) { goRoot() } }
    }
}
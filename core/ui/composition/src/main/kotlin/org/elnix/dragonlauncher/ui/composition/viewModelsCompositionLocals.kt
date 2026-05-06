package org.elnix.dragonlauncher.ui.composition

import androidx.compose.runtime.compositionLocalOf
import org.elnix.dragonlauncher.models.AppLifecycleViewModel
import org.elnix.dragonlauncher.models.AppsViewModel
import org.elnix.dragonlauncher.models.BackupViewModel
import org.elnix.dragonlauncher.models.DragonLogViewModel
import org.elnix.dragonlauncher.models.FloatingAppsViewModel
import org.elnix.dragonlauncher.models.PrivateSpaceViewModel
import org.elnix.dragonlauncher.models.ShizukuViewModel


val LocalAppsViewModel = compositionLocalOf<AppsViewModel> {
    error("No AppsViewModel bar provided")
}
val LocalAppLifecycleViewModel = compositionLocalOf<AppLifecycleViewModel> {
    error("No AppLifecycleViewModel bar provided")
}
val LocalPrivateSpaceViewModel = compositionLocalOf<PrivateSpaceViewModel> {
    error("No PrivateSpaceViewModel bar provided")
}
val LocalBackupViewModel = compositionLocalOf<BackupViewModel> {
    error("No BackupViewModel bar provided")
}
val LocalFloatingAppsViewModel = compositionLocalOf<FloatingAppsViewModel> {
    error("No FloatingAppsViewModel bar provided")
}
val LocalShizukuViewModel = compositionLocalOf<ShizukuViewModel> {
    error("No LocalShizukuViewModel bar provided")
}

val LocalDragonLogViewModel = compositionLocalOf<DragonLogViewModel> {
    error("No LocalDragonLogViewModel bar provided")
}
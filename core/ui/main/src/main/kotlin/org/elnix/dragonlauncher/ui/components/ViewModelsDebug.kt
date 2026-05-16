package org.elnix.dragonlauncher.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import org.elnix.dragonlauncher.models.AppLifecycleViewModel
import org.elnix.dragonlauncher.models.AppsViewModel
import org.elnix.dragonlauncher.models.BackupViewModel
import org.elnix.dragonlauncher.models.DragonLogViewModel
import org.elnix.dragonlauncher.models.WidgetsViewModel
import org.elnix.dragonlauncher.models.LockScreenViewModel
import org.elnix.dragonlauncher.models.PointSettingsViewModel
import org.elnix.dragonlauncher.models.PrivateSpaceViewModel
import org.elnix.dragonlauncher.models.ShizukuViewModel
import org.elnix.dragonlauncher.settings.stores.DebugSettingsStore
import org.elnix.dragonlauncher.ui.activityViewModel
import org.elnix.dragonlauncher.ui.base.asState
import org.elnix.dragonlauncher.ui.dragon.components.DragonColumnGroup

@Composable
fun DebugViewModel(
    appLifecycleViewModel: AppLifecycleViewModel = activityViewModel(),
    appsViewModel: AppsViewModel = activityViewModel(),
    backupViewModel: BackupViewModel = hiltViewModel(),
    dragonLogViewModel: DragonLogViewModel = hiltViewModel(),
    widgetsViewModel: WidgetsViewModel = hiltViewModel(),
    lockScreenViewModel: LockScreenViewModel = activityViewModel(),
    pointSettingsViewModel: PointSettingsViewModel = hiltViewModel(),
    privateSpaceViewModel: PrivateSpaceViewModel = activityViewModel(),
    shizukuViewModel: ShizukuViewModel = activityViewModel(),
) {
    val showDebugViewModel by DebugSettingsStore.showDebugViewModel.asState()
    if (!showDebugViewModel) return

    val vmColors = listOf(
        Color.Transparent, Color.Red, Color.Blue, Color.Green, Color.White, Color.Yellow, Color.Cyan, Color.Magenta
    )
    @Composable
    fun vmTestColor(name: String, vm: ViewModel) {

        var colorIdx by remember { mutableIntStateOf(0) }
        val color = vmColors[colorIdx]

        val hashCode = System.identityHashCode(vm)
        LaunchedEffect(hashCode) {
            colorIdx = (colorIdx + 1) % vmColors.size
        }


        Text("$name: $hashCode", color = color)
    }

    DragonColumnGroup {
        vmTestColor("appLifecycleViewModel", appLifecycleViewModel)
        vmTestColor("AppsViewModel", appsViewModel)
        vmTestColor("backupViewModel", backupViewModel)
        vmTestColor("dragonLogViewModel", dragonLogViewModel)
        vmTestColor("floatingAppsViewModel", widgetsViewModel)
        vmTestColor("lockScreenViewModel", lockScreenViewModel)
        vmTestColor("pointSettingsViewModel", pointSettingsViewModel)
        vmTestColor("privateSpaceViewModel", privateSpaceViewModel)
        vmTestColor("shizukuViewModel", shizukuViewModel)
    }
}
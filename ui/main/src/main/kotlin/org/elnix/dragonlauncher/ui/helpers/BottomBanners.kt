package org.elnix.dragonlauncher.ui.helpers

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.navigation3.runtime.NavKey
import io.github.elnix90.runtime.asState
import io.github.elnix90.runtime.asStateNull
import org.elnix.dragonlauncher.base.navigaton.NavigationRoute
import org.elnix.dragonlauncher.base.utils.rememberIsDefaultLauncher
import org.elnix.dragonlauncher.ktx.hasUriReadWritePermission
import org.elnix.dragonlauncher.settings.stores.map.BackupSettingsStore
import org.elnix.dragonlauncher.settings.stores.map.PrivateSettingsStore
import org.elnix.dragonlauncher.ui.base.components.Spacer

@Composable
fun BottomBanners(currentRoute: NavKey) {
    if (currentRoute == NavigationRoute.Welcome) return


    val ctx = LocalContext.current

    val showSetDefaultLauncherBanner by PrivateSettingsStore.showSetDefaultLauncherBanner.asStateNull()
    val isDefaultLauncher by rememberIsDefaultLauncher()

    val autoBackupEnabled by BackupSettingsStore.autoBackupEnabled.asState()
    val autoBackupUriString by BackupSettingsStore.autoBackupUri.asStateNull()
    val autoBackupUri by remember(autoBackupUriString) {
        derivedStateOf { autoBackupUriString?.toUri() }
    }


    var hasAutoBackupPermission by remember {
        mutableStateOf<Boolean?>(null)
    }

    LaunchedEffect(autoBackupUri) {
        hasAutoBackupPermission = if (autoBackupUri == null) {
            null
        } else {
            ctx.hasUriReadWritePermission(autoBackupUri!!)
        }
    }

    val showReselectAutoBackupFile = autoBackupEnabled && hasAutoBackupPermission == false && autoBackupUri != null
    val showSetAsDefaultBanner = (showSetDefaultLauncherBanner == true) && !isDefaultLauncher


    if (showSetAsDefaultBanner || showReselectAutoBackupFile) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Spacer()
            AnimatedVisibility(showSetAsDefaultBanner) {
                SetDefaultLauncherBanner()
            }
            AnimatedVisibility(showReselectAutoBackupFile) {
                ReselectAutoBackupBanner()
            }
        }
    }
}
package org.elnix.dragonlauncher.ui.whatsnew

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.common.loader.loadChangelogs
import org.elnix.dragonlauncher.common.utils.CopyPasteUtils.copyToClipboard
import org.elnix.dragonlauncher.common.utils.rememberVersionCode
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.ktx.openUrl
import org.elnix.dragonlauncher.logging.SETTINGS_TAG
import org.elnix.dragonlauncher.logging.logWtf
import org.elnix.dragonlauncher.settings.stores.map.PrivateSettingsStore
import org.elnix.dragonlauncher.ui.base.asState
import org.elnix.dragonlauncher.ui.base.components.Spacer
import org.elnix.dragonlauncher.ui.dragon.components.DragonModalBottomSheet

// I hate the behavior of this shitty modal sheet that force showing the system bars, even in fullscreen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WhatsNewBottomSheet() {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    val lastSeenVersionCodeWhatsNew by PrivateSettingsStore.lastSeenVersionCodeWhatsNew.asState()
    val versionCode by rememberVersionCode()

    LaunchedEffect(lastSeenVersionCodeWhatsNew) {
        logWtf(SETTINGS_TAG) { "lastSeenCodeWhatsNew :$lastSeenVersionCodeWhatsNew" }
    }
    if (lastSeenVersionCodeWhatsNew >= versionCode) return

    val updates by produceState(initialValue = emptyList()) {
        value = loadChangelogs(ctx, versionCode)
    }

    DragonModalBottomSheet(
        onDismissRequest = {
            scope.launch {
                PrivateSettingsStore.lastSeenVersionCodeWhatsNew.set(ctx, versionCode)
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = stringResource(R.string.whats_new),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(12.dp)

            updates.forEach { update ->
                UpdateCard(
                    update,
                    onLongCLick = {
                        ctx.copyToClipboard(update.toString())
                    },
                    onCLick = {
                        ctx.openUrl(
                            "https://github.com/Elnix90/Dragon-Launcher/blob/main/fastlane/metadata/android/en-US/changelogs/${versionCode}.txt"
                        )
                    }
                )
            }
        }
    }
}

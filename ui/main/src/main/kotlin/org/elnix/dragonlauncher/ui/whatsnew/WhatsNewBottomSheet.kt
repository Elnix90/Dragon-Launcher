package org.elnix.dragonlauncher.ui.whatsnew

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.elnix90.runtime.asState
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.base.loadChangelogs
import org.elnix.dragonlauncher.base.utils.CopyPasteUtils.copyToClipboard
import org.elnix.dragonlauncher.base.utils.rememberVersionCode
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.settings.stores.map.PrivateSettingsStore
import org.elnix.dragonlauncher.ui.base.components.Spacer
import org.elnix.dragonlauncher.ui.dragon.components.DragonModalBottomSheet

// I hate the behavior of this shitty modal sheet that force showing the system bars, even in fullscreen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WhatsNewBottomSheet() {
    val ctx = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val scope = rememberCoroutineScope()

    val lastSeenVersionCodeWhatsNew by PrivateSettingsStore.lastSeenVersionCodeWhatsNew.asState()
    val versionCode by rememberVersionCode()

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
        LazyColumn {
            item {
                Text(
                    text = stringResource(R.string.whats_new),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Spacer(12.dp)
            }

           items(updates) { update ->
                val updateRegex: Regex = "[\\d-.]+".toRegex()
                val matchResult = updateRegex.find(update.versionName)

                val link = if (matchResult != null) {
                    "https://github.com/Elnix90/Dragon-Launcher/releases/tag/v${matchResult.value}"
                } else {
                    "https://github.com/Elnix90/Dragon-Launcher/releases/latest"
                }

                UpdateCard(
                    update,
                    onLongClick = {
                        ctx.copyToClipboard(link)
                    },
                    onClick = {
                        uriHandler.openUri(link)
                    }
                )
            }
        }
    }
}

package org.elnix.dragonlauncher.ui.whatsnew

import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.common.messyfolder.loadChangelogs
import org.elnix.dragonlauncher.common.messyfolder.openUrl
import org.elnix.dragonlauncher.common.utils.CopyPasteUtils.copyToClipboard
import org.elnix.dragonlauncher.common.utils.rememberVersionCode
import org.elnix.dragonlauncher.ui.helpers.settings.SettingsScaffold

@Composable
fun ChangelogsScreen(
    onBack: () -> Unit
) {
    val ctx = LocalContext.current
    val versionCode = rememberVersionCode()

    val updates by produceState(initialValue = emptyList()) {
        value = loadChangelogs(ctx, versionCode)
    }

    SettingsScaffold(
        title = stringResource(R.string.changelogs),
        onBack = onBack,
        helpText = stringResource(R.string.changelogs_help),
        resetText = null,
        onReset = null,
        lazyContent = {
            items(updates) { update ->
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
    )
}

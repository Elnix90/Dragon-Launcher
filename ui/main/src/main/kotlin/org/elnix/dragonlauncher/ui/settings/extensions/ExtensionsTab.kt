package org.elnix.dragonlauncher.ui.settings.extensions

import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.delay
import org.elnix.dragonlauncher.base.loadExtensionRegistry
import org.elnix.dragonlauncher.base.model.serializables.ExtensionModel
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.ktx.tryStartActivity
import org.elnix.dragonlauncher.services.ExtensionManager
import org.elnix.dragonlauncher.ui.base.components.Spacer
import org.elnix.dragonlauncher.ui.components.BetaVersionType
import org.elnix.dragonlauncher.ui.components.BetaVersionWarning
import org.elnix.dragonlauncher.ui.dragon.components.DragonButton
import org.elnix.dragonlauncher.ui.dragon.components.DragonGroupScope
import org.elnix.dragonlauncher.ui.dragon.components.DragonSettingsGroup
import org.elnix.dragonlauncher.ui.helpers.settings.SettingsScaffold
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun ExtensionsTab() {
    val ctx = LocalContext.current
    var extensions by remember { mutableStateOf<List<ExtensionModel>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val registry = loadExtensionRegistry(ctx)
        extensions = registry ?: emptyList()
        isLoading = false
    }

    SettingsScaffold(
        title = stringResource(R.string.extensions),
        helpText = stringResource(R.string.extensions_description),
        onReset = null,
        resetText = null
    ) {
        BetaVersionWarning(BetaVersionType.Feature)

        when {
            isLoading -> {
                Text(
                    text = stringResource(R.string.loading),
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            extensions.isEmpty() -> {
                Text(
                    text = stringResource(R.string.no_extensions_found),
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            else -> {
                DragonSettingsGroup {
                    extensions.forEach { extension ->
                        ExtensionItem(extension)
                    }
                }
            }
        }

        val launcher =
            rememberLauncherForActivityResult(
                contract = ActivityResultContracts.OpenDocument(),
                onResult = { uri ->
                    uri?.let {
                        ExtensionManager.installApk(ctx, it)
                    }
                }
            )

        DragonSettingsGroup(R.string.extension_manual_install_title) {
            Text(
                text = stringResource(R.string.extension_manual_install_desc),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.dragonSettingGroup()
            )
            this.DragonButton(onClick = { launcher.launch(arrayOf("application/vnd.android.package-archive")) }) {
                Text(stringResource(R.string.select_apk))
            }
        }
    }
}

@Composable
private fun DragonGroupScope.ExtensionItem(extension: ExtensionModel) {
    val ctx = LocalContext.current
    val currentLanguage = LocalLocale.current.platformLocale.language
    val lifecycleOwner = LocalLifecycleOwner.current

    val description = extension.description[currentLanguage] ?: extension.description["en"] ?: ""

    var isInstalled by remember { mutableStateOf(false) }

    LaunchedEffect(Unit, lifecycleOwner) {
        delay(200.milliseconds)
        isInstalled = ExtensionManager.isExtensionInstalled(ctx, extension.packageName)
    }

    Column(Modifier.dragonSettingGroup()) {
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMediumEmphasized,
            modifier = Modifier.padding(10.dp)
        )
        if (extension.permissions.isNotEmpty()) {
            Text(
                text = stringResource(R.string.permissions),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(8.dp)
            )
            Column(
                verticalArrangement = Arrangement.spacedBy(3.dp),
                modifier = Modifier.padding(start = 8.dp)
            ) {
                extension.permissions.forEach { permission ->
                    Text(
                        text = "• $permission",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }

        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isInstalled) {
                val pkg = extension.packageName

                @Suppress("RemoveRedundantQualifierName") // This is needed to differentiate the 3 overloads (one with the Scope receiver)
                org.elnix.dragonlauncher.ui.dragon.components.DragonButton(
                    onClick = {
                        val intent = ctx.packageManager.getLaunchIntentForPackage(pkg)
                        if (intent != null) {
                            ctx.tryStartActivity(intent)
                        } else {
                            // Try showing app info instead if no launcher intent
                            val infoIntent =
                                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                    .apply { data = "package:$pkg".toUri() }
                            ctx.tryStartActivity(infoIntent)
                        }
                    }
                ) {
                    Icon(
                        painter = painterResource(R.drawable.open_in_new),
                        contentDescription = null
                    )
                    Spacer(8.dp)
                    Text(stringResource(R.string.open))
                }
            }

            @Suppress("RemoveRedundantQualifierName") // This is needed to differentiate the 3 overloads (one with the Scope receiver)
            org.elnix.dragonlauncher.ui.dragon.components.DragonButton(
                onClick = {
                    if (!isInstalled) {
                        ExtensionManager.installExtension(ctx, extension)
                    } else {
                        // Uninstall logic (via Intent)
                        val pkg = extension.packageName
                        val intent =
                            Intent(Intent.ACTION_DELETE)
                                .apply { data = "package:$pkg".toUri() }
                        ctx.tryStartActivity(intent)
                    }
                }
            ) {
                Icon(
                    painter = painterResource(if (isInstalled) R.drawable.delete_forever else R.drawable.download),
                    contentDescription = null
                )
                Spacer(8.dp)
                Text(stringResource(if (isInstalled) R.string.uninstall else R.string.install))
            }
        }
    }
}

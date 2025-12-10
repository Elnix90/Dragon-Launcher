package org.elnix.dragonlauncher.ui


import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Launch
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.SettingsSuggest
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.R
import org.elnix.dragonlauncher.data.stores.ColorModesSettingsStore
import org.elnix.dragonlauncher.data.stores.ColorSettingsStore
import org.elnix.dragonlauncher.data.stores.DebugSettingsStore
import org.elnix.dragonlauncher.data.stores.DrawerSettingsStore
import org.elnix.dragonlauncher.data.stores.LanguageSettingsStore
import org.elnix.dragonlauncher.data.stores.PrivateSettingsStore
import org.elnix.dragonlauncher.data.stores.SwipeSettingsStore
import org.elnix.dragonlauncher.data.stores.UiSettingsStore
import org.elnix.dragonlauncher.ui.helpers.SetDefaultLauncherBanner
import org.elnix.dragonlauncher.ui.helpers.TextDivider
import org.elnix.dragonlauncher.ui.helpers.settings.ContributorItem
import org.elnix.dragonlauncher.ui.helpers.settings.SettingsItem
import org.elnix.dragonlauncher.ui.helpers.settings.SettingsLazyHeader
import org.elnix.dragonlauncher.utils.copyToClipboard
import org.elnix.dragonlauncher.utils.isDefaultLauncher
import org.elnix.dragonlauncher.utils.openUrl
import org.elnix.dragonlauncher.utils.showToast


@Suppress("AssignedValueIsNeverRead")
@Composable
fun AdvancedSettingsScreen(
    navController: NavController,
    onReset: () -> Unit,
    onBack: () -> Unit
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()


    val isDebugModeEnabled by DebugSettingsStore.getDebugEnabled(ctx)
        .collectAsState(initial = false)
    val forceAppLanguageSelector by DebugSettingsStore.getForceAppLanguageSelector(ctx)
        .collectAsState(initial = false)

    val showSetDefaultLauncherBanner by PrivateSettingsStore.getShowSetDefaultLauncherBanner(ctx)
        .collectAsState(initial = true)


    var toast by remember { mutableStateOf<Toast?>(null) }
    val versionName = ctx.packageManager.getPackageInfo(ctx.packageName, 0).versionName ?: "unknown"
    var timesClickedOnVersion by remember { mutableIntStateOf(0) }


    BackHandler { onBack() }

    SettingsLazyHeader(
        title = stringResource(R.string.settings),
        onBack = onBack,
        helpText = stringResource(R.string.settings),
        onReset = {
            scope.launch {
                UiSettingsStore.resetAll(ctx)
                DebugSettingsStore.resetAll(ctx)
                SwipeSettingsStore.resetAll(ctx)
                LanguageSettingsStore.resetAll(ctx)
                ColorModesSettingsStore.resetAll(ctx)
                ColorSettingsStore.resetAll(ctx)
                DrawerSettingsStore.resetAll(ctx)

                // Small delay to allow the default apps to load before initializing
                delay(200)
                PrivateSettingsStore.resetAll(ctx)
                onReset()
            }
        },
        banner = if (showSetDefaultLauncherBanner && !ctx.isDefaultLauncher) { { SetDefaultLauncherBanner() } } else null
    ) {
        item {
            SettingsItem(
                title = stringResource(R.string.appearance),
                icon = Icons.Default.ColorLens
            ) {
                navController.navigate(SETTINGS.APPEARANCE)
            }
        }

        item {
            SettingsItem(
                title = stringResource(R.string.settings_language_title),
                icon = Icons.Default.Language,
                onClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !forceAppLanguageSelector) {
                        openSystemLanguageSettings(ctx)
                    } else {
                        navController.navigate(SETTINGS.LANGUAGE)
                    }
                }
            )
        }

        item {
           SettingsItem(
               title = stringResource(R.string.backup_restore),
               icon = Icons.Default.Restore
            ) {
                navController.navigate(SETTINGS.BACKUP)
            }
        }

        item {
            SettingsItem(
                title = stringResource(R.string.app_drawer),
                icon = Icons.Default.GridOn
            ) {
                navController.navigate(SETTINGS.DRAWER)
            }
        }


        item {
            SettingsItem(
                title = stringResource(R.string.android_settings),
                icon = Icons.Default.SettingsSuggest,
                leadIcon = Icons.AutoMirrored.Filled.Launch
            ) {
                val packageName = ctx.packageName
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", packageName, null)
                }
                ctx.startActivity(intent)
            }
        }


        item {
            if (isDebugModeEnabled) {
                SettingsItem(
                    title = stringResource(R.string.debug),
                    icon = Icons.Default.BugReport
                ) {
                    navController.navigate(SETTINGS.DEBUG)
                }
            }
        }


        item { TextDivider(stringResource(R.string.about)) }

        item {
            SettingsItem(
                title = stringResource(R.string.source_code),
                icon = Icons.Default.Code,
                leadIcon = Icons.AutoMirrored.Filled.Launch
            ) { ctx.openUrl("https://github.com/Elnix90/Dragon-Launcher") }
        }

        item {
            SettingsItem(
                title = stringResource(R.string.check_for_update),
                description = stringResource(R.string.check_for_updates_text),
                icon = Icons.Default.Update,
                leadIcon = Icons.AutoMirrored.Filled.Launch
            ) { ctx.openUrl("https://github.com/Elnix90/Dragon-Launcher/releases/latest") }
        }

        item {
            SettingsItem(
                title = stringResource(R.string.report_a_bug),
                description =stringResource(R.string.open_an_issue_on_github),
                icon = Icons.Default.ReportProblem,
                leadIcon = Icons.AutoMirrored.Filled.Launch
            ) { ctx.openUrl("https://github.com/Elnix90/Dragon-Launcher/issues/new") }
        }


        item {
            TextDivider(
                stringResource(R.string.contributors),
                Modifier.padding(horizontal = 60.dp)
            )
        }

        item {
            ContributorItem(
                name = "Elnix90",
                imageRes = R.drawable.elnix90,
                description = stringResource(R.string.app_developer),
                githubUrl = "https://github.com/Elnix90"
            )
        }

        item {
            ContributorItem(
                name = "ragebreaker (mlm-games)",
                imageRes = R.drawable.ragebreaker,
                description = stringResource(R.string.thanks_for_the_inspiration),
                githubUrl = "https://github.com/mlm-games/CCLauncher"
            )
        }

        item {
            Text(
                text = "${stringResource(R.string.version)} $versionName",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .padding(top = 8.dp, bottom = 16.dp)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        toast?.cancel()

                        when {

                            timesClickedOnVersion == 0 -> {
                                ctx.copyToClipboard(versionName)
                                ctx.showToast("Version name copied to clipboard")
                                timesClickedOnVersion += 1
                            }

                            isDebugModeEnabled -> {
                                toast = Toast.makeText(
                                    ctx,
                                    "Debug Mode is already enabled",
                                    Toast.LENGTH_SHORT
                                )
                                toast?.show()
                            }


                            timesClickedOnVersion < 6 -> {
                                timesClickedOnVersion++
                                if (timesClickedOnVersion > 2) {
                                    toast = Toast.makeText(
                                        ctx,
                                        "${7 - timesClickedOnVersion} more times to enable Debug Mode",
                                        Toast.LENGTH_SHORT
                                    )
                                }
                                toast?.show()
                            }

                            else -> {
                                scope.launch {
                                    DebugSettingsStore.setDebugEnabled(ctx, true)
                                    navController.navigate(SETTINGS.DEBUG)
                                }
                            }
                        }
                    }
            )
        }
    }
}



@RequiresApi(Build.VERSION_CODES.TIRAMISU)
private fun openSystemLanguageSettings(context: Context) {
    val intent = Intent(Settings.ACTION_APP_LOCALE_SETTINGS).apply {
        data = Uri.fromParts("package", context.packageName, null)
    }
    context.startActivity(intent)
}

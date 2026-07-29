package org.elnix.dragonlauncher.ui


import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.elnix90.core.util.clearAllData
import io.github.elnix90.runtime.asState
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.base.Constants.URLs.ELNIX90_GITHUB_PROFILE_LINK
import org.elnix.dragonlauncher.base.Constants.URLs.EXTENSIONS_GITHUB_REPO_LINK
import org.elnix.dragonlauncher.base.Constants.URLs.GITHUB_REPO_ISSUES_LINK
import org.elnix.dragonlauncher.base.Constants.URLs.GITHUB_REPO_LINK
import org.elnix.dragonlauncher.base.Constants.URLs.GITHUB_REPO_RELEASES_LINK
import org.elnix.dragonlauncher.base.navigaton.NavigationRoute
import org.elnix.dragonlauncher.common.utils.CopyPasteUtils.copyToClipboard
import org.elnix.dragonlauncher.common.utils.LifecycleUtils.closeApp
import org.elnix.dragonlauncher.common.utils.VersionsUtils.isBetaVersion
import org.elnix.dragonlauncher.common.utils.rememberVersionCode
import org.elnix.dragonlauncher.common.utils.rememberVersionName
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.ktx.alphaMultiplier
import org.elnix.dragonlauncher.ktx.showToast
import org.elnix.dragonlauncher.models.PointsViewModel
import org.elnix.dragonlauncher.models.SecurityViewModel
import org.elnix.dragonlauncher.settings.stores.map.DebugSettingsStore
import org.elnix.dragonlauncher.settings.stores.map.PrivateSettingsStore
import org.elnix.dragonlauncher.ui.base.activityViewModel
import org.elnix.dragonlauncher.ui.base.asState
import org.elnix.dragonlauncher.ui.components.BetaVersionType
import org.elnix.dragonlauncher.ui.components.BetaVersionWarning
import org.elnix.dragonlauncher.ui.components.LocalePickerSheet
import org.elnix.dragonlauncher.ui.compositionslocals.LocalNavigator
import org.elnix.dragonlauncher.ui.dragon.components.DragonSettingsGroup
import org.elnix.dragonlauncher.ui.helpers.settings.ContributorItem
import org.elnix.dragonlauncher.ui.helpers.settings.RouteItem
import org.elnix.dragonlauncher.ui.helpers.settings.SettingsItem
import org.elnix.dragonlauncher.ui.helpers.settings.SettingsScaffold
import org.elnix.dragonlauncher.ui.warning.GoogleWarningManager
import org.elnix.dragonlauncher.ui.warning.GoogleWarningReminder


@SuppressLint("LocalContextGetResourceValueCall")
@Composable
fun SettingsScreen(
    securityViewModel: SecurityViewModel = activityViewModel(),
    pointsViewModel: PointsViewModel = activityViewModel()
) {
    val ctx = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val navigator = LocalNavigator.current
    val scope = rememberCoroutineScope()

    val versionCode by rememberVersionCode()
    val versionName by rememberVersionName()

    val isDebugModeEnabled by DebugSettingsStore.debugEnabled.asState()

    var toast by remember { mutableStateOf<Toast?>(null) }
    var timesClickedOnVersion by remember { mutableIntStateOf(0) }

    var showLanguageSheet by remember { mutableStateOf(false) }


    val hideBetaVersionWarning by PrivateSettingsStore.hideBetaVersionWarning.asState(true)
    val showBetaVersionWarning = remember(hideBetaVersionWarning) {
        ctx.isBetaVersion() && !hideBetaVersionWarning
    }

    val nestId by pointsViewModel.nestsNavigationService.currentNestId.collectAsState()

    val signatureMatched by securityViewModel.signatureMatched.asState()

    SettingsScaffold(
        title = stringResource(R.string.settings),
        onBack = navigator::onBack,
        helpText = stringResource(R.string.settings),
        resetTitle = stringResource(R.string.reset_all_settings),
        resetText = stringResource(R.string.every_setting_will_return_to_its_default_state_this_cannot_be_undone_the_app_will_kill_itself),
        onReset = {
            scope.launch {
                clearAllData(ctx)
                closeApp(ctx as ComponentActivity)
            }
        }
    ) {
        AnimatedVisibility(showBetaVersionWarning) {
            BetaVersionWarning(BetaVersionType.App)
        }

        AnimatedVisibility(GoogleWarningManager.showWarning()) {
            GoogleWarningReminder()
        }

        AnimatedVisibility(!signatureMatched) {
            BetaVersionWarning(BetaVersionType.Custom(R.string.signature_not_matched))
        }

        DragonSettingsGroup(R.string.common_settings) {
            RouteItem(NavigationRoute.Appearance)
            RouteItem(NavigationRoute.Wallpaper)
            RouteItem(NavigationRoute.Widgets(nestId))
            RouteItem(NavigationRoute.Behavior)
            RouteItem(NavigationRoute.Backup)
            RouteItem(NavigationRoute.DrawerSettings)
            RouteItem(NavigationRoute.Wellbeing)

            val forceAppLanguageSelector by DebugSettingsStore.forceAppLanguageSelector.asState()
            SettingsItem(
                title = stringResource(R.string.language),
                icon = R.drawable.web,
                onClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !forceAppLanguageSelector) {
                        openSystemLanguageSettings(ctx)
                    } else {
                        showLanguageSheet = true
                    }
                }
            )
        }

        DragonSettingsGroup(R.string.advanced) {
            RouteItem(
                route = NavigationRoute.Extensions,
                onExternalClick = { uriHandler.openUri(EXTENSIONS_GITHUB_REPO_LINK) }
            )

            SettingsItem(
                title = stringResource(R.string.android_settings),
                icon = R.drawable.settings_alert,
                trailingIcon = R.drawable.open_in_new
            ) {
                val packageName = ctx.packageName
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", packageName, null)
                }
                ctx.startActivity(intent)
            }

            AnimatedVisibility(isDebugModeEnabled) {
                RouteItem(NavigationRoute.Debug)
            }
        }

        DragonSettingsGroup(R.string.about) {
            SettingsItem(
                title = stringResource(R.string.changelogs),
                icon = R.drawable.source_notes,
                trailingIcon = R.drawable.open_in_new,
                onExternalClick = { uriHandler.openUri("$GITHUB_REPO_LINK/blob/main/fastlane/metadata/android/en-US/changelogs/${versionCode}.txt") }
            ) { navigator.navigate(NavigationRoute.Changelogs) }

            SettingsItem(
                title = stringResource(R.string.source_code),
                icon = R.drawable.code,
                trailingIcon = R.drawable.open_in_new,
                onLongClick = { ctx.copyToClipboard(GITHUB_REPO_LINK) }
            ) { uriHandler.openUri(GITHUB_REPO_LINK) }

            SettingsItem(
                title = stringResource(R.string.check_for_update),
                description = stringResource(R.string.check_for_updates_github),
                icon = R.drawable.reset,
                trailingIcon = R.drawable.open_in_new,
                onLongClick = { ctx.copyToClipboard(GITHUB_REPO_RELEASES_LINK) }
            ) { uriHandler.openUri(GITHUB_REPO_RELEASES_LINK) }

            SettingsItem(
                title = stringResource(R.string.report_a_bug),
                description = stringResource(R.string.open_an_issue_on_github),
                icon = R.drawable.report,
                trailingIcon = R.drawable.open_in_new,
                onLongClick = { ctx.copyToClipboard(GITHUB_REPO_ISSUES_LINK) }
            ) { uriHandler.openUri(GITHUB_REPO_ISSUES_LINK) }
        }


        DragonSettingsGroup(R.string.contributors) {
            ContributorItem(
                name = "Elnix90",
                imageRes = R.drawable.elnix90,
                description = stringResource(R.string.app_developer),
                githubUrl = ELNIX90_GITHUB_PROFILE_LINK
            )

            ContributorItem(
                name = "YoannDev90",
                imageRes = R.drawable.yoanndev90,
                description = stringResource(R.string.yoann_desc),
                githubUrl = "https://github.com/YoannDev90"
            )

            ContributorItem(
                name = "Lucky",
                imageRes = R.drawable.lucky_the_cookie,
                description = stringResource(R.string.lucky_desc),
                githubUrl = "https://lthb.fr"
            )

            ContributorItem(
                name = "Federico",
                imageRes = R.drawable.federico,
                description = stringResource(R.string.federico_desc),
                githubUrl = "https://github.com/federicobuttafuori"
            )
        }


        // Version name (clickable to access debug / copy)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val infoStyle = MaterialTheme.typography.labelSmall
            val infoColor = MaterialTheme.colorScheme.onBackground.alphaMultiplier(0.7f)

            val debugModeAlreadyEnabledText =
                stringResource(R.string.debug_mode_already_enabled)
            val versionNameCopiedToClipboard =
                stringResource(R.string.version_copied_to_clipboard)

            Text(
                text = "${stringResource(R.string.app_name)} $versionName ($versionCode)",
                style = infoStyle,
                textAlign = TextAlign.Center,
                color = infoColor,
                modifier = Modifier
                    .padding(top = 16.dp, bottom = 16.dp)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        toast?.cancel()

                        when {

                            timesClickedOnVersion == 0 -> {
                                ctx.copyToClipboard(versionName)
                                ctx.showToast(versionNameCopiedToClipboard)
                                timesClickedOnVersion += 1
                            }

                            isDebugModeEnabled -> {
                                toast = Toast.makeText(
                                    ctx,
                                    debugModeAlreadyEnabledText,
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
                                scope.launch { DebugSettingsStore.debugEnabled.set(ctx, true) }
                            }
                        }
                    }
            )
        }
    }

    if (showLanguageSheet) {
        LocalePickerSheet { showLanguageSheet = false }
    }
}


@RequiresApi(Build.VERSION_CODES.TIRAMISU)
private fun openSystemLanguageSettings(ctx: Context) {
    val intent = Intent(Settings.ACTION_APP_LOCALE_SETTINGS).apply {
        data = Uri.fromParts("package", ctx.packageName, null)
    }
    ctx.startActivity(intent)
}

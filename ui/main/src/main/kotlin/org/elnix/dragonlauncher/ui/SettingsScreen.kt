package org.elnix.dragonlauncher.ui


import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ButtonGroupScope
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.elnix90.core.util.clearAllData
import io.github.elnix90.runtime.asState
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.base.Constants.URLs.CODEBERG_REPO_LINK
import org.elnix.dragonlauncher.base.Constants.URLs.DISCORD_INVITE_LINK
import org.elnix.dragonlauncher.base.Constants.URLs.DRAGON_WEBSITE
import org.elnix.dragonlauncher.base.Constants.URLs.ELNIX90_BUY_ME_A_COFFEE
import org.elnix.dragonlauncher.base.Constants.URLs.ELNIX90_GITHUB_PROFILE_LINK
import org.elnix.dragonlauncher.base.Constants.URLs.EXTENSIONS_GITHUB_REPO_LINK
import org.elnix.dragonlauncher.base.Constants.URLs.GITHUB_REPO_ISSUES_LINK
import org.elnix.dragonlauncher.base.Constants.URLs.GITHUB_REPO_LINK
import org.elnix.dragonlauncher.base.Constants.URLs.GITHUB_REPO_RELEASES_LINK
import org.elnix.dragonlauncher.base.Constants.URLs.GITLAB_REPO_LINK
import org.elnix.dragonlauncher.base.Constants.URLs.MAILTO_LINK
import org.elnix.dragonlauncher.base.Constants.URLs.REDDIT_LINK
import org.elnix.dragonlauncher.base.Constants.URLs.WEBLATE_LINK
import org.elnix.dragonlauncher.base.model.models.SocialLink
import org.elnix.dragonlauncher.base.model.models.buyMeACoffee
import org.elnix.dragonlauncher.base.model.models.codeberg
import org.elnix.dragonlauncher.base.model.models.github
import org.elnix.dragonlauncher.base.model.models.gitlab
import org.elnix.dragonlauncher.base.model.models.openInNew
import org.elnix.dragonlauncher.base.navigaton.NavigationRoute
import org.elnix.dragonlauncher.base.utils.CopyPasteUtils.copyToClipboard
import org.elnix.dragonlauncher.base.utils.LifecycleUtils.closeApp
import org.elnix.dragonlauncher.base.utils.VersionsUtils.isBetaVersion
import org.elnix.dragonlauncher.base.utils.rememberVersionCode
import org.elnix.dragonlauncher.base.utils.rememberVersionName
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


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
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
        AnimatedVisibility(GoogleWarningManager.showWarning()) {
            GoogleWarningReminder()
        }

        AnimatedVisibility(showBetaVersionWarning) {
            BetaVersionWarning(BetaVersionType.App)
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
                github(EXTENSIONS_GITHUB_REPO_LINK),
            )

            SettingsItem(
                title = stringResource(R.string.android_settings),
                icon = R.drawable.settings_alert
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
            Row(
                modifier = Modifier
                    .height(60.dp)
                    .dragonSettingGroup(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {

                fun ButtonGroupScope.ic(
                    @DrawableRes ic: Int,
                    link: String,
                    `is`: MutableInteractionSource
                ) {
                    customItem(
                        buttonGroupContent = {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .animateWidth(`is`)
                                    .fillMaxHeight()
                                    .clip(MaterialTheme.shapes.extraLarge)
                                    .clickable(interactionSource = `is`) { uriHandler.openUri(link) }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(ic),
                                    contentDescription = null,
                                    tint = Color.Unspecified,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    ) {}
                }

                val githubIcon = if (MaterialTheme.colorScheme.background.luminance() < 0.5) {
                    R.drawable.github_invertocat_white
                } else {
                    R.drawable.github_invertocat_black
                }

                val interactionSources = remember { List(6) { MutableInteractionSource() } }
                ButtonGroup(
                    overflowIndicator = { ButtonGroupDefaults.OverflowIndicator(it) },
                    horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)
                ) {
                    ic(githubIcon, GITHUB_REPO_LINK, interactionSources[0])
                    ic(R.drawable.discord_symbol_blurple, DISCORD_INVITE_LINK, interactionSources[1])
                    ic(R.drawable.reddit_icon_fullcolor, REDDIT_LINK, interactionSources[2])
                    ic(R.mipmap.dragon_launcher_foreground, DRAGON_WEBSITE, interactionSources[3])
                    ic(R.drawable.weblate_icon, WEBLATE_LINK, interactionSources[4])
                    ic(R.drawable.protonmail_icon, MAILTO_LINK, interactionSources[5])
                }
            }


            SettingsItem(
                title = stringResource(R.string.source_code),
                icon = R.drawable.code,
                description = null,
                github(GITHUB_REPO_LINK),
                gitlab(GITLAB_REPO_LINK),
                codeberg(CODEBERG_REPO_LINK)
            ) { uriHandler.openUri(GITHUB_REPO_LINK) }


            SettingsItem(
                title = stringResource(R.string.changelogs),
                icon = R.drawable.source_notes,
                description = null,
                openInNew("$GITHUB_REPO_LINK/blob/main/fastlane/metadata/android/en-US/changelogs/${versionCode}.txt")
            ) { navigator.navigate(NavigationRoute.Changelogs) }

            SettingsItem(
                title = stringResource(R.string.check_for_update),
                icon = R.drawable.reset,
                description = stringResource(R.string.check_for_updates_github),
                openInNew(GITHUB_REPO_RELEASES_LINK)
            ) { uriHandler.openUri(GITHUB_REPO_RELEASES_LINK) }

            SettingsItem(
                title = stringResource(R.string.report_a_bug),
                icon = R.drawable.report,
                description = stringResource(R.string.open_an_issue_on_github),
                openInNew(GITHUB_REPO_ISSUES_LINK)
            ) { uriHandler.openUri(GITHUB_REPO_ISSUES_LINK) }
        }



        DragonSettingsGroup(R.string.app_developer) {
            ContributorItem(
                name = "Elnix90",
                shape = MaterialShapes.Circle,
                imageRes = R.mipmap.elnix90,
                description = stringResource(R.string.app_developer),
                github(ELNIX90_GITHUB_PROFILE_LINK),
                buyMeACoffee(ELNIX90_BUY_ME_A_COFFEE)
            )
        }

        DragonSettingsGroup(R.string.contributors) {
            ContributorItem(
                name = "YoannDev90",
                shape = MaterialShapes.Gem,
                imageRes = R.mipmap.yoanndev90,
                description = stringResource(R.string.yoann_desc),
                github("https://github.com/YoannDev90"),
                buyMeACoffee("https://buymeacoffee.com/yoanndev90")
            )


            // TODO write script to fetch total lines added / removed and diaslay them per user
            ContributorItem(
                name = "Lucky",
                shape = MaterialShapes.Cookie7Sided,
                imageRes = R.mipmap.lucky_the_cookie,
                description = stringResource(R.string.lucky_desc),
                github("https://lthb.fr")
            )

            ContributorItem(
                name = "Federico",
                shape = MaterialShapes.Pill,
                imageRes = R.mipmap.federico,
                description = stringResource(R.string.federico_desc),
                github("https://github.com/federicobuttafuori"),
            )


            DragonSettingsGroup(R.string.translators) {
                val translators = listOf(
                    SocialLink("https://github.com/manmen2414", R.mipmap.mameeenn),
                    SocialLink("https://github.com/acress1", R.mipmap.acress1),
                    SocialLink("https://github.com/TamilNeram", R.mipmap.tamilneram),
                    SocialLink("https://github.com/sudo-py-dev", R.mipmap.sudopydev)
                )

                Column(
                    modifier = Modifier
                        .dragonSettingGroup(),
                    verticalArrangement = Arrangement.spacedBy(15.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    translators.chunked(7).forEach { translatorRow -> // Magic number hehe (it simply fits the screen perfectly
                        Row(horizontalArrangement = Arrangement.spacedBy(15.dp)) {
                            translatorRow.forEach { translator ->
                                Image(
                                    painter = painterResource(id = translator.icon),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .clickable {
                                            uriHandler.openUri(translator.url)
                                        },
                                    contentScale = ContentScale.Fit
                                )
                            }
                        }
                    }
                }
            }
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
                stringResource(R.string.copied_to_clipboard)

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

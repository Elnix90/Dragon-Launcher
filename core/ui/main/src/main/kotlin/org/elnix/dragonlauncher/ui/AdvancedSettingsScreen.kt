package org.elnix.dragonlauncher.ui


import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.base.ColorUtils.alphaMultiplier
import org.elnix.dragonlauncher.common.R
import org.elnix.dragonlauncher.common.messyfolder.Constants.URLs.DISCORD_INVITE_LINK
import org.elnix.dragonlauncher.common.messyfolder.Constants.URLs.DRAGON_WEBSITE
import org.elnix.dragonlauncher.common.messyfolder.Constants.URLs.ELNIX90_GITHUB_PROFILE_LINK
import org.elnix.dragonlauncher.common.messyfolder.Constants.URLs.EXTENSIONS_GITHUB_REPO_LINK
import org.elnix.dragonlauncher.common.messyfolder.Constants.URLs.GITHUB_REPO_ISSUES_LINK
import org.elnix.dragonlauncher.common.messyfolder.Constants.URLs.GITHUB_REPO_LINK
import org.elnix.dragonlauncher.common.messyfolder.Constants.URLs.GITHUB_REPO_RELEASES_LINK
import org.elnix.dragonlauncher.common.messyfolder.Constants.URLs.MAILTO_LINK
import org.elnix.dragonlauncher.common.messyfolder.Constants.URLs.REDDIT_LINK
import org.elnix.dragonlauncher.common.messyfolder.Constants.URLs.WEBLATE_LINK
import org.elnix.dragonlauncher.common.messyfolder.openUrl
import org.elnix.dragonlauncher.common.messyfolder.showToast
import org.elnix.dragonlauncher.common.navigaton.NavigationRoute
import org.elnix.dragonlauncher.common.utils.CopyPasteUtils.copyToClipboard
import org.elnix.dragonlauncher.common.utils.LifecycleUtils.closeApp
import org.elnix.dragonlauncher.common.utils.VersionsUtils.isBetaVersion
import org.elnix.dragonlauncher.common.utils.rememberVersionCode
import org.elnix.dragonlauncher.settings.clearAllData
import org.elnix.dragonlauncher.settings.stores.DebugSettingsStore
import org.elnix.dragonlauncher.settings.stores.PrivateSettingsStore
import org.elnix.dragonlauncher.ui.base.UiConstants.DragonShape
import org.elnix.dragonlauncher.ui.base.asState
import org.elnix.dragonlauncher.ui.components.BetaVersionType
import org.elnix.dragonlauncher.ui.components.BetaVersionWarning
import org.elnix.dragonlauncher.ui.dragon.text.TextDivider
import org.elnix.dragonlauncher.ui.helpers.settings.ContributorItem
import org.elnix.dragonlauncher.ui.helpers.settings.SettingItemWithExternalOpen
import org.elnix.dragonlauncher.ui.helpers.settings.SettingsItem
import org.elnix.dragonlauncher.ui.helpers.settings.SettingsScaffold


@SuppressLint("LocalContextGetResourceValueCall")
@Composable
fun AdvancedSettingsScreen(
    onNavigate: (NavigationRoute) -> Unit,
    onBack: () -> Unit
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    val versionCode = rememberVersionCode()

    val isDebugModeEnabled by DebugSettingsStore.debugEnabled.asState()

    var toast by remember { mutableStateOf<Toast?>(null) }
    val versionName = ctx.packageManager.getPackageInfo(ctx.packageName, 0).versionName ?: "unknown"
    var timesClickedOnVersion by remember { mutableIntStateOf(0) }

    val backgroundColor = MaterialTheme.colorScheme.background


    val hideBetaVersionWarning by PrivateSettingsStore.hideBetaVersionWarning.asState(true)
    val showBetaVersionWarning = remember(hideBetaVersionWarning) {
        ctx.isBetaVersion() && !hideBetaVersionWarning
    }

    SettingsScaffold(
        title = stringResource(R.string.settings),
        onBack = onBack,
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
        if (showBetaVersionWarning) {
            BetaVersionWarning(BetaVersionType.App)
        }


        SettingsItem(
            title = stringResource(R.string.appearance),
            icon = R.drawable.palette
        ) { onNavigate(NavigationRoute.Appearance) }

        SettingsItem(
            title = stringResource(R.string.behavior),
            icon = R.drawable.question_mark
        ) { onNavigate(NavigationRoute.Behavior) }

        SettingsItem(
            title = stringResource(R.string.backup_restore),
            icon = R.drawable.reset
        ) { onNavigate(NavigationRoute.Backup) }

        SettingsItem(
            title = stringResource(R.string.app_drawer),
            icon = R.drawable.grid_on
        ) { onNavigate(NavigationRoute.DrawerSettings) }

        SettingsItem(
            title = stringResource(R.string.workspaces),
            icon = R.drawable.workspaces
        ) { onNavigate(NavigationRoute.Workspace) }

        SettingsItem(
            title = stringResource(R.string.wellbeing),
            icon = R.drawable.self_improvement
        ) { onNavigate(NavigationRoute.Wellbeing) }
        TextDivider(stringResource(R.string.advanced))
//
//        item {
//            SettingsItem(
//                title = stringResource(R.string.permissions),
//                icon = Icons.Default.Security
//            ) {
//                navController.navigate(SETTINGS.PERMISSIONS)
//            }
//        }

        SettingItemWithExternalOpen(
            title = stringResource(R.string.extensions),
            icon = R.drawable.extension,
            onExtClick = { ctx.openUrl(EXTENSIONS_GITHUB_REPO_LINK) }
        ) {
            onNavigate(NavigationRoute.Extensions)
        }

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
            SettingsItem(
                title = stringResource(R.string.debug),
                icon = R.drawable.bug_report,
                modifier = Modifier
            ) {
                onNavigate(NavigationRoute.Debug)
            }
        }


        TextDivider(stringResource(R.string.about))


        // Social links
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(20.dp)
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.Center
        ) {

            val githubIcon = if (backgroundColor.luminance() < 0.5) {
                R.drawable.github_invertocat_white
            } else {
                R.drawable.github_invertocat_black
            }
            Icon(
                painter = painterResource(githubIcon),
                contentDescription = "Github Icon",
                tint = Color.Unspecified,
                modifier = Modifier
                    .weight(1f)
                    .clip(DragonShape)
                    .clickable { ctx.openUrl(GITHUB_REPO_LINK) }
            )


            Icon(
                painterResource(R.drawable.discord_symbol_blurple),
                contentDescription = "Discord icon",
                tint = Color.Unspecified,
                modifier = Modifier
                    .weight(1f)
                    .clip(DragonShape)
                    .clickable { ctx.openUrl(DISCORD_INVITE_LINK) }
            )

            Icon(
                painterResource(R.drawable.reddit_icon_fullcolor),
                contentDescription = "Reddit icon",
                tint = Color.Unspecified,
                modifier = Modifier
                    .weight(1f)
                    .clip(DragonShape)
                    .clickable { ctx.openUrl(REDDIT_LINK) }
            )

            Icon(
                painterResource(R.drawable.dragon_launcher_foreground),
                contentDescription = "Website icon",
                tint = Color.Unspecified,
                modifier = Modifier
                    .weight(1f)
                    .clip(DragonShape)
                    .clickable { ctx.openUrl(DRAGON_WEBSITE) }
            )

            Icon(
                painterResource(R.drawable.weblate_icon),
                contentDescription = "Weblate icon",
                tint = Color.Unspecified,
                modifier = Modifier
                    .weight(1f)
                    .clip(DragonShape)
                    .clickable { ctx.openUrl(WEBLATE_LINK) }
            )

            Icon(
                painterResource(R.drawable.protonmail_icon),
                contentDescription = "Proton Mail icon",
                tint = Color.Unspecified,
                modifier = Modifier
                    .weight(1f)
                    .clip(DragonShape)
                    .clickable { ctx.openUrl(MAILTO_LINK) }
            )
        }



        SettingItemWithExternalOpen(
            title = stringResource(R.string.changelogs),
            icon = R.drawable.source_notes,
            onExtClick = { ctx.openUrl("$GITHUB_REPO_LINK/blob/main/fastlane/metadata/android/en-US/changelogs/${versionCode}.txt") }
        ) {
            onNavigate(NavigationRoute.Changelogs)
        }

        SettingsItem(
            title = stringResource(R.string.source_code),
            icon = R.drawable.code,
            trailingIcon = R.drawable.open_in_new,
            onLongClick = { ctx.copyToClipboard(GITHUB_REPO_LINK) }
        ) { ctx.openUrl(GITHUB_REPO_LINK) }

        SettingsItem(
            title = stringResource(R.string.check_for_update),
            description = stringResource(R.string.check_for_updates_github),
            icon = R.drawable.reset,
            trailingIcon = R.drawable.open_in_new,
            onLongClick = { ctx.copyToClipboard(GITHUB_REPO_RELEASES_LINK) }
        ) {
            ctx.openUrl(GITHUB_REPO_RELEASES_LINK)
        }

        SettingsItem(
            title = stringResource(R.string.report_a_bug),
            description = stringResource(R.string.open_an_issue_on_github),
            icon = R.drawable.report,
            trailingIcon = R.drawable.open_in_new,
            onLongClick = { ctx.copyToClipboard(GITHUB_REPO_ISSUES_LINK) }
        ) { ctx.openUrl(GITHUB_REPO_ISSUES_LINK) }

        TextDivider(
            stringResource(R.string.contributors),
            Modifier.padding(horizontal = 60.dp)
        )


        // Contributors
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
                text = "Dragon Launcher $versionName ($versionCode)",
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
}


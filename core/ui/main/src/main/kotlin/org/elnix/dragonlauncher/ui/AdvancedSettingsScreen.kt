package org.elnix.dragonlauncher.ui


import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Launch
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.SettingsSuggest
import androidx.compose.material.icons.filled.Update
import androidx.compose.material.icons.filled.Workspaces
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
import org.elnix.dragonlauncher.common.navigaton.SETTINGS
import org.elnix.dragonlauncher.common.utils.Constants.URLs.DISCORD_INVITE_LINK
import org.elnix.dragonlauncher.common.utils.Constants.URLs.DRAGON_WEBSITE
import org.elnix.dragonlauncher.common.utils.Constants.URLs.ELNIX90_GITHUB_PROFILE_LINK
import org.elnix.dragonlauncher.common.utils.Constants.URLs.EXTENSIONS_GITHUB_REPO_LINK
import org.elnix.dragonlauncher.common.utils.Constants.URLs.GITHUB_REPO_ISSUES_LINK
import org.elnix.dragonlauncher.common.utils.Constants.URLs.GITHUB_REPO_LINK
import org.elnix.dragonlauncher.common.utils.Constants.URLs.GITHUB_REPO_RELEASES_LINK
import org.elnix.dragonlauncher.common.utils.Constants.URLs.MAILTO_LINK
import org.elnix.dragonlauncher.common.utils.Constants.URLs.REDDIT_LINK
import org.elnix.dragonlauncher.common.utils.Constants.URLs.WEBLATE_LINK
import org.elnix.dragonlauncher.common.utils.closeApp
import org.elnix.dragonlauncher.common.utils.copyToClipboard
import org.elnix.dragonlauncher.common.utils.getVersionCode
import org.elnix.dragonlauncher.common.utils.isBetaVersion
import org.elnix.dragonlauncher.common.utils.openUrl
import org.elnix.dragonlauncher.common.utils.showToast
import org.elnix.dragonlauncher.settings.clearAllData
import org.elnix.dragonlauncher.settings.stores.DebugSettingsStore
import org.elnix.dragonlauncher.settings.stores.PrivateSettingsStore
import org.elnix.dragonlauncher.ui.base.UiConstants.DragonShape
import org.elnix.dragonlauncher.ui.base.asState
import org.elnix.dragonlauncher.ui.components.BetaVersionType
import org.elnix.dragonlauncher.ui.components.BetaVersionWarning
import org.elnix.dragonlauncher.ui.composition.LocalNavController
import org.elnix.dragonlauncher.ui.dragon.text.TextDivider
import org.elnix.dragonlauncher.ui.helpers.settings.ContributorItem
import org.elnix.dragonlauncher.ui.helpers.settings.SettingItemWithExternalOpen
import org.elnix.dragonlauncher.ui.helpers.settings.SettingsItem
import org.elnix.dragonlauncher.ui.helpers.settings.SettingsScaffold


@SuppressLint("LocalContextGetResourceValueCall")
@Composable
fun AdvancedSettingsScreen(
    onBack: () -> Unit
) {
    val ctx = LocalContext.current
    val navController = LocalNavController.current

    val scope = rememberCoroutineScope()

    val versionCode = ctx.getVersionCode()

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
        },
    ) {

        if (showBetaVersionWarning) {
            item {
                BetaVersionWarning(BetaVersionType.App)
            }
        }

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
                title = stringResource(R.string.behavior),
                icon = Icons.Default.QuestionMark
            ) {
                navController.navigate(SETTINGS.BEHAVIOR)
            }
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
                title = stringResource(R.string.workspaces),
                icon = Icons.Default.Workspaces
            ) {
                navController.navigate(SETTINGS.WORKSPACE)
            }
        }

        item {
            SettingsItem(
                title = stringResource(R.string.wellbeing),
                icon = Icons.Default.SelfImprovement
            ) {
                navController.navigate(SETTINGS.WELLBEING)
            }
        }

        item { TextDivider(stringResource(R.string.advanced)) }
//
//        item {
//            SettingsItem(
//                title = stringResource(R.string.permissions),
//                icon = Icons.Default.Security
//            ) {
//                navController.navigate(SETTINGS.PERMISSIONS)
//            }
//        }

        item {
            SettingItemWithExternalOpen(
                title = stringResource(R.string.extensions),
                icon = Icons.Default.Extension,
                onExtClick = { ctx.openUrl(EXTENSIONS_GITHUB_REPO_LINK) }
            ) { navController.navigate(SETTINGS.EXTENSIONS) }
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


        if (isDebugModeEnabled) {
            item {
                SettingsItem(
                    title = stringResource(R.string.debug),
                    icon = Icons.Default.BugReport,
                    modifier = Modifier.animateItem()
                ) {
                    navController.navigate(SETTINGS.DEBUG)
                }
            }
        }


        item { TextDivider(stringResource(R.string.about)) }


        // Social links
        item {
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
        }

        item {

            SettingItemWithExternalOpen(
                title = stringResource(R.string.changelogs),
                icon = Icons.AutoMirrored.Filled.Notes,
                onExtClick = { ctx.openUrl("$GITHUB_REPO_LINK/blob/main/fastlane/metadata/android/en-US/changelogs/${versionCode}.txt") }
            ) { navController.navigate(SETTINGS.CHANGELOGS) }
        }

        item {
            SettingsItem(
                title = stringResource(R.string.source_code),
                icon = Icons.Default.Code,
                leadIcon = Icons.AutoMirrored.Filled.Launch,
                onLongClick = { ctx.copyToClipboard(GITHUB_REPO_LINK) }
            ) { ctx.openUrl(GITHUB_REPO_LINK) }
        }

        item {
            SettingsItem(
                title = stringResource(R.string.check_for_update),
                description = stringResource(R.string.check_for_updates_github),
                icon = Icons.Default.Update,
                leadIcon = Icons.AutoMirrored.Filled.Launch,
                onLongClick = { ctx.copyToClipboard(GITHUB_REPO_RELEASES_LINK) }
            ) {
                ctx.openUrl(GITHUB_REPO_RELEASES_LINK)
            }
        }

        item {
            SettingsItem(
                title = stringResource(R.string.report_a_bug),
                description = stringResource(R.string.open_an_issue_on_github),
                icon = Icons.Default.ReportProblem,
                leadIcon = Icons.AutoMirrored.Filled.Launch,
                onLongClick = { ctx.copyToClipboard(GITHUB_REPO_ISSUES_LINK) }
            ) { ctx.openUrl(GITHUB_REPO_ISSUES_LINK) }
        }


        item {
            TextDivider(
                stringResource(R.string.contributors),
                Modifier.padding(horizontal = 60.dp)
            )
        }


        // Contributors
        item {
            ContributorItem(
                name = "Elnix90",
                imageRes = R.drawable.elnix90,
                description = stringResource(R.string.app_developer),
                githubUrl = ELNIX90_GITHUB_PROFILE_LINK
            )
        }

        item {
            ContributorItem(
                name = "YoannDev90",
                imageRes = R.drawable.yoanndev90,
                description = stringResource(R.string.yoann_desc),
                githubUrl = "https://github.com/YoannDev90"
            )
        }

        item {
            ContributorItem(
                name = "Lucky",
                imageRes = R.drawable.lucky_the_cookie,
                description = stringResource(R.string.lucky_desc),
                githubUrl = "https://lthb.fr"
            )
        }

        item {
            ContributorItem(
                name = "Federico",
                imageRes = R.drawable.federico,
                description = stringResource(R.string.federico_desc),
                githubUrl = "https://github.com/federicobuttafuori"
            )
        }

        // Version name (clickable to access debug / copy)
        item {
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
}

package org.elnix.dragonlauncher.ui.settings.customization

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.elnix90.runtime.asState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.base.model.serializables.Action
import org.elnix.dragonlauncher.base.model.serializables.Profile
import org.elnix.dragonlauncher.base.navigation.NavigationRoute
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.models.DrawerViewModel
import org.elnix.dragonlauncher.models.IconsViewModel
import org.elnix.dragonlauncher.settings.stores.map.IconsSettingsStore
import org.elnix.dragonlauncher.ui.actions.AppIcon
import org.elnix.dragonlauncher.ui.base.activityViewModel
import org.elnix.dragonlauncher.ui.base.components.LazyRowWithScrollIndicator
import org.elnix.dragonlauncher.ui.base.components.Spacer
import org.elnix.dragonlauncher.ui.compositionslocals.LocalNavigator
import org.elnix.dragonlauncher.ui.dragon.components.DragonGroupScope
import org.elnix.dragonlauncher.ui.dragon.components.DragonIconButton
import org.elnix.dragonlauncher.ui.dragon.components.DragonSettingsGroup
import org.elnix.dragonlauncher.ui.dragon.settings.Setting
import org.elnix.dragonlauncher.ui.dragon.text.TextWithDescription
import org.elnix.dragonlauncher.ui.helpers.settings.SettingsScaffold
import org.elnix.dragonlauncher.ui.settings.customization.drawer.DrawerIconShapePicker
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun IconsTab(
    drawerViewModel: DrawerViewModel = activityViewModel(),
    iconsViewModel: IconsViewModel = activityViewModel()
) {
    val ctx = LocalContext.current
    val navigator = LocalNavigator.current
    val scope = rememberCoroutineScope()

    val apps by drawerViewModel.userApps.collectAsState(initial = emptyList())
    val packs by drawerViewModel.getInstalledIconPacks().collectAsState(emptyList())

    val iconSettings by iconsViewModel.iconSettings.collectAsState()
    val selectedPack = iconSettings.iconPack

    SettingsScaffold(
        title = stringResource(NavigationRoute.Icons.resId),
        onBack = {
            iconsViewModel.reloadAllPointsIcons()
            navigator.onBack()
        },
        helpText = stringResource(R.string.icon_pack_help),
        resetText = stringResource(R.string.reset_icon_packs_tab),
        onReset = {
            scope.launch {
                IconsSettingsStore.resetAll(ctx)
            }
        },
        topContent = {
            LazyRowWithScrollIndicator(
                items = apps,
                modifier = Modifier.height(70.dp),
            ) { app ->
                AppIcon(app, size = 56.dp)
            }
        }
    ) {

        // because of the icons in top content
        Spacer(30.dp)

        DragonSettingsGroup(R.string.colors_and_icons) {
            Setting(IconsSettingsStore.useIconTint)

            val useIconTint by IconsSettingsStore.useIconTint.asState()
            Setting(IconsSettingsStore.onlyTintIconPack, enabled = useIconTint) {
                iconsViewModel.reinstallAllIconPacks()
            }
            Setting(IconsSettingsStore.iconsTint, enabled = useIconTint) {
                iconsViewModel.reinstallAllIconPacks()
            }

            Setting(IconsSettingsStore.renderForeground)
            Setting(IconsSettingsStore.renderBackground)

            Setting(IconsSettingsStore.themedIcons)

            val themedIcons by IconsSettingsStore.themedIcons.asState()
            Setting(IconsSettingsStore.forceThemed, enabled = themedIcons)

            Setting(IconsSettingsStore.adaptify)

            DrawerIconShapePicker()
        }



        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Text(
                text = stringResource(R.string.icon_packs_found, packs.size),
                style = MaterialTheme.typography.bodyLargeEmphasized
            )


            var isLoading by remember { mutableStateOf(false) }

            LaunchedEffect(isLoading) {
                delay(2000.milliseconds)
                isLoading = false
            }

            AnimatedContent(isLoading) {
                if (it) {
                    LoadingIndicator()
                } else {
                    DragonIconButton(
                        icon = R.drawable.refresh,
                        contentDescription = R.string.reload
                    ) {
                        isLoading = true
                        iconsViewModel.updateIconPacks()
                    }
                }
            }
        }


        DragonSettingsGroup(R.string.icon_packs) {
            packs.forEach { pack ->
                val packPkg = pack.packageName
                val packAction = Action.LaunchApp(packPkg, Profile.dummy())
                val packApp by drawerViewModel.findOne(packAction).collectAsState(null)

                PackItem(
                    selected = selectedPack == packPkg,
                    text = pack.name,
                    description = pack.packageName,
                    onClick = {
                        scope.launch {
                            IconsSettingsStore.selectedIconPack.set(ctx, pack.packageName)
                        }
                    }
                ) {
                    if (packApp != null) {
                        AppIcon(packApp!!, size = 50.dp)
                    } else {
                        Icon(
                            painter = painterResource(R.drawable.palette),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(56.dp)
                        )
                    }
                }
            }

            PackItem(
                selected = selectedPack.isNullOrEmpty(),
                text = stringResource(R.string.default_text),
                description = stringResource(R.string.use_original_app_icon),
                onClick = {
                    scope.launch {
                        IconsSettingsStore.selectedIconPack.reset(ctx)
                    }
                }
            ) {
                Icon(
                    painter = painterResource(R.drawable.close),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(50.dp)
                )
            }
        }
    }
}


@Composable
private fun DragonGroupScope.PackItem(
    selected: Boolean,
    text: String,
    description: String,
    onClick: () -> Unit,
    icon: @Composable () -> Unit
) {
    Row(
        modifier = Modifier
            .dragonSettingGroup(selected = selected) {
                clickable(onClick = onClick)
            },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        icon()
        Spacer(12.dp)
        TextWithDescription(
            text = text,
            description = description,
            modifier = Modifier.weight(1f)
        )

        val scale by animateFloatAsState(
            targetValue = if (selected) 1f else 0f,
            animationSpec = tween(durationMillis = 300),
            label = "Check Scale Animation"
        )

        Box(
            modifier = Modifier
                .size(40.dp),
            contentAlignment = Alignment.Center
        ) {
            if (scale > 0f) {
                Icon(
                    painter = painterResource(R.drawable.check),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.scale(scale)
                )
            }
        }
    }
}

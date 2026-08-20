package org.elnix.dragonlauncher.ui.helpers

import android.os.Process
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.icons.IconPack
import org.elnix.dragonlauncher.models.DrawerViewModel
import org.elnix.dragonlauncher.models.IconsViewModel
import org.elnix.dragonlauncher.ui.actions.AppIcon
import org.elnix.dragonlauncher.ui.base.activityViewModel
import org.elnix.dragonlauncher.ui.base.components.Spacer
import org.elnix.dragonlauncher.ui.dragon.components.DragonIconButton
import org.elnix.dragonlauncher.ui.dragon.components.DragonSettingsGroup
import org.elnix.dragonlauncher.ui.dragon.text.TextWithDescription
import kotlin.time.Duration.Companion.milliseconds

@Composable
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
fun IconPackListContent(
    drawerViewModel: DrawerViewModel = activityViewModel(),
    iconViewModel: IconsViewModel = activityViewModel(),
    packs: List<IconPack>,
    selectedPackPackage: String?,
    showClearOption: Boolean,
    onPackClick: (IconPack) -> Unit,
    onClearClick: () -> Unit
) {
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(isLoading) {
        delay(2000.milliseconds)
        isLoading = false
    }

    DragonSettingsGroup(R.string.icon_packs) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Text(
                text = stringResource(R.string.icon_packs_found, packs.size),
                style = MaterialTheme.typography.bodyLargeEmphasized
            )

            AnimatedContent(isLoading) {
                if (it) {
                    LoadingIndicator()
                } else {
                    DragonIconButton(
                        icon = R.drawable.refresh,
                        contentDescription = stringResource(R.string.reload)
                    ) {
                        isLoading = true
                        iconViewModel.updateIconPacks()
                    }
                }
            }
        }

        packs.forEach { pack ->

            val packPkg = pack.packageName
            val packApp by drawerViewModel.findOne(packPkg, Process.myUserHandle()).collectAsState(null)

            PackItem(
                selected = selectedPackPackage == packPkg,
                text = pack.name,
                description = pack.packageName,
                onClick = { onPackClick(pack) }
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

        if (showClearOption) {
            PackItem(
                selected = selectedPackPackage.isNullOrEmpty(),
                text = stringResource(R.string.default_text),
                description = stringResource(R.string.use_original_app_icon),
                onClick = onClearClick
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
private fun PackItem(
    selected: Boolean,
    text: String,
    description: String,
    onClick: () -> Unit,
    icon: @Composable () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(10.dp),
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
        ){
            if (scale > 0f) {
                Icon(
                    painter = painterResource(R.drawable.check),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.scale(scale)
                )
            }
        }
    }
}
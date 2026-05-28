package org.elnix.dragonlauncher.ui.helpers

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.common.messyfolder.resolveShape
import org.elnix.dragonlauncher.common.serializables.IconPackInfo
import org.elnix.dragonlauncher.common.serializables.dummyAppModel
import org.elnix.dragonlauncher.models.AppsViewModel
import org.elnix.dragonlauncher.ui.activityViewModel
import org.elnix.dragonlauncher.ui.base.components.Spacer
import org.elnix.dragonlauncher.ui.composition.LocalDrawerIconsCache
import org.elnix.dragonlauncher.ui.composition.LocalIconShape
import org.elnix.dragonlauncher.ui.dragon.components.DragonIconButton
import org.elnix.dragonlauncher.ui.dragon.components.DragonRow
import org.elnix.dragonlauncher.ui.dragon.text.TextWithDescription

@Composable
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
fun IconPackListContent(
    appsViewModel: AppsViewModel = activityViewModel(),
    packs: List<IconPackInfo>,
    selectedPackPackage: String?,
    showClearOption: Boolean,
    onReloadPacks: () -> Unit,
    onPackClick: (IconPackInfo) -> Unit,
    onClearClick: () -> Unit
) {
    val icons = LocalDrawerIconsCache.current
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(isLoading) {
        delay(2000L)
        isLoading = false
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Text(
            text = stringResource(R.string.icon_packs_found, packs.size),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground
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
                    onReloadPacks()
                }
            }
        }
    }

    packs.forEach { pack ->

        DragonRow(
            { onPackClick(pack) }
        ) {
            val packPkg = pack.packageName
            val packCacheKey = dummyAppModel(packPkg).key

            val packIcon = icons.getOrLazyCompute(packCacheKey) {
                appsViewModel.reloadAppIcon(dummyAppModel(packPkg))
            }

            Box(
                Modifier.size(40.dp),
                contentAlignment = Alignment.Center
            ) {
                if (packIcon != null) {
                    Image(
                        bitmap = packIcon,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(LocalIconShape.current.resolveShape()),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        painter = painterResource(R.drawable.palette),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(12.dp)

            TextWithDescription(
                text = pack.name,
                description = pack.packageName,
            )

            Spacer()

            AnimatedVisibility(selectedPackPackage == pack.packageName) {
                Icon(
                    painter = painterResource(R.drawable.check),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }

    if (showClearOption) {
        DragonRow(
            { onClearClick() }
        ) {
            Icon(
                painter = painterResource(R.drawable.close),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            )
            Spacer(12.dp)


            TextWithDescription(
                text = stringResource(R.string.default_text),
                description = stringResource(R.string.use_original_app_icon)
            )

            Spacer()

            AnimatedVisibility(selectedPackPackage == null) {
                Icon(
                    painter = painterResource(R.drawable.check),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

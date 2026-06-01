package org.elnix.dragonlauncher.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.settings.stores.map.PrivateSettingsStore
import org.elnix.dragonlauncher.theme.AppObjectsColors
import org.elnix.dragonlauncher.ui.base.components.Spacer
import org.elnix.dragonlauncher.ui.dragon.components.DragonIconButton


enum class BetaVersionType {
    App, Feature
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BetaVersionWarning(
    betaVersionType: BetaVersionType
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()


    CompositionLocalProvider(
        LocalContentColor provides MaterialTheme.colorScheme.onErrorContainer
    ) {
        Card(
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                color = MaterialTheme.colorScheme.error,
                                shape = MaterialShapes.Arrow.toShape()
                            )
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.warning),
                            contentDescription = stringResource(R.string.warning)
                        )
                    }

                    Text(
                        text = stringResource(R.string.warning),
                        style = MaterialTheme.typography.titleMediumEmphasized
                    )

                    Spacer()

                    if (betaVersionType == BetaVersionType.App) {
                        DragonIconButton(
                            onClick = {
                                scope.launch {
                                    PrivateSettingsStore.hideBetaVersionWarning.set(ctx, true)
                                }
                            },
                            icon = R.drawable.close,
                            contentDescription = stringResource(R.string.close),
                            colors = AppObjectsColors.cancelIconButtonColors()
                        )
                    }
                }


                val warningText = stringResource(
                    when (betaVersionType) {
                        BetaVersionType.App -> R.string.this_is_a_beta_version
                        BetaVersionType.Feature -> R.string.this_feature_is_in_beta
                    }
                )

                Text(
                    text = warningText,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 12.sp
                )
            }
        }
    }
}
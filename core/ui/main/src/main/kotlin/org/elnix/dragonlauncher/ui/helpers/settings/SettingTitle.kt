package org.elnix.dragonlauncher.ui.helpers.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.elnix.dragonlauncher.common.R
import org.elnix.dragonlauncher.ui.base.components.AnimatedFab
import org.elnix.dragonlauncher.ui.components.burger.BurgerListAction
import org.elnix.dragonlauncher.ui.components.burger.MoreOptions
import org.elnix.dragonlauncher.ui.dialogs.rememberInteractionSource
import org.elnix.dragonlauncher.ui.dragon.components.DragonIconButton


@Composable
fun SettingsTitle(
    title: String,
    vararg otherIcons: Triple<(() -> Unit), Int, String>,
    resetIcon: (() -> Unit)?,
    helpIcon: () -> Unit,
    onBack: () -> Unit
) {
    val interactionSource = rememberInteractionSource()

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 20.dp)
            .clickable(
                indication = null,
                interactionSource = interactionSource,
                onClick = onBack
            )
    ) {

        AnimatedFab(
            onClick = onBack,
            interactionSource = interactionSource,
            icon = R.drawable.back
        )

        Text(
            text = title,
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.weight(1f)
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            otherIcons.forEach {
                DragonIconButton(
                    icon = it.second,
                    contentDescription = it.third
                ) { it.first() }
            }

            if (resetIcon != null) {
                DragonIconButton(
                    icon = R.drawable.reset,
                    contentDescription = stringResource(R.string.reset)
                ) { resetIcon() }
            }

            DragonIconButton(
                onClick = helpIcon,
                icon = R.drawable.help,
                contentDescription = stringResource(R.string.help)
            )
        }
    }
}


@Composable
fun SpecialSettingsTitle(
    showSubNestSizeSlider: Boolean,
    onSettings: () -> Unit,
    onEditDefaultPoint: () -> Unit,
    onToggleSubNestsSlider: () -> Unit,
    onReloadPoints: () -> Unit,
    onEditNest: () -> Unit,
    onResetPoints: () -> Unit,
    onBack: () -> Unit
) {
    val interactionSources = List(2) { rememberInteractionSource() }

    var showBurgerMenu by remember { mutableStateOf(false) }
    val dismiss = { showBurgerMenu = false }

    Row {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 26.dp, vertical = 20.dp)
                .clickable(
                    indication = null,
                    interactionSource = interactionSources[0],
                    onClick = onBack
                )
        ) {

            AnimatedFab(
                onClick = onBack,
                interactionSource = interactionSources[0],
                icon = R.drawable.back
            )

            Text(
                text = stringResource(R.string.points_settings),
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier
                    .weight(1f)
                    .align(Alignment.CenterVertically)
            )

        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(horizontal = 10.dp, vertical = 20.dp)
                .clickable(
                    indication = null,
                    interactionSource = interactionSources[1],
                    onClick = onSettings
                )
        ) {
            Box {
                DragonIconButton(
                    icon = R.drawable.more_vert,
                    contentDescription = stringResource(R.string.open_burger_menu)
                ) { showBurgerMenu = true }

                BurgerListAction(
                    isExpanded = showBurgerMenu,
                    onDismissRequest = dismiss,
                    actions = listOf(
                        MoreOptions(
                            text = { stringResource(R.string.edit_default_point_settings) },
                            icon = R.drawable.edit_rounded,
                            onClick = {
                                dismiss()
                                onEditDefaultPoint()
                            }
                        ),
                        MoreOptions(
                            text = { stringResource(R.string.reload_point_icons) },
                            icon = R.drawable.refresh,
                            onClick = {
                                dismiss()
                                onReloadPoints()
                            }
                        ),
                        MoreOptions(
                            text = { stringResource(R.string.edit_nest) },
                            icon = R.drawable.edit_nest,
                            onClick = {
                                dismiss()
                                onEditNest()
                            }
                        ),
                        MoreOptions(
                            text = { stringResource(R.string.show_sub_nest_size_slider) },
                            icon = if (showSubNestSizeSlider) R.drawable.toggle_on else R.drawable.toggle_off,
                            onClick = {
                                dismiss()
                                onToggleSubNestsSlider()
                            }
                        ),
                        MoreOptions(
                            text = { stringResource(R.string.reset_all_points) },
                            icon = R.drawable.delete_forever,
                            onClick = {
                                dismiss()
                                onResetPoints()
                            }
                        )
                    )
                )
            }

            AnimatedFab(
                onClick = onSettings,
                interactionSource = interactionSources[1],
                icon = R.drawable.settings
            )
        }
    }
}
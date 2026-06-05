package org.elnix.dragonlauncher.ui.helpers.settings

import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
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
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.models.PointViewModel
import org.elnix.dragonlauncher.models.utils.asState
import org.elnix.dragonlauncher.ui.base.activityViewModel
import org.elnix.dragonlauncher.ui.base.components.AnimatedFab
import org.elnix.dragonlauncher.ui.components.burger.BurgerListAction
import org.elnix.dragonlauncher.ui.components.burger.MoreOptions
import org.elnix.dragonlauncher.ui.dialogs.rememberInteractionSource
import org.elnix.dragonlauncher.ui.dragon.components.DragonIconButton

@Composable
private fun SettingsTitleInternal(
    title: String,
    onBack: () -> Unit,
    specialContent: @Composable RowScope.() -> Unit
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
            modifier = Modifier
                .weight(1f)
                .basicMarquee(iterations = 2)
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            specialContent()
        }
    }
}

@Composable
fun SettingsTitle(
    title: String,
    vararg otherIcons: Triple<(() -> Unit), Int, String>,
    resetIcon: (() -> Unit)?,
    helpIcon: () -> Unit,
    onBack: () -> Unit
) {
    SettingsTitleInternal(
        title = title,
        onBack = onBack
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


@Composable
fun SpecialSettingsTitle(
    pointViewModel: PointViewModel = activityViewModel(),
    onSettings: () -> Unit,
    onEditDefaultPoint: () -> Unit,
    onReloadPoints: () -> Unit,
    onEditNest: () -> Unit,
    onResetPoints: () -> Unit,
    onBack: () -> Unit
) {
    val interactionSource = rememberInteractionSource()

    var showBurgerMenu by remember { mutableStateOf(false) }
    val dismiss = { showBurgerMenu = false }

    val showSubNestSlider by pointViewModel.showSubNestSlider.asState()
    val showAdvancedPointTools by pointViewModel.showAdvancedPointTools.asState()

    SettingsTitleInternal(
        title = stringResource(R.string.points_settings),
        onBack = onBack
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
                        text = { stringResource(R.string.reset_all_points) },
                        icon = R.drawable.delete_forever,
                        onClick = {
                            dismiss()
                            onResetPoints()
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
                        text = { stringResource(R.string.show_sub_nest_size_slider) },
                        icon = if (showSubNestSlider) R.drawable.toggle_on else R.drawable.toggle_off,
                        onClick = {
                            pointViewModel.showSubNestSlider.set(!showSubNestSlider)
                            dismiss()
                        }
                    ),
                    MoreOptions(
                        text = { stringResource(R.string.show_advanced_edit_tools) },
                        icon = if (showAdvancedPointTools) R.drawable.toggle_on else R.drawable.toggle_off,
                        onClick = {
                            pointViewModel.showAdvancedPointTools.set(!showAdvancedPointTools)
                            dismiss()
                        }
                    ),
                    MoreOptions(
                        text = { stringResource(R.string.edit_default_point_settings) },
                        icon = R.drawable.edit_rounded,
                        onClick = {
                            dismiss()
                            onEditDefaultPoint()
                        }
                    ),
                    MoreOptions(
                        text = { stringResource(R.string.edit_nest) },
                        icon = R.drawable.nest_icon,
                        onClick = {
                            dismiss()
                            onEditNest()
                        }
                    )
                )
            )
        }

        AnimatedFab(
            onClick = onSettings,
            interactionSource = interactionSource,
            icon = R.drawable.settings
        )
    }
}

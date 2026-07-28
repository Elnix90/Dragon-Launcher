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
import org.elnix.dragonlauncher.ui.base.components.AnimatedFab
import org.elnix.dragonlauncher.ui.base.remember.rememberInteractionSource
import org.elnix.dragonlauncher.ui.components.burger.BurgerListAction
import org.elnix.dragonlauncher.ui.components.burger.MoreOptions
import org.elnix.dragonlauncher.ui.dragon.components.DragonIconButton
import org.elnix.dragonlauncher.ui.dragon.components.ResetIcon

@Composable
private fun SettingsTitleInternal(
    title: String,
    onBack: () -> Unit,
    moreOptions: ((() -> Unit) -> List<MoreOptions>)?,
    specialContent: @Composable RowScope.() -> Unit
) {
    val interactionSource = rememberInteractionSource()
    var showBurgerMenu by remember { mutableStateOf(false) }
    val dismiss = { showBurgerMenu = false }

    val actions = moreOptions?.invoke(dismiss)

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

        actions?.let {
            Box {
                DragonIconButton(
                    icon = R.drawable.more_vert,
                    contentDescription = stringResource(R.string.open_burger_menu)
                ) { showBurgerMenu = true }

                BurgerListAction(
                    isExpanded = showBurgerMenu,
                    onDismissRequest = dismiss,
                    actions = it
                )
            }
        }

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
    moreOptions: ((() -> Unit) -> List<MoreOptions>)? = null,
    resetIcon: (() -> Unit)?,
    helpIcon: () -> Unit,
    onBack: () -> Unit
) {

    SettingsTitleInternal(
        title = title,
        moreOptions = moreOptions,
        onBack = onBack
    ) {
        if (resetIcon != null) {
            ResetIcon(onReset = resetIcon)
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
    onSettings: () -> Unit,
    onShowMoreSheet: () -> Unit,
    onBack: () -> Unit
) {
    SettingsTitleInternal(
        title = stringResource(R.string.points_settings),
        onBack = onBack,
        moreOptions = null
    ) {
        AnimatedFab(
            onClick = onShowMoreSheet,
            icon = R.drawable.more_horiz
        )

        AnimatedFab(
            onClick = onSettings,
            icon = R.drawable.settings
        )
    }
}

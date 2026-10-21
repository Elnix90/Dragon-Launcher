package org.elnix.dragonlauncher.ui.dragon.components

import android.annotation.SuppressLint
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.elnix.dragonlauncher.ktx.semiTransparentIfDisabled
import org.elnix.dragonlauncher.ui.base.modifiers.conditional
import org.elnix.dragonlauncher.ui.dragon.text.SettingsWithTitle

class DragonGroupScope
	internal constructor(
		columnScope: ColumnScope
	) : ColumnScope by columnScope { // OMG I DISCOVERED THIS SYNTAX TODAY AND ITS WAY TOO COOOOL

		@SuppressLint("UnnecessaryComposedModifier")
		fun Modifier.dragonSettingGroup(
			enabled: Boolean = true,
			selected: Boolean = false,
			padding: Boolean = true,
			clickModifier: (Modifier.() -> Modifier)? = null
		): Modifier =
			composed {
				val animatedBgColor by animateColorAsState(
					if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh
				)

				this
					.fillMaxWidth()
					.clip(MaterialTheme.shapes.extraSmall)
					.background(animatedBgColor.semiTransparentIfDisabled(enabled))
					.conditional(clickModifier) { it() }
					.conditional(padding) {
						padding(10.dp)
					}
			}

		@SuppressLint("UnnecessaryComposedModifier")
		fun Modifier.dragonSettingGroupSpecialOuter(): Modifier =
			composed {
				this
					.fillMaxWidth()
					.clip(MaterialTheme.shapes.extraSmall)
			}

		@SuppressLint("UnnecessaryComposedModifier")
		fun Modifier.dragonSettingGroupSpecialInner(
			isFirstHalf: Boolean,
			enabled: Boolean = true,
			selected: Boolean = false,
			clickModifier: (Modifier.() -> Modifier)? = null
		): Modifier =
			composed {
				val animatedBgColor by animateColorAsState(
					if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh
				)

				this
					.conditional(isFirstHalf) {
						weight(1f)
					}.background(animatedBgColor.semiTransparentIfDisabled(enabled))
					.conditional(clickModifier) { it() }
					.padding(10.dp)
			}
	}

@Composable
fun DragonSettingsGroup(
	@StringRes
	title: Int?,
	modifier: Modifier = Modifier,
	@DrawableRes
	icon: Int? = null,
	trailingIcon: (@Composable RowScope.() -> Unit)? = null,
	content: @Composable DragonGroupScope.() -> Unit
) {
	DragonSettingsGroup(
		title = title?.let { stringResource(title) },
		modifier = modifier,
		icon = icon,
		trailingIcon = trailingIcon,
		content = content
	)
}

@Composable
fun DragonSettingsGroup(
	title: String? = null,
	@DrawableRes
	icon: Int? = null,
	@SuppressLint("ModifierParameter")
	modifier: Modifier = Modifier,
	trailingIcon: (@Composable RowScope.() -> Unit)? = null,
	content: @Composable DragonGroupScope.() -> Unit
) {
	CompositionLocalProvider(
		LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant
	) {
		SettingsWithTitle(title, modifier, icon, trailingIcon) {
			Column(
				horizontalAlignment = Alignment.CenterHorizontally,
				verticalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
				modifier =
					Modifier
						.fillMaxWidth()
						.clip(MaterialTheme.shapes.largeIncreased)
			) {
				val dragonGroupScope =
					remember {
						DragonGroupScope(this)
					}
				content(dragonGroupScope)
			}
		}
	}
}

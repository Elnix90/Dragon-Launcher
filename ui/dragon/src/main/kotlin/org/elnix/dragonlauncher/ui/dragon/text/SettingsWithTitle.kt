package org.elnix.dragonlauncher.ui.dragon.text

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import org.elnix.dragonlauncher.ui.base.components.Spacer

@Composable
fun SettingsWithTitle(
	title: String?,
	modifier: Modifier = Modifier,
	@DrawableRes
	icon: Int? = null,
	trailingIcon: (@Composable RowScope.() -> Unit)? = null,
	content: @Composable () -> Unit
) {
	Column(modifier = modifier) {
		if (title != null) {
			Row(
				modifier =
					Modifier
						.padding(start = 10.dp, end = 16.dp, top = 5.dp, bottom = 2.dp)
						.fillMaxWidth(),
				verticalAlignment = Alignment.CenterVertically
			) {
				if (icon != null) {
					Icon(
						painter = painterResource(icon),
						contentDescription = title,
						tint = MaterialTheme.colorScheme.secondary
					)
					Spacer(12.dp)
				}

				Text(
					text = title,
					color = MaterialTheme.colorScheme.secondary,
					style = MaterialTheme.typography.titleMediumEmphasized
				)

				Spacer()

				if (trailingIcon != null) {
					trailingIcon()
				}
			}
		}
		content()
	}
}

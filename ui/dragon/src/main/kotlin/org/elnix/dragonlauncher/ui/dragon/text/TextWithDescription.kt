package org.elnix.dragonlauncher.ui.dragon.text

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.elnix.dragonlauncher.ui.base.modifiers.semiTransparentIfDisabled

@Composable
fun TextWithDescription(
	text: String,
	description: String?,
	modifier: Modifier = Modifier,
	enabled: Boolean = true
) {
	Column(
		modifier = modifier.semiTransparentIfDisabled(enabled),
		verticalArrangement = Arrangement.spacedBy(5.dp)
	) {
		Text(
			text = text,
			style = MaterialTheme.typography.titleMediumEmphasized
		)
		if (description != null) {
			Text(
				text = description,
				style = MaterialTheme.typography.bodySmall
			)
		}
	}
}

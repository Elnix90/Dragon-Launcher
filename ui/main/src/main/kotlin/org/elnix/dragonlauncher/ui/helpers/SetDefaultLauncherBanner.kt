package org.elnix.dragonlauncher.ui.helpers

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import io.github.elnix90.shared.ui.dragon.components.DragonIconButton
import io.github.elnix90.shared.ui.dragon.components.DragonRow
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.permissions.PermissionGroup
import org.elnix.dragonlauncher.permissions.permissionsManager

@Composable
fun SetDefaultLauncherBanner(onHide: () -> Unit) {
	val ctx = LocalContext.current

	DragonRow(
		onClick = { ctx.permissionsManager.requestPermission(PermissionGroup.DefaultLauncher) }
	) {
		Text(
			stringResource(R.string.set_default_launcher),
			color = MaterialTheme.colorScheme.onPrimary,
			modifier = Modifier.weight(1f)
		)

		DragonIconButton(
			icon = R.drawable.close,
			contentDescription = R.string.close,
			onClick = onHide
		)
	}
}

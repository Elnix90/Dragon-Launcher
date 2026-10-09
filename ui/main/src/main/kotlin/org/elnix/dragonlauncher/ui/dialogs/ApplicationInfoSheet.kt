package org.elnix.dragonlauncher.ui.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import org.elnix.dragonlauncher.base.model.models.Application
import org.elnix.dragonlauncher.base.utils.CopyPasteUtils.copyToClipboard
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.ktx.getInstallSource
import org.elnix.dragonlauncher.ui.base.components.HorizontalScrollIndicator
import org.elnix.dragonlauncher.ui.dragon.components.DragonGroupScope
import org.elnix.dragonlauncher.ui.dragon.components.DragonModalBottomSheet
import org.elnix.dragonlauncher.ui.dragon.components.DragonSettingsGroup

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApplicationInfoSheet(
	app: Application,
	onDismiss: () -> Unit
) {
	val ctx = LocalContext.current
	val installerPackage = remember { ctx.getInstallSource(app.packageName) }

	DragonModalBottomSheet(
		onDismissRequest = onDismiss,
		skipPartiallyExpanded = true
	) {
		DragonSettingsGroup {
			Text2(stringResource(R.string.app_info_name, app.label)) { ctx.copyToClipboard(app.label) }
			Text2(stringResource(R.string.app_info_package_name, app.packageName)) { ctx.copyToClipboard(app.packageName) }
			Text2(stringResource(R.string.profile, app.profile.toString()))
			Text2(stringResource(R.string.app_info_is_system, app.isSystem.toString()))
			Text2(stringResource(R.string.app_info_is_work_profile, app.isWork.toString()))
			Text2(stringResource(R.string.app_info_is_private_profile, app.isPrivate.toString()))
			Text2(stringResource(R.string.app_info_is_launchable, app.isLaunchable.toString()))
			Text2(stringResource(R.string.app_info_cache_key, app.key.cacheKey)) { ctx.copyToClipboard(app.key.cacheKey) }
			Text2(stringResource(R.string.app_info_installer_package, installerPackage.installingPackageName ?: "<unknown>"))
			Text2(stringResource(R.string.app_info_installer_package, installerPackage.initiatingPackageName ?: "<unknown>"))
			Text2(stringResource(R.string.app_info_installer_package, installerPackage.originatingPackageName ?: "<unknown>"))
		}
	}
}

@Composable
private fun DragonGroupScope.Text2(
	text: String,
	onClick: (() -> Unit)? = null
) {
	val scrollState = rememberScrollState()
	Box {
		Text(
			text = text,
			fontFamily = FontFamily.Monospace,
			maxLines = 1,
			modifier =
				with(this@Text2) {
					Modifier
						.dragonSettingGroup {
							clickable {
								onClick?.invoke()
							}
						}.horizontalScroll(scrollState)
				}
		)

		HorizontalScrollIndicator(scrollState.canScrollForward)
	}
}

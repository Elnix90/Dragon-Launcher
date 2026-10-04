package org.elnix.dragonlauncher.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.elnix.dragonlauncher.base.utils.CopyPasteUtils.copyToClipboard
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.models.PointsViewModel
import org.elnix.dragonlauncher.points.DecodeError
import org.elnix.dragonlauncher.settings.backupableStores
import org.elnix.dragonlauncher.ui.base.activityViewModel
import org.elnix.dragonlauncher.ui.base.components.Spacer
import org.elnix.dragonlauncher.ui.components.BaseVersionChip
import org.elnix.dragonlauncher.ui.composition.LocalUseCustomColorChannels
import org.elnix.dragonlauncher.ui.dragon.components.DragonButton
import org.elnix.dragonlauncher.ui.dragon.components.DragonGroupScope
import org.elnix.dragonlauncher.ui.dragon.components.DragonIconButton
import org.elnix.dragonlauncher.ui.dragon.components.DragonModalBottomSheet
import org.elnix.dragonlauncher.ui.dragon.components.DragonSettingsGroup
import org.elnix.dragonlauncher.ui.dragon.expandable.ExpandableSection
import org.elnix.dragonlauncher.ui.dragon.expandable.rememberExpandableSection
import org.elnix.dragonlauncher.ui.remembers.rememberSafeSettingsExportLauncher

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DecodingErrorSheet(
	onDismissRequest: () -> Unit,
	errors: List<DecodeError>,
	pointsViewModel: PointsViewModel = activityViewModel()
) {
	val ctx = LocalContext.current
	val settingsExportLauncher = rememberSafeSettingsExportLauncher(backupableStores)

	DragonModalBottomSheet(onDismissRequest) {
		Row(
			modifier = Modifier.fillMaxWidth(),
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.spacedBy(15.dp)
		) {
			Box(
				contentAlignment = Alignment.Center,
				modifier =
					Modifier
						.size(48.dp)
						.background(
							color = MaterialTheme.colorScheme.errorContainer,
							shape = MaterialShapes.Pill.toShape()
						)
			) {
				Icon(
					painter = painterResource(R.drawable.warning),
					contentDescription = null,
					tint = MaterialTheme.colorScheme.onErrorContainer
				)
			}

			Text(
				text = stringResource(R.string.decode_error),
				style = MaterialTheme.typography.titleLargeEmphasized,
				color = MaterialTheme.colorScheme.error
			)
		}

		Spacer(15.dp)
		Text(
			text = stringResource(R.string.decode_error_desc),
			style = MaterialTheme.typography.labelSmall
		)
		Spacer(5.dp)

		Text(
			text = stringResource(R.string.decode_error_share_to_dev),
			style = MaterialTheme.typography.bodyMediumEmphasized
		)

		Spacer(8.dp)

		DragonSettingsGroup {
			DragonButton(
				onClick = {
					ctx.copyToClipboard(errors.toString(), 0)
				}
			) {
				Text(stringResource(R.string.share_logs))
			}

			DragonButton(
				onClick = {
					settingsExportLauncher.launch("decoding_error.json")
				}
			) {
				Text(stringResource(R.string.export_settings))
			}

			val state = rememberExpandableSection(
				R.string.logs,
				description = null,
				icon = R.drawable.bug_report
			)
			ExpandableSection(state) {
				errors.forEach {
					DecodeError(it)
				}
			}

			DragonButton(
				onClick = {
					pointsViewModel.pointsService.allDecodeSuccessful.reset()
				},
				isCancel = true
			) {
				Text(stringResource(R.string.decode_error_allow_anyway))
			}
		}
	}
}

@Composable
private fun DragonGroupScope.DecodeError(error: DecodeError) {
	val ctx = LocalContext.current
	Column(
		modifier = Modifier.dragonSettingGroup()
	) {
		Row(
			modifier = Modifier.fillMaxWidth(),
			horizontalArrangement = Arrangement.SpaceBetween,
			verticalAlignment = Alignment.CenterVertically
		) {
			BaseVersionChip(
				text = error.type,
				color = MaterialTheme.colorScheme.primary
			)
			DragonIconButton(
				icon = R.drawable.copy,
				contentDescription = R.string.copy
			) {
				ctx.copyToClipboard(error.toString())
			}
		}
		Spacer(3.dp)

		val message = error.exception.message ?: "Unknown exception"
		Text(
			text = message,
			style = MaterialTheme.typography.labelSmallEmphasized,
			fontFamily = FontFamily.Monospace
		)
	}
}

@Preview
@Composable
private fun DecodeErrorPreview() {
	CompositionLocalProvider(
		LocalUseCustomColorChannels provides true
	) {
		DragonSettingsGroup {
			DecodeError(
				DecodeError(
					type = "Test",
					exception = IllegalStateException(
						"Error test with a long reason i don't know wat I'm typing but I'm typing stuff to make the exception really long, and I'm still typing, it has cross the max length limit I set I think I'll stop there"
					),
					json = "empty json because why not"
				)
			)
		}
	}
}

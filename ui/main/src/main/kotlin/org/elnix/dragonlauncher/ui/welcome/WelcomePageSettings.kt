package org.elnix.dragonlauncher.ui.welcome

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import io.github.elnix90.runtime.asState
import io.github.elnix90.runtime.asStateNull
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.base.utils.rememberIsDefaultLauncher
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.ktx.openDefaultLauncherSettings
import org.elnix.dragonlauncher.settings.stores.map.BackupSettingsStore
import org.elnix.dragonlauncher.ui.base.components.Spacer
import org.elnix.dragonlauncher.ui.dragon.components.DragonButton
import org.elnix.dragonlauncher.ui.dragon.components.DragonSettingsGroup
import org.elnix.dragonlauncher.ui.dragon.settings.Setting
import org.elnix.dragonlauncher.ui.remembers.rememberAutoBackupLauncher
import org.elnix.dragonlauncher.ui.wellbeing.FloatingParticles

@Composable
fun WelcomePageSettings(
	onEnterSettings: () -> Unit
) {
	val ctx = LocalContext.current

	Box {
		FloatingParticles()

		WelcomePagerHeader {
			androidx.compose.foundation.layout
				.Spacer(Modifier.weight(1f))

			DragonSettingsGroup(
				title = R.string.set_default_launcher,
				icon = R.drawable.rocket_launch
			) {
				val isDefaultLauncher by rememberIsDefaultLauncher()

				DragonButton(
					enabled = !isDefaultLauncher,
					onClick = { ctx.openDefaultLauncherSettings() }
				) {
					Text(
						text = if (isDefaultLauncher) {
							stringResource(R.string.already_default_launcher)
						} else {
							stringResource(R.string.open_default_launcher_settings)
						}
					)
				}
			}

			Spacer(60.dp)

			DragonSettingsGroup(
				title = R.string.backup,
				icon = R.drawable.cloud_upload
			) {
				val scope = rememberCoroutineScope()

				Setting(BackupSettingsStore.autoBackupEnabled) {
					// If the user disabled the backup, also remove the uri
					if (!it) {
						scope.launch {
							BackupSettingsStore.autoBackupUri.reset(ctx)
						}
					}
				}

				val autoBackupEnabled by BackupSettingsStore.autoBackupEnabled.asState()
				val autoBackupUriString by BackupSettingsStore.autoBackupUri.asStateNull()
				val autoBackupUri = autoBackupUriString?.toUri()

				val autoBackupLauncher = rememberAutoBackupLauncher()

				DragonButton(
					enabled = autoBackupEnabled,
					onClick = {
						autoBackupLauncher.launch("dragonlauncher-auto-backup.json")
					}
				) {
					Text(
						text =
							if (autoBackupUri != null) {
								stringResource(R.string.choose_a_auto_backup_file)
							} else {
								stringResource(R.string.open_default_launcher_settings)
							}
					)
				}
			}

			Spacer(60.dp)
			DragonSettingsGroup(
				title = R.string.start_your_journey,
				icon = R.drawable.check
			) {
				DragonButton(
					onClick = onEnterSettings
				) {
					Text(stringResource(R.string.customize_apps))
				}
			}

			androidx.compose.foundation.layout
				.Spacer(Modifier.weight(1f))

			Text(
				text = stringResource(R.string.swipe_up_to_start_using_directly),
				color = MaterialTheme.colorScheme.onBackground.copy(0.5f),
				textDecoration = TextDecoration.Underline,
				textAlign = TextAlign.Center
			)
		}
	}
}

package org.elnix.dragonlauncher.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.elnix90.shared.ui.base.asState
import io.github.elnix90.shared.ui.base.components.Spacer
import io.github.elnix90.shared.ui.dragon.components.DragonModalBottomSheet
import io.github.elnix90.shared.ui.dragon.components.DragonSettingsGroup
import io.github.elnix90.shared.ui.dragon.text.DialogTitle
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.models.BackupViewModel
import org.elnix.dragonlauncher.ui.base.activityViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BackupResultSheet(
	backupViewModel: BackupViewModel = activityViewModel()
) {
	val backupService = backupViewModel.backupService
	val result by backupService.result.asState()

	result?.let { result ->
		val isError = result.error
		val isExport = result.export
		val errorMessage = result.message

		val message = when {
			isError -> errorMessage.ifBlank { stringResource(R.string.unknown_error) }
			isExport -> stringResource(R.string.export_successful)
			else -> null
		}

		DragonModalBottomSheet(
			onDismissRequest = { backupService.result.value = null }
		) {
			val titleBgColor = if (isError) MaterialTheme.colorScheme.errorContainer else Color(0xFF167E16)

			Row(
				horizontalArrangement = Arrangement.spacedBy(8.dp),
				verticalAlignment = Alignment.CenterVertically
			) {
				Icon(
					painter = painterResource(if (isError) R.drawable.warning else R.drawable.check),
					contentDescription = message,
					tint = if (isError) MaterialTheme.colorScheme.onErrorContainer else Color.Green,
					modifier = Modifier
						.size(40.dp)
						.background(titleBgColor, MaterialShapes.Clover8Leaf.toShape())
						.padding(10.dp)
				)
				DialogTitle(result.title)
			}

			Spacer(12.dp)

			DragonSettingsGroup {
				SelectionContainer(
					modifier = Modifier
						.dragonSettingGroup()
						.heightIn(min = 300.dp, max = 800.dp)
						.verticalScroll(rememberScrollState())
				) {
					Text(message ?: "")
				}
			}
		}
	}
}

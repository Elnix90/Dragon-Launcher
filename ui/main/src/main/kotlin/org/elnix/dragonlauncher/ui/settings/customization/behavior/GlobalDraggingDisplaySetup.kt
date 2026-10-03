package org.elnix.dragonlauncher.ui.settings.customization.behavior

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.models.PointsViewModel
import org.elnix.dragonlauncher.ui.base.activityViewModel
import org.elnix.dragonlauncher.ui.base.asState
import org.elnix.dragonlauncher.ui.compositionslocals.LocalNavigator
import org.elnix.dragonlauncher.ui.helpers.settings.SettingsScaffold
import org.elnix.dragonlauncher.ui.helpers.swipe.NestOverlay

@Composable
fun GlobalDraggingModeSetup(
	viewModel: GlobalDraggingModeSetupViewModel,
	pointsViewModel: PointsViewModel = activityViewModel()
) {
	val navigator = LocalNavigator.current
	val offset by viewModel.offset.asState()

	val nestId by pointsViewModel.nestsNavigationService.currentNestId.collectAsState()
	val currentNest = pointsViewModel.pointsService.findNestById(nestId)

	SettingsScaffold(
		title = stringResource(R.string.global_dragging_mode_setup),
		helpText = stringResource(R.string.global_dragging_mode_setup_help),
		resetText = stringResource(R.string.global_dragging_mode_setup_reset),
		onReset = null,
		scrollableContent = false,
		onBack = {
			viewModel.save()
			navigator.onBack()
		}
	) {
		Box(
			modifier = Modifier
				.fillMaxSize()
				.pointerInput(Unit) {
					with(viewModel) { draggingGesture() }
				}
		) {
			NestOverlay(
				nest = currentNest,
				center = offset,
				eraseColor = MaterialTheme.colorScheme.background,
				allowShowPointCenter = false,
				pointSettingsDisplay = true,
				showCancelZone = false,
				hideShapes = false,
				skipSelected = false
			)
		}
	}
}

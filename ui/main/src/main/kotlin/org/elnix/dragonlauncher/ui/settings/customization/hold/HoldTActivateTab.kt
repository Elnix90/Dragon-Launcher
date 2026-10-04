package org.elnix.dragonlauncher.ui.settings.customization.hold

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.elnix.dragonlauncher.base.model.enumsui.toggle.HoldActions
import org.elnix.dragonlauncher.base.model.serializables.CustomGlow
import org.elnix.dragonlauncher.base.model.serializables.CustomObject
import org.elnix.dragonlauncher.base.model.serializables.CustomObject.Companion.CustomObjectBlockProperties
import org.elnix.dragonlauncher.base.model.serializables.CustomObject.Companion.defaultHoldCustomObject
import org.elnix.dragonlauncher.base.model.serializables.IconShape
import org.elnix.dragonlauncher.base.model.serializables.serializers.ColorSerializer
import org.elnix.dragonlauncher.base.model.serializables.serializers.DpSerializer
import org.elnix.dragonlauncher.base.theme.LocalExtraColors
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.ktx.getCenter
import org.elnix.dragonlauncher.ktx.round
import org.elnix.dragonlauncher.ktx.toDp
import org.elnix.dragonlauncher.ktx.toHexWithAlpha
import org.elnix.dragonlauncher.models.SwipeViewModel
import org.elnix.dragonlauncher.settings.stores.map.ColorSettingsStore
import org.elnix.dragonlauncher.settings.stores.map.HoldToActivateArcSettingsStore
import org.elnix.dragonlauncher.ui.base.activityViewModel
import org.elnix.dragonlauncher.ui.base.asState
import org.elnix.dragonlauncher.ui.base.components.Spacer
import org.elnix.dragonlauncher.ui.components.Preset
import org.elnix.dragonlauncher.ui.components.PresetRow
import org.elnix.dragonlauncher.ui.components.VerticalDragZone
import org.elnix.dragonlauncher.ui.compositionslocals.LocalNavigator
import org.elnix.dragonlauncher.ui.dialogs.HoldSettingsOrderSheet
import org.elnix.dragonlauncher.ui.dragon.components.DragonButton
import org.elnix.dragonlauncher.ui.dragon.components.DragonSettingsGroup
import org.elnix.dragonlauncher.ui.dragon.components.SliderWithLabel
import org.elnix.dragonlauncher.ui.dragon.generic.MultiSelectConnectedButtonRow
import org.elnix.dragonlauncher.ui.dragon.settings.Setting
import org.elnix.dragonlauncher.ui.helpers.HoldToActivateArc
import org.elnix.dragonlauncher.ui.helpers.customobjects.EditCustomObjectBlock
import org.elnix.dragonlauncher.ui.helpers.settings.SettingsItem
import org.elnix.dragonlauncher.ui.helpers.settings.SettingsScaffold
import kotlin.time.Duration.Companion.milliseconds

@Stable
@Serializable
private data class HoldPreset(
	override val name: String,
	val customObject: CustomObject = defaultHoldCustomObject,
	val holdDelayBeforeStartingLongClickSettings: Int? = null,
	val longCLickSettingsDuration: Int? = null,
	@Serializable(with = DpSerializer::class)
	val holdToActivateSettingsTolerance: Dp? = null,
	val showToleranceOnMainScreen: Boolean? = null,
	val rotationsPerSecond: Float? = null,
	val holdRgbLoading: Boolean? = null,
	val pulsingRadius: Float? = null,
	val pulsingDuration: Int? = null,
	@Serializable(with = ColorSerializer::class)
	val color: Color? = null
) : Preset {
	override fun toString(): String =
		"HoldPreset(\n" +
			"    name = \"$name\",\n" +
			"    customObject = $customObject,\n" +
			"    holdDelayBeforeStartingLongClickSettings = $holdDelayBeforeStartingLongClickSettings,\n" +
			"    longCLickSettingsDuration = $longCLickSettingsDuration,\n" +
			"    holdToActivateSettingsTolerance = ${holdToActivateSettingsTolerance?.value?.round(2)},\n" +
			"    showToleranceOnMainScreen = $showToleranceOnMainScreen,\n" +
			"    rotationsPerSecond = ${rotationsPerSecond?.round(2)}f,\n" +
			"    holdRgbLoading = $holdRgbLoading,\n" +
			"    pulsingRadius = ${pulsingRadius?.round(2)}f,\n" +
			"    pulsingRDuration = $pulsingDuration\n" +
			"    color = ${color?.let { "Color(0x${color.toHexWithAlpha.replace("#", "")}" }})\n" +
			")"
}

@Composable
fun HoldToActivateTab(
	viewModel: HoldToActivateTabViewModel,
	swipeViewModel: SwipeViewModel = activityViewModel()
) {
	val ctx = LocalContext.current
	val extraColors = LocalExtraColors.current
	val navigator = LocalNavigator.current

	val scope = rememberCoroutineScope()

	val swipeService = swipeViewModel.swipeService
	val holdObject by swipeService.holdObject.asState()

	val rotationsPerSecond by swipeService.rotationsPerSecond.collectAsState()
	val rgbLoading by swipeService.holdRgbLoading.collectAsState()
	val holdToActivateSettingsTolerance by swipeService.holdToActivateSettingsTolerance.collectAsState()
	val showToleranceOnMainScreen by swipeService.showToleranceOnMainScreen.collectAsState()
	val pulsingRadius by swipeService.pulsingRadius.collectAsState()
	val pulsingDuration by swipeService.pulsingDuration.collectAsState()
	val holdDelayBeforeStartingLongClickSettings by swipeService.holdDelayBeforeStartingLongClickSettings.collectAsState()
	val longCLickSettingsDuration by swipeService.longCLickSettingsDuration.collectAsState()

	val holdAnchor by swipeService.holdAnchor.asState()
	val holdProgress by swipeService.holdProgress

	var showHoldSettingsOrderDialog by viewModel.showHoldSettingsOrderDialog
	var playAnimation by viewModel.playAnimation
	var manualMode by viewModel.manualMode

	val progress = viewModel.progress

	SettingsScaffold(
		title = stringResource(R.string.hold_settings),
		onBack = {
			swipeService.saveHoldObject()
			navigator.onBack()
		},
		helpText = stringResource(R.string.hold_settings_help),
		resetText = stringResource(R.string.reset_hold_tab),
		onReset = {
			scope.launch {
				HoldToActivateArcSettingsStore.resetAll(ctx)
				swipeService.resetHoldObject()
			}
		},
		topContent = {
			Row(
				verticalAlignment = Alignment.CenterVertically,
				horizontalArrangement = Arrangement.Center,
				modifier = Modifier.fillMaxWidth()
			) {
				MultiSelectConnectedButtonRow(
					entries = HoldActions.entries,
					checked = {
						when (it) {
							HoldActions.ManualMode -> manualMode
							HoldActions.PlayPause -> playAnimation
						}
					}
				) {
					when (it) {
						HoldActions.ManualMode -> {
							manualMode = !manualMode
						}

						HoldActions.PlayPause -> {
							playAnimation = !playAnimation
							manualMode = false
						}
					}
				}

				Spacer(5.dp)

				DragonSettingsGroup {
					SliderWithLabel(
						label = stringResource(R.string.animated_progress),
						value = progress.value,
						valueRange = 0f..1f,
						resetEnabled = progress.value != 0f,
						onReset = {
							scope.launch {
								progress.snapTo(0f)
							}
						}
					) {
						scope.launch {
							progress.animateTo(it)
						}
					}
				}
			}

			Column {
				var height by viewModel.height
				var isFirstPositioning by viewModel.isFirstPositioning

				BoxWithConstraints(
					modifier =
						Modifier
							.fillMaxWidth()
							.height(height.toDp)
							.onGloballyPositioned { layoutCoordinates ->
								if (isFirstPositioning) {
									height = layoutCoordinates.size.width
									isFirstPositioning = false
								}
							}.pointerInput(Unit) {
								with(swipeService) {
									holdGesture(scope.coroutineContext)
								}
							}
				) {
					val center =
						if (!manualMode) {
							this.constraints.getCenter()
						} else {
							holdAnchor
						}

					val progress =
						if (!manualMode) {
							progress.value
						} else {
							holdProgress
						}

					HoldToActivateArc(
						center = center,
						progress = progress,
						customObject = holdObject,
						playAnimation = playAnimation,
						swipeService = swipeService
					)
				}

				VerticalDragZone { height += it.toInt() }
			}
		}
	) {
		LaunchedEffect(
			holdDelayBeforeStartingLongClickSettings,
			longCLickSettingsDuration,
			playAnimation,
			manualMode
		) {
			if (!manualMode) {
				while (playAnimation) {
					progress.snapTo(0f)
					delay(holdDelayBeforeStartingLongClickSettings.milliseconds)

					progress.animateTo(
						targetValue = 1f,
						animationSpec =
							tween(
								durationMillis = longCLickSettingsDuration,
								easing = LinearEasing
							)
					)
				}
			}
		}

		PresetRow(
			presets = listOf(
				HoldPreset("Default"),
				HoldPreset(
					name = "Elnix",
					customObject = CustomObject(
						stroke = 4.5.dp,
						color = null,
						glow = CustomGlow(radius = 12.dp, color = null),
						shape = IconShape.Random,
						size = 75.0.dp,
						rotation = -1,
						mirror = false,
						eraseBackground = false,
						alignsWithDragAngle = false
					),
					holdDelayBeforeStartingLongClickSettings = 300,
					longCLickSettingsDuration = 500,
					holdToActivateSettingsTolerance = 10.0.dp,
					showToleranceOnMainScreen = false,
					rotationsPerSecond = 0.50f,
					holdRgbLoading = false,
					pulsingRadius = 2.0f,
					pulsingDuration = 500,
					color = Color(0xFFB902FF)
				),
				HoldPreset(
					name = "Yo",
					customObject = CustomObject(
						stroke = 4.5.dp,
						color = null,
						glow = CustomGlow(radius = 12.dp, color = null),
						shape = IconShape.Random,
						size = 75.0.dp,
						rotation = -1,
						mirror = false,
						eraseBackground = false,
						alignsWithDragAngle = false
					),
					holdDelayBeforeStartingLongClickSettings = 300,
					longCLickSettingsDuration = 500,
					holdToActivateSettingsTolerance = 10.0.dp,
					showToleranceOnMainScreen = false,
					rotationsPerSecond = 0.50f,
					holdRgbLoading = false,
					pulsingRadius = 2.0f,
					pulsingDuration = 500,
					color = Color(0xFFB902FF)
				)
			),
			get = {
				HoldPreset(
					name = "new",
					customObject = swipeService.holdObject.value,
					holdDelayBeforeStartingLongClickSettings = holdDelayBeforeStartingLongClickSettings,
					longCLickSettingsDuration = longCLickSettingsDuration,
					holdToActivateSettingsTolerance = holdToActivateSettingsTolerance,
					showToleranceOnMainScreen = showToleranceOnMainScreen,
					rotationsPerSecond = rotationsPerSecond,
					holdRgbLoading = rgbLoading,
					pulsingRadius = pulsingRadius,
					pulsingDuration = pulsingDuration,
					color = extraColors.holdToActivate
				)
			},
			set = { preset ->
				scope.launch {
					swipeService.holdObject.value = preset.customObject
					HoldToActivateArcSettingsStore.holdDelayBeforeStartingLongClickSettings.set(
						ctx,
						preset.holdDelayBeforeStartingLongClickSettings
					)
					HoldToActivateArcSettingsStore.longCLickSettingsDuration.set(ctx, preset.longCLickSettingsDuration)
					HoldToActivateArcSettingsStore.holdToActivateSettingsTolerance.set(ctx, preset.holdToActivateSettingsTolerance)
					HoldToActivateArcSettingsStore.showToleranceOnMainScreen.set(ctx, preset.showToleranceOnMainScreen)
					HoldToActivateArcSettingsStore.rotationsPerSecond.set(ctx, preset.rotationsPerSecond)
					HoldToActivateArcSettingsStore.holdRgbLoading.set(ctx, preset.holdRgbLoading)
					HoldToActivateArcSettingsStore.pulsingRadius.set(ctx, preset.pulsingRadius)
					HoldToActivateArcSettingsStore.pulsingRDuration.set(ctx, preset.pulsingDuration)
				}
			}
		)

		EditCustomObjectBlock(
			title = R.string.object_properties,
			editObject = holdObject,
			default = defaultHoldCustomObject,
			properties =
				CustomObjectBlockProperties(
					allowAlignCustomization = false,
					allowEraseBackgroundCustomization = false
				)
		) { swipeService.holdObject.value = it }

		DragonSettingsGroup(R.string.configuration) {
			Setting(HoldToActivateArcSettingsStore.longCLickSettingsDuration)
			Setting(HoldToActivateArcSettingsStore.holdDelayBeforeStartingLongClickSettings)
			Setting(HoldToActivateArcSettingsStore.rotationsPerSecond)
			DragonButton(
				onClick = {
					scope.launch {
						/**
						 * The number of rotations to achieve the same speed in both sides of the shape when playing (works best with circle)
						 */
						val magicNumber = 1000f / longCLickSettingsDuration
						HoldToActivateArcSettingsStore.rotationsPerSecond.set(ctx, magicNumber)
					}
				}
			) {
				Icon(
					painter = painterResource(R.drawable.flash_auto),
					contentDescription = null
				)
				Spacer(5.dp)
				Text(
					text = stringResource(R.string.automatic_magic_number),
					style = MaterialTheme.typography.labelMediumEmphasized
				)
			}

			SettingsItem(
				title = stringResource(R.string.edit_hold_to_activate_elements),
				description = stringResource(R.string.edit_hold_to_activate_elements_desc),
				icon = R.drawable.edit_rounded
			) { showHoldSettingsOrderDialog = true }
			Setting(HoldToActivateArcSettingsStore.holdToActivateSettingsTolerance)
			Setting(HoldToActivateArcSettingsStore.showToleranceOnMainScreen)
			Setting(HoldToActivateArcSettingsStore.pulsingRadius)
			Setting(HoldToActivateArcSettingsStore.pulsingRDuration)
			Setting(HoldToActivateArcSettingsStore.holdRgbLoading)
			Setting(ColorSettingsStore.holdToActivateColor)
		}
	}

	if (showHoldSettingsOrderDialog) {
		HoldSettingsOrderSheet { showHoldSettingsOrderDialog = false }
	}
}

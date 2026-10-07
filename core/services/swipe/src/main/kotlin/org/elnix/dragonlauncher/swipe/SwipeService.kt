package org.elnix.dragonlauncher.swipe

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Process
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.runtime.State
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.changedToDown
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.core.net.toUri
import io.github.elnix90.logging.logD
import io.github.elnix90.logging.logE
import io.github.elnix90.logging.logI
import io.github.elnix90.logging.logW
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.ANGLE_LINE_TAG
import org.elnix.dragonlauncher.SWIPE_TAG
import org.elnix.dragonlauncher.TAG
import org.elnix.dragonlauncher.applaunch.AppLaunchService
import org.elnix.dragonlauncher.applications.AppRepository
import org.elnix.dragonlauncher.base.Constants.Settings.DOUBLE_CLICK_ACTION_DELAY
import org.elnix.dragonlauncher.base.Constants.Settings.MAX_ITEMS_ALLOWED
import org.elnix.dragonlauncher.base.SettingFlow
import org.elnix.dragonlauncher.base.model.DragonJson
import org.elnix.dragonlauncher.base.model.json
import org.elnix.dragonlauncher.base.model.models.AngleLineObjects
import org.elnix.dragonlauncher.base.model.serializables.Action
import org.elnix.dragonlauncher.base.model.serializables.CustomObject
import org.elnix.dragonlauncher.base.model.serializables.CustomObject.Companion.defaultAngleCustomObject
import org.elnix.dragonlauncher.base.model.serializables.CustomObject.Companion.defaultEndCustomObject
import org.elnix.dragonlauncher.base.model.serializables.CustomObject.Companion.defaultHoldCustomObject
import org.elnix.dragonlauncher.base.model.serializables.CustomObject.Companion.defaultLineCustomObject
import org.elnix.dragonlauncher.base.model.serializables.CustomObject.Companion.defaultStartCustomObject
import org.elnix.dragonlauncher.base.model.serializables.GlobalDraggingMode
import org.elnix.dragonlauncher.base.model.serializables.GlobalDraggingMode.Companion.GlobalDraggingModeJson
import org.elnix.dragonlauncher.base.model.serializables.MainScreenLayer
import org.elnix.dragonlauncher.base.model.serializables.MainScreenLayer.Companion.MainScreenLayerJson
import org.elnix.dragonlauncher.base.model.serializables.MainScreenLayer.Companion.defaultMainScreenLayers
import org.elnix.dragonlauncher.base.navigation.NavigationRoute
import org.elnix.dragonlauncher.base.utils.ConnectivityUtils.getMobileDataStatus
import org.elnix.dragonlauncher.base.utils.ConnectivityUtils.isBluetoothEnabled
import org.elnix.dragonlauncher.base.utils.ConnectivityUtils.isWifiEnabled
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.ktx.expandQuickActionsDrawer
import org.elnix.dragonlauncher.ktx.hasUriReadPermission
import org.elnix.dragonlauncher.ktx.isNotBlankJson
import org.elnix.dragonlauncher.ktx.showToast
import org.elnix.dragonlauncher.lifecycle.LifecycleService
import org.elnix.dragonlauncher.points.NestsNavigationService
import org.elnix.dragonlauncher.points.PointsService
import org.elnix.dragonlauncher.services.SystemControl
import org.elnix.dragonlauncher.settings.stores.array.MainScreenLayersSettingsStore
import org.elnix.dragonlauncher.settings.stores.map.AngleLineSettingsStore
import org.elnix.dragonlauncher.settings.stores.map.BehaviorSettingsStore
import org.elnix.dragonlauncher.settings.stores.map.DebugSettingsStore
import org.elnix.dragonlauncher.settings.stores.map.DrawerSettingsStore
import org.elnix.dragonlauncher.settings.stores.map.HoldToActivateArcSettingsStore
import org.elnix.dragonlauncher.settings.stores.objects.AngleObjectSettingStore
import org.elnix.dragonlauncher.settings.stores.objects.EndObjectSettingStore
import org.elnix.dragonlauncher.settings.stores.objects.HoldToActivateObject
import org.elnix.dragonlauncher.settings.stores.objects.LineObjectSettingStore
import org.elnix.dragonlauncher.settings.stores.objects.StartObjectSettingStore
import org.elnix.dragonlauncher.shizuku.ShizukuService
import org.elnix.dragonlauncher.widgets.WidgetsService
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration.Companion.milliseconds

public interface SwipeService {
	public val globalDraggingMode: SettingFlow<GlobalDraggingMode>

	public val lineObject: SettingFlow<CustomObject>
	public val angleObject: SettingFlow<CustomObject>
	public val startObject: SettingFlow<CustomObject>
	public val endObject: SettingFlow<CustomObject>

	public val holdObject: SettingFlow<CustomObject>
	public val holdMenuEntriesString: SettingFlow<List<NavigationRoute>>

	public val lineObjectOrder: SettingFlow<List<AngleLineObjects>>
	public val mainScreenLayerOrder: SettingFlow<List<MainScreenLayer>>

	public val start: SettingFlow<Offset?>
	public val current: SettingFlow<Offset?>
	public val fixedOffset: SettingFlow<Offset?>

	/**
	 * Clears the dragging values after a successful launch
	 */
	public fun clearAfterLaunch()

	public suspend fun PointerInputScope.mainDragGesture()

	/**
	 * This function NEEDS this [context] parameter, otherwise it crashes and complains that it want a [androidx.compose.runtime.MonotonicFrameClock] in the coroutine context.
	 * The [context] is passed by compose and got using `rememberCoroutintScope()`
	 */
	public suspend fun PointerInputScope.holdGesture(context: CoroutineContext)

	public fun loadAllFromDisk()

	public fun saveLineObjects()

	public fun resetLineObjects()

	public fun saveHoldObject()

	public fun resetHoldObject()

	public fun saveAngleLineOrder()

	public fun resetAngleLineOrder()

	public fun saveMainScreenLayers()

	public fun resetMainScreenLayers()

	public fun setHoldMenuEntries(newEntries: List<NavigationRoute>)

	public fun saveHoldMenuEntries()

	public fun resetHoldMenuEntries()

	public fun saveGlobalDraggingMode()

	public fun resetGlobalDraggingMode()

	public fun setFixedOffset(mode: GlobalDraggingMode.Fixed)

	public val holdAnchor: SettingFlow<Offset?>
	public val holdProgress: State<Float>
	public val showDropDownMenuSettings: SettingFlow<Boolean>

	public val showFilePicker: SettingFlow<Action.OpenFile?>

	public fun dismissFilePicker()

	public fun onFilePicked(oldAction: Action.OpenFile, newAction: Action.OpenFile)

	public val navigatorChannel: Flow<NavigationRoute>
	public val showShizukuCommandPrompter: SettingFlow<Action.RunAdbCommand?>

	public fun dismissCommandPrompt()

	public fun launchAction(action: Action)

	public fun setting(route: NavigationRoute)

	public fun dismissMenu()

	public val longCLickSettingsDuration: StateFlow<Int>
	public val holdToActivateSettingsTolerance: StateFlow<Dp>
	public val holdDelayBeforeStartingLongClickSettings: StateFlow<Int>
	public val rotationsPerSecond: StateFlow<Float>
	public val holdRgbLoading: StateFlow<Boolean>
	public val showToleranceOnMainScreen: StateFlow<Boolean>
	public val pulsingRadius: StateFlow<Float>
	public val pulsingDuration: StateFlow<Int>
}

internal class SwipeServiceImpl(
	private val ctx: Context,
	private val nestsNavigationService: NestsNavigationService,
	private val widgetsService: WidgetsService,
	private val pointsService: PointsService,
	private val appLaunchService: AppLaunchService,
	private val appRepository: AppRepository,
	private val shizukuService: ShizukuService,
	private val lifecycleService: LifecycleService
) : SwipeService {
	private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

	override val globalDraggingMode: SettingFlow<GlobalDraggingMode> = SettingFlow(GlobalDraggingMode.default)
	override val lineObject: SettingFlow<CustomObject> = SettingFlow(defaultLineCustomObject)
	override val angleObject: SettingFlow<CustomObject> = SettingFlow(defaultAngleCustomObject)
	override val startObject: SettingFlow<CustomObject> = SettingFlow(defaultStartCustomObject)
	override val endObject: SettingFlow<CustomObject> = SettingFlow(defaultEndCustomObject)

	override val holdObject: SettingFlow<CustomObject> = SettingFlow(defaultHoldCustomObject)
	override val holdMenuEntriesString: SettingFlow<List<NavigationRoute>> = SettingFlow(emptyList())

	override val lineObjectOrder: SettingFlow<List<AngleLineObjects>> = SettingFlow(AngleLineObjects.entries)
	override val mainScreenLayerOrder: SettingFlow<List<MainScreenLayer>> = SettingFlow(defaultMainScreenLayers)
	override val showFilePicker: SettingFlow<Action.OpenFile?> = SettingFlow(null)

	override fun dismissFilePicker() {
		showFilePicker.value = null
	}

	override fun onFilePicked(oldAction: Action.OpenFile, newAction: Action.OpenFile) {
		val points = pointsService.points.value.values.filter {
			it.action == oldAction
		}

		points.forEach { point ->
			pointsService.editPoint(point.id) {
				it.copy(action = newAction)
			}
		}
	}

	override val start: SettingFlow<Offset?> = SettingFlow(null)
	override val current: SettingFlow<Offset?> = SettingFlow(null)
	override val fixedOffset: SettingFlow<Offset?> = SettingFlow(null)
	private var lastClickTime = 0L

	override fun setFixedOffset(mode: GlobalDraggingMode.Fixed) {
		val screenWidth = ctx.resources.displayMetrics.widthPixels
		val screenHeight = ctx.resources.displayMetrics.heightPixels

		val offset = Offset(
			x = screenWidth * mode.xRatio,
			y = screenHeight * mode.yRatio
		)

		fixedOffset.value = offset
	}

	override fun clearAfterLaunch() {
		start.value = null
		current.value = null
		pointsService.deselectAll()
	}

	private val doubleClickAction = BehaviorSettingsStore.doubleClickAction.stateFlow(ctx, scope)
	private val leftPadding = BehaviorSettingsStore.leftPadding.stateFlow(ctx, scope)
	private val rightPadding = BehaviorSettingsStore.rightPadding.stateFlow(ctx, scope)
	private val topPadding = BehaviorSettingsStore.topPadding.stateFlow(ctx, scope)
	private val bottomPadding = BehaviorSettingsStore.bottomPadding.stateFlow(ctx, scope)

	override suspend fun PointerInputScope.mainDragGesture() {
		awaitPointerEventScope {
			while (true) {
				val event = awaitPointerEvent(PointerEventPass.Initial)

				val down = event.changes.firstOrNull { it.changedToDown() } ?: continue
				val pos = down.position

				if (
					!pos.isInsideActiveZone(
						size = size,
						left = leftPadding.value,
						right = rightPadding.value,
						top = topPadding.value,
						bottom = bottomPadding.value
					)
				) {
					logD(SWIPE_TAG) { "Not inside active zone, skipping..." }
					continue
				}

				if (pos.isInsideForegroundWidget()) {
					// Let widget handle scroll - do NOT consume or process
					logD(SWIPE_TAG) { "Inside active widget, skipping..." }
					continue
				}

				start.value = down.position
				current.value = down.position

				val pointerId = down.id

				val currentTime = System.currentTimeMillis()
				val diff = currentTime - lastClickTime
				if (diff < DOUBLE_CLICK_ACTION_DELAY) {
					launchAction(doubleClickAction.value)
					continue
				}

				lastClickTime = currentTime

				while (true) {
					val event = awaitPointerEvent(PointerEventPass.Initial)
					val change = event.changes.firstOrNull { it.id == pointerId }

					if (change == null) {
						logW(SWIPE_TAG) { "Change is null, wtf" }
						clearAfterLaunch()
						break
					}

					// if another gesture consumed the ... gesture
					if (current.value == null) {
						logI(SWIPE_TAG) { "Another action consumed the launch" }
						clearAfterLaunch()
						break
					}

					if (change.pressed) {
						change.consume()
						current.value = change.position
					} else {
						pointsService.selectedPointsIds.value.firstOrNull()?.let { selectedPointId ->
							pointsService.findPointById(selectedPointId)?.let { point ->
								launchAction(point.action)
							}
						}
						clearAfterLaunch()
						break
					}
				}
			}
		}
	}

	private fun resetProgress() {
		scope.launch {
			_holdProgress.snapTo(0f)
		}
	}

	override suspend fun PointerInputScope.holdGesture(context: CoroutineContext) {
		awaitEachGesture {
			val down = awaitFirstDown()
			holdAnchor.value = down.position

			val holdJob =
				scope.launch(context) {
					_holdProgress.snapTo(0f)

					delay(holdDelayBeforeStartingLongClickSettings.value.milliseconds)

					_holdProgress.animateTo(
						targetValue = 1f,
						animationSpec =
							tween(
								durationMillis = longCLickSettingsDuration.value,
								easing = LinearEasing
							)
					)

					// When the code reaches here, it means the hold to activate is firing
					// First, resets all the dragging gestures in progress to cancel any drag

					resetProgress()
					clearAfterLaunch()

					val routes = holdMenuEntriesString.value
					val size = routes.size

					// When the list only has 1 element, directly go to that screen, otherwise, open the menu
					when {
						size > 1 -> {
							showDropDownMenuSettings.value = true
							current.value = null
							start.value = null
						}

						else -> {
							// To prevent any out-of-settings lock, when the list has anything different than more than one point
							val action = Action.OpenDragonLauncherSettings(NavigationRoute.PointsSettings)
							launchAction(action)
						}
					}
				}

			while (true) {
				val event = awaitPointerEvent()
				val change = event.changes.firstOrNull { it.id == down.id }

				if (change == null || !change.pressed) {
					holdJob.cancel()
					resetProgress()
					break
				}

				/*
				 * Check if the finger hadn't moved further that the allowed range.
				 * If it's the case, it'll cancel the gesture
				 */
				val dist =
					holdAnchor.value?.let {
						(change.position - it).getDistance()
					} ?: Float.MAX_VALUE

				if (dist > holdToActivateSettingsTolerance.value.toPx()) {
					holdJob.cancel()
					resetProgress()
					break
				}

				change.consume()
			}
		}
	}

	private val useAccessibilityInsteadOfContextToExpandActionPanel =
		DebugSettingsStore.useAccessibilityInsteadOfContextToExpandActionPanel.stateFlow(ctx, scope)

	private val openRootNestEachTime =
		BehaviorSettingsStore.openRootNestEachTime.stateFlow(ctx, scope)

	override val showShizukuCommandPrompter: SettingFlow<Action.RunAdbCommand?> = SettingFlow(null)

	override fun dismissCommandPrompt() {
		showShizukuCommandPrompter.value = null
	}

	private val _navigatorChannel = Channel<NavigationRoute>(Channel.CONFLATED)
	override val navigatorChannel: Flow<NavigationRoute> = _navigatorChannel.receiveAsFlow()

	private fun onShizukuCommand(command: Action.RunAdbCommand) {
		if (command.command.trim().isEmpty()) {
			showShizukuCommandPrompter.value = command
		} else {
			shizukuService.runShisukuCommandNotEmpty(command)
		}
	}

	override fun launchAction(action: Action) {
		clearAfterLaunch()

		lastClickTime = 0L
		showDropDownMenuSettings.value = false
		lifecycleService.blockHomeActionsTemporarily()

		if (openRootNestEachTime.value) {
			nestsNavigationService.clearStack()
		}

		when (action) {
			is Action.LaunchApp -> {
				appLaunchService.requestAppLaunch(action)
			}

			is Action.LaunchShortcut -> {
				appLaunchService.launchShortcut(action)
			}

			is Action.OpenUrl -> {
				val i = Intent(Intent.ACTION_VIEW, action.url.toUri())
				ctx.startActivity(i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
			}

			Action.NotificationShade -> {
				if (!SystemControl.isServiceEnabled(ctx)) {
					ctx.showToast(ctx.getString(R.string.please_enable_accessibility_services_to_use_that_feature))
					SystemControl.openServiceSettings(ctx)
					return
				}
				SystemControl.expandNotifications()
			}

			Action.ControlPanel -> {
				if (useAccessibilityInsteadOfContextToExpandActionPanel.value) {
					SystemControl.expandQuickSettings(
						ctx
					)
				} else {
					ctx.expandQuickActionsDrawer()
				}
			}

			is Action.OpenAppDrawer -> {
				val workspaceId = action.workspaceId
				if (workspaceId != null) {
					scope.launch {
						DrawerSettingsStore.lastWorkspaceUsed.set(ctx, workspaceId)
					}
				}
				_navigatorChannel.trySend(NavigationRoute.Drawer)
			}

			is Action.OpenDragonLauncherSettings -> {
				_navigatorChannel.trySend(action.route)
			}

			Action.Lock -> {
				if (!SystemControl.isServiceEnabled(ctx)) {
					ctx.showToast("Please enable accessibility settings to use that feature")
					SystemControl.openServiceSettings(ctx)
					return
				}
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
					SystemControl.lockScreen(ctx)
				} else {
					ctx.showToast(ctx.getString(R.string.not_supported_in_this_android_version))
				}
			}

			is Action.OpenFile -> {
				try {
					val uri = action.uri.toUri()

					if (!ctx.hasUriReadPermission(uri)) {
						ctx.showToast("Please reselect the file to allow access")
						showFilePicker.value = action
						return
					}

					val intent =
						Intent(Intent.ACTION_VIEW).apply {
							setDataAndType(uri, action.mimeType ?: "*/*")
							addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
							addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
						}

					if (intent.resolveActivity(ctx.packageManager) != null) {
						ctx.startActivity(intent)
					} else {
						ctx.showToast("No app available to open this file")
					}
				} catch (e: Exception) {
					ctx.showToast("Unable to open file")
					logE(TAG, e) { "Unable to open file" }
				}
			}

			Action.ReloadApps -> {
				scope.launch {
					appRepository.refreshApps()
				}
			}

			Action.OpenRecentApps -> {
				if (!SystemControl.isServiceEnabled(ctx)) {
					ctx.showToast("Please enable accessibility settings to use that feature")
					SystemControl.openServiceSettings(ctx)
					return
				}
				SystemControl.openRecentApps(ctx)
			}

			is Action.RunAdbCommand -> {
				onShizukuCommand(action)
			}

			is Action.ToggleBluetooth -> {
				onShizukuCommand(
					Action.RunAdbCommand(
						command =
							if (ctx.isBluetoothEnabled()) {
								action.command.commandDisable
							} else {
								action.command.commandEnable
							},
						toast = action.toast == true
					)
				)
			}

			is Action.ToggleData -> {
				onShizukuCommand(
					Action.RunAdbCommand(
						command =
							if (ctx.getMobileDataStatus().first) {
								action.command.commandDisable
							} else {
								action.command.commandEnable
							},
						toast = action.toast == true
					)
				)
			}

			is Action.ToggleWifi -> {
				onShizukuCommand(
					Action.RunAdbCommand(
						command =
							if (ctx.isWifiEnabled()) {
								action.command.commandDisable
							} else {
								action.command.commandEnable
							},
						toast = action.toast == true
					)
				)
			}

			Action.KillLauncher -> {
				Process.killProcess(Process.myPid())
			}

			Action.GoParentNest -> {
				nestsNavigationService.goBack()
				clearAfterLaunch()
			}

			is Action.OpenNest -> {
				nestsNavigationService.goToNest(action.nestId)
				clearAfterLaunch()
			}

			is Action.OpenWidget, Action.None -> {
				// Handled by the main screen / settings
				// The widget action isn't meant to be part of the choosable actions, so nothing on launch
				// None do nothing, pretty straightforward
			}
		}
	}

	override fun setting(route: NavigationRoute) {
		showDropDownMenuSettings.value = false
		launchAction(Action.OpenDragonLauncherSettings(route))
	}

	override val holdAnchor: SettingFlow<Offset?> = SettingFlow(null)
	override val showDropDownMenuSettings: SettingFlow<Boolean> = SettingFlow(false)

	override fun dismissMenu() {
		holdAnchor.value = null
		showDropDownMenuSettings.value = false
	}

	private val _holdProgress: Animatable<Float, AnimationVector1D> = Animatable(0f)
	override val holdProgress: State<Float> = _holdProgress.asState()

	override val longCLickSettingsDuration: StateFlow<Int> = HoldToActivateArcSettingsStore.longCLickSettingsDuration.stateFlow(ctx, scope)
	override val holdToActivateSettingsTolerance: StateFlow<Dp> = HoldToActivateArcSettingsStore.holdToActivateSettingsTolerance.stateFlow(ctx, scope)
	override val holdDelayBeforeStartingLongClickSettings: StateFlow<Int> =
		HoldToActivateArcSettingsStore.holdDelayBeforeStartingLongClickSettings.stateFlow(ctx, scope)
	override val rotationsPerSecond: StateFlow<Float> = HoldToActivateArcSettingsStore.rotationsPerSecond.stateFlow(ctx, scope)
	override val holdRgbLoading: StateFlow<Boolean> = HoldToActivateArcSettingsStore.holdRgbLoading.stateFlow(ctx, scope)
	override val showToleranceOnMainScreen: StateFlow<Boolean> = HoldToActivateArcSettingsStore.showToleranceOnMainScreen.stateFlow(ctx, scope)
	override val pulsingRadius: StateFlow<Float> = HoldToActivateArcSettingsStore.pulsingRadius.stateFlow(ctx, scope)
	override val pulsingDuration: StateFlow<Int> = HoldToActivateArcSettingsStore.pulsingRDuration.stateFlow(ctx, scope)

	override fun loadAllFromDisk() {
		loadMainScreenLayers()
		loadAngleLineObjects()
		loadHoldObject()
		loadAngleLineOrder()
		loadHoldMenuEntries()
		loadGlobalDraggingMode()
	}

	private fun loadAngleLineObjects() {
		scope.launch {
			val lineJsonString = LineObjectSettingStore.jsonSetting.get(ctx)
			lineObject.value = loadCustomObject(lineJsonString, defaultLineCustomObject)

			val angleJsonString = AngleObjectSettingStore.jsonSetting.get(ctx)
			angleObject.value = loadCustomObject(angleJsonString, defaultAngleCustomObject)

			val startJsonString = StartObjectSettingStore.jsonSetting.get(ctx)
			startObject.value = loadCustomObject(startJsonString, defaultStartCustomObject)

			val endJsonString = EndObjectSettingStore.jsonSetting.get(ctx)
			endObject.value = loadCustomObject(endJsonString, defaultEndCustomObject)
		}
	}

	override fun saveLineObjects() {
		scope.launch {
			val lineJsonString = json.encodeToString(lineObject.value)
			LineObjectSettingStore.jsonSetting.set(ctx, lineJsonString)

			val angleJsonString = json.encodeToString(angleObject.value)
			AngleObjectSettingStore.jsonSetting.set(ctx, angleJsonString)

			val startJsonString = json.encodeToString(startObject.value)
			StartObjectSettingStore.jsonSetting.set(ctx, startJsonString)

			val endJsonString = json.encodeToString(endObject.value)
			EndObjectSettingStore.jsonSetting.set(ctx, endJsonString)
		}
	}

	override fun resetLineObjects() {
		scope.launch {
			lineObject.value = defaultLineCustomObject
			LineObjectSettingStore.jsonSetting.reset(ctx)

			angleObject.value = defaultAngleCustomObject
			AngleObjectSettingStore.jsonSetting.reset(ctx)

			startObject.value = defaultStartCustomObject
			StartObjectSettingStore.jsonSetting.reset(ctx)

			endObject.value = defaultEndCustomObject
			EndObjectSettingStore.jsonSetting.reset(ctx)
		}
	}

	private fun loadHoldObject() {
		scope.launch {
			val holdJsonString = HoldToActivateObject.jsonSetting.get(ctx)
			holdObject.value = loadCustomObject(holdJsonString, defaultHoldCustomObject)
		}
	}

	override fun saveHoldObject() {
		scope.launch {
			val holdJsonString = json.encodeToString(holdObject.value)
			HoldToActivateObject.jsonSetting.set(ctx, holdJsonString)
		}
	}

	override fun resetHoldObject() {
		scope.launch {
			holdObject.value = defaultHoldCustomObject
			HoldToActivateObject.jsonSetting.reset(ctx)
		}
	}

	private fun loadAngleLineOrder() {
		scope.launch {
			val orderString = AngleLineSettingsStore.angleLineObjectsOrder.get(ctx)

			lineObjectOrder.value = try {
				orderString
					.takeIf { it.isNotEmpty() }
					?.split(",")
					?.map { AngleLineObjects.valueOf(it) }
			} catch (e: Exception) {
				logE(ANGLE_LINE_TAG, e) { "Failed to decode angle line objects order, using default value" }
				null
			} ?: AngleLineObjects.entries
		}
	}

	override fun saveAngleLineOrder() {
		scope.launch {
			val orderString = lineObjectOrder.value.joinToString(",")
			AngleLineSettingsStore.angleLineObjectsOrder.set(ctx, orderString)
		}
	}

	override fun resetAngleLineOrder() {
		scope.launch {
			lineObjectOrder.value = AngleLineObjects.entries
			AngleLineSettingsStore.angleLineObjectsOrder.reset(ctx)
		}
	}

	private fun loadMainScreenLayers() {
		scope.launch {
			val mainScreenLayerString = MainScreenLayersSettingsStore.jsonSetting.getOrNull(ctx)
			mainScreenLayerOrder.value = mainScreenLayerString
				?.let { MainScreenLayerJson.decode<List<MainScreenLayer>>(it) }
				?.takeIf { layers ->
					val expectedTypes =
						setOf(
							MainScreenLayer.ChargingAnimation::class,
							MainScreenLayer.StatusBar::class,
							MainScreenLayer.Widgets::class,
							MainScreenLayer.CustomDim::class,
							MainScreenLayer.DragOverlay::class,
							MainScreenLayer.HoldToActivate::class
						)

					layers.map { it::class }.toSet() == expectedTypes
				}
				?: defaultMainScreenLayers
		}
	}

	override fun saveMainScreenLayers() {
		scope.launch {
			val mainScreenLayersString = MainScreenLayerJson.encode(mainScreenLayerOrder.value)
			MainScreenLayersSettingsStore.jsonSetting.set(ctx, mainScreenLayersString)
		}
	}

	override fun resetMainScreenLayers() {
		scope.launch {
			mainScreenLayerOrder.value = defaultMainScreenLayers
			MainScreenLayersSettingsStore.jsonSetting.reset(ctx)
		}
	}

	override fun setHoldMenuEntries(newEntries: List<NavigationRoute>) {
		val edited = newEntries
			.take(MAX_ITEMS_ALLOWED)
			.toMutableList()
			.apply {
				if (!this.any { it is NavigationRoute.PointsSettings }) {
					add(0, NavigationRoute.PointsSettings)
				}
			}
		holdMenuEntriesString.value = edited
	}

	private fun loadHoldMenuEntries() {
		scope.launch {
			val holdMenuString = HoldToActivateArcSettingsStore.holdMenuEntriesJson.get(ctx)
			val decoded = HoldMenuEntriesJson.decode<List<NavigationRoute>>(holdMenuString, emptyList())
			setHoldMenuEntries(decoded)
		}
	}

	override fun saveHoldMenuEntries() {
		scope.launch {
			val encoded = HoldMenuEntriesJson.encode<List<NavigationRoute>>(holdMenuEntriesString.value)
			HoldToActivateArcSettingsStore.holdMenuEntriesJson.set(ctx, encoded)
		}
	}

	override fun resetHoldMenuEntries() {
		scope.launch {
			holdMenuEntriesString.value = emptyList()
			HoldToActivateArcSettingsStore.holdMenuEntriesJson.reset(ctx)
		}
	}

	private fun loadGlobalDraggingMode() {
		scope.launch {
			val globalDraggingModeString = BehaviorSettingsStore.globalDraggingMode.get(ctx)
			val decoded = GlobalDraggingModeJson.decode<GlobalDraggingMode>(globalDraggingModeString, GlobalDraggingMode.default)
			globalDraggingMode.value = decoded

			if (decoded is GlobalDraggingMode.Fixed) {
				setFixedOffset(decoded)
			}
		}
	}

	override fun saveGlobalDraggingMode() {
		scope.launch {
			val encoded = GlobalDraggingModeJson.encode(globalDraggingMode.value)
			BehaviorSettingsStore.globalDraggingMode.set(ctx, encoded)
		}
	}

	override fun resetGlobalDraggingMode() {
		scope.launch {
			globalDraggingMode.value = GlobalDraggingMode.default
			BehaviorSettingsStore.globalDraggingMode.reset(ctx)
		}
	}

	/**
	 *  Kept here for compatibility, I don't need it actually, but I'm afraid of what's could happen if I remove it
	 */
	private object HoldMenuEntriesJson : DragonJson<List<NavigationRoute>>()

	private inline fun <reified T> loadCustomObject(
		jsonString: String,
		default: T,
		crossinline onError: (Exception) -> Unit = {}
	): T =
		if (jsonString.isNotBlankJson) {
			try {
				json.decodeFromString<T>(jsonString)
			} catch (e: Exception) {
				onError(e)
				default
			}
		} else {
			default
		}

	/**
	 * Determines whether a pointer position lies within the allowed interaction zone.
	 *
	 * The active zone is defined as the rectangular area of the screen obtained by
	 * excluding padding margins from each edge. Any position inside this rectangle
	 * is considered valid for gesture handling.
	 *
	 * @receiver [Offset] Pointer position in screen coordinates.
	 * @param size Full size of the available surface.
	 * @param left Excluded distance from the left edge.
	 * @param right Excluded distance from the right edge.
	 * @param top Excluded distance from the top edge.
	 * @param bottom Excluded distance from the bottom edge.
	 *
	 * @return `true` if the position is inside the active zone, `false` otherwise.
	 */
	@Suppress("NOTHING_TO_INLINE")
	private inline fun Offset.isInsideActiveZone(
		size: IntSize,
		left: Int,
		right: Int,
		top: Int,
		bottom: Int
	): Boolean =
		x >= left &&
			x <= size.width - right &&
			y >= top &&
			y <= size.height - bottom

	/**
	 * Checks if pointer position is inside any foreground widget bounds.
	 */
	private fun Offset.isInsideForegroundWidget(): Boolean =
		widgetsService.widgets.value
			.filter {
				it.nestId == nestsNavigationService.currentNestId.value && it.foreground != false
			}.any { widget ->
				val dm = widgetsService.dm
				val left = widget.x * dm.widthPixels
				val top = widget.y * dm.heightPixels

				val width = widget.spanX * widgetsService.cellSizePx.value
				val height = widget.spanY * widgetsService.cellSizePx.value

				val right = left + width
				val bottom = top + height

				(x in left..right) && (y in top..bottom)
			}

	init {
		loadAllFromDisk()
	}
}

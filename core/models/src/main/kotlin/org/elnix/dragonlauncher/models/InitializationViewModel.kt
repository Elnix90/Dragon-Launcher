package org.elnix.dragonlauncher.models

import android.app.Application
import androidx.compose.runtime.Stable
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.elnix90.logging.logW
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.APPS_TAG
import org.elnix.dragonlauncher.applications.AppRepository
import org.elnix.dragonlauncher.base.SettingFlow
import org.elnix.dragonlauncher.base.model.serializables.Action
import org.elnix.dragonlauncher.base.model.serializables.IntersectionShape
import org.elnix.dragonlauncher.base.model.serializables.Nest
import org.elnix.dragonlauncher.base.model.serializables.Nests
import org.elnix.dragonlauncher.base.model.serializables.Point
import org.elnix.dragonlauncher.base.model.serializables.Points
import org.elnix.dragonlauncher.base.model.serializables.Profile
import org.elnix.dragonlauncher.ktx.rotateBy
import org.elnix.dragonlauncher.models.utils.viewModelInitialized
import org.elnix.dragonlauncher.permissions.PermissionGroup
import org.elnix.dragonlauncher.permissions.PermissionsManager
import org.elnix.dragonlauncher.points.PointsService
import org.elnix.dragonlauncher.settings.stores.map.PrivateSettingsStore
import org.elnix.dragonlauncher.timer.UsageStatsReader
import javax.inject.Inject

/**
 * Responsible (in the future) for initializing settings and more specifically the default poins in each circle and nests.
 * I don't know the correct architecture I should use, but I invite contributors to come to me to talk about that. RN I pasted the actual initialization code I used since the beginning
 */
@Stable
@HiltViewModel
public class InitializationViewModel
	@Inject
	constructor(
		application: Application,
		private val appRepository: AppRepository,
		private val permissionsManager: PermissionsManager,
		private val usageStatsReader: UsageStatsReader,
		private val pointsService: PointsService
	) : AndroidViewModel(application) {
		init {
			viewModelInitialized()
		}

		public val howMany: SettingFlow<Int> = SettingFlow(30)
		public val hasUsageStatsPermission: StateFlow<Boolean> = permissionsManager.hasPermission(PermissionGroup.UsageStat).stateIn(
			scope = viewModelScope,
			started = SharingStarted.Lazily,
			initialValue = false
		)

		public fun initializeSwipeSettings(
			points: Points,
			nests: Nests,
			defaultPoint: Point?
		) {
			viewModelScope.launch {
				pointsService.set(points, nests, defaultPoint)
				pointsService.persist()
				PrivateSettingsStore.hasInitialized.set(application, true)
			}
		}

		public fun initialize() {
			val points = initializePoints()
			val nests = initializeNests(points)

			initializeSwipeSettings(points, nests, null)
		}

		private fun initializePoints(): Points {
			val recentApps = getMostRecentApps().toMutableList()
			val pointApps = mutableListOf<Point>()

			fun angle(angle: Int) = Offset(0f, 100f).rotateBy(angle.toFloat())

			var shapeId = 0
			while (pointApps.size < 3 || recentApps.isNotEmpty()) {
				for (angle in 0..360 step 15) {
					when (angle) {
						180 if shapeId == 0 -> {
							pointApps.add(
								Point(
									offset = angle(angle),
									action = Action.OpenAppDrawer(),
									id = pointApps.size,
									shapeId = shapeId
								)
							)
						}

						15 if shapeId == 0 -> {
							pointApps.add(
								Point(
									offset = angle(angle),
									action = Action.NotificationShade,
									id = pointApps.size,
									shapeId = shapeId
								)
							)
						}

						345 if shapeId == 0 -> {
							pointApps.add(
								Point(
									offset = angle(angle),
									action = Action.ControlPanel,
									id = pointApps.size,
									shapeId = shapeId
								)
							)
						}

						else -> {
							if (recentApps.isNotEmpty()) {
								pointApps.add(
									Point(
										offset = angle(angle),
										action = recentApps.removeAt(0).action,
										id = pointApps.size,
										shapeId = shapeId
									)
								)
							}
						}
					}
				}

				shapeId += 1
			}

			return pointApps.associateBy { it.id }
		}

		private fun initializeNests(points: Points): Nests {
			val shapesNumber = points.values
				.mapNotNull { it.shapeId }
				.toSet()
				.size
			val shapes = mutableSetOf<IntersectionShape>()

			var scale = 1f
			repeat(maxOf(3, shapesNumber)) { id ->
				val shape = IntersectionShape(
					id = id,
					scale = scale
				)
				shapes.add(shape)
				scale += 0.5f
			}
			return mapOf(
				0 to Nest(0, intersectionShapes = shapes)
			)
		}

		private fun getMostRecentApps(): List<org.elnix.dragonlauncher.base.model.models.Application> {
			val stats = usageStatsReader.getMostUsedApps()
			val apps = mutableListOf<org.elnix.dragonlauncher.base.model.models.Application>()
			stats
				.asSequence()
				.filter { it.totalTimeInForeground > 0 }
				.sortedByDescending { it.lastTimeUsed }
				.take(howMany.value)
				.distinctBy { it.packageName }
				.map {
					viewModelScope.launch {
						// Cannot get the profile of the app, assumes it comes from the main profile. (where most of the user's apps live anyway
						val action = Action.LaunchApp(it.packageName, Profile.dummy())
						val app = appRepository.fromAction(action)

						if (app != null && app.isLaunchable) {
							apps.add(app)
						} else {
							logW(APPS_TAG) { "app not found for ${it.packageName}" }
						}
					}
				}.toList()
			return apps
		}
	}

package org.elnix.dragonlauncher.models

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import org.elnix.dragonlauncher.badges.Badge
import org.elnix.dragonlauncher.badges.BadgeService
import org.elnix.dragonlauncher.base.icons.LauncherIcon
import org.elnix.dragonlauncher.base.model.models.Application
import org.elnix.dragonlauncher.base.model.models.IconSettings
import org.elnix.dragonlauncher.base.model.serializables.Action
import org.elnix.dragonlauncher.base.model.serializables.CustomIcon
import org.elnix.dragonlauncher.base.model.serializables.CustomIconProperties
import org.elnix.dragonlauncher.base.model.serializables.Point
import org.elnix.dragonlauncher.icons.IconService
import org.elnix.dragonlauncher.icons.IconSettingsRepository
import org.elnix.dragonlauncher.models.utils.viewModelInitialized
import javax.inject.Inject

@Stable
@HiltViewModel
class IconsViewModel
	@Inject
	constructor(
		private val iconsService: IconService,
		private val badgeService: BadgeService,
		iconSettingsRepository: IconSettingsRepository
	) : ViewModel() {
		val iconSettings: StateFlow<IconSettings> =
			iconSettingsRepository.settings.stateIn(
				viewModelScope,
				SharingStarted.Eagerly,
				IconSettings()
			)

		fun getIconPickerVM(application: Application): IconPickerVM =
			IconPickerVM(application, iconsService)

		fun getIcon(application: Application): StateFlow<LauncherIcon?> =
			iconsService
				.getAppIcon(
					application
				).stateIn(
					viewModelScope,
					SharingStarted.Eagerly,
					null
				)

		fun getBadge(application: Application): StateFlow<Badge?> =
			badgeService.getBadge(application).stateIn(
				viewModelScope,
				SharingStarted.Eagerly,
				null
			)

		fun getIcon(shortcut: Action.LaunchShortcut): StateFlow<LauncherIcon?> =
			iconsService
				.getShortcutIcon(
					shortcut
				).stateIn(
					viewModelScope,
					SharingStarted.Eagerly,
					null
				)

		fun getIcon(activity: Action.LaunchActivity): StateFlow<LauncherIcon?> =
			iconsService
				.getActivityIcon(
					activity
				).stateIn(
					viewModelScope,
					SharingStarted.Eagerly,
					null
				)

		fun getIcon(action: Action): StateFlow<LauncherIcon?> =
			iconsService.getActionIcon(action).stateIn(
				viewModelScope,
				SharingStarted.Eagerly,
				null
			)

		/**
		 * One-shot preview of a single app icon with the given [customIcon] and [properties],
		 * bypassing the store and the override manager. Used by the icon editor to render
		 * live previews while editing.
		 */
		suspend fun getAppIconOnce(
			application: Application,
			size: Int,
			customIcon: CustomIcon?,
			properties: CustomIconProperties?
		): LauncherIcon? =
			iconsService.getAppIconOnce(application, size, customIcon, properties)

		/**
		 * One-shot preview of a single point icon with the given [customIcon] and [properties],
		 * bypassing the store and the override manager. Used by the icon editor to render
		 * live previews while editing.
		 */
		suspend fun getPointIconOnce(
			point: Point,
			size: Int,
			customIcon: CustomIcon?,
			properties: CustomIconProperties?
		): LauncherIcon? =
			iconsService.getPointIconOnce(point, size, customIcon, properties)

		fun reloadAllPointsIcons(): Unit = iconsService.reloadAllPointIcons()

		fun reinstallAllIconPacks(): Unit = iconsService.reinstallAllIconPacks()

		fun updateIconPacks(): Unit = iconsService.requestIconPackListUpdate()

		init {
			viewModelInitialized()
		}
	}

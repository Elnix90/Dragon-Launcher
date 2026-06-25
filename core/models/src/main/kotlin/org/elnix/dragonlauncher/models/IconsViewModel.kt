@file:OptIn(ExperimentalCoroutinesApi::class)

package org.elnix.dragonlauncher.models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import org.elnix.dragonlauncher.badges.Badge
import org.elnix.dragonlauncher.badges.BadgeService
import org.elnix.dragonlauncher.base.icons.LauncherIcon
import org.elnix.dragonlauncher.base.model.models.Application
import org.elnix.dragonlauncher.base.model.serializables.Action
import org.elnix.dragonlauncher.base.model.serializables.Point
import org.elnix.dragonlauncher.icons.IconService
import org.elnix.dragonlauncher.icons.IconSettings
import org.elnix.dragonlauncher.icons.IconSettingsRepository
import org.elnix.dragonlauncher.models.utils.viewModelInitialized
import javax.inject.Inject


@HiltViewModel
class IconsViewModel @Inject constructor(
    private val iconsService: IconService,
    private val badgeService: BadgeService,
    private val iconSettingsRepository: IconSettingsRepository
) : ViewModel() {


    val iconSettings = iconSettingsRepository.settings.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        IconSettings()
    )


    fun getIconPickerVM(application: Application): IconPickerVM =
        IconPickerVM(application, iconsService)


    fun getIcon(application: Application): StateFlow<LauncherIcon?> = iconsService.getAppIcon(application).stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        null
    )

    fun getBadge(application: Application): StateFlow<Badge?> = badgeService.getBadge(application).stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        null
    )



    fun getIcon(point: Point): StateFlow<LauncherIcon?> = iconsService.getPointIcon(point).stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        null
    )

    fun getIcon(shortcut: Action.LaunchShortcut): StateFlow<LauncherIcon?> = iconsService.getShortcutIcon(shortcut).stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        null
    )

    fun getIcon(action: Action): StateFlow<LauncherIcon?> = iconsService.getActionIcon(action).stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        null
    )


    fun reloadIcon(app: Application) = iconsService.reloadAppIcon(app)
    fun reloadIcon(point: Point) = iconsService.reloadPointIcon(point)
    fun reloadIcon(shortcut: Action.LaunchShortcut) = iconsService.reloadShortcutIcon(shortcut)



    fun updateMaxCacheSize(newSize: Int) = iconsService.updateMaxCacheSize(newSize)
    fun reloadAllPointsIcons() = iconsService.reloadAllPointIcons()
    fun incrementPointCacheSize() = iconsService.incrementPointCacheSize()

    fun reinstallAllIconPacks() = iconsService.reinstallAllIconPacks()
    fun updateIconPacks() = iconsService.requestIconPackListUpdate()


    init {
        viewModelInitialized()
    }
}
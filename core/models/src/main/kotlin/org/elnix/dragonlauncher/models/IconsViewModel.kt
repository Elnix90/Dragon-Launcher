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
import org.elnix.dragonlauncher.base.model.models.IconSettings
import org.elnix.dragonlauncher.base.model.serializables.Action
import org.elnix.dragonlauncher.base.model.serializables.Point
import org.elnix.dragonlauncher.icons.IconService
import org.elnix.dragonlauncher.icons.IconSettingsRepository
import org.elnix.dragonlauncher.models.utils.viewModelInitialized
import javax.inject.Inject


@HiltViewModel
public class IconsViewModel @Inject constructor(
    private val iconsService: IconService,
    private val badgeService: BadgeService,
    private val iconSettingsRepository: IconSettingsRepository
) : ViewModel() {


    public val iconSettings: StateFlow<IconSettings> = iconSettingsRepository.settings.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        IconSettings()
    )


    public fun getIconPickerVM(application: Application): IconPickerVM =
        IconPickerVM(application, iconsService)


    public fun getIcon(application: Application): StateFlow<LauncherIcon?> = iconsService.getAppIcon(application).stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        null
    )

    public fun getBadge(application: Application): StateFlow<Badge?> = badgeService.getBadge(application).stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        null
    )



    public fun getIcon(point: Point): StateFlow<LauncherIcon?> = iconsService.getPointIcon(point).stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        null
    )

    public fun getIcon(shortcut: Action.LaunchShortcut): StateFlow<LauncherIcon?> = iconsService.getShortcutIcon(shortcut).stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        null
    )

    public fun getIcon(action: Action): StateFlow<LauncherIcon?> = iconsService.getActionIcon(action).stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        null
    )


//    /**
//     * One-shot icon load that does NOT create a permanent [StateFlow] subscription.
//     * Prefer this over [getIcon] when you only need the current value.
//     */
//    public suspend fun getPointIconOnce(point: Point): LauncherIcon? =
//        iconsService.getPointIcon(point).first()
//
//    public suspend fun getShortcutIconOnce(shortcut: Action.LaunchShortcut): LauncherIcon? =
//        iconsService.getShortcutIcon(shortcut).first()
//
//    public suspend fun getActionIconOnce(action: Action): LauncherIcon? =
//        iconsService.getActionIcon(action).first()

    public fun reloadIcon(app: Application): Unit = iconsService.reloadAppIcon(app)
    public fun reloadIcon(point: Point): Unit = iconsService.reloadPointIcon(point)
    public fun reloadIcon(shortcut: Action.LaunchShortcut): Unit = iconsService.reloadShortcutIcon(shortcut)



    public fun updateMaxCacheSize(newSize: Int): Unit = iconsService.updateMaxCacheSize(newSize)
    public fun reloadAllPointsIcons(): Unit = iconsService.reloadAllPointIcons()
    public fun incrementPointCacheSize(): Unit = iconsService.incrementPointCacheSize()

    public fun reinstallAllIconPacks(): Unit = iconsService.reinstallAllIconPacks()
    public fun updateIconPacks(): Unit = iconsService.requestIconPackListUpdate()


    init {
        viewModelInitialized()
    }
}
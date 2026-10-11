package org.elnix.dragonlauncher.models

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.elnix.dragonlauncher.base.model.models.Application
import org.elnix.dragonlauncher.base.model.serializables.Action
import org.elnix.dragonlauncher.icons.CustomIconWithPreview
import org.elnix.dragonlauncher.icons.IconPack
import org.elnix.dragonlauncher.icons.IconService
import kotlin.time.Duration.Companion.milliseconds

class IconPickerVM(
	app: Application,
	private val iconService: IconService
) {
	private val action = Action.LaunchApp(app)

	fun getDefaultIcon(size: Int): Flow<CustomIconWithPreview?> =
		flow {
			emit(iconService.getUncustomizedDefaultIcon(action, size))
		}

	fun getIconSuggestions(size: Int): Flow<List<CustomIconWithPreview>> =
		flow {
			emit(iconService.getCustomIconSuggestions(action, size))
		}

	val installedIconPacks: Flow<List<IconPack>> = iconService.getInstalledIconPacks()

	val iconSearchResults: MutableState<List<CustomIconWithPreview>> = mutableStateOf(emptyList())
	val isSearchingIcons: MutableState<Boolean> = mutableStateOf(false)

	private var debounceSearchJob: Job? = null

	public suspend fun searchIcon(query: String, iconPack: IconPack?) {
		debounceSearchJob?.cancelAndJoin()
		if (query.isBlank()) {
			iconSearchResults.value = emptyList()
			isSearchingIcons.value = false
			return
		}
		withContext(Dispatchers.IO) {
			debounceSearchJob =
				launch {
					delay(500.milliseconds)
					isSearchingIcons.value = true
					iconSearchResults.value = emptyList()
					iconSearchResults.value = iconService.searchCustomIcons(query, iconPack)
					isSearchingIcons.value = false
				}
		}
	}
}

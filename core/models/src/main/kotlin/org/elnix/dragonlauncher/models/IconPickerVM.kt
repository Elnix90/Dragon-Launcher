package org.elnix.dragonlauncher.models

import androidx.compose.runtime.mutableStateOf
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.elnix.dragonlauncher.base.model.models.Application
import org.elnix.dragonlauncher.icons.CustomIconWithPreview
import org.elnix.dragonlauncher.icons.IconPack
import org.elnix.dragonlauncher.icons.IconService
import kotlin.coroutines.coroutineContext

class IconPickerVM (
    private val app: Application,
    private val iconService: IconService
) {

    fun getDefaultIcon(size: Int) = flow {
        emit(iconService.getUncustomizedDefaultIcon(app, size))
    }

    fun getIconSuggestions(size: Int) = flow {
        emit(iconService.getCustomIconSuggestions(app, size))
    }

    val installedIconPacks = iconService.getInstalledIconPacks()

    val iconSearchResults = mutableStateOf(emptyList<CustomIconWithPreview>())
    val isSearchingIcons = mutableStateOf(false)


    private var debounceSearchJob: Job? = null
    suspend fun searchIcon(query: String, iconPack: IconPack?) {
        debounceSearchJob?.cancelAndJoin()
        if (query.isBlank()) {
            iconSearchResults.value = emptyList()
            isSearchingIcons.value = false
            return
        }
        withContext(coroutineContext) {
            debounceSearchJob = launch {
                delay(500)
                isSearchingIcons.value = true
                iconSearchResults.value = emptyList()
                iconSearchResults.value = iconService.searchCustomIcons(query, iconPack)
                isSearchingIcons.value = false
            }
        }
    }
}
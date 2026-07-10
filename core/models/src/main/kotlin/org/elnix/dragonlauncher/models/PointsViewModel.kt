package org.elnix.dragonlauncher.models

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import org.elnix.dragonlauncher.models.utils.viewModelInitialized
import org.elnix.dragonlauncher.points.NestsNavigationService
import org.elnix.dragonlauncher.points.PointsService
import javax.inject.Inject

/**
 * Point view model, responsible for holding different values related to the point settings screen
 *
 * it exposes the [PointsService] to let the UI access it
 */
@HiltViewModel
public class PointsViewModel @Inject constructor(
    application: Application,
    public val pointsService: PointsService,
    public val nestsNavigationService: NestsNavigationService
) : AndroidViewModel(application) {

    public val currentNestId: StateFlow<Int> = nestsNavigationService.currentNestId.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = 0
    )


    init {
        viewModelInitialized()
    }
}

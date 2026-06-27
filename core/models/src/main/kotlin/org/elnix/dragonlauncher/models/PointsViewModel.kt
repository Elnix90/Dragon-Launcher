package org.elnix.dragonlauncher.models

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import org.elnix.dragonlauncher.models.utils.viewModelInitialized
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
    public val pointsService: PointsService
) : AndroidViewModel(application) {
    init {
        viewModelInitialized()
    }
}

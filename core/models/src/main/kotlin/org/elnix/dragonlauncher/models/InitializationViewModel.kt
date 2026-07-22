package org.elnix.dragonlauncher.models

import android.app.Application
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.elnix90.logging.INIT_TAG
import io.github.elnix90.logging.logD
import io.github.elnix90.logging.logI
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.base.model.serializables.Action
import org.elnix.dragonlauncher.base.model.serializables.Nest
import org.elnix.dragonlauncher.base.model.serializables.Nests
import org.elnix.dragonlauncher.base.model.serializables.Point
import org.elnix.dragonlauncher.base.model.serializables.Points
import org.elnix.dragonlauncher.models.utils.viewModelInitialized
import org.elnix.dragonlauncher.points.PointsService
import org.elnix.dragonlauncher.settings.stores.map.PrivateSettingsStore
import javax.inject.Inject

/**
 * Responsible (in the future) for initializing settings and more specifically the default poins in each circle and nests.
 * I don't know the correct architecture I should use, but I invite contributors to come to me to talk about that. RN I pasted the actual initialization code I used since the beginning
 */
@HiltViewModel
public class InitializationViewModel @Inject constructor(
    application: Application,
    private val pointsService: PointsService,
) : AndroidViewModel(application) {

    init {
        viewModelInitialized()
    }

    public fun checkLauncherInitialization() {
        viewModelScope.launch {
            val hasInitialized = PrivateSettingsStore.hasInitialized.get(application.applicationContext)

            if (!hasInitialized) {
                logD(INIT_TAG) { "Initialisation not complete, initializing" }
                initialize()
            }
        }
    }

    public fun initializeSwipeSettings(
        points: Points,
        nests: Nests,
        defaultPoint: Point?
    ) {
        logI(INIT_TAG) { "Initializing:\nPoints = $points\nNests = $nests" }

        viewModelScope.launch {
            pointsService.set(points, nests, defaultPoint)
            PrivateSettingsStore.hasInitialized.set(application, true)
        }
    }

    public fun initialize() {
        initializeSwipeSettings(defaultInitializationPoints, defaultNestsInitializationSetup, null)
    }
}


private val defaultInitializationPoints: Points = mapOf(
    0 to Point(
        offset = Offset(0f, -200f),
        action = Action.OpenAppDrawer(),
        id = 0,
        shapeId = 0
    ),
    1 to Point(
        offset = Offset(-150f, 100f),
        action = Action.NotificationShade,
        id = 1,
        shapeId = 0
    ),
    2 to Point(
        offset = Offset(150f, 100f),
        action = Action.ControlPanel,
        id = 2,
        shapeId = 0
    )
)

public val defaultNestsInitializationSetup: Nests = mapOf(
    0 to Nest(0)
)

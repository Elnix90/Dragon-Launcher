package org.elnix.dragonlauncher.models

import android.annotation.SuppressLint
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.base.model.serializables.Action
import org.elnix.dragonlauncher.base.model.serializables.Nest
import org.elnix.dragonlauncher.base.model.serializables.Point
import org.elnix.dragonlauncher.logging.INIT_TAG
import org.elnix.dragonlauncher.logging.logD
import org.elnix.dragonlauncher.logging.logI
import org.elnix.dragonlauncher.models.utils.viewModelInitialized
import org.elnix.dragonlauncher.recents.PointsService
import org.elnix.dragonlauncher.settings.stores.map.PrivateSettingsStore
import java.util.UUID
import javax.inject.Inject

/**
 * Responsible (in the future) for initializing settings and more specifically the default poins in each circle and nests.
 * I don't know the correct architecture I should use, but I invite contributors to come to me to talk about that. RN I pasted the actual initialization code I used since the beginning
 */
@HiltViewModel
class InitializationViewModel @Inject constructor(
    application: Application,
    private val pointsService: PointsService,
) : AndroidViewModel(application) {

    @SuppressLint("StaticFieldLeak")
    private val ctx = application.applicationContext


    init {
        checkLauncherInitialization()
        viewModelInitialized()
    }

    fun checkLauncherInitialization() {
        viewModelScope.launch {
            val hasInitialized = PrivateSettingsStore.hasInitialized.get(ctx)

            if (!hasInitialized) {
                logD(INIT_TAG) { "Initialisation not complete, initializing" }
                initialize()
            }
        }
    }


    fun initializeSwipeSettings(
        points: Set<Point>,
        nests: Set<Nest>
    ) {
        logI(INIT_TAG) { "Initializing:\nPoints = $points\nNests = $nests" }

        viewModelScope.launch {
            pointsService.set(points, nests)
            PrivateSettingsStore.hasInitialized.set(ctx, true)
        }
    }

    fun initialize() {
        initializeSwipeSettings(defaultInitializationSetup, defaultNestsInitializationSetup)
    }
}


private val defaultInitializationSetup = setOf(
    Point(
        circleNumber = 0,
        angleDeg = 0.toDouble(),
        action = Action.OpenAppDrawer(),
        id = UUID.randomUUID().toString()
    ),
    Point(
        circleNumber = 1,
        angleDeg = 200.toDouble(),
        action = Action.NotificationShade,
        id = UUID.randomUUID().toString()
    ),
    Point(
        circleNumber = 1,
        angleDeg = 160.toDouble(),
        action = Action.ControlPanel,
        id = UUID.randomUUID().toString()
    )
)
val defaultNestsInitializationSetup = setOf(
    Nest(0)
)

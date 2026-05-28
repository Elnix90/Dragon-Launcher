package org.elnix.dragonlauncher.models

import android.annotation.SuppressLint
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.common.serializables.Nest
import org.elnix.dragonlauncher.common.serializables.SwipeAction
import org.elnix.dragonlauncher.common.serializables.Point
import org.elnix.dragonlauncher.logging.INIT_TAG
import org.elnix.dragonlauncher.logging.TAG
import org.elnix.dragonlauncher.logging.logD
import org.elnix.dragonlauncher.settings.stores.PrivateSettingsStore
import org.elnix.dragonlauncher.settings.stores.SwipeSettingsStore
import java.util.UUID
import javax.inject.Inject

/**
 * Responsible (in the future) for initializing settings and more specifically the default poins in each circle and nests.
 * I don't know the correct architecture I should use, but I invite contributors to come to me to talk about that. RN I pasted the actual initialization code I used since the beginning
 */
@HiltViewModel
class InitializationViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {

    @SuppressLint("StaticFieldLeak")
    private val ctx = application.applicationContext


    init {
        logD(TAG) { "created InitializationViewModel ${System.identityHashCode(this)}" }
        checkInitialization()
    }

    fun checkInitialization() {
        viewModelScope.launch {
            val hasInitialized = PrivateSettingsStore.hasInitialized.get(ctx)

            if (!hasInitialized) {
                logD(INIT_TAG) { "Initialisation not complete, initializing"}
                initialize()
            }
        }
    }


    fun initializeSwipeSettings(
        points: List<Point>,
        nests: List<Nest>
    ) {
        viewModelScope.launch{
            SwipeSettingsStore.savePoints(ctx, points)
            SwipeSettingsStore.saveNests(ctx, nests)

            PrivateSettingsStore.hasInitialized.set(ctx, true)
        }
    }

    fun initialize() {
        initializeSwipeSettings(defaultInitializationSetup, defaultNestsInitializationSetup)
    }
}


val defaultInitializationSetup = listOf(
    Point(
        circleNumber = 0,
        angleDeg = 0.toDouble(),
        action = SwipeAction.OpenAppDrawer(),
        id = UUID.randomUUID().toString()
    ),
    Point(
        circleNumber = 1,
        angleDeg = 200.toDouble(),
        action = SwipeAction.NotificationShade,
        id = UUID.randomUUID().toString()
    ),
    Point(
        circleNumber = 1,
        angleDeg = 160.toDouble(),
        action = SwipeAction.ControlPanel,
        id = UUID.randomUUID().toString()
    )
)
val defaultNestsInitializationSetup = listOf(
    Nest(0)
)

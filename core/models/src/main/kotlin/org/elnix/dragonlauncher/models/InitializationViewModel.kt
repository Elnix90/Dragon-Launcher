package org.elnix.dragonlauncher.models

import android.annotation.SuppressLint
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.common.messyfolder.Constants.Logging.INIT_TAG
import org.elnix.dragonlauncher.common.messyfolder.Constants.Logging.TAG
import org.elnix.dragonlauncher.common.serializables.CircleNest
import org.elnix.dragonlauncher.common.serializables.SwipeActionSerializable
import org.elnix.dragonlauncher.common.serializables.SwipePointSerializable
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


    suspend fun initializeSwipeSettings(
        points: List<SwipePointSerializable>,
        nests: List<CircleNest>
    ) {
        SwipeSettingsStore.savePoints(ctx, points)
        SwipeSettingsStore.saveNests(ctx, nests)

        PrivateSettingsStore.hasInitialized.set(ctx, true)
    }

    suspend fun initialize() {
        initializeSwipeSettings(defaultInitializationSetup, defaultNestsInitializationSetup)
    }
}


val defaultInitializationSetup = listOf(
    SwipePointSerializable(
        circleNumber = 0,
        angleDeg = 0.toDouble(),
        action = SwipeActionSerializable.OpenAppDrawer(),
        id = UUID.randomUUID().toString()
    ),
    SwipePointSerializable(
        circleNumber = 1,
        angleDeg = 200.toDouble(),
        action = SwipeActionSerializable.NotificationShade,
        id = UUID.randomUUID().toString()
    ),
    SwipePointSerializable(
        circleNumber = 1,
        angleDeg = 160.toDouble(),
        action = SwipeActionSerializable.ControlPanel,
        id = UUID.randomUUID().toString()
    )
)
val defaultNestsInitializationSetup = listOf(
    CircleNest(0)
)

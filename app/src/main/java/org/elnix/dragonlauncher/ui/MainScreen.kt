package org.elnix.dragonlauncher.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import org.elnix.dragonlauncher.data.datastore.SettingsStore
import org.elnix.dragonlauncher.data.datastore.SwipeDataStore
import org.elnix.dragonlauncher.ui.helpers.HoldToActivateArc
import org.elnix.dragonlauncher.ui.helpers.rememberHoldToOpenSettings
import org.elnix.dragonlauncher.utils.actions.launchSwipeAction

@Composable
fun MainScreen(
    onAppDrawer: () -> Unit,
    onLongPress3Sec: () -> Unit
) {
    val ctx = LocalContext.current

    var start by remember { mutableStateOf<Offset?>(null) }
    var current by remember { mutableStateOf<Offset?>(null) }
    var isDragging by remember { mutableStateOf(false) }
    var size by remember { mutableStateOf(IntSize.Zero) }

    val hold = rememberHoldToOpenSettings(
        onSettings = onLongPress3Sec
    )

    val defaultColor = Color.Red
    val rgbLoading by SettingsStore.getRGBLoading(ctx)
        .collectAsState(initial = true)

    val points by SwipeDataStore.getPointsFlow(ctx).collectAsState(emptyList())

    // To prevent the user from exiting the app on back, since it's a launcher
    BackHandler { }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(WindowInsets.systemBars.asPaddingValues())
            .imePadding()
            .pointerInput(Unit) {

                awaitEachGesture {
                    val down = awaitFirstDown()
                    start = down.position
                    current = down.position
                    isDragging = true

                    val pointerId = down.id

                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == pointerId }

                        if (change != null) {
                            if (change.pressed) {
                                change.consume()
                                current = change.position
                            } else {
                                isDragging = false
                                start = null
                                current = null
                                break
                            }
                        } else {
                            isDragging = false
                            start = null
                            current = null
                            break
                        }
                    }
                }
            }
            .onSizeChanged { size = it }
            .then(hold.pointerModifier)
    ) {
        MainScreenOverlay(
            start = start,
            current = current,
            isDragging = isDragging,
            surface = size,
            points =points,
            onLaunch = { launchSwipeAction(ctx, it) { onAppDrawer} }
        )

        HoldToActivateArc(
            center = hold.centerProvider(),
            progress = hold.progressProvider(),
            defaultColor = defaultColor,
            rgbLoading = rgbLoading
        )
    }
}

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.data.SwipePointSerializable
import org.elnix.dragonlauncher.data.stores.PrivateSettingsStore
import org.elnix.dragonlauncher.data.stores.SwipeSettingsStore
import org.elnix.dragonlauncher.data.stores.UiSettingsStore
import org.elnix.dragonlauncher.ui.helpers.FilePickerDialog
import org.elnix.dragonlauncher.ui.helpers.HoldToActivateArc
import org.elnix.dragonlauncher.ui.helpers.UserValidation
import org.elnix.dragonlauncher.ui.helpers.rememberHoldToOpenSettings
import org.elnix.dragonlauncher.utils.models.AppDrawerViewModel
import org.elnix.dragonlauncher.utils.actions.launchSwipeAction

@Suppress("AssignedValueIsNeverRead")
@Composable
fun MainScreen(
    appsViewModel: AppDrawerViewModel,
    onAppDrawer: () -> Unit,
    onGoWelcome: () -> Unit,
    onLongPress3Sec: () -> Unit
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    var showFilePicker: SwipePointSerializable? by remember { mutableStateOf(null) }
    var showMethodDialog by remember { mutableStateOf(false) }


    val showMethodAsking by PrivateSettingsStore.getShowMethodAsking(ctx)
        .collectAsState(initial = false)


    val icons by appsViewModel.icons.collectAsState()

    var start by remember { mutableStateOf<Offset?>(null) }
    var current by remember { mutableStateOf<Offset?>(null) }
    var isDragging by remember { mutableStateOf(false) }
    var size by remember { mutableStateOf(IntSize.Zero) }

    val hold = rememberHoldToOpenSettings(
        onSettings = onLongPress3Sec
    )

    val defaultColor = Color.Red
    val rgbLoading by UiSettingsStore.getRGBLoading(ctx)
        .collectAsState(initial = true)

    val hasSeenWelcome by PrivateSettingsStore.getHasSeenWelcome(ctx)
        .collectAsState(initial = true)

    val useAccessibilityInsteadOfContextToExpandActionPanel by PrivateSettingsStore
        .getUseAccessibilityInsteadOfContextToExpandActionPanel(ctx)
        .collectAsState(initial = false)


    LaunchedEffect(hasSeenWelcome) {
        if (!hasSeenWelcome) onGoWelcome()
    }

    val points by SwipeSettingsStore.getPointsFlow(ctx).collectAsState(emptyList())

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
            icons = icons,
            start = start,
            current = current,
            isDragging = isDragging,
            surface = size,
            points = points,
            onLaunch = {
                launchSwipeAction(
                    ctx = ctx,
                    action = it?.action,
                    useAccessibilityInsteadOfContextToExpandActionPanel = useAccessibilityInsteadOfContextToExpandActionPanel,
                    onAskWhatMethodToUseToOpenQuickActions = { showMethodDialog = true },
                    onReloadApps = { scope.launch { appsViewModel.reloadApps(ctx) } },
                    onReselectFile = { showFilePicker = it },
                    onAppSettings = onLongPress3Sec,
                    onAppDrawer = onAppDrawer
                )
            }
        )

        HoldToActivateArc(
            center = hold.centerProvider(),
            progress = hold.progressProvider(),
            defaultColor = defaultColor,
            rgbLoading = rgbLoading
        )
    }

    if (showFilePicker != null) {
        val currentPoint = showFilePicker!!

        FilePickerDialog(
            onDismiss = { showFilePicker = null },
            onFileSelected = { newAction ->

                // Build the updated point
                val updatedPoint = currentPoint.copy(action = newAction)

                // Replace only this point
                val finalList = points.map { p ->
                    if (p == currentPoint) updatedPoint else p
                }


                scope.launch {
                    SwipeSettingsStore.save(ctx, finalList)
                }

                showFilePicker = null
            }
        )
    }


    if (showMethodDialog and showMethodAsking) {
        UserValidation(
            title = "What method to open the quick actions?",
            message = "Did the quick actions open or was it the notifications?",
            validateText = "Quick Actions",
            cancelText = "Notifications",
            canDismissByOuterClick = false,
            doNotRemindMeAgain = {
                scope.launch {
                    PrivateSettingsStore.setShowMethodAsking(ctx, false)
                }
            },
            onCancel = {
                // The simple ctx method didn't work, so forced to use the accessibility method, that doesn't work well on my phone
                scope.launch {
                    PrivateSettingsStore.setUseAccessibilityInsteadOfContextToExpandActionPanel(ctx, true)
                }
                showMethodDialog = false
            },
            onAgree = {
                // The simple ctx method worked, keep it
                scope.launch {
                    PrivateSettingsStore.setUseAccessibilityInsteadOfContextToExpandActionPanel(ctx, false)
                }
                showMethodDialog = false
            }
        )
    }
}

package org.elnix.dragonlauncher.ui.welcome

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import `in`.hridayan.shapeindicators.ShapeIndicatorDefaults
import `in`.hridayan.shapeindicators.ShapeIndicatorRow
import io.github.elnix90.logging.logD
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.WELCOME_TAG
import org.elnix.dragonlauncher.base.navigaton.NavigationRoute
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.models.InitializationViewModel
import org.elnix.dragonlauncher.settings.stores.map.PrivateSettingsStore
import org.elnix.dragonlauncher.ui.base.activityViewModel
import org.elnix.dragonlauncher.ui.base.components.AnimatedFab
import org.elnix.dragonlauncher.ui.base.components.Spacer
import org.elnix.dragonlauncher.ui.compositionslocals.LocalNavigator


private const val pageNumber = 6

@SuppressLint("LocalContextGetResourceValueCall", "FrequentlyChangingValue")
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun WelcomeScreen(
    initializationViewModel: InitializationViewModel = activityViewModel()
) {
    val ctx = LocalContext.current
    val navigator = LocalNavigator.current
    val pagerState = rememberPagerState(pageCount = { pageNumber })
    val scope = rememberCoroutineScope()

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                scope.launch {
                    val pagerPage = PrivateSettingsStore.welcomeScreenTempPage.getOrNull(ctx)

                    if (pagerPage != null) {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerPage)
                        }
                    }
                }
            }
        }

        // Add the observer to the lifecycle
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }


    LaunchedEffect(pagerState.currentPage) {
        val pageId = pagerState.currentPage

        logD(WELCOME_TAG) { "Setting the pager to $pageId" }
        // Set the current page to remember it
        scope.launch {
            PrivateSettingsStore.welcomeScreenTempPage.set(ctx, pageId)
        }
    }

    // Prevent the user to quit
    BackHandler { }

    fun setHasSeen() {
        scope.launch {
            PrivateSettingsStore.hasSeenWelcome.set(ctx, true)
            // Resets the pager, that is only used to scroll to the page the user left when it re-enters the welcome screen
            PrivateSettingsStore.welcomeScreenTempPage.reset(ctx)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(WindowInsets.systemBars.asPaddingValues())
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(24.dp)

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) { page ->
                when (page) {
                    0 -> WelcomePageIntro(pagerState.currentPage < 2, ::setHasSeen)
                    1 -> WelcomePagePrivacy()
                    2 -> WelcomePageTutorial()
                    3 -> WelcomePageLauncher()
                    4 -> WelcomePageBackup()
                    5 -> WelcomePageFinish(
                        onEnterSettings = {
                            setHasSeen()

                            // Initialize only when exiting from the welcome screen, to avoid the initialization layer to override points/nests
                            initializationViewModel.checkLauncherInitialization()
                            navigator.popBackMainScreen()
                            navigator.go(NavigationRoute.PointsSettings())
                        },
                        onEnterApp = {
                            setHasSeen()

                            // Initialize only when exiting from the welcome screen, to avoid the initialization layer to override points/nests
                            initializationViewModel.checkLauncherInitialization()

                            navigator.onBack()
                        }
                    )
                }
            }

            Spacer(16.dp)

            ShapeIndicatorRow(
                pagerState = pagerState,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                shuffleShapes = true,
                overflow = ShapeIndicatorDefaults.overflow(maxVisibleItems = pageNumber)
            )
        }

        val fabScale by animateFloatAsState(
            if (pagerState.currentPage < pageNumber - 1) 1f else -(pagerState.currentPageOffsetFraction * 2)
        )
        AnimatedFab(
            icon = R.drawable.arrow_forward,
            modifier = Modifier
                .padding(10.dp)
                .align(Alignment.BottomEnd)
                .scale(fabScale),
            minSize = 80.dp
        ) {
            val next = pagerState.currentPage + 1
            if (next < pageNumber) {
                scope.launch { pagerState.animateScrollToPage(next) }
            }
        }
    }
}

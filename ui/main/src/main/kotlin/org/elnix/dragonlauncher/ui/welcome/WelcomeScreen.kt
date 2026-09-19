package org.elnix.dragonlauncher.ui.welcome

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import `in`.hridayan.shapeindicators.ShapeIndicatorColumn
import `in`.hridayan.shapeindicators.ShapeIndicatorDefaults
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.elnix.dragonlauncher.base.navigation.NavigationRoute
import org.elnix.dragonlauncher.models.InitializationViewModel
import org.elnix.dragonlauncher.ui.base.activityViewModel
import org.elnix.dragonlauncher.ui.base.animation.slideInHorizontalBouncy
import org.elnix.dragonlauncher.ui.base.animation.slideOutHorizontalBouncy
import org.elnix.dragonlauncher.ui.base.components.VerticalScrollIndicator
import org.elnix.dragonlauncher.ui.compositionslocals.LocalNavigator
import kotlin.time.Duration.Companion.milliseconds

@SuppressLint("LocalContextGetResourceValueCall", "FrequentlyChangingValue")
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun WelcomeScreen(
    welcomeViewModel: WelcomeViewModel,
    initializationViewModel: InitializationViewModel = activityViewModel()
) {
    val navigator = LocalNavigator.current

    val pagerState = welcomeViewModel.pagerState

    var showScrollIndicator by remember { mutableStateOf(false) }
    var showShapesScrollBar by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(500.milliseconds)
        showShapesScrollBar = true
    }

    LaunchedEffect(pagerState.currentPage) {
        val pageId = pagerState.currentPage

        // When the page is the first one
        if (pageId == 0) {
            launch {
                delay(500.milliseconds)
                showScrollIndicator = true
            }
        } else {
            showScrollIndicator = false
        }
    }

    // Prevent the user to quit
    BackHandler { }

    val currentPage = pagerState.currentPage
    val pagerTransparency = when {
        currentPage < PAGES_NUMBER - 2 -> {
            1f
        }

        currentPage == PAGES_NUMBER - 1 -> {
            0f
        }

        else -> {
            1f - pagerState.currentPageOffsetFraction * 2
        }
    }

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                // Here I purposely use the .copy method instead of the .alphaMultiplier, in order to force the full transparency of the background
                .background(MaterialTheme.colorScheme.background.copy(pagerTransparency))
    ) {
        VerticalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) { displayPage ->
            when (displayPage) {
                0 -> {
                    WelcomePageIntro(pagerState.currentPage < 2, welcomeViewModel::setAsSeen)
                }

                1 -> {
                    WelcomePageTutorial()
                }

                2 -> {
                    WelcomePageSettings(
                        onEnterSettings = {
                            welcomeViewModel.setAsSeen()

                            // Initialize only when exiting from the welcome screen, to avoid the initialization layer to override points/nests
                            initializationViewModel.checkLauncherInitialization()
                            navigator.popBackMainScreen()
                            navigator.go(NavigationRoute.PointsSettings)
                        }
                    )
                }

                3 -> {
                    LaunchedEffect(pagerState.currentPage) {
                        if (pagerState.currentPage == PAGES_NUMBER - 1) {
                            welcomeViewModel.setAsSeen()

                            showShapesScrollBar = false

                            // Initialize only when exiting from the welcome screen, to avoid the initialization layer to override points/nests
                            initializationViewModel.checkLauncherInitialization()

                            navigator.onBack()
                        }
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = showShapesScrollBar,
            enter = slideInHorizontalBouncy,
            exit = slideOutHorizontalBouncy,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 12.dp)
        ) {
            ShapeIndicatorColumn(
                pagerState = pagerState,
                verticalArrangement = Arrangement.spacedBy(10.dp),
                shuffleShapes = true,
                overflow = ShapeIndicatorDefaults.overflow(maxVisibleItems = PAGES_NUMBER)
            )
        }

        VerticalScrollIndicator(
            visible = showScrollIndicator,
            modifier = Modifier.padding(bottom = 50.dp)
        )
    }
}

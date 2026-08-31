package org.elnix.dragonlauncher.ui.welcome

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.elnix.dragonlauncher.i18n.R

@Composable
fun WelcomePageTutorial() {
    WelcomePagerHeader(
        title = stringResource(R.string.quick_tutorial),
        icon = R.drawable.help
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            TutorialEntry(R.mipmap.long_click_3second, R.string.long_click_to_access_settings)
            TutorialEntry(R.mipmap.configure_your_apps, R.string.configure_your_apps)
            TutorialEntry(R.mipmap.swipe_to_open_app, R.string.swipe_to_open_app)
        }
    }
}

@Composable
private fun TutorialEntry(
    painterResId: Int,
    titleResId: Int
) {
    Card {
        Column(
            modifier = Modifier.padding(5.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(titleResId),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(5.dp)
            )

            Image(
                painterResource(painterResId),
                contentDescription = stringResource(titleResId)
            )
        }
    }
}

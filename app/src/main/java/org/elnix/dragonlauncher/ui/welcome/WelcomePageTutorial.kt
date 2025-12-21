package org.elnix.dragonlauncher.ui.welcome

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.elnix.dragonlauncher.R
import org.elnix.dragonlauncher.utils.colors.AppObjectsColors

@Composable
fun WelcomePageTutorial() {


    WelcomePagerHeader(
        title = stringResource(R.string.quick_tutorial),
        icon = Icons.AutoMirrored.Filled.Help
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Card(
                colors = AppObjectsColors.cardColors(),
            ){
                Column(
                    modifier = Modifier.padding(5.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ){
                    Text(
                        text = stringResource(R.string.long_click_to_access_settings),
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(5.dp)
                        )

                    Image(
                        painterResource(R.drawable.long_click_3second),
                        contentDescription = stringResource(R.string.long_click_to_access_settings),
                    )
                }
            }

            Spacer(Modifier.height(15.dp))

            Card(
                colors = AppObjectsColors.cardColors(),
            ){
                Column(
                    modifier = Modifier.padding(5.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ){
                    Text(
                        text = stringResource(R.string.configure_your_apps),
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(5.dp)
                    )

                    Image(
                        painterResource(R.drawable.configure_your_apps),
                        contentDescription = stringResource(R.string.configure_your_apps),
                    )
                }
            }

            Spacer(Modifier.height(15.dp))

            Card(
                colors = AppObjectsColors.cardColors(),
            ){
                Column(
                    modifier = Modifier.padding(5.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ){
                    Text(
                        text = stringResource(R.string.swipe_to_open_app),
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(5.dp)
                    )

                    Image(
                        painterResource(R.drawable.swipe_to_open_app),
                        contentDescription = stringResource(R.string.swipe_to_open_app),
                    )
                }
            }
        }
    }
}

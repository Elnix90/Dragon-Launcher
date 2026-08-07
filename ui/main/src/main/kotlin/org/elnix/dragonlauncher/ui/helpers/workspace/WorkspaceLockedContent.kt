package org.elnix.dragonlauncher.ui.helpers.workspace

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import org.elnix.dragonlauncher.base.model.serializables.Profile
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.models.ProfilesViewModel
import org.elnix.dragonlauncher.ui.base.activityViewModel
import org.elnix.dragonlauncher.ui.base.animation.barsContentTransform
import org.elnix.dragonlauncher.ui.compositionslocals.LocalDrawerSettings
import org.elnix.dragonlauncher.ui.dragon.components.DragonButton
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun WorkspaceLockedContent(
    workspaceProfile: Profile,
    isActive: Boolean,
    profilesViewModel: ProfilesViewModel = activityViewModel()
) {
    val hasProfilesPermission by profilesViewModel.hasProfilesPermission.collectAsState(false)
    val workspaceProfileType = workspaceProfile.type

    val autoAskToUnlockProfile = LocalDrawerSettings.current.autoAskToUnlockProfile
    var hasAsked by remember { mutableStateOf(false) }

    LaunchedEffect(isActive, autoAskToUnlockProfile) {
        if (isActive && autoAskToUnlockProfile) {
            profilesViewModel.askProfileLock(workspaceProfile, false)
            hasAsked = true
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth()
                .border(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant,
                    MaterialTheme.shapes.small
                )
                .background(
                    MaterialTheme.colorScheme.surfaceContainer,
                    MaterialTheme.shapes.small
                )
                .padding(vertical = 64.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                painterResource(if (workspaceProfileType == Profile.Type.Work) R.drawable.enterprise_off else R.drawable.lock),
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.secondary,
            )
            Text(
                stringResource(
                    if (workspaceProfileType == Profile.Type.Work) R.string.profile_work_profile_state_locked
                    else R.string.profile_private_profile_state_locked
                ),
                modifier = Modifier.padding(top = 8.dp),
                color = MaterialTheme.colorScheme.secondary,
                style = MaterialTheme.typography.titleSmall,
            )
            if (hasProfilesPermission) {
                AnimatedContent(
                    targetState = hasAsked,
                    modifier = Modifier.padding(top = 32.dp),
                    transitionSpec = { barsContentTransform }
                ) { isLoading ->
                    if (!isLoading) {
                        DragonButton(
                            onClick = {
                                profilesViewModel.askProfileLock(workspaceProfile, false)
                                hasAsked = true
                            }
                        ) {
                            val iconRes = when(workspaceProfileType) {
                                Profile.Type.Personal -> error("Personal profile is never locked")
                                Profile.Type.Work -> R.drawable.enterprise
                                Profile.Type.Private -> R.drawable.lock_open
                            }

                            val stringRes = when(workspaceProfileType) {
                                Profile.Type.Personal -> error("Personal profile is never locked")
                                Profile.Type.Work ->R.string.profile_work_profile_action_unlock
                                Profile.Type.Private ->  R.string.profile_private_profile_action_unlock
                            }

                            Icon(
                                painterResource(iconRes),
                                contentDescription = null,
                                modifier = Modifier
                                    .padding(end = ButtonDefaults.IconSpacing)
                                    .size(ButtonDefaults.IconSize)
                            )
                            Text(stringResource(stringRes))
                        }
                    } else {
                        LaunchedEffect(Unit) {
                            delay(10.seconds)
                            hasAsked = false
                        }
                        LoadingIndicator()
                    }
                }
            }
        }
    }
}

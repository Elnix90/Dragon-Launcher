package org.elnix.dragonlauncher.ui.warning

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.models.SecurityViewModel
import org.elnix.dragonlauncher.ui.base.activityViewModel
import org.elnix.dragonlauncher.ui.base.asState
import org.elnix.dragonlauncher.ui.dragon.components.ValidateCancelButtons

@OptIn(
    ExperimentalMaterial3ExpressiveApi::class,
    ExperimentalMaterial3Api::class
)
@Composable
fun SignatureWarningDialog(
    securityViewModel: SecurityViewModel = activityViewModel()
) {
    val signatureMatched by securityViewModel.signatureMatched.asState()
    val useAnyways by securityViewModel.useAnyways.asState()
    if (signatureMatched || useAnyways) return

    val ctx = LocalContext.current

    BasicAlertDialog(
        onDismissRequest = {}
    ) {
        Card(shape = MaterialTheme.shapes.extraLarge) {
            Column(
                modifier =
                    Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier =
                        Modifier
                            .size(48.dp)
                            .background(
                                color = MaterialTheme.colorScheme.errorContainer,
                                shape = MaterialShapes.Pill.toShape()
                            )
                ) {
                    Icon(
                        painter = painterResource(R.drawable.warning),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                }

                Text(
                    text = stringResource(R.string.signature_not_matched),
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.error
                )

                ValidateCancelButtons(
                    cancelText = stringResource(R.string.use_anyways),
                    validateText = "${stringResource(R.string.uninstall)} ☠\uFE0F",
                    onCancel = {
                        securityViewModel.useAnyways.value = true
                    },
                    onConfirm = {
                        ctx.startActivity(
                            Intent(Intent.ACTION_DELETE).apply {
                                data = "package:${ctx.packageName}".toUri()
                            }
                        )
                    }
                )
            }
        }
    }
}

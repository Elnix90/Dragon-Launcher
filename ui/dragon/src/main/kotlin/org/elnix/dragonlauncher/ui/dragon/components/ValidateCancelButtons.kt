package org.elnix.dragonlauncher.ui.dragon.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.ui.base.withHaptic
import org.elnix.dragonlauncher.ui.dragon.text.AutoResizeableText

@Composable
fun ValidateCancelButtons(
	confirmText: String = stringResource(R.string.save),
	cancelText: String = stringResource(R.string.cancel),
	validateEnabled: Boolean = true,
	onCancel: (() -> Unit)? = null,
	onConfirm: () -> Unit
) {
	val interactionSources = remember { List(2) { MutableInteractionSource() } }

	ButtonGroup(
		overflowIndicator = { ButtonGroupDefaults.OverflowIndicator(it) },
		modifier = Modifier.fillMaxWidth()
	) {
		if (onCancel != null) {
			customItem(
				buttonGroupContent = {
					DragonButton(
						onClick =
							withHaptic(HapticFeedbackType.Reject) {
								onCancel()
							},
						modifier =
							Modifier
								.weight(1f)
								.animateWidth(interactionSources[0]),
						interactionSource = interactionSources[0],
						isCancel = true
					) {
						Icon(
							painter = painterResource(R.drawable.close),
							contentDescription = cancelText
						)
						AutoResizeableText(
							text = cancelText,
							style = MaterialTheme.typography.labelLarge
						)
					}
				},
				menuContent = {}
			)
		}

		customItem(
			buttonGroupContent = {
				DragonButton(
					onClick =
						withHaptic(HapticFeedbackType.Confirm) {
							onConfirm()
						},
					enabled = validateEnabled,
					modifier =
						Modifier
							.weight(1f)
							.animateWidth(interactionSources[1]),
					interactionSource = interactionSources[1]
				) {
					Icon(
						painter = painterResource(R.drawable.check),
						contentDescription = confirmText
					)
					AutoResizeableText(
						text = confirmText,
						style = MaterialTheme.typography.labelLarge
					)
				}
			},
			menuContent = { }
		)
	}
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ValidateCancelButtonsWithLoading(
	confirmText: String = stringResource(R.string.save),
	cancelText: String = stringResource(R.string.cancel),
	validateEnabled: Boolean = true,
	onCancel: (() -> Unit)? = null,
	hasClickedValidate: Boolean,
	onConfirm: () -> Unit
) {
	val interactionSources = remember { List(2) { MutableInteractionSource() } }

	ButtonGroup(
		overflowIndicator = { ButtonGroupDefaults.OverflowIndicator(it) },
		modifier = Modifier.fillMaxWidth()
	) {
		if (onCancel != null) {
			customItem(
				buttonGroupContent = {
					DragonButton(
						onClick =
							withHaptic(HapticFeedbackType.Reject) {
								onCancel()
							},
						enabled = !hasClickedValidate,
						modifier =
							Modifier
								.weight(1f)
								.animateWidth(interactionSources[0]),
						interactionSource = interactionSources[0],
						isCancel = true
					) {
						Icon(
							painter = painterResource(R.drawable.close),
							contentDescription = cancelText
						)
						AutoResizeableText(
							text = cancelText,
							style = MaterialTheme.typography.labelLarge
						)
					}
				},
				menuContent = {}
			)
		}

		customItem(
			buttonGroupContent = {
				AnimatedContent(
					targetState = !hasClickedValidate,
					modifier =
						Modifier
							.weight(1f)
							.animateWidth(interactionSources[1])
				) { hasClickedImport ->
					if (hasClickedImport) {
						DragonButton(
							onClick =
								withHaptic(HapticFeedbackType.Confirm) {
									onConfirm()
								},
							enabled = validateEnabled,
							interactionSource = interactionSources[1]
						) {
							Icon(
								painter = painterResource(R.drawable.check),
								contentDescription = confirmText
							)
							AutoResizeableText(
								text = confirmText,
								style = MaterialTheme.typography.labelLarge
							)
						}
					} else {
						Row(
							modifier = Modifier.clip(CircleShape),
							horizontalArrangement = Arrangement.Center,
							verticalAlignment = Alignment.CenterVertically
						) {
							LoadingIndicator()
						}
					}
				}
			},
			menuContent = { }
		)
	}
}

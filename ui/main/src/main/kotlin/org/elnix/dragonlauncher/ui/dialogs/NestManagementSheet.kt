package org.elnix.dragonlauncher.ui.dialogs

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.FocusInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuGroup
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import org.elnix.dragonlauncher.base.model.serializables.Action
import org.elnix.dragonlauncher.base.model.serializables.Nest
import org.elnix.dragonlauncher.base.model.serializables.Point
import org.elnix.dragonlauncher.base.navigation.NavigationRoute
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.ktx.getCenter
import org.elnix.dragonlauncher.models.PointsViewModel
import org.elnix.dragonlauncher.theme.AppObjectsColors
import org.elnix.dragonlauncher.ui.base.activityViewModel
import org.elnix.dragonlauncher.ui.base.animation.Icon
import org.elnix.dragonlauncher.ui.base.animation.rememberAnimatedIcon
import org.elnix.dragonlauncher.ui.base.asState
import org.elnix.dragonlauncher.ui.base.components.Spacer
import org.elnix.dragonlauncher.ui.base.remember.rememberInteractionSource
import org.elnix.dragonlauncher.ui.compositionslocals.LocalNavigator
import org.elnix.dragonlauncher.ui.dragon.components.DragonButton
import org.elnix.dragonlauncher.ui.dragon.components.DragonDropDownMenu
import org.elnix.dragonlauncher.ui.dragon.components.DragonGroupScope
import org.elnix.dragonlauncher.ui.dragon.components.DragonModalBottomSheet
import org.elnix.dragonlauncher.ui.dragon.components.DragonSettingsGroup
import org.elnix.dragonlauncher.ui.dragon.components.MoreIcon
import org.elnix.dragonlauncher.ui.dragon.components.ValidateCancelButtons
import org.elnix.dragonlauncher.ui.dragon.text.DialogDescription
import org.elnix.dragonlauncher.ui.dragon.text.DialogTitle
import org.elnix.dragonlauncher.ui.helpers.swipe.PointIcon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NestManagementSheet(
	pointsViewModel: PointsViewModel = activityViewModel(),
	title: String? = null,
	onSelect: ((Nest) -> Unit)? = null,
	onDismissRequest: () -> Unit
) {
	val pointsService = pointsViewModel.pointsService
	val navigator = LocalNavigator.current
	val recomposeTrigger by pointsService.recomposeTrigger.asState()
	val nests by pointsService.nests.collectAsState()

	val currentNestId by pointsViewModel.nestsNavigationService.currentNestId.collectAsState()

	var hasClickedNewNest by remember { mutableStateOf<Int?>(null) }
	val listState = rememberLazyListState()
	LaunchedEffect(hasClickedNewNest) {
		if (hasClickedNewNest != null) {
			listState.animateScrollToItem(hasClickedNewNest!!)
			hasClickedNewNest = null
		}
	}

	val nestsList = remember(recomposeTrigger, nests.size) { nests.toList() }

	DragonModalBottomSheet(
		onDismissRequest = onDismissRequest,
		skipPartiallyExpanded = true
	) {
		DialogTitle(title ?: stringResource(R.string.manage_nests))
		Spacer(10.dp)

		DragonButton(
			onClick = {
				hasClickedNewNest = pointsService.addNest()
			},
			modifier = Modifier.fillMaxWidth()
		) {
			Icon(
				painter = painterResource(R.drawable.add_circle),
				contentDescription = stringResource(R.string.create_new_nest)
			)
			Spacer(15.dp)
			Text(stringResource(R.string.create_new_nest))
		}

		Spacer(15.dp)

		DragonSettingsGroup {
			LazyColumn(
				verticalArrangement = Arrangement.spacedBy(5.dp),
				modifier = Modifier.heightIn(max = 700.dp),
				state = listState
			) {
				items(nestsList) { (id, nest) ->
					NestManagementItem(
						nest = nest,
						selected = id == currentNestId,
						modifier = Modifier.animateItem(),
						onEditName = { newName ->
							pointsService.editNest(nest.id) { old ->
								old.copy(name = newName)
							}
						},
						onDelete = { deleteAllRelatedPoints -> pointsService.removeNest(nest.id, deleteAllRelatedPoints) },
						onDuplicate = { pointsService.duplicateNest(nest.id) },
						onEdit = {
							pointsViewModel.nestsNavigationService.goToNest(id, true)
							navigator.navigate(NavigationRoute.NestEdit)
						},
						onSelect = { onSelect?.invoke(nest) }
					)
				}
			}
		}
	}
}

@Composable
private fun DragonGroupScope.NestManagementItem(
	nest: Nest,
	selected: Boolean,
	modifier: Modifier,
	onEditName: (newName: String?) -> Unit,
	onDelete: (deleteAllRelatedPoints: Boolean) -> Unit,
	onDuplicate: () -> Unit,
	onEdit: () -> Unit,
	onSelect: (() -> Unit)? = null
) {
	// Same as in dragonSettingGroup, needed to prevent the nest to erase the bg
	val bgColor = MaterialTheme.colorScheme.surfaceContainerHigh

	var showPopup by rememberSaveable { mutableStateOf(false) }
	var showDeleteConfirmDialog by remember { mutableStateOf(false) }

	Row(
		modifier =
			modifier
				.dragonSettingGroup(selected = selected) {
					height(120.dp)
						.clickable { onSelect?.invoke() }
				},
		verticalAlignment = Alignment.CenterVertically,
		horizontalArrangement = Arrangement.End
	) {
		BoxWithConstraints(Modifier.requiredSize(100.dp)) {
			val center = constraints.getCenter()
			PointIcon(
				selected = false,
				point =
					Point(
						offset = Offset.Zero,
						action = Action.OpenNest(nest.id),
						id = -3
					),
				center = center,
				eraseColor = bgColor
			)
		}

		Spacer(5.dp)

		var tempState by rememberSaveable { mutableStateOf(nest.name) }
		var isEditing by rememberSaveable { mutableStateOf(false) }

		// Sync the text with external value changes (slider drag, programmatic updates).
		// The state must NOT be re-created on valueText change: the focus-interaction
		// collector below captures `onDone` once, and re-creating the state would make it
		// read an orphaned/stale value after the first commit.
		LaunchedEffect(nest.name) {
			if (!isEditing) tempState = nest.name
		}

		val focusManager = LocalFocusManager.current
		val interactionSource = rememberInteractionSource()
		val animatedIcon = rememberAnimatedIcon()

		fun onDone() {
			animatedIcon.setSuccess()
			focusManager.clearFocus(true)
			onEditName(tempState)
		}

		BackHandler(isEditing, onBack = ::onDone)

		LaunchedEffect(interactionSource) {
			interactionSource.interactions.collect { interaction ->
				when (interaction) {
					is FocusInteraction.Focus -> {
						isEditing = true
					}

					is FocusInteraction.Unfocus -> {
						onDone()
						isEditing = false
					}
				}
			}
		}

		TextField(
			value = tempState ?: "",
			onValueChange = { tempState = it },
			label = null,
			placeholder = { Text(stringResource(R.string.custom_name)) },
			colors =
				AppObjectsColors.outlinedTextFieldColors(
					removeBorder = true
				),
			shape = CircleShape,
			interactionSource = interactionSource,
			modifier = Modifier
				.weight(1f)
				.wrapContentSize(),
			singleLine = true,
			keyboardOptions =
				KeyboardOptions(
					capitalization = KeyboardCapitalization.Words,
					imeAction = ImeAction.Done
				),
			keyboardActions =
				KeyboardActions(
					onDone = { onDone() }
				),
			trailingIcon = {
				val showReset = !isEditing

				AnimatedContent(showReset) { showReset ->
					animatedIcon.Icon(
						defaultIcon = if (showReset) R.drawable.reset else R.drawable.check,
						successIcon = if (showReset) R.drawable.check else R.drawable.save,
						enabled = if (showReset) tempState != null else tempState != nest.name
					) {
						if (showReset) {
							tempState = null
							onEditName(null)

							animatedIcon.setSuccess()
							focusManager.clearFocus(true)
						} else {
							onDone()
						}
					}
				}
			}
		)

		Box {
			MoreIcon { showPopup = true }

			DragonDropDownMenu(
				expanded = showPopup,
				onDismissRequest = { showPopup = false }
			) {
				DropdownMenuGroup(MenuDefaults.groupShapes()) {
					DropdownMenuItem(
						text = {
							Text(stringResource(R.string.edit_nest))
						},
						onClick = {
							onEdit()
							showPopup = false
						},
						leadingIcon = {
							Icon(
								painter = painterResource(R.drawable.edit_rounded),
								contentDescription = stringResource(R.string.edit_nest)
							)
						}
					)

					DropdownMenuItem(
						text = {
							Text(stringResource(R.string.duplicate))
						},
						onClick = {
							onDuplicate()
							showPopup = false
						},
						leadingIcon = {
							Icon(
								painter = painterResource(R.drawable.copy),
								contentDescription = stringResource(R.string.duplicate)
							)
						}
					)

					if (nest.id != 0) {
						DropdownMenuItem(
							text = {
								Text(stringResource(R.string.delete_nest))
							},
							onClick = {
								showDeleteConfirmDialog = true
								showPopup = false
							},
							leadingIcon = {
								Icon(
									painter = painterResource(R.drawable.delete_forever),
									contentDescription = stringResource(R.string.delete_nest),
									tint = MaterialTheme.colorScheme.error
								)
							}
						)
					}
				}
			}
		}
	}

	if (showDeleteConfirmDialog) {
		DeleteNestConfirmDialog(
			nest = nest,
			onValidate = onDelete,
			onDismiss = { showDeleteConfirmDialog = false }
		)
	}
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun DeleteNestConfirmDialog(
	nest: Nest,
	onValidate: (deleteAllRelatedPoints: Boolean) -> Unit,
	onDismiss: () -> Unit
) {
	var deleteAllRelatedPoints by rememberSaveable { mutableStateOf(true) }

	AlertDialog(
		onDismissRequest = onDismiss,
		confirmButton = {
			ValidateCancelButtons(
				confirmText = stringResource(R.string.ok),
				cancelText = stringResource(R.string.cancel),
				onCancel = onDismiss,
				onConfirm = {
					onValidate(deleteAllRelatedPoints)
					onDismiss()
				}
			)
		},
		icon = {
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
					tint = MaterialTheme.colorScheme.onErrorContainer,
					modifier = Modifier.padding(5.dp)
				)
			}
		},
		title = {
			DialogTitle(
				text = stringResource(R.string.delete_nest),
				modifier =
					Modifier
						.fillMaxWidth()
						.padding(bottom = 4.dp)
						.clip(MaterialTheme.shapes.large)
						.background(MaterialTheme.colorScheme.surface)
						.padding(8.dp)
			)
		},
		text = {
			Column {
				DialogDescription(
					text = stringResource(R.string.are_you_sure_to_delete_the_nest, nest.name.takeIf { !it.isNullOrBlank() } ?: nest.id.toString()),
					modifier = Modifier.padding(top = 8.dp)
				)
				Spacer(15.dp)

				val interactionSource = rememberInteractionSource()
				Row(
					verticalAlignment = Alignment.CenterVertically,
					horizontalArrangement = Arrangement.spacedBy(8.dp),
					modifier = Modifier
						.clip(MaterialTheme.shapes.large)
						.clickable(interactionSource = interactionSource) { deleteAllRelatedPoints = !deleteAllRelatedPoints }
						.padding(5.dp)
				) {
					Checkbox(
						checked = deleteAllRelatedPoints,
						onCheckedChange = null,
						interactionSource = interactionSource,
						colors = AppObjectsColors.checkboxColors()
					)
					Text(
						text = stringResource(R.string.delete_all_related_points),
						style = MaterialTheme.typography.bodySmall,
						color = MaterialTheme.colorScheme.onSurface,
						modifier = Modifier.padding(end = 8.dp)
					)
				}
			}
		},
		containerColor = MaterialTheme.colorScheme.surface,
		tonalElevation = 6.dp,
		shape = MaterialTheme.shapes.large
	)
}

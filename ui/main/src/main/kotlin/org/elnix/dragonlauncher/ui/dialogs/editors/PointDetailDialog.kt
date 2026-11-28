package org.elnix.dragonlauncher.ui.dialogs.editors

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.elnix90.shared.ui.base.components.Spacer
import io.github.elnix90.shared.ui.dragon.components.DragonGroupScope
import io.github.elnix90.shared.ui.dragon.components.DragonModalBottomSheet
import io.github.elnix90.shared.ui.dragon.components.DragonSettingsGroup
import io.github.elnix90.shared.ui.dragon.text.DialogTitle
import io.github.elnix90.shared.ui.dragon.text.TextWithDescription
import org.elnix.dragonlauncher.base.model.serializables.Point
import org.elnix.dragonlauncher.base.theme.JetBrainsMono
import org.elnix.dragonlauncher.base.theme.LocalExtraColors
import org.elnix.dragonlauncher.i18n.R
import org.elnix.dragonlauncher.ktx.cleanString
import org.elnix.dragonlauncher.ktx.toHexWithAlpha
import org.elnix.dragonlauncher.models.PointsViewModel
import org.elnix.dragonlauncher.ui.actions.actionLabel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PointDetailDialog(
	point: Point,
	defaultPoint: Point,
	isDefaultEditing: Boolean,
	pointsViewModel: PointsViewModel,
	onDismissRequest: () -> Unit
) {
	val extraColors = LocalExtraColors.current
	val pointsService = pointsViewModel.pointsService
	val nest = pointsService.findNestById(point.nestId)
	val defaultNest = pointsService.defaultNest.value
	val defaultShape = pointsService.defaultIntersectionShape.value
	val shape =
		point.shapeId?.let { shapeId ->
			nest.getInterSectionShapes(defaultNest, isDefaultEditing).firstOrNull { it.id == shapeId }
		}

	DragonModalBottomSheet(onDismissRequest) {
		DialogTitle(stringResource(R.string.point_details))

		Spacer(8.dp)

		SelectionContainer {
			Column(
				modifier = Modifier
					.fillMaxWidth()
					.heightIn(max = 520.dp)
					.verticalScroll(rememberScrollState()),
				verticalArrangement = Arrangement.spacedBy(4.dp)
			) {
				DragonSettingsGroup(R.string.point_details_general) {
					PointDetailRow(stringResource(R.string.point_details_id), point.id)
					PointDetailRow(stringResource(R.string.point_detail_action), actionLabel(point.action))
					PointDetailRow(stringResource(R.string.point_detail_action_data), point.action.toString())
					point.customName?.let { PointDetailRow(stringResource(R.string.custom_name), it) }
					PointDetailRow(
						stringResource(R.string.point_details_nest_id),
						nest.name?.let { "$it (#${nest.id})" } ?: "#${nest.id}"
					)
					PointDetailRow(
						stringResource(R.string.point_details_shape_id),
						shape?.let {
							"#${it.id} (${it.getShape(defaultShape, isDefaultEditing).javaClass.simpleName})"
						} ?: point.shapeId?.let { "#$it" } ?: stringResource(R.string.point_details_free_position)
					)
					PointDetailRow(stringResource(R.string.point_details_offset), point.offset.cleanString())
					PointDetailRow(stringResource(R.string.point_details_position), point.getPos().cleanString())
				}
				DragonSettingsGroup(R.string.point_details_appearance) {
					point.customIcon?.let {
						PointDetailRow(stringResource(R.string.point_detail_custom_icon), it.toString())
					}
					point.iconProperties?.takeIf { it.isNotEmpty }?.let {
						PointDetailRow(stringResource(R.string.point_detail_icon_properties), it.toString())
					}
					PointDetailRow(stringResource(R.string.size), point.getSize(defaultPoint, isDefaultEditing))
					PointDetailRow(stringResource(R.string.inner_padding), point.getInnerPadding(defaultPoint, isDefaultEditing))
					PointDetailRow(
						stringResource(R.string.point_detail_border_shape),
						point.getBorderShape(false, defaultPoint, isDefaultEditing).javaClass.simpleName
					)
					PointDetailRow(
						stringResource(R.string.point_detail_selected_border_shape),
						point.getBorderShape(true, defaultPoint, isDefaultEditing).javaClass.simpleName
					)
					PointDetailRow(
						stringResource(R.string.border_stroke),
						point.getBorderStroke(false, defaultPoint, isDefaultEditing)
					)
					PointDetailRow(
						stringResource(R.string.border_stroke_selected),
						point.getBorderStroke(true, defaultPoint, isDefaultEditing)
					)
					PointDetailRow(
						stringResource(R.string.border_color),
						point.getBorderColor(false, defaultPoint, extraColors, isDefaultEditing)
					)
					PointDetailRow(
						stringResource(R.string.border_color_selected),
						point.getBorderColor(true, defaultPoint, extraColors, isDefaultEditing)
					)
					PointDetailRow(
						stringResource(R.string.background_color),
						point.getBackgroundColor(false, defaultPoint, isDefaultEditing)
					)
					PointDetailRow(
						stringResource(R.string.background_selected),
						point.getBackgroundColor(true, defaultPoint, isDefaultEditing)
					)
					PointDetailRow(
						stringResource(R.string.point_detail_glow),
						point.getGlow(false, defaultPoint, isDefaultEditing).toString()
					)
					PointDetailRow(
						stringResource(R.string.point_detail_selected_glow),
						point.getGlow(true, defaultPoint, isDefaultEditing).toString()
					)
					point.customActionColor?.let { PointDetailRow(stringResource(R.string.custom_action_color), it) }
				}
				DragonSettingsGroup(R.string.point_details_behavior) {
					PointDetailRow(
						stringResource(R.string.haptic_feedback),
						point.getHaptic(defaultPoint, isDefaultEditing)?.toString() ?: stringResource(R.string.default_text)
					)
					PointDetailRow(
						stringResource(R.string.fast_activation),
						point.getFastActivation(defaultPoint, isDefaultEditing)
					)

					point.liveNestTargetNestId?.let { targetNestId ->
						val targetNest = pointsService.findNestById(targetNestId)
						PointDetailRow(
							stringResource(R.string.live_nest_target_nest),
							targetNest.name?.let { "$it (#${targetNest.id})" } ?: "#${targetNest.id}"
						)
						PointDetailRow(
							stringResource(R.string.live_nest_hold_delay),
							"${point.getLiveNestPreviewDelayMs(defaultPoint, isDefaultEditing)} ms"
						)
						PointDetailRow(
							stringResource(R.string.live_nest_scale),
							point.getLiveNestScale(defaultPoint, isDefaultEditing)
						)
						PointDetailRow(
							stringResource(R.string.live_nest_grace_distance),
							point.getLiveNestGraceDistance(defaultPoint, isDefaultEditing)
						)
						PointDetailRow(
							stringResource(R.string.live_nest_sub_nest_opacity),
							"${point.getLiveNestMainNestOpacityPercent(defaultPoint, isDefaultEditing)}%"
						)
						PointDetailRow(
							stringResource(R.string.live_nest_snaps_to_finger_position),
							point.getLiveNestSnapsToFingerPosition(defaultPoint, isDefaultEditing)
						)
					}

					point.cycleActions?.let { stages ->
						PointDetailRow(stringResource(R.string.cycle_actions), stages.size)
						stages.forEachIndexed { index, stage ->
							PointDetailRow(
								stringResource(R.string.point_details_cycle_stage, index + 1, stage.triggerTimeMs),
								actionLabel(stage.action)
							)
							stage.hapticFeedback?.let {
								PointDetailRow(stringResource(R.string.point_detail_stage_haptic), it.toString())
							}
						}
						PointDetailRow(
							stringResource(R.string.cycle_actions_loop),
							point.getCycleActionsStageLoop(defaultPoint, isDefaultEditing)
						)
						PointDetailRow(
							stringResource(R.string.cycle_actions_loop_delay),
							"${point.getCycleActionsStageLoopDelayMs(defaultPoint, isDefaultEditing)} ms"
						)
					}

					point.holdAndRunDelayMs?.let {
						PointDetailRow(
							stringResource(R.string.hold_and_run_delay),
							"${point.getHoldAndRunDelayMs(defaultPoint, isDefaultEditing)} ms"
						)
					}
					point.holdAndRunAction?.let {
						PointDetailRow(stringResource(R.string.hold_and_run_custom_action), actionLabel(it))
					}
				}
			}
		}
	}
}

@Composable
private fun DragonGroupScope.PointDetailRow(label: String, value: String) {
	TextWithDescription(
		text = label,
		description = value,
		modifier = Modifier.dragonSettingGroup()
	)
}

@Composable
private fun DragonGroupScope.BasePointDetailRow(label: String, value: @Composable ColumnScope.() -> Unit) {
	Column(
		modifier = Modifier.dragonSettingGroup(),
		verticalArrangement = Arrangement.spacedBy(5.dp)
	) {
		Text(
			text = label,
			style = MaterialTheme.typography.titleMediumEmphasized
		)

		value()
	}
}

@Composable
private fun DragonGroupScope.PointDetailRow(label: String, value: Color) {
	BasePointDetailRow(label) {
		Row(
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.spacedBy(5.dp)
		) {
			Box(
				Modifier
					.size(10.dp)
					.background(value)
			)
			Text(
				text = value.toHexWithAlpha,
				style = MaterialTheme.typography.bodySmall
			)
		}
	}
}

@Composable
private fun DragonGroupScope.PointDetailRow(label: String, value: Boolean) {
	BasePointDetailRow(label) {
		Row(
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.spacedBy(5.dp)
		) {
			val (text, color) = if (value) {
				"true" to Color.Green
			} else {
				"false" to Color.Red
			}

			Text(
				text,
				style = MaterialTheme.typography.bodySmallEmphasized.copy(fontFamily = JetBrainsMono),
				color = color,
				modifier = Modifier
					.clip(MaterialTheme.shapes.small)
					.background(MaterialTheme.colorScheme.surfaceContainerLowest)
					.padding(3.dp)
			)
		}
	}
}

@Composable
private fun DragonGroupScope.PointDetailRow(label: String, value: Number) {
	BasePointDetailRow(label) {
		Row(
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.spacedBy(5.dp)
		) {
			Text(
				value.toString(),
				style = MaterialTheme.typography.bodySmallEmphasized.copy(fontFamily = JetBrainsMono),
				color = MaterialTheme.colorScheme.primary,
				modifier = Modifier
					.clip(MaterialTheme.shapes.small)
					.background(MaterialTheme.colorScheme.surfaceContainerLowest)
					.padding(3.dp)
			)
		}
	}
}

@Composable
private fun DragonGroupScope.PointDetailRow(label: String, value: Dp) {
	BasePointDetailRow(label) {
		Row(
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.spacedBy(5.dp)
		) {
			Text(
				text = value.toString(),
				style = MaterialTheme.typography.bodySmallEmphasized.copy(fontFamily = JetBrainsMono, fontWeight = FontWeight.Bold),
				color = MaterialTheme.colorScheme.secondary,
				modifier = Modifier
					.clip(MaterialTheme.shapes.small)
					.background(MaterialTheme.colorScheme.surfaceContainerLowest)
					.padding(3.dp)
			)
		}
	}
}

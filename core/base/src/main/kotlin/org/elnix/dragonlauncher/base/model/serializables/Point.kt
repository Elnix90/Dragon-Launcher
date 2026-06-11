package org.elnix.dragonlauncher.base.model.serializables

import androidx.compose.runtime.Immutable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.elnix.dragonlauncher.base.model.DragonJson
import java.util.UUID


@Immutable
@Serializable
@SerialName("Point")
data class Point(

    /** Index of the circle (ring) this swipe point belongs to. */
    var circleNumber: Int,

    /** Angular position in degrees (0–360), clockwise, relative to the circle center. */
    var angleDeg: Double,

    /** Optional action executed when the swipe point is triggered. */
    val action: Action,

    /** Stable unique identifier for persistence, diffing, and migrations. */
    val id: String,

    /** Optional nesting/group identifier for hierarchical or contextual swipe layouts. */
    var nestId: Int? = 0,

    /** Fully customizable icon definition overriding default visuals. */
    val customIcon: CustomIcon? = null,

    /** Border thickness (dp) when the swipe point is not selected. */
    val borderStroke: Float? = null,

    /** Border thickness (dp) when the swipe point is selected or active. */
    val borderStrokeSelected: Float? = null,

    /** Border color in ARGB format when not selected. */
    val borderColor: Int? = null,

    /** Background fill color (ARGB) in normal state. */
    val backgroundColor: Int? = null,

    /** Border color in ARGB format when selected. */
    val borderColorSelected: Int? = null,

    /** Background fill color (ARGB) in selected state. */
    val backgroundColorSelected: Int? = null,

    /** Global opacity multiplier (0.0 – 1.0) applied to the whole swipe point. */
    val opacity: Float? = null,

    /**
     * Fully customizable haptic feedback generator.
     * Stores the setting in a map of [Boolean] to [Int].
     * when the boolean is true, it indicates a vibration, and when off a pause.
     * the [Int] value is the duration of the vibration
     */
    val hapticFeedback: CustomHapticFeedback? = null,

    /** Optional user-defined display name (labels, accessibility, debug UI). */
    val customName: String? = null,

    /** Inner padding (dp) between border and content. */
    val innerPadding: Int? = null,

    /** Optional override for action color, default (null) will use the action color */
    val customActionColor: Int? = null,

    /** Optional size override */
    val size: Int? = null,

    /** Optional resolution override */
    val resolution: Int? = null,

    /**
     * Shape of the border icon, default is a circle
     */
    val borderShape: IconShape? = null,

    /**
     * Shape of the selected border icon, default is a circle
     */
    val borderShapeSelected: IconShape? = null,

    /**
     * Id of the [Nest] to render as a scaled overlay when this point is held.
     * Null means Live Nest is disabled for this point.
     */
    val liveNestTargetNestId: Int? = null,

    /**
     * How long (ms) the user must hold on this point before Live Nest activates.
     * Null falls back to a sensible default (500 ms) defined in the overlay controller.
     */
    val liveNestPreviewDelayMs: Int? = null,

    /**
     * Scale factor applied to the Live Nest radii, range 0.3–1.0.
     * Null defaults to 0.65.
     */
    val liveNestScale: Float? = null,

    /**
     * Extra tolerance radius (px) added beyond the outermost Live Nest ring before an
     * out-of-bounds exit is triggered. Prevents accidental dismissal when the finger
     * drifts slightly outside the circle.
     *  `null` / `0` means no grace (strict bounds).
     *  `-1` means no bounds (infinite drag)
     */
    val liveNestGraceDistancePx: Int? = null,

    /**
     * When non-null and Live Nest is open, the main nest layer is drawn at this opacity (0–100).
     * Null means the option is off (main nest stays fully opaque). Default when enabled is 50.
     */
    val liveNestMainNestOpacityPercent: Int? = null,

    /**
     * Whether if the live nest drawn should have its center exactly where it got activated after the timeout, or if it snaps to its host point position
     */
    val liveNestSnapsToFingerPosition: Boolean? = null,

    /**
     * Ordered list of extra timed stages for Cycle Actions.
     * Null means Cycle Actions is disabled for this point.
     *
     * Stage 0 is always the point's own [action] (base, no threshold).
     * Each entry is Stage[1..N]: [CycleActionStage.triggerTimeMs] is the **additional** hold time
     * after the previous stage (or after finger-down for Stage 1) before that stage becomes current.
     */
    val cycleActions: List<CycleActionStage>? = null,

    /**
     * The default delay that is used to wait between stages
     */
    val cycleActionStageDefaultDelay: Int? = null,

    /**
     * Milliseconds to wait in the "Loop Over" phase before the cycle restarts.
     * When null, the actions doesn't loop; -1 = No loop
     */
    val cycleActionsLoopDelayMs: Int? = null,

    /**
     * Milliseconds of continuous hold after which [action] fires automatically, without release.
     * Null means Hold & Run is disabled for this point.
     *
     * When set, the gesture is consumed as soon as the delay elapses; releasing the finger
     * afterwards does not trigger any additional launch.
     */
    val holdAndRunDelayMs: Int? = null,

    /**
     * When non-null, Hold & Run runs this action instead of the point’s main [action].
     * Null means the same action as tap/release (default).
     */
    val holdAndRunAction: Action? = null

) {

    val key: CacheKey = CacheKey(this)

    override fun toString(): String = this.id.substring(0, 8)

    companion object {
        fun dummySwipePoint(
            action: Action? = null,
            id: String? = null
        ) =
            Point(
                circleNumber = 0,
                angleDeg = 0.0,
                action = action ?: Action.OpenDragonLauncherSettings(),
                id = id ?: UUID.randomUUID().toString(),
                nestId = 0
            )

        val defaultSwipePointsValues = dummySwipePoint(null, "defaultPoint").copy(
            borderStroke = 4f,
            borderStrokeSelected = 8f,
            opacity = 1f,
            innerPadding = 5,
            size = 22,
            borderShape = IconShape.Circle,
            borderShapeSelected = IconShape.Circle,
            liveNestPreviewDelayMs = 500,
            liveNestScale = 0.65f,
            liveNestGraceDistancePx = 50,
            liveNestSnapsToFingerPosition = true,
            holdAndRunDelayMs = 500,
            cycleActionsLoopDelayMs = 500,
            cycleActionStageDefaultDelay = 500,
            liveNestMainNestOpacityPercent = 50
        )

        fun Point.applyColorAction(): Boolean = (
                action !is Action.LaunchApp &&
                        action !is Action.LaunchShortcut &&
                        action !is Action.OpenDragonLauncherSettings
                ) &&

                // Don't draw tint over custom icon
                customIcon == null


        object PointsListJson: DragonJson<List<Point>>()
        object PointsJson: DragonJson<Point>()
    }
}
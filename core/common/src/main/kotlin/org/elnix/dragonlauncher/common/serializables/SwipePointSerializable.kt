package org.elnix.dragonlauncher.common.serializables

import androidx.compose.runtime.Immutable
import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable
import org.elnix.dragonlauncher.common.serializables.SwipeActionSerializable.OpenDragonLauncherSettings
import java.util.UUID

/*  ─────────────  Cycle Actions model  ─────────────  */

/**
 * One timed stage in a Cycle Actions sequence.
 *
 * Stage 0 is implicit (the point's base action, 0 ms). Stages 1..N are stored in
 * [SwipePointSerializable.cycleActions] and evaluated continuously during a hold to
 * determine which action fires on release.
 *
 * @param triggerTimeMs Extra milliseconds to hold **after the previous stage** before this stage
 *   becomes current (after finger-down for Stage 1). The runtime sums these into absolute thresholds.
 * @param action        Action executed on release while this stage is active.
 * @param hapticFeedback Haptic pulse played once when transitioning into this stage.
 *                      Null falls back to the point's own haptic setting.
 */
@Serializable
data class CycleActionStage(
    val triggerTimeMs: Int,
    val action: SwipeActionSerializable,
    val hapticFeedback: CustomHapticFeedbackSerializable? = null
)

/**
 * Serializable model representing a single swipe point on a radial / circular UI.
 *
 * This object is intentionally compact and fully nullable-extensible to allow
 * backward-compatible evolution of visual, behavioral, and interaction features.
 *
 * All visual values are interpreted by the rendering layer (Compose / Canvas / View).
 *
 * DO NOT PUT [SerializedName] decoration above new parameters, they are only for legacy support in Gson (the old storage system)
 */
@Immutable
@Serializable
data class SwipePointSerializable(

    /** Index of the circle (ring) this swipe point belongs to. */
    @SerializedName("a")
    var circleNumber: Int,

    /** Angular position in degrees (0–360), clockwise, relative to the circle center. */
    @SerializedName("b")
    var angleDeg: Double,

    /** Optional action executed when the swipe point is triggered. */
    @SerializedName("c")
    val action: SwipeActionSerializable,

    /** Stable unique identifier for persistence, diffing, and migrations. */
    @SerializedName("d")
    val id: String,

    /** Optional nesting/group identifier for hierarchical or contextual swipe layouts. */
    @SerializedName("e")
    var nestId: Int? = 0,

    /** Fully customizable icon definition overriding default visuals. */
    @SerializedName("f")
    val customIcon: CustomIconSerializable? = null,

    /** Border thickness (dp) when the swipe point is not selected. */
    @SerializedName("g")
    val borderStroke: Float? = null,

    /** Border thickness (dp) when the swipe point is selected or active. */
    @SerializedName("h")
    val borderStrokeSelected: Float? = null,

    /** Border color in ARGB format when not selected. */
    @SerializedName("i")
    val borderColor: Int? = null,

    /** Background fill color (ARGB) in normal state. */
    @SerializedName("k")
    val backgroundColor: Int? = null,

    /** Border color in ARGB format when selected. */
    @SerializedName("j")
    val borderColorSelected: Int? = null,

    /** Background fill color (ARGB) in selected state. */
    @SerializedName("l")
    val backgroundColorSelected: Int? = null,

    /** Global opacity multiplier (0.0 – 1.0) applied to the whole swipe point. */
    @SerializedName("m")
    val opacity: Float? = null,

    /**
     * Fully customizable haptic feedback generator.
     * Stores the setting in a map of [Boolean] to [Int].
     * when the boolean is true, it indicates a vibration, and when off a pause.
     * the [Int] value is the duration of the vibration
     */
    // No Serialized name, as it was the previous version, and it's a new setting
    val hapticFeedback: CustomHapticFeedbackSerializable? = null,


    /** Optional user-defined display name (labels, accessibility, debug UI). */
    @SerializedName("o")
    val customName: String? = null,

    /** Per-corner radius definition for the swipe point container. */
    @SerializedName("p")
    val cornerRadius: CornerRadiusSerializable? = null,

    /** Inner padding (dp) between border and content. */
    @SerializedName("q")
    val innerPadding: Int? = null,

    /** Optional override for action color, default (null) will use the action color */
    @SerializedName("r")
    val customActionColor: Int? = null,

    /** Optional size override */
    @SerializedName("s")
    val size: Int? = null,

    /** Optional resolution override */
    val resolution: Int? = null,


    /**
     * Shape of the border icon, default is a circle
     */
    @SerializedName("borderShape")
    val borderShape: IconShape? = null,

    /**
     * Shape of the selected border icon, default is a circle
     */
    @SerializedName("borderShapeSelected")
    val borderShapeSelected: IconShape? = null,

    /*  ─────────────  Live Nest configuration  ─────────────  */

    /**
     * Id of the [CircleNest] to render as a scaled overlay when this point is held.
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
     * When enabled, a sharp angle in the user's drag while hovering this point
     * immediately enters the nested nest instead of waiting for the hold delay.
     */
    val fastActivation: Boolean? = null,

    /*  ─────────────  Cycle Actions configuration  ─────────────  */

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
     * When null, the actions doesn't loop
     */
    val cycleActionsLoopDelayMs: Int? = null,

    /*  ─────────────  Hold & Run configuration  ─────────────  */

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
    val holdAndRunAction: SwipeActionSerializable? = null,

) {

    companion object {
        fun dummySwipePoint(
            action: SwipeActionSerializable? = null,
            id: String? = null
        ) =
            SwipePointSerializable(
                circleNumber = 0,
                angleDeg = 0.0,
                action = action ?: OpenDragonLauncherSettings(),
                id = id ?: UUID.randomUUID().toString(),
                nestId = 0
            )

        val defaultSwipePointsValues = dummySwipePoint(null).copy(
            borderStroke = 4f,
            borderStrokeSelected = 8f,
            opacity = 1f,
            cornerRadius = null,
            innerPadding = 5,
            size = 22,
//            resolution = 48, // No default resolution, it uses the size instead
            borderShape = IconShape.Circle,
            borderShapeSelected = IconShape.Circle,
            liveNestPreviewDelayMs = 500,
            liveNestScale = 0.65f,
            liveNestGraceDistancePx = 50,
            liveNestSnapsToFingerPosition = true,
            fastActivation = false,
            holdAndRunDelayMs = 500,
            cycleActionsLoopDelayMs = 500, // -1 = No loop
            cycleActionStageDefaultDelay = 500,
            liveNestMainNestOpacityPercent = 50
        )
    }

}

fun SwipePointSerializable.applyColorAction(): Boolean = (
            action !is SwipeActionSerializable.LaunchApp &&
            action !is SwipeActionSerializable.LaunchShortcut &&
            action !is OpenDragonLauncherSettings
    ) &&
    // Checks if the source is null (no custom icons, image, text or pack)
    // to avoid drawing the tint on the rendered icon
    customIcon?.type == null

# Changes

## Feature: Open root nest each time

A global toggle that resets the live nest navigation back to the root nest on every new gesture, so the user always starts from the root regardless of which nest they were in before.

### Files

#### Settings

- **`core/settings/.../stores/BehaviorSettingsStore.kt`**
  — Added `openRootNestEachTime` boolean setting (default `false`, key `"openRootNestEachTime"`).

- **`core/ui/.../settings/customization/BehaviorTab.kt`**
  — Added `SettingsSwitchRow` for the toggle.

- **`core/common/.../res/values/strings.xml`**
  — Added `open_root_nest_each_time` and `open_root_nest_each_time_desc` strings.

#### Functionality

- **`core/ui/.../ui/MainScreen.kt`**
  — On finger-down (`start = down.position`), calls `nestNavigation.clearStack()` when the setting is enabled.
  
  — Removed `nestId` from the `pointerInput(Unit, nestId)` key. `nestId` changed mid-gesture when `clearStack()` ran, which cancelled and restarted the pointer scope — the fresh scope saw a MOVE event (not DOWN) and silently dropped the gesture. The handler reads all reactive state fresh per iteration, so the key was unnecessary.


---

## Feature: Fast activation (sharp-turn live nest entry)

A live-nest toggle. When enabled, a sharp change in the user's drag direction while hovering the live nest point immediately opens the nested nest, without waiting for the full hold delay.

### Files

#### Settings

- **`core/ui/.../dialogs/EditPointSheet.kt`**
  — Added a `SwitchRow` for `fastActivation` under the live nest settings section (after the "Snaps to finger position" toggle).

- **`core/common/.../serializables/SwipePointSerializable.kt`**
  — Added `fastActivation: Boolean? = null` field (default `false` in `defaultSwipePointsValues`).

- **`core/common/.../res/values/strings.xml`**
  — Strings `fast_activation` / `fast_activation_desc` kept; semantics unchanged (the description already described per-point behavior).

#### Functionality

- **`core/ui/.../remembers/rememberLiveNestController.kt`**
  — **Position tracking:** Uses a `SideEffect` to sync `current` into a reactive `currentSnap` state, then a `LaunchedEffect(Unit)` polling loop at 16 ms reads `currentSnap`, applies the jitter filter, fills the buffer, and bumps `angleVersion`.
  — **`hasSharpAngle` derived state:** Reads `angleVersion`, then computes the turn angle from the two buffer halves and the stroke displacement. Returns `true` only when both exceed their thresholds.
  — **Hold timer:** Resolves `pointFastActivation` from the per-point setting (`currentPoint.fastActivation ?: defaultPoint.fastActivation ?: defaultSwipePointsValues.fastActivation!!`) and checks `hasSharpAngle.value` every 32 ms. If true, breaks the delay early and opens the nested nest.
  — **Early version update:** Added `angleVersion++` after `recentPositions.clear()` (gesture end) so the derived state cache is invalidated immediately.


# The history editor on the shared UI (KMP step 7, detail PR 4c)

## Objetivo
Move the content of the history editor (the calendar of a habit in a dialog) to `salem-ui`, with a layer that shows it over the detail screen, so iOS can use it in PR 4d.

## Por qué
In the detail screen the History card has an Edit button that opens a calendar where each day opens a checkmark or number popup. On Android that was a Fragment (`HistoryEditorDialog`) that drew the chart with the Android chart host; iOS needs the same thing from shared code.

## Alcance
- In: `HistoryEditorContent` (the calendar over a `Scrim`), `historyEditorHeight`, `HistoryEditorLayer` (state: who hears the taps) and `HistoryEditorHost` (draws the layer, back closes it) in `salem-ui` `ui.detail`; `HistoryEditorDialog` keeps its Fragment (lifecycle, listener after a recreation, its own refresh on commands) and draws the shared content; Android's own `CoreChart` and `CoreView` (no other users) and the `history_editor_max_height` dimension are deleted.
- Out (4d): the iOS wiring that composes the detail screen, this layer and the popups of `ListOverlayHost`.
- Decisions (user): odd-full, 4 stacked PRs, TDD where there is logic, no emulator/simulator without asking.

## Exploración
The popup of a day (checkmark or number) opens from the editor and must sit above it. `ListOverlays` has one slot, so a popup would replace the editor: this is why the plan changed from "a new overlay kind" to a layer of its own. The state of the editor is the `history` of the detail state (same presenter), so no extra refresh code is needed on iOS.

## Incertidumbre
- Height: Android used `screenHeightDp`; the shared content uses the height that `BoxWithConstraints` gives inside the scrim (the window minus the safe areas, at most 350 dp). On a short window this is a bit smaller.
- The tap on a day was tested through the listener (a tap in the middle of the calendar reaches it once), not for a particular date.
- Real gestures, back on a device and the look of the calendar were not checked.

## Checklist
- [x] TASK-1: `HistoryEditorLayerTest` (4), `HistoryEditorHostTest` (7) and the moved `HistoryEditorHeightTest` (2) — criterio: RED observed
- [x] TASK-2: `HistoryEditorContent`, `HistoryEditorLayer` and `HistoryEditorHost`
- [x] TASK-3: `HistoryEditorDialog` over the shared content; Android `CoreChart`/`CoreView` and the dimension deleted
- [x] TASK-4: docs and changelog

## Estrategia de entrega
Stacked PRs 4a to 4d; this one is `feat/ui-history-editor`, from `develop` after #65. The user merges in order. Commits only when the user asks.

## Evidencia
`:salem-core:jvmTest`, `:salem-ui:desktopTest` (all), `:salem-android:testDebugUnitTest`, `ktlintCheck`, `compileDebugAndroidTestKotlin`, `assembleDebug`, iOS compile (arm64 and simulator) and `xcodebuild` (build only). Not run: the simulator, any emulator. To check by hand on Android: the Edit button of the History card, taps and long presses on days (popups above the editor), scrolling the calendar, tap outside to close, a short window (landscape) and rotation with the editor open.

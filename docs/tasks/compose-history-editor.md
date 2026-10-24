# Habit detail: the history editor dialog in Compose (KMP step 6i)

## Objetivo
Move `HistoryEditorDialog`, the last dialog of the habit detail screen that was made of Views, to Compose.

## Por qué
Step 6 of `.specs/migration-kmp.md`. The number, checkmark and delete dialogs were already `ComposeDialogFragment`s; the history editor was a plain `Dialog` that held a `HistoryChart` of `salem-core` in an `AndroidDataView` (a `Scroller` and a gesture detector). `CoreChart` and `ChartScrollState` already do that for the calendar card.

## Alcance
- In: `HistoryEditorDialog` as a `ComposeDialogFragment` with `CoreChart`, a scrim that dismisses on a tap outside, the height up to `history_editor_max_height` (`historyEditorHeight`, tested), the refresh after every command with the real theme; `ShowHabitActivity` dismisses an older editor before showing a new one; removal of the unused `AndroidDataView.resetDataOffset()`.
- Out: `AndroidDataView` and `AndroidView` (the history widget uses them), the other dialogs, the presenters in core.
- Decisions (user): **no emulator or device runs without asking**.

## Exploración
- The editor is given a listener by the activity, which gives it again after a recreation, so unlike the small popups it is not dismissed when restored.
- The old refresh used `LightTheme()` after the first change (the chart was created with the theme of the app); the new one uses the theme of the app every time.
- A tap on a day goes to `HistoryCardPresenter`, which opens the number or checkmark popup or toggles the day; nothing changed in core.

## Incertidumbre
- Nothing was run on a device: the look of the dialog (width, corners, background), the drag and fling of the calendar, the taps and the refresh after a change, rotation, dark theme.

## Checklist
- [x] TASK-1: `historyEditorHeight` with a test — criterio: `HistoryEditorDialogTest` passes
- [x] TASK-2: Compose `HistoryEditorDialog` — criterio: assembles
- [x] TASK-3: dismiss an older editor in `ShowHabitActivity`; remove the dead `resetDataOffset` — criterio: assembles
- [x] TASK-4: CHANGELOG, PENDING, AGENTS.md — criterio: docs updated; ktlint and unit tests green

## Estrategia de entrega
`single-pr`, branch `feat/compose-history-editor`, base `develop`. Commits only when the user asks.

## Evidencia
- `ktlintCheck`, `:salem-android:testDebugUnitTest`, `assembleDebug`, `assembleRelease`, `:salem-core:jvmTest` and `compileDebugAndroidTestKotlin` pass.
- Nothing was run on an emulator or a device.

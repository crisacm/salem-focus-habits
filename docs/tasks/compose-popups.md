# Habit popups in Compose (KMP step 6e, part 1)

## Objetivo
Move `ConfirmDeleteDialog`, `CheckmarkDialog` and `NumberDialog` (the popups opened from the habit list and the habit detail) to Jetpack Compose, with the same public API and behaviour.

## Por qué
Step 6 of `.specs/migration-kmp.md`. They follow the `ComposeDialogFragment` pattern of the previous dialogs and remove `checkmark_popup.xml` and its styles. Part 2 of step 6e will migrate the frequency picker and the inline lists of `EditHabitActivity`.

## Alcance
- In: the three dialogs, pure helpers with tests (checkmark options, notes-on-dismiss rule, number field formatting and parsing, input filter), removal of `checkmark_popup.xml`, its styles and drawables, the unused `number_picker_dialog.xml` and the dead `ShowHabitActivity.getPopupAnchor`, instrumented steps that click "Yes" in the delete confirmation.
- Out: `HistoryEditorDialog` (it hosts a canvas chart, to be migrated with the charts), `FrequencyPickerDialog` and the inline lists (part 2), the vendored `TimePickerDialog`.
- Decisions (user): two PRs, odd-full; keep that closing `CheckmarkDialog` or `NumberDialog` without choosing saves changed notes with the original value.

## Exploración
- `CheckmarkDialog` (102 lines) and `NumberDialog` (142 lines) are `AppCompatDialogFragment`s with a plain `Dialog` and the shared layout `checkmark_popup.xml`. Arguments: `color`, `value`, `notes`. Callbacks: `onToggle(Int, String)` / `onToggle(Double, String)` and `onDismiss`. Callers: `ShowHabitActivity` and `ListHabitsScreen`, shown with `dismissCurrentAndShow(fm, tag)`.
- Checkmark values: `YES_MANUAL 2`, `NO 0`, `SKIP 3` (only with `isSkipEnabled`), `UNKNOWN -1` (only with `areQuestionMarksEnabled`); buttons use FontAwesome glyphs (`fa_check`, `fa_skipped`, `fa_times`, `fa_question`) with the habit colour or `contrast60`.
- `NumberDialog`: decimal field limited to digits and the locale separator (text keyboard for SwiftKey and Samsung), initial text `0` below 0.01 else `#.##`, Enter saves, Save, Skip (writes 0.003) and Unknown (writes -0.001) buttons, empty text means unknown, a parse error keeps the original value, notes field whose action saves; the keyboard is opened on show.
- `ConfirmDeleteDialog` (49 lines) is an `AlertDialog` built with a context (not a fragment): plural title and message, Yes and No.
- Listeners are not saved by any of them; `ShowHabitActivity` and `ListHabitsActivity` call `dismissCurrentDialog()` on pause.
- Tests: no instrumented test opens Number or Checkmark; `HabitsTest`, `BackupTest` and `ListHabitsRegressionTest` click "Yes" in the delete confirmation with Espresso.

## Incertidumbre
- Keyboard and focus behaviour (the old code simulates a touch to open the keyboard), the IME covering the card and the FontAwesome rendering can only be judged on a device.

## Checklist
- [x] TASK-1: task doc — criterio: this file committed
- [x] TASK-2: pure helpers with tests first — criterio: JVM tests pass
- [x] TASK-3: `ConfirmDeleteDialog` as a `ComposeDialogFragment` and its callers — criterio: assembles
- [x] TASK-4: `CheckmarkDialog` in Compose — criterio: assembles
- [x] TASK-5: `NumberDialog` in Compose — criterio: assembles
- [x] TASK-6: remove old layout, styles, drawables and dead code; port the instrumented steps — criterio: no references left, `compileDebugAndroidTestKotlin` passes
- [x] TASK-7: CHANGELOG, PENDING, AGENTS.md — criterio: docs updated; ktlint, unit tests, core tests green

## Estrategia de entrega
`single-pr`, branch `feat/compose-popups`, base `develop`. Commits only when the user asks.

## Evidencia
- `assembleDebug`, `assembleRelease`, `compileDebugAndroidTestKotlin`, `ktlintCheck`, `:salem-android:testDebugUnitTest` (12 new tests in `PopupModelsTest`) and `:salem-core:jvmTest` pass.
- Removed: `checkmark_popup.xml`, the `CheckmarkPopupBtn` and `NumericalPopupBtn` styles, the two popup drawables, the unreferenced `number_picker_dialog.xml` and the dead `ShowHabitActivity.getPopupAnchor`.
- New strings: `unknown` (accessibility label of the Unknown button; English only). The old popup buttons had no labels.
- `ConfirmDeleteDialog` is now a fragment: `ConfirmDeleteDialog.newInstance(quantity, callback)`, shown with `dismissCurrentAndShow(fragmentManager, tag)`. The three popups dismiss themselves when restored because their callbacks are not saved.
- Not run: any popup on a device (keyboard opening and focus, the IME covering the card, FontAwesome glyphs, colours, rotation, long press and short press on checkmarks) or the instrumented tests.
- Review fixes (rdd-lite): the popup card is limited to 320 dp (`fillMaxWidth` inside a wrap-content card made it as wide as the screen) and the initial focus request of the number field is wrapped in `runCatching`. Left as they are: no dividers between the buttons of the number row, `isDigit()` accepting digits of other scripts, and tests covering only the pure helpers.

# Frequency picker and edit lists in Compose (KMP step 6e, part 2)

## Objetivo
Move `FrequencyPickerDialog` and the two inline `AlertDialog` lists of `EditHabitActivity` (target type and numerical frequency) to Jetpack Compose, keeping the rules that turn the user input into a `Frequency`.

## Por qué
Step 6 of `.specs/migration-kmp.md`. It follows the popups (#33). The frequency rules had no test and are now extracted to pure functions that pin the current behaviour.

## Alcance
- In: `FrequencyPickerDialog` (same constructor, no-arg constructor and `onFrequencyPicked`), a reusable `ChoiceDialog` for the two inline lists, pure `FrequencyModel` with tests (input to `(num, den)`, `(num, den)` to input, label of a frequency), removal of `frequency_picker_dialog.xml` and the unused input box drawable, instrumented frequency steps ported to UiAutomator.
- Out: `HistoryEditorDialog` (with the charts), the vendored `TimePickerDialog`, changing the silent reset to "every day" (kept and tested), saving `targetType` across rotation (noted in `docs/PENDING.md`).
- Decisions (user): two PRs, odd-full; keep the frequency rules and test them.

## Exploración
- `FrequencyPickerDialog` (233 lines + 161 of layout): five radios (every day; every X days; X times per week; X times per month; X times in Y days) with number fields (max 3, 1, 2, 3+3 digits); focusing a field checks its radio; the fields that are not selected keep the layout defaults 3, 3, 10, 3 and 14; the dialog has only a Save button and `onResume` rebuilds the views from the constructor values.
- Save rules: every day gives 1/1; every X days gives 1/X (empty keeps 1); X per week gives X/7; X per month gives X/30; X in Y days needs both fields; empty fields give 1/1; then `X >= Y` or `X < 1` resets to 1/1 (so "every 1 day" and "every 0 days" are every day).
- Reopening: denominator 30 or 31 is "per month" (also for 1/30), otherwise numerator 1 is every day (den 1) or every X days, otherwise den 7 is per week, else X in Y days.
- `EditHabitActivity`: `formatFrequency(num, den, resources)` builds the label; `targetTypePicker` and `numericalFrequencyPicker` build `AlertDialog`s (the second is shown with `builder.show()` and is not tracked); on creation it dismisses every restored fragment; `targetType` is not saved across rotation.
- Tests: `EditHabitSteps.pickFrequency`, `pickMonthFrequency` and `pickDailyFrequency` use the ids of the old layout and the uppercase "SAVE" (the AlertDialog button style); `HabitsTest` and `ListHabitsRegressionTest` use them through `CommonSteps`.

## Incertidumbre
- Field focus and keyboard behaviour inside the dialog (the old code selects the text on focus) can only be judged on a device.

## Checklist
- [x] TASK-1: task doc — criterio: this file committed
- [x] TASK-2: `FrequencyModel` with tests first — criterio: JVM tests pass
- [x] TASK-3: `ChoiceDialog` and the two lists of `EditHabitActivity` — criterio: assembles
- [x] TASK-4: `FrequencyPickerDialog` in Compose, `formatFrequency` on the model — criterio: assembles
- [x] TASK-5: remove the old layout and port the instrumented steps — criterio: no references left, `compileDebugAndroidTestKotlin` passes
- [x] TASK-6: CHANGELOG, PENDING, AGENTS.md — criterio: docs updated; ktlint, unit tests, core tests green

## Estrategia de entrega
`single-pr`, branch `feat/compose-frequency`, base `develop`. Commits only when the user asks.

## Evidencia
- `assembleDebug`, `assembleRelease`, `compileDebugAndroidTestKotlin`, `ktlintCheck`, `:salem-android:testDebugUnitTest` (13 new tests in `FrequencyModelTest`) and `:salem-core:jvmTest` pass.
- The rules are now `frequencyFromInput` / `initialFrequencyInput` / `frequencyLabel` (`FrequencyModel.kt`); `formatFrequency` maps the label to the strings. Tests pin the current behaviour, including that 1/30 is reopened as "once per month" and that "every 1 day" saves every day.
- Removed: `frequency_picker_dialog.xml` and the `bg_input_box` drawable. `DialogSurface` takes an optional title (null for the frequency and option dialogs).
- `numericalFrequencyPicker` is now tracked with `dismissCurrentAndShow` (it used `builder.show()`).
- Instrumented steps: `pickFrequency`, `pickMonthFrequency` and `pickDailyFrequency` click the Compose texts with UiAutomator and a case-insensitive `Save` (the AlertDialog showed `SAVE`); the month field is found as an `EditText` with the default text `10`. They compile but were not run.
- Not run: the dialog on a device (focus and keyboard on the number fields, selecting a radio by tapping the text, the 320 dp-free layout width, rotation) or the instrumented tests.
- Review fixes (rdd-lite): the rows of the picker are a `FlowRow` with a minimum height, so the text and the fields wrap with large fonts or long translations; `pickDailyFrequency` clicks the last "Every day" (`clickLastComposeText`) because the edit screen behind the dialog shows the same label. Left: radios are not in a `selectableGroup`, the number fields have no label of their own, and the dialog has not been seen on a device.

# Create and edit habit screen in Compose (KMP step 6f)

## Objetivo
Move `EditHabitActivity` and its layout to Jetpack Compose, with the form state and its validation in `salem-core`, keeping the intent contract and the way a habit is saved.

## Por qué
Step 6 of `.specs/migration-kmp.md`. All the dialogs this screen opens are already in Compose (#29-#34). The form logic (validation, filling a `Habit`) had no tests and lived in the activity; in core it can be shared with iOS later.

## Alcance
- In: `EditHabitForm` in core (state, validation, `applyTo`, `from`) with tests; Compose screen and a `Saver` for the form; `EditHabitActivity` hosting it; `testTag`s exposed as resource ids for UiAutomator; removal of `activity_edit_habit.xml`, the `Form*` styles and helpers left unused; instrumented steps ported.
- Out: the vendored `TimePickerDialog` (still Views, also used by snooze), `ShowHabitActivity`, the list, a prompt for unsaved changes (none today), the intent contract.
- Decisions (user): logic in `salem-core`; fix the small bugs with tests (`targetType` and `unit` lost on rotation, blank name accepted, `toDouble()` crash); keep the vendored time picker; one PR, odd-full.

## Exploración
- `EditHabitActivity` (374 lines) holds the state (`habitId`, `habitType`, `unit`, `color`, `freqNum/freqDen`, reminder hour/minute/days, `targetType`), loads a habit from the `habitId` extra or a type from `habitType`, and saves with `modelFactory.buildHabit()` + `copyFrom` + `EditHabitCommand` / `CreateHabitCommand` through `commandRunner`, then calls `finish()` at once (the write is asynchronous).
- Validation: name `isEmpty()` (no trim) and, for numerical habits, target `isEmpty()`; the target is read with `toDouble()` (can throw). Rotation restores only some fields; `targetType` and `unit` are lost. `habitList.getById(habitId)!!` and an unchecked cast to `DialogFragment` can crash.
- Layout `activity_edit_habit.xml` (272 lines): toolbar with a Save button, a scroll of boxes (name, colour, question, frequency, unit, target, target type, numerical frequency, reminder time and days, notes) with `Form*` styles; ids used by tests (`nameInput`, `questionInput`, `notesInput`, `colorButton`, `boolean_frequency_picker`, `reminderTimePicker`, `reminderDatePicker`, `buttonSave`).
- Launch points: `HabitTypeDialog` (create), `ListHabitsScreen.showEditHabitsScreen` and `ShowHabitActivity.showEditHabitScreen` (edit) through `IntentFactory.startEditActivity`. The manifest entry is `exported=true` with the legacy `PARENT_ACTIVITY`.
- Tests: `EditHabitSteps`, `HabitsTest`, `ListHabitsRegressionTest` and `CommonSteps.verifyShowsScreen(EDIT_HABIT)` (anchored on `R.id.questionInput`) use Espresso ids that disappear with Compose.

## Incertidumbre
- Keyboard and focus, reaching Save with the keyboard open, the toolbar colour in light and dark, and edge-to-edge insets can only be judged on a device.
- Whether `testTagsAsResourceId` exposes the tags to UiAutomator as expected cannot be confirmed here.

## Checklist
- [x] TASK-1: task doc — criterio: this file committed
- [x] TASK-2: `EditHabitForm` in core with tests first — criterio: `:salem-core:jvmTest` passes
- [x] TASK-3: `Saver` for the form with a round-trip test — criterio: JVM test passes
- [x] TASK-4: Compose screen and `EditHabitActivity` — criterio: assembles
- [x] TASK-5: remove the old layout, styles and helpers; port the instrumented steps — criterio: no references left, `compileDebugAndroidTestKotlin` passes
- [x] TASK-6: CHANGELOG, PENDING, AGENTS.md — criterio: docs updated; ktlint, unit tests, core tests green

## Estrategia de entrega
`single-pr`, branch `feat/compose-edit-habit`, base `develop`. Commits only when the user asks.

## Evidencia
- `assembleDebug`, `assembleRelease`, `compileDebugAndroidTestKotlin`, `ktlintCheck`, `:salem-core:jvmTest` (11 new tests in `EditHabitFormTest`) and `:salem-android:testDebugUnitTest` (2 new in `EditHabitFormSaverTest`) pass.
- The form (`EditHabitForm`) is in `salem-core`: `from(habit)`, `new(type)`, `validate()`, `applyTo(habit)`, `clearReminder()`. Fixed with tests: a blank name (spaces only) is rejected, a target that is not a number ("." or text) shows the blank error instead of crashing, a comma is accepted as the decimal separator, and `targetType` and `unit` survive a rotation (the whole form is saved by `EditHabitFormSaver` through `rememberSaveable`).
- `EditHabitActivity` keeps the intent contract (`habitId`, `habitType`) and the saving path (`buildHabit` + `copyFrom` + the commands, then `finish()`). It finishes if the habit to edit no longer exists, and dismisses restored dialog fragments without the unsafe cast.
- Removed: `activity_edit_habit.xml`. The `Form*` styles stay because `automation.xml` uses them.
- Instrumented steps find the fields by `testTag` exposed as resource ids (`testTagsAsResourceId`): `clickTag`, `setTagText`, and the `EDIT_HABIT` anchor waits for `questionInput`. They compile but were not run; that the tags reach UiAutomator as expected is unverified.
- Not run: the screen on a device (keyboard and focus, reaching Save with the keyboard open, bar colour in light and dark, insets, rotation, the vendored time picker over it) or the instrumented tests.
- Review fixes (rdd-full, one independent reviewer, Risk and Reliability): the target must be a finite number (NaN, infinity and 1e999 are rejected, with tests); Save is ignored and disabled once a valid form was sent, so a double tap cannot create two habits (`saving` in the activity and in the screen); the content column uses `consumeWindowInsets(padding)` and `imePadding()` before the scroll and no `navigationBarsPadding()`, to avoid a doubled bottom inset (to be checked on a device). Left in `docs/PENDING.md`: status bar icon contrast, accessibility roles and descriptions, stacked unit and target fields, no IME action, validation errors lost on rotation, emoji cut by the name limit, stale lint baseline entries, and no visible feedback if a save fails.

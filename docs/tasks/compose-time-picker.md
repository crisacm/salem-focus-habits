# Time picker: Material 3 dialog instead of the vendored AOSP picker (KMP step 6j)

## Objetivo
Replace the time picker copied from the Android Open Source Project by a Material 3 Compose dialog, and delete the vendored date and time pickers.

## Por qué
Step 6 of `.specs/migration-kmp.md`. The vendored package (21 Java files, about 212 KB, with layouts and `pickers.xml`) was the last big block of Views, and only two screens used it: the time of the reminder when a habit is edited and the custom time of a snooze. Nothing used the date picker.

## Alcance
- In: `TimePickerDialog` (a `ComposeDialogFragment` with the Material 3 `TimePicker`, the colour of the habit, 12 or 24 hours, Cancel, OK and an optional Clear, with test tags `timePickerOk`, `timePickerClear`, `timePickerCancel`), its use in `EditHabitActivity` and `SnoozeDelayPickerActivity`, a `DialogButton` overload with a test tag; removal of `com/android/datetimepicker`, the 8 layouts, the 2 colour selectors, the 2 `done_background_color` drawables and `pickers.xml`; `transparent` moved to `colors.xml` and the two colour swatch strings to `strings.xml` because other code still used them; the acceptance step `setReminder` presses `timePickerOk`; the stale entries of `lint-baseline.xml`.
- Out: `NOTICE.md` keeps the Android Open Source Project credit because other drawables of the app come from it; the other dialogs.
- Decisions (user): **no emulator or device runs without asking**.

## Exploración
- The old callbacks (`onTimeSet`, `onTimeCleared`) map to `OnTimePickedListener.onTimePicked` and `onTimeCleared`. The snooze dialog has no Clear.
- The Material 3 picker needs the hour, the minute and the 24 hour flag (`DateFormat.is24HourFormat`), as the old one did; the colour selector is set with `TimePickerDefaults.colors`.
- Like `WeekdayPickerDialog`, the dialog keeps its listener only in memory, so it is dismissed when restored after a rotation.

## Incertidumbre
- Nothing was run on a device: the look of the clock, the keyboard mode, the colour of the habit, 12 and 24 hours, Clear, the custom snooze, the acceptance step of the reminder.

## Checklist
- [x] TASK-1: `TimePickerDialog` in Compose — criterio: assembles
- [x] TASK-2: `EditHabitActivity` and `SnoozeDelayPickerActivity` use it — criterio: assembles
- [x] TASK-3: remove the vendored package, layouts, colours, drawables and `pickers.xml` — criterio: assembles, no references left
- [x] TASK-4: acceptance step and lint baseline — criterio: `compileDebugAndroidTestKotlin` passes
- [x] TASK-5: CHANGELOG, PENDING, AGENTS.md — criterio: docs updated; ktlint and unit tests green

## Estrategia de entrega
`single-pr`, branch `feat/compose-time-picker`, base `develop`. Commits only when the user asks.

## Evidencia
- `ktlintCheck`, `:salem-android:testDebugUnitTest`, `assembleDebug`, `assembleRelease`, `:salem-core:jvmTest` and `compileDebugAndroidTestKotlin` pass.
- Nothing was run on an emulator or a device.

# Small dialogs in Compose (KMP step 6d, part 2)

## Objetivo
Move `HabitTypeDialog`, `WeekdayPickerDialog`, `ColorPickerDialog` and `SnoozeDelayPickerActivity` to Jetpack Compose, keeping their public API so the Views-based callers do not change much.

## Por qué
Step 6 of `.specs/migration-kmp.md`. These dialogs are self-contained and block the removal of the vendored AOSP color picker.

## Alcance
- In: a `ComposeDialogFragment` base (Compose content, same `show` and `setListener` API), the four dialogs, pure helpers with tests (weekday toggling, snake-ordered palette, snooze choice, theme resolution), removal of the AOSP color picker (`com/android/colorpicker`, its layouts and resources) and of `select_habit_type.xml`, instrumented steps ported to UiAutomator.
- Out: `FrequencyPickerDialog`, `NumberDialog`, `CheckmarkDialog`, `ConfirmDeleteDialog`, `HistoryEditorDialog`, the inline `AlertDialog`s of `EditHabitActivity`, the widget `HabitPickerDialog` activities, the vendored `TimePickerDialog` (still used by `EditHabitActivity` and snooze), and the Android 12+ snooze limitation (the receiver ignores the action, see `docs/PENDING.md`).
- Decisions (user): one PR with the four dialogs, odd-full; keep texts and content descriptions and move the Espresso steps to UiAutomator.

## Exploración
- `HabitTypeDialog` (`AppCompatDialogFragment`, theme `Translucent`): two buttons start `EditHabitActivity` with `HabitType.YES_NO` / `NUMERICAL`, the scrim dismisses. Called from `ListHabitsScreen.showSelectHabitTypeDialog`.
- `WeekdayPickerDialog`: `setSelectedDays(WeekdayList)`, `setListener(OnWeekdaysPickedListener)`, weekday names from `longWeekdayNames(SATURDAY)`, saved state under `selectedDays`. Called from `EditHabitActivity` (empty result becomes `EVERY_DAY`).
- `ColorPickerDialog` wraps the vendored AOSP picker (4 columns, serpentine rows, swatch description "Color N" in reading order). `ColorPickerDialogFactory.create(color, theme)` is used by `EditHabitActivity` and `ListHabitsScreen`; colours come from `StyledResources.getPalette()`.
- `SnoozeDelayPickerActivity`: translucent `FragmentActivity` with an `AlertDialog` of `snooze_picker_names` / `snooze_picker_values` (-1 opens the vendored `TimePickerDialog`); `finish()` has no `return`, `habit!!` after a possible `finish()`. The context theme is set to `BaseDialog(Dark)` after `super.onCreate` and works that way, so the order is kept.
- Tests: `EditHabitSteps.pickColor` uses `By.descStartsWith("Color N")`; `unselectAllDays` and `CommonSteps.verifyShowsScreen(SELECT_HABIT_TYPE)` / `clickText("Yes or No")` use Espresso `withText`.

## Incertidumbre
- Rotation: listeners are not saved in the old dialogs either; only the selected days and colour are restored.
- Look and feel and dialog window behaviour (scrim, insets, dark theme) can only be judged on a device.

## Checklist
- [x] TASK-1: task doc — criterio: this file committed
- [x] TASK-2: pure helpers with tests first (weekday toggling, palette layout, snooze choice, theme resolution) — criterio: JVM tests pass
- [x] TASK-3: `ComposeDialogFragment`, `DialogSurface`, `HabitTypeDialog`, `WeekdayPickerDialog` — criterio: assembles
- [x] TASK-4: Compose `ColorPickerDialog` and factory; remove the AOSP picker — criterio: assembles, no references left
- [x] TASK-5: `SnoozeDelayPickerActivity` in Compose with its fixes — criterio: assembles
- [x] TASK-6: port instrumented steps — criterio: `compileDebugAndroidTestKotlin` passes
- [x] TASK-7: CHANGELOG, PENDING, AGENTS.md — criterio: docs updated; ktlint, unit tests, core tests green

## Estrategia de entrega
`single-pr` (decided with the user), branch `feat/compose-dialogs`, base `develop`. Commits only when the user asks.

## Evidencia
- `assembleDebug`, `assembleRelease`, `compileDebugAndroidTestKotlin`, `ktlintCheck`, `:salem-android:testDebugUnitTest` (11 new tests: dialog helpers, snooze choice, theme resolution) and `:salem-core:jvmTest` pass.
- Removed: the AOSP colour picker (5 Java files, 2 layouts, 2 drawables, 4 dimens), `select_habit_type.xml` and its styles. The picker keeps the serpentine layout, 4 columns and the "Color N" labels (reading order); the selected colour is now matched by palette index (it used to be by colour value, which failed for two colours whose XML and core values differ).
- `ColorPickerDialogFactory` no longer takes a context or a theme: `create(color)`.
- `SnoozeDelayPickerActivity`: added the missing `return` after `finish()` and removed `habit!!`. The context theme is still set after `super.onCreate` (deliberate, commented), so the plan item about applying the theme earlier was not done.
- Instrumented steps (`unselectAllDays`, `Yes or No`, `SELECT_HABIT_TYPE`) use UiAutomator and compile; `pickColor` is unchanged.
- Not run: any of the dialogs on a device (scrim, dark theme, insets, rotation, listener after rotation, the time picker over the snooze list) or the instrumented tests.
- Review fixes (rdd-full, one independent reviewer): the dialogs now follow the system night mode only from Android 10, like `AndroidThemeSwitcher` (`systemIsDark`, tested); the colour and weekday dialogs dismiss themselves when restored after a rotation, because their listener is not saved. Left in `docs/PENDING.md`: unlabeled clickable scrim and card, and the snooze activity ignoring `onNewIntent` and pure black. Also left: double tap on the habit type dialog can open two edit screens (as before), tests cover only pure helpers, stale entries in `lint-baseline.xml`.

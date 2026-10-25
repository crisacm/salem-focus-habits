# The habit editor on the shared UI (KMP step 7, PR 3c-2)

## Objetivo
Move the create and edit habit screen, with its pickers, to `salem-ui` and reduce `EditHabitActivity` to a host.

## Por qué
PR 3 makes the editor usable from iOS. 3a moved the simple dialogs, 3b the colour, weekday and time pickers, 3c-1 the helpers; 3c-2 moves the screen that uses them. 3d wires iOS.

## Alcance
- In: `EditHabitScreen`, `PickerLabels`/`defaultPickerLabels()` (`salem-ui` `ui.edit`); the pickers are overlays of the screen (no Fragments); `EditHabitActivity` as a host over `HabitEditorSaver`; the Android `EditHabitContent` and the Fragments `ChoiceDialog`, `FrequencyPickerDialog` and `WeekdayPickerDialog` are deleted (only the editor used them).
- Out (3d): the iOS wiring (`IosListScreen`, overlays in `IosListRoot`). Known editor issues of `docs/PENDING.md` (status bar contrast, TalkBack roles of the colour rows, `take(50)` cutting an emoji, validation errors lost on rotation) are not touched.
- Decisions (user): two stacked PRs for 3c; TDD where there is logic; no emulator/simulator without asking; rdd-full after 3d.

## Exploración
`EditHabitContent` used `LocalContext`, `R.string` and `testTagsAsResourceId`; the Activity kept the form for the pickers, opened them as `DialogFragment`s with listeners and dismissed them after a recreation. The colour palette, the weekday names and the clock format were already shared by 3b and 3c-1; `exposeTestTags()` and the CMP `BackHandler` already existed.

## Incertidumbre
- The overlays live in the same window as the top bar. UiAutomator would find two "Save" texts and the fields of the form behind the dialog, so the form gets `clearAndSetSemantics` while a picker is open (tested: `buttonSave` and `nameInput` are not in the tree). The instrumented steps (`EditHabitSteps`) were compiled, not run.
- Palette: the editor now takes it from `theme.paletteColors()` instead of `StyledResources` (identical values for the light and dark themes; `PureBlackTheme` extends `DarkTheme`).
- The open picker is not saved on rotation (the dialogs were dismissed after a recreation anyway); the form is.
- `BackHandler` closes the open picker before the Activity leaves; checked with the navigation event dispatcher of the desktop tests, not on a device.

## Checklist
- [x] TASK-1: `EditHabitScreenTest` (8 tests: validation, save, numerical target, colour picker, reminder time, frequency picker, hidden form, back) — criterio: RED before the screen existed
- [x] TASK-2: `EditHabitScreen` and the overlays — criterio: `:salem-ui:desktopTest`
- [x] TASK-3: `EditHabitActivity` as host, dead code deleted — criterio: `compileDebugKotlin`, `assembleDebug`, `compileDebugAndroidTestKotlin`
- [x] TASK-4: docs and changelog

## Estrategia de entrega
Stacked PRs: 3c-2 (`feat/ui-edit-screen`, from `develop` after #60), then 3d. The user merges in order. Commits only when the user asks.

## Evidencia
`:salem-core:jvmTest`, `:salem-ui:desktopTest`, `:salem-android:testDebugUnitTest`, `ktlintCheck` (all modules), `compileDebugAndroidTestKotlin`, iOS compile (arm64 and simulator) and `assembleDebug`. Not run: instrumented tests, emulator, simulator. To check by hand on Android: create (yes/no and numerical) and edit a habit; every picker (colour, frequency, target type, numerical frequency, time with Clear, days); the back button with a picker open; rotation with the form filled; the `HabitsTest` acceptance flows if an emulator is available.

# Shared helpers of the habit editor (KMP step 7, PR 3c-1)

## Objetivo
Move to common code everything the habit editor needs that is not UI layout: the save logic, the form saver, the texts of frequency and days, the choices of the pickers and the time format of the device.

## Por qué
PR 3c moves the editor screen to `salem-ui`. It was split in two (user decision): 3c-1 (this one) prepares the helpers so 3c-2 only moves the screen and its overlays.

## Alcance
- In: `HabitEditorSaver`/`SaveResult` (core); `EditHabitFormSaver` (moved from Android); `weekdaysSummary`, `denominatorForChoice`, `numericalFrequencyKind`, `reminderStartTime`, `uses24Hour` (pure, tested); `frequencyText`, `numericalFrequencyText`, `weekdaysText` (composables over `Res` strings); `TimeFormat` + `rememberTimeFormat()` (expect/actual: Android, desktop, iOS). The old editor and the subtitle card of the detail screen use them; `formatFrequency`, `formatTime` and `toFormattedString` are gone.
- Out (3c-2/3d): `EditHabitScreen` and its picker overlays, thin `EditHabitActivity`, iOS wiring.
- Decisions (user): two stacked PRs for 3c; TDD where there is logic; no emulator/simulator without asking; rdd-full after 3d.

## Exploración
`formatFrequency` and `formatTime` have two callers (the editor and `SubtitleNotesCards`), `toFormattedString` one; no time formatter existed in common code or on iOS. The save flow of `EditHabitActivity` had a `saving` flag that was never reset and finished silently when the habit was gone. `exposeTestTags()` is already an expect/actual in `salem-ui`.

## Incertidumbre
- iOS `rememberTimeFormat()` (`NSDateFormatter` short time style in GMT, 24 hour cycle from `dateFormatFromTemplate("j")`) is compiled for `iosArm64` and `iosSimulatorArm64` but not run; `uses24Hour` is the tested part.
- `weekdaysText` takes the names from `LocalDateFormatter` (`shortWeekdayName`/`longWeekdayName`) instead of the Java-only plural helpers; the Android editor passes `JavaLocalDateFormatter`.
- `HabitEditorSaver` answers `HABIT_NOT_FOUND` without taking the saving flag, so the host can decide; the Android host still closes the screen, as before.

## Checklist
- [x] TASK-1: `HabitEditorSaver` (TDD) — criterio: `HabitEditorSaverTest` (5 tests, `:salem-core:jvmTest`)
- [x] TASK-2: pure helpers (TDD) + `EditHabitFormSaver` moved with its test — criterio: `EditHelpersTest` (12), `EditHabitFormSaverTest` (2)
- [x] TASK-3: text composables and `TimeFormat` (expect/actual) — criterio: compile for Android, desktop and both iOS targets
- [x] TASK-4: the old editor and the detail subtitle card use them — criterio: `assembleDebug`, `compileDebugAndroidTestKotlin`
- [x] TASK-5: docs and changelog

## Estrategia de entrega
Stacked PRs: 3c-1 (`feat/ui-edit-helpers`, from `develop` after #59) then 3c-2 and 3d. The user merges in order. Commits only when the user asks.

## Evidencia
`:salem-core:jvmTest`, `:salem-ui:desktopTest`, `:salem-android:testDebugUnitTest`, `ktlintCheck` (all modules), `compileDebugAndroidTestKotlin`, iOS compile (arm64 and simulator) and `assembleDebug`. Not run: instrumented tests, emulator, simulator. To check by hand on Android: the edit screen (frequency, reminder time and days texts in 12 and 24 hour clocks) and the subtitle card of the habit detail (frequency and reminder time).

# Shared colour, weekday and time pickers (KMP step 7, PR 3b)

## Objetivo
Move the colour, weekday and time pickers of the habit editor to `salem-ui` so iOS can show them, with the Android Fragments reduced to hosts.

## Por qué
PR 3 moves the editor and its dialogs to the shared UI so iOS can create and edit habits. 3a moved the simple dialogs; 3b moves the three pickers that needed platform data (palette, weekday names, 24 hour clock).

## Alcance
- In: `ColorPickerContent`, `WeekdayPickerContent`, `TimePickerContent` and `Theme.paletteColors()`/`weekdayNames(LocalDateFormatter)` (`ui.dialogs`); `ok` and `cancel` strings; the three Android Fragments as hosts.
- Out (3c-3d): `formatTime`/`is24HourFormat`, `WeekdayList.toFormattedString`, `formatFrequency` as common code, the editor screen, the iOS wiring.
- Decisions (user): same as 3a (move to `salem-ui`, stacked PRs, TDD where there is logic, no emulator/simulator without asking).

## Exploración
Haiku brief: the Android `lightPalette`/`darkPalette` equal `LightTheme`/`DarkTheme.color(0..19)`; `LocalDateFormatter` has `longWeekdayName` and the common `getWeekdaySequence`; Material 3 `TimePicker` is in the CMP material3 1.9.0-beta03 artifacts; no tests existed for the three dialogs; `SnoozeDelayPickerActivity` also hosts `TimePickerDialog`.

## Incertidumbre
- `TimePicker` on iOS: confirmed by compiling `salem-ui` for `iosArm64` and `iosSimulatorArm64`; its look on iOS is not checked.
- Button labels: `android.R.string.ok/cancel/yes` are localized by Android and Compose resources cannot read them. The shared composables take `confirmLabel`/`cancelLabel` (defaults: new `ok`/`cancel` app strings, English until translated) and the Android hosts pass the system strings, so Android stays localized.
- The Android colour picker keeps `StyledResources.getPalette()` (no behaviour change); `paletteColors()` is for iOS and is tested against both themes.
- The weekday Fragment no longer saves its checked days in `onSaveInstanceState`: a restored dialog is dismissed anyway because its listener is lost.

## Checklist
- [x] TASK-1: `paletteColors` and `weekdayNames` (TDD) — criterio: `PickerModelsTest`
- [x] TASK-2: shared contents + Compose tests — criterio: `PickerContentTest` (4 tests)
- [x] TASK-3: Android Fragments as hosts, `ok`/`cancel` strings — criterio: compile, unit tests, `compileDebugAndroidTestKotlin`, `assembleDebug`
- [x] TASK-4: docs and changelog

## Estrategia de entrega
Stacked PRs 3a to 3d, branch `feat/ui-shared-pickers` from `develop` (3a is merged as #58). The user merges in order. Commits only when the user asks.

## Evidencia
`:salem-ui:desktopTest` (all), `:salem-android:testDebugUnitTest`, ktlintCheck (ui and android), `compileDebugAndroidTestKotlin`, iOS compile (arm64 and simulator), `assembleDebug`. Not run: instrumented tests, emulator, simulator. To check by hand on Android: the colour picker (edit screen and selection bar), the days of the reminder, the reminder time (including Clear) and the snooze time of the notification, in a localized language.

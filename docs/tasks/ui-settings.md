# The Settings screen on the shared UI (KMP step 7, PR 5c)

## Objetivo
Move the rows of Settings that are not tied to Android to `salem-ui` and show the screen on iOS; Android hosts it and adds its own sections.

## Por qué
iOS only logged `showSettingsScreen`. Third of three stacked PRs (5a About, 5b Intro, 5c this one). The Android rows wrote raw `SharedPreferences` keys (repeated as string literals) and relied on `SharedPreferencesStorage` to call four setters of `Preferences`; iOS has no such bridge, so the shared rows go through the setters of `Preferences`.

## Alcance
- In: `Preferences.firstWeekday` as a `var` (it had no setter; the key `pref_first_weekday` is Sunday-based, "1".."7"), `salem-ui` `ui.settings` (`SettingsState`, `SettingsSwitch`, `SettingsContent`, `SettingsSwitchRow`, weekday options), `PreferenceRows` moved to `ui/compose` (`Res.string.cancel`), `ScreenNavigation` as a stack (About opened from Settings goes back to Settings), the iOS layer with live effects, `SettingsActivity` as host with `AndroidSettingsSections` (reminder, database, troubleshooting) and `WidgetOpacityRow` passed as slots.
- Out (user decision: iOS shows only rows that do something): animations and sticky notifications (no confetti or notification tray on iOS yet), widget opacity, customize notification, backups and the public folder, export/import, bug report, repair database.
- Decisions (user): odd-full, 3 stacked PRs, TDD where there is logic, no emulator/simulator without asking; the Intro PR (#71) goes to `develop` apart.

## Exploración
Haiku `explorer` brief on Settings: rows, keys, `RESULT_*` contract, the storage bridge on Android, no change notification on iOS, widget opacity arrays in `constants.xml` (not synced), weekday names from `LocalDateFormatter`.

## Incertidumbre
- The first weekday still does not apply live on Android (only at process start), as before. On iOS the change applies at once (`setFirstWeekday`, header and rows refresh).
- Android writes now go through the setters, so the listeners fire twice for the four bridged keys (setter, then the storage bridge calls it again). Harmless, not measured.
- iOS pure black, midnight delay, unknown marks, skip and reverse order apply through `onSettingsChanged` (theme, today, first weekday, header, list refresh); not seen on the simulator.

## Checklist
- [x] TASK-1: `PreferencesTest` for `firstWeekday` — RED observed (`'val' cannot be reassigned`), then the setter
- [x] TASK-2: `SettingsStateTest` (5), `SettingsModelTest` (3), `SettingsContentTest` (9), then the code
- [x] TASK-3: Android host and sections; `SettingsModelTest` of Android kept as `SafPathsTest`
- [x] TASK-4: `ScreenNavigationTest` (stack) and the iOS layer
- [x] TASK-5: docs and changelog

## Estrategia de entrega
Stacked PRs 5a to 5c. 5a is in `develop` (#69); the Intro is in #71 (`feat/ui-about` to `develop`); this one is `feat/ui-settings`, from `feat/ui-about`. The user merges in order. Commits only when the user asks.

## Evidencia
See the verification run in the PR. Not run: the simulator, any emulator, the instrumented tests. To check by hand: every switch and the first day of the week on Android (and that the widgets, backups, CSV, import, bug report, repair, notification settings and the folder picker still work from Settings), Settings from the iOS menu with each switch (dark mode with pure black, reverse order in the header, skip and unknown marks in the cells, first day in the history), About from Settings and back, the back gesture.

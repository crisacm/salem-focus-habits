# Settings screen in Compose (KMP step 6c)

## Objetivo
Move `SettingsActivity`/`SettingsFragment` (`PreferenceFragmentCompat`) to Jetpack Compose without changing behaviour, except for the removals listed in Alcance.

## Por qué
Step 6 of `.specs/migration-kmp.md`, slice after About (PR #29). Settings is the next self-contained screen and gives reusable preference rows (switch, list, click, category) for later slices.

## Alcance
- In: Compose `SettingsContent`, preference state/rows, testable settings model, SAF folder picker, developer gate, strings moved to `strings.xml`, removal of `SettingsFragment` and its layout, port of instrumented steps that touch Settings.
- Out: `ListHabitsScreen` and the `RESULT_*` codes (kept as `setResult` + `finish`), `Preferences`/`SharedPreferencesStorage` keys, `RingtoneManager` and `pref_ringtone_uri`, live first-weekday and pure-black re-theming, removing `legacy-preference-v14`.
- Decisions (user): odd-full, one PR; keep the `RESULT_*` contract; remove the sync preferences (`pref_sync_base_url`, `pref_sync_key`, `pref_encryption_key`, no readers); do not migrate the always-hidden `reminderSound` row.

## Exploración
- `SettingsFragment` (268 lines): inflates `res/xml/preferences.xml`; hides `devCategory` unless `prefs.isDeveloper`; fills `pref_first_weekday` in code (values `"7","1".."6"`, summary `dayNames[current % 7]`); on any change calls `BackupManager.dataChanged` and, for `pref_widget_opacity`, `WidgetUpdater.updateWidgets()`; five rows return `RESULT_IMPORT_DATA/EXPORT_CSV/EXPORT_DB/REPAIR_DB/BUG_REPORT` through `setResult` + `finish`; `reminderCustomize` opens the channel settings; `publicBackupFolder` uses the SAF tree picker with a persistable permission and writes the key `publicBackupFolder`.
- `SharedPreferencesStorage` uses the default SharedPreferences file and calls `PreferenceManager.setDefaultValues(context, R.xml.preferences, false)`, so `preferences.xml` must stay as the source of defaults. It also re-fires core setters for four keys on change.
- Keys are duplicated across the XML, `Preferences.kt`, `AutoBackup`, `ExportDBTask`, `RingtoneManager`: they must not change.
- Tests touching Settings (Espresso, will not see Compose): `BackupSteps` ("Export full backup", "Import data"), `AboutTest.shouldDisplayAboutScreenFromSettings`, `LinksTest.shouldLinkToHelpFromSettings` (ignored). Nothing tests the fragment itself.

## Incertidumbre
- Not verified on a device: SAF flow, import/export results back to the list, first weekday and theme behaviour. Keeping the current behaviour is the goal, so any difference must be treated as a bug.

## Checklist
- [x] TASK-1: task doc — criterio: this file committed
- [x] TASK-2: settings model (weekday options and summary, SAF tree path) with tests first — criterio: JVM tests pass
- [x] TASK-3: preference state and reusable rows in `ui/compose` — criterio: compiles, rows read and write the default SharedPreferences
- [x] TASK-4: `SettingsContent` and `SettingsActivity` in Compose (RESULT_*, SAF, intents, developer gate) — criterio: assembles
- [x] TASK-5: remove `SettingsFragment`, `settings_activity.xml`, sync and ringtone rows of `preferences.xml`; move hardcoded strings — criterio: no references left, assembles
- [x] TASK-6: port instrumented steps that use Settings to a Compose rule — criterio: `compileDebugAndroidTestKotlin` passes
- [x] TASK-7: CHANGELOG, PENDING, AGENTS.md — criterio: docs updated; ktlint, unit tests, core tests green

## Estrategia de entrega
`single-pr` (decided with the user), branch `feat/compose-settings`, base `develop`. Commits only when the user asks.

## Evidencia
- `assembleDebug`, `assembleRelease`, `compileDebugAndroidTestKotlin`, `ktlintCheck`, `:salem-android:testDebugUnitTest` (7 new tests for the settings model) and `:salem-core:jvmTest` pass.
- `preferences.xml` is now defaults only (kept because `SharedPreferencesStorage` loads it); `SettingsFragment` and `settings_activity.xml` are removed; `About` and `Settings` share `SalemTopAppBar`.
- Instrumented steps that open Settings use a new UiAutomator `clickComposeText` (Espresso cannot see Compose text).
- Not run: instrumented tests and the real flows on a device (SAF folder picker, import/export results back to the list, theme and first weekday behaviour, widget opacity refresh).

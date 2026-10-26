# Exporting and sharing data on iOS (KMP step 7, PRs E1 and E2)

## Objetivo
Let the iOS user take the data out of the app: export the habits as CSV, export the whole database as a backup, export one habit from its detail, repair the database and send a bug report, all through the share sheet of the system (AirDrop, Messages, Save to Files).

## Por qué
On iOS the Export item of the detail only said "not available", the settings had no rows for exporting, repairing or reporting, and the messages of the core (`Message.*`) were never shown. The CSV export, the repair and the report already exist in the core; the backup of the database existed only on Android (`ExportDBTask`). Two sequential PRs, each straight to `develop`: E1 the common parts (this section), E2 the share sheet, the wiring of the app and the Settings rows.

## Alcance
- In E1: `UserFile.copyTo` (Java and Apple), `backupDatabase` and `backupFileName` in `salem-core`, `DatabaseBackupExport` and `listMessage` in `salem-ui`. No visible change yet.
- In E2: the detail shares the file it exports (`HabitDetailRoute(share)`, `HabitDetailLayers(onShare)`, no more `exportOverride` or "not available"), `IosShareSheet` (`UIActivityViewController`, with the popover that the iPad needs), the folder of the exports in the cache of the app, the messages of the core as a banner, the rows of Settings.
- Out: renaming "Loop Habits CSV" (the name is fixed in the shared `ExportCSVTask`), a log in the bug report (iOS has none), the recipient of the report (no support address yet), the preferences in the backup.
- Decisions (user): odd-full, TDD, share sheet, the scope above, no emulator/simulator without asking (the user runs them at the end).

## Exploración
Haiku `explorer` brief: `ExportCSVTask` and `HabitsCSVExporter` are common and use the Apple zip writer; the database of iOS is opened in `JournalMode.DELETE`, so the main file is complete between writes; `ExportDBTask` is Android only; the CSV folder of iOS is in Application Support, which the Files app does not show.

## Incertidumbre
- The first design copied the database with `VACUUM INTO`. With the JDBC adapter of the tests it failed with "cannot VACUUM - SQL statements in progress" because the repositories keep their prepared statements, and the behaviour of sqliter could not be checked here. It was replaced by a copy of the file (`UserFile.copyTo`), which does not depend on either driver. The risk is a write in flight while the file is copied; the export runs from a tap, when the app is idle.
- The copy keeps `user_version`, which `LoopDBImporter` needs to migrate: a test imports the copy again.

## Checklist
- [x] TASK-1 (E1): `UserFileCopyTest` (3) — RED observed — and `UserFile.copyTo` for Java and Apple (the Apple one is compiled, its test runs on the simulator)
- [x] TASK-2 (E1): `DatabaseBackupTest` (5: same version and habits, an existing file is replaced, a name with an apostrophe, the copy can be imported again, the file name) — RED observed
- [x] TASK-3 (E1): `DatabaseBackupExportTest` (4: copies and shares, a database that cannot be copied, a share that fails, every message has its text) — RED observed
- [x] TASK-4 (E2): `HabitDetailRoute(share)` and `HabitDetailLayers(onShare)` (the detail tests changed: exporting writes the zip and shares it), `IosShareSheet`, `IosExports.freshFolder()` (the caches of the app, emptied at every export), `IosListScreen` shares files and the bug report and shows the messages of the core as a banner, the Settings rows (export backup, export CSV, bug report, repair) — iosMain is only compiled, nothing of it has a test
- [x] TASK-5 (E2): docs and changelog

## Estrategia de entrega
Sequential PRs from `develop` straight back to `develop`, checking with `gh pr view` that the previous one is merged.

## Evidencia
(E1) `:salem-core:jvmTest` and `:salem-ui:desktopTest` with the new tests plus the checks listed in the PR. Not run: the iOS simulator tests (`UserFileCopyTest` and `DatabaseBackupTest` run there too).

(E2) `:salem-core:jvmTest`, `:salem-ui:desktopTest` (the detail tests changed), `:salem-android:testDebugUnitTest`, `ktlintCheck`, `compileDebugAndroidTestKotlin`, `assembleDebug`, the iOS compile of core, its tests and `salem-ui` (arm64 and simulator) and `xcodebuild` (build only). Not run by me: the iOS simulator tests and the app on the simulator; the user runs them at the end of the step. To check by hand: Settings, Export full backup opens the share sheet with "Salem Backup <date>.db" and the file can be saved to Files; the same file imports again from Settings, Import data (9 habits of a seeded database, or the ones of the app); Export as CSV shares a zip; the Export item of a habit detail shares its zip; Generate bug report shares the text (version and device); Repair database shows "Database repaired." and the list is still right; on an iPad the share sheet appears as a popover and does not crash; a message shows as a banner for a few seconds.

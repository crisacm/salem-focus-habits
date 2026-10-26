# Importing a Loop database on iOS (KMP step 7, PRs 7a and 7b)

## Objetivo
Let the user import a Loop Habit Tracker database (`.db`, and the other formats the core importers read) on iOS from the Files app, which is an acceptance criterion of step 7.

## Por qué
iOS could not import: there was no picker, no way to tell the result, and the transaction around the import (`ImportDataTask`) existed only on Android, where it also commits when the import fails and leaves it half done. Two sequential PRs, each straight to `develop`: 7a the common logic (this part of the document), 7b the picker, the wiring and the Settings row.

## Alcance
- In 7a: `ImportRunner` and `ImportResult` in `salem-core` (a transaction around `GenericImporter` that rolls back on failure and then lets the caller read the habits again), `ImportFlow` and `importMessage` in `salem-ui` (pick, import, delete the copy, refresh, report), `TransientMessage` and `MessageBanner` in `salem-ui` `ui/compose` (a message that goes away by itself).
- In 7b: the `UIDocumentPickerViewController` (UIKit, no dependency, `asCopy = true`), the importers built by hand for iOS, the "Database" section of iOS Settings with "Import data", the refresh of the list and the reminders after an import.
- Out: replacing the data (the importers merge by UUID, as on Android), a backup before importing, moving the Android task to `ImportRunner`.
- Decisions (user): odd-full, TDD, UIKit picker without libraries, merge with a transaction that is undone on failure, no backup, a Settings row plus a temporary message, no emulator/simulator without asking.

## Exploración
Haiku `explorer` brief: the importers are in `commonMain`; `LoopDBImporter` migrates the file it receives in place, so it must get a copy; the importers run their commands directly, so the `CommandRunner` listeners (list cache, reminder sync, permission) do not hear about an import and the app must refresh by hand; `SQLiteHabitList.reload()` makes the list read the database again.

## Incertidumbre
- The rollback is checked on the JVM (JDBC); on iOS it runs through sqliter, which understands `ROLLBACK` since the atomic migrations. `:salem-core:iosSimulatorArm64Test` has not been run for it.
- A Loop database older than version 24 gets new UUIDs every time it is migrated, so importing the same old file twice can duplicate habits.
- `ImportFlow` refreshes after a failed import too, because the rollback made the list read the database again; the habits objects the screens held are gone.

## Checklist
- [x] TASK-1 (7a): `ImportRunnerTest` (4: success kept, unknown file untouched, failure half way undone, import again after a failure) — RED observed (unresolved reference), then `ImportRunner`
- [x] TASK-2 (7a): `ImportFlowTest` (7) and `TransientMessageTest` (3) — RED observed, then the classes
- [x] TASK-3 (7b): picker (`IosFilePicker`), the importers by hand (`IosImport`), `IosReminders.rescheduleAll()`, the "Database" section of Settings and the banner in `IosListRoot` — only compiled: nothing of the iOS side has a test (the logic is in `ImportRunner` and `ImportFlow`, tested)
- [x] TASK-4 (7b): docs and changelog

## Estrategia de entrega
Sequential PRs from `develop` straight back to `develop`, checking with `gh pr view` that the previous one is merged.

## Evidencia
(7a, #76) `:salem-core:jvmTest` with the new `ImportRunnerTest`, `:salem-ui:desktopTest` with the new tests, the rest of the checks listed in the PR. Not run: the iOS simulator tests, a device.

(7b) `:salem-core:jvmTest`, `:salem-ui:desktopTest`, `:salem-android:testDebugUnitTest`, `ktlintCheck`, `compileDebugAndroidTestKotlin`, `assembleDebug`, the iOS compile of core, its tests and `salem-ui` (arm64 and simulator) and `xcodebuild` (build only). Not run by me: the iOS simulator tests and the app on the simulator; the user runs them at the end of the step. To check by hand: Settings, Import data, choose `loop.db` from the Files app: the habits appear in the list with their history, the message "Habits imported successfully." shows for a few seconds, and the reminders of the imported habits are scheduled (and the permission is asked if it was not); a text file gives "File not recognized."; cancelling the picker changes nothing; importing the same file again does not duplicate habits that already have a UUID.

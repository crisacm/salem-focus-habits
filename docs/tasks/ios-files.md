# iOS files (KMP step 4c)

## Objetivo
Implement `FileOpener`, `UserFile` and `ResourceFile` for iOS in `uhabits-core/src/appleMain` and make `createTestFileOpener()` (appleTest) real, so importers, migrations and CSV export can run on iOS.

## Por qué
Step 4c of `.specs/migration-kmp.md`. Unblocks `FilesTest`, `FileExtensionsTest`, `ImportTest`, `Version22Test` and `Version23Test` on the iOS simulator.

## Alcance
- In: `AppleFileOpener`, `AppleUserFile`, `AppleResourceFile`, appleTest helper, shared `FilesTest` additions, blocked-tests file, docs.
- Out: Image/Canvas (`ResourceFile.toImage()` throws on iOS until steps 6-7), Zip (4e), bundling the assets in the app (step 7).
- Decisions: user files in Application Support (roots injected, so it can change); TDD active; delivery `single-pr`, stacked on `feat/ios-strings` (#15).

## Exploración
- Contract in `commonMain/.../platform/io/Files.kt`; references `JavaUserFile` (jvmAndroidMain), `JavaFileOpener`/`JavaResourceFile` (jvmMain, `assets/main` then `assets/test`), `AndroidFileOpener(assets, filesDir)`.
- JVM quirks not to copy: `delete()` throws on a missing file (contract: no-op), `readBytes` does a single `read()`, `listFiles` order is OS-dependent. `resolve` is relative to the path itself (kept).
- Importers pass `file.pathString` to `SqliterDatabaseOpener`, so paths must be absolute. `HabitBullCSVImporter.canHandle` relies on `lines()` throwing for missing files.
- `ShowHabitMenuPresenterTest.testOnExport` and `ListHabitsBehaviorTest.testOnExportCSV*` also need `ZipWriter` (4e).

## Incertidumbre
1. ByteArray <-> NSData (empty arrays), covered by tests.
2. Atomic overwrite with `writeToFile(atomically)`.
3. Errors reported as `IllegalStateException` with the path (no NSError plumbing).
4. Importers may show iOS-only problems once they can run; fix if small, otherwise re-block with the exact reason.

## Checklist
- [x] TASK-1: task doc and Engram mirror — criterio: this file committed
- [x] TASK-2: `AppleUserFile` core IO — criterio: AppleFilesTest (write/read, lines, delete, readBytes) green
- [x] TASK-3: directory API (`mkdirs`, `resolve`, `listFiles`) — criterio: tests green
- [x] TASK-4: `AppleResourceFile`, `AppleFileOpener`, `createDefault()` — criterio: tests green on the simulator
- [x] TASK-5: real `createTestFileOpener()`, shared `FilesTest`, unblock tests — criterio: iOS and JVM suites green
- [x] TASK-6: docs and changelog — criterio: ktlint, assembleDebug, CI green (CI pending)

## Estrategia de entrega
`single-pr`, base `feat/ios-strings` until #14 and #15 merge, then retarget to `develop`.

## Evidencia
- AppleFilesTest: 24 tests green on the iOS simulator (RED observed first: unresolved references, then `NotImplementedError` for the directory API).
- `iosSimulatorArm64Test`: 288 tests, 0 failures; `jvmTest`: 292 tests, 0 failures; `ktlintCheck` ok.
- Unblocked: `FilesTest`, `FileExtensionsTest`, `ImportTest`, `Version22Test` (except four cases), `Version23Test`. `ShowHabitMenuPresenterTest.testOnExport` moved to the Zip section.
- Incertidumbre 4 resolved: four `Version22Test` cases expect "constraint" in the message of a failed insert; sqliter throws `SQLiteExceptionErrorCode("sqlite3_step failed")` with a private error code, so they are re-blocked with that reason.
- Not verified: the default opener (`createDefault()`) on a real device, and the app bundle `assets` folder (step 7).

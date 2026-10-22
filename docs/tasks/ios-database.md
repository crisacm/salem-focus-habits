# iOS Database on sqliter (migration step 3d, PR 3d-1)

## Objetivo
Implement `Database`/`PreparedStatement`/`DatabaseOpener` for iOS in `uhabits-core/src/appleMain` directly on sqliter, and run the existing
database contract tests (`DatabaseTest`, `DatabaseQueryHelpersTest`, `MigrationTest`) on the iOS simulator. Method: odd-full, two chained PRs
(this is 3d-1; 3d-2 adds the repositories and a `loop.db` migration test).

## Por qué
Step 3 of `.specs/migration-kmp.md` ends with "commonMain compiles for iOS with real (not stub) persistence and preferences". The decision
table said SQLDelight; the spike showed that its `SqlDriver` API does not fit our cursor-style abstraction, so the engine under it (sqliter) is used directly.

## Alcance
In: sqliter dependency and `-lsqlite3`, `SqliteFileHeader` (`peekUserVersion`), `SqliterDatabase`/`SqliterPreparedStatement`/`SqliterDatabaseOpener`,
appleTest actuals for `createTestDatabaseOpener`/`TestDatabaseHelper`, iOS tests, CI filters, docs, spec decision update.
Out (3d-2): repository tests on iOS, `loop.db` migration test. Out (step 4): `FileOpener`, `SQLiteHabitListTest`/`BaseUnitTest` on iOS.
Out (always): Android/JVM implementations, `NN.sql` migrations, `SQLParser`, `.sq`/`.sqm`, SQLDelight runtime.

## Exploración
Explorer briefs (persistence layer, Database contract) plus the spike on `spike/ios-database` (local, never pushed; Engram `discovery/ios-database-spike`).
- Contract (reference `JavaDatabase`): `step()` runs on first call then `next()`; stays DONE; write statements re-run on `step()`; 1-based typed binds,
  0-based getters, `*OrNull` via null check; `reset()` closes the cursor and clears bindings; repositories keep `lazy` statements and never `finalize()`;
  nested queries need two live statements; `BEGIN`/`COMMIT` intercepted by prefix with a depth counter; a new file reports `user_version` 0.
- sqliter 1.3.3 API (from `klib dump-metadata`): `DatabaseConnection.createStatement(sql)`, `rawExecSql`, `close`, `closed`; `Statement.bind{Long,Double,String,Blob,Null}`
  (1-based), `clearBindings`, `execute`, `query`, `resetStatement`, `finalizeStatement`; `Cursor.next/getLong/getDouble/getString/isNull/columnCount/columnNames`;
  `DatabaseConfiguration(name, version, create, upgrade, ..., journalMode, extendedConfig(basePath), inMemory)`.
- Spike: config version > file version rewrites `user_version`; lower throws; equal is safe, so the version is peeked from the file header; `JournalMode.DELETE` gives one file;
  one connection handles raw BEGIN/COMMIT, `last_insert_rowid()` and reads inside a transaction; migrations 09-25 and loop.db 12 -> 25 run through core `SQLParser`.
- commonTest classes runnable on iOS without `FileOpener`: `DatabaseTest` (6), `DatabaseQueryHelpersTest` (8), `MigrationTest` (2), `HabitRepositoryTest`, `EntryRepositoryTest`, `SQLiteEntryListTest`.

## Incertidumbre
All resolved:
- U1: persistent statements exist (`createStatement`, `query()`, `resetStatement`, `finalizeStatement`); two live statements work (`testTwoStatementsCanBeAliveAtTheSameTime`).
- U2: `DatabaseConfiguration.inMemory = true` with a unique name per open gives independent databases (`testInMemoryDatabasesAreIndependent`).
- U3: a file with `user_version` 0 is opened with config version 1 and restored to 0 afterwards (`testFileWithVersionZeroStaysAtZero`).
- U4: a database in WAL mode opens fine with `JournalMode.DELETE`, keeps its data and ends as a single file (`testDatabaseInWalModeIsOpenedAndConvertedToASingleFile`). The journal mode of the file changes; importers open temporary copies.
- U5: Gradle `environment("SIMCTL_CHILD_SALEM_ASSETS_DIR", ...)` on `KotlinNativeSimulatorTest` reaches the test as `SALEM_ASSETS_DIR`; without the prefix it does not. No generated source needed.
- U6: closing with a live statement works (statements are finalized on `close()`); `COMMIT` at depth 0 is ignored (`testCommitWithoutBeginIsIgnored`).
Other findings: opening a second connection to a file that has an open write transaction fails with "database is locked" (sqliter sets the journal mode on open), which does not happen in production because importers open other files; a statement without result columns is executed by the first `next()` and must not be stepped again (the adapter resets it instead); `sqliter` does not end up in the Android APK.

## Checklist
TDD mode: red/green. Red: appleTest sources reference classes that do not exist yet; then the existing contract tests fail on the iOS `TODO` stubs.
- [x] TASK-1: sqliter dependency (catalog `sqliter = "1.3.3"`), `-lsqlite3` kept — criterio: `compileKotlinIosSimulatorArm64` green
- [x] TASK-2: RED tests (header peek, opener semantics, statements, transactions) — criterio: they fail to compile / fail before the implementation
- [x] TASK-3: `SqliteFileHeader`, `SqliterDatabase`, `SqliterPreparedStatement`, `SqliterDatabaseOpener` — criterio: TASK-2 tests green on the simulator
- [x] TASK-4: real appleTest actuals and the contract tests on iOS — criterio: `DatabaseTest`, `DatabaseQueryHelpersTest`, `MigrationTest` green on the simulator
- [x] TASK-5: CI filters, docs, spec decision, CHANGELOG — criterio: `ktlintCheck` green, PR CI green (ubuntu and macOS)

## Estrategia de entrega
Chosen with the user: two chained PRs (3d-1 foundation, 3d-2 repositories). One commit per task, PR to `develop`; CI read via `gh api .../jobs`.

## Evidencia
macOS arm64, Xcode 27.1, iOS 27.1 simulator, Gradle run from the repo root.
- RED: with the tests in place and no implementation, `compileTestKotlinIosSimulatorArm64` failed with unresolved references to `peekUserVersion` and `SqliterDatabaseOpener`.
- GREEN on the simulator (the exact CI command, 37 tests, 0 failures): `SqliteFileHeaderTest` 2, `SqliterDatabaseTest` 13, `UserDefaultsStorageTest` 6, and the common contract tests `DatabaseTest` 6, `DatabaseQueryHelpersTest` 8, `MigrationTest` 2 (this one runs migrations 09-25 from the real `assets/main/migrations` on iOS).
- Regression: `:uhabits-core:jvmTest` (277 tests), `compileDebugKotlinAndroid`, `compileKotlinIosArm64`, `:uhabits-android:assembleDebug`, `assembleRelease`, `testDebugUnitTest`, `compileDebugAndroidTestKotlin`, `ktlintCheck` pass; the release APK has no sqliter classes.
- Not run: the rest of the commonTest suite on iOS (needs step 4 and PR 3d-2), instrumented tests.
- CI: see the PR checks.

## Hallazgos de la revisión (rdd-lite de #11)
- Fixed: two `SqliterDatabaseOpener` instances shared the `:memory:` database because the name came from a per-instance counter and sqliter shares in-memory databases by name within the process. Each open now uses a unique name (`NSUUID`), covered by `testInMemoryDatabasesFromDifferentOpenersAreIndependent`.
- Open (low): opening a file that is not a SQLite database fails with a cryptic `error while compiling: PRAGMA journal_mode`. Open (info): `peekUserVersion` can be stale for a WAL database whose latest `user_version` is only in the `-wal` file. Both are tracked in `docs/PENDING.md`.

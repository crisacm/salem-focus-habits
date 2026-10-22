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
Resolved:
- U1: persistent statements exist (`createStatement` + `query()` + `resetStatement` + `finalizeStatement`); two live statements still to be proven by a test.
Open (record the answer here):
- U2: `inMemory` behaviour for `:memory:`; fallback: temp file removed on close.
- U3: opening a file with `user_version` 0 without rewriting it (config `version` 0 allowed? else restore with `PRAGMA user_version`).
- U4: foreign database in WAL mode opened with `JournalMode.DELETE` (conversion side effect).
- U5: how appleTest reads `assets/main/migrations` and the test DBs (Gradle `environment` vs a generated `TestPaths` source).
- U6: `close()` with live statements; `COMMIT`/`ROLLBACK` at depth 0 (decision: no-op, documented and tested).

## Checklist
TDD mode: red/green. Red: appleTest sources reference classes that do not exist yet; then the existing contract tests fail on the iOS `TODO` stubs.
- [ ] TASK-1: sqliter dependency (catalog `sqliter = "1.3.3"`), `-lsqlite3` kept — criterio: `compileKotlinIosSimulatorArm64` green
- [ ] TASK-2: RED tests (header peek, opener semantics, statements, transactions) — criterio: they fail to compile / fail before the implementation
- [ ] TASK-3: `SqliteFileHeader`, `SqliterDatabase`, `SqliterPreparedStatement`, `SqliterDatabaseOpener` — criterio: TASK-2 tests green on the simulator
- [ ] TASK-4: real appleTest actuals and the contract tests on iOS — criterio: `DatabaseTest`, `DatabaseQueryHelpersTest`, `MigrationTest` green on the simulator
- [ ] TASK-5: CI filters, docs, spec decision, CHANGELOG — criterio: `ktlintCheck` green, PR CI green (ubuntu and macOS)

## Estrategia de entrega
Chosen with the user: two chained PRs (3d-1 foundation, 3d-2 repositories). One commit per task, PR to `develop`; CI read via `gh api .../jobs`.

## Evidencia
(pending)

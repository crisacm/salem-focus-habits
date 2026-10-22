# Repositories and migrations on iOS (migration step 3d, PR 3d-2)

## Objetivo
Close step 3: run the database-backed tests that sit on top of the iOS `Database` on the simulator (repositories, SQLite lists), add a `loop.db` migration
test, and record exactly what still blocks the rest of the commonTest suite on iOS (input for step 4). Method: odd-full, single PR.

## Por qué
Step 3 "Done when": commonMain compiles for iOS with real persistence and preferences. PR 3d-1 added the iOS `Database` and the contract tests; this PR proves
the layers above it (HabitRepository, EntryRepository, SQLite lists) and a real foreign database migration work on iOS.

## Alcance
In: run `HabitRepositoryTest`, `EntryRepositoryTest`, `SQLiteEntryListTest`, `SQLParserTest`, `SQLiteHabitListTest` on iOS; make `BaseUnitTest.fileOpener` lazy;
`LoopDbMigrationTest` (loop.db 12 -> 25); discovery run of the whole iOS suite; CI filters and docs.
Out (step 4): iOS `FileOpener`/`UserFile`, real `format`/`formatInvariant`, Zip, Canvas, `Version22Test`/`Version23Test`/`ImportTest` on iOS.

## Exploración
Explorer brief (static analysis) plus the plan in the session plan file.
- Runnable with no stub reached: `HabitRepositoryTest`, `EntryRepositoryTest`, `SQLiteEntryListTest`, `SQLParserTest` (no `BaseUnitTest`).
- Blocked by the eager `fileOpener = createTestFileOpener()` in `BaseUnitTest.kt:46` (iOS actual is a `TODO` stub): all 30 `BaseUnitTest` subclasses, including `SQLiteHabitListTest`.
- `Version22Test`/`Version23Test` also need `fileOpener.openResourceFile` and the `format` stub; `LoopDBImporter`/`ImportTest` need `UserFile`/`FileOpener` and `formatInvariant`.
- `loop.db`: `user_version` 12, 9 habits, 164 repetitions (per habit id: 15->33, 16->21, 17->2, 21->15, 22->12, 23->13, 24->43, 25->12, 26->13); names repeat, so identify by id.
- This branch is stacked on PR #12 (unique in-memory database names): the repository tests create many `:memory:` databases with different opener instances.

## Incertidumbre
Open (record the answer here):
- U1: the four runnable classes pass on Kotlin/Native as-is.
- U2: Mokkery works on Kotlin/Native (`SQLiteHabitListTest`).
- U3: a lazy `fileOpener` keeps the JVM and Android unit tests unchanged.
- U4: how many `BaseUnitTest` subclasses pass on iOS without stubs once `fileOpener` is lazy (gap list for step 4).
- U5: CI time of the larger iOS test set; include list vs whole passing set.
- U6: repositories read the expected data after migrating a copy of `loop.db` 12 -> 25.

## Checklist
TDD mode: red/green. Tests are the work here: baseline first, then the lazy `fileOpener`, then the new migration test (RED before it exists).
- [ ] TASK-1: baseline run of the five classes on the simulator — criterio: pass/fail and cause recorded (U1, U2)
- [ ] TASK-2: lazy `BaseUnitTest.fileOpener` — criterio: `SQLiteHabitListTest` passes on iOS, `jvmTest` 277 and Android unit tests unchanged (U3)
- [ ] TASK-3: `LoopDbMigrationTest` — criterio: RED first, GREEN after the test is complete (U6)
- [ ] TASK-4: discovery run of the whole iOS suite — criterio: pass/fail per class and breaking stub recorded (U4)
- [ ] TASK-5: CI filters, docs, spec, CHANGELOG — criterio: `ktlintCheck` green, PR CI green (ubuntu and macOS) (U5)

## Estrategia de entrega
`single-pr` (small diff), stacked on PR #12 until it is merged. One commit per task; CI read via `gh api .../jobs`.

## Evidencia
(pending)

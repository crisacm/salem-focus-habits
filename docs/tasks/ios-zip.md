# iOS zip (KMP step 4e)

## Objetivo
Implement `ZipReader` and `ZipWriter` for iOS in `uhabits-core/src/appleMain` so the CSV export and its tests run on iOS.

## Por qué
Step 4e of `.specs/migration-kmp.md`. Unblocks `ZipTest`, `HabitsCSVExporterTest`, `ShowHabitMenuPresenterTest.testOnExport` and `ListHabitsBehaviorTest.testOnExportCSV` / `_fail` on the iOS simulator.

## Alcance
- In: raw DEFLATE and CRC-32 through `platform.zlib`, zip container (local headers, central directory, end record) written and parsed in Kotlin, tests, JDK interop fixtures, blocked-tests file, docs.
- Out: ZIP64, encryption, streaming, binary entries (the API is text only).
- Decisions: odd-full, TDD active, delivery `single-pr` stacked on `feat/ios-date-formatter` (#17).

## Exploración
- API: `ZipEntry(name, content: String)`, `ZipReader(bytes).entries()` (suspend), `ZipWriter().addEntry(name, content)` and `suspend toBytes()`.
- JVM: `ZipOutputStream` DEFLATED, insertion order, no directory entries written; reader keeps directory entries; non-zip input returns an empty list silently.
- Only production writer: `HabitsCSVExporter.writeArchive`. `ZipReader` is used by tests only. `ZipTest.testLargeContent` needs real compression.
- No third-party library rule (AGENTS.md), GPLv3.

## Incertidumbre
- Spike (Engram `spike/ios-zip`): `platform.zlib` works on the simulator with no Gradle change (`deflateInit2_` with windowBits -15 gives raw DEFLATE, `crc32` ok). Device target not linked yet (only compiled in appleMain); linking happens with the app framework in step 7.
- Interop with other zip tools is checked with fixtures: an archive written by the JDK is read on iOS, an archive written on iOS is read by the JDK in a JVM test.
- Non-zip input: iOS throws `IllegalArgumentException`; the JVM returns an empty list for non-zip bytes.

## Checklist
- [ ] TASK-1: branch, task doc, spike recorded — criterio: this file committed
- [ ] TASK-2: CRC-32, DOS date and raw deflate/inflate helpers — criterio: helper tests green
- [ ] TASK-3: `ZipWriter` — criterio: structure tests and common `ZipTest` writer cases green
- [ ] TASK-4: `ZipReader` — criterio: round trips, JDK fixture, error cases green
- [ ] TASK-5: interop fixtures and extended common `ZipTest` — criterio: iOS and JVM suites green
- [ ] TASK-6: unblock tests, docs, changelog — criterio: ktlint, assembleDebug, CI green

## Estrategia de entrega
`single-pr`, base `feat/ios-date-formatter` until the parents merge, then retarget to `develop`.

## Evidencia

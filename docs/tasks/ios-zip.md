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
- [x] TASK-1: branch, task doc, spike recorded — criterio: this file committed
- [x] TASK-2: CRC-32, DOS date and raw deflate/inflate helpers — criterio: helper tests green
- [x] TASK-3: `ZipWriter` — criterio: structure tests and common `ZipTest` writer cases green
- [x] TASK-4: `ZipReader` — criterio: round trips, JDK fixture, error cases green
- [x] TASK-5: interop fixtures and extended common `ZipTest` — criterio: iOS and JVM suites green
- [x] TASK-6: unblock tests, docs, changelog — criterio: ktlint, assembleDebug, CI green

## Estrategia de entrega
`single-pr`, base `feat/ios-date-formatter` until the parents merge, then retarget to `develop`.

## Evidencia
- Spike: Engram `spike/ios-zip` (`platform.zlib` raw deflate, inflate and crc32 on the simulator with no Gradle change).
- New iOS tests, each written before the code and seen failing: `AppleDeflateTest` (6), `AppleZipWriterTest` (8, header layout byte by byte), `AppleZipReaderTest` (7: JDK-written archive with data descriptors, stored entries, round trip, garbage, truncated, corrupt CRC and data, unsupported method, encryption, bad offset), `AppleZipInteropTest` (writer output equals the committed `ios-written.zip`), two cases added to the shared `ZipTest`.
- Interop: `ios-written.zip` read by the JDK (`ZipInteropTest`, JVM), by Python `zipfile.testzip()` and by the system `unzip -t`; `jdk-written.zip` (data descriptors) and `stored.zip` (Python) read on iOS.
- Unblocked: `ZipTest`, `HabitsCSVExporterTest`, `ShowHabitMenuPresenterTest.testOnExport`, `ListHabitsBehaviorTest.testOnExportCSV` and `_fail`. iOS suite 337 tests, 0 failures; `jvmTest` 295, 0 failures; `ktlintCheck`, `compileKotlinIosArm64` and `assembleDebug` pass.
- Not verified: linking for the device target (only the framework of step 7 links), CI.

# Add iOS targets to uhabits-core with stub actuals (migration step 2, PR 2c)

## Objetivo
Add `iosArm64` and `iosSimulatorArm64` to `uhabits-core` with `TODO()` stub actuals so
`:uhabits-core:compileKotlinIosSimulatorArm64` succeeds. Method: odd-full.

## Por qué
`.specs/migration-kmp.md` step 2. Core is `jvm` + `android`. iOS needs real targets before steps 3 and 4 can
implement persistence and platform pieces.

## Alcance
In: iOS targets, `appleMain` stub actuals for the 7 production `expect` declarations, a macOS CI job, docs.
Out: real iOS implementations (steps 3/4), iOS test actuals and `iosSimulatorArm64Test`, `iosX64`, SQLDelight.

## Exploración
Brief from the `explorer` agent plus Context7 docs.
- Production `expect`s needing actuals: `ZipReader`/`ZipWriter` (`platform/io/Zip.kt`), `format` x3 (`platform/io/Strings.kt`),
  `getFirstWeekdayNumberAccordingToLocale` and `computeToday` (`platform/time/Dates.kt`). `Synchronized`/`JvmStatic` are `@OptionalExpectation`.
- commonMain has no `java.*` or `kotlin.jvm` imports; the default hierarchy template is active, so `appleMain` appears with the targets.
- Mokkery 3.3.0 supports Kotlin 2.3.0-2.3.21 and all Kotlin/Native targets; kotlin-inject-runtime 0.9.0 has iOS variants in the Gradle cache.
- Xcode 27.1 selected, license accepted, iOS SDKs present, no simulator runtime. `~/.konan` missing: first build downloads Kotlin/Native.
- commonTest `expect`s have actuals only in `jvmTest`, so iOS test compilation, `check` and `allTests` do not work until step 4.

## Incertidumbre
Resolved locally:
- U1: Kotlin/Native 2.3.20 works with Xcode 27.1: the toolchain downloaded to `~/.konan` (1.3 GB) and compilation reached the frontend and finished.
- U2: `compileOnly(kotlin-inject-runtime)` in commonMain compiles for native targets with no changes.
- U3: `Synchronized`/`JvmStatic` need no actual on native (no error for them). `@Throws` is fine.
- U5: Mokkery and the default hierarchy keep working. `jvmTest` (274), Android and ktlint are unaffected; ktlint checks `appleMain`.
Pending the CI of this PR:
- U4: the ubuntu job must stay green with Apple targets declared (Apple targets are skipped on Linux).
- U6: the macOS job must be able to configure core (Android library) and compile the iOS targets.

## Checklist
TDD mode: red/green gradle checks. Red check: `./gradlew :uhabits-core:compileKotlinIosSimulatorArm64` fails now (task does not exist).
- [x] TASK-1: add `iosArm64()` and `iosSimulatorArm64()` — criterio: the compile errors are exactly the 7 missing actuals
- [x] TASK-2: stub actuals in `src/appleMain` — criterio: `compileKotlinIosSimulatorArm64` and `compileKotlinIosArm64` green
- [x] TASK-3: regression — criterio: `jvmTest` 274, `compileDebugKotlinAndroid`, `assembleDebug`, `assembleRelease`, `testDebugUnitTest`, `ktlintCheck` green
- [ ] TASK-4: macOS job in `.github/workflows/ci.yml` — criterio: PR shows both jobs green
- [x] TASK-5: docs, re-index codebase-memory, Engram — criterio: ktlintCheck green

## Estrategia de entrega
`single-pr`: build file, 3 stub files, CI job and docs, well under ~400 lines. One commit per task, PR to `develop`.

## Evidencia
Run on macOS (arm64), Xcode 27.1, with `JAVA_TOOL_OPTIONS="-Duser.language=en -Duser.country=US"`.
- RED: `:uhabits-core:compileKotlinIosSimulatorArm64` failed with "task not found"; after adding the targets it failed with exactly seven errors: `format` x3, `ZipReader`, `ZipWriter`, `getFirstWeekdayNumberAccordingToLocale`, `computeToday`.
- GREEN: `:uhabits-core:compileKotlinIosSimulatorArm64` and `:uhabits-core:compileKotlinIosArm64` pass with the stubs.
- Regression: `:uhabits-core:jvmTest` (274 tests, 0 failures), `:uhabits-core:compileDebugKotlinAndroid`, `:uhabits-android:assembleDebug`, `:uhabits-android:assembleRelease`, `:uhabits-android:testDebugUnitTest` (5 tests, 0 failures), `ktlintCheck`.
- Not run: iOS tests (no simulator runtime, no iOS test actuals).
- CI: see the PR checks.

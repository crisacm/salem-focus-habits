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
Open (record the answer here):
- U1: Kotlin/Native 2.3.20 works with Xcode 27.1.
- U2: `compileOnly(kotlin-inject-runtime)` in commonMain works for native targets.
- U3: `Synchronized`/`JvmStatic` need no actual on native.
- U4: Linux CI is not broken by declaring Apple targets (maybe `kotlin.native.ignoreDisabledTargets=true`).
- U5: Mokkery and the default hierarchy keep working; jvmTest, Android and ktlint unaffected.
- U6: the macOS runner can configure core (Android SDK 36 / `rm -f local.properties`).

## Checklist
TDD mode: red/green gradle checks. Red check: `./gradlew :uhabits-core:compileKotlinIosSimulatorArm64` fails now (task does not exist).
- [ ] TASK-1: add `iosArm64()` and `iosSimulatorArm64()` — criterio: the compile errors are exactly the 7 missing actuals
- [ ] TASK-2: stub actuals in `src/appleMain` — criterio: `compileKotlinIosSimulatorArm64` and `compileKotlinIosArm64` green
- [ ] TASK-3: regression — criterio: `jvmTest` 274, `compileDebugKotlinAndroid`, `assembleDebug`, `assembleRelease`, `testDebugUnitTest`, `ktlintCheck` green
- [ ] TASK-4: macOS job in `.github/workflows/ci.yml` — criterio: PR shows both jobs green
- [ ] TASK-5: docs, re-index codebase-memory, Engram — criterio: ktlintCheck green

## Estrategia de entrega
`single-pr`: build file, 3 stub files, CI job and docs, well under ~400 lines. One commit per task, PR to `develop`.

## Evidencia
(pending)

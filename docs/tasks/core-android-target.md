# Add the Android target to uhabits-core (migration step 2, PR 2b)

## Objetivo
Make `uhabits-core` a real Android KMP library: add `androidTarget` and a source set shared by `jvm` and
`android`, so iOS targets (PR 2c) can be added next. Method: odd-full.

## Por qué
`.specs/migration-kmp.md` step 2. Core is `jvm`-only and the app consumes its jvm variant. Android needs a proper
variant, and `java.awt`/JDBC code must be kept out of it.

## Alcance
In: `androidTarget`, `jvmAndroidMain` group, moving `java.*`-only code into it, splitting `JavaFiles.kt`,
removing the unused Mockito `MockMaker` resource, docs. Out: iOS targets (2c), CI macOS job (2d), AGP upgrade /
new `com.android.kotlin.multiplatform.library` plugin, SQLDelight, removing the `jvm` target.

## Exploración
Briefs from the `explorer` agent (two runs) plus Context7 docs.
- Kotlin 2.3.20 does not warn on `androidTarget` (deprecation reverted in 2.3.10); the new Android KMP plugin needs AGP >= 8.10.
- A manual `dependsOn` disables the default hierarchy template, so the shared set is a template group:
  `applyDefaultHierarchyTemplate { common { group("jvmAndroid") { withJvm(); withAndroidTarget() } } }`.
- Android-safe (`java.*` only), to move to `jvmAndroidMain`: `JvmAnnotations`, `JavaStrings`, `Zip`, `DateFormats`,
  `DateUtils`, `JavaDates`, `JvmDates`, `ReminderScheduler`, `MidnightTimer`, plus `JavaFileOpener`/`JavaUserFile` from `JavaFiles.kt`.
- Stay in `jvmMain`: `JavaCanvas`, `JavaImage` (java.awt), `JavaDatabase` (JDBC), `JavaResourceFile` (ImageIO).
- jvmTest keeps all tests and the AWT/JDBC helpers. commonTest `expect` helpers have actuals only in jvmTest.
- `mockito-extensions/org.mockito.plugins.MockMaker` in jvmMain resources is unused.
- Android main uses `JavaUserFile` (`AndroidFiles.kt`, `ListHabitsScreen.kt`, `HabitsDirFinder.kt`).

## Incertidumbre
Open (resolve during implementation, record the answer here):
- U1: `androidTarget` + `com.android.library` on AGP 8.9.2 / KGP 2.3.20 / Gradle 8.11.1, plugin id resolves without a catalog entry.
- U2: library builds with no `AndroidManifest.xml` (only `namespace`).
- U3: core Android unit-test variant: disable vs minimal actuals (preferred: disable; tests stay in `jvmTest`).
- U4: custom `jvmAndroid` group coexists with the default template; ktlint source-set tasks pass.
- U5: `JavaFileOpener` has no dependency on `JavaResourceFile`/`JavaImage`.
- U6: the app resolves the core android variant; androidTest sources (`AutoBackupTest`, `IntentSchedulerTest`) still compile.

## Checklist
TDD mode: active for build-structure work as red/green gradle checks (no project TDD config found).
Red check: `./gradlew :uhabits-core:compileDebugKotlinAndroid` fails now (task does not exist); safety net: `jvmTest` 274 tests, 0 failures with en-US locale.
- [ ] TASK-1: declare `jvmAndroid` group and `git mv` the Android-safe files into `src/jvmAndroidMain` — criterio: `jvmTest` 274 green, `assembleDebug` green
- [ ] TASK-2: split `JavaFiles.kt` (JavaResourceFile stays in jvmMain) — criterio: same checks green
- [ ] TASK-3: add `androidTarget` and disable the core Android unit-test variant — criterio: `:uhabits-core:compileDebugKotlinAndroid` green, `assembleDebug`, `testDebugUnitTest`, `compileDebugAndroidTestKotlin` green
- [ ] TASK-4: delete the unused MockMaker resource — criterio: `jvmTest` green
- [ ] TASK-5: docs (AGENTS.md, PENDING.md, CHANGELOG.md, spec), re-index codebase-memory — criterio: ktlintCheck green, PR CI green

## Estrategia de entrega
`single-pr`: the diff outside pure renames is small (build file, file split, docs), well under ~400 lines.
One commit per task, one PR to `develop`. CI status is read from `gh api .../jobs` (Actions incidents can delay the check).

## Evidencia
(pending)

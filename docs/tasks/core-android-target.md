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
All resolved during implementation:
- U1: `androidTarget` + `com.android.library` works on AGP 8.9.2 / KGP 2.3.20 / Gradle 8.11.1. The plugin is declared in the catalog (`agp-library`, `apply false` at the root). `withJava()` had to go: it is incompatible with the Android plugin (and was deprecated).
- U2: a library without `AndroidManifest.xml` builds (`namespace` only).
- U3: core Android unit tests are disabled with `androidComponents.beforeVariants` using `HasHostTestsBuilder` (the `enableUnitTest` property is deprecated in AGP 8.9). Tests stay on `jvmTest`.
- U4: the `jvmAndroid` template group coexists with the default hierarchy; `ktlintJvmAndroidMainSourceSetCheck` runs and passes. It needs `@OptIn(ExperimentalKotlinGradlePluginApi::class)`.
- U5: `JavaFileOpener` returns `JavaResourceFile` (ImageIO), so only `JavaUserFile` moved to the shared set; `JavaResourceFile` and `JavaFileOpener` stay in `jvmMain`.
- U6: the app builds against the core Android variant (`processDebugManifest`, `writeDebugAarMetadata` run for core) and `compileDebugAndroidTestKotlin` passes.
Side effects found: (a) core's `jvmMain` dependencies no longer leak to the app; only `androidx.annotation` (1.9.1) was missing, so the app now declares it (guava, jsr305 and opencsv were already declared). (b) D8 prints Kotlin metadata warnings; they also occur on `develop` (verified with an uncached rebuild in a worktree).

## Checklist
TDD mode: active for build-structure work as red/green gradle checks (no project TDD config found).
Red check: `./gradlew :uhabits-core:compileDebugKotlinAndroid` fails now (task does not exist); safety net: `jvmTest` 274 tests, 0 failures with en-US locale.
- [x] TASK-1: declare `jvmAndroid` group and `git mv` the Android-safe files into `src/jvmAndroidMain` — criterio: `jvmTest` 274 green, `assembleDebug` green
- [x] TASK-2: split `JavaFiles.kt` (JavaResourceFile stays in jvmMain) — criterio: same checks green
- [x] TASK-3: add `androidTarget` and disable the core Android unit-test variant — criterio: `:uhabits-core:compileDebugKotlinAndroid` green, `assembleDebug`, `testDebugUnitTest`, `compileDebugAndroidTestKotlin` green
- [x] TASK-4: delete the unused MockMaker resource — criterio: `jvmTest` green
- [x] TASK-5: docs (AGENTS.md, PENDING.md, CHANGELOG.md, spec), re-index codebase-memory — criterio: ktlintCheck green, PR CI green

## Estrategia de entrega
`single-pr`: the diff outside pure renames is small (build file, file split, docs), well under ~400 lines.
One commit per task, one PR to `develop`. CI status is read from `gh api .../jobs` (Actions incidents can delay the check).

## Evidencia
Run on macOS with `JAVA_TOOL_OPTIONS="-Duser.language=en -Duser.country=US"`.
- RED: `:uhabits-core:compileDebugKotlinAndroid` failed with "task not found" before the change.
- GREEN after TASK-3: `:uhabits-core:jvmTest` (274 tests, 0 failures), `:uhabits-core:compileDebugKotlinAndroid`, `:uhabits-android:assembleDebug`, `:uhabits-android:testDebugUnitTest` (5 tests, 0 failures), `:uhabits-android:compileDebugAndroidTestKotlin`, `ktlintCheck`.
- Commits: `0f7139b` (move), `af926d4` (JavaUserFile), `33b67fd` (androidTarget), `6c219ba` (MockMaker), plus the docs commit.
- CI: see the PR checks.

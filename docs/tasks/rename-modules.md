# Rename the Gradle modules (KMP step 5b)

## Objetivo
Rename `uhabits-core` to `salem-core` and `uhabits-android` to `salem-android` without changing behaviour.

## Por qué
Step 5 of `.specs/migration-kmp.md` and `.specs/rename.md`. The packages are already `io.github.crisacm.salem` (5a); the module names still carry Loop's `uhabits` name.

## Alcance
- In: module directories, `settings.gradle.kts`, project references, CI task names, `build.sh` paths and APK names, `gradle/translators.gradle.kts`, docs (`AGENTS.md`, `docs/BUILD.md`, `docs/TEST.md`, `docs/PENDING.md`), two Kotlin comments.
- Out: `applicationId`, `DATABASE_FILENAME`, the external protocol, the `assets/` folder inside the core module, `build.sh` Loop branding and its cleanup of directories that no longer exist (tracked in `docs/PENDING.md`), historical `docs/tasks/*.md` and `CHANGELOG.md`.
- Decisions (user): odd-full; separate PR after the package rename.

## Exploración
- References to the old names (git grep): `build.sh` 26, `AGENTS.md` 26, `docs/PENDING.md` 6, `ci.yml` 6, `uhabits-android/build.gradle.kts` 3, `docs/BUILD.md` 3, `settings.gradle.kts` 2 (the `include` line is duplicated), `gradle/translators.gradle.kts` 1, `docs/TEST.md` 1, comments in `AppleFiles.kt` and `TestAssets.kt`.
- `JavaFiles.kt` opens `assets/main/...` relative to the working directory and Gradle sets `SALEM_ASSETS_DIR` from `file("assets")`, so `assets/` must stay inside the core module. `uhabits-android/.gitignore` refers to `../local.properties`, which keeps working when the directory is only renamed.
- Baseline on `develop` (42ee686): `jvmTest` 313, `iosSimulatorArm64Test` 439, Android unit tests 5.

## Checklist
- [x] TASK-1: task doc — criterio: this file committed
- [x] TASK-2: `git mv` of the two module directories alone — criterio: `git diff -M` shows only renames
- [x] TASK-3: references updated (Gradle, CI, `build.sh`, translators script, comments) — criterio: `./gradlew projects` lists `:salem-android` and `:salem-core`
- [x] TASK-4: full verification — criterio: same test counts, builds and `ktlintCheck` green
- [x] TASK-5: docs and re-index

## Estrategia de entrega
`single-pr`, commits: task doc, moves only, references, docs; base `develop`.

## Evidencia
- Same results as the baseline from a clean build: `jvmTest` 313, `iosSimulatorArm64Test` 439, Android unit tests 5, 0 failures; `assembleDebug`, `assembleRelease` (now `salem-android-release-unsigned.apk`), `compileDebugAndroidTestKotlin`, `compileKotlinIosArm64`, `ktlintCheck` pass; `lintDebug` gives 86 errors and 829 warnings, as before. `./gradlew projects` lists `:salem-android` and `:salem-core`. The four `incompatible version of Kotlin` lines of `assembleRelease` are the ones already present on `develop`.
- The directory commit contains only renames (`git diff -M` shows 760 files, no content change). The duplicated `include` line of `settings.gradle.kts` is now a single line.
- Left as they were on purpose: `build.sh` Loop branding and its legacy cleanup lines (`uhabits-android/uhabits-android`, `uhabits-core-legacy`; listed in `docs/PENDING.md`), historical `docs/tasks/*.md` and `CHANGELOG.md`.
- Not verified: `build.sh` commands (they need emulators or devices), external scripts that use the old paths, CI of the PR.

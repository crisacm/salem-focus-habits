# Rename the Kotlin packages (KMP step 5a)

## Objetivo
Move every Kotlin/Java package from Loop's `org.isoron.*` to `io.github.crisacm.salem.*` in one mechanical change, without touching behaviour.

## Por qué
Step 5 of `.specs/migration-kmp.md` and `.specs/rename.md`. The app id is already `io.github.crisacm.salem`, but the namespace and all packages are still `org.isoron.*`, which is Loop's identity and makes `namespace` differ from `applicationId`.

## Alcance
- In: packages and directories of both modules (443 Kotlin/Java files), `namespace` of both modules, manifest, layouts, `res/xml`, ProGuard, `ios-blocked-tests.txt`, `lint-baseline.xml`, androidTest strings, docs.
- Out: Gradle module names (5b), the external protocol, `DATABASE_FILENAME`, `applicationId`, the vendored `com.android.*` Java.
- Decisions (user): odd-full; Android code in the root package; modules in a separate PR afterwards.
- Mapping, in this order: `org.isoron.uhabits.core` -> `io.github.crisacm.salem.core`; `org.isoron.platform` -> `io.github.crisacm.salem.platform`; `org.isoron.uhabits` -> `io.github.crisacm.salem`.
- Must keep `org.isoron` (whitelist): intent actions `org.isoron.uhabits.ACTION_*` and the content URI/host `org.isoron.uhabits` (external protocol for Tasker and widgets), the Loop store links in `README.md` (`play.google.com/...id=org.isoron.uhabits`, `f-droid.org/app/org.isoron.uhabits`), the stale `market://` entry of `lint-baseline.xml`, and historical text in `CHANGELOG.md` and docs about Loop.

## Exploración
- Explorer brief: 430 Kotlin/Java files and 27 other files contain `org.isoron`; `org.isoron.platform` is a split package in both modules; directories mirror packages; manifest has 23 relative class names that follow the namespace; 11 layouts and 7 `res/xml` entries name classes; `ios-blocked-tests.txt` holds 7 class patterns; `HabitsApplication.isTestMode()` uses `Class.forName`.
- Baseline on `develop` (492aab8): `jvmTest` 313, `iosSimulatorArm64Test` 439, `assembleDebug`, `testDebugUnitTest`, `compileDebugAndroidTestKotlin`, `ktlintCheck` all green.

## Incertidumbre
- Import order after the rename (ktlint), matching of `lint-baseline.xml` paths, and the R8 keep rule are only known by running the builds (steps 3-4).

## Checklist
- [ ] TASK-1: task doc and baseline — criterio: this file committed
- [ ] TASK-2: mechanical move and text replacement — criterio: the whitelist is all that is left of `org.isoron`; `git diff -M` shows renames
- [ ] TASK-3: `ktlintFormat` import re-sort — criterio: `ktlintCheck` green
- [ ] TASK-4: full verification (`assembleDebug`, `assembleRelease`, `testDebugUnitTest`, `compileDebugAndroidTestKotlin`, `jvmTest`, `iosSimulatorArm64Test`, `compileKotlinIosArm64`, `lintDebug`)
- [ ] TASK-5: docs (AGENTS.md, README.md, PENDING.md, CHANGELOG.md) and re-index

## Estrategia de entrega
`single-pr` of mechanical commits (moves and replacements first, formatting second, docs last) so that `git diff -M` stays readable; base `develop`.

## Evidencia
(pending)

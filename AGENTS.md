## Memory
<!-- snippet-version: 1.0.0 -->

You have access to Engram persistent memory via MCP (mem_save, mem_search, mem_session_summary, etc.).

- Proactively save after significant work, without waiting to be asked.
- After any context compaction or reset, first persist the injected summary using mem_session_summary. Request mem_context only if more context is needed.

## Workflow
<!-- snippet-version: 1.1.0 -->

Before any coding task, evaluate whether the odd (normal work, the default methodology) or rdd (only if asked to review something already done) skill applies. Default to the `-lite` version (odd-lite, rdd-lite) and simply follow it, without announcing it.

If the complexity or risk of the task suggests the `-full` version, do NOT choose it on your own: ask the user which one to use before starting, recommend one, and wait for the answer. Keep the question short, stating what each option implies:

- odd: both versions always delegate exploration to the `explorer` subagent. Lite works directly afterwards (one task doc if substantial); full also resolves real uncertainty explicitly, uses TDD and plans delivery for large diffs (~400+ lines). Recommend full for changes with real uncertainty or likely large.
- rdd: lite reviews in the same session; full delegates medium/high-risk reviews to a `fresh-reviewer` subagent that only sees the diff (no self-review bias; more tokens). Recommend full for business logic, auth, payments, migrations or infra.

When delegating to `explorer`, launch it with a lighter model than your own, depending on which model you are (">=" means that version or higher):

- Claude Opus >= 5.5 or Sonnet >= 5.5 → `explorer` runs on Claude Haiku >= 5.5.
- Grok 4.5, 4.6 or 4.7 → `explorer` runs on Composer >= 2.5.
- GPT Sol >= 5.6 or Terra >= 5.6 → `explorer` runs on GPT Luna >= 6.

If your model is not listed, apply no restriction. If the required model is unavailable in the tool, tell the user before launching `explorer` with another one.

odd (both versions) needs the `explorer` agent installed; `rdd-full` needs `fresh-reviewer`. If an agent is missing, tell the user before continuing (for rdd, use `rdd-lite`). rdd is never started on your own: it requires an explicit request or the user's acceptance of your end-of-task offer.

When an odd task finishes, offer an RDD review in one short question: after odd-lite, offer a quick review (`rdd-lite`); after odd-full, offer a quick (`rdd-lite`) or deep (`rdd-full`) review, recommending the deep one for auth, payments, migrations or infra. Run it only if the user accepts.

## Code exploration
<!-- snippet-version: 1.0.0 -->

You have access to the codebase-memory-mcp knowledge graph (search_graph, trace_path, get_code_snippet, query_graph, get_architecture, detect_changes, etc.). Use it autonomously, without waiting to be asked, BEFORE reading or grepping files by hand when you need to:

- Explore or understand the codebase: architecture, structure, where something is defined.
- Follow relationships: who calls a function, what it calls, call chains, dependencies.
- Assess the impact of a change: before refactors or edits that touch shared code, and detect_changes over the current diff.
- Audit code quality: dead code, high fan-out, refactor candidates.

Use grep/search_code only for literals or when the graph does not cover the path (check_index_coverage). Skip the graph for trivial, single-file edits where the location is already known.

---

# Project guide

Everything below this line is project-specific knowledge for agents maintaining this repository. It was written by reading the code; if something here disagrees with the code, the code wins — fix this file.

## 1. Project overview

- **Salem - Focus & Habit Tracker** is a fork of [Loop Habit Tracker](https://github.com/iSoron/uhabits) (iSoron/uhabits), GPLv3. Goal: migrate to Jetpack Compose + Kotlin Multiplatform (KMP) and ship on the App Store (iOS) as well as Android. Original authorship must stay credited.
- **Current state: still the original Android app** (XML layouts + custom Views). There is no Compose and no iOS app yet (`uhabits-core` already has iOS targets with stub actuals). Open placeholders and TODOs are tracked in `docs/PENDING.md`.
- Identity: `applicationId = io.github.crisacm.salem`; `namespace` and all Kotlin packages are still `org.isoron.uhabits` (the package move is deferred to the KMP migration). App display name: `Salem`.
- Repository: `github.com/crisacm/salem-focus-habits`. Branches: `develop` (day-to-day work, PRs target it) and `main`.
- Docs for humans: `README.md`, `CONTRIBUTING.md`, `SECURITY.md`, `docs/GUIDELINES.md`, `docs/BUILD.md`, `docs/TEST.md`, `docs/PENDING.md`, `CHANGELOG.md`. Do not duplicate them here; link to them.

## 2. Repository map

| Path | Purpose |
|---|---|
| `uhabits-core/` | Kotlin Multiplatform library (targets `jvm`, `android`, `iosArm64` and `iosSimulatorArm64`; see `.specs/migration-kmp.md` step 2): domain model, persistence abstraction, commands, presenters ("behaviors"), chart data. 180 Kotlin files, 0 Java. |
| `uhabits-android/` | Android application module (depends on `project(":uhabits-core")`). 189 Kotlin files + 26 vendored Java files (AOSP pickers under `src/main/java/com/android/`). |
| `uhabits-core/assets/{main,test}` | Core resources, including SQL migrations `assets/main/migrations/NN.sql`. |
| `uhabits-android/src/main/play/` | Google Play metadata (listings only `en-US` and `es-rES`, release notes, contacts). |
| `docs/` | Developer docs (see above). |
| `.github/` | Issue templates, PR template and `workflows/ci.yml` (assemble, core JVM tests, Android unit tests, ktlint on PRs to `develop`). |
| `gradle/` | Version catalog `libs.versions.toml`, `translators.gradle.kts`, wrapper (Gradle 8.11.1). |
| `build.sh` | Legacy CI/emulator helper script (still Loop-branded). |
| `tools/parseInstrument.py` | Parses `adb shell am instrument -r` logs. |
| `translators-*.csv` | Translator credits; `translators-classic.csv` feeds the `updateTranslators` Gradle task. |
| `screenshots/` | Empty on purpose (`.gitkeep`). |
| `.engram/`, `AGENTS.md`, `local.properties` | Tooling/agent config and local SDK path (committed at the owner's request). |

**Never edit by hand:** `build/`, `.gradle/`, `.kotlin/`, `.idea/`, `kotlin-js-store/`, KSP/generated output inside module `build/` dirs, and the translator XML/layouts generated by `updateTranslators`.

## 3. Architecture

### 3.1 Modules
`uhabits-android` → `uhabits-core`. Core is platform-agnostic: it talks to the platform only through interfaces and `expect/actual` declarations. The Android module implements those interfaces and owns all UI, Android services and DI wiring.

### 3.2 uhabits-core (`src/<set>/kotlin/org/isoron/...`; `jvmMain` and `jvmTest` use `src/jvmMain/java` and `src/jvmTest/java` but contain Kotlin)
- `commonMain/platform/` — platform abstractions: `gui` (Canvas, Color, Image, View, FontAwesome), `io` (Database, Files, Strings `format`, Zip), `time` (`LocalDate`, `DayOfWeek`, `LocalDateFormatter`, `getToday/setToday`), `utils`. `JvmAnnotations.kt` declares `expect` `Synchronized`/`JvmStatic`.
- `commonMain/uhabits/core/`:
  - `models/` — domain model; `models/memory/` (in-memory) and `models/sqlite/` (SQLite-backed) implementations behind `ModelFactory`.
  - `commands/` — `Command { run() }` implementations (Archive, Unarchive, ChangeHabitColor, CreateHabit, CreateRepetition, DeleteHabits, EditHabit) executed by `CommandRunner`.
  - `database/` — `HabitRepository`, `EntryRepository`, `SQLParser`, `UnsupportedDatabaseVersionException`.
  - `io/` — importers (`GenericImporter` → `LoopDBImporter`, `RewireDBImporter`, `TickmateDBImporter`, `HabitBullCSVImporter`), `HabitsCSVExporter` (zip of CSVs), `Logging`.
  - `preferences/` — `Preferences` over a `Storage` interface (`MemoryStorage` in common), `WidgetPreferences`.
  - `tasks/` — `TaskRunner`/`CoroutineTaskRunner` (Main + IO dispatchers), `ExportCSVTask`.
  - `ui/` — presenters/behaviors (`screens/habits/list`, `screens/habits/show` + `show/views` cards, `views/` chart data and themes, `callbacks/`, `widgets/WidgetBehavior`), `NotificationTray`, `ThemeSwitcher`.
  - `test/HabitFixtures.kt` lives in **main** source (ships with the library).
  - `Constants.kt`: `DATABASE_FILENAME = "uhabits.db"`, `DATABASE_VERSION = 25`. `AppScope.kt`: kotlin-inject scope annotation.
- `jvmAndroidMain` (group shared by the jvm and android targets, `java.*` APIs only): `reminders/ReminderScheduler` (takes a `SystemScheduler` interface), `utils/MidnightTimer`, `JavaStrings`, `Zip`, `JavaUserFile`, date helpers, `JvmAnnotations`.
- `appleMain` (iOS): `TODO("iOS: step 4")` stub actuals for `ZipReader`/`ZipWriter`, `format`, `formatInvariant`, `computeToday` and `getFirstWeekdayNumberAccordingToLocale` (`AppleZip.kt`, `AppleStrings.kt`, `AppleDates.kt`). The real implementations come in migration steps 3 and 4.
- `jvmMain` only (desktop APIs: `java.awt`, JDBC): `JavaCanvas`, `JavaImage`, `JavaDatabase`, `JavaResourceFile`, `JavaFileOpener`. They back the `jvmTest` helpers; the Android app does not use them. The `js` target was removed.

### 3.3 Domain model essentials (`models/`)
- `LocalDate(daysSince2000: Int)`; "today" and first weekday are global and overridable (`setToday`, `resetToday`) — tests rely on this.
- `Entry(date, value, notes, completed)`. Values: `UNKNOWN=-1`, `NO=0`, `YES_AUTO=1`, `YES_MANUAL=2`, `SKIP=3`. For numerical habits `value` is the number ×1000.
- `Habit` (data class with mutable `var`s): color (`PaletteColor`), `Frequency(numerator, denominator)`, `Reminder(hour, minute, WeekdayList bitmask)`, `type` (YES_NO / NUMERICAL), target type/value, `uuid`, `position`, plus `originalEntries`, `computedEntries`, `scores`, `streaks`, a `ModelObservable`. `recompute()` runs: computed entries → completion → scores → streaks.
- Score: exponential moving average, `multiplier = 0.5^(sqrt(freq)/13)`; skipped days are ignored. Streaks: consecutive completed entries up to today.
- `EntryList`/`ScoreList`/`StreakList` are `@Synchronized`.

### 3.4 Persistence
- `Database`/`DatabaseOpener`/`PreparedStatement` abstraction (`platform/io/Database.kt`); schema version via `PRAGMA user_version`. Migrations are numbered SQL files run in order up to `DATABASE_VERSION`. JVM = JDBC (sqlite-jdbc, test dependency), Android = `AndroidDatabase`/`AndroidDatabaseOpener`.
- No sync code, no JSON export. Backups: `HabitsBackupAgent` (Google backup), `AutoBackup` (daily, keeps 5, optional SAF folder), CSV zip export, DB export/import tasks.

### 3.5 uhabits-android (`src/main/java/org/isoron/uhabits`)
- Entry: `HabitsApplication` (builds the DI component, initializes/repairs the DB, starts widget/reminder/notification listeners). Launcher alias `.MainActivity` → `ListHabitsActivity`.
- DI: kotlin-inject + KSP. `inject/HabitsApplicationComponent` (`@AppScope`, constructed with `@AppContext Context` and the db file) and `HabitsActivityComponent` (`@ActivityScope`, parent = app component, `@ActivityContext`). Non-activity classes get the global via `HabitsApplication.component`. `ReceiverScope` exists in `receivers/`.
- UI: programmatic Views + ~48 XML layouts + view binding in some screens + canvas-drawn charts (`activities/common/views/*Chart`, via `org/isoron/platform/gui/AndroidCanvas`). Pattern per screen: core `*Behavior`/presenter + Android `*Screen` / `*RootView` / `*Menu` / `*Adapter` implementing core interfaces (see `activities/habits/list`, `activities/habits/show`, `activities/about`, `automation/`).
- Other packages: `widgets/` (6 widget types, `WidgetUpdater`, RemoteViews bitmaps, `StackWidgetService`, picker dialogs), `receivers/` (`ReminderReceiver`, `WidgetReceiver`, `ReminderController`), `intents/` (`IntentFactory`, `IntentParser`, `IntentScheduler` using exact alarms, `PendingIntentFactory`), `notifications/`, `database/`, `preferences/` (SharedPreferences storage), `tasks/`, `automation/` (Tasker/Locale plugin), `utils/`.

### 3.6 Design patterns in use
Command (`commands/`), Observer (`ModelObservable`, `Preferences.Listener`, `CommandRunner.Listener`, `MidnightListener`), Repository (`HabitRepository`, `EntryRepository`), Factory (`ModelFactory`, `*TaskFactory`), Strategy/Adapter via interfaces and `expect/actual` (Database, Canvas, Files, SystemScheduler, SystemTray), MVP-style Behavior/Screen split, constructor DI with scopes.

## 4. Libraries and toolchain

Versions come from `gradle/libs.versions.toml` — **add or bump dependencies only there** and reference them as `libs.*`.

- Language/build: Kotlin 2.3.20, AGP 8.9.2, KSP 2.3.6, Gradle wrapper 8.11.1, JDK 17 (`jvmToolchain(17)`), compileSdk/targetSdk 36, minSdk 28, core library desugaring on. iOS targets need macOS with Xcode selected (`xcode-select -p` must point to Xcode, not the Command Line Tools); the first iOS build downloads Kotlin/Native into `~/.konan` (about 1.3 GB).
- DI: kotlin-inject 0.9.0 (runtime `compileOnly` in core; compiler via KSP in android).
- Async: kotlinx-coroutines 1.10.1. Network: ktor 1.6.8 (android/core/jackson/json; mock in tests).
- UI/Android: AppCompat 1.7.0, Material 1.12.0, AppIntro 6.3.1, Konfetti 2.0.2, `legacy-support-v4`, `legacy-preference-v14`, `documentfile`.
- Data/utils: Guava, OpenCSV, commons-codec, commons-lang3, JSR305.
- Tests: kotlin.test, JUnit 4/Jupiter, Hamcrest, Mokkery 3.3.0 (autofill mocks, class inheritance and concrete instantiation allowed), Espresso, UiAutomator, `androidx.test` rules, sqlite-jdbc.
- Lint/format: ktlint-gradle plugin 11.6.1 with defaults (no `.editorconfig`), Android lint (`abortOnError = false`, large `lint-baseline.xml`), ProGuard `-dontobfuscate` with `-keep class org.isoron.** {*;}`.

## 5. Commands

All run from the repo root. Add `--offline` when dependencies are already cached.

| Goal | Command |
|---|---|
| Debug APK | `./gradlew :uhabits-android:assembleDebug` |
| Compile Kotlin only (fast check) | `./gradlew :uhabits-android:compileDebugKotlin` |
| Core JVM tests | `./gradlew :uhabits-core:jvmTest` |
| Compile core for iOS | `./gradlew :uhabits-core:compileKotlinIosSimulatorArm64 :uhabits-core:compileKotlinIosArm64` (macOS only; the iOS test source sets have no actuals yet, so do not use `check` or `allTests` on macOS until step 4) |
| Compile core for Android | `./gradlew :uhabits-core:compileDebugKotlinAndroid` (core tests run only on `jvm`: the Android unit-test variant is disabled) |
| Android JVM unit tests | `./gradlew :uhabits-android:testDebugUnitTest` |
| Instrumented tests (device/emulator) | `./gradlew :uhabits-android:connectedDebugAndroidTest` or `./build.sh android-tests <API>` |
| Compile instrumented sources | `./gradlew :uhabits-android:compileDebugAndroidTestKotlin` |
| Lint / format | `./gradlew ktlintCheck` / `./gradlew ktlintFormat` (per module: `:uhabits-core:`, `:uhabits-android:`) |
| Regenerate translator lists | `./gradlew updateTranslators` |
| Emulator helpers | `./build.sh android-setup <API>`, `android-tests-parallel`, `android-accept-images` (needs `ANDROID_HOME`) |

Release signing reads env vars `LOOP_KEY_ALIAS`, `LOOP_KEY_PASSWORD`, `LOOP_KEY_STORE`, `LOOP_STORE_PASSWORD`; without `LOOP_KEY_ALIAS` the release build is unsigned.

## 6. Conventions

- **License header:** every source file starts with the GPLv3 header with the original copyright ("Copyright (C) 2016-2025 Álinson Santos Xavier"). Keep it on existing files and use the same header on new files.
- **Language:** new code in Kotlin (Java conversions welcome). Docs, comments, commit messages and PRs in English.
- **Style:** ktlint defaults; run `ktlintCheck` before finishing. Match the surrounding code (naming, comment density, idioms).
- **Naming:** `Android*` for Android implementations of core abstractions, `*Behavior` for core presenters, `*Screen`/`*RootView`/`*Menu`/`*Adapter` for Android UI glue, `*Test` for tests. Models are data classes with `var`s; nullable values use `*OrNull` getters; shared model lists are `@Synchronized`.
- **Strings:** add UI strings to `uhabits-android/src/main/res/values/strings.xml`; do not hand-edit translations. Only `app_name` was changed to "Salem" in the ~55 `values-*` locales; other translated strings may still say "Loop".
- **Changelog:** user-visible or project-level changes go under the Salem `## [Unreleased]` section of `CHANGELOG.md` (Keep a Changelog). The history below it belongs to Loop and must not be rewritten.
- **Git:** Conventional Commits (`feat:`, `fix:`, `docs:`, `chore:`…) ending with the required `Co-Authored-By` trailer; one logical change per commit; work on `develop`; commit and push **only when the user asks**; never force-push; never rewrite published history.
- **PRs:** target `develop`, small and focused, follow `.github/pull_request_template.md` and `docs/GUIDELINES.md`.
- **Security/privacy:** the default sync server URL is intentionally empty — do not reintroduce third-party endpoints. Never commit secrets or keystores.

## 7. Testing

- **Core** (`uhabits-core/src/commonTest`): kotlin.test + `kotlinx-coroutines-test` (`runTest`). Base class `BaseUnitTest` fixes today to 2015-01-25, builds the in-memory model factory and `HabitFixtures`. Platform-specific helpers via `expect/actual` (`TestDatabaseHelper`, `TestPlatformHelper`) in `jvmTest`. Mokkery is used for presenter/UI tests. Migration tests follow `database/migrations/Version22Test`/`Version23Test`.
- **Android JVM** (`uhabits-android/src/test`): `BaseAndroidJVMTest`, `ReminderControllerTest`.
- **Instrumented** (`uhabits-android/src/androidTest`): bases `BaseAndroidTest`, `BaseViewTest`, `BaseUserInterfaceTest`; DI test components `HabitsApplicationTestComponent`/`HabitsActivityTestComponent`; suites for acceptance, views, widgets, database, intents, regression, performance. View tests render a View/widget to a bitmap and compare to PNGs in `src/androidTest/assets/views/**` (`assertRenders`, `similarityCutoff = 0.00018`); accept new images with `./build.sh android-accept-images`. They require a clean emulator (Nexus 4 profile, en-US, animations off) — see `docs/TEST.md`.
- **Rule:** put new logic in core where possible and cover it with a core test; every bug fix gets a regression test when feasible. Finish a change by running the build, ktlint and the tests relevant to the touched module.

## 8. Known limitations and gotchas

- Core is an Android library (`namespace org.isoron.uhabits.core`, `androidTarget` with `com.android.library`). Its `jvmMain` dependencies no longer reach the app at runtime, so the app declares what it uses (e.g. `androidx.annotation`). D8 (R8 8.9.35 in AGP 8.9.2) prints `Unexpected error during rewriting of Kotlin metadata` warnings for Kotlin 2.3 classes; they existed before and are harmless.
- No Compose and no iOS app. CI is limited to `.github/workflows/ci.yml`: an ubuntu job (build, tests, ktlint) and a macOS job that compiles the iOS targets (no instrumented tests, no iOS tests). Core `jvmTest` pins the locale to en-US so tests do not depend on the machine; data written to files (CSV, migration file names) uses `formatInvariant`, not the locale-sensitive `format` (which is for UI text).
- `applicationId` ≠ `namespace`. Keep these consistent: FileProvider authority is `${applicationId}` in the manifest and `packageName` in `ViewExtensions.showSendFileScreen`; `BackupManager.dataChanged(requireContext().packageName)`; `res/xml/preferences.xml` hardcodes `targetPackage="io.github.crisacm.salem"`; `build.sh` has `PACKAGE_NAME=io.github.crisacm.salem`; androidTest constants use the new id.
- Intent actions `org.isoron.uhabits.ACTION_*` and the URI `content://org.isoron.uhabits/habit/<id>` (`Habit.uriString`, manifest `<data android:host>`) are an external protocol (Tasker, widgets) — change them only together and deliberately.
- `HabitsApplication.isTestMode()` detects tests by `Class.forName("org.isoron.uhabits.BaseAndroidTest")`.
- Reminders and `MidnightTimer` exist only on JVM/Android; JS has no scheduler. `HabitFixtures` is in commonMain.
- Database changes: add `NN+1.sql`, bump `DATABASE_VERSION`, add a migration test, and keep importers working (`LoopDBImporter` rejects newer versions and migrates older).
- Manifest still contains Loop's `com.google.android.backup.api_key` meta-data; `build.sh` still prints Loop branding, lists removed legacy dirs and names backups `Loop Habits Backup *.db`. Treat as to-do, not as intended design.
- Deprecated/legacy: `legacy-support-v4`, `legacy-preference-v14`, `@Deprecated` overrides in `SettingsFragment.kt` and `ListHabitsActivity.kt`, `TODO("Not yet implemented")` in `platform/gui/AndroidImage.kt`.
- Play listings contain only English and Spanish; `contact-email.txt` is a placeholder; the Play URL in `constants.xml` points to the GitHub repo until the app is published.

## 9. Task playbooks

- **Add a DB migration:** new `uhabits-core/assets/main/migrations/26.sql`, bump `DATABASE_VERSION`, update repositories/importers if columns change, add `Version26Test` modelled on `Version23Test`, run `:uhabits-core:jvmTest`.
- **Add a habit command:** implement `Command` in `core/commands`, run it through `CommandRunner`, test with `BaseUnitTest`, wire it from a `*Behavior`/Android screen.
- **Add a setting:** key + accessor in core `Preferences`, entry in `res/xml/preferences.xml` and `strings.xml`, handle in `SettingsFragment`; no hardcoded user-visible strings.
- **Add a widget type:** follow an existing pair (`*Widget` + `*WidgetProvider`), register in the manifest with its `res/xml/widget_*_info.xml`, add widget view tests.
- **Change UI strings:** edit `values/strings.xml` only; mention in the summary that translations are not updated.
- **Rename/move packages or modules:** out of scope until the KMP phase (see `docs/PENDING.md`); needs a dedicated plan (manifest, `preferences.xml`, ProGuard, tests, `build.sh`, `Habit.uriString`).
- **Before finishing any change:** `./gradlew :uhabits-android:assembleDebug`, `ktlintCheck`, relevant tests, update `CHANGELOG.md` if user-facing, and report honestly what could not be run.

## 10. Code exploration index (codebase-memory-mcp)

Status: **indexed on 2026-10-21** (project id `Users-cristianc-Desktop-Claude-Cowork-salem-focus-habits`, 6,794 nodes, 31,864 edges). The index was built with the CLI (`codebase-memory-mcp cli index_repository --repo_path <root>`) because the MCP server timed out on connect (`CONNECT_TIMEOUT`); if the tools are missing in a session, reconnect with `/mcp`. 108 files are intentionally not indexed (gitignore, binaries such as PNG/TTF, `local.properties`). Parsed only partially: `gradlew` and SQL migrations `09`, `17`, `19`, `22`. Use `detect_changes` after large edits and re-run `index_repository` after big refactors (e.g. the package move). Use Grep/Read for literals, SQL and Gradle files.

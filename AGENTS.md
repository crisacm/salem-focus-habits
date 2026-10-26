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
- **Current state: still the original Android app** (XML layouts + custom Views). There is no iOS app with real screens yet (`salem-core` has iOS targets with a few stub actuals; `salem-ui` and `iosApp` hold the shared UI base). Open placeholders and TODOs are tracked in `docs/PENDING.md`.
- Identity: `applicationId = io.github.crisacm.salem`; `namespace` (the Android app) is the same id, and the Kotlin packages are `io.github.crisacm.salem` (Android code in the root package), `io.github.crisacm.salem.core` and `io.github.crisacm.salem.platform`; the Gradle modules are `salem-core` and `salem-android`. App display name: `Salem`.
- Repository: `github.com/crisacm/salem-focus-habits`. Branches: `develop` (day-to-day work, PRs target it) and `main`.
- Docs for humans: `README.md`, `CONTRIBUTING.md`, `SECURITY.md`, `docs/GUIDELINES.md`, `docs/BUILD.md`, `docs/TEST.md`, `docs/PENDING.md`, `CHANGELOG.md`. Do not duplicate them here; link to them.

## 2. Repository map

| Path | Purpose |
|---|---|
| `salem-core/` | Kotlin Multiplatform library (targets `jvm`, `android`, `iosArm64` and `iosSimulatorArm64`; see `.specs/migration-kmp.md` step 2): domain model, persistence abstraction, commands, presenters ("behaviors"), chart data. 180 Kotlin files, 0 Java. |
| `salem-ui/` | Compose Multiplatform library (targets `androidTarget`, `iosArm64`, `iosSimulatorArm64`, plus a `desktop` JVM target only for rendering tests; step 7): `DrawScopeCanvas`/`CoreCanvasView`/`CoreChart` (the core `Canvas` over a Compose `DrawScope`, in `ui/canvas`; text is placed like `AndroidCanvas`, with a `CoreChart` that scrolls and takes taps), `SalemUiSample`, the iOS entry `MainViewController`, the shared `SalemTheme`/`toColorScheme`/`resolveTheme` (`ui/compose`, same package as the Android overload) and `SalemFonts`; its Compose resources (strings of every language and the core fonts) are generated by `syncSharedStrings`. Depends on `salem-core`; the habit list below the screen shell is here (`ui/list`: models, `HabitListState`/`HabitListController`/menus without DI annotations, cells, rows, header, empty state, hint; `ui/compose/ChartScrollState`); the bar (`ListHabitsBar`, `ListIcons`), the screen (`ListHabitsContent` over a `ListScreenModel`) and `TaskProgress` too; `salem-android` keeps `ListHabitsHost` (implements the model), `ListHabitsRoot` (condensed font, theme, side insets), `ListHabitsScreen` and the activity. The dialogs and popups of the editor and the checkmark and number cells (`ui/dialogs`: habit type, choice, frequency, delete, `CheckmarkPopup`, `NumberPopup`, `NotesSession`) and `DialogSurface` (`ui/compose`) are here too, hosted on Android by thin Fragments; the colour, weekday and time pickers (`ColorPickerContent`, `WeekdayPickerContent`, `TimePickerContent`, `docs/tasks/ui-shared-pickers.md`) too; the helpers of the editor (`HabitEditorSaver` in core; `ui/edit`: `EditHabitFormSaver`, `frequencyText`, `weekdaysText`, `rememberTimeFormat()`, `docs/tasks/ui-edit-helpers.md`) are shared too; the editor screen (`EditHabitScreen`, `docs/tasks/ui-edit-screen.md`) is shared too, and the habit detail, About, Intro and Settings are shared too (`ui/detail`, `ui/about`, `ui/intro`, `ui/settings`). |
| `iosApp/` | Xcode project (`iosApp.xcodeproj`, SwiftUI shell hosting `MainViewController`, which shows `IosListRoot` from `salem-ui/src/iosMain/.../ui/ios`: `IosHabitsGraph` builds the database, commands and state holders by hand, `IosListScreenModel`, `IosThemeSwitcher`, `IosListScreen` (opens the dialogs, popups and the editor through `ListOverlays`, drawn by the shared `ListOverlayHost`, and the habit detail through `DetailNavigation`, drawn by the shared `HabitDetailRoute`; the other screens still log), the reminders (`IosReminders`: scheduler, tray, Yes/No actions through `IosNotificationRouter`, refresh through `AppForegroundRefresher`, the permission through `ReminderPermissionPrompter`, the import of data through `IosFilePicker`/`IosImport` over `ImportRunner` and `ImportFlow` with a `TransientMessage` banner (`docs/tasks/ios-import.md`); `docs/tasks/ios-reminders-wiring.md`), debug seed with `SALEM_SEED_DEBUG_DATA=1`). Build phases: Gradle `:salem-ui:embedAndSignAppleFrameworkForXcode` and a copy of `salem-core/assets/main` into the bundle `assets` folder. Simulator only, no signing team. |
| `salem-android/` | Android application module (depends on `project(":salem-core")`). Kotlin only since the vendored AOSP date and time pickers were replaced by the Compose `TimePickerDialog`. |
| `salem-core/assets/{main,test}` | Core resources, including SQL migrations `assets/main/migrations/NN.sql`. |
| `salem-android/src/main/play/` | Google Play metadata (listings only `en-US` and `es-rES`, release notes, contacts). |
| `docs/` | Developer docs (see above). |
| `.github/` | Issue templates, PR template and `workflows/ci.yml` (assemble, core JVM tests, Android unit tests, ktlint on every pull request, on pushes to `develop` and on manual runs). |
| `gradle/` | Version catalog `libs.versions.toml`, `translators.gradle.kts`, wrapper (Gradle 8.11.1). |
| `build.sh` | Legacy CI/emulator helper script (still Loop-branded). |
| `tools/parseInstrument.py` | Parses `adb shell am instrument -r` logs. |
| `translators-*.csv` | Translator credits; `translators-classic.csv` feeds the `updateTranslators` Gradle task. |
| `screenshots/` | Empty on purpose (`.gitkeep`). |
| `.engram/`, `AGENTS.md`, `local.properties` | Tooling/agent config and local SDK path (committed at the owner's request). |

**Never edit by hand:** `build/`, `.gradle/`, `.kotlin/`, `.idea/`, `kotlin-js-store/`, KSP/generated output inside module `build/` dirs, and the translators list generated by `updateTranslators` (`salem-ui` `ui/about/AboutTranslators.kt`).

## 3. Architecture

### 3.1 Modules
`salem-android` → `salem-core`, and `salem-ui` → `salem-core` (the iOS app links `salem-ui`; `salem-android` depends on it for the shared theme and will use more as screens move). Core is platform-agnostic: it talks to the platform only through interfaces and `expect/actual` declarations. The Android module implements those interfaces and owns all UI, Android services and DI wiring.

### 3.2 salem-core (`src/<set>/kotlin/io/github/crisacm/salem/...`; `jvmMain` and `jvmTest` use `src/jvmMain/java` and `src/jvmTest/java` but contain Kotlin)
- `commonMain/.../salem/platform/` — platform abstractions: `gui` (Canvas, Color, Image, View, FontAwesome), `io` (Database, Files, Strings `format`, Zip), `time` (`LocalDate`, `DayOfWeek`, `LocalDateFormatter`, `getToday/setToday`), `utils`. `JvmAnnotations.kt` declares `expect` `Synchronized`/`JvmStatic`.
- `commonMain/.../salem/core/`:
  - `models/` — domain model; `models/memory/` (in-memory) and `models/sqlite/` (SQLite-backed) implementations behind `ModelFactory`.
  - `commands/` — `Command { run() }` implementations (Archive, Unarchive, ChangeHabitColor, CreateHabit, CreateRepetition, DeleteHabits, EditHabit) executed by `CommandRunner`.
  - `database/` — `HabitRepository`, `EntryRepository`, `SQLParser`, `UnsupportedDatabaseVersionException`.
  - `io/` — importers (`GenericImporter` → `LoopDBImporter`, `RewireDBImporter`, `TickmateDBImporter`, `HabitBullCSVImporter`), `HabitsCSVExporter` (zip of CSVs), `Logging`.
  - `preferences/` — `Preferences` over a `Storage` interface (`MemoryStorage` in common), `WidgetPreferences`.
  - `tasks/` — `TaskRunner`/`CoroutineTaskRunner` (Main + IO dispatchers), `ExportCSVTask`.
  - `ui/` — presenters/behaviors (`screens/habits/list`, `screens/habits/show` + `show/views` cards, `views/` chart data and themes, `callbacks/`, `widgets/WidgetBehavior`), `NotificationTray`, `ThemeSwitcher`.
  - `test/HabitFixtures.kt` lives in **main** source (ships with the library).
  - `Constants.kt`: `DATABASE_FILENAME = "uhabits.db"`, `DATABASE_VERSION = 25`. `AppScope.kt`: kotlin-inject scope annotation.
- `jvmAndroidMain` (group shared by the jvm and android targets, `java.*` APIs only): `JavaStrings`, `Zip`, `JavaUserFile`, date helpers, `JvmAnnotations`.
- `appleMain` (iOS): `UserDefaultsStorage` (`Preferences.Storage` over `NSUserDefaults`), `SqliterDatabase`/`SqliterPreparedStatement`/`SqliterDatabaseOpener` (`Database` over sqliter, see 3.4) with `SqliteFileHeader.peekUserVersion`, tested on the simulator from `appleTest` and by the shared database, repository and list tests, and `ZipReader`/`ZipWriter` (`AppleZip.kt`: zip container written in Kotlin, raw DEFLATE and CRC-32 through `platform.zlib` in `AppleDeflate.kt`; STORED and DEFLATED entries, no ZIP64, non-zip input throws; checked against the JDK with fixtures in `assets/test/zip`). `format`/`formatInvariant` (`AppleStrings.kt`) use the pure-Kotlin `formatPrintf` of commonMain (`platform/io/Printf.kt`: `%d %f %s`, rounding HALF_UP like Java, parity-tested against `String.format` on the JVM); `format` takes the decimal separator from `NSLocale`. `AppleDates.kt` has the real `computeToday` and `getFirstWeekdayNumberAccordingToLocale` (NSCalendar/NSTimeZone). `AppleFiles.kt` has `AppleFileOpener` (roots injected: user files in Application Support, resources in the bundle `assets` folder via `createDefault()`; the app target must copy `salem-core/assets/main` there in step 7), `AppleUserFile` and `AppleResourceFile` (`toImage()` throws until steps 6-7). `AppleLocalDateFormatter` (`appleMain/platform/time`) builds names from `NSDateFormatter` symbols for a given `NSLocale`. Reminders and notifications (`appleMain/platform/notifications`): `AppleSystemScheduler` (a `ReminderScheduler.SystemScheduler`: one local notification per enabled day of the next 7 days, `reminder-<habitId>-<day>`, because iOS runs no code when a notification fires; weekday mask applied when scheduling via `upcomingReminderTimes`; the 64 pending limit keeps the earliest, `reminderIdsToDrop`), `AppleSystemTray` (`NotificationTray.SystemTray`, shown notifications use `shown-<habitId>`), Yes/No actions (`buildReminderCategory`, `parseNotificationResponse`, `AppleNotificationDelegate`), `HabitCompletionRearmer` (moves the reminder to tomorrow when the habit is done), `AppleReminderSync` (clears and schedules again after each command, used instead of `ReminderScheduler.startListening()`, because deleted, archived or reminder-less habits would otherwise keep their pending notifications), `AppForegroundRefresher` and `AppLifecycleObserver` (iOS has no background timer: on launch and when returning from the background they refresh the day, the first weekday and call `clear()` plus `scheduleAll()`). The real notification center cannot be created in the unit-test host, so it sits behind `PendingNotificationCenter`/`DeliveredNotificationCenter` with fakes in `appleTest`; the app wires everything (checklist in `docs/tasks/ios-reminders.md`). The only missing iOS implementation is Canvas/Image, which waits for steps 6-7. `appleTest` still has a `TODO` stub actual for `createTestCanvas` so the iOS test sources link; the stubs throw if a test uses them. sqliter hides the SQLite error message (`sqlite3_step failed`), which keeps four `Version22Test` cases blocked.
- `jvmMain` only (desktop APIs: `java.awt`, JDBC): `JavaCanvas`, `JavaImage`, `JavaDatabase`, `JavaResourceFile`, `JavaFileOpener`. They back the `jvmTest` helpers; the Android app does not use them. The `js` target was removed.

### 3.3 Domain model essentials (`models/`)
- `LocalDate(daysSince2000: Int)`; "today" and first weekday are global and overridable (`setToday`, `resetToday`) — tests rely on this.
- `Entry(date, value, notes, completed)`. Values: `UNKNOWN=-1`, `NO=0`, `YES_AUTO=1`, `YES_MANUAL=2`, `SKIP=3`. For numerical habits `value` is the number ×1000.
- `Habit` (data class with mutable `var`s): color (`PaletteColor`), `Frequency(numerator, denominator)`, `Reminder(hour, minute, WeekdayList bitmask)`, `type` (YES_NO / NUMERICAL), target type/value, `uuid`, `position`, plus `originalEntries`, `computedEntries`, `scores`, `streaks`, a `ModelObservable`. `recompute()` runs: computed entries → completion → scores → streaks.
- Score: exponential moving average, `multiplier = 0.5^(sqrt(freq)/13)`; skipped days are ignored. Streaks: consecutive completed entries up to today.
- `EntryList`/`ScoreList`/`StreakList` are `@Synchronized`.

### 3.4 Persistence
- `Database`/`DatabaseOpener`/`PreparedStatement` abstraction (`platform/io/Database.kt`); schema version via `PRAGMA user_version`. Migrations are numbered SQL files run in order up to `DATABASE_VERSION`. iOS = `SqliterDatabase` over sqliter (`co.touchlab:sqliter-driver`, the engine under SQLDelight's native driver; SQLDelight's `SqlDriver` API was rejected because its cursor has no column count and it does not see uncommitted rows after a raw `BEGIN`). The iOS opener reads `user_version` from the SQLite file header first, because sqliter rewrites the version of a file when it is opened with a newer one; it opens with `JournalMode.DELETE` (single file) and a new file reports version 0. JVM = JDBC (sqlite-jdbc, test dependency), Android = `AndroidDatabase`/`AndroidDatabaseOpener`.
- No sync code, no JSON export. Backups: `HabitsBackupAgent` (Google backup), `AutoBackup` (daily, keeps 5, optional SAF folder), CSV zip export, DB export/import tasks.

### 3.5 salem-android (`src/main/java/io/github/crisacm/salem`)
- Entry: `HabitsApplication` (builds the DI component, initializes/repairs the DB, starts widget/reminder/notification listeners). Launcher alias `.MainActivity` → `ListHabitsActivity`.
- DI: kotlin-inject + KSP. `inject/HabitsApplicationComponent` (`@AppScope`, constructed with `@AppContext Context` and the db file) and `HabitsActivityComponent` (`@ActivityScope`, parent = app component, `@ActivityContext`). Non-activity classes get the global via `HabitsApplication.component`. `ReceiverScope` exists in `receivers/`.
- UI: programmatic Views + ~48 XML layouts + view binding in some screens + canvas-drawn charts (`activities/common/views/*Chart`, via `platform/gui/AndroidCanvas`). The habit list rows are Compose (`HabitListState` over `HabitCardListCache`, `HabitListController`, `ListRows.kt`); the rest of the Views follow the pattern per screen: core `*Behavior`/presenter + Android `*Screen` / `*RootView` / `*Menu` / `*Adapter` implementing core interfaces (see `activities/habits/list`, `activities/habits/show`, `activities/about`, `automation/`).
- Other packages: `widgets/` (6 widget types, `WidgetUpdater`, RemoteViews bitmaps, `StackWidgetService`, picker dialogs), `receivers/` (`ReminderReceiver`, `WidgetReceiver`, `ReminderController`), `intents/` (`IntentFactory`, `IntentParser`, `IntentScheduler` using exact alarms, `PendingIntentFactory`), `notifications/`, `database/`, `preferences/` (SharedPreferences storage), `tasks/`, `automation/` (Tasker/Locale plugin), `utils/`.

### 3.6 Design patterns in use
Command (`commands/`), Observer (`ModelObservable`, `Preferences.Listener`, `CommandRunner.Listener`, `MidnightListener`), Repository (`HabitRepository`, `EntryRepository`), Factory (`ModelFactory`, `*TaskFactory`), Strategy/Adapter via interfaces and `expect/actual` (Database, Canvas, Files, SystemScheduler, SystemTray), MVP-style Behavior/Screen split, constructor DI with scopes.

## 4. Libraries and toolchain

Versions come from `gradle/libs.versions.toml` — **add or bump dependencies only there** and reference them as `libs.*`.

- Language/build: Kotlin 2.3.20, AGP 8.9.2, KSP 2.3.6, Gradle wrapper 8.11.1, JDK 17 (`jvmToolchain(17)`), compileSdk/targetSdk 36, minSdk 28, core library desugaring on. iOS targets need macOS with Xcode selected (`xcode-select -p` must point to Xcode, not the Command Line Tools); the first iOS build downloads Kotlin/Native into `~/.konan` (about 1.3 GB). Native binaries link `-lsqlite3` (set in `salem-core/build.gradle.kts`; the future iOS app framework needs it too).
- DI: the state holders that live in `salem-ui` have no annotations; `HabitsActivityComponent` builds them with `@Provides` (`habitListState`, `habitListController`, `listHabitsMenu`, `listHabitsSelectionMenu`). `toShortString` is the one in `salem-core` (`NumberButton.kt`). Do not give a file of `salem-android` the same name as a file of `salem-ui` in the same package (both compile to the same facade class, as `SalemTheme.kt` did; the Android one is `AndroidSalemTheme.kt`).
- DI: kotlin-inject 0.9.0 (runtime `implementation` in core, because Kotlin/Native does not support `compileOnly`; compiler via KSP in android).
- Async: kotlinx-coroutines 1.10.1. No network library: the app has no sync code (Ktor was removed).
- UI/Android: Jetpack Compose (BOM 2026.02.00, Material 3, activity-compose, compiler plugin = Kotlin version), AppCompat 1.7.0, Material 1.12.0, Konfetti 2.0.2, `legacy-support-v4`, `legacy-preference-v14`, `documentfile`.
- Data/utils: no third-party utility libraries (Guava, OpenCSV, Apache Commons, JSR305 were unused and removed); CSV is parsed and written in `platform/io/Strings.kt` and `core/io`.
- Tests: kotlin.test, JUnit 4/Jupiter, Hamcrest, Mokkery 3.3.0 (autofill mocks, class inheritance and concrete instantiation allowed), Espresso, UiAutomator, `androidx.test` rules, sqlite-jdbc.
- Lint/format: ktlint-gradle plugin 11.6.1 with defaults (no `.editorconfig`), Android lint (`abortOnError = false`, large `lint-baseline.xml`), ProGuard `-dontobfuscate` with `-keep class io.github.crisacm.salem.** {*;}`.

## 5. Commands

All run from the repo root. Add `--offline` when dependencies are already cached.

| Goal | Command |
|---|---|
| Debug APK | `./gradlew :salem-android:assembleDebug` |
| Compile Kotlin only (fast check) | `./gradlew :salem-android:compileDebugKotlin` |
| Core JVM tests | `./gradlew :salem-core:jvmTest` |
| Compile core for iOS | `./gradlew :salem-core:compileKotlinIosSimulatorArm64 :salem-core:compileKotlinIosArm64` (macOS only) |
| Run iOS tests (simulator) | `./gradlew :salem-core:iosSimulatorArm64Test` (needs an iOS Simulator runtime; Gradle passes `SALEM_ASSETS_DIR` through `SIMCTL_CHILD_SALEM_ASSETS_DIR` so tests read `salem-core/assets`). It runs the whole commonTest and appleTest suite except the tests listed in `salem-core/ios-blocked-tests.txt`, which need the Canvas/Image actual (steps 6-7) or a driver that reports SQLite messages; add `-PiosRunBlocked=true` to run them anyway. `allTests` and `check` work on macOS. Filter with `--tests "*SomeTest*"` |
| Shared UI tests (JVM, Skia) | `./gradlew :salem-ui:desktopTest` (renders core charts and rings with `DrawScopeCanvas` and compares them with `salem-core/assets/test`, tolerance 0.2 for the rings and a limit per chart in `ChartRenderTest`) |
| Build the iOS app (simulator) | `xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp -configuration Debug -sdk iphonesimulator -destination 'generic/platform=iOS Simulator' CODE_SIGNING_ALLOWED=NO build` (runs Gradle for the `SalemUi` framework; macOS only) |
| Compile core for Android | `./gradlew :salem-core:compileDebugKotlinAndroid` (core tests run only on `jvm`: the Android unit-test variant is disabled) |
| Android JVM unit tests | `./gradlew :salem-android:testDebugUnitTest` |
| Instrumented tests (device/emulator) | `./gradlew :salem-android:connectedDebugAndroidTest` or `./build.sh android-tests <API>` |
| Compile instrumented sources | `./gradlew :salem-android:compileDebugAndroidTestKotlin` |
| Lint / format | `./gradlew ktlintCheck` / `./gradlew ktlintFormat` (per module: `:salem-core:`, `:salem-android:`) |
| Regenerate translator lists | `./gradlew updateTranslators` |
| Emulator helpers | `./build.sh android-setup <API>`, `android-tests-parallel`, `android-accept-images` (needs `ANDROID_HOME`) |

Release signing reads env vars `LOOP_KEY_ALIAS`, `LOOP_KEY_PASSWORD`, `LOOP_KEY_STORE`, `LOOP_STORE_PASSWORD`; without `LOOP_KEY_ALIAS` the release build is unsigned.

## 6. Conventions

- **License header:** every source file starts with the GPLv3 header with the original copyright ("Copyright (C) 2016-2025 Álinson Santos Xavier"). Keep it on existing files and use the same header on new files.
- **Language:** new code in Kotlin (Java conversions welcome). Docs, comments, commit messages and PRs in English.
- **Style:** ktlint defaults; run `ktlintCheck` before finishing. Match the surrounding code (naming, comment density, idioms).
- **Naming:** `Android*` for Android implementations of core abstractions, `*Behavior` for core presenters, `*Screen`/`*RootView`/`*Menu`/`*Adapter` for Android UI glue, `*Test` for tests. Models are data classes with `var`s; nullable values use `*OrNull` getters; shared model lists are `@Synchronized`.
- **Strings:** add UI strings to `salem-android/src/main/res/values/strings.xml`; do not hand-edit translations. `salem-ui` gets them through the `syncSharedStrings` Gradle task (generated under `salem-ui/build`, never committed): `%d`/`%s` become positional placeholders, `\'` and `\"` are unescaped, `in`/`iw`/`no` folders are renamed to `id`/`he`/`nb` (and `no` is kept). Use `%1$s`-style or plain `%d`/`%s` in new strings; do not reference other strings from a string-array for shared screens. The same task copies the core fonts (`font/fontawesome`, `font/notosans_regular`, `font/notosans_bold`) and the intro images (`drawable/intro_icon_*`); do not add files to `salem-ui/src/commonMain/composeResources` (the custom directory replaces it). Only `app_name` was changed to "Salem" in the ~55 `values-*` locales; other translated strings may still say "Loop".
- **Changelog:** user-visible or project-level changes go under the Salem `## [Unreleased]` section of `CHANGELOG.md` (Keep a Changelog). The history below it belongs to Loop and must not be rewritten.
- **Git:** Conventional Commits (`feat:`, `fix:`, `docs:`, `chore:`…) ending with the required `Co-Authored-By` trailer; one logical change per commit; work on `develop`; commit and push **only when the user asks**; never force-push; never rewrite published history.
- **PRs:** target `develop`, small and focused, follow `.github/pull_request_template.md` and `docs/GUIDELINES.md`.
- **Security/privacy:** the default sync server URL is intentionally empty — do not reintroduce third-party endpoints. Never commit secrets or keystores.

## 7. Testing

- **Core** (`salem-core/src/commonTest`): kotlin.test + `kotlinx-coroutines-test` (`runTest`). Base class `BaseUnitTest` fixes today to 2015-01-25, builds the in-memory model factory and `HabitFixtures`. Platform-specific helpers via `expect/actual` (`TestDatabaseHelper`, `TestPlatformHelper`) in `jvmTest`. Mokkery is used for presenter/UI tests. Migration tests follow `database/migrations/Version22Test`/`Version23Test`.
- **Android JVM** (`salem-android/src/test`): `BaseAndroidJVMTest`, `ReminderControllerTest`.
- **Instrumented** (`salem-android/src/androidTest`): bases `BaseAndroidTest`, `BaseViewTest`, `BaseUserInterfaceTest`; DI test components `HabitsApplicationTestComponent`/`HabitsActivityTestComponent`; suites for acceptance, views, widgets, database, intents, regression, performance. View tests render a View/widget to a bitmap and compare to PNGs in `src/androidTest/assets/views/**` (`assertRenders`, `similarityCutoff = 0.00018`); accept new images with `./build.sh android-accept-images`. They require a clean emulator (Nexus 4 profile, en-US, animations off) — see `docs/TEST.md`. The habit list is Compose, so its steps use UiAutomator and test tags (`habitRow`, `checkmark_<value>`, `dayHeader`, the bar tags) instead of Espresso view matchers (`CommonSteps` switches to UiAutomator while `habitRow` is on screen). Run on one emulator with `ANDROID_SERIAL=<serial>` so a connected phone is left alone; the Android 11 image works and an Android 17 one fails with Espresso 3.6.1 (see `docs/PENDING.md` for the tests that still fail).
- **Rule:** put new logic in core where possible and cover it with a core test; every bug fix gets a regression test when feasible. Finish a change by running the build, ktlint and the tests relevant to the touched module.

## 8. Known limitations and gotchas

- Core is an Android library (`namespace io.github.crisacm.salem.core`, `androidTarget` with `com.android.library`). Its `jvmMain` dependencies no longer reach the app at runtime, so the app declares what it uses (e.g. `androidx.annotation`). D8 (R8 8.9.35 in AGP 8.9.2) prints `Unexpected error during rewriting of Kotlin metadata` warnings for Kotlin 2.3 classes; they existed before and are harmless.
- Compose is used in the About, Settings and Intro screens and in the small dialogs (`ui/compose/ComposeDialogFragment.kt`, habit type, weekdays, color, snooze, delete confirmation, checkmark and number popups, frequency picker, option lists) and the create and edit habit screen (shared `EditHabitScreen` in `salem-ui` `ui/edit`, hosted by `activities/habits/edit/EditHabitActivity.kt`; form logic in core `EditHabitForm`, saving in core `HabitEditorSaver`); the habit list is Compose too: `ListHabitsActivity` uses `setContent { ListHabitsContent(host) }` (`ListHabitsContent.kt`: top bar, day header, rows, empty state, progress bar and hint in one composition; `ListHabitsHost` keeps the state outside of it, the `HeaderState` that shares the scroll with the rows, and the listeners that follow the screen while it is attached; `HabitListContent` in `ListRows.kt` draws the rows with the cells of `ListCells.kt`; the confetti is a `KonfettiView` laid over the content by the activity; `ListHabitsMenu` and `ListHabitsSelectionMenu` are state holders for `ListHabitsBar`; Settings and the file picker answer through `ActivityResultContracts`) so far (`ui/compose/` has `SalemTheme` and the preference rows; `SalemTopAppBar` and the About screen (`AboutRoute`/`AboutContent` in `ui/about`, `SalemLinks`, `AboutTranslators` generated by `updateTranslators`, `docs/tasks/ui-about.md`) are in `salem-ui`, hosted by `AboutActivity` on Android and by a layer of `IosListRoot` over `ScreenNavigation` on iOS; Settings (`SettingsContent`/`SettingsState`/`SettingsSwitch` in `ui/settings`, `docs/tasks/ui-settings.md`) is shared too: the activity adds its Android-only rows (`AndroidSettingsSections.kt`) through slots and iOS opens it from `IosListRoot`, with `ScreenNavigation` as a stack of screens; the intro (`IntroContent`/`IntroModel` in `ui/intro`, `docs/tasks/ui-intro.md`) is shared too, hosted by `IntroActivity` and by a layer of `IosListRoot`, shown on the first launch); the rest is Views, except the habit detail screen, which is shared: `ShowHabitActivity` (host: intent, dialogs as Fragments, status bar, toasts) gives a `HabitDetailSession` its `Hooks` and shows `HabitDetailScreen` from `salem-ui` `ui/detail` (`HabitDetailBar` with the menu, `HabitDetailContent` with the scrolling cards of `HistoryBarCards.kt`, `ScoreFrequencyCards.kt`, `StreakTargetOverviewCards.kt` and `SubtitleNotesCards.kt`); the charts of `salem-core` (`HistoryChart`, `BarChart`, `ScoreChart`, `FrequencyChart`, `StreakChart`, `TargetChart`) are drawn with the shared `CoreChart`/`CoreCanvasView` and `ChartScrollState`; the history editor (`HistoryEditorContent` with the layer `HistoryEditorLayer`/`HistoryEditorHost`, `docs/tasks/ui-history-editor.md`), number, checkmark and delete dialogs it opens are Android Fragments over shared contents (`docs/tasks/ui-detail-screen.md`). `AndroidCanvas` remains for the widgets (`CoreWidgetView`). The Compose BOM is 2026.02.00 (newer ones need AGP 9.1 and compileSdk 37); `salem-ui` uses Compose Multiplatform 1.10.3 (checked with Kotlin 2.3.20 and AGP 8.9.2, alongside the `kotlin.compose` plugin; 1.11 needs Kotlin 2.4 for multiplatform). ktlint reads the Kotlin files that Compose Multiplatform generates in `salem-ui/build/generated`, so `salem-ui/build.gradle.kts` limits each ktlint task to `src/<source set>`. The iOS app shows the habit list, can create and edit habits (`docs/tasks/ios-edit-wiring.md`) and opens the habit detail (`docs/tasks/ios-detail-wiring.md`); it also shows Settings, About and the intro (`docs/tasks/ui-settings.md`); the other screens are next. CI is limited to `.github/workflows/ci.yml`: an ubuntu job (build, tests, ktlint) and a macOS job that compiles the iOS targets and runs the shared tests on the iOS simulator (no instrumented tests). The migration PRs #14-#25 were merged in order and the CI run of each push to `develop` passed (including the shared tests on the iOS simulator). The CI runs on every pull request, on pushes to `develop` and on manual runs, but `workflow_dispatch` only becomes available once the workflow file reaches `main`, the default branch. Core `jvmTest` pins the locale to en-US so tests do not depend on the machine; data written to files (CSV, migration file names) uses `formatInvariant`, not the locale-sensitive `format` (which is for UI text).
- `applicationId` is the same as the Android `namespace` (`io.github.crisacm.salem`); the core library namespace is `io.github.crisacm.salem.core`. Keep these consistent: FileProvider authority is `${applicationId}` in the manifest and `packageName` in `ViewExtensions.showSendFileScreen`; `BackupManager.dataChanged(requireContext().packageName)`; `res/xml/preferences.xml` hardcodes `targetPackage="io.github.crisacm.salem"`; `build.sh` has `PACKAGE_NAME=io.github.crisacm.salem`; androidTest constants use the new id.
- Intent actions `org.isoron.uhabits.ACTION_*` and the URI `content://org.isoron.uhabits/habit/<id>` (`Habit.uriString`, manifest `<data android:host>`) are an external protocol (Tasker, widgets) and deliberately keep Loop's `org.isoron.uhabits` name after the package rename — change them only together and deliberately.
- `HabitsApplication.isTestMode()` detects tests by `Class.forName("io.github.crisacm.salem.BaseAndroidTest")`.
- `ReminderScheduler` (takes a `SystemScheduler` interface), `MidnightTimer` (coroutine based, runs between `onResume` and `onPause`) and `DateUtils` (over the `ZoneOffsets` abstraction, with `expect` system zone, zone by id and clock) are in commonMain; the platform pieces that actually fire reminders are `IntentScheduler` on Android and `AppleSystemScheduler` over `UNUserNotificationCenter` on iOS (see 3.2). `HabitFixtures` is in commonMain.
- Database changes: add `NN+1.sql`, bump `DATABASE_VERSION`, add a migration test, and keep importers working (`LoopDBImporter` rejects newer versions and migrates older).
- Manifest still contains Loop's `com.google.android.backup.api_key` meta-data; `build.sh` still prints Loop branding, lists removed legacy dirs and names backups `Loop Habits Backup *.db`. Treat as to-do, not as intended design.
- Deprecated/legacy: `legacy-support-v4`, `legacy-preference-v14`, `@Deprecated` overrides in `ListHabitsActivity.kt`, `TODO("Not yet implemented")` in `platform/gui/AndroidImage.kt`.
- Play listings contain only English and Spanish; `contact-email.txt` is a placeholder; the Play URL in `constants.xml` points to the GitHub repo until the app is published.

## 9. Task playbooks

- **Add a DB migration:** new `salem-core/assets/main/migrations/26.sql`, bump `DATABASE_VERSION`, update repositories/importers if columns change, add `Version26Test` modelled on `Version23Test`, run `:salem-core:jvmTest`.
- **Add a habit command:** implement `Command` in `core/commands`, run it through `CommandRunner`, test with `BaseUnitTest`, wire it from a `*Behavior`/Android screen.
- **Add a setting:** key + accessor in core `Preferences`, default in `res/xml/preferences.xml` (it only holds defaults), row in `salem-ui` `ui/settings` (`SettingsSwitch` + `SettingsState`; Android-only rows go in `activities/settings/AndroidSettingsSections.kt`) that writes through the `Preferences` setter, and strings in `strings.xml`; keep the key identical everywhere; no hardcoded user-visible strings.
- **Add a widget type:** follow an existing pair (`*Widget` + `*WidgetProvider`), register in the manifest with its `res/xml/widget_*_info.xml`, add widget view tests.
- **Change UI strings:** edit `values/strings.xml` only; mention in the summary that translations are not updated.
- **Rename/move packages or modules:** out of scope until the KMP phase (see `docs/PENDING.md`); needs a dedicated plan (manifest, `preferences.xml`, ProGuard, tests, `build.sh`, `Habit.uriString`).
- **Before finishing any change:** `./gradlew :salem-android:assembleDebug`, `ktlintCheck`, relevant tests, update `CHANGELOG.md` if user-facing, and report honestly what could not be run.

## 10. Code exploration index (codebase-memory-mcp)

Status: **indexed on 2026-10-22** (project id `Users-cristianc-Desktop-Claude-Cowork-salem-focus-habits`, 7,423 nodes, 36,133 edges). The index was built with the CLI (`codebase-memory-mcp cli index_repository --repo_path <root>`) because the MCP server timed out on connect (`CONNECT_TIMEOUT`); if the tools are missing in a session, reconnect with `/mcp`. 108 files are intentionally not indexed (gitignore, binaries such as PNG/TTF, `local.properties`). Parsed only partially: `gradlew` and SQL migrations `09`, `17`, `19`, `22`. Use `detect_changes` after large edits and re-run `index_repository` after big refactors (e.g. the package move). Use Grep/Read for literals, SQL and Gradle files.

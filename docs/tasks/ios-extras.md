# Widget and shortcuts for iOS (KMP step 7, PRs X1-X3)

## Objetivo
Give the iOS app the native extras of step 7: a widget with the habits of today (WidgetKit) and shortcuts (App Intents) that replace the Tasker automation of Android.

## Por qué
`.specs/migration-kmp.md` lists "WidgetKit widgets, App Intents/Shortcuts (replaces Tasker automation), share sheet" as the iOS extras; the share sheet is done (PR E2). Three sequential PRs, each straight to `develop`: X1 the Kotlin module and the app side (this document), X2 the widget extension, X3 the shortcuts extension.

## Alcance
- In X1: the module `salem-extras` (framework `SalemExtras`, the core without Compose), `HabitsBoard` (the habits of today and check, uncheck, toggle, increment, decrement), `HabitsBoardFactory` (iOS), the App Group `group.io.github.crisacm.salem` for the database and the preferences with a fallback to the private places when the build cannot use it, `moveDatabase` (one-time move of the database), `ExternalChanges` (`PRAGMA data_version`) and `IosHabitsGraph.reloadIfChangedElsewhere()`, `MainViewController(onHabitsChanged)`, the minimum iOS version 17.0, CI.
- In X2: the widget extension target (`project.pbxproj`), the interactive widget, the entitlements. In X3: the App Intents extension and the shortcuts.
- Out: the chart widgets of Android (history, score, frequency, streak, target): there is no iOS canvas for the core charts. A Team ID and the signing: until the user has an Apple Developer account nothing here can run with the App Group on a device.
- Decisions (user): odd-full, TDD, iOS 17, a small Kotlin framework for the extensions, the widget of today plus shortcuts, no Apple account yet, no emulator/simulator by me.

## Exploración
Haiku `explorer` brief: Android has six widgets and the Locale plugin (`FireSettingReceiver`: check, uncheck, toggle, increment, decrement for today on one habit); `iosApp` is one SwiftUI target with a hand-managed `project.pbxproj`, no signing, no entitlements; the database was in Application Support and the preferences in the standard defaults; Swift only called `MainViewController()`; the scores and streaks of the app are computed in memory, so the app must read the database again after an extension changed it; the listeners of the commands (reminders, tray) run only in the process that runs the command; two Kotlin/Native runtimes in one process are risky, so the app links `SalemUi` and the extensions link `SalemExtras`, never both in the same process.

## Incertidumbre
- Without the entitlement (an unsigned build) the system gives no container: the app keeps its database in Application Support and the extensions find no database ("open the app"). The App Group and the move of the database were never run, only compiled.
- The preferences of an existing install are not copied to the shared defaults: the first launch after this change shows the intro and the default settings again (the only installs are development ones).
- Two processes write to the same SQLite file in `JournalMode.DELETE` with no busy timeout: a write at the same moment can fail. `PRAGMA data_version` was checked with two JDBC connections, not with sqliter.
- The size and the memory of the framework in a widget (the limit of an extension is small) were not measured.

## Checklist
- [x] TASK-1 (X1): `HabitsBoardTest` (15: the list of today, archived ones, an empty database, an older schema, colour, check, uncheck, toggle with skip and question marks, yes/no and numerical increments, targets, unknown ids, score, notes kept) — RED observed (unresolved reference), then `HabitsBoard`
- [x] TASK-2 (X1): `MoveDatabaseTest` (4) and `ExternalChangesTest` (3) — RED observed, then `moveDatabase` and `ExternalChanges`
- [x] TASK-3 (X1): `AppGroup`, `AppleFileOpener.createDefault(groupId)`, the graph uses the group and reloads after external changes, `HabitsBoardFactory`, iOS 17.0, CI — compiled only, nothing of iosMain has a test
- [x] TASK-4 (X2): `SalemWidgets` (WidgetKit extension: `TodayWidget` with the habits of today, small, medium and large; `ToggleHabitIntent` and `IncrementHabitIntent` for the buttons; strings in English and Spanish in a string catalog), the target and its embedding in the app by hand in `project.pbxproj`, the entitlements of the App Group, the call to `WidgetCenter.reloadAllTimelines()` from `ContentView` — built and embedded (`PlugIns/SalemWidgets.appex`), nothing of it has a test
- [x] TASK-5 (X3): `SalemIntents` (App Intents extension: `HabitEntity` and `HabitQuery`, the five action intents, `SalemShortcuts` with the Siri phrases, string catalogs for the actions and for the phrases in English and Spanish), the target by hand in `project.pbxproj` (it depends on `SalemWidgets` so the two Gradle runs of `:salem-extras` never overlap), embedded in the app — built (`PlugIns/SalemIntents.appex`, `NSExtensionPointIdentifier` `com.apple.appintents-extension`, iOS 17, `Metadata.appintents` with the five actions and two App Shortcuts), nothing of it has a test

## Estrategia de entrega
Sequential PRs from `develop` straight back to `develop`, checking with `gh pr view` that the previous one is merged.

## Evidencia
(X2) `xcodebuild … iosApp … build` with and without `CODE_SIGNING_ALLOWED=NO` builds both targets; the appex has `NSExtension` `com.apple.widgetkit-extension`, bundle `io.github.crisacm.salem.widgets`, minimum iOS 17.0 and the Spanish strings. Not run by me: the app and the widget on the simulator.

(X1) `:salem-core:jvmTest`, `:salem-extras:jvmTest`, `:salem-ui:desktopTest`, `:salem-android:testDebugUnitTest`, `ktlintCheck`, `compileDebugAndroidTestKotlin`, `assembleDebug`, the iOS compile of core, its tests, `salem-extras` and `salem-ui` (arm64 and simulator) and `xcodebuild` (build only). Not run by me: the iOS simulator tests and the app on the simulator.

## Widget de hoy (X2)
- Where: `iosApp/SalemWidgets/`. The app target depends on the widget target and embeds it, which also makes Gradle build `:salem-extras` before `:salem-ui` (never two Gradle runs at once). The widget links `SalemExtras` (about 4 MB in the Debug dylib), never `SalemUi`.
- What it shows: the active habits of today in the order of the app (3 rows small, 4 medium, 8 large): a circle of the colour of the habit (filled when done) that is a button (`ToggleHabitIntent`), the name and the score; a numerical habit shows "value/target unit" and a plus button (`IncrementHabitIntent`). If the app has not created its database (or is older) it says to open Salem. It draws again in an hour or just after midnight, and the app tells it to draw again whenever the habits change.
- Signing: a build for the simulator with no team ("Sign to Run Locally") compiles and embeds simulated entitlements that include the App Group (`FAKETEAMID`, checked in `Salem.app-Simulated.xcent` and `SalemWidgets.appex-Simulated.xcent`), so the shared container should exist in the simulator; a device needs a team. `CODE_SIGNING_ALLOWED=NO` (CI) applies no entitlements.
- To try it in the simulator (the user does it at the end): run the app once, create a habit, go to the home screen, long press, add the Salem widget, tap its circle and open the app again.

## Atajos (X3)
- Where: `iosApp/SalemIntents/`. Links `SalemExtras` like the widget, never `SalemUi`. The intents have their own names (`ToggleHabitShortcutIntent`, `IncrementHabitShortcutIntent`; the widget keeps `ToggleHabitIntent` and `IncrementHabitIntent`, which take a plain id and are not discoverable).
- Each action takes a `HabitEntity` (chosen from the active habits of the shared database), changes today, answers with a dialog ("Name is done", "Name: 3 / 5") and calls `WidgetCenter.reloadAllTimelines()`. The board returns nil both for an unknown habit and for a database that is not ready, so the intent asks `today().needsApp` first and throws "Open Salem first…" or "That habit no longer exists.".
- Not known until it runs: whether Shortcuts and Siri pick up the `AppShortcutsProvider` from an extension (the metadata of the build lists the provider and the two shortcuts; if Siri does not find the phrases, the actions are still in the Shortcuts app); the memory of the Kotlin framework in this extension.
- Known gap: `HabitCompletionRearmer` runs only inside the app, so a shortcut that completes a habit does not move its pending reminder to tomorrow until the app is opened again (the delivered notification is removed).
- To try it (the user does it at the end): run the app once, create a habit, open the Shortcuts app, add the action "Toggle habit" of Salem, choose a habit and run it; then open the app and the widget.

## Equivalencia con Tasker (Android) para los atajos (X3)
| Locale action | Parameter | iOS intent (X3) |
|---|---|---|
| 0 check | habit id | `CheckHabitIntent` (`HabitsBoard.check`) |
| 1 uncheck | habit id | `UncheckHabitIntent` (`uncheck`) |
| 2 toggle | habit id | `ToggleHabitIntent` (`toggle`) |
| 3 increment | habit id | `IncrementHabitIntent` (`increment`, one unit) |
| 4 decrement | habit id | `DecrementHabitIntent` (`decrement`, one unit, never below zero) |
All of them act on today. Differences from Android: a numerical habit is checked by setting its target (Android writes the value 2), and it is never lowered below zero.

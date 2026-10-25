# The habit list on iOS (KMP step 7, PR 2d)

## Objetivo
Show the shared habit list in the iOS app over a real SQLite database, with the app graph built by hand.

## Por qué
After PR 2a-2c the list is shared; the iOS app still showed a sample ring. This is the first screen of the iOS app with real data and the base for the rest.

## Alcance
- In: `initializeHabitsSchema` (core, tested); `checkmarkColumns`/`HabitNameWidth` and `listScreenColorsFor` in `salem-ui` common (tested); in `iosMain`: `IosHabitsGraph`, `IosListScreenModel`, `IosListScreen` (stubs), `IosThemeSwitcher`, `IosBugReporter`/`IosDirFinder`, `IosListRoot`, `DebugSeed`; `MainViewController` shows the list.
- Out: reminders, popups, detail/edit/settings, creating or importing habits, confetti, Konfetti-like effects, the condensed font.
- Decisions (user): seed example habits only with `SALEM_SEED_DEBUG_DATA=1`; show and mark only, everything else logs; the simulator is used only from a Haiku 5.5 sub-agent after my checks and the user's confirmation.

## Exploración
Android startup (`HabitsApplication`, `HabitsDatabaseOpener`), the core interfaces an app must implement (`ListHabitsBehavior` and its `Screen`, `BugReporter`, `DirFinder`, the menu and selection `Screen`s) and the public iOS pieces of `salem-core/appleMain` (`AppleFileOpener`, `SqliterDatabaseOpener`, `UserDefaultsStorage`, `AppleLocalDateFormatter`, `AppLifecycleObserver`) were listed by the explorer; kotlin-inject runs only for Android, so the graph is built by hand.

## Incertidumbre
- Colours: the core themes do not match the Android attributes, so `listScreenColorsFor` copies the values of the Android themes (tested).
- `Dispatchers.IO` is internal on Native in this version: `kotlinx.coroutines.IO` is imported. `UITraitCollection.currentTraitCollection` is not available: the system appearance is read from `UIScreen.mainScreen.traitCollection`.
- String-arrays of Compose resources do not resolve `@string/` items: the two hints are read with `getString`.
- Not verified until the simulator run: that the app starts, the database opens from Application Support with the bundled migrations, the list draws, and light/dark follow the system.

## Checklist
- [x] TASK-1: `initializeHabitsSchema` + test — criterio: `:salem-core:jvmTest`
- [x] TASK-2: `checkmarkColumns` and `listScreenColorsFor` in `salem-ui` + tests — criterio: `:salem-ui:desktopTest`
- [x] TASK-3: iOS graph, model, theme switcher, stub screen, root, debug seed — criterio: iOS compile and `xcodebuild`
- [x] TASK-4: simulator run with screenshots (Haiku sub-agent, user confirmed) — criterio: the list shows seeded habits in light and dark
- [x] TASK-5: docs and changelog

## Estrategia de entrega
`single-pr`, branch `feat/ios-habit-list` from `develop`. Commits only when the user asks.

## Evidencia
ktlintCheck, `:salem-ui:desktopTest` (headless), `:salem-core:jvmTest`, `:salem-android:testDebugUnitTest`, `assembleDebug`, `compileDebugAndroidTestKotlin`, iOS compile (arm64 and simulator, including core tests) and `xcodebuild`. 
Simulator (iPhone 16, iOS 27.0 runtime, run by a Haiku sub-agent after the user confirmed): the first launch aborted 3 s after start with an uncaught Kotlin exception from the Compose UIKit sanity check, because the `Info.plist` lacked `CADisableMinimumFrameDurationOnPhone` = true; fixed with `iosApp/iosApp/Info.plist` (merged with the generated one through `INFOPLIST_FILE`). After the fix the app stays running through light, dark and back to light and a 20 s wait; `uhabits.db` is created in Application Support, the migrations ran, and with `SALEM_SEED_DEBUG_DATA=1` the screen shows the bar, the day header (SAT 24 to WED 21) and the rows Meditate, Run (numbers with units), Wake up early (notes marks, the outlined automatic check) and Read a book, in the light and the dark themes. Not checked: taps (simctl cannot tap), drag, selection, landscape, other sizes. The runtime iOS 27.1 (24A94425) installed before only supported the device type iPhone Duo, so it was deleted and iOS 27.0 (24A434) was downloaded; both runtimes are installed now.


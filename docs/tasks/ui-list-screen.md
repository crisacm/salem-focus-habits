# Habit list bar and screen in `salem-ui` (KMP step 7, PR 2c)

## Objetivo
Move the top bar, the search and selection bars and the composition of the habit list screen to `salem-ui`, so the activity only adds what is Android.

## Por qué
After 2b the rows were shared but the screen around them (bar, header, progress, hint, empty state) still lived in `salem-android` behind `ListHabitsHost`. iOS needs the same screen.

## Alcance
- In: `ListHabitsBar` (shared `BackHandler`, `Res.string`, `ListIcons`, `exposeTestTags`), `ListScreenModel` + `ListScreenColors`, `ListHabitsContent(model, theme, onSideInsets)`, `TaskProgress` (replaces the `TaskProgressBar` View), `ListHabitsRoot` in `salem-android` (condensed font, theme resolution, side insets), `ListHabitsHost` implementing the model.
- Out: the iOS list (PR 2d), `ListHabitsScreen` and the activity logic, confetti on iOS, the Settings/About screens.
- Decisions (user): 2c Android only, iOS list in 2d; confetti stays Android-only; test tags and labels unchanged; **no emulator, device or simulator runs without asking**.

## Exploración
`ListHabitsBar` used `BackHandler`, `painterResource` for 4 vector drawables and `R.string`; `ListHabitsContent` read ~20 members from the host, among them the Android `TaskProgressBar` through `AndroidView`, `Build.VERSION` and `systemIsDark`. The four drawables are plain path data (24/32 dp, 4 units of margin in the 32 dp ones).

## Incertidumbre (resolved)
- The XML drawables become `ImageVector`s built from the same paths (`ListIcons`), with the same 4 unit group translation, instead of Compose drawable resources (no change to `syncSharedStrings`).
- `BackHandler` needs the `ui-backhandler` artifact (an experimental API of Compose UI).
- `TaskProgress` follows the last count after 500 ms like the old View; the bar animates forever, so its test advances the clock by hand.
- `ListHabitsBar` builds on real `ListHabitsMenuBehavior` and `ListHabitsSelectionMenuBehavior` (final classes, Mokkery cannot mock them) with mocked screens and adapters.

## Checklist
- [x] TASK-1: `ListHabitsBar` + `ListIcons` in `salem-ui` — criterio: compiles for Android, iOS and desktop
- [x] TASK-2: `ListScreenModel`, `ListHabitsContent`, `TaskProgress`; host and `ListHabitsRoot` in Android — criterio: `assembleDebug`, `compileDebugAndroidTestKotlin`
- [x] TASK-3: tests (`ListHabitsBarTest`, `ListHabitsContentTest`, `TaskProgressTest`) — criterio: green, also headless
- [x] TASK-4: docs and changelog

## Estrategia de entrega
`single-pr`, branch `feat/ui-list-screen` chained on `feat/ui-list-core`; PR 2d (iOS list) follows. Commits only when the user asks.

## Evidencia
ktlintCheck, `:salem-ui:desktopTest` (headless), `:salem-core:jvmTest`, `:salem-android:testDebugUnitTest`, `assembleDebug`, `compileDebugAndroidTestKotlin`, iOS compile and `xcodebuild`. Not run: instrumented/acceptance tests, the app on a device or simulator.

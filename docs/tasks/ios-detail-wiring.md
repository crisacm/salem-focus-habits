# The habit detail on iOS (KMP step 7, detail PR 4d)

## Objetivo
Open the shared habit detail screen from the iOS habit list, with the history editor, the popups, the editor and the messages over it.

## Por qué
PRs 4a-4c moved the canvas, the cards, the screen, the session and the history editor to `salem-ui`. iOS still logged "habit screen" when a habit was tapped. This closes the detail block.

## Alcance
- In (common, tested on the JVM): `HabitDetailRoute` (the screen, the history editor layer, the popups, the editor, the delete confirmation and a message banner, with back closing the topmost), `HabitDetailLayers` (the hooks of the session over `ListOverlays`, `HistoryEditorLayer` and a message), `DetailNavigation`, `DetailMessage`/`DetailMessageBanner`, `HabitDetailSession.developerOptions`/`exportOverride`/`onExport()`, and the text `export_not_available`. In `iosMain` (compiled only): `IosHabitsGraph` exposes `commandRunner`, `dirFinder` and `navigation`, `IosDirFinder` is also the `ShowHabitMenuPresenter.System`, `IosListScreen.showHabitScreen` opens the habit, `IosListRoot` draws the route over the list.
- Out: exporting a CSV and sharing a file on iOS (the Export item shows a message and writes nothing), haptic feedback, widgets, reminders, settings, about, help, importing a Loop `.db`.
- Decisions (user): odd-full; 4 stacked PRs; iOS menu = edit, archive, unarchive, delete; export shows "not available"; Randomize only in debug builds; rdd-full of the block afterwards if the user asks.

## Exploración
`iosMain` has no tests, so the integration that matters (layers, back order, messages, menu) lives in `commonMain` and is tested with `runComposeUiTest`; the iOS files only create the pieces. The detail cannot be a `ListOverlay` (single slot) and the history editor cannot either (the popup of a day opens above it), so the route stacks: detail, history editor layer, popups. `HabitDetailRoute` is composed so that back goes popup/editor first, then the history editor, then the detail.

## Incertidumbre
- Not run on the simulator: how the route draws inside the `ComposeUIViewController` (safe areas, keyboard over the editor), that the iOS back gesture reaches `BackHandler`, the charts and their gestures on iOS, `AppleLocalDateFormatter.longFormat` and `NSDateFormatter` at runtime, VoiceOver.
- The banner clears itself after 3 seconds; the tests stop the clock to see it.
- Randomize uses `Platform.isDebugBinary` instead of the developer preference (iOS has no settings screen to turn it on); it still changes entries without a command, as on Android.
- The detail refreshes through a `CommandRunner.Listener` while the route is composed; after a delete the route closes and the list refreshes through its own listener.

## Checklist
- [x] TASK-1: `HabitDetailSessionTest` (+2: `developerOptions`, `exportOverride`) and the bar calls `onExport()`
- [x] TASK-2: `HabitDetailLayersTest` (12: each hook, layers independent, messages, closing, `DetailNavigation`) — criterio: RED observed
- [x] TASK-3: `HabitDetailRouteTest` (10: screen, editor and back, history editor and back, popup above the editor, archive message and menu, delete and close, export not available, back and the back button, Randomize hidden)
- [x] TASK-4: iOS glue (graph, services, list screen, root) — criterio: iOS compile (arm64 and simulator) and `xcodebuild`
- [x] TASK-5: docs and changelog

## Estrategia de entrega
Stacked PRs 4a to 4d; this one is `feat/ios-detail-wiring`, from `develop` after #66. The user merges. Commits only when the user asks. The review of the whole block (rdd-full) comes after it if the user asks.

## Evidencia
`:salem-core:jvmTest`, `:salem-ui:desktopTest` (all), `:salem-android:testDebugUnitTest`, `ktlintCheck`, `compileDebugAndroidTestKotlin`, `assembleDebug`, iOS compile (arm64 and simulator) and `xcodebuild` (build only). Not run: the simulator, any emulator. To check by hand on iOS (needs the user's OK to run the simulator): tap a habit to open the detail; the cards and the six charts (look, scrolling, taps); the menu (Edit, Archive/Unarchive with its message, Delete with confirmation, Export message); the History Edit button, taps on days (popups above the editor) and back at each level; the number and checkmark popups; rotation; dark mode; VoiceOver.

# Create and edit habits on iOS (KMP step 7, PR 3d)

## Objetivo
Let the iOS app create and edit habits, change their colour, delete them and use the checkmark and number popups, with the shared dialogs and editor of PR 3a-3c.

## Por qué
Until now the iOS list only showed and marked habits (the debug seed was the only way to have data). This is the last piece of PR 3: the shared editor and dialogs are reachable from the app.

## Alcance
- In: `ListOverlays`/`ListOverlay` (state) and `ListOverlayHost` (drawing) in `salem-ui` common code, tested on the JVM; `IosHabitsGraph` exposes `overlays` and `saverFactory`; `IosListScreen` opens the overlays for the new habit dialog, the edit of the first selected habit (as Android), the colour picker, the delete confirmation and the checkmark and number popups; `IosListRoot` draws the host over the list; `iosDecimalSeparator()` reads the decimal mark of the device.
- Out: the habit detail, settings, about, help, intro, sharing, reminders, importing a Loop `.db`, confetti, showing `Message`s (`lastMessage` is still unread).
- Decisions (user): move to `salem-ui` and share with Android; stacked PRs; TDD where there is logic; no emulator/simulator without asking; rdd-full after this PR.

## Exploración
`IosListScreen` was logging stubs; the graph built `modelFactory` and `commandRunner` as locals. The core behaviours already call the `Screen` methods with callbacks (`NumberPickerCallback`, `CheckMarkDialogCallback`, `OnColorPickedCallback`, `OnConfirmedCallback`). Android saves the notes of a popup closed without a button through `NotesSession` (3a). `iosMain` has no unit tests, so the logic that can be tested lives in common code.

## Incertidumbre
- Verified only by compiling (both iOS targets, `xcodebuild`) and by the JVM tests of the host; the screens were not run on the simulator.
- Not known without running: the safe area and the keyboard over the editor (the Swift shell uses `ignoresSafeArea(.all)` and `EditHabitScreen` uses `imePadding`), the iOS back gesture (`BackHandler` closes the overlay, but `enableBackGesture` was not checked), `TimePicker` and the text fields on iOS.
- The editor receives the theme of the root (`resolveTheme`) and `isNight = theme is DarkTheme`, so it follows the same rule as the list.
- A habit deleted while its editor was about to open closes the overlay.

## Checklist
- [x] TASK-1: `ListOverlayHostTest` (14 tests: type choice, new habit, edit, missing habit, delete, colour, checkmark, number, back, replacing an overlay) — criterio: RED before the host existed
- [x] TASK-2: `ListOverlays` and `ListOverlayHost` — criterio: `:salem-ui:desktopTest`
- [x] TASK-3: iOS graph, screen and root — criterio: iOS compile (arm64 and simulator) and `xcodebuild`
- [x] TASK-4: docs and changelog

## Estrategia de entrega
Stacked PRs 3a to 3d; this one is `feat/ios-edit-wiring`, from `develop` after #61. The user merges. Commits only when the user asks. The review of the whole block (rdd-full) comes after it.

## Evidencia
`:salem-core:jvmTest`, `:salem-ui:desktopTest`, `:salem-android:testDebugUnitTest`, `ktlintCheck` (all modules), `compileDebugAndroidTestKotlin`, `assembleDebug`, iOS compile (arm64 and simulator) and `xcodebuild` (build only). Not run: the simulator and any instrumented test. To check by hand on iOS (needs the user's OK to run the simulator): new yes/no and numerical habits, edit, every picker, colour from the selection bar, delete, checkmark and number popups with notes, back gesture, keyboard and safe areas, dark mode.

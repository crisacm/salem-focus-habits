# The habit detail screen on the shared UI (KMP step 7, detail PR 4b)

## Objetivo
Move the habit detail screen (top bar with menu, eight cards, six charts) to `salem-ui` so Android and iOS share it, and reduce `ShowHabitActivity` to a host.

## Por qué
iOS opens nothing when a habit is tapped. The detail moves in four PRs: 4a the canvas and chart host, 4b (this one) the cards, the screen and the state of the detail, 4c the history editor, 4d the iOS wiring.

## Alcance
- In: `salem-ui` `ui.detail`: `ShowHabitModel` (pure, with its tests), `CardTitle`, `CardSpinner`, the cards (`HistoryBarCards`, `ScoreFrequencyCards`, `StreakTargetOverviewCards`, `SubtitleNotesCards`), `HabitDetailContent`, `HabitDetailBar`, `HabitDetailScreen`, `IntervalLabels`, `DetailStyle`, and `HabitDetailSession` (presenters, state, menu flags, refresh on commands, `Hooks` for what the platform does). Core: `LocalDateFormatter.longFormat` (Java and Apple implementations) and four `FontAwesome` constants. Android: `ShowHabitActivity` as host, its own `intervalToLabel` for the target widget (`widgets/IntervalLabels.kt`); the Android cards, content, bar, host and screen are deleted; `ShowCardsComposeTest` (androidTest) is now `ShowCardsTest` on the desktop.
- Out (4c-4d): the history editor as a shared overlay (the Fragment stays), the iOS wiring, a shared hook for export/share.
- Decisions (user): odd-full, 4 stacked PRs, TDD where there is logic, no emulator/simulator without asking.

## Exploración
Three Haiku explorations plus direct reads of the Android files: the cards depended on `R.string`, `R.dimen` (16 and 14 sp), `grey_400`, `fa_*` glyph strings that Compose resources do not copy, two string-arrays of `@string` references, `Resources`, `String.format`, `JavaLocalDateFormatter` and `AndroidCanvas` charts; the presenters are pure builders and the Activity drove them with a `CommandRunner.Listener`.

## Incertidumbre
- On Android the charts now draw through `DrawScopeCanvas` with Noto Sans (not the system font) and the new text placement of 4a; the edit icon is the one of Material. Not seen on a device.
- `HabitDetailSession.refresh` builds the state on the scope it is given; Android passes `Dispatchers.Main`, the tests an unconfined one. The first state appears after `attach()`.
- The menu items of the shared bar keep the old tags (`overflowMenu`, `export`, `action_archive_habit`, ...); the instrumented acceptance tests were compiled, not run.
- `ShowHabitMenuPresenter.onRandomize` still changes entries without a command (as before).

## Checklist
- [x] TASK-1: `longFormat` in the interface (Java test, Apple test compiled) and four `FontAwesome` constants — criterio: `FontAwesomeGlyphsTest` RED then GREEN
- [x] TASK-2: `HabitDetailSessionTest` (10 tests) and `HabitDetailSession` — criterio: RED observed
- [x] TASK-3: `HabitDetailScreenTest` (9 tests: cards per habit type, notes, back, edit, menu with archive/unarchive/delete/randomize) and the screen — criterio: RED observed
- [x] TASK-4: the cards (`ShowCardsTest`, 9 tests ported from Android) and the moved model tests (`ShowHabitModelTest`, `OverviewCardTest`)
- [x] TASK-5: `ShowHabitActivity` as a host, Android files deleted
- [x] TASK-6: docs and changelog

## Estrategia de entrega
Stacked PRs 4a to 4d; this one is `feat/ui-detail-screen`, from `develop` after #64. The user merges in order. Commits only when the user asks.

## Evidencia
`:salem-core:jvmTest`, `:salem-ui:desktopTest` (all), `:salem-android:testDebugUnitTest`, `ktlintCheck`, `compileDebugAndroidTestKotlin`, `assembleDebug`, iOS compile (arm64 and simulator, including the core tests of Apple) and `xcodebuild` (build only). Not run: the simulator, any emulator, the instrumented tests. To check by hand on Android: open a habit (yes/no and numerical), every card and chart (look, fonts, scrolling, taps in the history), the spinners, Edit, Archive/Unarchive, Delete, Export, the history editor, the number and checkmark popups, rotation and the status bar colour.

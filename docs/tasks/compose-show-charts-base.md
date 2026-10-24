# Habit detail: chart base and the history and bar cards in Compose (KMP step 6h, part 1)

## Objetivo
Show the `salem-core` charts in Compose and move the calendar (history) card and the bar card of the habit detail screen to Compose.

## Por qué
Step 6 of `.specs/migration-kmp.md`; first of five PRs that migrate the habit detail screen (`ShowHabitActivity`, nine cards, seven charts). The charts need a way to be drawn and scrolled in Compose before any card can move; the history and bar charts already live in `salem-core` and draw on the multiplatform `Canvas`, so they are the cheapest first cards.

## Alcance
- In: `CoreChart` (a composable that draws a `DataView` through `AndroidCanvas` on the native canvas, drags, flings, passes taps), `ChartScrollState` (tested), `CardSpinner`, `HistoryCardContent` and `BarCardContent`; `HistoryCardView` and `BarCardView` keep their place in `show_habit.xml` and now host a `ComposeView`; removal of `show_habit_history.xml`, `show_habit_bar.xml`, `HistoryCardViewTest` and its PNG.
- Out: the other seven cards and charts, the activity, `AndroidDataView` (still used by the history widget and `HistoryEditorDialog`), a `Canvas` over `DrawScope` (step 7), the widgets.
- Decisions (user): five incremental PRs; adapter for the core `Canvas`, implemented on the native canvas now (the pure `DrawScope` version waits for iOS).

## Exploración
- Spike: `HistoryChart` and `BarChart` use only methods that the core `Canvas` has (`fill`, `fillRect`, `fillRoundRect`, `fillCircle`, `drawLine`, text with alignment and size, `measureText`). The Android-Canvas charts (score, frequency, streak, target, ring) need more than the core `Canvas` offers: bitmap caches, blend modes (`CLEAR`), ovals, stroked arcs, sp text sizes and font metrics. They come in the next PRs.
- `AndroidDataView` scrolled with a `Scroller`: the offset is `scrollX / (dataColumnWidth * density)`, never below zero, no upper limit, fling at half the speed; the chart gets taps in dp. `ChartScrollState` reuses `HeaderScroll` (the pure scroll of the day header).
- The history card has no date click listener wired in the old card either (the presenter's `OnDateClickedListener` is used by the editor dialog).

## Incertidumbre
- Look and feel of the chart scrolling (the Views used a `Scroller`), exact spacing against the old XML, the dropdown look, text sizes with font scaling: only a device shows them.

## Checklist
- [x] TASK-1: `ChartScrollState` with tests first — criterio: `ChartScrollStateTest` passes
- [x] TASK-2: `CoreChart` and `CardSpinner` — criterio: assembles
- [x] TASK-3: history and bar cards in Compose hosted by the existing card Views — criterio: assembles, no references left to the removed layouts
- [x] TASK-4: remove the old layouts, test and PNG; add `ShowCardsComposeTest` — criterio: `compileDebugAndroidTestKotlin` passes
- [x] TASK-5: CHANGELOG, PENDING, AGENTS.md — criterio: docs updated; ktlint and unit tests green

## Estrategia de entrega
`single-pr`, branch `feat/compose-show-charts-base`, base `develop`. Commits only when the user asks.

## Evidencia
- `ktlintCheck`, `:salem-android:testDebugUnitTest`, `assembleDebug` and `compileDebugAndroidTestKotlin` pass.
- See the PR for the emulator run.

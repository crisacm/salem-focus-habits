# Habit list: empty state and hint in Compose (KMP step 6g, part 1)

## Objetivo
Move the two small overlays of the habit list, `EmptyListView` and `HintView`, to Jetpack Compose, hosted by `ComposeView`s inside the existing `ListHabitsRootView`.

## Por qué
Step 6 of `.specs/migration-kmp.md`. First of five PRs that migrate the habit list piece by piece (empty state and hint; toolbar, menus and selection bar; day header and cells; list rows with selection and drag; activity). These two views are pure display with almost no coupling, so they prove the mixed Views and Compose root.

## Alcance
- In: `EmptyListContent` and `HintContent` composables, pure rules with tests (`emptyListState`, `nextHint`), `ComposeView`s in `ListHabitsRootView`, removal of `EmptyListView`, `HintView`, their image tests and PNGs.
- Out: everything else of the list (toolbar, header, rows, activity), the `HintList` logic in core.
- Decisions (user): five incremental PRs; retire the PNG view tests of each migrated view; odd-full.

## Exploración
- `ListHabitsRootView` builds a programmatic `RelativeLayout`: konfetti, toolbar, `HeaderView`, `HabitCardListView`, `EmptyListView` (below the header, match parent, gone), progress bar and `HintView` at the bottom. `updateEmptyView()` runs on every model change of the adapter: no items and no habits at all → empty; no items but habits exist (filtered) → done; otherwise hidden.
- `EmptyListView`: a FontAwesome icon at 40sp (`fa_star_half_o` or `fa_umbrella_beach`) and a text (`no_habits_found` / `no_habits_left_to_do`) with 20dp top padding, both `contrast60`, centred.
- `HintView`: indigo background (`indigo_500`), padding 16/16/4/16 dp, bold white title (`hint_title`), the hint text with 5dp top padding; on attach it asks `HintList.shouldShow()` and `pop()`, fades in over 500 ms, and a tap fades out over 500 ms and hides it.
- Tests: `EmptyListViewTest` (2 PNG) and `HintViewTest` (1 PNG, plus a click test); nothing else references the two views.

## Incertidumbre
- Colours and spacing can only be compared with the old views on a device.

## Checklist
- [x] TASK-1: task doc — criterio: this file committed
- [x] TASK-2: pure rules with tests first — criterio: JVM tests pass
- [x] TASK-3: `EmptyListContent` and `HintContent` and their `ComposeView`s in `ListHabitsRootView` — criterio: assembles
- [x] TASK-4: remove the old views, their image tests and PNGs — criterio: no references left, `compileDebugAndroidTestKotlin` passes
- [x] TASK-5: CHANGELOG, PENDING, AGENTS.md — criterio: docs updated; ktlint, unit tests, core tests green

## Estrategia de entrega
`single-pr`, branch `feat/compose-list-empty-hint`, base `develop`. Commits only when the user asks.

## Evidencia
- `assembleDebug`, `assembleRelease`, `compileDebugAndroidTestKotlin`, `ktlintCheck`, `:salem-android:testDebugUnitTest` (6 new tests in `ListEmptyStateTest`) and `:salem-core:jvmTest` pass.
- `ListHabitsRootView` hosts two `ComposeView`s: the empty state (made visible only when it has something to show) and the hint banner at the bottom. The hint asks `HintList` once, when the view is composed, like the old view did on attach.
- The Compose compiler failed ("No mapping for symbol") when the hint list was a local `val` captured by the content lambda inside an `init` block; it is now a property of the root view.
- Removed: `EmptyListView`, `HintView`, `EmptyListViewTest`, `HintViewTest` and their PNGs (`EmptyListView/`, `HintView/`).
- Not run: the list on a device (spacing and colours against the old views, the 500 ms fades, the hint on first launch of the day) or any instrumented test.
- Review fix (rdd-lite): the icon glyph of the empty state is hidden from screen readers (`clearAndSetSemantics`), so only the text is read. Left: the hint state is not kept if its `ComposeView` is composed again (the old view was rebuilt with the activity too), and the fades and the tap to dismiss have no test.

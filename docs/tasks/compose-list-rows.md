# Habit list: rows in Compose (KMP step 6g, part 4)

## Objetivo
Move the rows of the habit list from `RecyclerView` Views to a Jetpack Compose `LazyColumn`, using the cells added in part 3, and retire the Views of the rows with their image tests.

## Por qué
Step 6 of `.specs/migration-kmp.md`; fourth of five PRs that migrate the habit list piece by piece. The header already drives the horizontal offset through `HeaderState`; the rows are the last big piece before the activity itself (part 5).

## Alcance
- In: `HabitListState` (replaces `HabitCardListAdapter`), `HabitListContent`/row composables, selection and long-press drag reorder (own gestures), bottom inset, confetti origin, midnight refresh, removal of `HabitCardListView`, `HabitCardListController`, `HabitCardView(Holder)`, the panel and button Views and their instrumented tests and PNGs, port of `CommonSteps`/`ListHabitsSteps`.
- Out: `ListHabitsActivity` to `setContent` and `ListHabitsRootView` removal (part 5), `RingView` and `ScrollableChart` (widgets and charts), the unused core drawing classes.
- Decisions (user): five incremental PRs, retire the image tests of each migrated view, keep the long-press gesture (select, then drag with manual order).

## Exploración
- Cache: `HabitCardListCache` has a single listener slot and index-based callbacks (`onItemChanged/Inserted/Moved/Removed`, `onRefreshFinished`); `onAttached/onDetached` add and remove its `CommandRunner` listener.
- `HabitCardListAdapter` also implements `MidnightTimer.MidnightListener`, `ListHabitsMenuBehavior.Adapter` and `ListHabitsSelectionMenuBehavior.Adapter`, and exposes `observable`, `selected`, `isSortable` (`primaryOrder == BY_POSITION`), `hasNoHabit`.
- `HabitCardListController`: Normal mode (click opens, long press starts selection) and Selection mode (click toggles, empty selection goes back to Normal); drag only after long press and only if sortable; `drop` cancels the selection, reorders the adapter and calls `ListHabitsBehavior.onReorderHabit`. `startDrag` and `HabitListener` have no users.
- The header writes `dataOffset` into `listView.dataOffset`; rows must read it from the same state.
- Tests to retire: `EntryButtonViewTest`, `EntryPanelViewTest`, `NumberButtonViewTest`, `NumberPanelViewTest`, `HabitCardViewTest` and 16 PNGs under `assets/views/habits/list/`. To port: `CommonSteps` (`verifyDisplaysCheckmarks`, `scrollToText`), `ListHabitsSteps.longPressCheckmarks`.

## Incertidumbre
- Feel of the long press to drag, scroll performance, confetti origin and the look of the rows can only be judged on a device.

## Checklist
- [x] TASK-1: task doc and branch — criterio: this file committed
- [x] TASK-2: pure rules (selection mode, reorder) with tests first — criterio: JVM tests pass
- [x] TASK-3: `HabitListState` replacing `HabitCardListAdapter` — criterio: assembles
- [x] TASK-4: row and list composables — criterio: assembles
- [x] TASK-5: selection and drag reorder — criterio: assembles, rules tested
- [x] TASK-6: wire into `ListHabitsRootView`, screen, activity and DI — criterio: assembles
- [x] TASK-7: remove the Views, tests and PNGs; port the instrumented steps — criterio: no references left, `compileDebugAndroidTestKotlin` passes
- [x] TASK-8: CHANGELOG, PENDING, AGENTS.md — criterio: docs updated; ktlint, unit tests, core tests green

## Estrategia de entrega
`single-pr`, branch `feat/compose-list-rows`, base `develop`. Commits only when the user asks.

## Evidencia
- `ktlintCheck`, `:salem-android:testDebugUnitTest` (134 tests, 0 failures), `assembleDebug`, `assembleRelease` and `compileDebugAndroidTestKotlin` pass.
- New: `ListRowModel.kt` (selection, reorder and per-day rules, 15 tests), `HabitListState.kt` (9 tests, real `HabitCardListCache` because Mokkery cannot mock a final class; the fields of `BaseAndroidJVMTest` are now `protected`), `HabitListController.kt` (+ `SelectionListener`, implemented by `ListHabitsSelectionMenu`; 6 tests), `ListRows.kt` (`HabitListContent`, own gestures, live reorder). `ListHabitsRootView`, `ListHabitsScreen`, `ListHabitsActivity` (saves the day offset in `onSaveInstanceState`), `ListHabitsSelectionMenu` and both DI components use them.
- Removed: `HabitCardListView/Adapter/Controller`, `HabitCardView(Holder)`, the panel and button Views, five image tests, 16 PNGs and `HasButtonsViewAssertion`; `toShortString` moved to `activities/habits/list/ShortString.kt`. `RingView` and `ScrollableChart` stay.
- Instrumented steps ported to UiAutomator (cells carry `checkmark_<value>`, rows `habitRow`). Run on a Pixel_5 emulator (Android 11): 10 of 16 tests of `HabitsTest`, `ListHabitsRegressionTest` and `ListCellsComposeTest` pass, including the horizontal scroll with checkmarks, toggling, delete, archive and hide completed. Six fail (selection bar not seen by UiAutomator, edit habit, one screen check); see `docs/PENDING.md`. A Pixel_10 (Android 17) emulator cannot run Espresso 3.6.1.
- Found and fixed on the way: `ComposeDialogFragment` called its own `Content()` forever and crashed with a `StackOverflowError` on opening any dialog built on it (in `develop` since the dialogs PR). It should go in its own commit.
- Review fixes (rdd-full, one independent reviewer, Risk and Reliability; tests first, 7 new): a drag is now saved once when it ends, from where the row started to where it finished (`HabitListController.onDragStarted/onDragFinished`; before, every row crossed started its own database task and they could finish out of order); while a drag is in progress `HabitListState` holds back the refreshes of the cache and, if one arrived, applies the net move to the cache again at the end (`beginDrag`/`endDrag`); `HabitListState.onDetached` only detaches the cache if it is still its listener (`HabitCardListCache.isListener`), so a replaced activity cannot leave the new one without command updates. Left in `docs/PENDING.md`: rebuild cost, midnight shift, ghost selection, captured habit instance, drag without auto-scroll, minor repeated work. Checks after the fixes: `:salem-android:testDebugUnitTest` (141) and `:salem-core:jvmTest` (325) pass, ktlint, `assembleDebug` and `compileDebugAndroidTestKotlin` pass; the instrumented tests were not run again.
- Not checked: the rows next to the old Views, scroll feel, drag on a device, the bottom padding, the confetti origin, edge auto-scroll (missing), the cases listed in `docs/PENDING.md`.

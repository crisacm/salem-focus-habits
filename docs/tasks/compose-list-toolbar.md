# Habit list: top bar, menus and selection bar in Compose (KMP step 6g, part 2)

## Objetivo
Move the top bar of the habit list (title, add and filter buttons, search, sort, overflow menu) and the multi-selection bar to Jetpack Compose, replacing the Android menus and the `ActionMode`, without changing what each action does.

## Por qué
Step 6 of `.specs/migration-kmp.md`; second of five PRs that migrate the habit list piece by piece. The behaviour already lives in core (`ListHabitsMenuBehavior`, `ListHabitsSelectionMenuBehavior`, with tests), so this PR only changes how it is shown and triggered.

## Alcance
- In: a Compose bar (`ListHabitsBar`) with the normal, search and selection states; `ListHabitsMenu` and `ListHabitsSelectionMenu` turned into state holders (same classes and injection); pure rules with tests; removal of `res/menu/list_habits.xml`, `res/menu/list_habits_selection.xml`, `res/layout/toolbar.xml` and the `ActionMode`; test tags and the instrumented steps that use the bar.
- Out: the day header, the rows and the drag (next PRs), keeping the search across a rotation (it is lost today and stays that way), the activity glue and result codes (last PR).
- Decisions (user): five incremental PRs, odd-full, deep review for this one.

## Exploración
- `ListHabitsMenu` inflates `R.menu.list_habits` on every `invalidateOptionsMenu()` and reads the checked states from `Preferences` (`showArchived`, `showCompleted`, `defaultPrimaryOrder`) and `themeSwitcher.isNightMode`. The hide completed item says "hide entered" when question marks or skip are enabled. The sort arrows: ascending orders show an arrow down, descending ones an arrow up, manual shows an arrow up. The search view is shown in place of the buttons while `isSearchActive`; closing it clears the query.
- `ListHabitsSelectionMenu` starts a support `ActionMode` when `HabitCardListController` enters the selection; the title is the number of selected habits; edit shows only with one selected, archive and unarchive depend on `canArchive` and `canUnarchive`, notify only for developers, delete and colour always. Back or the close button of the mode ends the selection through `onSelectionFinished`.
- The bar is built with `setupToolbar`: palette colour 17 when `useHabitColorAsPrimary` (light theme), otherwise `colorPrimary`, 2 dp elevation, status bar painted with the same colour.
- `ListHabitsActivity` forwards `onCreateOptionsMenu` and `onOptionsItemSelected` to the menu and calls `invalidateOptionsMenu()` after every tap and when the question marks preference changes (the skip preference does not notify, so the label could be stale).
- Tests: `ListHabitsSteps` uses `R.id.actionCreateHabit`, `R.id.action_filter`, `R.id.action_edit_habit`, the overflow ("More options" in a Toolbar) and text clicks; `HabitsTest.shouldAllowMultipleSelection` reads the count as text.

## Incertidumbre
- The nested sort menu, the checked states and the back behaviour of the selection bar can only be judged on a device.
- Whether the bar looks like the old one (colour, elevation, status bar) in light, dark and pure black.

## Checklist
- [x] TASK-1: task doc — criterio: this file committed
- [x] TASK-2: pure bar rules with tests first — criterio: JVM tests pass
- [x] TASK-3: `ListHabitsMenu` and `ListHabitsSelectionMenu` as state holders — criterio: assembles
- [x] TASK-4: `ListHabitsBar` and its wiring in `ListHabitsRootView` and `ListHabitsActivity` — criterio: assembles
- [x] TASK-5: remove the menus, the toolbar layout and the `ActionMode`; port the instrumented steps — criterio: no references left, `compileDebugAndroidTestKotlin` passes
- [x] TASK-6: CHANGELOG, PENDING, AGENTS.md — criterio: docs updated; ktlint, unit tests, core tests green

## Estrategia de entrega
`single-pr`, branch `feat/compose-list-toolbar`, base `develop`. Commits only when the user asks.

## Evidencia
- `assembleDebug`, `assembleRelease`, `compileDebugAndroidTestKotlin`, `ktlintCheck`, `:salem-android:testDebugUnitTest` (6 new tests in `ListBarModelTest`) and `:salem-core:jvmTest` pass.
- `ListHabitsBar` (Compose) has three states: main bar (title, add, filter menu with hide archived, hide completed, a sort list with arrows and search; overflow with dark theme, settings, help and about), search field, and selection bar (count, close, edit when one is selected, colour, overflow with archive, unarchive, delete and notify for developers). The back button leaves the selection through `ListHabitsSelectionMenu.cancel()` (the same `onSelectionFinished` the action mode used).
- `ListHabitsMenu` and `ListHabitsSelectionMenu` keep their names and injection (so `HabitCardListController` and the DI components are untouched) but no longer use Android menus or `ActionMode`; their state is read again every time a menu opens, so the hide completed label follows the skip setting too.
- Removed: `res/menu/list_habits.xml`, `res/menu/list_habits_selection.xml`, `res/layout/toolbar.xml`, `buildToolbar()`, `onCreateOptionsMenu`/`onOptionsItemSelected` and all `invalidateOptionsMenu()` calls of the list. `setupToolbar` stays for the habit and automation screens.
- New strings `more_options` and `close` (English only; AppCompat translated the old overflow label).
- Instrumented steps: the bar buttons are found by test tag (`actionCreateHabit`, `action_filter`, `overflowMenu`, `selectionOverflow`, `action_edit_habit`, `selectionCount`); steps shared with the habit screen (edit, delete, archive, settings...) look for the Compose button first and fall back to the Espresso overflow. `shouldAllowMultipleSelection` checks the count with `verifySelectionCount`. They compile but were not run.
- Not run: the bar on a device (colour, elevation and status bar in light, dark and pure black; the filter, sort and overflow menus; searching; selecting and the back button) or the instrumented tests.
- Review fixes (rdd-full, one independent reviewer, Risk and Reliability): the status bar is painted with the colour of the bar (it is not edge to edge before Android 15, and `setupToolbar` used to do it); Back closes the search field; the bars only take the top window inset, because `applyRootViewInsets` already pads the root on the left and right; the instrumented steps wait up to 1.5 s for the Compose button before falling back to the Views one. Left in `docs/PENDING.md`: keyboard on closing the search, TalkBack state of the checkable items, dropdown colours, untranslated labels, tests of the two state holders.

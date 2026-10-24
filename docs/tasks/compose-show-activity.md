# Habit detail: the activity in Compose (KMP step 6h, part 5)

## Objetivo
Make the habit detail screen one Compose screen: `ShowHabitActivity` uses `setContent`, with a Compose top bar and menu, and the Views that held the toolbar, the menu and the cards are removed.

## Por qué
Step 6 of `.specs/migration-kmp.md`; last of five PRs for the habit detail screen. After part 4 only the toolbar, the options menu and the activity were Views.

## Alcance
- In: `ShowHabitBar` (colour of the habit or primary, way back, Edit, menu with Export, Archive or Unarchive, Delete and Randomize for developers), `ShowHabitHost` (state, callbacks, menu state and bar colour), `ShowHabitScreen` (bar and cards, side insets), the activity (`setContent`, safe lookup of the habit, status bar colour, theme from the switcher); pure `habitIdFrom` and `barColorFor` with tests; the string `randomize`; removal of `ShowHabitView`, `ShowHabitMenu`, `show_habit.xml`, the options menu `show_habit.xml` and `applyBottomInset` (no users); the instrumented steps look for the Compose tags.
- Out: the dialogs (View fragments), the presenters in core, `setupToolbar`, `applyToolbarInsets` and `applyRootViewInsets` (`EditSettingActivity` still uses them), the intent actions and the habit address (external protocol).
- Decisions (user): five incremental PRs; **no emulator or device runs without asking**; a deep review is offered at the end.

## Exploración
- The activity built everything by hand (no DI) and never invalidated the options menu, so Archive did not become Unarchive after archiving; the Compose menu is read from the state of each refresh.
- `habit = getById(ContentUris.parseId(intent.data!!))!!` crashed when the address was missing or the habit deleted (widget, notification, back stack). Now it finishes the activity.
- The up navigation comes from the `PARENT_ACTIVITY` of the manifest; `onSupportNavigateUp()` of `AppCompatActivity` follows it.

## Incertidumbre
- Nothing was run on a device: bar and status bar colours, the way back, the menu actions, Randomize, opening a deleted habit, rotation, themes.

## Checklist
- [x] TASK-1: `habitIdFrom` and `barColorFor` with tests first — criterio: `ShowHabitModelTest` passes
- [x] TASK-2: `ShowHabitBar`, `ShowHabitHost`, `ShowHabitScreen` — criterio: assembles
- [x] TASK-3: the activity with `setContent` and the safe lookup — criterio: assembles
- [x] TASK-4: remove the old Views, layout, menu and dead code; port the steps — criterio: `compileDebugAndroidTestKotlin` passes
- [x] TASK-5: CHANGELOG, PENDING, AGENTS.md — criterio: docs updated; ktlint, unit tests and core tests green

## Estrategia de entrega
`single-pr`, branch `feat/compose-show-activity`, base `develop`. Commits only when the user asks.

## Evidencia
- `ktlintCheck`, `:salem-android:testDebugUnitTest`, `assembleDebug`, `assembleRelease`, `:salem-core:jvmTest` and `compileDebugAndroidTestKotlin` pass.
- Nothing was run on an emulator or a device.

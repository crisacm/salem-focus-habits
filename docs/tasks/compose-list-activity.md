# Habit list: the activity in Compose (KMP step 6g, part 5)

## Objetivo
Make the habit list screen one Compose composition: `ListHabitsActivity` uses `setContent`, `ListHabitsRootView` is removed, and the Settings and import-file results go through `ActivityResultContracts`.

## Por qué
Step 6 of `.specs/migration-kmp.md`; last of five PRs that migrate the habit list piece by piece. After parts 1 to 4 the screen was a `FrameLayout` holding five separate `ComposeView`s plus two Views; the rest of the habit list still depended on that Android View.

## Alcance
- In: `ListHabitsHost` (state and listeners outside the composition), `ListHabitsContent` (the whole screen), the activity (`setContent`, status bar colour, confetti overlay), `ListHabitsScreen` (no `ListHabitsRootView`, `registerForActivityResult` for Settings and the file picker, no `onActivityResult`), guards for an empty selection and for a habit that no longer exists, the `LIST_HABITS` step of the instrumented tests, CHANGELOG, PENDING, AGENTS.md.
- Out: the habit detail (`ShowHabitActivity`), `HistoryEditorDialog`, the vendored `TimePickerDialog`, the Settings screen itself (it keeps `setResult` with the codes 101 to 105), the intent actions and the `org.isoron.uhabits` URI (external protocol).
- Decisions (user): five incremental PRs; deep review at the end of this one.

## Exploración
- `ListHabitsRootView` (234 lines) did: wiring of the five composables, attach and detach listeners (preferences, midnight, list state observable), `onSizeChanged` (columns and maximum day offset), the saved day offset, bar colour and status bar colour, side insets, the confetti and progress Views, and the empty state visibility.
- `ListHabitsScreen` used it only for `konfettiView` and the current theme; it started Settings (107) and the file picker (106) with `startActivityForResult`, answered in `onActivityResult`.
- Manifest: `ListHabitsActivity` is already `singleTop` and the `.MainActivity` alias exists; nothing to change. `ACTION_EDIT` is only sent as an explicit intent.
- `parseIntents` did `getById(habitId)!!` and `ListHabitsSelectionMenuBehavior.onChangeColor` did `getSelected()[0]`.

## Incertidumbre
- Insets and status bar colour on Android 15 (edge to edge) and before, rotation, confetti position (the overlay is padded like the old root), midnight, the order of the attach listeners relative to `onResume`, importing a file, coming back from Settings with each code: only a device shows them.

## Checklist
- [x] TASK-1: guards (empty selection in core with a regression test, missing habit in `parseIntents`, `showEditHabitsScreen`) — criterio: core test passes
- [x] TASK-2: `checkmarkColumns` as a tested pure function — criterio: `CheckmarkColumnsTest` passes
- [x] TASK-3: `ListHabitsHost` and `ListHabitsContent`, the activity uses `setContent`, `ListHabitsRootView` removed — criterio: assembles, no references left
- [x] TASK-4: results through `ActivityResultContracts` — criterio: assembles
- [x] TASK-5: instrumented step `LIST_HABITS` waits for the tag `habitListScreen` — criterio: `compileDebugAndroidTestKotlin` passes
- [x] TASK-6: CHANGELOG, PENDING, AGENTS.md — criterio: docs updated; ktlint, unit tests, core tests green

## Estrategia de entrega
`single-pr`, branch `feat/compose-list-activity`, base `develop`. Commits only when the user asks.

## Evidencia
- `ktlintCheck`, `:salem-android:testDebugUnitTest`, `:salem-core:jvmTest`, `assembleDebug` and `compileDebugAndroidTestKotlin` pass.
- Not checked yet: the look on a device (see Incertidumbre), the instrumented tests (they are flaky, see `docs/PENDING.md`).

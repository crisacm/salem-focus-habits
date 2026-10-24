# Habit detail: subtitle, notes and the cards container in Compose (KMP step 6h, part 4)

## Objetivo
Move the subtitle and notes cards of the habit detail screen to Compose and replace the layout that held the nine cards by one Compose column, so no card is a View any more.

## Por qué
Step 6 of `.specs/migration-kmp.md`; fourth of five PRs for the habit detail screen. Seven cards were Compose but each one still sat inside a View that gave it the card style; with all nine in Compose the column can draw the style itself and the hosting Views go away. Only the toolbar, the menu and the activity remain for the next PR.

## Alcance
- In: `SubtitleCardContent`, `NotesCardContent`, `ShowHabitContent` (scrolling column, card style, bottom inset), `ShowHabitModel.kt` (`ShowCard`, `visibleCards`, `targetText`, tested), `ShowHabitView` reduced to the toolbar plus a `ComposeView`, `show_habit.xml` with the toolbar and the `ComposeView`; removal of the nine card Views, `show_habit_subtitle.xml`, `show_habit_notes.xml`, `styles_show_habit.xml`, the `CardList` style, `SubtitleCardViewTest`, `NotesCardViewTest` and their PNGs; `CommonSteps` looks for Compose tags.
- Out: `ShowHabitActivity`, the menu and the toolbar (`setupToolbar`, `setSupportActionBar`), `HistoryEditorDialog`, the `Card` and `CardHeader` styles (still used by `about_translators.xml`).
- Decisions (user): five incremental PRs; **no emulator or device runs without asking**.

## Exploración
- The old rules were spread over the layout and the view: the overview card is hidden for numerical habits and the target card for yes or no habits; the notes are hidden without a description; the subtitle hides the target for yes or no habits and the question when empty.
- The card style (`Card`, `CardCommon`): card colour, 1 dp elevation, 3 dp at the sides, 1 dp below, padding 16 and 4 dp, exceptions for overview and target (12 dp above) and history (0 below). The subtitle has its own style (header colour, 2 dp elevation, 60 dp at the start).
- The theme colours of core match the attributes of the Android themes (`appBackgroundColor`, `cardBackgroundColor`, `headerBackgroundColor`, `mediumContrastTextColor`, `highContrastTextColor`).

## Incertidumbre
- Nothing was run on a device: the copied style (spacing, shadows), the subtitle layout and its FontAwesome icons, vertical scroll with horizontally draggable charts, the bottom space with the keyboard, dark and pure black themes.

## Checklist
- [x] TASK-1: pure rules with tests first — criterio: `ShowHabitModelTest` passes
- [x] TASK-2: subtitle and notes composables — criterio: assembles
- [x] TASK-3: `ShowHabitContent` and `ShowHabitView` with the toolbar and a `ComposeView` — criterio: assembles
- [x] TASK-4: remove the Views, layouts, styles, tests and PNGs; port the steps — criterio: `compileDebugAndroidTestKotlin` passes
- [x] TASK-5: CHANGELOG, PENDING, AGENTS.md — criterio: docs updated; ktlint and unit tests green

## Estrategia de entrega
`single-pr`, branch `feat/compose-show-container`, base `develop`. Commits only when the user asks.

## Evidencia
- `ktlintCheck`, `:salem-android:testDebugUnitTest` (new `ShowHabitModelTest`), `assembleDebug` and `compileDebugAndroidTestKotlin` pass.
- Nothing was run on an emulator or a device.

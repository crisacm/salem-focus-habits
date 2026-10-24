# Habit detail: score and frequency cards in Compose (KMP step 6h, part 2)

## Objetivo
Move the score and frequency cards of the habit detail screen to Compose and rewrite their charts as classes of `salem-core` that draw on the multiplatform `Canvas`.

## Por qué
Step 6 of `.specs/migration-kmp.md`; second of five PRs for the habit detail screen. The history and bar charts were already in core; these two were Views that drew with `android.graphics.Canvas`. Writing them once in core lets `CoreChart` show them and prepares the iOS work (step 7).

## Alcance
- In: `Canvas.fontSpacing()` (core interface, `AndroidCanvas`, `JavaCanvas`), core `ScoreChart` and `FrequencyChart` with image tests and reference PNGs, `ScoreCardContent` and `FrequencyCardContent`, `CardTitle` shared, `ScoreCardView` and `FrequencyCardView` host a `ComposeView`; removal of `show_habit_score.xml`, `show_habit_frequency.xml`, the two card image tests and their PNGs.
- Out: the old Views `ScoreChart`, `FrequencyChart` and `ScrollableChart` (the widgets use them), the transparent marker of the widget (needs a blend mode in the `Canvas`), the other cards, the activity.
- Decisions (user): five incremental PRs; charts rewritten once in core over the `Canvas`; widgets keep their Views for now.

## Exploración
- Both charts are drawn from the data and the size of the canvas; the sizes come from the height (`baseSize`) and the width of the widest month name. The score marker is a disc of the card colour with a smaller disc of the chart colour on top, so the opaque version needs no blend mode.
- The core `Canvas` takes the text y about the middle of the lower case letters, the old Views used the baseline; the new charts convert with `0.3 * fontSize`.
- Reference images: written by running the new tests, checked by eye against the old Android PNGs, and copied to `salem-core/assets/test/views/`. `JavaCanvas` has larger font metrics than Android, so the images show a slightly shorter chart.

## Incertidumbre
- The look on a device (text positions, footer labels, scale of the dots), the scroll feel and the spinner with other colours.

## Checklist
- [x] TASK-1: `Canvas.fontSpacing()` — criterio: core and Android compile
- [x] TASK-2: core `ScoreChart` with image tests — criterio: `ScoreChartTest` passes against reviewed PNGs
- [x] TASK-3: core `FrequencyChart` with image tests — criterio: `FrequencyChartTest` passes
- [x] TASK-4: Compose cards, host Views, old layouts and tests removed — criterio: assembles, `compileDebugAndroidTestKotlin` passes
- [x] TASK-5: CHANGELOG, PENDING, AGENTS.md, iOS block list — criterio: docs updated; ktlint, unit tests and core tests green

## Estrategia de entrega
`single-pr`, branch `feat/compose-show-score-frequency`, base `develop`. Commits only when the user asks.

## Evidencia
- `ktlintCheck`, `:salem-core:jvmTest`, `:salem-android:testDebugUnitTest`, `assembleDebug` and `compileDebugAndroidTestKotlin` pass.
- iOS: the two new image tests are in `salem-core/ios-blocked-tests.txt` like the other chart tests (no iOS `Canvas` yet); the CI compiles the iOS targets.

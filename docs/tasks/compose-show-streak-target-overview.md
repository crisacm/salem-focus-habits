# Habit detail: streak, target and overview cards in Compose (KMP step 6h, part 3)

## Objetivo
Move the streak, target and overview cards of the habit detail screen to Compose, with the streak and target charts rewritten as classes of `salem-core`.

## Por qué
Step 6 of `.specs/migration-kmp.md`; third of five PRs for the habit detail screen. The bars of the streak and target charts only need what the multiplatform `Canvas` already has, so they are written once in core; the overview ring is the Compose ring that the list already uses.

## Alcance
- In: core `StreakChart` and `TargetChart` with image tests and reference PNGs, `drawTextAtBaseline` (the baseline conversion that `ScoreChart` and `FrequencyChart` had copied), `Theme.highContrastReverseTextColor`, `CoreView` (draws any core `View`; `CoreChart` is built on it), `ScoreRing` with size, thickness and precision, `StreakCardContent`, `TargetCardContent`, `OverviewCardContent`, the three card Views host a `ComposeView`; removal of the three card layouts, `StreakCardViewTest`, `OverviewCardViewTest` and their PNGs; `intervalToLabel` as a top-level function (the target widget imports it).
- Out: the old Views `StreakChart`, `TargetChart`, `RingView` and their tests (widgets), the other cards, the activity, the core `Ring` (no user).
- Decisions (user): five incremental PRs; charts rewritten once in core; widgets keep their Views; **no emulator or device runs without asking**.

## Exploración
- Streak and target are rows of 20 dp with rounded bars; no blend mode or stroked arc is needed. The overview ring is a filled arc, an inactive arc and a hole, which a stroked arc draws in Compose.
- `longFormat` of the date formatter and `toShortString` are not in the core interface (Java and Android code), so the core charts take `formatDate` and `formatValue` as functions.
- The old streak text colour `contrast0` is white in the light themes and dark grey in the dark ones; the core `Theme` had no equivalent, so it gets `highContrastReverseTextColor`.

## Incertidumbre
- Nothing was run on a device: the look of the bars and the text, the overview ring at 30 dp, the five columns of the overview and the colours with other habits and themes.

## Checklist
- [x] TASK-1: shared text helper; `ScoreChart` and `FrequencyChart` use it without changing their images — criterio: existing core tests pass
- [x] TASK-2: core `StreakChart` with image tests — criterio: `StreakChartTest` passes against reviewed PNGs
- [x] TASK-3: core `TargetChart` with image tests — criterio: `TargetChartTest` passes
- [x] TASK-4: `CoreView`, `ScoreRing` parameters, the three Compose cards and host Views — criterio: assembles
- [x] TASK-5: remove old layouts, tests and PNGs — criterio: `compileDebugAndroidTestKotlin` passes
- [x] TASK-6: CHANGELOG, PENDING, AGENTS.md, iOS block list — criterio: docs updated; ktlint, unit tests and core tests green

## Estrategia de entrega
`single-pr`, branch `feat/compose-show-streak-target-overview`, base `develop`. Commits only when the user asks.

## Evidencia
- `ktlintCheck`, `:salem-core:jvmTest`, `:salem-android:testDebugUnitTest` (new `OverviewCardTest`), `assembleDebug` and `compileDebugAndroidTestKotlin` pass.
- Nothing was run on an emulator or a device.

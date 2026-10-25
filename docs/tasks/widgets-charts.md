# Widgets on the charts of salem-core, part 2: the chart widgets (KMP step 6k)

## Objetivo
Make the score, frequency, streak, target and history widgets draw their charts with the chart classes of `salem-core`, and remove the old chart Views.

## Por qué
The cards of the detail screen already use the core charts; the widgets still had their own copies written with `android.graphics.Canvas`. With the base of the previous change (`Canvas.clearCircle`, the transparent score marker, `CoreWidgetView`) the widgets can use the same code.

## Alcance
- In: `ScoreWidget`, `FrequencyWidget`, `StreakWidget`, `TargetWidget` and `HistoryWidget` build a `CoreWidgetView` and give it the core chart; `TargetChart.stretchRows` (the rows of the widget share its height, as the old chart did) with an image test; the number of streaks that fit is computed from the measured height; removal of `ScoreChart`, `FrequencyChart`, `StreakChart`, `TargetChart`, `ScrollableChart`, `BundleSavedState`, `AndroidDataView`, `AndroidView`, the dimension `baseSize`, the tests `ScoreChartTest`, `FrequencyChartTest` and `StreakChartTest` of androidTest with their images, and the entries of `lint-baseline.xml` of deleted files.
- Out: `CheckmarkWidgetView` and `RingView` (next change), the reference images of the widget tests of androidTest (they can only be regenerated on a device).
- Decisions (user): three incremental changes; **no emulator or device runs without asking**.

## Exploración
- Every widget fills its chart in `refreshData(view)` after the view was measured; the core charts are built there with `WidgetTheme`, `theme.color(habit.color)`, a `JavaLocalDateFormatter` and the same data as before.
- The widget bitmap is drawn at the density of the device and `CoreWidgetView` uses the same density, so the dp of the core charts land on the pixels the old Views used.
- The old target chart stretched its rows over the height of the widget; the core one has fixed rows unless `stretchRows` is on.

## Incertidumbre
- Nothing was run on a device: the five widgets, the markers of the score, the rows of the target widget, the number of streaks, and the reference images of the widget tests.

## Checklist
- [x] TASK-1: `TargetChart.stretchRows` with an image test — criterio: `:salem-core:jvmTest` passes
- [x] TASK-2: the five widgets use the core charts — criterio: assembles
- [x] TASK-3: remove the old Views, `AndroidDataView`, `AndroidView`, their tests and images — criterio: assembles, `compileDebugAndroidTestKotlin` passes
- [x] TASK-4: CHANGELOG, PENDING — criterio: docs updated; ktlint and tests green

## Estrategia de entrega
`single-pr`, branch `feat/widgets-charts`, base `develop`. Commits only when the user asks.

## Evidencia
- `ktlintCheck`, `:salem-core:jvmTest`, `:salem-android:testDebugUnitTest`, `assembleDebug`, `assembleRelease` and `compileDebugAndroidTestKotlin` pass.
- Nothing was run on an emulator or a device.

# Widgets on the charts of salem-core, part 1: the base (KMP step 6k)

## Objetivo
Give the charts of `salem-core` what the home-screen widgets need, so the widgets can stop using the old chart Views in the next two changes.

## Por qué
The widgets draw to a bitmap with a transparent card (`WidgetTheme`). The core charts already look right on it with normal drawing, except where the old Views cut a hole with `PorterDuff.CLEAR` in a bitmap of their own: the markers of the score chart and the hole of the ring. The multiplatform `Canvas` had no way to do that, and the core `Ring` was unused and incomplete.

## Alcance
- In: `Canvas.clearCircle` (`AndroidCanvas` with a CLEAR paint, `JavaCanvas` with `AlphaComposite.Clear`); the marker of the core `ScoreChart` is a hole when the card of the theme is transparent; `RingChart` (percentage with a precision, an inactive colour, a hole that is a colour or transparent, a text that can be FontAwesome) replacing the unused `Ring` and its orphan PNG; `CoreWidgetView`, a View that draws a core view on a transparent layer; core image tests with `WidgetTheme` for the score, frequency, streak and target charts and for the ring.
- Out: the widgets themselves, the old Views, the widget image tests of androidTest (changes 2 and 3), a stroke for the text of the ring.
- Decisions (user): three incremental PRs; **no emulator or device runs without asking** (the widgets are tested by hand at the end).

## Exploración
- With `SRC_OVER` the bars, lines, grid and text of the core charts are right on a transparent card; the hole is needed only for the score marker and the ring.
- CLEAR on the bitmap of a widget would erase the card behind the chart, so `CoreWidgetView` draws on a `saveLayer` that starts transparent, as the old Views did with a bitmap cache.
- The reference images with `WidgetTheme` are transparent with white text; they were checked by putting them over a dark colour.

## Incertidumbre
- Nothing was run on a device: `CoreWidgetView` and the CLEAR paint inside the layer, the look of the markers and of the ring against the old widgets.

## Checklist
- [x] TASK-1: `clearCircle` in the interface and both implementations — criterio: core and Android compile
- [x] TASK-2: the transparent marker of the score chart and `RingChart` with image tests — criterio: `:salem-core:jvmTest` passes against reviewed PNGs
- [x] TASK-3: `CoreWidgetView` and the widget-theme tests of the other charts — criterio: assembles
- [x] TASK-4: CHANGELOG, PENDING, iOS block list — criterio: docs updated; ktlint and tests green

## Estrategia de entrega
`single-pr`, branch `feat/widgets-core-base`, base `develop`. Commits only when the user asks.

## Evidencia
- `ktlintCheck`, `:salem-core:jvmTest`, `:salem-android:testDebugUnitTest`, `assembleDebug` and `compileDebugAndroidTestKotlin` pass.
- Nothing was run on an emulator or a device.

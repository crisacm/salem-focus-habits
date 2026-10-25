# Widgets on the charts of salem-core, part 3: the checkmark widget (KMP step 6k)

## Objetivo
Make the checkmark widget draw its ring with `RingChart` and remove `RingView`.

## Por qué
It was the last widget on an old chart View.

## Alcance
- In: `Canvas.strokeText` (`AndroidCanvas`, `JavaCanvas`); `RingChart.isTextStroked` with an image test; `CheckmarkWidgetView` hosts a `CoreWidgetView` in the `scoreRing` frame; removal of `RingView`, `RingViewTest` and its images.
- Out: the reference images of `CheckmarkWidgetTest` and `CheckmarkWidgetViewTest` (only regenerated on a device).
- Decisions (user): keep the outlined text of the automatic checkmark (stroke added to the Canvas); odd-lite; **no emulator or device runs without asking**.

## Exploración
- The old ring was square (`min(width, height)`), had a transparent hole and always used FontAwesome; `RingChart` centres itself the same way. Sizes were px, now px / density in dp.
- The old `RingView` never reset its paint style after stroking text; `AndroidCanvas.strokeText` restores FILL.

## Incertidumbre
- Nothing run on a device: the widget, the outlined check, the reference images.

## Checklist
- [x] TASK-1: `Canvas.strokeText` + `RingChart.isTextStroked` with image test — criterio: `:salem-core:jvmTest` passes
- [x] TASK-2: `CheckmarkWidgetView` on `CoreWidgetView`, `RingView` removed — criterio: assembles, `compileDebugAndroidTestKotlin` passes
- [x] TASK-3: CHANGELOG, PENDING — criterio: docs updated; ktlint and tests green

## Estrategia de entrega
`single-pr`, branch `feat/widgets-checkmark`, base `develop`. Commits only when the user asks.

# Charts on the shared canvas (KMP step 7, detail screen PR 4a)

## Objetivo
Make the shared canvas (`DrawScopeCanvas`) place text like the Android one and give `salem-ui` a chart host with gestures (`CoreChart`), so the charts of the habit detail can run on iOS and on Android from the same code.

## Por qué
The habit detail (cards and six core charts) moves to `salem-ui` in PRs 4a-4d. The charts of the core are laid out for the placement of `AndroidCanvas` (`y` is about the middle of the lower-case letters; `core/ui/views/ChartText.kt` subtracts 0.3 of the font size from it), while `DrawScopeCanvas` centred the whole line box, and `CoreChart`/`CoreView` existed only for Android.

## Alcance
- In: text placement of `DrawScopeCanvas`; `CoreChart` in `salem-ui` (`ui.canvas`, drag, fling, tap and long press in dp, `version`); desktop tests that render the six charts through `DrawScopeCanvas` and compare them with the reference images of the core; tests of the placement and of the gestures.
- Out (4b-4d): the cards, the detail screen, the history editor, the iOS wiring; the Android cards keep their own `CoreChart`/`CoreView` (`AndroidCanvas`) until 4b.
- Decisions (user): workflow odd-full, 4 stacked PRs, TDD where there is logic, no emulator/simulator without asking.

## Exploración
`AndroidCanvas.drawText` draws at `y + 0.6 * height("m")`; `JavaCanvas` (which made the reference images) centres the bounding box of the string; `DrawScopeCanvas` centred the line box. The explorer reported that `measureText` and `fontSpacing` ignore the font and size: they do not (`measure` applies `style()`), and the new tests keep that covered. Noto Sans, the font of the core on every platform, has an x-height of 0.536 em (regular) and 0.546 em (bold), read from its `OS/2` table.

## Incertidumbre
- Height of the "m": Compose has no API for the ink of a glyph, so the code uses 0.54 times the font size, which is right for the bundled Noto Sans. `Font.FONT_AWESOME` keeps the old centring of the line box (a glyph has no lower-case letters), so the rings keep matching their reference images.
- The reference images were made by `JavaCanvas`, so the distances are not zero. Measured with the placement of this PR (percent of luminosity): TargetChart 0.059, BarChart 0.105, StreakChart 0.126, ScoreChart 0.127, HistoryChart 0.250, FrequencyChart 0.425. The images were looked at (Frequency): same structure, the text a few pixels lower. Each chart has its own limit of about 1.4 times its distance.
- Not known without a device: how it looks next to the Android Views (`AndroidCanvas` uses the system font, the shared canvas Noto Sans).

## Checklist
- [x] TASK-1: `DrawScopeCanvasTextTest` (7 tests; the baseline ones fail with the old placement) — criterio: RED observed
- [x] TASK-2: text placement in `DrawScopeCanvas`
- [x] TASK-3: `CoreChartTest` (4 tests: tap, long press, drag, drawn) and `CoreChart` — criterio: RED observed
- [x] TASK-4: `ChartRenderTest` (6 charts against the core images) with per-chart limits
- [x] TASK-5: docs and changelog

## Estrategia de entrega
Stacked PRs 4a to 4d; this one is `feat/ui-charts-canvas`, from `develop` after #63. The user merges in order. Commits only when the user asks.

## Evidencia
`:salem-core:jvmTest`, `:salem-ui:desktopTest` (all), `:salem-android:testDebugUnitTest`, `ktlintCheck`, `compileDebugAndroidTestKotlin`, `assembleDebug`, iOS compile (arm64 and simulator) and `xcodebuild` (build only). Nothing in the Android app uses the new code yet. Not run: the simulator, any emulator, real gestures.

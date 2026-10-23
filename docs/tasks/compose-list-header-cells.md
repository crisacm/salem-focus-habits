# Habit list: day header and cells in Compose (KMP step 6g, part 3)

## Objetivo
Move the day header of the habit list to Jetpack Compose, driving the shared horizontal offset of the rows, and add the Compose cells (checkmark, number, score ring) that the next PR will use in the rows.

## Por qué
Step 6 of `.specs/migration-kmp.md`; third of five PRs that migrate the habit list piece by piece. Only the header changes the horizontal offset (`dataOffset`) of the rows, so it can be migrated on its own while the rows are still Views. The cells are written and tested now so that the rows PR only has to wire them.

## Alcance
- In: `HeaderContent` and `HeaderState` (columns, drag and fling), pure rules with tests (header columns, scroll direction, scroll to offset conversion, cell looks, ring angle), the Compose cells `CheckmarkCell`, `NumberCell` and `ScoreRing` (not used yet), removal of `HeaderView` and its image test and PNGs, the instrumented `offsetHeaders` step.
- Out: `ScrollableChart` (still used by the score and frequency charts), `RingView` (used by the widget), the Views cells and rows, the dead drawing classes of `salem-core` (kept for the iOS work; noted in `docs/PENDING.md`).
- Decisions (user): five incremental PRs, retire the image tests of each migrated view, leave the unused core drawing classes alone.

## Exploración
- `HeaderView` draws `buttonCount` columns of 48 dp aligned to the right with 3 dp of margin: the date of column `index` is `today - (index + dataOffset)`, weekday in upper case (short name) over the day of the month, 10 sp bold in `contrast60`, on `headerBackgroundColor`, 48 dp tall with 2 dp of elevation. Default order has today on the left; with `isCheckmarkSequenceReversed` it is on the right; RTL mirrors it. It refreshes at midnight.
- The header is the only writer of `dataOffset`: drag and fling (`ScrollableChart`) with a 48 dp bucket, direction ±1 (reversed and RTL flip it), maximum `max(60 - count, 0)` set from `ListHabitsRootView.onSizeChanged`; the root view copies it to `listView.dataOffset`, which forwards it to the attached cards. Rows do not scroll by themselves.
- `ScrollableChart.onScroll` has no lower bound for its internal position, so dragging past zero builds a dead zone before the header answers a reversal. The Compose version clamps to `[0, max]`.
- Cell rules: checkmark glyph and colour by value (check for yes, `skipped` for skip, times or question for no and unknown depending on question marks; the automatic yes is a hollow check; colours habit, `contrast40` and `contrast60`; sizes 14, 13 and 12 sp; a dot when there are notes; a tap toggles and a long press edits, or the opposite with the short toggle preference, with haptic feedback on toggle); number cell (`toShortString`, bold condensed, units with ellipsis, skipped glyph for 0.003, question mark for negative values, colour by `completed` and sign); the ring angle is quantized to 1/16.
- Tests: `HeaderViewTest` and its PNGs; `offsetHeaders` swipes at fixed pixel coordinates.

## Incertidumbre
- The feel of the fling (the Views used a `Scroller`), the restore of the offset after a rotation (the list restores its own offset), and the exact look of the cells can only be judged on a device.

## Checklist
- [x] TASK-1: task doc — criterio: this file committed
- [x] TASK-2: pure rules with tests first — criterio: JVM tests pass
- [x] TASK-3: `HeaderState` and `HeaderContent` replacing `HeaderView` in `ListHabitsRootView` — criterio: assembles
- [x] TASK-4: `CheckmarkCell`, `NumberCell` and `ScoreRing` — criterio: assembles
- [x] TASK-5: remove `HeaderView` and its tests and PNGs; port `offsetHeaders` — criterio: no references left, `compileDebugAndroidTestKotlin` passes
- [x] TASK-6: CHANGELOG, PENDING, AGENTS.md — criterio: docs updated; ktlint, unit tests, core tests green

## Estrategia de entrega
`single-pr`, branch `feat/compose-list-header-cells`, base `develop`. Commits only when the user asks.

## Evidencia
- `assembleDebug`, `assembleRelease`, `compileDebugAndroidTestKotlin`, `ktlintCheck`, `:salem-android:testDebugUnitTest` (22 new tests in `HeaderModelTest` and `CellModelTest`) and `:salem-core:jvmTest` pass.
- `HeaderState` owns the scroll (`HeaderScroll`, pure and tested) and tells the rows through the same `listView.dataOffset` setter as before; `HeaderContent` draws the columns, drags with `draggable` and flings with a Compose decay (a new drag stops the fling). `ListHabitsRootView` keeps the `Preferences` and `MidnightTimer` listeners, attaches the offset of the list on attach, and sets the number of columns and the maximum offset from `onSizeChanged`.
- `toShortString` moved to `views/ShortString.kt`: in `NumberButtonView.kt` the file-level `Typeface.create` initializers made it impossible to call from a JVM test.
- The cells (`CheckmarkCell`, `NumberCell`, `ScoreRing` in `ListCells.kt`) are not used by any screen yet; they have content descriptions for the checkmark values, which the Views did not.
- Removed: `HeaderView`, `HeaderViewTest` and its PNGs. `offsetHeaders` drags the header found by its test tag instead of fixed coordinates. A compile-only Compose test covers a tap on a checkmark cell and the header.
- Not run: the header on a device (look, drag and fling feel, reversed order and right-to-left, the offset after a rotation, midnight), the cells, or any instrumented test.
- Review fixes (rdd-full, one independent reviewer, Risk and Reliability): an offset restored before the size is known (the usual case after a recreation) is kept and applied when the maximum is set, and the rows are told, so the header and the rows end on the same day (6 tests in `HeaderStateTest`); the fling now stops itself with `cancelAnimation()` of an `AnimationState` instead of throwing a `CancellationException` inside the animation, and stops at the ends; the column width and the height of the header come from `checkmarkWidth` and `checkmarkHeight`. Left in `docs/PENDING.md`: touch without movement not stopping a fling, drag slop, text baselines, clipping, shadow over the progress bar.

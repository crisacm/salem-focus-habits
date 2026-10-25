# Fixes of the review of the habit detail block (PRs #64-#67)

## Objetivo
Apply the correction that the user accepted after the rdd-full review of the shared canvas, the detail screen, the history editor and the iOS detail, and strengthen the tests that the review found weak.

## Por qué
The review (Reliability lens, two independent fresh reviewers, range `7216f1a..fbcb57b`) found no High issues, three Medium and several Low ones. The user accepted one bounded correction.

## Alcance
- In: `HabitDetailSession.attach()` is idempotent and a refresh does nothing while detached (a refresh queued before `detach()` is dropped); `HabitDetailRoute` refreshes the session when the kind of theme changes, so an open detail follows the dark mode; `HabitDetailScreen` draws its black screen from the first frame, before the state exists; the Android `ShowHabitActivity` cancels the scope of its refreshes in `onDestroy`; tests: `CoreChartTest` runs at two pixels per dp, `DrawScopeCanvasTextTest` measures the height of an "m", `ChartRenderTest` keeps the measured distance next to each limit and requires a distance above zero.
- Out (noted in `docs/PENDING.md`): the day or locale changing while the detail is open, the minor points of the review.
- Decisions (user): apply that correction in one pass; no new review loop.

## Exploración
The reviewers cited the places; nothing else was explored. Checked by hand: `resolveTheme` returns a new `Theme` object on every call (a theme has no equality), so the refresh is keyed by the class of the theme; the card states are data classes and the old Android cards also reset the scroll inside `remember(state)`, so that point is not a regression; `historyEditorHeight` exists in `salem-ui` (a doubt of the reviewer).

## Incertidumbre
- The tests of the density first failed for a wrong reason of mine: replacing `LocalDensity` does not change the density of the window that lays out and reads the pointers. They now run with `runSkikoComposeUiTest(density = Density(2f))`; changing the conversion of `CoreChart` to pixels makes two of them fail (checked, then restored).
- A failure while building the state of a deleted habit is not caught: the known flows detach before it can happen, and hiding other exceptions would hide real bugs.
- Not run: the simulator, any emulator, the instrumented tests; the theme change was checked on the JVM by the colour of the cards, not on a device.

## Checklist
- [x] TASK-1: 4 session tests (attach twice, one detach, a queued refresh, a refresh while detached) — criterio: RED observed, then GREEN
- [x] TASK-2: route test of the theme change and screen test of the first frame — criterio: RED observed, then GREEN
- [x] TASK-3: `CoreChartTest` at density 2 and its mutation check; the "m" height test; the measured distances in `ChartRenderTest`
- [x] TASK-4: the activity cancels its scope
- [x] TASK-5: docs, changelog and the receipt in Engram

## Estrategia de entrega
`single-pr`, branch `fix/detail-review-findings` from `develop`. Commits only when the user asks.

## Evidencia
`:salem-core:jvmTest`, `:salem-ui:desktopTest` (all), `:salem-android:testDebugUnitTest`, `ktlintCheck`, `compileDebugAndroidTestKotlin`, `assembleDebug`, iOS compile (arm64 and simulator) and `xcodebuild` (build only). Not run: the simulator, any emulator.

# The Intro on the shared UI (KMP step 7, PR 5b)

## Objetivo
Move the intro (three slides with Skip / Next / Done) to `salem-ui` so Android and iOS share it, and show it on the first launch of the iOS app.

## Por qué
`onStartup()` clears `isFirstRun` and calls `showIntroScreen`, which only logged on iOS, so the intro was lost. Second of three stacked PRs (5a About, 5b this one, 5c Settings). It is stacked on `feat/ui-about` (PR #69), which holds `ScreenNavigation` and the intro icons as shared drawables.

## Alcance
- In: `salem-ui` `ui.intro` (`IntroModel` with `StringResource`/`DrawableResource` slides, `IntroPager`, `IntroContent`), `AppScreen.INTRO`, the iOS layer, `IntroActivity` as host. `IntroModelTest` moves to the desktop tests.
- Out: reopening the intro from Settings or About (the user chose first launch only), Settings (5c).
- Decisions (user): odd-full, 3 stacked PRs, TDD where there is logic, no emulator/simulator without asking.

## Exploración
The brief of the Haiku `explorer` (5a) covered the intro: pure Compose except `R.*` ids and `androidx.activity.compose.BackHandler`; completion is a plain `finish()`; the trigger is `ListHabitsBehavior.onFirstRun`.

## Incertidumbre
- The Android back handler becomes the multiplatform `androidx.compose.ui.backhandler.BackHandler`; on Android it should behave the same (not seen on a device).
- On iOS the layer appears when `onStartup()` runs in a `LaunchedEffect` after the list is shown; not seen on the simulator.

## Checklist
- [x] TASK-1: move model and content, tests ported (`IntroModelTest` 5) — the logic was moved, not new
- [x] TASK-2: `IntroContentTest` (4: first page, next to Done, Skip and Done finish, back) and `ScreenNavigationTest` (3)
- [x] TASK-3: iOS layer (`IosListScreen.showIntroScreen`, `IosListRoot`)
- [x] TASK-4: docs and changelog

## Estrategia de entrega
Stacked PRs 5a to 5c; this one is `feat/ui-intro`, from `feat/ui-about`. The user merges in order. Commits only when the user asks.

## Evidencia
See the verification run in the PR. Not run: the simulator, any emulator, the instrumented tests. To check by hand: first launch on Android and iOS (three slides, Skip, Next, Done, dots, background colour animation), back on the second page, rotation, the status bar hidden on Android.

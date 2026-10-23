# Intro screen in Compose (KMP step 6d, part 1)

## Objetivo
Move `IntroActivity` from AppIntro to Jetpack Compose with the same slides and behaviour, and remove the AppIntro dependency.

## Por qué
Step 6 of `.specs/migration-kmp.md`. The intro is a small self-contained screen, shown once after the first launch (`ListHabitsBehavior.onFirstRun` → `showIntroScreen`). Removing AppIntro drops a View-only library from the Compose migration.

## Alcance
- In: Compose pager with the three slides, Skip / Next / Done and page indicators, pure slide list and pager state with tests, removal of `appIntro` (catalog, build file, ProGuard rule, `AGENTS.md`).
- Out: the AppIntro section of `NOTICE.md` (left for the owner to decide, it is a licence notice), the dialogs (second PR of step 6d), the missing slide number 3 (kept as is: `intro_title_1/2/4`), launching logic, theming of the intro (fixed slide colours, light AppCompat theme from the manifest).
- Decisions (user, via the plan): two PRs with odd-full; this one is Intro only.

## Exploración
- `IntroActivity : AppIntro2()` adds three `AppIntroFragment` slides (`#194673`, `#ffa726`, `#9575cd`; strings `intro_title_*`/`intro_description_*` 1, 2, 4; drawables `intro_icon_1/2/4`), hides the status bar, and calls `finish()` on done and skip. It writes no prefs and returns no result.
- Launch: `ListHabitsBehavior.onStartup` → `onFirstRun` (sets `isFirstRun=false`) → `ListHabitsScreen.showIntroScreen` → `IntentFactory.startIntroActivity`. Manifest: not exported, label empty, `Theme.AppCompat.Light.NoActionBar`.
- `intro_icon_1` is also used by the About header. AppIntro provides its own translated Skip/Done/Next labels; `done_label` and `skip_day` ("Skip") already exist in `strings.xml`, so the Compose version reuses them and adds `intro_next`.
- Tests: no test references `IntroActivity`; `BaseUserInterfaceTest` sets `isFirstRun=false`; core `ListHabitsBehaviorTest` covers `onStartup`.

## Incertidumbre
- Visual and gesture parity with AppIntro can only be judged on a device.

## Checklist
- [x] TASK-1: task doc — criterio: this file committed
- [x] TASK-2: slide list and pager state with tests first — criterio: JVM tests pass
- [x] TASK-3: Compose `IntroActivity` (pager, colours, buttons, indicators, hidden status bar) — criterio: assembles
- [x] TASK-4: remove AppIntro (catalog, build, ProGuard) and update docs — criterio: no references left, `assembleRelease` passes

## Estrategia de entrega
`single-pr`, branch `feat/compose-intro`, base `develop`. Commits only when the user asks.

## Evidencia
- `assembleDebug`, `assembleRelease`, `compileDebugAndroidTestKotlin`, `ktlintCheck`, `:salem-android:testDebugUnitTest` (5 new tests) and `:salem-core:jvmTest` pass; no references to AppIntro remain in code or build files.
- Skip reuses `skip_day` and Done reuses `done_label` (already translated); `intro_next` is new and only in English.
- Not run: the intro on a device (gestures, colour transition, hidden status bar, insets) and any instrumented test.

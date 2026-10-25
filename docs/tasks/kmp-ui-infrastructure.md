# Shared UI base: `salem-ui` module, Canvas over DrawScope and `iosApp` (KMP step 7, part 1)

## Objetivo
Create the base of the shared Compose Multiplatform UI: the `salem-ui` module, a `Canvas` of `salem-core` over a Compose `DrawScope` for iOS and Android, and an iOS app that hosts it.

## Por qué
Step 7 moves the screens to Compose Multiplatform. Before any screen moves, the shared module, the chart rendering on iOS (the only missing iOS piece) and the app that runs it must exist and be built by CI.

## Alcance
- In: version spike; module `salem-ui` (`androidTarget`, `iosArm64`, `iosSimulatorArm64`, `desktop` for tests); `DrawScopeCanvas` and `CoreCanvasView`; FontAwesome as a Compose resource; parity tests against the core images; `iosApp` (SwiftUI shell, framework build phase, assets copy); CI; docs.
- Out: moving any screen, strings, widgets, iOS rendering tests on the simulator, `Image.export`/`setPixel`, signing.
- Decisions (user): module `salem-ui`; Canvas over `DrawScope` (no Core Graphics); strings as Compose resources (with the first screen); odd-full; **no emulator or device runs without asking**.

## Exploración
- Core `Canvas`/`Image`/`View` and `AndroidCanvas`/`JavaCanvas` semantics; the brief of the explorer is summarised in the decisions below. `AppleFileOpener.createDefault()` expects `assets` (the content of `salem-core/assets/main`) in the bundle; Android already packs the same folder.
- Angles: the core is counterclockwise, Compose clockwise, so `fillArc` negates both like `AndroidCanvas`. `clearCircle` uses `BlendMode.Clear` and `CoreCanvasView` draws on an offscreen layer.
- Text: the line box is centred on `y` (like `JavaCanvas`, so the core images match); `AndroidCanvas` uses the height of the "m" instead (known difference, `docs/PENDING.md`).

## Incertidumbre
- Spike: Compose Multiplatform 1.10.3 compiles with Kotlin 2.3.20 and AGP 8.9.2 next to the `kotlin.compose` plugin, for Android and both iOS targets; no BOM conflict because `salem-android` does not depend on `salem-ui` yet. 1.11 is not needed.
- ktlint (plugin 11.6.1, ktlint 0.47) fails on the generated resource files; `.editorconfig` globs and `filter {}` did not work, limiting each task source to `src/<source set>` does (verified that a real violation is still reported).
- Parity: the three `RingChart` images and the stroked one differ from the core images by 0.02-0.08 (average luminosity, %), tolerance 0.2.

## Checklist
- [x] TASK-1: spike of versions — criterio: the module compiles for Android and iOS
- [x] TASK-2: `DrawScopeCanvas`, `CoreCanvasView`, FontAwesome resource — criterio: parity tests green on the JVM
- [x] TASK-3: `iosApp` — criterio: `xcodebuild` for the simulator succeeds, the bundle has `assets` and the Compose resources
- [x] TASK-4: CI and docs — criterio: workflow valid, ktlint and the other modules still green

## Estrategia de entrega
`single-pr`, branch `feat/kmp-ui-infra`, base `develop` (the diff is mostly new files; the next PRs move the screens). Commits only when the user asks.

## Revisión (rdd-full)
Fixed after the review: the ktlint filter now fails if the plugin renames its tasks and also covers the format tasks; the Xcode build phase accepts JDK 17 or 21 and fails with a message otherwise; tests for the primitives, text alignment, bold, measuring, `toImage`, and `CoreCanvasView` (the offscreen layer, checked to fail without it). The rest is listed in `docs/PENDING.md`.

## Evidencia
`:salem-ui:desktopTest` (16 tests), `:salem-ui:compileDebugKotlinAndroid`, `:salem-ui:linkDebugFrameworkIosSimulatorArm64`, `xcodebuild` build succeeded. The app was not launched in the simulator.

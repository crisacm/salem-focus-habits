# Shared strings, fonts and theme for `salem-ui` (KMP step 7, PR 2a)

## Objetivo
Prepare `salem-ui` to host the habit list: spike the Compose Multiplatform APIs the list needs, share the app strings and fonts without duplicating them, and move the portable theme.

## Por qué
The list uses ~32 strings, Material 3, icons, haptics, window insets and a back handler. Before moving ~3,000 lines, the open questions (APIs in commonMain, how Compose resources read Android strings) must be answered and the carry-overs of the PR #52 review fixed.

## Alcance
- In: spike; `syncSharedStrings` (strings of all languages + core fonts, adapted); `SalemFonts`; `CoreCanvasView.version`; `SalemTheme`, `toColorScheme`, `resolveTheme` in `salem-ui` (same package `io.github.crisacm.salem.ui.compose`); `salem-android` depends on `salem-ui`; their tests move.
- Out: the list itself (PR 2b/2c); text baseline parity of `DrawScopeCanvas` (needs a device comparison; moved to the first chart on the shared UI, `docs/PENDING.md`); the list colours interface (with `ListRows`, PR 2b).
- Decisions (user): Android stays the source of strings (generated copy in `build/`, nothing committed); state holders move without DI annotations (PR 2b); three chained PRs; odd-full; **no emulator, device or simulator runs without asking**.

## Exploración
Brief of the habit list: 2,951 lines in `activities/habits/list` + 780 in `ui/compose`; 32 string keys (no plurals); 6 FontAwesome glyphs (core `FontAwesome` constants already); `BackHandler`, `AndroidView`, `Build.VERSION`, `rootWindowInsets`, `Typeface`, `DecimalFormat`, `R.attr` colours are the Android-bound spots.

## Incertidumbre (spike, all resolved by compiling or testing)
- commonMain of CMP 1.10.3 has Material 3 (`org.jetbrains.compose.material3:material3:1.9.0-beta03`: `TopAppBar`, `DropdownMenu`, `TextField`, `ColorScheme`), `material-icons-core:1.7.3`, `LocalHapticFeedback`, `WindowInsets.displayCutout/safeDrawing`. `BackHandler` needs the extra artifact `ui-backhandler` (same version as CMP). `testTagsAsResourceId` is Android-only.
- Compose resources read `stringResource` with positional args only (`%1$s`: plain `%d` is left as is), do not unescape `\'`, and do not resolve `@string/` items of a string-array. The sync adapts the first two; `hints` must be read as two strings.
- Locale folders: `values-es-rES`, `pt-rBR`, `zh-rCN` work as they are; `in`/`iw` need `id`/`he` (the JVM and iOS report the new codes); Norwegian is `nb` on iOS and `no` on the JVM, so both are generated. `values-sr-rCS` and `-rSP` match no real locale (as on Android).
- `customDirectory` replaces `composeResources`: the fonts are copied by the same task and the committed `fontawesome.ttf` is gone.
- `salem-android` with `salem-ui` as a dependency resolves Material 3 to androidx 1.4.0 and the runtime to the BOM versions; no conflict, `assembleDebug` passes.

## Checklist
- [x] TASK-1: spike — criterio: compiles for Android, iOS and desktop; the strings test documents the behaviour
- [x] TASK-2: `syncSharedStrings` + `SharedStringsTest` — criterio: English, Spanish, arguments, plurals, apostrophes, legacy codes and Norwegian resolve
- [x] TASK-3: `SalemFonts`, one copy of the fonts, `CoreCanvasView.version` — criterio: iOS framework and app build, tests green
- [x] TASK-4: shared theme and its tests — criterio: `salem-android` unit tests and `assembleDebug` pass
- [x] TASK-5: docs and changelog

## Estrategia de entrega
`single-pr`, branch `feat/ui-strings-base`, base `develop` (first of three chained PRs). Commits only when the user asks.

## Evidencia
`ktlintCheck`, `:salem-ui:desktopTest` (all green), `:salem-android:testDebugUnitTest`, `assembleDebug`, `compileDebugAndroidTestKotlin`, iOS compile and `xcodebuild` (bundle has `compose-resources` with 49 value folders and the three fonts). The app was not launched in the simulator.

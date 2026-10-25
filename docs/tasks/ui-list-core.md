# Habit list core in `salem-ui` (KMP step 7, PR 2b)

## Objetivo
Move the models, state holders, cells, rows, header, empty state and hint of the habit list from `salem-android` to `salem-ui` (commonMain), leaving the screen shell (bar, `ListHabitsContent`, activity) for PR 2c.

## Por qué
The list is the first screen of the shared UI; iOS needs the same rows and state. What does not depend on the Android Views can already be shared.

## Alcance
- In: `CellModel`, `ListRowModel`, `ListBarModel`, `ListEmptyState`, `HeaderModel`, `HeaderState`, `ChartScrollState`, `HabitListState`, `HabitListController`, `ListHabitsMenu`, `ListHabitsSelectionMenu`, `ListCells`, `ListRows`, `HeaderContent`, `HintContent`, `EmptyListContent` (package `io.github.crisacm.salem.ui.list`); abstractions; DI `@Provides` in `salem-android`; tests moved.
- Out: `ListHabitsBar`, `ListHabitsContent`, `ListHabitsHost`, `ListHabitsActivity` and the iOS list (PR 2c); the text baseline of `DrawScopeCanvas`.
- Decisions (user): no DI annotations in `salem-ui`; `LocalCondensedFonts` for the condensed number font (Android provides `sans-serif-condensed`); one `toShortString` (core, HALF_UP); odd-full; **no emulator, device or simulator runs without asking**.

## Exploración
Per-file imports, DI wiring, callers and tests were listed by the explorer. The Android-only spots were `LocalContext`/`getFontAwesome`, `R.string/attr/dimen/color`, `Typeface`, `StyledResources`, `LocalView` + `rootWindowInsets`, `JavaLocalDateFormatter`, `DecimalFormat`, `testTagsAsResourceId` and the kotlin-inject annotations.

## Incertidumbre (resolved)
- Window insets: `windowCenter` only corrects the position the popups use (display cutout left, status bar before Android 16): `rememberWindowInsetOffset()` is an expect/actual (Android real, iOS and desktop zero).
- UiAutomator ids: `exposeTestTags()` expect/actual (Android `testTagsAsResourceId`, no-op elsewhere); tags unchanged.
- `ListColors`: a plain class the platform fills (Android from `StyledResources`, the exact current look).
- `stringResource` in a composition test does not need a display (`ListCellsComposeTest` passes with `java.awt.headless=true`); the header needs `setToday`.
- Hidden problem found: `salem-android/ui/compose/SalemTheme.kt` and the `salem-ui` file had the same JVM facade (`SalemThemeKt`), so `resolveTheme` was unresolved from other Android files; the Android file is now `AndroidSalemTheme.kt`.
- iOS: exposing core types made the framework header export `Entry.Companion.NO`, which clashes with the Objective-C `NO` macro; fixed with `@ObjCName("NO_VALUE")`.

## Checklist
- [x] TASK-1: move pure models, state holders, composables (git mv, new package) — criterio: `salem-ui` compiles for Android, iOS and desktop
- [x] TASK-2: abstractions (fonts, colours, strings, insets, test ids, formatter) — criterio: no `android.*`/`R` in `salem-ui/commonMain`
- [x] TASK-3: `salem-android` wiring (`@Provides`, imports, `ListHabitsHost`/`Content`) — criterio: `assembleDebug`, `compileDebugAndroidTestKotlin`
- [x] TASK-4: tests moved (Mokkery in `salem-ui`, `BaseUiTest`), `ListCellsComposeTest` and `FontAwesomeGlyphsTest` — criterio: `:salem-ui:desktopTest` green, also headless
- [x] TASK-5: docs and changelog

## Estrategia de entrega
`single-pr` (mostly renames), branch `feat/ui-list-core`, base `develop`; 2c follows. Commits only when the user asks.

## Evidencia
ktlintCheck, `:salem-ui:desktopTest` (111 tests, headless), `:salem-core:jvmTest`, `:salem-android:testDebugUnitTest`, `assembleDebug`, `compileDebugAndroidTestKotlin`, iOS compile and `xcodebuild`. Not run: instrumented tests, the app on a device or simulator.

# Shared dialogs and popups (KMP step 7, PR 3a)

## Objetivo
Move the dialogs of the habit editor and the checkmark/number popups to `salem-ui` (Compose Multiplatform) so iOS can show them, with the Android Fragments reduced to hosts.

## Por qué
iOS shows and marks habits only; new habit, edit, delete and the popups are logging stubs. PR 3 (3a-3d) moves the editor and its dialogs to the shared UI and wires iOS. 3a is the base: the dialog surface and the simple dialogs.

## Alcance
- In: `DialogSurface`/`Scrim`/`DialogButton` (same package `ui.compose`), models `FrequencyModel`, `DialogModels`, `PopupModels`, `PopupCard`, and shared content `HabitTypeDialogContent`, `ChoiceDialogContent`, `FrequencyPickerContent`, `ConfirmDeleteDialogContent`, `CheckmarkPopup`, `NumberPopup` (package `ui.dialogs`); Android Fragments (`HabitTypeDialog`, `ChoiceDialog`, `FrequencyPickerDialog`, `ConfirmDeleteDialog`, `CheckmarkDialog`, `NumberDialog`) host them.
- Out (3b-3d): colour, weekday and time pickers, `EditHabitContent`/`EditHabitScreen`, the saver, iOS wiring and overlays.
- Decisions (user): move to `salem-ui` and make Android use it; stacked PRs 3a-3d; TDD where there is logic; no emulator/simulator without asking.

## Exploración
The six dialogs are Fragments extending `ComposeDialogFragment`, not composables: only the bodies of `Content()` move, the Fragments (arguments, callbacks that are not saved, dismiss on restore) stay in Android. `CheckmarkDialog`/`NumberDialog` read `HabitsApplication.component.preferences`, `NumberDialog` reads `Settings.Secure` and `java.text` formats: all become parameters of the shared composables. Glyphs come from `FontAwesome` (core) and `rememberFontAwesome()` instead of `fa_*` strings (not synced).

## Incertidumbre
- Shared strings have positional placeholders (`%1$s`): the frequency rows split with a regex, not `"%d"`.
- `DecimalFormat`/`NumberFormat` replaced by `formatInvariant` plus the decimal separator passed by the host: rounding is HALF_UP (printf) instead of HALF_EVEN, which only differs on exact halves (0.125).
- `NotesSession` (new, tested) keeps the notes and the "answered by a button" flag so Android and iOS save changed notes when the popup is closed any other way.

## Checklist
- [x] TASK-1: move `DialogSurface`, the pure models and `PopupCard` to `salem-ui` — criterio: `:salem-ui:desktopTest`, Android compiles
- [x] TASK-2: `PopupModels` without `java.text` + `NotesSession` (TDD) — criterio: `PopupModelsTest` (16 tests)
- [x] TASK-3: shared content composables + Compose tests — criterio: `DialogContentTest` (6 tests)
- [x] TASK-4: Android Fragments as hosts — criterio: `compileDebugKotlin`, `compileDebugAndroidTestKotlin`, `assembleDebug`
- [x] TASK-5: docs and changelog

## Estrategia de entrega
Stacked PRs 3a to 3d, branch `feat/ui-shared-dialogs` from `develop`. The user merges in order. Commits only when the user asks.

## Evidencia
`:salem-ui:desktopTest`, `:salem-android:testDebugUnitTest`, ktlintCheck (ui and android), `compileDebugAndroidTestKotlin`, iOS compile (arm64 and simulator), `assembleDebug`. Not run: instrumented tests, simulator, emulator; to check by hand on Android: the six dialogs (create menu, frequency, delete, checkmark and number popups, notes saved when closing) and rotation.

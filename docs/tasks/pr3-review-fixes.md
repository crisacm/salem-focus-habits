# Fixes of the review of PR 3a-3d (#58-#62)

## Objetivo
Fix the findings of the rdd-full review of the shared editor, dialogs and iOS wiring that the user accepted, and add the tests the review found missing.

## Por qué
The review (Reliability lens, two independent fresh reviewers, range `e45f71e..5b92829`) found no High issues, two Medium and several Low ones. The user accepted one bounded correction for findings 1, 2, 4 and 5 and the missing tests of finding 3.

## Alcance
- In: the number field accepts only ASCII digits and one decimal mark; changed notes are compared without surrounding spaces on both sides; `ListOverlays.dismiss(overlay)` closes only the overlay it was given and `Editor`, `Delete` and `Color` are objects (fresh state each time); the habit list is hidden from accessibility while an overlay is open (`Modifier.hiddenWhileOverlay`, used by the iOS root); 8 new tests.
- Out: the time format and decimal mark captured once, and the saving flags after a throwing command (recorded in `docs/PENDING.md`); showing `Message`s; any run on the simulator.
- Decisions (user): apply that correction in one pass; no new review loop.

## Exploración
The reviewers cited the exact places, so no new exploration was needed: `PopupModels.kt`, `ListOverlays.kt`, `ListOverlayHost.kt`, `IosListRoot.kt` and `ListOverlayHostTest.kt`.

## Incertidumbre
- The new tests run on the desktop JVM with the navigation event dispatcher of the tests: they show that the picker of the editor closes before the editor and that a tap outside a popup saves the changed notes once, but not the real iOS back gesture or VoiceOver.
- A stale `onSave` after another overlay replaced the editor is covered at the unit level (`aStaleDismissDoesNotCloseANewerOverlay`), not through the host.
- Finding 7 of the review (small stored values shown as "0") mirrors the old Android behaviour and is not changed.

## Checklist
- [x] TASK-1: `PopupModelsTest` (3 new: non-ASCII digits, one separator, notes with spaces) — criterio: RED before the fix
- [x] TASK-2: `ListOverlayHostTest` (8 new: stale dismiss, hidden list, back with a picker open, number popup back, repeated save, scrim taps) — criterio: RED before the fix
- [x] TASK-3: fixes in `PopupModels`, `ListOverlays`, `ListOverlayHost`, `IosListRoot`
- [x] TASK-4: docs, changelog and the receipt in Engram

## Estrategia de entrega
`single-pr`, branch `fix/pr3-review-findings` from `develop`. Commits only when the user asks.

## Evidencia
`:salem-core:jvmTest`, `:salem-ui:desktopTest` (22 overlay host tests, 19 popup model tests), `:salem-android:testDebugUnitTest`, `ktlintCheck`, `compileDebugAndroidTestKotlin`, `assembleDebug`, iOS compile (arm64 and simulator) and `xcodebuild` (build only). Not run: the simulator, instrumented tests, VoiceOver or TalkBack, the real back gesture.

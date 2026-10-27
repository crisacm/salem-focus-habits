# iOS SwiftUI

## Objetivo
La app iOS dibuja con SwiftUI. La lógica sigue en `salem-core`, expuesta por el framework `SalemKit`. Android sigue en Compose (`salem-ui`).

## Por qué
Las interfaces van con el ecosistema de cada plataforma. Un proceso de iOS solo puede cargar un runtime de Kotlin, así que la app no puede enlazar Compose y el framework de la sesión a la vez.

## Alcance
- Módulo `salem-kit` y esquema temporal `SalemSwift`.
- `SalemSession`: lista, editor, detalle, ajustes, importación y exportación.
- `RecordingCanvas` para pintar los gráficos del core en SwiftUI.
- Corte: `iosApp` enlaza `SalemKit` y `salem-ui` deja de tener targets iOS.

## Exploración
`IosHabitsGraph` arma a mano la base, los comandos, los recordatorios y el estado Compose. Los widgets ya consumen `SalemExtras` desde Swift y no pueden compartir proceso con otro framework de Kotlin. Los gráficos dibujan contra `Canvas`; el estado de las tarjetas ya es dato plano en el core.

## Checklist
- [x] TASK-1: Rama, módulo `salem-kit` y esquema temporal `SalemSwift` (borrado en el corte).
- [x] TASK-2: `SalemSession` con tests de marcar, crear, editar, archivar y preferencias.
- [x] TASK-3: Lista SwiftUI.
- [x] TASK-4: Editor y diálogos.
- [x] TASK-5: `RecordingCanvas` y detalle.
- [x] TASK-6: Ajustes, about, intro, import/export y catálogo de cadenas.
- [x] TASK-7: `iosApp` enlaza `SalemKit`, se quitan los targets iOS de `salem-ui`, CI y docs.

## Evidencia
- 2026-10-27: `:salem-kit:jvmTest --offline` pass (phase-1 acceptance).
- 2026-10-27: `:salem-kit:jvmTest --offline` and `:salem-kit:ktlintCheck` pass (phase-2 acceptance).
- `:salem-kit:jvmTest` y `RecordingCanvasTest` pasan.
- `:salem-kit:compileKotlinIosSimulatorArm64` pasa.
- `xcodebuild` del esquema `iosApp` (SwiftUI, `SalemKit`) pasa para el simulador.
- No se abrió la app en el simulador.
- 2026-10-27: `xcodebuild` scheme `iosApp` Debug iphonesimulator CODE_SIGNING_ALLOWED=NO ** BUILD SUCCEEDED ** (TASK-3 list hint + selection).
- 2026-10-27: `xcodebuild` scheme `iosApp` Debug iphonesimulator CODE_SIGNING_ALLOWED=NO ** BUILD SUCCEEDED ** (TASK-4 editor + SwiftUI dialogs).
- 2026-10-27: `:salem-core:jvmTest --tests "*RecordingCanvasTest*"` + `ktlintCheck --offline` pass; `xcodebuild` iosApp ** BUILD SUCCEEDED ** (TASK-5 audit, no code changes).
- 2026-10-27: `:salem-kit:syncIosStrings` + `jvmTest` + `ktlintCheck --offline` pass; `xcodebuild` iosApp ** BUILD SUCCEEDED ** (TASK-6 plural flatten in `syncIosStrings`).
- 2026-10-27: `:salem-android:assembleDebug` + `:salem-core:jvmTest` + `:salem-kit:jvmTest --offline` pass; `xcodebuild` iosApp ** BUILD SUCCEEDED ** (TASK-7 cutover).

# iOS reminders and notifications (KMP step 4f)

## Objetivo
Make reminders, notifications and the midnight refresh work on iOS: the scheduling logic is shared in `commonMain`, and `appleMain` provides `UNUserNotificationCenter` implementations.

## Por qué
Step 4f of `.specs/migration-kmp.md`. `ReminderScheduler`, `MidnightTimer` and `DateUtils` live in `jvmAndroidMain` and use `java.util`, so iOS cannot compile them. iOS has no exact alarms and no background timers, and limits pending local notifications to 64.

## Alcance
- In: common time helpers, `ReminderScheduler`, `MidnightTimer` and `DateUtils` in `commonMain`; iOS `SystemScheduler`, `SystemTray`, Yes/No actions, permission wrapper, foreground refresh; tests; docs.
- Out: snooze action (Android already removes it on API 31+), widgets (`scheduleWidgetUpdate` returns `null`), the app that wires the callbacks (step 7).
- Decisions: odd-full, TDD active, several PRs stacked on `feat/ios-zip` (#18): 4f-1 move to common, 4f-2 iOS `SystemScheduler`, 4f-3 iOS `SystemTray` and actions, 4f-4 foreground refresh. 4g (closing docs) is separate.

## Exploración
- `ReminderScheduler` (`SystemScheduler`, `SchedulerResult`) and `MidnightTimer` (`MidnightListener`) are in `jvmAndroidMain`; `NotificationTray` and `SystemTray` are already in `commonMain`.
- `DateUtils` is a `java.util` object (`TimeZone`, `Calendar`, `Date`); non-test callers outside core use only default arguments (`AutoBackup`, `ReminderController`, `WidgetUpdater`).
- The weekday mask is checked when the notification is shown (`ShowNotificationTask`), not when it is scheduled.
- Tests today: `ReminderSchedulerTest`, `MidnightTimerTest`, `DateUtilsTest` in `jvmTest`; none of them runs on iOS.

## Incertidumbre
- 4f-1: a common time-zone type is needed because the tests use real zones (Sydney DST). Decision: `ZoneOffsets` interface (`offsetAt(utcMillis)`) with `expect` providers for the system zone and for a zone by id; the existing `java.util.TimeZone` setter stays as a JVM/Android extension.
- 4f-1: `MidnightTimer` moves from a scheduled executor to a coroutine loop. Risk for Android timing; covered by virtual-time tests and Android unit tests.
- 4f-2: whether `UNUserNotificationCenter` works inside the simulator test host (spike first; otherwise the center sits behind an interface with a fake) and how to handle the weekday mask (schedule the next enabled day).

## Checklist
- [ ] TASK-1: task doc and Engram mirror — criterio: this file committed
- [ ] TASK-2 (4f-1): common `DateUtils` over `ZoneOffsets`, with common tests — criterio: JVM and iOS tests green, existing JVM `DateUtilsTest` unchanged
- [ ] TASK-3 (4f-1): `ReminderScheduler` in `commonMain` with its test in `commonTest` — criterio: green on JVM and iOS
- [ ] TASK-4 (4f-1): `MidnightTimer` in `commonMain` with virtual-time test — criterio: green on JVM and iOS, Android compiles and unit tests pass
- [ ] TASK-5 (4f-2): iOS `SystemScheduler`, 64 limit, permission wrapper
- [ ] TASK-6 (4f-3): iOS `SystemTray` and Yes/No action handler
- [ ] TASK-7 (4f-4): foreground refresh

## Estrategia de entrega
`multi-pr`, base `feat/ios-zip` until the parents merge, then retarget to `develop`.

## Evidencia
(pending)

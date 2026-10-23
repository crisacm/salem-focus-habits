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
- [x] TASK-1: task doc and Engram mirror — criterio: this file committed
- [x] TASK-2 (4f-1): common `DateUtils` over `ZoneOffsets`, with common tests — criterio: JVM and iOS tests green, existing JVM `DateUtilsTest` unchanged
- [x] TASK-3 (4f-1): `ReminderScheduler` in `commonMain` with its test in `commonTest` — criterio: green on JVM and iOS
- [x] TASK-4 (4f-1): `MidnightTimer` in `commonMain` with virtual-time test — criterio: green on JVM and iOS, Android compiles and unit tests pass
- [x] TASK-5 (4f-2): iOS `SystemScheduler`, 64 limit, permission wrapper — criterio: appleTest green, device target compiles
- [ ] TASK-6 (4f-3): iOS `SystemTray` and Yes/No action handler
- [ ] TASK-7 (4f-4): foreground refresh. Requirements from the #20 review: call `AppleSystemScheduler.clear()` at launch before `scheduleAll()` (the count of pending requests is in memory and is lost on restart, so iOS could hold more than 64); decide who calls `clear()` and whether `AppleSystemScheduler` needs its own `@Synchronized` (`ReminderScheduler` methods are synchronized, `clear()` is not)

## Estrategia de entrega
`multi-pr`, base `feat/ios-zip` until the parents merge, then retarget to `develop`.

## Evidencia
- 4f-1, each test written before the code and seen failing (compile error): `DateUtilsCommonTest` (8, incl. Sydney daylight saving), `ReminderSchedulerTest` moved to `commonTest` (6, runs on JVM and iOS), `MidnightTimerTest` rewritten with virtual time in `commonTest` (5: first notification, daily repeat, pause, double resume, removed listener). Existing JVM `DateUtilsTest` (13) unchanged and green.
- Android: `assembleDebug` (kotlin-inject regenerated; `MidnightTimer` uses the default dispatcher), `testDebugUnitTest`, `compileDebugAndroidTestKotlin`, `ktlintCheck` pass. iOS suite 358 tests, 0 failures; `jvmTest` 307, 0 failures; `compileKotlinIosArm64` passes.
- Behaviour notes: `MidnightTimer.onPause()` no longer returns the pending runnables; `getStartOfDay` uses `floorDiv` (same result for dates after 1970); listeners are called from a snapshot of the list.
- #19 review fixes: a throwing listener no longer stops the other listeners or the next days; the delay to the next notification is recomputed every lap (a 23 hour day in Sydney is followed by a notification 23 hours later). Tests written first (two failed): `MidnightTimerTest` now 7 cases; iOS suite 361, `jvmTest` 310, 0 failures.
- 4f-2 spike (Engram `spike/ios-notifications`): `platform.UserNotifications` compiles on simulator and device targets with no Gradle change and requests/triggers can be built and inspected in tests, but `UNUserNotificationCenter.currentNotificationCenter()` aborts the process in the test host (no app bundle). So the center is behind `PendingNotificationCenter` with a fake; `UNPendingNotificationCenter` and `NotificationPermission` are compiled but only the app (step 7) can run them.
- 4f-2 tests written first (compile error): `ReminderPlanningTest` (9: next enabled weekday, week wrap, local day vs UTC day, Sydney daylight saving, 64-limit selection and ties), `AppleSystemSchedulerTest` (11: content, replace per habit, past time, no id/reminder, weekday shift, limit with the earliest kept, clear, no widget updates), `NotificationRequestsTest` (2, real UN classes: trigger fires at the given local time, userInfo). iOS suite 383 tests, 0 failures (see below), `compileKotlinIosArm64` and `ktlintCheck` pass.
- Decisions 4f-2: weekday mask is applied when scheduling (iOS cannot run code at fire time); `AppleSystemScheduler.clear()` must be called before `scheduleAll()` so reminders of habits that lost theirs are removed (wired in 4f-4); calendar triggers use the device zone at fire time.
- #20 review fix: the notification trigger now keeps the seconds, so a snoozed time inside the current minute still fires (test written first, failed before the fix).
- Not verified: Android timing on a device (the coroutine loop replaces `scheduleAtFixedRate`), CI.

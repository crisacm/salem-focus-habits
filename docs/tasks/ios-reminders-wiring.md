# Reminders and notifications wired into the iOS app (KMP step 7, PR 6a)

## Objetivo
Make the iOS app schedule the reminders of the habits, clear them when a habit changes, take the Yes/No buttons and the tap of a notification, and schedule them again when the app returns from the background.

## Por qué
All the pieces were built in step 4f (`docs/tasks/ios-reminders.md`) but nothing created or started them: `IosHabitsGraph` built a `NotificationTray` with an empty system tray that nobody listened to, and `IosListRoot` only refreshed the timer and the list when the app came back. Without this iOS never reminds. First of two sequential PRs, each straight to `develop`: 6a the wiring, 6b the permission request and the notification row of Settings.

## Alcance
- In: `IosReminders` (the scheduler, the tray, `AppleReminderSync`, `HabitCompletionRearmer`, `AppForegroundRefresher`, the Yes/No category, `ReminderActions`), `ReminderActions` in `salem-ui` commonMain (open, check, uncheck; ignores deleted and archived habits), `IosNotificationRouter` (the delegate of the notification center, set when the root view is created, with a queue until the app is ready), the lifecycle of `IosListRoot` through `AppForegroundRefresher`.
- Out (6b): the permission request and the notification row of Settings. Until 6b nothing asks the user, so no notification is shown on a fresh install.
- Decisions (user): odd-full, 2 sequential PRs, TDD where there is logic, no emulator/simulator without asking, the permission is asked when a habit with a reminder is saved and at launch if there are reminders (6b).

## Exploración
Haiku `explorer` brief: nothing of step 4f was built on iOS; `@Synchronized` does nothing on iOS; the listeners of the commands run on the main thread (the `CoroutineTaskRunner` reports finished tasks on the main dispatcher), so the classes that are not synchronized are used from one thread; the notification center calls its delegate from any thread; `iosApp` is a SwiftUI shell without an app delegate; local notifications need no `Info.plist` key.

## Incertidumbre
- A tap on a notification that launches the app: the delegate is set in `MainViewController()` (as early as the shared code can) and the actions wait in a queue until the graph exists. Whether iOS delivers a cold-start response to a delegate set from there is not checked on a device.
- `AppleSystemScheduler`/`AppleSystemTray` assume one thread. The wiring only touches them from the main thread (listeners, lifecycle, the router). `IosReminders.create` runs on a background thread but only builds and registers.
- Nothing calls `NotificationTray.show()` on iOS: the notifications are the scheduled requests. The tray only removes delivered notifications of a habit that was checked or deleted.

## Checklist
- [x] TASK-1: `ReminderActionsTest` (5: open, check on the day of the notification, uncheck, missing habit, archived habit) — RED observed (unresolved reference), then `ReminderActions`
- [x] TASK-2: `IosNotificationRouter` and `IosReminders`, `IosHabitsGraph` builds them
- [x] TASK-3: `IosListRoot` uses `AppForegroundRefresher` for launch and for the foreground and background
- [x] TASK-4: docs and changelog

## Estrategia de entrega
Sequential PRs, each from `develop` straight back to `develop` (no stacking). 6b starts after 6a is merged and checked with `gh pr view`.

## Evidencia
See the verification run in the PR. Not run: the simulator, any device, the iOS test suite. To check by hand (needs 6b to get the permission): a habit with a reminder gets a notification at its time; the Yes and No buttons check and uncheck the habit; a tap opens its detail; checking the habit in the app removes the delivered notification; the reminders come back after the app returns from the background or after midnight; a deleted or archived habit stops notifying.

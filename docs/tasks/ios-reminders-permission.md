# The notification permission and the notification row of Settings on iOS (KMP step 7, PR 6b)

## Objetivo
Ask for the permission to show notifications when the user needs it, and give iOS Settings a row that opens the notification settings of the app.

## Por qué
PR 6a wired the reminders (`docs/tasks/ios-reminders-wiring.md`) but nothing asked the user, so a fresh install showed no notification. Second of two sequential PRs, each straight to `develop`.

## Alcance
- In: `ReminderPermissionPrompter` in `salem-ui` commonMain (asks after a habit with a reminder is created or edited, and at launch when there are reminders; a failing request is ignored), its wiring in `IosReminders` (the request is `NotificationPermission().request()`, the scope is the main dispatcher) and in `IosListRoot` (the launch), the Settings section "Reminder" with "Customize notifications", which opens the notification settings of the app (`UIApplicationOpenNotificationSettingsURLString`, iOS 16, the deployment target).
- Out: a switch for sticky notifications (iOS cannot make a delivered notification non-dismissible), animations (no confetti on iOS yet).
- Decisions (user): odd-full, TDD, the permission is asked when a habit with a reminder is saved and at launch if there are reminders, the Settings row opens the system settings, no emulator/simulator without asking.

## Exploración
The Haiku `explorer` brief of 6a: Android asks in `ListHabitsActivity.onResume` when `hasHabitsWithReminders()`; the editor saves through `CreateHabitCommand`/`EditHabitCommand`, which carry the habit with its reminder; `NotificationPermission.request()` returns whether notifications are allowed and the system shows its question only the first time.

## Incertidumbre
- Asking at launch and after every save relies on iOS showing its question once: later calls return the stored answer. Not seen on a device.
- The row of Settings opens the settings of the app; where exactly iOS lands (the notification page or the page of the app) is not checked.

## Checklist
- [x] TASK-1: `ReminderPermissionPrompterTest` (7: create and edit with and without reminder, other commands, launch with and without reminders, a failing request) — RED observed (unresolved reference), then the class
- [x] TASK-2: wiring in `IosReminders`, `IosListRoot` and the Settings row
- [x] TASK-3: docs and changelog

## Estrategia de entrega
Sequential PRs, each from `develop` straight back to `develop`. 6a (#74) is merged.

## Evidencia
See the verification run in the PR. Not run: the simulator, any device. To check by hand: saving a habit with a reminder shows the system question once; allowing it makes the notification arrive at the time of the reminder; denying it and opening Settings, Customize notifications leads to the settings of the app; opening the app with reminders already set asks only if the user was not asked.

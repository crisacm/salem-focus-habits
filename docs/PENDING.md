# Pending tasks

Maintainer checklist of things that were left as placeholders or TODOs during
the fork of Loop Habit Tracker. Check items off as they are completed.

## Project contact

- [ ] Create a project email address and use it in:
  - `salem-android/src/main/play/contact-email.txt` (currently the placeholder `TODO-contact-email@example.invalid`)
  - `bugReportTo` in `salem-android/src/main/res/values/constants.xml` (currently empty, so the user chooses the recipient)
  - optionally `SECURITY.md`
- [ ] If the project gets a website, set it in `salem-android/src/main/play/contact-website.txt` (currently the GitHub repository)

## Privacy policy

- [ ] Write Salem's own privacy policy (Google Play and the App Store require one)
- [ ] Point `privacyPolicyURL` in `constants.xml` to it (currently the README)

## GitHub repository settings (manual)

- [ ] Enable Discussions, with categories named `ideas` and `q-a` (used by `.github/ISSUE_TEMPLATE/config.yml`)
- [ ] Enable Private vulnerability reporting (used by `SECURITY.md`)
- [ ] Optionally set the repository description and topics

## Store assets and screenshots

- [ ] Salem icon, feature graphic and phone screenshots in `salem-android/src/main/play/listings/en-US/graphics/` (currently empty)
- [ ] Screenshots for the README (the section is commented out) and the `screenshots/` folder (currently empty)

## App identity

- [ ] Replace the launcher icon and branding (still Loop's)
- [ ] Set `playStoreURL` to `market://details?id=io.github.crisacm.salem` once the app is published
- [ ] Prepare the App Store listing and metadata for iOS

## Localization

- [ ] Only the `en-US` and `es-rES` Play listings remain; add more languages if needed
- [ ] Translated strings such as `intro_description_1` still mention Loop
- [ ] Set up a translation platform, then update `docs/GUIDELINES.md` and `translateURL`

## Code

- [ ] Decide what to do with the `org.isoron.uhabits.ACTION_*` intent actions and the `content://org.isoron.uhabits/...` URI (kept for Tasker compatibility)
- [ ] Sync server: the default URL is empty; set up a server or remove the setting
- [ ] Review `versionCode` and `versionName`
- [ ] `build.sh` still prints Loop branding (`loop-$VERSION-*.apk`) and removes legacy directories that no longer exist (`uhabits-android/uhabits-android`, `uhabits-core-legacy`); clean it up
- [ ] Continue the Compose migration slice by slice (step 6): the habit list is being migrated in five PRs (empty state and hint, the top bar with its menus and selection bar, and the day header are done; the rows with the Compose cells, selection and drag; activity), then the habit detail (9 cards), the history editor with the charts, and the rest of the open points below
- [ ] `salem-core` has four drawing classes with no users and orphaned test images (`ui/views/CheckmarkButton.kt`, `NumberButton.kt`, `Ring.kt`, `HabitListHeader.kt` and `assets/test/views/{CheckmarkButton,NumberButton,HabitListHeader}`); keep them for the iOS drawing work or delete them
- [ ] The day header no longer scrolls with a `Scroller`: the fling uses Compose decay and may feel different; the Compose cells (`ListCells.kt`) are not wired yet (next PR) and their look has to be compared with the Views on a device
- [ ] Habit list top bar: the search text and the search mode are lost on rotation or when the theme is toggled (as before); back does not close the search field; the sort submenu is an inline list in the filter menu
- [ ] Replace the vendored `TimePickerDialog` (reminder time in the edit screen and custom snooze) with the Material 3 time picker and remove `com/android/datetimepicker`
- [ ] Compose dialogs: the scrim and the card are `clickable` without semantics, so TalkBack and UiAutomator see unlabeled button nodes (`ui/compose/DialogSurface.kt`); use `pointerInput` or `clearAndSetSemantics`, and check the dialogs with TalkBack
- [ ] `SnoozeDelayPickerActivity` is `singleInstance` but ignores `onNewIntent`, so a second snooze notification tapped while it is open shows the previous habit; it also ignores the pure black theme (only light or dark)
- [ ] Since Android 12 `ReminderReceiver` ignores the snooze action (`SDK_INT < S` only) while the notification still shows it; decide whether to hide the action or support snooze there. The vendored `TimePickerDialog` (reminder time and custom snooze) is still on Views
- [ ] After the Settings migration: `res/xml/preferences.xml` now only holds default values for `SharedPreferencesStorage`; move them to code and drop `legacy-preference-v14`; `RingtoneManager` and `pref_ringtone_uri` have no UI; the first weekday and pure black changes still apply only after restarting or returning to the list; preference keys are duplicated in several classes
- [ ] Compose BOM is pinned to 2026.02.00 because newer ones need AGP 9.1 and compileSdk 37; revisit when AGP is upgraded
- [ ] Implement the iOS `Canvas`/`Image` actuals or replace the chart rendering with Compose (steps 6-7). Of the shared tests, 439 (shared plus iOS-specific) run on the iOS simulator; the 13 canvas and chart tests (`CanvasTest`, `BarChartTest`, `HistoryChartTest`) and 4 `Version22Test` cases (sqliter does not report the SQLite error message) stay in `salem-core/ios-blocked-tests.txt`. iOS `getFirstWeekdayNumberAccordingToLocale`, `computeToday`, `format`, `formatInvariant`, files, `LocalDateFormatter`, zip and reminders/notifications are implemented
- [ ] Check on a device or simulator app (step 7): local notifications (delivery over several days, the 64 limit, Yes/No buttons, foreground banners, permission prompt), `AppForegroundRefresher` with `AppLifecycleObserver`, and that a shown notification replaces nothing pending. The classes that need an app bundle are compiled but not exercised by tests; wiring checklist in `docs/tasks/ios-reminders.md`
- [ ] iOS reminders stop after about 7 days without opening the app (they are scheduled 7 days ahead, and fewer days with more than about 9 reminder habits because of the 64 limit); consider a background refresh task at step 7
- [ ] Merge `develop` into `main` (the default branch) when ready, so `workflow_dispatch` (manual CI runs) becomes available
- [ ] Check on an Android emulator that Java's HALF_UP rounding of ties matches the iOS `format` (finding 1 of the 4b review)
- [ ] iOS database: opening a file that is not a SQLite database fails with a cryptic sqliter error (`error while compiling: PRAGMA journal_mode`); wrap it with a clear message. `peekUserVersion` reads the file header, which can be stale for a database in WAL mode whose latest `user_version` is only in the `-wal` file (the app database uses a single journal file, importers open copies)
- [ ] Upgrade AGP to 8.10 or later and migrate the core Android target from `androidTarget` to the `com.android.kotlin.multiplatform.library` plugin (the upgrade also silences the D8 Kotlin metadata warnings)

## Documentation

- [ ] Add iOS and KMP instructions to `docs/BUILD.md` and `docs/TEST.md`
- [ ] Add a `CODE_OF_CONDUCT.md`
- [ ] Add Salem's own features to the Features section of the README (the current list is inherited from Loop and marked with `*`)
- [ ] Keep the Salem section of `CHANGELOG.md` up to date

## Release and CI

- [x] Add CI workflow (`.github/workflows/ci.yml`: assemble, core JVM tests, Android unit tests and ktlint on every pull request, on pushes to `develop` and on manual runs)
- [ ] Confirm the first GitHub Actions run of `ci.yml` is green
- [ ] `WidgetPreferences` keys (`widget-%06d-habit`, `snooze-%06d`) still use the locale-sensitive `format`; on devices whose locale uses non-Latin digits (for example Arabic or Persian) the stored keys contain those digits. Changing them to `formatInvariant` would orphan existing widget settings on those devices, so it needs a data migration first
- [ ] Set up signing and publishing for Google Play and the App Store
- [ ] Edit habit screen, from the deep review: the status bar icons may not contrast with a saturated habit colour (the bar relies on the window being edge-to-edge); the colour row and the picker rows have no role or description for TalkBack; unit and target are stacked instead of side by side; the text fields have no IME action; the validation errors are not kept across a rotation; the name limit (`take(50)`) can cut an emoji; `lint-baseline.xml` still lists the deleted `activity_edit_habit.xml`; a failed save is invisible because the screen closes right after running the command
- [ ] Habit list bar, from the deep review: the keyboard is not hidden when the search closes and may come back after a selection interrupts a search; the checkable items of the filter and overflow menus do not announce their state to TalkBack (`stateDescription`); the dropdown colours come from the Compose theme instead of the old popup style; "More options" and "Close" are not translated; `ListHabitsMenu` and `ListHabitsSelectionMenu` have no unit tests of their own; the selection count is not refreshed if a model change removes a selected habit (as before)
- [ ] Day header, from the deep review: a touch that does not move does not stop a running fling (as before); `draggable` starts after the touch slop, unlike the old gesture detector; the Material `Text` has font padding, so the baselines of the two lines differ from the old canvas drawing; the columns are not clipped if there were ever more of them than fit; the header shadow may overlap the progress bar (negative margin); no test covers the restore path in `ListHabitsRootView` itself

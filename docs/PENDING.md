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
- [ ] Continue the Compose migration slice by slice (step 6): the habit list is being migrated in five PRs (empty state and hint, the top bar with its menus and selection bar, the day header, the rows with selection and drag, and the activity with `ListHabitsContent` are done), then the habit detail (9 cards), the history editor with the charts, and the rest of the open points below
- [ ] Habit detail screen in Compose (step 6h): done (the nine cards, their container `ShowHabitContent`, the bar `ShowHabitBar`, `ShowHabitHost` and `ShowHabitScreen`; `ShowHabitActivity` uses `setContent`). Left: its dialogs are still View fragments (`HistoryEditorDialog`, `NumberDialog`, `CheckmarkDialog`, `ConfirmDeleteDialog`) opened by the activity, the activity builds its presenters by hand (no DI component), and it installs no uncaught exception handler as the list does. `CoreChart` and `CoreView` draw the `salem-core` charts through `AndroidCanvas` on the native canvas; a `Canvas` over `DrawScope` is still needed for iOS (step 7). The chart Views that the widgets use (`ScoreChart`, `FrequencyChart`, `StreakChart`, `TargetChart`, `RingView`) stay until the widgets move
- [ ] Score and frequency charts are now in `salem-core` (`ScoreChart`, `FrequencyChart`) and the old Views `ScoreChart` and `FrequencyChart` (with `ScrollableChart`) are kept only for `ScoreWidget` and `FrequencyWidget`: when the widgets move, delete the Views and their image tests. The widget needs the transparent marker of the score chart (a hole punched with a clear blend mode), which the multiplatform `Canvas` cannot express yet; `isBackgroundTransparent` of the old `FrequencyChart` is dead code. The core charts take the text position from the middle of the lower case letters (`AndroidCanvas`) while the old Views used the baseline, so text may sit slightly differently; their reference images come from `JavaCanvas`, whose font metrics differ from Android's (the chart is a little shorter in the images)
- [ ] Streak and target charts are now in `salem-core` (`StreakChart`, `TargetChart`, with `formatDate` and `formatValue` passed in because `longFormat` and `toShortString` still live in the Android and JVM code; the iOS formatter needs `longFormat` and a short number format in step 7). The old Views `StreakChart`, `TargetChart` and `RingView` stay for `StreakWidget`, `TargetWidget` and the checkmark widget (which needs the transparent ring: a hole punched with a clear blend mode); delete them with their tests when the widgets move. `Ring.kt` in `salem-core` has no user and no test (its `percentage` is clamped to 0..360 before multiplying by 360), and `assets/test/views/Ring/draw1.png` is orphaned: delete both or fix it when the widgets are done. `Theme.highContrastReverseTextColor` is the `contrast0` of the Android themes
- [ ] `salem-core` has four drawing classes with no users and orphaned test images (`ui/views/CheckmarkButton.kt`, `NumberButton.kt`, `Ring.kt`, `HabitListHeader.kt` and `assets/test/views/{CheckmarkButton,NumberButton,HabitListHeader}`); keep them for the iOS drawing work or delete them
- [ ] The day header no longer scrolls with a `Scroller`: the fling uses Compose decay and may feel different; the look of the Compose cells and rows has to be compared with the old Views on a device (an emulator screenshot shows them drawn correctly)
- [ ] Habit list rows (`ListRows.kt`, `HabitListController.kt`), not checked on a device: the list does not scroll by itself when a row is dragged near an edge (`ItemTouchHelper` did); the rows that make way do not animate (`animateItem`); a toggled cell has no ripple; the drop target uses the height of the dragged row, so rows of different height (two-line names) may land one place off; a drag is saved once, when it ends; the row gesture relies on the cells consuming their own touches; the bottom padding uses `WindowInsets.systemBars` inside a nested `ComposeView`; the confetti origin is the centre of the cell in window coordinates minus the cutout and, before Android 16, the status bar; `HabitListState` rebuilds all the rows each time the cache changes (O(rows), and a refresh reports once per habit, so O(rows²)) and `HabitRowData` is not stable, so every row recomposes on each step: rebuild only on `onRefreshFinished` or batch the changes; `drop` no longer cancels the selection when an index is invalid; at midnight the header moves to the new day before the cache arrays do, so the cells show a day shifted for a moment; a selected habit that a refresh or a filter removes stays in the selection (`selectedHabits` is kept apart from the rows) and the bar counts it; the row gesture keeps the `Habit` instance of the first composition (the key is its id), which is fine while the cache reuses its instances; the drag cancels the selection at the first crossing and, when the dragged row is not visible, its height is 0 and the drag does nothing; `ListColors` is not remembered and `HabitListController.onModelChange` calls `onSelectionFinish` on every change without a selection (harmless); there are no tests of a drag together with a refresh at the screen level or of a selection after a refresh that removes its habit
- [ ] Instrumented tests of the list on a Pixel_5 emulator (Android 11; `HabitsTest`, `ListHabitsRegressionTest`, `ListCellsComposeTest`, 16 tests) are flaky, a different set fails on each run. On the branch 4 to 7 failed per run; the baseline (`a8350e9` plus the dialog fix `1d2d969`) failed 0, 2 and 1 in three runs, so the "Take a walk" not found after editing and the list not found after saving already happened before the migration. New with the migration: UiAutomator keeps seeing the normal bar ("Habits") while the screen shows the selection bar ("Selection count 2 not shown", `selectionOverflow` and `action_edit_habit` not found); the app state and the screenshots are right, so it is a stale accessibility tree of the bar `ComposeView`. Tried without success: notifying accessibility when the selection changes (`TYPE_WINDOW_CONTENT_CHANGED` and `liveRegion`), waiting 5 s for the tag instead of falling back to Espresso. Not tried: dumping the UiAutomator tree from inside the test at the moment of a failure, polling with `waitForWindowUpdate`, moving the bar into the `ComposeView` of the rows. The Pixel_10 (Android 17) cannot run them (see below)
- [ ] Espresso 3.6.1 fails on Android 17 emulators (`InputManager.getInstance` no longer exists): bump Espresso and the other `androidx.test` libraries before using those images. `connectedDebugAndroidTest` needs the network the first time (it downloads the UTP plugin)
- [ ] `lint-baseline.xml` still has entries for the removed list Views (`HabitCardListAdapter`, `CheckmarkButtonView`, `CheckmarkPanelView`, `HabitCardListView` and others); regenerate it, and check for resources only those Views used (the `scrollableRecyclerViewStyle` style, `selected_box`, `ripple`)
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
- [ ] Day header, from the deep review: a touch that does not move does not stop a running fling (as before); `draggable` starts after the touch slop, unlike the old gesture detector; the Material `Text` has font padding, so the baselines of the two lines differ from the old canvas drawing; the columns are not clipped if there were ever more of them than fit; the header shadow may overlap the progress bar (negative margin); no test covers the restore path in `ListHabitsHost` itself
- [ ] Habit list screen, from the deep review of the activity migration (`ListHabitsHost.kt`, `ListHabitsContent.kt`, `ListHabitsActivity.kt`): the header and the rows draw one frame with zero day columns until `onSizeChanged` writes the real count (seed it from `displayMetrics`); no test covers a resize after a restored day offset, the attach and detach pairing of `ListHabitsHost`, or the Settings and file picker result flow; `currentTheme!!` is dead code (the property is never null) and the host must be built after `themeSwitcher.apply()`; `windowCenter` in `ListRows.kt` may place the confetti origin one status bar height too high on Android 15 (it was like that before); `TaskProgressBar` is created outside the composition and hosted with `AndroidView` (no leak seen); `lint-baseline.xml` still has three entries for `ListHabitsRootView`

- [ ] Habit detail container, not checked on a device: the style of the old `Card` (colour, elevation 1 dp, 1 dp between cards, padding 16 and 4 dp) and of the old subtitle (60 dp start padding, 2 dp elevation) was copied by hand into `ShowHabitContent`; the bottom spacer follows the navigation bar and the keyboard; the charts that drag horizontally sit inside a column that scrolls vertically. `about_translators.xml` still uses the `Card` and `CardHeader` styles
- [ ] Habit detail top bar, not checked on a device: the colour of the bar and of the status bar (habit colour or primary, Android 15 and earlier), the way back (`onSupportNavigateUp`, parent activity from the manifest), Edit and the menu, Randomize for developers (new string `randomize`, English only), the menu after archiving and unarchiving. `setupToolbar`, `applyToolbarInsets` and `applyRootViewInsets` stay for `EditSettingActivity`
# Changelog

All notable changes to Salem are documented in this file, starting with the
section below. Everything after the Salem sections is the history of the
original [Loop Habit Tracker](https://github.com/iSoron/uhabits), kept here for
attribution.

## [Unreleased]

### Changed

- Android: the notes sheet of a yes or no habit (opened from the list and from the detail) is a bottom sheet with the badge, name and day of the habit, a large notes field and a round yes and no button (Skip and Unknown stay below them when they are on in Settings), and Settings has the new design: a round back button and a large title, a card for each section (Interface, Reminders, Database, Troubleshooting and Links, each in its own colour) and a badge with an icon in each row. The new string `reminders` is only in English.
- Android: the menus of the filter and the more buttons of the habit list, the menu of the selection bar and the menu of the habit detail have the Material 3 Expressive style (a rounded tonal container, a rounded row with an icon for each option, the checked options filled and Delete in red). The colour button of the selection bar only shows when one habit is selected, and changing the type of an existing habit in the editor asks for confirmation first, as it does on iOS. The new strings `change_type_title`, `change_type_message` and `change_type` are only in English.
- iOS: the create and edit habit screen has the same design: a Yes / No and Measurable selector, rounded cards for the name and question, the unit, target and target type, four colour swatches (tapping Color opens the whole palette), a Frequency menu (Every day, Per week and Custom), a Reminder card with a switch whose time and round weekday toggles appear when it is on, and a notes card. Save stays disabled until the habit has a name (and a target when it is measurable), so the "cannot be blank" messages are gone. The new string `reminder_time` is only in English.
- iOS: the habit screen has a new design: the icon of the habit in the ring of its score with its name, question, frequency and reminder, an overview card with the score and its change in the last month and year, a Target card for measurable habits, a 3M / 6M / 1Y selector, and the score (a line over an area), the history (rounded bars), the completions by weekday, the best streaks and the calendar drawn in SwiftUI from the same data the core gives Android (a new read-only `detailCharts` in `salem-kit`). The menu has Edit history, Archive and Delete, and tapping the calendar opens the history editor. The new strings `edit_history`, `range_3m`, `range_6m`, `range_1y`, `now`, `this_week`, `this_month`, `missed`, `day_one` and `day_other` are only in English.
- iOS: the two sheets of the habit list have a new design: Habit Type, with one row for Yes / No and one for Measurable, and the value of a measurable habit, with the name of the habit, the day, the value with its unit and the notes (Skip and Unknown stay below the notes when they are on in Settings). Both are as tall as their content and the value field is focused on opening. The new string `habit_type` is only in English.
- iOS fixes: the buttons of the selection bar of the list (Archive, Delete and Done) are spaced out; changing the type of an existing habit in the editor asks for confirmation first, warning that the recorded entries are kept but not shown; Cancel is the highlighted button of the delete confirmation; and editing the history refreshes the row on tap, toggling a yes or no day and asking for the amount of a measurable one. The new strings `change_type_title`, `change_type_message` and `change_type` are only in English.
- iOS: the habit list has a new design: a large "Today" title with the date and how many habits are done, a strip of five days that chooses which day the rows show (the calendar button jumps to older days), one rounded card with a row per habit (its icon inside a ring with its score, a round check or the amount of a numerical habit, and a chevron to open it), and a floating bar with calendar, filter and more buttons and a round add button, in Liquid Glass on iOS 26 and a translucent material before. Selecting habits to archive or delete moved to the more menu, and rows can be archived or deleted by swiping. Icons come from a pool of symbols chosen from the habit id, so they do not match the mockup. The new strings `habits_done_of_total`, `calendar`, `select`, `more` and `unarchive` are only in English.
- Android: the two sheets of the habit list have the Material 3 Expressive design: Habit type, with a card for Yes or No and one for Measurable, and the value of a measurable habit, with the badge, name and day of the habit, the value with its unit, the notes and a full-width Save button in the colour of the habit (Skip and Unknown stay under it when they are on in Settings). Both are bottom sheets that can be swiped down. The new strings `habit_type` and `habit_type_subtitle` are only in English.
- Android: the habit list has the Material 3 Expressive design: a large title with round filter and more buttons, five fixed days (today in a tonal pill, scrolling the header goes back in time), one rounded card per habit with its icon inside the score ring, a button to open it and rounded day cells, and an extended "New habit" button in place of the plus in the bar. The card tones follow the colour of the habit for the light, dark and pure black themes: blue, pink, orange and purple habits have the exact tones of the design and the other colours get tones made the same way. Habits do not store an icon yet, so each one gets a stable icon from a pool, chosen from its uuid.
- Android: the create and edit habit screen has the same design: a rounded header with the badge and the large title, a Yes/No and Measurable selector (it can also change the type of an existing habit; a numerical habit keeps a daily, weekly or monthly frequency), four colour swatches plus a button for the whole palette, rounded fields, a target type selector, the Every day, Per week and Custom chips (Custom opens the previous frequency list) and a reminder card with a switch whose days are round toggles. The new strings (`new_habit`, `yes_no_option`, `per_week`, `custom`) are only in English.
- Android: the habit detail has the same design: a header in the tones of the habit with round back, edit and menu buttons, the icon and the name, the question and pills for the frequency and the reminder; a large score card with a ring and the month, year and total cards; and rounded cards for the score (line over an area with a value bubble), the history (rounded bars), the completions by weekday (horizontal bars) and the calendar (round dots) that scroll back in time as before, plus the target (pill tracks with what is left), the best streaks (centred pills in three tones) and the notes. Score, history, weekday, calendar, target and best streaks are drawn in Compose from the same card states; the core charts, the widgets and iOS are unchanged. The calendar days are changed with the EDIT button, no longer by tapping the dots. The new string `completions_by_weekday` is only in English.

### Fixed

- Opening the iOS app no longer fails when the database already has habits: the day is set before those habits are recomputed.
- The iOS app no longer crashes on launch when it reschedules the reminders: the reminder and notification loggers passed a Kotlin string straight to the variadic `NSLog`, which read it as a pointer
- A habit completed from a shortcut or the iOS widget no longer shows today's reminder (the pending notification is removed, and put back if the habit is unchecked before its time), and the reminders scheduled when the app returns to the foreground skip today for a habit that is already done. Shortcuts also do less work per action.
- The habit detail follows the dark mode while it is open on iOS (the cards, charts and history editor used to keep the old theme), it no longer refreshes after it was left or registers twice for the commands, it covers the screen from the first frame, and the Android detail stops its pending refreshes when it is destroyed.
- The number popup only accepts ASCII digits and one decimal mark (other digits used to be accepted and then replaced by the old value without a warning), a popup whose notes only differ in spaces is not saved again when it is closed, a late callback can no longer close a newer dialog of the iOS list, and the iOS habit list is hidden from VoiceOver while a dialog, popup or the editor is open.
- Migrations on iOS are atomic: each one and its version number are a transaction (`ROLLBACK` is now understood by the JVM and iOS databases), so a crash in the middle no longer leaves a half-migrated file that cannot be opened again
- The short form of numbers (`toShortString`, used by numerical cells, charts and widgets) no longer keeps trailing zeros: 5.004 shows "5" and 12.04 shows "12", as before the list moved to `salem-ui`; exact halves still round up
- The shared strings are copied as UTF-8 whatever the machine charset, accept the legacy language folders without a region, and the build fails on a format it cannot adapt (`%.1f`, `%%`...) instead of shipping it
- The task progress bar of the habit list sits at the top of the rows again (it was shifted 6 dp up), and the iOS app creates its timer on the main thread, no longer swallows cancellations, follows the system appearance as Compose reports it, refreshes the first weekday when it returns, and inserts the debug habits only in debug binaries

### Added

- The iOS app draws its screens in SwiftUI (list, editor, habit detail with charts, settings, about and intro). The habit logic stays in Kotlin (`SalemKit`). Android keeps Compose. Strings for iOS are generated from `strings.xml` by `syncIosStrings`. The habit list was opened on the iPhone 18 Pro Max simulator. Editor, charts, import, export and the notification permission are not fully checked there yet.
- Add shortcuts to iOS: the Shortcuts app gets the actions Check habit, Uncheck habit, Toggle habit, Add one to habit and Subtract one from habit (the iOS counterpart of the Tasker actions of Android), each on a habit you choose and always for today, and Siri and Spotlight know "Toggle a habit in Salem" and "Add one to a habit in Salem"; they change the database of the app through the App Group and tell the widget to draw again. English and Spanish. Not seen on a device or the simulator yet.
- Add a widget to iOS with the habits of today (small, medium and large): each habit has a button to check it from the home screen, a numerical habit has a plus button, and it shows the score; it uses the database of the app through the App Group and the app tells it to draw again when the habits change. It needs iOS 17 (the minimum of the app is now 17.0). Not seen on a device or the simulator yet.
- Export and share data on iOS with the share sheet of the system (AirDrop, Messages, Save to Files): Settings has "Export full backup" (a copy of the database that can be imported again), "Export as CSV", "Generate bug report" (version and device, as text) and "Repair database"; the Export item of the habit detail now shares the CSV of that habit instead of saying it is not available, and the messages of the app (exported, repaired, imported) show as a banner.
- Import data on iOS: Settings has a "Database" section with "Import data", which opens the Files app and imports a Loop database (or the files of Rewire, Tickmate and HabitBull), merging the habits by their identifier. The list and the reminders are updated when it ends and a message tells the result. A failed import is undone, so the habits stay as they were.
- Ask for the permission to show notifications on iOS when a habit with a reminder is saved, and when the app starts with reminders already set, and add "Customize notifications" to the iOS Settings, which opens the notification settings of the app. With the previous change the reminders now work on iOS.
- Wire the reminders into the iOS app: the habits with a reminder schedule their local notifications (the next days, again after every change and every time the app comes back), the Yes and No buttons check and uncheck the habit, a tap opens its detail, and a checked or deleted habit loses its delivered notification. The permission request and the notification row of Settings come in the next change, so nothing is shown yet on a fresh install.
- Show Settings on iOS (menu Settings): the short press toggle, the midnight delay, skip days, question marks, reversed days, pure black, the first day of the week, Help, Rate, About and the developer switch. The rows move to `salem-ui` and are shared with Android, which keeps its own sections (widgets, reminders, backups, import and export, troubleshooting). The first day of the week can be set from the shared code (`Preferences.firstWeekday` has a setter), and About opened from Settings goes back to Settings.
- Show the intro on the first launch of the iOS app: the intro (three slides with Skip, Next and Done) moves to `salem-ui` and is shared with Android; before, iOS marked the first run as done and showed nothing.
- Show the About screen on iOS (menu About, and Help opens the project page): the screen moves to `salem-ui` and is shared with Android, with the app version, the links, the developers and the translators (the list is generated as Kotlin by `updateTranslators` and drawn with Compose instead of an XML layout); five taps on the version turn on the developer mode and a message says so (a snackbar instead of a toast).
- Open the habit detail from the iOS habit list: tapping a habit shows its screen with the cards, the six charts, the history editor and the popups of each day, and the menu with Edit, Archive/Unarchive and Delete (Export says that it is not available on iOS yet). Back closes the topmost layer first.
- Move the history editor (the calendar that opens from the History card of the habit detail) to `salem-ui`, with a layer that shows it over the detail screen so the popup of a day opens above it; the Android dialog draws the shared content, and the Android-only chart host is gone.
- Move the habit detail screen to `salem-ui` (Compose Multiplatform), shared with the future iOS screen: the top bar and its menu, the eight cards and the six charts are the same code on both platforms, and `ShowHabitActivity` only hosts them. On Android the charts of the detail are now drawn with Noto Sans and the shared text placement, and the Edit icon is the one of Material.
- Prepare the charts of the habit detail for iOS: the canvas of `salem-ui` now places text like the Android one (the line is centred on the middle of its lower-case letters, which the charts of the core are laid out for), and `salem-ui` has a `CoreChart` with drag, fling, tap and long press. The six charts are rendered through that canvas in the JVM tests and compared with the reference images of the core.
- Create and edit habits in the iOS app: the new habit dialog, the editor, the colour picker, the delete confirmation and the checkmark and number popups of the shared UI open over the habit list (`ListOverlays` and `ListOverlayHost` in `salem-ui`, tested on the JVM); the back gesture closes them and a popup closed that way still saves the notes that changed.
- Move the create and edit habit screen to `salem-ui` (`EditHabitScreen`), shared with the future iOS screens: the colour, frequency, target type, time and days pickers are now opened over the form instead of as separate dialogs, the back button closes the open picker first, and the form is hidden from accessibility services while a picker is open. `EditHabitActivity` only hosts it.
- Share the save logic and the texts of the habit editor with the future iOS screens: a tested `HabitEditorSaver` in `salem-core` (a second tap on Save is ignored, a deleted habit is reported), and in `salem-ui` the frequency, reminder days and reminder time texts, the latter following the clock format of the device on Android, the desktop and iOS.
- Move the colour, weekday and time pickers of the habit editor to `salem-ui`, shared with the future iOS screens; the Android dialogs are thin hosts of the shared ones and keep the system labels of their buttons. New `OK` and `Cancel` strings (not translated yet) are used by the shared ones.
- Move the dialog surface, the habit type, list, frequency and delete dialogs and the checkmark and number popups to `salem-ui`, shared with the future iOS screens; the Android dialogs are now thin hosts of the shared ones. The number popup no longer uses `java.text` (an exact half such as 0.125 rounds up, as everywhere else in the app).
- Show the habit list in the iOS app over a real SQLite database: the app opens `uhabits.db` in its files and migrates it with the schema of `salem-core` (new `initializeHabitsSchema`, with the same version rules as Android), builds the list by hand on iOS (theme from the system appearance, day header, rows, bar, hint) and marks days with a tap. Creating, editing, settings, dialogs, reminders and the confetti are not on iOS yet; example habits can be inserted in debug runs with `SALEM_SEED_DEBUG_DATA=1`
- Move the top bar of the habit list (menus, search, selection bar) and the whole list screen to `salem-ui`, behind a `ListScreenModel` that `ListHabitsHost` implements; the task progress bar is now a Compose component (`TaskProgress`) and the action icons are vectors shared by every platform. The activity only adds what is Android (the condensed number font, the theme and the confetti). Test ids, labels and menus are unchanged
- Move the models, state holders (`HabitListState`, `HabitListController`, the menus), cells, rows, day header, empty state and hint of the habit list to `salem-ui` (Compose Multiplatform), with the Android-only parts behind small abstractions (colours, fonts, window insets, test ids). The list looks and behaves the same on Android; the number shown in a numerical cell now rounds exact halves up (12.25 shows 12.3, not 12.2) because the app uses one `toShortString`, the one of `salem-core`
- Share the app strings with the Compose Multiplatform UI: `salem-ui` copies the `strings.xml` of every language from `salem-android` at build time (Android stays the source, Crowdin is unchanged) and the Noto Sans and FontAwesome fonts of the core, with tests for translations, arguments, plurals and legacy language codes. `SalemTheme`, the colour scheme and `resolveTheme` moved to `salem-ui`, and the shared canvas now uses Noto Sans on every platform
- Add the `salem-ui` module (Compose Multiplatform for Android and iOS) with a `Canvas` of `salem-core` over a Compose `DrawScope` (`DrawScopeCanvas`, `CoreCanvasView`), checked against the core reference images on the JVM, and the `iosApp` Xcode project that hosts it and builds for the simulator. No screen uses it yet; the CI builds the module and the iOS app
- Move the rows of the habit list to Compose (`LazyColumn`): score ring, name and one cell per day, with long press to select, tap to open or change the selection, and drag to reorder when the order is manual. The data now comes from `HabitListState` and the gestures from `HabitListController`, with their rules in tested pure functions. The old `RecyclerView`, card and button Views and their image tests are removed. The list does not scroll by itself while dragging near an edge yet, and a toggled cell no longer shows a ripple
- Move the day header of the habit list to Compose, and add the Compose cells (checkmark, number and score ring) that the rows will use next; the header no longer has a dead zone when dragged past today
- Move the top bar of the habit list (add, filter, sort, search and overflow menus) and the multi-selection bar to Compose, replacing the Android menus and the action mode. The label of the hide completed filter now follows the skip setting too. The new "More options" and "Close" labels are not translated yet
- Move the empty state and the hint banner of the habit list to Compose, the first piece of the list to migrate
- Move the create and edit habit screen to Compose, with its form state and validation in `salem-core` (`EditHabitForm`, tested). A blank name or an invalid target is now rejected, and the unit and the target type are kept across a rotation
- Move the frequency picker and the target type and numerical frequency lists of the edit screen to Compose; the rules that turn the typed numbers into a frequency are unchanged and now covered by tests (an invalid or empty entry still becomes every day)
- Move the delete confirmation and the checkmark and number popups to Compose; closing a popup without choosing still saves changed notes. The buttons of the checkmark popup now have accessibility labels, and the new "Unknown" label is not translated yet
- Move the habit type, weekday, colour and snooze dialogs to Compose and remove the vendored AOSP colour picker; the colours keep their layout and accessibility labels. The snooze dialog no longer crashes when the habit is missing
- Move the Intro screen to Compose and remove the AppIntro library; the slides, colours and behaviour are the same, and the Next button label is not translated yet
- Move the Settings screen to Compose with reusable preference rows; the preference keys and the results returned to the habit list are unchanged. The unused sync server, sync key and encryption key settings and the hidden reminder sound row are gone
- Add Jetpack Compose (Material 3) to `salem-android` with `SalemTheme`, which maps the core `Theme` (light, dark, pure black), and move the About screen to Compose as the first screen of the migration; the developer-mode tap counter is now a tested class
- Add an iOS preferences storage (`UserDefaultsStorage`) to `uhabits-core`, tested on the iOS simulator
- Add an iOS database (`SqliterDatabase`, on sqliter) to `uhabits-core`; the database contract tests and the real migrations run on the iOS simulator
- Add the iOS first-weekday and "today" calculation (`NSCalendar`/`NSTimeZone`) to `uhabits-core`, which unblocks the numerical score and streak tests on the iOS simulator
- Add iOS `format`/`formatInvariant` to `uhabits-core` through a small printf implementation that rounds like the Android one (checked against `String.format` on the JVM)
- Add iOS file access (`AppleFileOpener`, user files in Application Support) to `uhabits-core`; the file, import and migration tests run on the iOS simulator
- Add an iOS date formatter (`AppleLocalDateFormatter`, on `NSDateFormatter`) to `uhabits-core` for weekday and month names
- Add an iOS zip reader and writer (raw DEFLATE through the system zlib) to `uhabits-core`, so the CSV export runs on iOS; archives are checked against the JDK
- Add iOS reminders and notifications to `uhabits-core`: local notifications scheduled for the next 7 days (respecting the 64 pending limit and the weekdays of each reminder), Yes/No actions, and a refresh of the day and the reminders when the app returns to the foreground
- Share `DateUtils`, `ReminderScheduler` and `MidnightTimer` between Android and iOS (they moved to `commonMain`)
- Run the shared tests on the iOS simulator in CI (439 tests run locally; only the canvas and chart tests and four database migration cases wait for later steps, listed in `uhabits-core/ios-blocked-tests.txt`)
- Add GitHub Actions CI on pull requests (any base branch, so stacked pull requests are checked), on pushes to `develop` and on manual runs (build, tests and ktlint)

### Fixed

- The habit detail screen no longer crashes when it is opened for a habit that was deleted meanwhile (from a widget, a notification or the back stack) or with a wrong address: it closes. Its menu now follows the habit: after archiving, the menu offers Unarchive at once
- The habit list no longer crashes when a reminder, widget or notification asks to edit a habit that was deleted meanwhile, or when the colour of an empty selection is requested
- Opening a Compose dialog (colour, habit type, weekdays, snooze and the others built on `ComposeDialogFragment`) no longer crashes the app with a `StackOverflowError`: `Content()` called itself forever inside `ComposeView.apply`
- CSV export no longer depends on the device locale: scores and target values always use `.` as decimal separator and ASCII digits (they were written as `0,5000` under Spanish and with Arabic-Indic digits under Arabic locales)
- Importing a Loop database no longer fails on devices whose locale uses non-Latin digits (the migration file names were built with the locale's digits)

### Changed

- The checkmark widget draws its ring with the core `RingChart` (through `CoreWidgetView`), and the `Canvas` of `salem-core` can outline text (`strokeText`) for the automatic checkmark; the last chart View, `RingView`, is removed. The image tests of the checkmark widget need new reference images (`./build.sh android-accept-images` on a device)
- The score, frequency, streak, target and history widgets draw their charts with the chart classes of `salem-core` (through `CoreWidgetView`), the same code as the cards of the detail screen, and the old chart Views, `ScrollableChart`, `AndroidDataView` and `AndroidView` are removed. The rows of the target widget still share the height of the widget. The image tests of these widgets in `src/androidTest` need new reference images (`./build.sh android-accept-images` on a device)
- The `Canvas` of `salem-core` can cut a hole in what it draws (`clearCircle`); the score chart of the widgets uses it for the markers, and a new `RingChart` (replacing the unused `Ring`) draws the ring of the checkmark widget with a transparent hole. Nothing uses them in the app yet: the widgets move to them in the next changes
- Remove code and resources that nothing used after the Compose migration: 78 entries of the lint baseline for files that no longer exist, 5 drawables, a layout, 13 styles, 2 strings, the `HabitChart` interface, `AndroidTestView`, 13 helpers of `ViewExtensions.kt` and the scroll controller of `ScrollableChart`
- The time pickers (the time of the reminder of a habit and the custom time of a snooze) are a Material 3 Compose dialog that keeps the colour of the habit, and the vendored Android Open Source Project date and time pickers (21 Java files, their layouts and `pickers.xml`) are removed; the date picker had no users
- The history editor (the calendar dialog of the habit detail screen) is drawn with Jetpack Compose and the same chart code as the calendar card; it now keeps the theme of the app when it refreshes (it used the light theme after the first change)
- The habit detail screen is a single Compose screen (`ShowHabitActivity` uses `setContent`): its top bar, with the way back, Edit and the menu (export, archive or unarchive, delete and, for developers, Randomize), is Compose too, and `ShowHabitView`, `ShowHabitMenu` and their layout and menu files are gone
- The habit detail screen shows all its cards (subtitle, notes, overview, target, score, bar, history, streaks and frequency) in one Jetpack Compose column under the toolbar, which is still a View until the activity moves; the old card Views and layouts are gone
- The streak, target and overview cards of the habit detail screen are drawn with Jetpack Compose; the streak and target charts now live in `salem-core` (`StreakChart`, `TargetChart`, drawn on the multiplatform `Canvas`, with image tests) and the overview ring is the Compose `ScoreRing` of the list. The widgets keep using the old chart Views
- The score and frequency cards of the habit detail screen are drawn with Jetpack Compose, and their charts now live in `salem-core` (`ScoreChart`, `FrequencyChart`, drawn on the multiplatform `Canvas`, with image tests); the `Canvas` interface gained `fontSpacing()`. The widgets keep using the old chart Views
- The history (calendar) and bar cards of the habit detail screen are drawn with Jetpack Compose: the charts of `salem-core` are shown by a new `CoreChart` composable (same drawing code as before), which scrolls them with a drag and a fling, and the interval drop-down is a Compose menu
- The habit list screen is now a single Compose screen (`ListHabitsActivity` uses `setContent`; the top bar, day header, rows, empty state, progress bar and hint are one composition) and `ListHabitsRootView` is gone; the Settings and import-file results use `ActivityResultContracts` (the result codes 101 to 105 are unchanged)
- Rename the Gradle modules `uhabits-core` and `uhabits-android` to `salem-core` and `salem-android`
- Rename the Kotlin packages from `org.isoron.*` to `io.github.crisacm.salem.*` (the Android `namespace` is now the same as the application id); the intent actions, the `content://org.isoron.uhabits` URI and the database file name are unchanged
- Remove the JavaScript (browser) target from `uhabits-core`; the focus is Android and iOS
- Remove unused dependencies (Ktor, Jackson, Guava, OpenCSV, Apache Commons, JSR305 and others) from the app and `uhabits-core`
- Add an Android target to `uhabits-core` and a source set shared by the jvm and android targets
- Add iOS targets (`iosArm64`, `iosSimulatorArm64`) to `uhabits-core` with stub implementations, and a macOS CI job that compiles them
- Rebrand the README as "Salem - Focus & Habit Tracker" and credit the original Loop Habit Tracker project
- Adapt the issue templates, developer guidelines, build and test documentation to the Salem fork
- Remove the original screenshots
- Rename the app to Salem and change the application id to `io.github.crisacm.salem`
- Rewrite the Google Play listing for Salem (English and Spanish) and remove Loop's store graphics and translations
- Add CONTRIBUTING, SECURITY and pull request template
- Add docs/PENDING.md with the pending tasks

## Loop Habit Tracker -- unreleased upstream changes included in the fork base

### Added

- Add habit search that filters by name, question and notes (@I-Dont-Remember, #2338)
- Add archive and unarchive actions on the habit statistics page (@KyleSCraig, #2194)
- Add option to select a public folder for automatic backups via SAF (@MihanEntalpo, #2209)
- Allow adding newlines to the habit name field (@powerjungle, #2137)

### Changed

- Auto-save notes when dismissing checkmark and number dialogs (@fictiontoreality, #2261)
- Show a toast message after archiving or unarchiving habits (@iSoron, 54275020)

### Fixed

- Fix streak chart date format to be more compact (@iSoron, 07d87e4a)
- Support content URIs when sharing backup files (@iSoron, 4383b3ed)
- Fix frequency chart rendering random data in production (@TangilHossain, #2242)

### Refactoring & Testing

- Migrate uhabits-core to Kotlin multiplatform (JVM + JS) (@sgallese, @iSoron)
- Improve build.sh logging, validation, emulator lifecycle and ARM64 support (@iSoron)

## [2.3.2] -- 2026-10-17

### Fixed

- Fix widget picker dialog styling (@iSoron, #2341)
- Fix habit edit screen not being scrollable when the keyboard is shown (@iSoron, #2342)
- Fix crash when scrolling charts to dates before 1970 (@TangilHossain, #2229)
- Improve ripple effect in "pure black" dark mode and handle navigation bar insets in settings (@Vladuken, #2219)

## [2.3.1] -- 2025-08-13

### Changed

- Add notes to exported CSV files (@iSoron)

### Fixed

- Prevent some views from being obscured by system UI (@iSoron, #2171)
- Disable confetti if animations are disabled globally (@iSoron, #2170)
- Make symbols easier to distinguish in "pure black" dark mode (powerjungle, #2136)
- Trim unit labels when necessary (@hiqua, @iSoron, #2158)

## [2.3.0] -- 2025-06-23

### Added

- Add support for Android 15 and 16 (@iSoron)
- Show confetti animation (@gokulk16, @iSoron, #1743)
- Show streaks for measurable habits (@teckwarz, #2059)
- Allow user to unset measurable habits (@leontodd, @kalina559, #1899, #2109)

### Changed

- Change background widget color for habits with implicit checks (@wobbba, #1915)

### Fixed

- Fix notification when goal type is set to maximum (@manish99verma, #1931)
- Never mark "at most" habits as completed for the day (@kalina559, #2077)
- Increase minimum widget size (@iSoron, #2118)
- Improve Gradle configuration (@jimlyas, #2108)

## [2.2.0] -- 2024-01-30

### Added

- Add support for Android 14 (@iSoron, @hiqua)
- Allow user to change app language (@leondzn)

### Fixed

- Implement workaround to make notifications non-dismissible in Android 14 (@iSoron, #1872)
- Fix splash screen background color in dark mode (@SIKV, #1888)

## [2.1.3] -- 2023-08-28

### Fixed

- Use text input on Samsung devices (@iSoron, #1719)
- Prevent crash if alarm permission is revoked (@iSoron)
- Adjust widget colors (@iSoron)
- Fix bug preventing screens from updating at midnight (@iSoron)
- Fix skip button in locales that use comma instead of dot (@iSoron, #1721)

## [2.1.2] -- 2023-05-26

### Fixed

- Fix bug that caused widget to enter checkmark on wrong date (@iSoron, #1541)
- Fix widget corners on Android 12 (@iSoron)
- Fix bug that caused notes to be lost when editing a checkmark (@iSoron, #1566)
- Prevent soft keyboard from covering entry popup (@iSoron)
- Accept comma (instead of dot) in certain locales (@iSoron)

### Changed

- Remove update delay after entering a checkmark (@iSoron)

### Removed

- Remove stack widgets (@iSoron)

## [2.1.1] -- 2022-09-24

### Fixed

- Fix Tasker plugin (@iSoron, #1503)

## [2.1.0] -- 2022-09-10

### Added

- Allow user to add notes to specific dates (@vbh, #1103)
- Allow user to track "at most" numerical habits (@KristianTashkov, #1101)
- Allow user to add skips to measurable habits (@kalina559, #1319)
- Bring back custom frequencies (x times in y days) (@hiqua, #1079)
- Improve number picker (@hiqua, @iSoron, #1082, #1370)
- Add new checkmark and number picker (@iSoron, #1370)
- Allow user to import numerical habits from HabitBull (@hiqua, #1278)
- Add support for Android 13 themed icons (@cheeeeer, #1497)

### Removed

- Hide snooze button in Android 12 notifications (@hiqua, #1226)
- Remove preference to set LED lights (@iSoron)

### Changed

- Hide failed habits along with completed ones (@hiqua, #1052)
- Cycle through all checkmark states when toggling (@iSoron)
- Add delay after toggling a habit (@hiqua, @kalina559, #1147)
- Small theme improvements (@KristianTashkov, #1113)
- Left-align habit notes (@iSoron)
- Increase target SDK to 31 (@hiqua)

### Fixed

- Fix small dialog buttons (@kalina559, #1096)
- Fix invalid CSV files (@hiqua, #1177)
- Fix small issues in calendar chart (@kalina559, #1314)
- Resort habit list after edit (@hiqua, #1350)
- Fix marker scaling in frequency display (@eduebernal, #1425)
- Fix widgets not working correctly on API 33 (@iSoron, #1488)

### Refactoring & Testing

- Replace raster icons by vector assets (@kalina559)
- Remove JVM dependencies from uhabits-core module (@sgallese)
- Add various missing tests (@sgallese)
- Upgrade project dependencies (@hiqua, @sgallese)

## [2.0.3] - 2021-08-21

### Fixed

- Improve automatic checkmarks for monthly habits (@iSoron, #947)
- Fix small theme issues (@iSoron)
- Fix ANR on some Samsung phones (@iSoron, #962)
- Fix dates before the year 2000 (@iSoron, #967)
- Fix notification adding checkmarks to the wrong day (@hiqua, #969)
- Fix crashes in widgets (@hiqua, @iSoron, #907, #966, #965)
- Fix crash when moving habits (@hiqua, #968)

## [2.0.2] - 2021-05-23

### Changed

- Make checkmark widget resizable

### Fixed

- Fix crash caused by numerical habits with zero target (@iSoron, #903)
- Fix small issues with font size (@iSoron)
- Allow fractional target values (@sumanabhi, #911)
- Fix IllegalStateException in androidx.customview.view (@iSoron, #906)
- Fix crash when selecting habit frequency in some languages (@iSoron, #926)
- Fix IllegalArgumentException in RingView (@iSoron, #904)

## [2.0.1] - 2021-05-09

### Added

- Make midnight delay optional and disabled by default (@hiqua)
- Add arrows to sort menu (@iSoron)

### Removed

- Temporarily remove experimental device sync functionality. This feature will be re-added in
  Loop 2.1.

### Changed

- Make implicit checkmarks easier to read (@iSoron)
- Update and improve list of translators (@hiqua, @iSoron)

### Fixed

- Disable transparency for stacked widgets (@hiqua)
- Fix various color issues on the dark theme (@hiqua, @iSoron)
- Fix "customize notifications" on older devices (@hiqua)
- Fix snooze button in notifications when device is locked (@hiqua)
- Fix a crash when deleting habits (@engineering4good)
- Fix checkmark widget not rendering properly on some Samsung phones (@iSoron)

### Refactoring & Testing

- Finish conversion of the entire project to Kotlin (@hiqua, @iSoron, @MarKco)
- Automatically run large tests on GitHub Actions (@iSoron)
- Remove unused v21 resources (@hiqua)

## [2.0.0-alpha] - 2020-11-29

### Added

- Track numeric habits (@iSoron, @namnl)
- Skip days without breaking streak (@KristianTashkov)
- Sort habits by status (@hiqua)
- Sort habits in reverse order (@iSoron)
- Add notes to habits (@recheej)
- Improve readability of charts (@chennemann)
- Delay new day until 3am (@KristianTashkov)
- Export backups daily (@iSoron)

### Removed

- Drop support to devices older than Android 6.0 (API 23)

### Fixed

- Reset chart offset when switching scale (@alxmjo)
- Don't show reminders from archived habits (@KristianTashkov)
- Lapses on non-daily habits decrease the score too much (@iSoron)
- Update widgets at midnight (@KristianTashkov)

### Refactoring

- Convert files to Kotlin (@olegivo)

## [1.8.12] - 2021-01-30

- Fix bug that caused incorrect check marks to show after scrolling (#713)
- Fix issue preventing widgets from updating at midnight (#680)

## [1.8.11] - 2020-12-29

- Fix theme issues on Xiaomi phones

## [1.8.10] - 2020-11-26

- Update translations

## [1.8.9] - 2020-11-18

- Manage exceptions when activities don't exist to handle intents (#181)
- MemoryHabitList: Inherit parent's order (#598)
- Remove notification groups; revert to default system behavior
- Remove SyncManager and Internet permission

## [1.8.8] - 2020-06-21

- Make small changes to the habit scheduling algorithm, so that "1 time every x days" habits work
  more predictably.
- Fix crash when saving habit

## [1.8.0] - 2020-01-01

- New bar chart showing number of repetitions performed in each week, month, quarter or year.
- Improved calculation of streaks for non-daily habits: performing habits on irregular weekdays will
  no longer break your streak.
- Many more colors to choose from (now 20 in total).
- Ability to customize how transparent the widgets are on your home screen.
- Ability to customize the first day of the week.
- Yes/No buttons on notifications, instead of just "Check".
- Automatic dark theme according to phone settings (Android 10).
- Smaller APK and backup files.
- Many other internal code changes improving performance and stability.

## [1.7.11] - 2019-08-10

- Fix bug that produced corrupted CSV files in some countries

## [1.7.10] - 2019-06-15

- Fix bug that prevented some devices from showing notifications.
- Update targetSdk to Android Pie (API level 28)

## [1.7.8] - 2018-04-21

- Add support for adaptive icons (Oreo)
- Add support for notification channels (Oreo)
- Update translations

## [1.7.7] - 2017-09-30

- Fix bug that caused reminders to show repeatedly on DST changes

## [1.7.6] - 2017-07-18

- Fix bug that caused widgets not to render sometimes
- Fix other minor bugs
- Update translations

## [1.7.3] - 2017-05-30

- Improve performance of 'sort by score'
- Other minor bug fixes

## [1.7.2] - 2017-05-27

- Fix crash at startup

## [1.7.1] - 2017-05-21

- Fix crash (BadParcelableException)
- Fix layout for RTL languages such as Arabic
- Automatically detect and reject invalid database files
- Add Hebrew translation

## [1.7.0] - 2017-03-31

- Sort habits automatically
- Allow swiping the header to see previous days
- Import backups directly from Google Drive or Dropbox
- Refresh data automatically at midnight
- Other minor bug fixes and enhancements

## [1.6.2] - 2016-10-13

- Fix crash on Android 4.1

## [1.6.1] - 2016-10-10

- Fix a crash at startup when database is corrupted

## [1.6.0] - 2016-10-10

- Add option to make notifications sticky
- Add option to hide completed habits
- Display total number of repetitions for each habit
- Pebble integration: check/snooze habits from the watch
- Tasker/Locale integration: allow third-party apps to add checkmarks
- Export an unified CSV file, with checkmarks for all the habits
- Increase width of name column according to screen size
- Stop showing reminders for archived habits
- Add Danish, Dutch, Greek, Hindi and Portuguese (PT) translations
- Other minor fixes and enhancements

## [1.5.6] - 2016-06-19

- Fix bug that prevented checkmark widget from working

## [1.5.5] - 2016-06-19

- Fix bug that prevented check button on notification to work sometimes
- Fix bug that caused back button to apparently erase some checkmarks
- Complete French translation
- Add Croatian and Slovenian translations

## [1.5.4] - 2016-05-29

- Fix crash upon opening settings screen in some phones
- Fix missing folders in CSV archive
- Add Serbian translation

## [1.5.3] - 2016-05-22

- Complete Arabic and Czech translations
- Fix crash at startup
- Fix checkmark widget on custom launchers

## [1.5.2] - 2016-05-19

- Fix missing attachment on bug reports
- Fix bug that prevents some widgets from rendering
- Complete Japanese translation

## [1.5.1] - 2016-05-17

- Fix build on F-Droid

## [1.5.0] - 2016-05-15

- Add night mode, with AMOLED support
- Backport material design to older devices
- Display more information on statistics screen
- Display score on main screen and checkmark widget
- Make widgets react immediately to touch
- Reschedule reminders after reboot
- Pick first day of the week according to country
- Add option to reverse order of days on main screen
- Add option to change notification sounds
- Add Catalan, Indonesian, Turkish, Ukrainian translations
- Switch between Simplified/Traditional Chinese according to country

## [1.4.1] - 2016-04-09

- Show error message on widgets, instead of crashing
- Complete French translation
- Minor fixes to other translations

## [1.4.0] - 2016-04-07

- Ability to import data from third-party apps
- Ability to save and restore full database backup
- Show more information on streak chart
- Simplify interface for creating habits
- Add link to Frequently Asked Questions (FAQ)
- Reduce app loading time and lag on widgets
- Generate bug reports on crash and from settings screen
- Disable vibration according to phone settings
- Add Czech translation
- Fix wrong month names for some languages

## [1.3.3] - 2016-03-20

- Add Spanish and Korean translations
- Make small corrections to other translations
- Fix incorrect date in history calendar

## [1.3.2] - 2016-03-18

- Add Arabic, Italian, Polish, Russian and Swedish translations
- Minor fixes to German and French translations
- Minor bug fixes

## [1.3.1] - 2016-03-15

- Fixes crash on devices with large screen, such as the Nexus 10
- Fixes crash when clicking widgets and reminders of deleted habits
- Other minor bug fixes

## [1.3.0] - 2016-03-12

- New frequency plot: view total repetitions per day of week
- New history editor: put checkmarks in the past
- Add German, French and Japanese translations
- Add about screen, with credits to all contributors
- Fix small bug that prevented habit from being reordered
- Fix small bug caused by rotating the device

## [1.2.0] - 2016-03-04

- Ability to export habit data as CSV
- Widgets (checkmark, history, score and streaks)
- More natural scrolling on data views (fling)
- Minor UI improvements on pre-Lollipop devices
- Fix crash on Samsung Galaxy TabS 8.4
- Other minor bug fixes

## [1.1.1] - 2016-02-24

- Show reminder only on chosen days of the week
- Rearrange habits by long-pressing then dragging
- Select and modify multiple habits simultaneously
- 12/24 hour format according to phone preferences
- Permanently delete habits
- Usage hints during startup
- Translation to Brazilian Portuguese and Chinese
- Other minor fixes

## [1.0.0] - 2016-02-19

- Initial release

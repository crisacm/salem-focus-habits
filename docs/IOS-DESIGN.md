# iOS design: SwiftUI with the look of iOS 26

This document records the design decisions of the iOS app of Salem: the habit list, the create and edit habit screen,
the habit detail, the sheets and Settings. It explains the concept so that new screens and later changes keep the same
look. The Android counterpart is `docs/ANDROID-DESIGN.md`.

If this document disagrees with the code, the code wins; fix this file.

## 1. Scope and principles

- **iOS only.** The screens are SwiftUI in `iosApp/iosApp/`, over the `SalemKit` framework (`salem-kit`, which wraps
  `salem-core`). The iOS app does not link Compose or `salem-ui`. Android, the widgets and the shortcuts are not changed by
  the iOS design.
- **Faithful to the mockups.** The screens follow the supplied mockups (light and dark) as closely as possible. Layouts were
  measured on captures with a 402 pt wide frame, and the numbers below are in pt.
- **System first.** Use the controls of the system (`Form`, `Picker`, `Menu`, `NavigationStack`, sheets with detents)
  and the colours that adapt to the appearance (`systemGroupedBackground`, `secondarySystemGroupedBackground`, `.primary`,
  `.secondary`). Draw something custom only when the mockup needs it.
- **Liquid Glass where the system has it.** On iOS 26 the floating bar and the close button of the sheets use the glass of
  the system. The minimum is iOS 17, where a translucent material takes its place (see "Compatibility" below).
- **One colour per habit.** The ring, the check, the pills and the charts of a habit take their tones from its colour.
- **No data model changes.** The screens read snapshots from `SalemKit` (`ListSnapshot`, `DetailSnapshot`,
  `DetailCharts`, `EditorSnapshot`, `SettingsSnapshot`, `HistoryDay`) and change data through `SessionModel`.

### Compatibility

Code that needs a newer SDK is written as `#if compiler(>=6.2)` plus `if #available(iOS 26.0, *)`, with the older
look in the other branch, as `floatingGlass(in:)` (`FloatingBar.swift`) and `SheetCloseButton` (`Dialogs.swift`) do. The
Xcode that builds the app is 26; the app still runs from iOS 17.

## 2. Colour

- `dynamicColor(light:dark:)` (`HabitStyle.swift`) makes a colour that follows the light and dark appearance of the screen.
  Use it for any colour of the mockups that has two values.
- **Backgrounds:** screens and sheets use `Color(.systemGroupedBackground)`; cards, text fields and rows sit on
  `Color(.secondarySystemGroupedBackground)`. Sheets set their background with `presentationBackground` (see `fittedSheet()`).
- **Indigo of the system** is the colour of the primary actions: the add button and the Save button of the value sheet use
  `#5856D6` in light and `#7D7AFE` in dark with a white or `#0B0B14` glyph; the check pill of the Notes sheet uses `#7D7AFF` in dark.
- **Greys of the controls:** the cross pill of the Notes sheet is `#DEDEE4` in light and `#3D3D41` in dark with a `.primary`
  glyph; the pill of a measurable habit that has not reached its target (`pillGrey`) is `#E9E9EB` in light and `#3D3D41` in dark.
- **Habit tones** (`HabitTone` and `habitTone(palette:colorArgb:)`): `accent`, `onAccent` and `track` (`accent` at 20%).
  - Blue (palette 11), pink (15), orange (2) and purple (13) have the exact accents of the mockups, in light and dark.
  - Any other colour gets an accent of the same lightness made in Oklab from its hue (tone 41 in light, 80 in dark).
  - The charts use `ChartPalette` (`DetailCharts.swift`): `accent`, `muted` (60%), `area` (12%) and `line` (`systemGray5`).
- **Editor palette:** `editorPalette(_:)` (`Dialogs.swift`) and `paletteArgb(_:)` (`EditorComponents.swift`) give the colour of
  each of the 20 palette entries.
- Do not hard-code `Color.white` or `Color.black` for text on a habit colour; use `tone.onAccent`.

## 3. Shape, space and type

- **Radii:** cards of the detail 24 (`DetailCard`), text cards of the sheets 28 (`continuous` style), the small cells of
  the pickers 12. Fields of one line, pills and buttons are capsules.
- **Rows** of the editor are 56 pt high with 17.5 pt of side inset (`formRow()`); fields of the sheets are 56 pt high with
  17 pt inside and 20 pt of margin to the edge of the sheet.
- **Type:** the system font (SF). Large title of the list 34 bold (`.largeTitle.bold()`), the title of the habit 34 bold,
  titles of sheets 17 semibold with a 13 pt secondary subtitle, card titles 18 semibold with a 12 pt subtitle, body 17,
  labels 15 and 13, figures of the detail 28 to 48 bold. Prefer fixed sizes where the mockup measures them, and keep the
  rest on text styles so that Dynamic Type works.
- **Icons:** SF Symbols. Habits store no icon: `habitSymbol(id:)` picks a stable one from a pool of 28 by the id of the
  habit (`habitSymbols`); the pool only grows at its end so the icon of a habit never changes.
- **Touch targets** are 44 pt at least (`BarIcon` is 48 × 44, `SheetCloseButton` 44 × 44, the swatches of `ColorSwatchRow` have a 44 pt hit area).

## 4. Habit list (`ListScreen.swift`, `HabitStyle.swift`, `FloatingBar.swift`)

- There is **no navigation bar** (`.toolbar(.hidden, for: .navigationBar)`). The header is a large title with the day
  ("Today" or the weekday), a 15 pt line with the date and "N of M" habits done, and a strip of **5 day circles**; the selected
  day is a filled circle in `.primary`. Picking a day shows that day in the rows.
- The list is `List` with `.insetGrouped` style on `systemGroupedBackground`. Each row is 56 pt at least: the **score ring**
  (`ScoreRing`, 37 pt, with the habit symbol inside), the name (17 pt, up to 2 lines), the mark of the day and a chevron
  that opens the detail.
- **Mark of the day:** a yes or no habit shows `HabitCheck` (28 pt circle: filled with a check when done, empty grey
  circle otherwise, "–" for a skipped day and "?" for an unknown one); a measurable habit shows `ValuePill` (34 pt capsule,
  filled with the colour of the habit when the target is reached, grey when not).
- **Interaction:** tapping the mark opens the Notes sheet (yes or no) or the value sheet (measurable); with the setting
  "Toggle with short press" on, a tap toggles at once and a long press opens the sheet. Tapping the rest of the row opens the
  detail. In selection mode the rows show a selection circle in the colour of the habit.
- **Floating bar:** a capsule with glass (calendar, filter menu, more menu) and the round **add button** (44 pt, indigo).
  In selection mode the capsule shows Archive, Delete and Done. The bar floats over the list at the top (`actionBar`).
- The empty state shows a title and an "Add habit" button; the hint banner (`ListHintBanner`) shows the tips of the app.

## 5. Create and edit habit (`EditorScreen.swift`, `EditorComponents.swift`)

- A sheet with a `NavigationStack` and a `Form`; the close button is `EditorCloseButton` (the close role on iOS 26) and
  Save is a confirmation button that stays disabled until the habit has a name (and a target when it is measurable).
- The first section is a **segmented Yes / No and Measurable selector**; it can change the type of an existing habit, which
  first asks for confirmation.
- Cards: name and question; for a measurable habit the unit, the target and the target type; **colour** with four swatches
  (`ColorSwatchRow`: blue, pink, orange and purple, plus the current colour if it is another one) and the full palette
  in a sheet; the **frequency menu** (Every day, Per week, Custom, the last two open the frequency sheet); the **reminder**
  card, a switch whose time row and round weekday toggles (`WeekdayCircles`, from Monday) appear when it is on; a notes card.
- Rows are built with `formRow()` so all of them have the same height and insets.

## 6. Habit detail (`DetailScreen.swift`, `DetailCards.swift`, `DetailCharts.swift`)

- A scrolling column of cards on `systemGroupedBackground`, 15 pt apart. The navigation bar only has Edit and a menu
  (Edit history, Archive or Unarchive, Delete).
- **Header** (`DetailHeader`): the symbol of the habit, the name (34 bold), the question, the frequency and the reminder.
- **Overview** (`OverviewCard`, yes or no habits): the score in large type with its ring and the change in the last month and
  year and the total. **Target** (`TargetCard`, measurable habits): what is left for the day, week, month, quarter and year.
- A **3M / 6M / 1Y segmented selector** drives the score and history charts (weekly points up to 6 months, monthly after).
- Charts are drawn in SwiftUI from `DetailCharts` (`salem-kit`, read only): `ScoreLineChart` (smooth line over an area),
  `HistoryBarsChart` (rounded bars), `WeekdayBarsChart`, `StreakRowsChart` (best streaks, with a text when there are none)
  and `CalendarDotsChart` (18 weeks; tapping it opens the history editor). They share `ChartPalette`.
- The notes of the habit show in `NotesCard` when it has some.
- **Edit history** (`HistoryEditor`): a sheet that lists the last 60 days; tapping a yes or no day opens the Notes sheet,
  tapping a measurable day opens the value sheet.

## 7. Sheets and dialogs (`Dialogs.swift`)

- **Pattern:** a header of 44 pt that starts 23 pt from the top with `SheetCloseButton` at the left and a centred title (17
  semibold, optional 13 pt secondary subtitle); the content below with 20 pt of margin; a 24 pt space at the bottom.
  The sheet is as tall as its content (`fittedSheet()`: it measures the content, sets `.presentationDetents([.height])`,
  shows the drag indicator and the grouped background).
- **Habit type** (`HabitTypeDialog`): two rows, Yes / No and Measurable, each with a symbol, a title and an example.
- **Value of a measurable habit** (`NumberPopup`): the name and day in the header with a Save capsule, a 56 pt capsule field
  with the value and the unit (focused on opening) and a 100 pt notes card; Skip and Unknown are text buttons under the
  notes when they are on in Settings.
- **Notes of a yes or no habit** (`CheckmarkPopup`): "Notes" with "habit · day" as subtitle, a notes card 148 pt high
  (radius 28, "Notes" placeholder) and two 56 pt pills of equal width, 12 pt apart: a check on indigo (saves Yes) and a
  cross on grey (saves No); both save the note. Skip and Unknown are text buttons under the pills when they are on in Settings.
- Others: `ColorPickerDialog`, `FrequencyPickerDialog`, `WeekdayPickerDialog`, `TimePickerDialog` and
  `DeleteHabitsDialog` (a confirmation with two buttons).
- Any new sheet should reuse `SheetCloseButton` and `fittedSheet()` and keep this header.

## 8. Settings, About and Intro (`SettingsAboutIntro.swift`)

- Settings is a sheet with a `Form`: the switches (toggle with short press, extend the day past midnight, skip days,
  question marks, reverse the order of days, pure black, animations, sticky notifications), the first day of the week,
  **Theme** (Automatic, Dark, Light) and **Language** (System default, English, Español) as `Picker`s, and the buttons
  that import, export, repair and open the notification settings of the system.
- About shows the app and its credits; the Intro runs the first time the app opens.
- Theme is applied with `.preferredColorScheme` in `RootView`.

## 9. Language and strings (`Strings.swift`)

- All the text goes through `L(key, fallback)`, which reads the strings of the language of the app (`AppLanguage`: "" for
  the device, "en" or "es"). Changing the language redraws the screens at once, because `AppLanguage` is observable.
- Dates and names of days and months use `appDateFormatter()`, `appCalendar()` and `appTimeText(_:)` so they follow the
  language of the app; do not create a bare `DateFormatter()` or read `Calendar.current` symbols.
- Strings come from `salem-android/src/main/res/values*/strings.xml` through the `syncIosStrings` Gradle task, which
  rewrites `Localizable.xcstrings`. Add a key there, not in the catalog. The widgets and the shortcuts have their own
  catalogs and follow the language of the device.

## 10. Icon and name

- The icon is `Assets.xcassets/AppIcon.appiconset` (PNG without an alpha channel, as the App Store requires). The name under
  the icon is `Salem` (`CFBundleDisplayName`); the full name "Salem: Focus & Habits" does not fit there.

## 11. Where the code is

| Area | Files (under `iosApp/iosApp`) |
|---|---|
| Entry and root | `iOSApp.swift`, `ContentView.swift`, `RootView.swift` |
| State and data | `SessionModel.swift` (over `SalemSession` in `salem-kit`) |
| Colour, tones, icons, ring, check, pill | `HabitStyle.swift` |
| List and its bar | `ListScreen.swift`, `FloatingBar.swift` |
| Create and edit | `EditorScreen.swift`, `EditorComponents.swift` |
| Detail | `DetailScreen.swift`, `DetailCards.swift`, `DetailCharts.swift`, `ChartView.swift` |
| Sheets and dialogs | `Dialogs.swift` |
| Settings, About, Intro | `SettingsAboutIntro.swift` |
| Strings and dates | `Strings.swift`, `Localizable.xcstrings` (generated) |
| Widgets and shortcuts | `iosApp/SalemWidgets`, `iosApp/SalemIntents` (they link `SalemExtras`, never `SalemKit`) |

## 12. Known deviations and alternatives

| Difference from the mockups | Why | Alternative |
|---|---|---|
| The floating bar, the close button and the floating sheet are the glass of iOS 26 | The mockups are drawn for iOS 26 | On iOS 17 to 25 a translucent material and sheets that reach the edges are used |
| System font (SF) | It is the font of the mockups and of the system | None needed |
| Icons come from a pool of symbols | Habits store no icon | Add an icon column with a migration and a picker |
| Skip and Unknown are text buttons under the pills of the Notes sheet | They do not appear in the mockups (they are off by default) | Move them into a menu |
| The sheets are as tall as their content, so the height changes with Dynamic Type | Fixed heights would cut the text | Use a fixed detent and scroll |
| Widgets and shortcuts follow the language of the device | They run in other processes | Share the language through the App Group |
| Some strings exist only in English or in English and Spanish | Translations are not hand-edited | Run the translation process |

## 13. How to check a change

```
xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp -configuration Debug \
  -sdk iphonesimulator -destination 'generic/platform=iOS Simulator' CODE_SIGNING_ALLOWED=NO build
./gradlew :salem-kit:jvmTest :salem-extras:jvmTest
```

Then compare each screen in the simulator with its mockup in light and dark, on iOS 26 and on iOS 17 to 25 (material
instead of glass), with a large Dynamic Type size, with VoiceOver (every icon button has an accessibility label) and with
the language set to Spanish. The layout numbers above were measured on captures and the tests cover the logic of the
kit, not the pixels, so the comparison in the simulator is the real check.

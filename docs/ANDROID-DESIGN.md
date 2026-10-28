# Android design: Material 3 Expressive

This document records the design decisions of the Android redesign of Salem: the habit list, the create and edit
habit screen and the habit detail. It explains the concept so that new screens and later changes keep the same look.

If this document disagrees with the code, the code wins; fix this file.

## 1. Scope and principles

- **Android only.** The screens live in `salem-ui` (shared Compose) and are hosted by `salem-android`. The iOS app
  (SwiftUI over `SalemKit`), `salem-core`, the widgets and the old core charts are not changed by the redesign.
- **Faithful to the mockups.** The screens follow the supplied mockups (light, dark and pure black) as closely as
  possible. Layouts were measured on captures with a 390 dp wide frame, and the numbers below are in dp (or sp for text).
- **Rules that are inferred from the mockups are applied as drawn.** For example, the history bars never go above 62.5% of
  the chart height and the labels of the score axis are evenly spaced.
- **One colour per habit.** Every card, chart and control of a habit takes its tones from the colour of the habit.
- **No data model changes.** The redesign only reads the existing card states of the core.

## 2. Colour

### Expressive colour scheme

`ui/compose/ExpressiveScheme.kt` gives the Material 3 colours of the screens: `Theme.toExpressiveColorScheme()` picks
the light (lavender), the dark or the pure black scheme, and `Theme.usesDarkTones()` says which tones of the habits to use.
The values come from the mockups. Examples:

| Role | Light | Dark |
|---|---|---|
| background / surface | `#FBF8FD` | `#111315` (pure black: `#000000`) |
| surfaceContainer (cards) | `#F1EEF6` | `#1F1E25` |
| surfaceContainerHighest (tracks, dim pills) | `#E3E0E8` | `#2F3137` |
| primary | `#1B5FC1` | `#AAC7FF` |
| onSurfaceVariant | `#434549` | `#C9C9CD` |
| outline | `#79747E` | `#938F99` |

### Habit tones

`ui/list/HabitTones.kt` (`habitTones(paletteIndex, dark)`) gives each habit these tones: `container`, `onContainer`,
`accent`, `onAccent`, `soft`, `onSoft`, `ringTrack`, `chip`, `muted` and `emphasis`.

- Blue (palette 11), pink (15), orange (2) and purple (13) have the **exact tones measured in the mockups**, in light and dark.
- Any other colour gets tones **generated in Oklab** with the same lightness for each role and a similar chroma.
- Archived habits use grey (`ARCHIVED_TONES`).
- `muted = lerp(container, accent, 0.30)` is the softer colour of the bars that are not highlighted.
- `emphasis = lerp(accent, onContainer, 0.27)` in dark and `0.5` in light, for the figures that stand out on a neutral card.
- The detail screen reads them with `rememberDetailTones(theme, color)`.

## 3. Shape, space and type

- Cards are rounded panels with a radius of 36 and 20 of padding (`ExpressiveCard`); stat cards use 28.
- Tracks, bars, chips and buttons are pills (fully rounded).
- Card titles are 24sp semibold with an optional 14sp subtitle (`CardHeader`); labels are 14sp, values and numbers
  inside pills 16sp, chart labels 12sp.
- The font is the system one. The mockups use another font; see section 8.

## 4. Habit list

- The header always shows **5 day columns** (`DAY_COLUMNS`); today is a tonal pill, and scrolling the header goes back in
  time. The rows follow the same offset.
- Each habit is **one rounded card** in its container colour, with the icon inside the score ring, the name, the open
  button and rounded day cells.
- **Icons:** habits do not store an icon. Each habit gets a stable one from a pool of 28 chosen from its uuid; the pool only
  grows at its end so the icon of a habit never changes. Icons therefore differ from the ones drawn in the mockups.
- The create button is an **extended "New habit" button** instead of the plus of the old bar.
- Tones follow the colour of the habit in the light, dark and pure black themes.

## 5. Create and edit habit

- A rounded header with the badge and a large title.
- A **Yes/No and Measurable selector**. It can also change the type of an existing habit (`EditHabitForm.withType`); a
  measurable habit only has a daily, weekly or monthly frequency, so another frequency becomes every day.
- **Colour:** four swatches (the card opens the whole palette of 20 when tapped) and a button for the palette.
- Rounded fields; **Name takes the focus** when creating a habit.
- Measurable habits add the unit, the target and a target type selector (at least / at most).
- Frequency for Yes/No habits is a row of chips: **Every day, Per week and Custom**; Custom opens the previous list of
  frequencies.
- **Reminder** is a card with a switch. The weekday toggles (round) are shown under it only when a reminder is active.

## 6. Habit detail

The screen is a header and a column of cards (`ui/detail`). Score, history, weekday, calendar, target and best streaks are
drawn in Compose from the same card states of the core (`ui/detail/charts`). The core charts stay for the widgets and iOS.

- **Header:** tones of the habit with round back, edit and menu buttons, the icon and name, the question and pills for the
  frequency and the reminder.
- **Overview:** a large card in the accent colour with the score and a ring, and cards with the change of the last month,
  of the last year and the total.
- **Score:** a line over an area with a bubble for the value; the axis labels are evenly spaced
  (`SCORE_AXIS_SPACING_DP = 40.3`).
- **History:** rounded bars scrolling back in time; the tallest bar fills 62.5% of the chart (`TALLEST_BAR_FRACTION`).
- **Frequency (completions by weekday):** horizontal pill bars with the count at the end; the day with most completions
  has the full accent colour, the others `muted`.
- **Calendar:** round dots by week, scrolling back in time; **days are changed with the EDIT button**, not by tapping dots.
- Charts that scroll share `ChartScroll` (`rememberChartScroll`, `chartScrollable`).

### 6.1 Target

`TargetBarsChart` with the rules of `TargetModel`:

- One row per interval (Today, Week, Month, Quarter, Year): a 14sp label column (71.7 wide) and a **pill track of 44
  height** in `surfaceContainerHighest`, with 8 between rows.
- The fill uses the habit `accent`, inset 1.5, with a width of `min(1, value / target)` of the track and **never narrower than 41**.
  A row with nothing reached has no fill.
- The number is 16sp semibold, **centred on the whole track**, and shows **what is left** to reach the target
  (`max(0, target - value)`). It takes `onAccent` when the fill reaches the middle of the track and `onSurface` otherwise.

### 6.2 Best streaks

`StreakBarsChart` with the rules of `StreakModel`:

- Up to 10 centred pills of **36 height**, 8 apart. The width is its share of the longest streak
  (`STREAK_LONGEST_WIDTH_DP = 147`), **at least 41** so the number fits.
- The number of days is 16sp bold inside the pill. The first and the last day are 12sp, 8 outside the pill, formatted with
  the medium date format of the language of the device.
- **Three tones**, by length against the longest: 100% `accent` with `onAccent`; 50% or more `muted` with `onContainer`;
  the rest `surfaceContainerHighest` with `onSurfaceVariant`. (The old chart had a fourth step at 80%; the mockups show three.)
- The dates are left out when a pill leaves less than 64 at each side.

## 7. Where the code is

| Area | Files (under `salem-ui/src/commonMain/kotlin/io/github/crisacm/salem/ui`) |
|---|---|
| Colour scheme | `compose/ExpressiveScheme.kt` |
| Habit tones and icons | `list/HabitTones.kt`, `list/HabitIcons.kt` |
| List | `list/*`, host in `salem-android` `activities/habits/list/ListHabitsHost.kt` |
| Create and edit | `edit/EditHabitScreen.kt`, `edit/EditComponents.kt`; form logic in core `EditHabitForm` |
| Detail cards | `detail/*Cards.kt`, `detail/DetailComponents.kt`, `detail/StreakTargetOverviewCards.kt` |
| Detail charts and models | `detail/charts/*` |

## 8. Known deviations and alternatives

| Difference from the mockups | Why | Alternative |
|---|---|---|
| System font | The font of the mockups is not bundled | Bundle the font in `SalemFonts` |
| Icons come from a pool | Habits store no icon | Add an icon column with a migration and a picker |
| Calendar days change with EDIT | Dots are too small to tap safely | Make the dots tappable |
| Dates of the best streaks follow the device language | They use the locale format | Fix one format for all languages |
| Text colour on the middle streak pill is inferred from the mockup | Only two colours were measured | Use `onContainer` always (current) or compute it by luminance |
| New strings (`new_habit`, `yes_no_option`, `per_week`, `custom`, `completions_by_weekday`) exist only in English | Translations are not hand-edited | Run the translation process |

## 9. How to check a change

```
./gradlew :salem-core:jvmTest :salem-ui:desktopTest ktlintCheck :salem-android:assembleDebug
```

Then compare each screen on a device with its mockups in light, dark and pure black. The layout numbers above were
measured on captures and the tests cover the pure models (`ChartModelsTest`, `HabitTonesTest`), not the final pixels, so
the comparison on a device is the real check.

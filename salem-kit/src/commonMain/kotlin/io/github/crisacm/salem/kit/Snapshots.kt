/*
 * Copyright (C) 2016-2025 Álinson Santos Xavier <git@axavier.org>
 *
 * This file is part of Loop Habit Tracker.
 *
 * Loop Habit Tracker is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Loop Habit Tracker is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package io.github.crisacm.salem.kit

/** One checkmark of a habit on the list. [value] uses the `Entry` scale; [number] is the amount of a numerical habit. */
class DayCell(val day: Int, val value: Int, val number: Double, val notes: String)

/** One habit on the list, with one [DayCell] per visible day. */
class HabitRow(
    val id: Long,
    val name: String,
    val question: String,
    val colorArgb: Long,
    val palette: Int,
    val numerical: Boolean,
    val archived: Boolean,
    val score: Int,
    val unit: String,
    val target: Double,
    val cells: List<DayCell>
)

/** The habit list as SwiftUI draws it. [days] are the columns, oldest first unless the day order is reversed. */
class ListSnapshot(
    val today: Int,
    val days: List<Int>,
    val rows: List<HabitRow>,
    val showArchived: Boolean,
    val showCompleted: Boolean,
    val primaryOrder: String,
    val secondaryOrder: String,
    val reversed: Boolean,
    val skipEnabled: Boolean,
    val questionMarks: Boolean,
    val shortToggle: Boolean,
    val firstRun: Boolean
)

/** The create and edit form. [id] is -1 for a new habit. [reminderHour] below zero means no reminder. */
class EditorSnapshot(
    val id: Long,
    val type: Int,
    val name: String,
    val question: String,
    val notes: String,
    val unit: String,
    val targetText: String,
    val targetType: Int,
    val color: Int,
    val freqNum: Int,
    val freqDen: Int,
    val reminderHour: Int,
    val reminderMin: Int,
    val reminderDays: Int
)

/** The texts and numbers of the habit screen. The charts are drawn separately from [SalemSession.chartOps]. */
class DetailSnapshot(
    val id: Long,
    val name: String,
    val question: String,
    val notes: String,
    val colorArgb: Long,
    val palette: Int,
    val numerical: Boolean,
    val archived: Boolean,
    val frequency: String,
    val reminder: String,
    val unit: String,
    val target: Double,
    val targetType: Int,
    val scorePercent: Int,
    val scoreMonth: String,
    val scoreYear: String,
    val total: Long,
    val bestStreak: Int,
    val scoreSpinner: Int,
    val barSpinner: Int,
    val targetIntervals: List<Int>
)

/** A point of the score line: [day] counts from 2000-01-01 and [value] goes from 0 to 1. */
class ScorePoint(val day: Int, val value: Double)

/** A bar of the history: the first [day] of its week or month and the amount, in units, done in it. */
class BarPoint(val day: Int, val value: Double)

/** The completions on a day of the week, [day] being 0 for Sunday. */
class WeekdayTotal(val day: Int, val total: Int)

/** A streak from [start] to [end] (days since 2000-01-01) that lasted [length] days. */
class StreakItem(val start: Int, val end: Int, val length: Int)

/** What a period (1 day, 7, 30, 91 or 365) has reached out of its target; [remaining] is what is left, written as the core writes it. */
class TargetRow(val interval: Int, val value: Double, val target: Double, val remaining: String)

/** How a day looks in the calendar: [state] is 0 missed, 1 done, 2 soft (done by the frequency or only partly), 3 skipped. */
class CalendarDay(val day: Int, val state: Int)

/**
 * The data of the charts of the habit screen, the same the core gives Android. The score and the bars come grouped by week
 * and by month, oldest first; the calendar comes newest first, starting with today.
 */
class DetailCharts(
    val today: Int,
    val scoresByWeek: List<ScorePoint>,
    val scoresByMonth: List<ScorePoint>,
    val barsByWeek: List<BarPoint>,
    val barsByMonth: List<BarPoint>,
    val weekdays: List<WeekdayTotal>,
    val streaks: List<StreakItem>,
    val targets: List<TargetRow>,
    val calendar: List<CalendarDay>
)

/** One day in the history editor. */
class HistoryDay(val day: Int, val value: Int, val number: Double, val notes: String)

/** The switches and choices of Settings. [firstWeekday] is 0 for Sunday. [theme] is 0 automatic, 1 dark, 2 light. */
class SettingsSnapshot(
    val shortToggle: Boolean,
    val midnightDelay: Boolean,
    val skip: Boolean,
    val unknown: Boolean,
    val reverseDays: Boolean,
    val pureBlack: Boolean,
    val disableAnimation: Boolean,
    val stickyNotifications: Boolean,
    val developer: Boolean,
    val firstWeekday: Int,
    val theme: Int,
    val showArchived: Boolean,
    val showCompleted: Boolean
)

/** Which chart [SalemSession.chartOps] should draw. */
object ChartCard {
    const val HISTORY = 0
    const val SCORE = 1
    const val FREQUENCY = 2
    const val STREAK = 3
    const val TARGET = 4
    const val BAR = 5
}

/** Which Settings switch [SalemSession.setSwitch] changes. */
object SettingKey {
    const val SHORT_TOGGLE = 0
    const val MIDNIGHT_DELAY = 1
    const val SKIP = 2
    const val UNKNOWN = 3
    const val REVERSE_DAYS = 4
    const val PURE_BLACK = 5
    const val DISABLE_ANIMATION = 6
    const val STICKY_NOTIFICATIONS = 7
    const val DEVELOPER = 8
}

/** What only the iOS process can do. Tests leave these empty. */
class SessionHooks(
    val databasePath: String = "",
    val reschedule: () -> Unit = {},
    val onLaunch: () -> Unit = {},
    val onForeground: () -> Unit = {},
    val onBackground: () -> Unit = {},
    val hasReminders: () -> Boolean = { false },
    val importFile: (String) -> Int = { IMPORT_FAILED }
) {
    companion object {
        const val IMPORT_OK = 0
        const val IMPORT_UNRECOGNIZED = 1
        const val IMPORT_FAILED = 2
    }
}

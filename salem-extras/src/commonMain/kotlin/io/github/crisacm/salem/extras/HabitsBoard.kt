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

package io.github.crisacm.salem.extras

import io.github.crisacm.salem.core.DATABASE_VERSION
import io.github.crisacm.salem.core.commands.CreateRepetitionCommand
import io.github.crisacm.salem.core.models.Entry
import io.github.crisacm.salem.core.models.Habit
import io.github.crisacm.salem.core.models.HabitList
import io.github.crisacm.salem.core.models.HabitMatcher
import io.github.crisacm.salem.core.models.HabitType
import io.github.crisacm.salem.core.models.sqlite.SQLModelFactory
import io.github.crisacm.salem.core.ui.views.LightTheme
import io.github.crisacm.salem.platform.io.Database
import io.github.crisacm.salem.platform.io.getVersion
import io.github.crisacm.salem.platform.time.DayOfWeek
import io.github.crisacm.salem.platform.time.LocalDate
import io.github.crisacm.salem.platform.time.setFirstWeekday
import io.github.crisacm.salem.platform.time.setToday
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * A habit as a widget or a shortcut shows it for today. [value] is the entry of the day (see `Entry`: -1 unknown, 0 no,
 * 1 and 2 yes, 3 skipped) and [number] the amount of a numerical habit (zero for the others). [colorArgb] is the colour
 * of the habit in the light theme, as 0xAARRGGBB.
 */
class BoardHabit(
    val id: Long,
    val name: String,
    val colorArgb: Long,
    val isNumerical: Boolean,
    val value: Int,
    val number: Double,
    val isDone: Boolean,
    val target: Double,
    val unit: String,
    val scorePercent: Int
)

/** What the board has for today. [needsApp] is true when the database is not the version of this build: the app must be opened first. */
class BoardState(val needsApp: Boolean, val habits: List<BoardHabit>)

/**
 * The habits of today and the changes a widget or a shortcut makes to them, on the database that the app shares with
 * its extensions. It reuses the commands and the rules of the core, never migrates the database (that is the work of
 * the app) and reads everything again on every call, as a short-lived process does. [date] is today (the day changes
 * at the hour the app chose; it is read once per call) and [firstWeekday] the first day of the week of the settings.
 * [onChanged] is given the habit a change touched (already recomputed), so the platform can remove its delivered
 * notification and move the reminder of today.
 *
 * The public functions are `@Throws(Exception::class)`: on iOS an exception that is not declared ends the process of the
 * extension, while a declared one reaches Swift as an error that a widget or a shortcut can answer. Call [close] when done.
 */
class HabitsBoard(
    private val database: Database,
    private val date: () -> LocalDate,
    private val firstWeekday: DayOfWeek = DayOfWeek.SUNDAY,
    private val isSkipEnabled: Boolean = false,
    private val areQuestionMarksEnabled: Boolean = false,
    private val onChanged: (Habit) -> Unit = {}
) {
    /**
     * Whether the database is the version of this build, which is what [today] reports as `needsApp`. It is one query,
     * where [today] computes every habit; the changes below return null both for that and for an unknown habit.
     */
    @Throws(Exception::class)
    fun isReady(): Boolean = database.getVersion() == DATABASE_VERSION

    @Throws(Exception::class)
    fun today(): BoardState {
        val day = date()
        val list = open(day) ?: return BoardState(needsApp = true, habits = emptyList())
        return BoardState(needsApp = false, habits = active(list).map { show(it, day) })
    }

    /** Marks the habit as done: a yes, or the target for a numerical habit (an amount above it is kept). */
    @Throws(Exception::class)
    fun check(id: Long): BoardHabit? = change(id) { habit, entry, _ ->
        if (habit.isNumerical()) max(entry.value, target(habit)) else Entry.YES_MANUAL
    }

    /** Marks the habit as not done: a no, or zero for a numerical habit. */
    @Throws(Exception::class)
    fun uncheck(id: Long): BoardHabit? = change(id) { _, _, _ -> Entry.NO }

    /** The next value of the cycle of the app (yes, no, and skip or unknown when they are on); a numerical habit goes between zero and its target. */
    @Throws(Exception::class)
    fun toggle(id: Long): BoardHabit? = change(id) { habit, entry, day ->
        if (habit.isNumerical()) {
            if (habit.isCompletedOn(day)) 0 else target(habit)
        } else {
            Entry.nextToggleValue(entry.value, isSkipEnabled, areQuestionMarksEnabled)
        }
    }

    /** One more unit of a numerical habit; for the others, the same as [check]. */
    @Throws(Exception::class)
    fun increment(id: Long): BoardHabit? = change(id) { habit, entry, _ ->
        if (habit.isNumerical()) max(entry.value, 0) + UNIT else Entry.YES_MANUAL
    }

    /** One unit less of a numerical habit, never below zero; for the others, the same as [uncheck]. */
    @Throws(Exception::class)
    fun decrement(id: Long): BoardHabit? = change(id) { habit, entry, _ ->
        if (habit.isNumerical()) max(entry.value - UNIT, 0) else Entry.NO
    }

    /** Closes the database; the board cannot be used afterwards. */
    fun close() = database.close()

    private fun change(id: Long, newValue: (Habit, Entry, LocalDate) -> Int): BoardHabit? {
        val day = date()
        val list = open(day) ?: return null
        val habit = active(list).firstOrNull { it.id == id } ?: return null
        val entry = habit.originalEntries.get(day)
        CreateRepetitionCommand(list, habit, day, newValue(habit, entry, day), entry.notes).run()
        onChanged(habit)
        return show(habit, day)
    }

    // The database must be of the version of this build; the habits are computed here, as the app does at start
    private fun open(day: LocalDate): HabitList? {
        if (!isReady()) return null
        setToday(day)
        setFirstWeekday(firstWeekday)
        val list = SQLModelFactory(database).buildHabitList()
        for (habit in list) habit.recompute()
        return list
    }

    private fun active(list: HabitList): List<Habit> = list.getFiltered(HabitMatcher()).toList()

    private fun target(habit: Habit): Int = (habit.targetValue * UNIT).roundToInt()

    private fun Habit.isNumerical() = type == HabitType.NUMERICAL

    private fun show(habit: Habit, day: LocalDate): BoardHabit {
        val entry = habit.originalEntries.get(day)
        val color = LightTheme().color(habit.color)
        val numerical = habit.isNumerical()
        return BoardHabit(
            id = habit.id!!,
            name = habit.name,
            colorArgb = argb(color.red, color.green, color.blue),
            isNumerical = numerical,
            value = entry.value,
            number = if (numerical) max(entry.value, 0).toDouble() / UNIT else 0.0,
            isDone = habit.isCompletedOn(day),
            target = if (numerical) habit.targetValue else 0.0,
            unit = habit.unit,
            scorePercent = (habit.scores.get(day).value * 100).roundToInt().coerceIn(0, 100)
        )
    }

    private fun argb(red: Double, green: Double, blue: Double): Long =
        0xFF000000L or (channel(red) shl 16) or (channel(green) shl 8) or channel(blue)

    private fun channel(value: Double): Long = (value * 255).roundToInt().toLong()

    private companion object {
        // The amount of a numerical habit is stored times a thousand
        const val UNIT = 1000
    }
}

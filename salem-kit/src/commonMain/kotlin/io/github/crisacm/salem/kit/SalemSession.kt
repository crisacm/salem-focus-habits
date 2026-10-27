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

import io.github.crisacm.salem.core.commands.ArchiveHabitsCommand
import io.github.crisacm.salem.core.commands.ChangeHabitColorCommand
import io.github.crisacm.salem.core.commands.Command
import io.github.crisacm.salem.core.commands.CommandRunner
import io.github.crisacm.salem.core.commands.CreateRepetitionCommand
import io.github.crisacm.salem.core.commands.DeleteHabitsCommand
import io.github.crisacm.salem.core.commands.UnarchiveHabitsCommand
import io.github.crisacm.salem.core.io.ExternalChanges
import io.github.crisacm.salem.core.io.HabitsCSVExporter
import io.github.crisacm.salem.core.models.Entry
import io.github.crisacm.salem.core.models.Habit
import io.github.crisacm.salem.core.models.HabitList
import io.github.crisacm.salem.core.models.HabitMatcher
import io.github.crisacm.salem.core.models.HabitType
import io.github.crisacm.salem.core.models.ModelFactory
import io.github.crisacm.salem.core.models.NumericalHabitType
import io.github.crisacm.salem.core.models.PaletteColor
import io.github.crisacm.salem.core.models.WeekdayList
import io.github.crisacm.salem.core.models.sqlite.SQLiteHabitList
import io.github.crisacm.salem.core.preferences.Preferences
import io.github.crisacm.salem.core.ui.ThemeSwitcher
import io.github.crisacm.salem.core.ui.screens.habits.edit.EditHabitForm
import io.github.crisacm.salem.core.ui.screens.habits.edit.HabitEditorSaver
import io.github.crisacm.salem.core.ui.screens.habits.edit.SaveResult
import io.github.crisacm.salem.core.ui.screens.habits.show.ShowHabitPresenter
import io.github.crisacm.salem.core.ui.views.DarkTheme
import io.github.crisacm.salem.core.ui.views.LightTheme
import io.github.crisacm.salem.platform.gui.DrawOp
import io.github.crisacm.salem.platform.gui.toArgb
import io.github.crisacm.salem.platform.io.Database
import io.github.crisacm.salem.platform.time.LocalDate
import io.github.crisacm.salem.platform.time.LocalDateFormatter
import io.github.crisacm.salem.platform.time.computeToday
import io.github.crisacm.salem.platform.time.getToday
import io.github.crisacm.salem.platform.time.setFirstWeekday
import io.github.crisacm.salem.platform.time.setToday
import kotlinx.coroutines.runBlocking
import kotlin.io.encoding.Base64
import kotlin.math.roundToInt

/**
 * What the SwiftUI app calls. It runs the commands and reads the models of the core, and returns plain snapshots.
 * [onHabitsChanged] tells the shell to reload the widget. Call [close] when the app is done with the database.
 */
class SalemSession(
    private val habitList: HabitList,
    private val modelFactory: ModelFactory,
    private val commandRunner: CommandRunner,
    val preferences: Preferences,
    private val database: Database?,
    private val dateFormatter: LocalDateFormatter = EnglishDateFormatter(),
    private val onHabitsChanged: () -> Unit = {},
    private val clock: () -> LocalDate = { computeToday(preferences.midnightDelayHours, 0) }
) {
    private val listeners = mutableListOf<() -> Unit>()
    private val external = database?.let { ExternalChanges(it) }
    private val charts = ChartRecorder(preferences, dateFormatter, ::theme)
    var hooks = SessionHooks()
    var systemIsDark: Boolean = false
    var pendingHabitId: Long = -1

    init {
        commandRunner.addListener(object : CommandRunner.Listener {
            override fun onCommandFinished(command: Command) = changed()
        })
        applyClock()
    }

    fun addListener(listener: () -> Unit) {
        listeners += listener
    }

    /** Increments the launch count and returns whether this is the first launch (the intro should show). */
    fun startup(): Boolean {
        val first = preferences.isFirstRun
        preferences.incrementLaunchCount()
        if (first) {
            preferences.isFirstRun = false
            preferences.updateLastHint(-1, getToday())
        }
        hooks.onLaunch()
        return first
    }

    fun foreground() {
        applyClock()
        hooks.onForeground()
        if (reloadIfChangedElsewhere()) return
        changed()
    }

    fun background() = hooks.onBackground()

    fun databasePath(): String = hooks.databasePath

    fun hasReminders(): Boolean = hooks.hasReminders()

    fun rescheduleReminders() = hooks.reschedule()

    /** 0 imported, 1 not a known file, 2 failed. */
    @Throws(Exception::class)
    fun importFile(path: String): Int = hooks.importFile(path)

    @Throws(Exception::class)
    fun exportCsvBase64(): String {
        val bytes = runBlocking { HabitsCSVExporter(habitList, habitList.toList()).writeArchive() }
        return Base64.encode(bytes)
    }

    fun repairDatabase() {
        habitList.repair()
        for (habit in habitList) habit.recompute()
        changed()
    }

    fun listSnapshot(dayCount: Int): ListSnapshot {
        applyClock()
        habitList.primaryOrder = preferences.defaultPrimaryOrder
        habitList.secondaryOrder = preferences.defaultSecondaryOrder
        val count = dayCount.coerceIn(1, 60)
        val today = getToday()
        var days = (0 until count).map { today.minus(count - 1 - it).daysSince2000 }
        if (preferences.isCheckmarkSequenceReversed) days = days.reversed()
        val matcher = HabitMatcher(
            isArchivedAllowed = preferences.showArchived,
            isCompletedAllowed = preferences.showCompleted
        )
        val rows = habitList.getFiltered(matcher).mapNotNull { row(it, days) }
        return ListSnapshot(
            today = today.daysSince2000,
            days = days,
            rows = rows,
            showArchived = preferences.showArchived,
            showCompleted = preferences.showCompleted,
            primaryOrder = preferences.defaultPrimaryOrder.name,
            secondaryOrder = preferences.defaultSecondaryOrder.name,
            reversed = preferences.isCheckmarkSequenceReversed,
            skipEnabled = preferences.isSkipEnabled,
            questionMarks = preferences.areQuestionMarksEnabled,
            shortToggle = preferences.isShortToggleEnabled,
            firstRun = preferences.isFirstRun
        )
    }

    fun toggle(id: Long, day: Int) {
        val habit = habitList.getById(id) ?: return
        val date = LocalDate(day)
        val shown = habit.computedEntries.get(date)
        val original = habit.originalEntries.get(date)
        val value = if (habit.isNumerical) {
            if (habit.isCompletedOn(date)) 0 else (habit.targetValue * 1000).roundToInt()
        } else {
            Entry.nextToggleValue(shown.value, preferences.isSkipEnabled, preferences.areQuestionMarksEnabled)
        }
        commandRunner.runAndWait(CreateRepetitionCommand(habitList, habit, date, value, original.notes))
    }

    fun setEntry(id: Long, day: Int, value: Int, notes: String) {
        val habit = habitList.getById(id) ?: return
        commandRunner.runAndWait(CreateRepetitionCommand(habitList, habit, LocalDate(day), value, notes))
    }

    fun reorder(fromId: Long, toId: Long) {
        val from = habitList.getById(fromId) ?: return
        val to = habitList.getById(toId) ?: return
        habitList.reorder(from, to)
        changed()
    }

    fun archive(ids: List<Long>) = runOn(ids) { ArchiveHabitsCommand(habitList, it) }

    fun unarchive(ids: List<Long>) = runOn(ids) { UnarchiveHabitsCommand(habitList, it) }

    fun delete(ids: List<Long>) = runOn(ids) { DeleteHabitsCommand(habitList, it) }

    fun setColor(ids: List<Long>, palette: Int) {
        runOn(ids) { ChangeHabitColorCommand(habitList, it, PaletteColor(palette.coerceIn(0, 19))) }
    }

    fun setShowArchived(show: Boolean) {
        preferences.showArchived = show
        changed()
    }

    fun setShowCompleted(show: Boolean) {
        preferences.showCompleted = show
        changed()
    }

    fun cyclePrimaryOrder() {
        val values = HabitList.Order.entries
        val index = values.indexOf(preferences.defaultPrimaryOrder).coerceAtLeast(0)
        preferences.defaultPrimaryOrder = values[(index + 1) % values.size]
        changed()
    }

    fun editorSnapshot(id: Long): EditorSnapshot {
        val habit = if (id < 0) null else habitList.getById(id)
        val form = if (habit == null) EditHabitForm.new(HabitType.YES_NO) else EditHabitForm.from(habit)
        return editor(id, form)
    }

    fun blankEditor(type: Int): EditorSnapshot = editor(-1, EditHabitForm.new(HabitType.fromInt(type)))

    /** Saves the form. Returns the habit id, -1 when the name is blank, -3 when the numerical target is invalid, or -2 when nothing changed. */
    fun saveEditor(form: EditorSnapshot): Long {
        val edit = toForm(form)
        val errors = edit.validate()
        if (errors.nameBlank) return -1
        if (errors.targetBlank) return -3
        val habitId = form.id.takeIf { it >= 0 }
        if (habitId != null) {
            val original = habitList.getById(habitId)
            if (original != null && EditHabitForm.from(original) == edit.normalizedForCompare()) return -2
        }
        val result = HabitEditorSaver(modelFactory, habitList, commandRunner).saveAndWait(edit, habitId)
        return when (result) {
            SaveResult.SAVED -> habitId ?: habitList.maxOf { it.id ?: -1 }
            SaveResult.HABIT_NOT_FOUND -> -2
            SaveResult.ALREADY_SAVING -> -2
        }
    }

    fun detail(id: Long): DetailSnapshot? {
        val habit = habitList.getById(id) ?: return null
        val state = ShowHabitPresenter.buildState(habit, preferences, theme())
        val reminder = habit.reminder
        return DetailSnapshot(
            id = id,
            name = habit.name,
            question = habit.question,
            notes = habit.description,
            colorArgb = theme().color(habit.color).toArgb(),
            palette = habit.color.paletteIndex,
            numerical = habit.isNumerical,
            archived = habit.isArchived,
            frequency = "${habit.frequency.numerator}/${habit.frequency.denominator}",
            reminder = if (reminder == null) {
                ""
            } else {
                "${reminder.hour.toString().padStart(2, '0')}:${reminder.minute.toString().padStart(2, '0')}"
            },
            unit = habit.unit,
            target = habit.targetValue,
            targetType = habit.targetType.value,
            scorePercent = (state.overview.scoreToday * 100).roundToInt().coerceIn(0, 100),
            scoreMonth = signedPercent(state.overview.scoreMonthDiff),
            scoreYear = signedPercent(state.overview.scoreYearDiff),
            total = state.overview.totalCount,
            bestStreak = state.streaks.bestStreaks.maxOfOrNull { it.length } ?: 0,
            scoreSpinner = preferences.scoreCardSpinnerPosition,
            barSpinner = if (habit.isNumerical) {
                preferences.barCardNumericalSpinnerPosition
            } else {
                preferences.barCardBoolSpinnerPosition
            },
            targetIntervals = state.target.intervals
        )
    }

    fun setScoreSpinner(position: Int) {
        preferences.scoreCardSpinnerPosition = position
        changed()
    }

    fun setBarSpinner(numerical: Boolean, position: Int) {
        if (numerical) {
            preferences.barCardNumericalSpinnerPosition = position
        } else {
            preferences.barCardBoolSpinnerPosition = position
        }
        changed()
    }

    fun history(id: Long, days: Int): List<HistoryDay> {
        val habit = habitList.getById(id) ?: return emptyList()
        val today = getToday()
        return (0 until days.coerceIn(1, 400)).map { offset ->
            val date = today.minus(offset)
            val entry = habit.originalEntries.get(date)
            HistoryDay(
                day = date.daysSince2000,
                value = entry.value,
                number = if (habit.isNumerical) entry.value.coerceAtLeast(0) / 1000.0 else 0.0,
                notes = entry.notes
            )
        }
    }

    fun chartOps(id: Long, card: Int, width: Double, height: Double, offset: Int, labels: List<String>): List<DrawOp> {
        val habit = habitList.getById(id) ?: return emptyList()
        return charts.draw(habit, card, width, height, offset, labels)
    }

    fun chartHeight(id: Long, card: Int): Double {
        val habit = habitList.getById(id) ?: return 0.0
        return charts.height(habit, card)
    }

    fun chartTap(id: Long, card: Int, x: Double, y: Double, width: Double, height: Double, offset: Int): Int {
        val habit = habitList.getById(id) ?: return -1
        return charts.tap(habit, card, x, y, width, height, offset)
    }

    fun settingsSnapshot() = SettingsSnapshot(
        shortToggle = preferences.isShortToggleEnabled,
        midnightDelay = preferences.isMidnightDelayEnabled,
        skip = preferences.isSkipEnabled,
        unknown = preferences.areQuestionMarksEnabled,
        reverseDays = preferences.isCheckmarkSequenceReversed,
        pureBlack = preferences.isPureBlackEnabled,
        disableAnimation = preferences.isConfettiAnimationDisabled,
        stickyNotifications = preferences.shouldMakeNotificationsSticky(),
        developer = preferences.isDeveloper,
        firstWeekday = preferences.firstWeekday.daysSinceSunday,
        theme = preferences.theme,
        showArchived = preferences.showArchived,
        showCompleted = preferences.showCompleted
    )

    fun setSwitch(key: Int, value: Boolean) {
        when (key) {
            SettingKey.SHORT_TOGGLE -> preferences.isShortToggleEnabled = value
            SettingKey.MIDNIGHT_DELAY -> preferences.isMidnightDelayEnabled = value
            SettingKey.SKIP -> preferences.isSkipEnabled = value
            SettingKey.UNKNOWN -> preferences.areQuestionMarksEnabled = value
            SettingKey.REVERSE_DAYS -> preferences.isCheckmarkSequenceReversed = value
            SettingKey.PURE_BLACK -> preferences.isPureBlackEnabled = value
            SettingKey.DISABLE_ANIMATION -> preferences.isConfettiAnimationDisabled = value
            SettingKey.STICKY_NOTIFICATIONS -> preferences.setNotificationsSticky(value)
            SettingKey.DEVELOPER -> preferences.isDeveloper = value
        }
        if (key == SettingKey.MIDNIGHT_DELAY) applyClock()
        changed()
    }

    fun setFirstWeekday(day: Int) {
        preferences.firstWeekday = io.github.crisacm.salem.platform.time.DayOfWeek.entries[day.coerceIn(0, 6)]
        applyClock()
        changed()
    }

    fun setTheme(theme: Int) {
        preferences.theme = theme.coerceIn(0, 2)
        changed()
    }

    fun useDark(): Boolean = when (preferences.theme) {
        ThemeSwitcher.THEME_DARK -> true
        ThemeSwitcher.THEME_LIGHT -> false
        else -> systemIsDark
    }

    /** Reads the habits again when a widget or a shortcut wrote the database. Returns whether it did. */
    fun reloadIfChangedElsewhere(): Boolean {
        if (external?.hasChanged() != true) return false
        (habitList as? SQLiteHabitList)?.reload()
        for (habit in habitList) habit.recompute()
        hooks.reschedule()
        changed()
        return true
    }

    fun close() {
        database?.close()
    }

    private fun theme() = if (useDark()) DarkTheme() else LightTheme()

    private fun applyClock() {
        setToday(clock())
        setFirstWeekday(preferences.firstWeekday)
    }

    private fun changed() {
        onHabitsChanged()
        listeners.toList().forEach { it() }
    }

    private fun runOn(ids: List<Long>, command: (List<Habit>) -> Command) {
        val habits = ids.mapNotNull { habitList.getById(it) }
        if (habits.isEmpty()) return
        commandRunner.runAndWait(command(habits))
    }

    private fun row(habit: Habit, days: List<Int>): HabitRow? {
        val id = habit.id ?: return null
        val cells = days.map { day ->
            val date = LocalDate(day)
            val entry = habit.computedEntries.get(date)
            val notes = habit.originalEntries.get(date).notes
            DayCell(
                day = day,
                value = entry.value,
                number = if (habit.isNumerical) entry.value.coerceAtLeast(0) / 1000.0 else 0.0,
                notes = notes
            )
        }
        val today = getToday()
        return HabitRow(
            id = id,
            name = habit.name,
            question = habit.question,
            colorArgb = theme().color(habit.color).toArgb(),
            palette = habit.color.paletteIndex,
            numerical = habit.isNumerical,
            archived = habit.isArchived,
            score = (habit.scores.get(today).value * 100).roundToInt().coerceIn(0, 100),
            unit = habit.unit,
            target = habit.targetValue,
            cells = cells
        )
    }

    private fun editor(id: Long, form: EditHabitForm) = EditorSnapshot(
        id = id,
        type = form.type.value,
        name = form.name,
        question = form.question,
        notes = form.notes,
        unit = form.unit,
        targetText = form.targetText,
        targetType = form.targetType.value,
        color = form.color.paletteIndex,
        freqNum = form.freqNum,
        freqDen = form.freqDen,
        reminderHour = form.reminderHour,
        reminderMin = form.reminderMin,
        reminderDays = form.reminderDays.toPacked()
    )

    private fun EditHabitForm.normalizedForCompare() = copy(
        name = name.trim(),
        question = question.trim(),
        notes = notes.trim(),
        unit = unit.trim(),
        targetText = targetText.trim()
    )

    private fun toForm(snapshot: EditorSnapshot) = EditHabitForm(
        type = HabitType.fromInt(snapshot.type),
        name = snapshot.name,
        question = snapshot.question,
        notes = snapshot.notes,
        unit = snapshot.unit,
        targetText = snapshot.targetText,
        targetType = NumericalHabitType.fromInt(snapshot.targetType),
        color = PaletteColor(snapshot.color.coerceIn(0, 19)),
        freqNum = snapshot.freqNum.coerceAtLeast(1),
        freqDen = snapshot.freqDen.coerceAtLeast(1),
        reminderHour = snapshot.reminderHour,
        reminderMin = snapshot.reminderMin,
        reminderDays = WeekdayList(snapshot.reminderDays)
    )
}

fun openHabits(preferences: Preferences, habitList: HabitList, seed: () -> Unit = {}) {
    setToday(computeToday(preferences.midnightDelayHours, 0))
    setFirstWeekday(preferences.firstWeekday)
    seed()
    for (habit in habitList) habit.recompute()
}

private fun WeekdayList.toPacked(): Int {
    val days = toArray()
    var packed = 0
    for (index in days.indices) if (days[index]) packed = packed or (1 shl index)
    return packed
}

private fun signedPercent(diff: Float): String {
    val percent = kotlin.math.abs(diff * 100).roundToInt()
    return if (diff >= 0) "+$percent%" else "−$percent%"
}

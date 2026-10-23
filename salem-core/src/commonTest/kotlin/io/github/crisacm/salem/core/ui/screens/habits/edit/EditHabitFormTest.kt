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

package io.github.crisacm.salem.core.ui.screens.habits.edit

import io.github.crisacm.salem.core.BaseUnitTest
import io.github.crisacm.salem.core.models.Frequency
import io.github.crisacm.salem.core.models.HabitType
import io.github.crisacm.salem.core.models.NumericalHabitType
import io.github.crisacm.salem.core.models.PaletteColor
import io.github.crisacm.salem.core.models.Reminder
import io.github.crisacm.salem.core.models.WeekdayList
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class EditHabitFormTest : BaseUnitTest() {
    private fun yesNo(name: String = "Wake up") = EditHabitForm(HabitType.YES_NO, name = name)

    private fun numerical(name: String = "Run", target: String = "5") =
        EditHabitForm(HabitType.NUMERICAL, name = name, targetText = target)

    @Test
    fun aNewFormStartsWithTheDefaults() {
        val form = EditHabitForm.new(HabitType.NUMERICAL)
        assertEquals(HabitType.NUMERICAL, form.type)
        assertEquals("", form.name)
        assertEquals(PaletteColor(11), form.color)
        assertEquals(1, form.freqNum)
        assertEquals(1, form.freqDen)
        assertEquals(NumericalHabitType.AT_LEAST, form.targetType)
        assertFalse(form.hasReminder)
        assertEquals(WeekdayList.EVERY_DAY, form.reminderDays)
    }

    @Test
    fun aBlankNameIsRejected() {
        assertTrue(yesNo("").validate().nameBlank)
        assertTrue(yesNo("   ").validate().nameBlank)
        assertFalse(yesNo("Wake up").validate().nameBlank)
    }

    @Test
    fun aYesNoHabitIgnoresTheTarget() {
        val errors = yesNo().copy(targetText = "").validate()
        assertFalse(errors.targetBlank)
        assertTrue(errors.isValid)
    }

    @Test
    fun aNumericalHabitNeedsAValidNumberAsTarget() {
        assertTrue(numerical(target = "").validate().targetBlank)
        assertTrue(numerical(target = "  ").validate().targetBlank)
        assertTrue(numerical(target = ".").validate().targetBlank)
        assertTrue(numerical(target = "abc").validate().targetBlank)
        assertFalse(numerical(target = "5").validate().targetBlank)
        assertFalse(numerical(target = "2.5").validate().targetBlank)
        assertFalse(numerical(target = "2,5").validate().targetBlank)
        assertTrue(numerical().validate().isValid)
    }

    @Test
    fun aTargetThatIsNotAFiniteNumberIsRejected() {
        assertTrue(numerical(target = "NaN").validate().targetBlank)
        assertTrue(numerical(target = "Infinity").validate().targetBlank)
        assertTrue(numerical(target = "-Infinity").validate().targetBlank)
        assertTrue(numerical(target = "1e999").validate().targetBlank)
        assertTrue(numerical(target = "1,000.5").validate().targetBlank)
        assertFalse(numerical(target = "1e3").validate().targetBlank)
    }

    @Test
    fun applyingAYesNoFormFillsTheHabit() {
        val habit = fixtures.createEmptyHabit()
        yesNo("  Wake up  ").copy(
            question = " Did you? ",
            notes = " notes ",
            color = PaletteColor(3),
            freqNum = 3,
            freqDen = 7
        ).applyTo(habit)
        assertEquals("Wake up", habit.name)
        assertEquals("Did you?", habit.question)
        assertEquals("notes", habit.description)
        assertEquals(PaletteColor(3), habit.color)
        assertEquals(Frequency(3, 7), habit.frequency)
        assertEquals(HabitType.YES_NO, habit.type)
        assertNull(habit.reminder)
    }

    @Test
    fun applyingAYesNoFormKeepsTheTargetOfTheHabit() {
        val habit = fixtures.createEmptyHabit()
        habit.targetValue = 9.0
        habit.unit = "km"
        yesNo().applyTo(habit)
        assertEquals(9.0, habit.targetValue, 0.0)
        assertEquals("km", habit.unit)
    }

    @Test
    fun applyingANumericalFormFillsTheTarget() {
        val habit = fixtures.createEmptyHabit()
        numerical(target = "2,5").copy(unit = " km ", targetType = NumericalHabitType.AT_MOST).applyTo(habit)
        assertEquals(HabitType.NUMERICAL, habit.type)
        assertEquals(2.5, habit.targetValue, 0.0)
        assertEquals("km", habit.unit)
        assertEquals(NumericalHabitType.AT_MOST, habit.targetType)
    }

    @Test
    fun theReminderIsOnlyAppliedWhenThereIsAnHour() {
        val habit = fixtures.createEmptyHabit()
        val days = WeekdayList(5)
        yesNo().copy(reminderHour = 8, reminderMin = 30, reminderDays = days).applyTo(habit)
        assertEquals(Reminder(8, 30, days), habit.reminder)
        yesNo().applyTo(habit)
        assertNull(habit.reminder)
    }

    @Test
    fun clearingTheReminderResetsTheDays() {
        val form = yesNo().copy(reminderHour = 8, reminderMin = 30, reminderDays = WeekdayList(5)).clearReminder()
        assertFalse(form.hasReminder)
        assertEquals(-1, form.reminderMin)
        assertEquals(WeekdayList.EVERY_DAY, form.reminderDays)
    }

    @Test
    fun aFormBuiltFromAHabitAppliesBackToTheSameValues() {
        val source = fixtures.createEmptyHabit()
        source.name = "Read"
        source.question = "Did you read?"
        source.description = "Some notes"
        source.type = HabitType.NUMERICAL
        source.targetValue = 30.0
        source.targetType = NumericalHabitType.AT_MOST
        source.unit = "pages"
        source.color = PaletteColor(7)
        source.frequency = Frequency(2, 7)
        source.reminder = Reminder(20, 15, WeekdayList(3))
        val target = fixtures.createEmptyHabit()
        EditHabitForm.from(source).applyTo(target)
        assertEquals("Read", target.name)
        assertEquals("Did you read?", target.question)
        assertEquals("Some notes", target.description)
        assertEquals(30.0, target.targetValue, 0.0)
        assertEquals(NumericalHabitType.AT_MOST, target.targetType)
        assertEquals("pages", target.unit)
        assertEquals(PaletteColor(7), target.color)
        assertEquals(Frequency(2, 7), target.frequency)
        assertEquals(Reminder(20, 15, WeekdayList(3)), target.reminder)
    }

    @Test
    fun aFormBuiltFromAHabitWithoutReminderHasNone() {
        val form = EditHabitForm.from(fixtures.createEmptyHabit())
        assertFalse(form.hasReminder)
    }
}

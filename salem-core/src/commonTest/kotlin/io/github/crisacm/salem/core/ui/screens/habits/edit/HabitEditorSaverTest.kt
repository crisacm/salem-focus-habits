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
import io.github.crisacm.salem.core.models.HabitType
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class HabitEditorSaverTest : BaseUnitTest() {
    private lateinit var saver: HabitEditorSaver

    @BeforeTest
    override fun setUp() {
        super.setUp()
        saver = HabitEditorSaver(modelFactory, habitList, commandRunner)
    }

    @Test
    fun aNewFormCreatesTheHabit() {
        val form = EditHabitForm.new(HabitType.YES_NO).copy(name = "Meditate")

        assertEquals(SaveResult.SAVED, saver.save(form, habitId = null))

        assertEquals(1, habitList.size())
        assertEquals("Meditate", habitList.getByPosition(0).name)
    }

    @Test
    fun anExistingHabitIsUpdated() {
        val habit = fixtures.createShortHabit()
        habitList.add(habit)
        val form = EditHabitForm.from(habit).copy(name = "Renamed")

        assertEquals(SaveResult.SAVED, saver.save(form, habit.id))

        assertEquals("Renamed", habitList.getById(habit.id!!)!!.name)
        assertEquals(1, habitList.size())
    }

    @Test
    fun aMissingHabitIsReportedAndNothingIsCreated() {
        val form = EditHabitForm.new(HabitType.YES_NO).copy(name = "Ghost")

        assertEquals(SaveResult.HABIT_NOT_FOUND, saver.save(form, habitId = 12345L))

        assertEquals(0, habitList.size())
    }

    @Test
    fun aMissingHabitDoesNotBlockALaterSave() {
        val form = EditHabitForm.new(HabitType.YES_NO).copy(name = "Meditate")
        saver.save(form, habitId = 12345L)

        assertEquals(SaveResult.SAVED, saver.save(form, habitId = null))

        assertEquals(1, habitList.size())
    }

    @Test
    fun aSecondTapDoesNotSaveAgain() {
        val form = EditHabitForm.new(HabitType.YES_NO).copy(name = "Meditate")

        assertEquals(SaveResult.SAVED, saver.save(form, habitId = null))
        assertEquals(SaveResult.ALREADY_SAVING, saver.save(form, habitId = null))

        assertEquals(1, habitList.size())
    }
}

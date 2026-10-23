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
package io.github.crisacm.salem.performance

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import io.github.crisacm.salem.platform.io.begin
import io.github.crisacm.salem.platform.io.commit
import io.github.crisacm.salem.platform.time.LocalDate
import io.github.crisacm.salem.BaseAndroidTest
import io.github.crisacm.salem.core.commands.CreateHabitCommand
import io.github.crisacm.salem.core.commands.CreateRepetitionCommand
import io.github.crisacm.salem.core.models.Habit
import io.github.crisacm.salem.core.models.sqlite.SQLModelFactory
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@MediumTest
class PerformanceTest : BaseAndroidTest() {
    private var habit: Habit? = null
    override fun setUp() {
        super.setUp()
        habit = fixtures.createLongHabit()
    }

    @Ignore
    @Test(timeout = 5000)
    fun benchmarkCreateHabitCommand() {
        val db = (modelFactory as SQLModelFactory).database
        db.begin()
        for (i in 0..999) {
            val model = modelFactory.buildHabit()
            CreateHabitCommand(modelFactory, habitList, model).run()
        }
        db.commit()
    }

    @Ignore
    @Test(timeout = 5000)
    fun benchmarkCreateRepetitionCommand() {
        val db = (modelFactory as SQLModelFactory).database
        db.begin()
        val habit = fixtures.createEmptyHabit()
        var date = LocalDate(2000, 1, 1)
        for (i in 0..4999) {
            CreateRepetitionCommand(habitList, habit, date, 1, "").run()
            date = date.plus(1)
        }
        db.commit()
    }
}

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
package io.github.crisacm.salem.activities.habits.show.views

import android.view.LayoutInflater
import android.view.View
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import io.github.crisacm.salem.BaseViewTest
import io.github.crisacm.salem.R
import io.github.crisacm.salem.core.ui.screens.habits.show.views.FrequencyCardPresenter
import io.github.crisacm.salem.core.ui.views.LightTheme
import io.github.crisacm.salem.platform.time.DayOfWeek
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@MediumTest
class FrequencyCardViewTest : BaseViewTest() {
    val PATH = "habits/show/FrequencyCard/"
    private lateinit var view: FrequencyCardView

    @Before
    override fun setUp() {
        super.setUp()
        val habit = fixtures.createLongHabit()
        view = LayoutInflater
            .from(targetContext)
            .inflate(R.layout.show_habit, null)
            .findViewById<View>(R.id.frequencyCard) as FrequencyCardView
        view.setState(
            FrequencyCardPresenter.buildState(
                habit = habit,
                firstWeekday = DayOfWeek.SATURDAY,
                theme = LightTheme()
            )
        )
        measureView(view, 800f, 600f)
    }

    @Test
    fun testRender() {
        assertRenders(view, PATH + "render.png")
    }
}

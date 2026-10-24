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

package io.github.crisacm.salem.activities.habits.show

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.crisacm.salem.activities.habits.show.views.BarCardContent
import io.github.crisacm.salem.activities.habits.show.views.FrequencyCardContent
import io.github.crisacm.salem.activities.habits.show.views.HistoryCardContent
import io.github.crisacm.salem.activities.habits.show.views.OverviewCardContent
import io.github.crisacm.salem.activities.habits.show.views.ScoreCardContent
import io.github.crisacm.salem.activities.habits.show.views.StreakCardContent
import io.github.crisacm.salem.activities.habits.show.views.TargetCardContent
import io.github.crisacm.salem.core.models.Entry
import io.github.crisacm.salem.core.models.PaletteColor
import io.github.crisacm.salem.core.models.Score
import io.github.crisacm.salem.core.models.Streak
import io.github.crisacm.salem.core.ui.screens.habits.show.views.BarCardState
import io.github.crisacm.salem.core.ui.screens.habits.show.views.FrequencyCardState
import io.github.crisacm.salem.core.ui.screens.habits.show.views.HistoryCardState
import io.github.crisacm.salem.core.ui.screens.habits.show.views.OverviewCardState
import io.github.crisacm.salem.core.ui.screens.habits.show.views.ScoreCardState
import io.github.crisacm.salem.core.ui.screens.habits.show.views.StreakCardState
import io.github.crisacm.salem.core.ui.screens.habits.show.views.TargetCardState
import io.github.crisacm.salem.core.ui.views.HistoryChart
import io.github.crisacm.salem.core.ui.views.LightTheme
import io.github.crisacm.salem.platform.time.DayOfWeek
import io.github.crisacm.salem.platform.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ShowCardsComposeTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val today = LocalDate(5000)

    @Test
    fun theHistoryCardDrawsItsChartAndTheEditButtonAsks() {
        var edits = 0
        val state = HistoryCardState(
            color = PaletteColor(8),
            firstWeekday = DayOfWeek.MONDAY,
            series = List(100) { HistoryChart.Square.OFF },
            defaultSquare = HistoryChart.Square.OFF,
            notesIndicators = List(100) { false },
            theme = LightTheme(),
            today = today
        )
        composeRule.setContent { HistoryCardContent(state, onEdit = { edits++ }) }
        composeRule.onNodeWithTag("historyChart").assertIsDisplayed()
        composeRule.onNodeWithTag("historyEdit").performClick()
        assertEquals(1, edits)
    }

    @Test
    fun theBarCardShowsTheSpinnerOfItsKind() {
        val entries = List(20) { Entry(today.minus(it), Entry.YES_MANUAL) }
        val state = BarCardState(
            theme = LightTheme(),
            boolSpinnerPosition = 0,
            bucketSize = 7,
            color = PaletteColor(8),
            entries = entries,
            isNumerical = false,
            numericalSpinnerPosition = 0
        )
        composeRule.setContent { BarCardContent(state, onBoolSpinner = {}, onNumericalSpinner = {}) }
        composeRule.onNodeWithTag("barChart").assertIsDisplayed()
    }

    @Test
    fun theScoreCardDrawsItsChart() {
        val scores = List(30) { Score(today.minus(it), 0.5 + it / 100.0) }
        val state = ScoreCardState(scores, 7, 0, PaletteColor(8), LightTheme())
        composeRule.setContent { ScoreCardContent(state, onSpinner = {}) }
        composeRule.onNodeWithTag("scoreChart").assertIsDisplayed()
    }

    @Test
    fun theFrequencyCardDrawsItsChart() {
        val frequency = hashMapOf(LocalDate(today.year, today.month, 1) to arrayOf(1, 2, 3, 4, 5, 6, 7))
        val state = FrequencyCardState(PaletteColor(8), DayOfWeek.MONDAY, frequency, LightTheme(), false)
        composeRule.setContent { FrequencyCardContent(state) }
        composeRule.onNodeWithTag("frequencyChart").assertIsDisplayed()
    }

    @Test
    fun theStreakCardDrawsItsChart() {
        val streaks = listOf(Streak(today.minus(20), today.minus(10)), Streak(today.minus(5), today))
        composeRule.setContent { StreakCardContent(StreakCardState(PaletteColor(8), streaks, LightTheme())) }
        composeRule.onNodeWithTag("streakChart").assertIsDisplayed()
    }

    @Test
    fun theTargetCardDrawsItsChart() {
        val state = TargetCardState(PaletteColor(8), listOf(1.0, 3.0), listOf(1.0, 5.0), listOf(1, 7), LightTheme())
        composeRule.setContent { TargetCardContent(state) }
        composeRule.onNodeWithTag("targetChart").assertIsDisplayed()
    }

    @Test
    fun theOverviewCardShowsItsValues() {
        val state = OverviewCardState(PaletteColor(8), 0.23f, -0.12f, 0.74f, 44, LightTheme())
        composeRule.setContent { OverviewCardContent(state) }
        composeRule.onNodeWithTag("overviewCard").assertIsDisplayed()
    }
}

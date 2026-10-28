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

package io.github.crisacm.salem.ui.detail

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import io.github.crisacm.salem.core.models.Entry
import io.github.crisacm.salem.core.models.Frequency
import io.github.crisacm.salem.core.models.PaletteColor
import io.github.crisacm.salem.core.models.Reminder
import io.github.crisacm.salem.core.models.Score
import io.github.crisacm.salem.core.models.Streak
import io.github.crisacm.salem.core.models.WeekdayList
import io.github.crisacm.salem.core.ui.screens.habits.show.views.BarCardState
import io.github.crisacm.salem.core.ui.screens.habits.show.views.FrequencyCardState
import io.github.crisacm.salem.core.ui.screens.habits.show.views.HistoryCardState
import io.github.crisacm.salem.core.ui.screens.habits.show.views.NotesCardState
import io.github.crisacm.salem.core.ui.screens.habits.show.views.OverviewCardState
import io.github.crisacm.salem.core.ui.screens.habits.show.views.ScoreCardState
import io.github.crisacm.salem.core.ui.screens.habits.show.views.StreakCardState
import io.github.crisacm.salem.core.ui.screens.habits.show.views.SubtitleCardState
import io.github.crisacm.salem.core.ui.screens.habits.show.views.TargetCardState
import io.github.crisacm.salem.core.ui.views.HistoryChart
import io.github.crisacm.salem.core.ui.views.LightTheme
import io.github.crisacm.salem.platform.time.DayOfWeek
import io.github.crisacm.salem.platform.time.JavaLocalDateFormatter
import io.github.crisacm.salem.platform.time.LocalDate
import io.github.crisacm.salem.platform.time.resetToday
import io.github.crisacm.salem.platform.time.setToday
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.Locale
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class ShowCardsTest {
    private val formatter = JavaLocalDateFormatter(Locale.US)

    private val today = LocalDate(5000)

    @Before
    fun fixToday() = setToday(today)

    @After
    fun restoreToday() = resetToday()

    @Test
    fun theHistoryCardDrawsItsChartAndTheEditButtonAsks() = runComposeUiTest {
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
        setContent { HistoryCardContent(state, onEdit = { edits++ }, dateFormatter = formatter) }
        onNodeWithTag("historyChart").assertIsDisplayed()
        onNodeWithTag("historyEdit").performClick()
        assertEquals(1, edits)
    }

    @Test
    fun theBarCardShowsTheSpinnerOfItsKind() = runComposeUiTest {
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
        setContent { BarCardContent(state, onBoolSpinner = {}, onNumericalSpinner = {}, dateFormatter = formatter) }
        onNodeWithTag("barChart").assertIsDisplayed()
    }

    @Test
    fun theScoreCardDrawsItsChart() = runComposeUiTest {
        val scores = List(30) { Score(today.minus(it), 0.5 + it / 100.0) }
        val state = ScoreCardState(scores, 7, 0, PaletteColor(8), LightTheme())
        setContent { ScoreCardContent(state, onSpinner = {}, dateFormatter = formatter) }
        onNodeWithTag("scoreChart").assertIsDisplayed()
    }

    @Test
    fun theFrequencyCardDrawsItsChart() = runComposeUiTest {
        val frequency = hashMapOf(LocalDate(today.year, today.month, 1) to arrayOf(1, 2, 3, 4, 5, 6, 7))
        val state = FrequencyCardState(PaletteColor(8), DayOfWeek.MONDAY, frequency, LightTheme(), false)
        setContent { FrequencyCardContent(state, formatter) }
        onNodeWithTag("frequencyChart").assertIsDisplayed()
        onNodeWithText("Completions by weekday").assertIsDisplayed()
    }

    @Test
    fun theStreakCardDrawsItsChart() = runComposeUiTest {
        val streaks = listOf(Streak(today.minus(20), today.minus(10)), Streak(today.minus(5), today))
        setContent { StreakCardContent(StreakCardState(PaletteColor(8), streaks, LightTheme()), formatter) }
        onNodeWithTag("streakChart").assertIsDisplayed()
    }

    @Test
    fun theTargetCardDrawsItsChart() = runComposeUiTest {
        val state = TargetCardState(PaletteColor(8), listOf(1.0, 3.0), listOf(1.0, 5.0), listOf(1, 7), LightTheme())
        setContent { TargetCardContent(state) }
        onNodeWithTag("targetChart").assertIsDisplayed()
    }

    @Test
    fun theOverviewCardShowsItsValues() = runComposeUiTest {
        val state = OverviewCardState(PaletteColor(8), 0.23f, -0.12f, 0.74f, 44, LightTheme())
        setContent { OverviewCardContent(state) }
        onNodeWithTag("overviewCard").assertIsDisplayed()
        onNodeWithText("74%").assertIsDisplayed()
        onNodeWithText("+23%").assertIsDisplayed()
        onNodeWithText("\u221212%").assertIsDisplayed()
        onNodeWithText("44").assertIsDisplayed()
    }

    @Test
    fun theSubtitleShowsTheQuestionTheFrequencyAndTheReminder() = runComposeUiTest {
        val state = SubtitleCardState(
            color = PaletteColor(8),
            frequency = Frequency(3, 7),
            isNumerical = false,
            question = "Did you meditate this morning?",
            reminder = Reminder(8, 30, WeekdayList.EVERY_DAY),
            theme = LightTheme()
        )
        setContent { SubtitleCardContent(state) }
        onNodeWithTag("subtitleQuestion").assertIsDisplayed()
        onNodeWithTag("subtitleFrequency").assertIsDisplayed()
        onNodeWithTag("subtitleReminder").assertIsDisplayed()
    }

    @Test
    fun theNotesAreShownOnlyWithADescription() = runComposeUiTest {
        setContent { NotesCardContent(NotesCardState("Remember to breathe"), Color.Black) }
        onNodeWithTag("notesCard").assertIsDisplayed()
    }
}

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

package io.github.crisacm.salem.ui.list

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import io.github.crisacm.salem.core.models.Entry
import io.github.crisacm.salem.platform.time.JavaLocalDateFormatter
import io.github.crisacm.salem.platform.time.LocalDate
import io.github.crisacm.salem.platform.time.resetToday
import io.github.crisacm.salem.platform.time.setToday
import java.util.Locale
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class ListCellsComposeTest {
    private val colors = CellColors(Color.Blue, Color.LightGray, Color.Gray, Color.White)

    // The header counts the days back from today, which the app sets when it starts
    @BeforeTest
    fun setUp() = setToday(LocalDate(2015, 1, 25))

    @AfterTest
    fun tearDown() = resetToday()

    @Test
    fun aTapOnACheckmarkAsksForTheNextValue() = runComposeUiTest {
        var next = -1
        setContent {
            MaterialTheme {
                CheckmarkCell(
                    value = Entry.NO,
                    notes = "",
                    colors = colors,
                    questionMarksEnabled = false,
                    skipEnabled = false,
                    shortToggle = true,
                    onToggle = { next = it },
                    onEdit = {}
                )
            }
        }
        onNodeWithContentDescription("No").performClick()
        assertEquals(Entry.YES_MANUAL, next)
    }

    @Test
    fun theHeaderShowsItsColumns() = runComposeUiTest {
        val state = HeaderState(bucketPx = 100f) {}
        state.buttonCount = 4
        setContent {
            MaterialTheme { HeaderContent(state, JavaLocalDateFormatter(Locale.US), Color.LightGray, Color.Gray) }
        }
        onNodeWithTag("dayHeader").assertIsDisplayed()
    }
}

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

package io.github.crisacm.salem.activities.habits.list

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.crisacm.salem.core.models.Entry
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ListCellsComposeTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val colors = CellColors(Color.Blue, Color.LightGray, Color.Gray, Color.White)

    @Test
    fun aTapOnACheckmarkAsksForTheNextValue() {
        var next = -1
        composeRule.setContent {
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
        composeRule.onNodeWithContentDescription("No").performClick()
        assertEquals(Entry.YES_MANUAL, next)
    }

    @Test
    fun theHeaderShowsItsColumns() {
        val state = HeaderState(bucketPx = 100f) {}
        state.buttonCount = 4
        composeRule.setContent {
            MaterialTheme { HeaderContent(state, Color.LightGray, Color.Gray) }
        }
        composeRule.onNodeWithTag("dayHeader").assertIsDisplayed()
    }
}

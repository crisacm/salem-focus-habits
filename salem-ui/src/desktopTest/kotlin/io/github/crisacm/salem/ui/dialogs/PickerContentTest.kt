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

package io.github.crisacm.salem.ui.dialogs

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import io.github.crisacm.salem.core.models.WeekdayList
import org.junit.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class PickerContentTest {
    private val palette = List(20) { Color(0xFF000000 + it * 0x010101L) }

    @Test
    fun colorPickerReportsTheTappedSwatchAndMarksTheSelectedOne() = runComposeUiTest {
        var picked = -1
        setContent {
            MaterialTheme { ColorPickerContent(palette, selected = 3, onPicked = { picked = it }, onDismiss = {}) }
        }
        onNodeWithContentDescription("Color 4 selected").assertIsDisplayed()
        onNodeWithContentDescription("Color 1").performClick()
        assertEquals(0, picked)
    }

    @Test
    fun weekdayPickerReportsTheCheckedDays() = runComposeUiTest {
        var picked: WeekdayList? = null
        val names = listOf("Saturday", "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday")
        setContent {
            MaterialTheme {
                WeekdayPickerContent(names, WeekdayList(BooleanArray(7)), onPicked = { picked = it }, onDismiss = {})
            }
        }
        onNodeWithText("Monday").performClick()
        onNodeWithText("Sunday").performClick()
        onNodeWithText("Sunday").performClick()
        onNodeWithText("OK").performClick()
        assertEquals(booleanArrayOf(false, false, true, false, false, false, false).toList(), picked!!.toArray().toList())
    }

    @Test
    fun timePickerReportsTheInitialTimeOnOk() = runComposeUiTest {
        var picked: Pair<Int, Int>? = null
        setContent {
            MaterialTheme {
                TimePickerContent(8, 30, is24Hour = true, color = Color.Blue, showClear = false, onPicked = { h, m -> picked = h to m }, onCleared = {}, onDismiss = {})
            }
        }
        onNodeWithTag("timePicker", useUnmergedTree = true).assertIsDisplayed()
        onNodeWithTag("timePickerOk").performClick()
        assertEquals(8 to 30, picked)
    }

    @Test
    fun timePickerClearIsOnlyOfferedWhenAsked() = runComposeUiTest {
        var cleared = false
        var withClear by mutableStateOf(false)
        setContent {
            MaterialTheme {
                TimePickerContent(8, 30, true, Color.Blue, withClear, { _, _ -> }, { cleared = true }, {})
            }
        }
        onNodeWithTag("timePickerClear").assertDoesNotExist()
        withClear = true
        onNodeWithTag("timePickerClear").performClick()
        assertEquals(true, cleared)
    }
}

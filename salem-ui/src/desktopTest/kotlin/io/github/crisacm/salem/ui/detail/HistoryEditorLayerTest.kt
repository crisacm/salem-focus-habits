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

import io.github.crisacm.salem.core.ui.views.OnDateClickedListener
import io.github.crisacm.salem.platform.time.LocalDate
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class HistoryEditorLayerTest {
    private val first = object : OnDateClickedListener {
        override fun onDateShortPress(date: LocalDate) {}

        override fun onDateLongPress(date: LocalDate) {}
    }
    private val second = object : OnDateClickedListener {
        override fun onDateShortPress(date: LocalDate) {}

        override fun onDateLongPress(date: LocalDate) {}
    }

    @Test
    fun itStartsClosed() {
        val layer = HistoryEditorLayer()
        assertFalse(layer.isOpen)
        assertNull(layer.listener)
    }

    @Test
    fun showingItOpensItWithTheListener() {
        val layer = HistoryEditorLayer()
        layer.show(first)
        assertTrue(layer.isOpen)
        assertSame(first, layer.listener)
    }

    @Test
    fun showingItAgainReplacesTheListenerAndDoesNotStack() {
        val layer = HistoryEditorLayer()
        layer.show(first)
        layer.show(second)
        assertSame(second, layer.listener)
    }

    @Test
    fun dismissingClosesIt() {
        val layer = HistoryEditorLayer()
        layer.show(first)
        layer.dismiss()
        assertFalse(layer.isOpen)
        assertEquals(null, layer.listener)
    }
}

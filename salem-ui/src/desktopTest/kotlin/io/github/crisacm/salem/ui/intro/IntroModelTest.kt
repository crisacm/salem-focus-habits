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

package io.github.crisacm.salem.ui.intro

import io.github.crisacm.salem.ui.resources.Res
import io.github.crisacm.salem.ui.resources.intro_icon_1
import io.github.crisacm.salem.ui.resources.intro_icon_2
import io.github.crisacm.salem.ui.resources.intro_icon_4
import io.github.crisacm.salem.ui.resources.intro_title_1
import io.github.crisacm.salem.ui.resources.intro_title_2
import io.github.crisacm.salem.ui.resources.intro_title_4
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class IntroModelTest {
    @Test
    fun slidesKeepTheOriginalOrderAndColors() {
        assertEquals(
            listOf(Res.string.intro_title_1, Res.string.intro_title_2, Res.string.intro_title_4),
            introSlides.map { it.title }
        )
        assertEquals(
            listOf(0xFF194673, 0xFFFFA726, 0xFF9575CD),
            introSlides.map { it.color }
        )
        assertEquals(
            listOf(Res.drawable.intro_icon_1, Res.drawable.intro_icon_2, Res.drawable.intro_icon_4),
            introSlides.map { it.icon }
        )
    }

    @Test
    fun onlyTheLastPageHasDone() {
        val pager = IntroPager(3)
        assertFalse(pager.isLast(0))
        assertFalse(pager.isLast(1))
        assertTrue(pager.isLast(2))
    }

    @Test
    fun skipIsHiddenOnTheLastPage() {
        val pager = IntroPager(3)
        assertTrue(pager.showSkip(0))
        assertTrue(pager.showSkip(1))
        assertFalse(pager.showSkip(2))
    }

    @Test
    fun nextStopsAtTheLastPage() {
        val pager = IntroPager(3)
        assertEquals(1, pager.next(0))
        assertEquals(2, pager.next(1))
        assertEquals(2, pager.next(2))
    }

    @Test
    fun previousStopsAtTheFirstPage() {
        val pager = IntroPager(3)
        assertEquals(1, pager.previous(2))
        assertEquals(0, pager.previous(1))
        assertEquals(0, pager.previous(0))
    }
}

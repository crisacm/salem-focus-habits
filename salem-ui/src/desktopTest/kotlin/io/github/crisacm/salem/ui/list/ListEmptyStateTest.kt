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

import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock
import dev.mokkery.verify
import dev.mokkery.verify.VerifyMode
import io.github.crisacm.salem.core.ui.screens.habits.list.HintList
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ListEmptyStateTest {
    @Test
    fun withItemsNothingIsShown() {
        assertEquals(EmptyListState.HIDDEN, emptyListState(itemCount = 3, hasNoHabit = false))
        assertEquals(EmptyListState.HIDDEN, emptyListState(itemCount = 1, hasNoHabit = true))
    }

    @Test
    fun withoutHabitsTheListIsEmpty() {
        assertEquals(EmptyListState.EMPTY, emptyListState(itemCount = 0, hasNoHabit = true))
    }

    @Test
    fun withHabitsHiddenByTheFilterEverythingIsDone() {
        assertEquals(EmptyListState.DONE, emptyListState(itemCount = 0, hasNoHabit = false))
    }

    @Test
    fun aHintIsOnlyPoppedWhenItIsTimeToShowIt() {
        val list: HintList = mock()
        every { list.shouldShow() } returns true
        every { list.pop() } returns "Did you know?"
        assertEquals("Did you know?", nextHint(list))
    }

    @Test
    fun noHintIsShownBeforeItsTime() {
        val list: HintList = mock()
        every { list.shouldShow() } returns false
        every { list.pop() } returns "Did you know?"
        assertNull(nextHint(list))
        verify(mode = VerifyMode.not) { list.pop() }
    }

    @Test
    fun noHintIsShownWhenThereAreNoneLeft() {
        val list: HintList = mock()
        every { list.shouldShow() } returns true
        every { list.pop() } returns null
        assertNull(nextHint(list))
    }
}

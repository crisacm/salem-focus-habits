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

import io.github.crisacm.salem.core.models.Entry
import io.github.crisacm.salem.core.models.PaletteColor
import io.github.crisacm.salem.core.ui.callbacks.CheckMarkDialogCallback
import io.github.crisacm.salem.core.ui.callbacks.NumberPickerCallback
import io.github.crisacm.salem.core.ui.callbacks.OnConfirmedCallback
import io.github.crisacm.salem.core.ui.screens.habits.show.ShowHabitMenuPresenter
import io.github.crisacm.salem.core.ui.views.OnDateClickedListener
import io.github.crisacm.salem.platform.time.LocalDate
import io.github.crisacm.salem.ui.BaseUiTest
import io.github.crisacm.salem.ui.list.ListOverlay
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class HabitDetailLayersTest : BaseUiTest() {
    private var closed = 0
    private val shared = mutableListOf<String>()
    private val layers = HabitDetailLayers(onClose = { closed++ }, onShare = { shared += it })
    private val listener = object : OnDateClickedListener {
        override fun onDateShortPress(date: LocalDate) {}

        override fun onDateLongPress(date: LocalDate) {}
    }

    @Test
    fun theHistoryEditorOpensWithTheListener() {
        layers.showHistoryEditorDialog(listener)
        assertTrue(layers.historyEditor.isOpen)
        assertSame(listener, layers.historyEditor.listener)
    }

    @Test
    fun aPopupDoesNotCloseTheHistoryEditor() {
        layers.showHistoryEditorDialog(listener)
        layers.showNumberPopup(fixtures.createShortHabit(), LocalDate(2015, 1, 25), 2.5, "", NumberPickerCallback { _, _ -> })
        assertTrue(layers.historyEditor.isOpen)
        assertTrue(layers.overlays.current is ListOverlay.Number)
    }

    @Test
    fun theNumberAndCheckmarkPopupsOpenAsOverlays() {
        layers.showNumberPopup(fixtures.createShortHabit(), LocalDate(2015, 1, 25), 2.5, "note", NumberPickerCallback { _, _ -> })
        assertEquals(2.5, (layers.overlays.current as ListOverlay.Number).value)
        layers.showCheckmarkPopup(Entry.YES_MANUAL, "", PaletteColor(3), CheckMarkDialogCallback { _, _ -> })
        assertEquals(Entry.YES_MANUAL, (layers.overlays.current as ListOverlay.Checkmark).value)
    }

    @Test
    fun editingOpensTheEditorOfTheHabit() {
        val habit = fixtures.createShortHabit().also { habitList.add(it) }
        layers.showEditHabitScreen(habit)
        val editor = layers.overlays.current as ListOverlay.Editor
        assertEquals(habit.id, editor.habitId)
        assertEquals(habit.type, editor.type)
    }

    @Test
    fun theDeleteConfirmationAsksForOneHabit() {
        var confirmed = 0
        layers.showDeleteConfirmationScreen(OnConfirmedCallback { confirmed++ })
        val delete = layers.overlays.current as ListOverlay.Delete
        assertEquals(1, delete.quantity)
        delete.callback.onConfirmed()
        assertEquals(1, confirmed)
    }

    @Test
    fun theMessagesOfTheMenuBecomeBannerMessages() {
        layers.showMessage(ShowHabitMenuPresenter.Message.HABIT_ARCHIVED)
        assertEquals(DetailMessage.ARCHIVED, layers.message)
        layers.showMessage(ShowHabitMenuPresenter.Message.HABIT_UNARCHIVED)
        assertEquals(DetailMessage.UNARCHIVED, layers.message)
        layers.showMessage(ShowHabitMenuPresenter.Message.COULD_NOT_EXPORT)
        assertEquals(DetailMessage.COULD_NOT_EXPORT, layers.message)
    }

    @Test
    fun aNullMessageChangesNothing() {
        layers.showMessage(ShowHabitMenuPresenter.Message.HABIT_ARCHIVED)
        layers.showMessage(null)
        assertEquals(DetailMessage.ARCHIVED, layers.message)
    }

    @Test
    fun sharingAFileCallsTheShareAction() {
        layers.showSendFileScreen("/cache/habits.zip")
        assertEquals(listOf("/cache/habits.zip"), shared)
        assertNull(layers.message)
    }

    @Test
    fun aMessageCanBeCleared() {
        layers.show(DetailMessage.ARCHIVED)
        layers.clearMessage()
        assertNull(layers.message)
    }

    @Test
    fun closingTellsTheHost() {
        layers.close()
        assertEquals(1, closed)
    }

    @Test
    fun itIsCoveringWhileAnOverlayOrTheEditorIsOpen() {
        assertFalse(layers.isCovering)
        layers.showHistoryEditorDialog(listener)
        assertTrue(layers.isCovering)
        layers.historyEditor.dismiss()
        assertFalse(layers.isCovering)
        layers.showDeleteConfirmationScreen(OnConfirmedCallback { })
        assertTrue(layers.isCovering)
    }

    @Test
    fun theNavigationOpensAndClosesAHabit() {
        val navigation = DetailNavigation()
        assertNull(navigation.habit)
        val habit = fixtures.createShortHabit()
        navigation.open(habit)
        assertSame(habit, navigation.habit)
        navigation.close()
        assertNull(navigation.habit)
    }
}

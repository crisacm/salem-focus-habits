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

package io.github.crisacm.salem.ui.ios

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.github.crisacm.salem.core.models.ModelObservable
import io.github.crisacm.salem.core.preferences.Preferences
import io.github.crisacm.salem.core.ui.views.LightTheme
import io.github.crisacm.salem.core.ui.views.Theme
import io.github.crisacm.salem.core.utils.MidnightTimer
import io.github.crisacm.salem.platform.time.AppleLocalDateFormatter
import io.github.crisacm.salem.platform.time.getToday
import io.github.crisacm.salem.ui.list.CheckmarkSize
import io.github.crisacm.salem.ui.list.EmptyListState
import io.github.crisacm.salem.ui.list.HabitNameWidth
import io.github.crisacm.salem.ui.list.HeaderState
import io.github.crisacm.salem.ui.list.ListRowCallbacks
import io.github.crisacm.salem.ui.list.ListScreenColors
import io.github.crisacm.salem.ui.list.ListScreenModel
import io.github.crisacm.salem.ui.list.MAX_CHECKMARK_COUNT
import io.github.crisacm.salem.ui.list.checkmarkColumns
import io.github.crisacm.salem.ui.list.emptyListState
import io.github.crisacm.salem.ui.list.listScreenColorsFor
import kotlin.math.max

/**
 * The habit list screen of the iOS app over an [IosHabitsGraph]: the day header, the callbacks of the rows and the
 * listeners that follow the screen while it is shown, as `ListHabitsHost` does on Android. [density] converts the
 * sizes in dp to the pixels of the screen. The root sets [theme] when the theme changes.
 */
class IosListScreenModel(private val graph: IosHabitsGraph, private val density: Float) : ListScreenModel, ModelObservable.Listener {
    var theme: Theme by mutableStateOf(LightTheme())

    override val menu = graph.menu
    override val selectionMenu = graph.selectionMenu
    override val listState = graph.listState
    override val preferences: Preferences = graph.preferences
    override val hintList = graph.hintList
    override val taskRunner = graph.taskRunner
    override val dateFormatter = AppleLocalDateFormatter()
    override val emptyState = mutableStateOf(EmptyListState.HIDDEN)
    override val headerState = HeaderState(CheckmarkSize.value * density) {}
    override val rowCallbacks = ListRowCallbacks(
        onClick = graph.controller::onClick,
        onLongClick = graph.controller::onLongClick,
        onToggle = graph.behavior::onToggle,
        onEdit = graph.behavior::onEdit,
        onDrop = graph.controller::drop,
        onDragStart = graph.controller::onDragStarted,
        onDragEnd = graph.controller::onDragFinished
    )
    override val colors: ListScreenColors
        get() = listScreenColorsFor(theme)

    private val preferencesListener = object : Preferences.Listener {
        override fun onCheckmarkSequenceChanged() {
            headerState.reversed = preferences.isCheckmarkSequenceReversed
        }
    }
    private val midnightListener = object : MidnightTimer.MidnightListener {
        override fun atMidnight() {
            headerState.today = getToday()
        }
    }

    override fun onAttached() {
        headerState.reversed = preferences.isCheckmarkSequenceReversed
        headerState.today = getToday()
        preferences.addListener(preferencesListener)
        graph.midnightTimer.addListener(midnightListener)
        listState.observable.addListener(this)
        listState.onAttached()
    }

    override fun onDetached() {
        listState.onDetached()
        listState.observable.removeListener(this)
        graph.midnightTimer.removeListener(midnightListener)
        preferences.removeListener(preferencesListener)
    }

    override fun onWidthChanged(width: Int) {
        val count = checkmarkColumns(
            width = width.toFloat(),
            nameWidth = HabitNameWidth.value * density,
            buttonWidth = CheckmarkSize.value * density
        )
        headerState.buttonCount = count
        headerState.setMaxDataOffset(max(MAX_CHECKMARK_COUNT - count, 0))
    }

    override fun onModelChange() {
        emptyState.value = emptyListState(listState.itemCount, listState.hasNoHabit())
    }
}

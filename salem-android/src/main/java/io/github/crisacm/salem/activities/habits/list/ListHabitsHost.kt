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

import android.app.Activity
import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.toArgb
import androidx.core.view.WindowCompat
import io.github.crisacm.salem.R
import io.github.crisacm.salem.core.models.ModelObservable
import io.github.crisacm.salem.core.preferences.Preferences
import io.github.crisacm.salem.core.tasks.TaskRunner
import io.github.crisacm.salem.core.ui.ThemeSwitcher
import io.github.crisacm.salem.core.ui.screens.habits.list.HintListFactory
import io.github.crisacm.salem.core.ui.screens.habits.list.ListHabitsBehavior
import io.github.crisacm.salem.core.utils.MidnightTimer
import io.github.crisacm.salem.inject.ActivityContext
import io.github.crisacm.salem.inject.ActivityScope
import io.github.crisacm.salem.platform.time.JavaLocalDateFormatter
import io.github.crisacm.salem.platform.time.getToday
import io.github.crisacm.salem.ui.list.DAY_COLUMNS
import io.github.crisacm.salem.ui.list.EmptyListState
import io.github.crisacm.salem.ui.list.HabitListController
import io.github.crisacm.salem.ui.list.HabitListState
import io.github.crisacm.salem.ui.list.HeaderState
import io.github.crisacm.salem.ui.list.ListHabitsMenu
import io.github.crisacm.salem.ui.list.ListHabitsSelectionMenu
import io.github.crisacm.salem.ui.list.ListRowCallbacks
import io.github.crisacm.salem.ui.list.ListScreenModel
import io.github.crisacm.salem.ui.list.MAX_CHECKMARK_COUNT
import io.github.crisacm.salem.ui.list.emptyListState
import io.github.crisacm.salem.ui.list.isDarkList
import io.github.crisacm.salem.ui.list.toListColorScheme
import me.tatarka.inject.annotations.Inject
import nl.dionsegijn.konfetti.xml.KonfettiView
import java.util.Locale
import kotlin.math.max

/**
 * Everything the habit list screen keeps outside of its composition: the shared day offset of the header
 * and the rows, the empty state, the confetti and progress Views, and the listeners that follow the
 * screen while it is attached. It replaces the old `ListHabitsRootView`.
 */
@Inject
@ActivityScope
class ListHabitsHost(
    @ActivityContext private val context: Context,
    hintListFactory: HintListFactory,
    override val menu: ListHabitsMenu,
    override val selectionMenu: ListHabitsSelectionMenu,
    override val preferences: Preferences,
    private val midnightTimer: MidnightTimer,
    override val taskRunner: TaskRunner,
    override val listState: HabitListState,
    controller: HabitListController,
    behavior: ListHabitsBehavior,
    private val themeSwitcher: ThemeSwitcher
) : ModelObservable.Listener, ListScreenModel {

    override val emptyState = mutableStateOf(EmptyListState.HIDDEN)
    val konfettiView = KonfettiView(context)
    override val hintList = hintListFactory.create(context.resources.getStringArray(R.array.hints))

    // The rows read the offset from the header state, so nobody needs to be told when it changes.
    // One day is scrolled for every 64 dp that the header is dragged, about the width of a column.
    override val headerState = HeaderState(context.resources.displayMetrics.density * 64f) {}

    override val rowCallbacks = ListRowCallbacks(
        onClick = controller::onClick,
        onLongClick = controller::onLongClick,
        onToggle = behavior::onToggle,
        onEdit = behavior::onEdit,
        onDrop = controller::drop,
        onDragStart = controller::onDragStarted,
        onDragEnd = controller::onDragFinished
    )

    /** The names of the weekdays in the header. */
    override val dateFormatter = JavaLocalDateFormatter(Locale.getDefault())

    // Compose state can be written from any thread, so the listeners need no handler.
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

    /** How many days the rows are scrolled; the activity saves it to survive a recreation. */
    val dataOffset: Int
        get() = headerState.dataOffset

    fun restoreDataOffset(offset: Int) = headerState.restoreOffset(offset)

    // Before Android 15 the window is not edge to edge, so the status bar needs the colour of the screen; on every
    // version its icons follow the theme of the list: dark on the light one, light on the dark ones.
    @Suppress("DEPRECATION")
    fun paintStatusBar(activity: Activity) {
        val theme = themeSwitcher.currentTheme ?: return
        activity.window.statusBarColor = theme.toListColorScheme().background.toArgb()
        WindowCompat.getInsetsController(activity.window, activity.window.decorView).isAppearanceLightStatusBars = !theme.isDarkList()
    }

    /** Called when the screen enters the composition, which happens when its window is attached. */
    override fun onAttached() {
        headerState.reversed = preferences.isCheckmarkSequenceReversed
        headerState.today = getToday()
        preferences.addListener(preferencesListener)
        midnightTimer.addListener(midnightListener)
        listState.observable.addListener(this)
        listState.onAttached()
    }

    override fun onDetached() {
        listState.onDetached()
        listState.observable.removeListener(this)
        midnightTimer.removeListener(midnightListener)
        preferences.removeListener(preferencesListener)
    }

    /** Set by the activity: the confetti overlay follows the side insets that the composition applied. */
    var onSideInsetsChanged: ((left: Int, right: Int) -> Unit)? = null

    fun onSideInsets(left: Int, right: Int) {
        onSideInsetsChanged?.invoke(left, right)
    }

    /** Called with the width in pixels of the screen: the header always shows [DAY_COLUMNS] days and can scroll back to the oldest one. */
    override fun onWidthChanged(width: Int) {
        headerState.buttonCount = DAY_COLUMNS
        headerState.setMaxDataOffset(max(MAX_CHECKMARK_COUNT - DAY_COLUMNS, 0))
    }

    override fun onModelChange() {
        updateEmptyView()
    }

    private fun updateEmptyView() {
        emptyState.value = emptyListState(listState.itemCount, listState.hasNoHabit())
    }
}

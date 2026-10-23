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
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import android.widget.RelativeLayout
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import io.github.crisacm.salem.R
import io.github.crisacm.salem.activities.common.views.TaskProgressBar
import io.github.crisacm.salem.activities.habits.list.views.HabitCardListAdapter
import io.github.crisacm.salem.activities.habits.list.views.HabitCardListView
import io.github.crisacm.salem.activities.habits.list.views.HabitCardListViewFactory
import io.github.crisacm.salem.core.models.ModelObservable
import io.github.crisacm.salem.core.models.PaletteColor
import io.github.crisacm.salem.core.preferences.Preferences
import io.github.crisacm.salem.core.tasks.TaskRunner
import io.github.crisacm.salem.core.ui.screens.habits.list.HintListFactory
import io.github.crisacm.salem.core.utils.MidnightTimer
import io.github.crisacm.salem.inject.ActivityContext
import io.github.crisacm.salem.inject.ActivityScope
import io.github.crisacm.salem.platform.gui.toInt
import io.github.crisacm.salem.platform.time.getToday
import io.github.crisacm.salem.ui.compose.SalemTheme
import io.github.crisacm.salem.utils.StyledResources
import io.github.crisacm.salem.utils.addAtBottom
import io.github.crisacm.salem.utils.addAtTop
import io.github.crisacm.salem.utils.addBelow
import io.github.crisacm.salem.utils.currentTheme
import io.github.crisacm.salem.utils.dim
import io.github.crisacm.salem.utils.dp
import io.github.crisacm.salem.utils.sres
import me.tatarka.inject.annotations.Inject
import nl.dionsegijn.konfetti.xml.KonfettiView
import kotlin.math.max
import kotlin.math.min

const val MAX_CHECKMARK_COUNT = 60

@Inject
@ActivityScope
class ListHabitsRootView(
    @ActivityContext context: Context,
    hintListFactory: HintListFactory,
    menu: ListHabitsMenu,
    selectionMenu: ListHabitsSelectionMenu,
    private val preferences: Preferences,
    private val midnightTimer: MidnightTimer,
    runner: TaskRunner,
    private val listAdapter: HabitCardListAdapter,
    habitCardListViewFactory: HabitCardListViewFactory
) : FrameLayout(context), ModelObservable.Listener {

    val listView: HabitCardListView = habitCardListViewFactory.create()
    private val emptyState = mutableStateOf(EmptyListState.HIDDEN)
    val llEmpty = ComposeView(context).apply {
        visibility = View.GONE
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent { SalemTheme { EmptyListContent(emptyState.value) } }
    }
    val tbar = ComposeView(context).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        val barColor = Color(barColor())
        paintStatusBar(barColor.toArgb())
        setContent {
            SalemTheme { ListHabitsBar(menu, selectionMenu, barColor, Color.White) }
        }
    }
    val konfettiView = KonfettiView(context).apply {
        translationZ = 10f
    }
    val progressBar = TaskProgressBar(context, runner)
    private val hintList = hintListFactory.create(context.resources.getStringArray(R.array.hints))
    val hintView = ComposeView(context).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent { SalemTheme { HintContent(hintList) } }
    }
    private val headerState = HeaderState(context.resources.getDimension(R.dimen.checkmarkWidth)) {
        listView.dataOffset = it
    }
    val header = ComposeView(context).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        val styled = StyledResources(context)
        val background = Color(styled.getColor(R.attr.headerBackgroundColor))
        val text = Color(styled.getColor(R.attr.contrast60))
        setContent { SalemTheme { HeaderContent(headerState, background, text) } }
    }
    private val preferencesListener = object : Preferences.Listener {
        override fun onCheckmarkSequenceChanged() {
            post { headerState.reversed = preferences.isCheckmarkSequenceReversed }
        }
    }
    private val midnightListener = object : MidnightTimer.MidnightListener {
        override fun atMidnight() {
            post { headerState.today = getToday() }
        }
    }

    init {
        val rootView = RelativeLayout(context).apply {
            background = sres.getDrawable(R.attr.windowBackgroundColor)
            addAtTop(konfettiView)
            addAtTop(tbar)
            addBelow(header, tbar)
            addBelow(listView, header, height = MATCH_PARENT)
            addBelow(llEmpty, header, height = MATCH_PARENT)
            addBelow(progressBar, header) {
                it.topMargin = dp(-6.0f).toInt()
            }
            addAtBottom(hintView)
        }
        addView(rootView, MATCH_PARENT, MATCH_PARENT)
        listAdapter.setListView(listView)
    }

    /** The colour of the top bar: the habit palette colour in the light theme, the primary colour otherwise. */
    private fun barColor(): Int {
        val resources = StyledResources(context)
        return if (resources.getBoolean(R.attr.useHabitColorAsPrimary)) {
            currentTheme().color(PaletteColor(17)).toInt()
        } else {
            resources.getColor(R.attr.colorPrimary)
        }
    }

    // Before Android 15 the window is not edge to edge, so the status bar needs the colour of the bar.
    @Suppress("DEPRECATION")
    private fun paintStatusBar(color: Int) {
        (context as? Activity)?.window?.statusBarColor = color
    }

    override fun onModelChange() {
        updateEmptyView()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        headerState.reversed = preferences.isCheckmarkSequenceReversed
        headerState.today = getToday()
        // The list restores its own offset when the screen is recreated, so the header starts from it.
        headerState.restoreOffset(listView.dataOffset)
        preferences.addListener(preferencesListener)
        midnightTimer.addListener(midnightListener)
        listAdapter.observable.addListener(this)
    }

    override fun onDetachedFromWindow() {
        listAdapter.observable.removeListener(this)
        midnightTimer.removeListener(midnightListener)
        preferences.removeListener(preferencesListener)
        super.onDetachedFromWindow()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        val count = getCheckmarkCount()
        headerState.buttonCount = count
        headerState.setMaxDataOffset(max(MAX_CHECKMARK_COUNT - count, 0))
        listView.checkmarkCount = count
        super.onSizeChanged(w, h, oldw, oldh)
    }

    private fun getCheckmarkCount(): Int {
        val nameWidth = dim(R.dimen.habitNameWidth)
        val buttonWidth = dim(R.dimen.checkmarkWidth)
        val labelWidth = max((measuredWidth / 3).toFloat(), nameWidth)
        val buttonCount = ((measuredWidth - labelWidth) / buttonWidth).toInt()
        return min(MAX_CHECKMARK_COUNT, max(0, buttonCount))
    }

    private fun updateEmptyView() {
        val state = emptyListState(listAdapter.itemCount, listAdapter.hasNoHabit())
        emptyState.value = state
        llEmpty.visibility = if (state == EmptyListState.HIDDEN) View.GONE else View.VISIBLE
    }
}

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
import android.graphics.Typeface
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontFamily
import io.github.crisacm.salem.R
import io.github.crisacm.salem.activities.common.views.TaskProgressBar
import io.github.crisacm.salem.core.models.ModelObservable
import io.github.crisacm.salem.core.models.PaletteColor
import io.github.crisacm.salem.core.preferences.Preferences
import io.github.crisacm.salem.core.tasks.TaskRunner
import io.github.crisacm.salem.core.ui.ThemeSwitcher
import io.github.crisacm.salem.core.ui.screens.habits.list.HintListFactory
import io.github.crisacm.salem.core.ui.screens.habits.list.ListHabitsBehavior
import io.github.crisacm.salem.core.utils.MidnightTimer
import io.github.crisacm.salem.inject.ActivityContext
import io.github.crisacm.salem.inject.ActivityScope
import io.github.crisacm.salem.platform.gui.toInt
import io.github.crisacm.salem.platform.time.JavaLocalDateFormatter
import io.github.crisacm.salem.platform.time.getToday
import io.github.crisacm.salem.ui.list.CondensedFonts
import io.github.crisacm.salem.ui.list.EmptyListState
import io.github.crisacm.salem.ui.list.HabitListController
import io.github.crisacm.salem.ui.list.HabitListState
import io.github.crisacm.salem.ui.list.HeaderState
import io.github.crisacm.salem.ui.list.ListColors
import io.github.crisacm.salem.ui.list.ListHabitsMenu
import io.github.crisacm.salem.ui.list.ListHabitsSelectionMenu
import io.github.crisacm.salem.ui.list.ListRowCallbacks
import io.github.crisacm.salem.ui.list.MAX_CHECKMARK_COUNT
import io.github.crisacm.salem.ui.list.emptyListState
import io.github.crisacm.salem.utils.StyledResources
import me.tatarka.inject.annotations.Inject
import nl.dionsegijn.konfetti.xml.KonfettiView
import java.util.Locale
import kotlin.math.max
import kotlin.math.min

/**
 * How many day columns fit in [width] pixels: the name of the habit takes a third of the width, or
 * [nameWidth] if that is larger, and each column is [buttonWidth] wide.
 */
fun checkmarkColumns(width: Float, nameWidth: Float, buttonWidth: Float): Int {
    val labelWidth = max(width / 3, nameWidth)
    val buttonCount = ((width - labelWidth) / buttonWidth).toInt()
    return min(MAX_CHECKMARK_COUNT, max(0, buttonCount))
}

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
    val menu: ListHabitsMenu,
    val selectionMenu: ListHabitsSelectionMenu,
    val preferences: Preferences,
    private val midnightTimer: MidnightTimer,
    runner: TaskRunner,
    val listState: HabitListState,
    controller: HabitListController,
    behavior: ListHabitsBehavior,
    private val themeSwitcher: ThemeSwitcher
) : ModelObservable.Listener {

    val emptyState = mutableStateOf(EmptyListState.HIDDEN)
    val konfettiView = KonfettiView(context)
    val progressBar = TaskProgressBar(context, runner)
    val hintList = hintListFactory.create(context.resources.getStringArray(R.array.hints))

    // The rows read the offset from the header state, so nobody needs to be told when it changes.
    val headerState = HeaderState(context.resources.getDimension(R.dimen.checkmarkWidth)) {}

    val rowCallbacks = ListRowCallbacks(
        onClick = controller::onClick,
        onLongClick = controller::onLongClick,
        onToggle = behavior::onToggle,
        onEdit = behavior::onEdit,
        onDrop = controller::drop,
        onDragStart = controller::onDragStarted,
        onDragEnd = controller::onDragFinished
    )

    private val styled = StyledResources(context)
    val windowBackground = Color(styled.getColor(R.attr.windowBackgroundColor))
    val headerBackground = Color(styled.getColor(R.attr.headerBackgroundColor))
    val headerText = Color(styled.getColor(R.attr.contrast60))

    /** The colours of the rows, from the theme of the activity. */
    val listColors = ListColors(
        card = Color(styled.getColor(R.attr.cardBgColor)),
        highlighted = Color(styled.getColor(R.attr.highlightedBackgroundColor)),
        low = Color(styled.getColor(R.attr.contrast40)),
        medium = Color(styled.getColor(R.attr.contrast60)),
        ringTrack = Color(styled.getColor(R.attr.contrast100)).copy(alpha = 0.15f)
    )

    /** The names of the weekdays in the header. */
    val dateFormatter = JavaLocalDateFormatter(Locale.getDefault())

    /** The condensed font of the number cells, which only Android has by name. */
    val condensedFonts = CondensedFonts(
        regular = FontFamily(Typeface.create("sans-serif-condensed", Typeface.NORMAL)),
        bold = FontFamily(Typeface.create("sans-serif-condensed", Typeface.BOLD))
    )

    /** The colour of the top bar: the habit palette colour in the light theme, the primary colour otherwise. */
    val barColor: Color by lazy {
        if (styled.getBoolean(R.attr.useHabitColorAsPrimary)) {
            Color(themeSwitcher.currentTheme!!.color(PaletteColor(17)).toInt())
        } else {
            Color(styled.getColor(R.attr.colorPrimary))
        }
    }

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

    // Before Android 15 the window is not edge to edge, so the status bar needs the colour of the bar.
    @Suppress("DEPRECATION")
    fun paintStatusBar(activity: Activity) {
        activity.window.statusBarColor = barColor.toArgb()
    }

    /** Called when the screen enters the composition, which happens when its window is attached. */
    fun onAttached() {
        headerState.reversed = preferences.isCheckmarkSequenceReversed
        headerState.today = getToday()
        preferences.addListener(preferencesListener)
        midnightTimer.addListener(midnightListener)
        listState.observable.addListener(this)
        listState.onAttached()
    }

    fun onDetached() {
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

    /** Called with the width in pixels of the screen, so the header and the rows know how many days to show. */
    fun onWidthChanged(width: Int) {
        val resources = context.resources
        val count = checkmarkColumns(
            width = width.toFloat(),
            nameWidth = resources.getDimension(R.dimen.habitNameWidth),
            buttonWidth = resources.getDimension(R.dimen.checkmarkWidth)
        )
        headerState.buttonCount = count
        headerState.setMaxDataOffset(max(MAX_CHECKMARK_COUNT - count, 0))
    }

    override fun onModelChange() {
        updateEmptyView()
    }

    private fun updateEmptyView() {
        emptyState.value = emptyListState(listState.itemCount, listState.hasNoHabit())
    }
}

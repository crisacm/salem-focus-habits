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

import android.os.Build
import android.view.View
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitLongPressOrCancellation
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import io.github.crisacm.salem.R
import io.github.crisacm.salem.core.models.Habit
import io.github.crisacm.salem.core.preferences.Preferences
import io.github.crisacm.salem.core.ui.views.Theme
import io.github.crisacm.salem.platform.time.LocalDate
import io.github.crisacm.salem.ui.compose.toCompose
import io.github.crisacm.salem.utils.StyledResources

/** What the rows tell the screen; [onDrop] moves the row at `from` to `to` while it is dragged, and [onDragEnd] ends the drag. Positions are the centre of the touched cell in window coordinates. */
class ListRowCallbacks(
    val onClick: (Habit) -> Unit,
    val onLongClick: (Habit) -> Unit,
    val onToggle: (habit: Habit, date: LocalDate, value: Int, notes: String, x: Float, y: Float) -> Unit,
    val onEdit: (habit: Habit, date: LocalDate, x: Float, y: Float) -> Unit,
    val onDrop: (from: Int, to: Int) -> Unit,
    val onDragStart: (Habit) -> Unit,
    val onDragEnd: () -> Unit
)

/** The colours of the list taken from the theme of the activity. */
private class ListColors(context: android.content.Context) {
    private val styled = StyledResources(context)
    val card = Color(styled.getColor(R.attr.cardBgColor))
    val highlighted = Color(styled.getColor(R.attr.highlightedBackgroundColor))
    val low = Color(styled.getColor(R.attr.contrast40))
    val medium = Color(styled.getColor(R.attr.contrast60))
    val ringTrack = Color(styled.getColor(R.attr.contrast100)).copy(alpha = 0.15f)
}

/** The row being dragged and how far it has moved from its place. */
private class RowDrag {
    var id by mutableStateOf<Long?>(null)
    var offset by mutableFloatStateOf(0f)
}

private val SelectedBorder = Color(0xFF9E9E9E)

/**
 * The habit rows: a score ring, the name and one cell per day, with the days of the header ([header]
 * gives the number of columns, the offset, the order and today). Preferences and colours are read each
 * time a row is composed, so a refresh of the rows after the settings screen picks up changes.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun HabitListContent(
    state: HabitListState,
    header: HeaderState,
    theme: Theme,
    preferences: Preferences,
    callbacks: ListRowCallbacks,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues()
) {
    val colors = ListColors(LocalContext.current)
    val selected = state.selectedIds
    val listState = rememberLazyListState()
    val drag = remember { RowDrag() }
    val sortable = state.isSortable
    LazyColumn(
        state = listState,
        modifier = modifier.semantics { testTagsAsResourceId = true },
        contentPadding = contentPadding
    ) {
        items(state.rows, key = { it.habit.id!! }) { row ->
            val id = row.habit.id!!
            HabitRow(
                modifier = if (drag.id == id) {
                    Modifier.zIndex(1f).graphicsLayer { translationY = drag.offset }
                } else {
                    Modifier
                },
                sortable = sortable,
                onDragStart = {
                    drag.id = id
                    drag.offset = 0f
                    callbacks.onDragStart(row.habit)
                },
                onDrag = { dy ->
                    drag.offset += dy
                    val index = state.rows.indexOfFirst { it.habit.id == id }
                    val height = listState.layoutInfo.visibleItemsInfo.firstOrNull { it.key == id }?.size ?: 0
                    val target = dragTargetIndex(index, drag.offset, height.toFloat(), state.rows.size)
                    if (index >= 0 && target != index) {
                        callbacks.onDrop(index, target)
                        drag.offset -= (target - index) * height
                    }
                },
                onDragEnd = {
                    drag.id = null
                    drag.offset = 0f
                    callbacks.onDragEnd()
                },
                row = row,
                header = header,
                selected = row.habit.id in selected,
                habitColor = if (row.habit.isArchived) colors.medium else theme.color(row.habit.color).toCompose(),
                colors = colors,
                preferences = preferences,
                callbacks = callbacks
            )
        }
    }
}

@Composable
private fun HabitRow(
    row: HabitRowData,
    header: HeaderState,
    selected: Boolean,
    habitColor: Color,
    colors: ListColors,
    preferences: Preferences,
    callbacks: ListRowCallbacks,
    sortable: Boolean,
    onDragStart: () -> Unit,
    onDrag: (Float) -> Unit,
    onDragEnd: () -> Unit,
    modifier: Modifier = Modifier
) {
    val habit = row.habit
    val currentCallbacks by rememberUpdatedState(callbacks)
    val currentDragStart by rememberUpdatedState(onDragStart)
    val currentDrag by rememberUpdatedState(onDrag)
    val currentDragEnd by rememberUpdatedState(onDragEnd)
    val cellColors = CellColors(habitColor, colors.low, colors.medium, colors.card)
    val dates = headerColumns(header.buttonCount, header.dataOffset, header.today, header.reversed)
    Surface(
        color = if (selected) colors.highlighted else colors.card,
        shadowElevation = 1.dp,
        modifier = modifier
            .padding(start = 3.dp, end = 3.dp, bottom = 3.dp)
            .testTag("habitRow")
            .then(if (selected) Modifier.border(2.dp, SelectedBorder) else Modifier)
            .pointerInput(habit.id, sortable) {
                // The cells consume their own touches first, so only the rest of the row gets here.
                awaitEachGesture {
                    val down = awaitFirstDown()
                    val longPress = awaitLongPressOrCancellation(down.id)
                    if (longPress == null) {
                        val change = currentEvent.changes.firstOrNull { it.id == down.id }
                        if (change != null && !change.pressed && !change.isConsumed) {
                            change.consume()
                            currentCallbacks.onClick(habit)
                        }
                        return@awaitEachGesture
                    }
                    currentCallbacks.onLongClick(habit)
                    if (!sortable) {
                        waitForUpOrCancellation()
                        return@awaitEachGesture
                    }
                    currentDragStart()
                    try {
                        drag(longPress.id) {
                            it.consume()
                            currentDrag(it.positionChange().y)
                        }
                    } finally {
                        currentDragEnd()
                    }
                }
            }
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            ScoreRing(
                percentage = row.score.toFloat(),
                color = habitColor,
                inactiveColor = colors.ringTrack,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            Text(
                habit.name,
                color = habitColor,
                fontSize = 14.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Row(horizontalArrangement = Arrangement.End) {
                for (date in dates) {
                    val daysAgo = date.daysUntil(header.today)
                    DayCell(row, date, daysAgo, cellColors, preferences, callbacks)
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    row: HabitRowData,
    date: LocalDate,
    daysAgo: Int,
    colors: CellColors,
    preferences: Preferences,
    callbacks: ListRowCallbacks
) {
    val habit = row.habit
    val view = LocalView.current
    val coordinates = remember { arrayOfNulls<LayoutCoordinates>(1) }
    val locate = { windowCenter(view, coordinates[0]) }
    val modifier = Modifier.onGloballyPositioned { coordinates[0] = it }
    val notes = noteAt(row.notes, daysAgo)
    if (habit.isNumerical) {
        NumberCell(
            value = numberAt(row.checkmarks, daysAgo),
            completed = completedAt(row.completed, daysAgo),
            units = habit.unit,
            notes = notes,
            colors = colors,
            questionMarksEnabled = preferences.areQuestionMarksEnabled,
            onEdit = {
                val at = locate()
                callbacks.onEdit(habit, date, at.x, at.y)
            },
            modifier = modifier
        )
    } else {
        CheckmarkCell(
            value = checkmarkAt(row.checkmarks, daysAgo),
            notes = notes,
            colors = colors,
            questionMarksEnabled = preferences.areQuestionMarksEnabled,
            skipEnabled = preferences.isSkipEnabled,
            shortToggle = preferences.isShortToggleEnabled,
            onToggle = { value ->
                val at = locate()
                callbacks.onToggle(habit, date, value, notes, at.x, at.y)
            },
            onEdit = {
                val at = locate()
                callbacks.onEdit(habit, date, at.x, at.y)
            },
            modifier = modifier
        )
    }
}

/**
 * The centre of a cell in the coordinates the old rows reported: window position minus the display cutout
 * on the left and, before Android 16, the status bar (the window was not edge to edge there).
 */
@Suppress("DEPRECATION")
private fun windowCenter(view: View, coordinates: LayoutCoordinates?): Offset {
    if (coordinates == null || !coordinates.isAttached) return Offset.Zero
    val insets = view.rootWindowInsets
    val xInset = insets?.displayCutout?.safeInsetLeft ?: 0
    val yInset = if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.VANILLA_ICE_CREAM) insets?.systemWindowInsetTop ?: 0 else 0
    val centre = coordinates.positionInWindow() + Offset(coordinates.size.width / 2f, coordinates.size.height / 2f)
    return Offset(centre.x - xInset, centre.y - yInset)
}

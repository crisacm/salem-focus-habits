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

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitLongPressOrCancellation
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import io.github.crisacm.salem.core.models.Habit
import io.github.crisacm.salem.core.preferences.Preferences
import io.github.crisacm.salem.core.ui.views.Theme
import io.github.crisacm.salem.platform.time.LocalDate
import io.github.crisacm.salem.ui.compose.toCompose
import io.github.crisacm.salem.ui.compose.usesDarkTones

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

/** The row being dragged and how far it has moved from its place. */
private class RowDrag {
    var id by mutableStateOf<Long?>(null)
    var offset by mutableFloatStateOf(0f)
}

/** The space between two cards, and between the cards and the edges of the screen. */
private val CardGap = 12.dp
private val ScreenMargin = 16.dp
private val CardShape = RoundedCornerShape(28.dp)

/** The colour that a habit with no colour of its own, an archived one, takes. */
private val ArchivedColor = Color(0xFF9E9E9E)

/**
 * The habit cards: the icon of the habit inside its score ring, the name, a button to open the habit and one
 * cell per day, with the days of the header ([header] gives the number of columns, the offset, the order and
 * today). Preferences are read each time a card is composed, so a refresh of the rows after the settings screen
 * picks up changes.
 */
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
    val selected = state.selectedIds
    val listState = rememberLazyListState()
    val drag = remember { RowDrag() }
    val sortable = state.isSortable
    val dark = theme.usesDarkTones()
    val gap = with(LocalDensity.current) { CardGap.roundToPx() }
    val direction = LocalLayoutDirection.current
    LazyColumn(
        state = listState,
        modifier = modifier.exposeTestTags(),
        verticalArrangement = Arrangement.spacedBy(CardGap),
        contentPadding = PaddingValues(
            start = ScreenMargin + contentPadding.calculateStartPadding(direction),
            end = ScreenMargin + contentPadding.calculateEndPadding(direction),
            top = contentPadding.calculateTopPadding() + 4.dp,
            bottom = contentPadding.calculateBottomPadding()
        )
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
                    val pitch = height + gap
                    val target = dragTargetIndex(index, drag.offset, pitch.toFloat(), state.rows.size)
                    if (index >= 0 && target != index) {
                        callbacks.onDrop(index, target)
                        drag.offset -= (target - index) * pitch
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
                tones = habitTones(if (row.habit.isArchived) ArchivedColor else theme.color(row.habit.color).toCompose(), dark),
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
    tones: HabitTones,
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
    val dates = headerColumns(header.buttonCount, header.dataOffset, header.today, header.reversed)
    Surface(
        color = tones.container,
        shape = CardShape,
        modifier = modifier
            .fillMaxWidth()
            .testTag("habitRow")
            .then(if (selected) Modifier.border(3.dp, MaterialTheme.colorScheme.primary, CardShape) else Modifier)
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
        Column(Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                HabitIconRing(
                    percentage = row.score.toFloat(),
                    icon = habitIconFor(habit.uuid, habit.id),
                    tones = tones
                )
                Text(
                    habit.name,
                    color = tones.onContainer,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(tones.chip)
                        .clickable { currentCallbacks.onClick(habit) }
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = habit.name,
                        tint = tones.onContainer,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                for (date in dates) {
                    val daysAgo = date.daysUntil(header.today)
                    DayCell(row, date, daysAgo, tones, preferences, callbacks, Modifier.weight(1f))
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
    tones: HabitTones,
    preferences: Preferences,
    callbacks: ListRowCallbacks,
    modifier: Modifier
) {
    val habit = row.habit
    val windowInsets = rememberWindowInsetOffset()
    val coordinates = remember { arrayOfNulls<LayoutCoordinates>(1) }
    val locate = { windowCenter(windowInsets(), coordinates[0]) }
    val cellModifier = modifier.onGloballyPositioned { coordinates[0] = it }
    val notes = noteAt(row.notes, daysAgo)
    if (habit.isNumerical) {
        NumberCell(
            value = numberAt(row.checkmarks, daysAgo),
            completed = completedAt(row.completed, daysAgo),
            units = habit.unit,
            notes = notes,
            tones = tones,
            questionMarksEnabled = preferences.areQuestionMarksEnabled,
            onEdit = {
                val at = locate()
                callbacks.onEdit(habit, date, at.x, at.y)
            },
            modifier = cellModifier
        )
    } else {
        CheckmarkCell(
            value = checkmarkAt(row.checkmarks, daysAgo),
            notes = notes,
            tones = tones,
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
            modifier = cellModifier
        )
    }
}

/**
 * The centre of a cell in the coordinates the old rows reported: window position minus [insets] (the display
 * cutout on the left and, on Android before 16, the status bar, because the window was not edge to edge there).
 */
private fun windowCenter(insets: Offset, coordinates: LayoutCoordinates?): Offset {
    if (coordinates == null || !coordinates.isAttached) return Offset.Zero
    val centre = coordinates.positionInWindow() + Offset(coordinates.size.width / 2f, coordinates.size.height / 2f)
    return centre - insets
}

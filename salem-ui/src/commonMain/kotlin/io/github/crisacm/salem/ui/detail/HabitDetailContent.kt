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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import io.github.crisacm.salem.core.ui.screens.habits.show.ShowHabitState
import io.github.crisacm.salem.platform.time.LocalDateFormatter
import io.github.crisacm.salem.ui.list.exposeTestTags

/**
 * The detail screen of a habit in a column that scrolls: the [header] first, then the cards in the order of
 * [visibleCards], each one in a rounded panel (the overview has its own).
 */
@Composable
fun HabitDetailContent(
    state: ShowHabitState,
    callbacks: ShowHabitCallbacks,
    dateFormatter: LocalDateFormatter,
    modifier: Modifier = Modifier,
    header: @Composable () -> Unit = {}
) {
    Column(
        modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .exposeTestTags()
            .testTag("showHabitCards"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        for (shown in visibleCards(state.isNumerical, state.notes.description.isNotEmpty())) {
            when (shown) {
                ShowCard.SUBTITLE -> header()

                ShowCard.OVERVIEW -> OverviewCardContent(state.overview)

                ShowCard.TARGET -> ExpressiveCard { TargetCardContent(state.target) }

                ShowCard.SCORE -> ExpressiveCard { ScoreCardContent(state.scores, callbacks.onScoreSpinner, dateFormatter) }

                ShowCard.BAR -> ExpressiveCard {
                    BarCardContent(state.bar, callbacks.onBoolSpinner, callbacks.onNumericalSpinner, dateFormatter)
                }

                ShowCard.FREQUENCY -> ExpressiveCard { FrequencyCardContent(state.frequency, dateFormatter) }

                ShowCard.STREAK -> ExpressiveCard { StreakCardContent(state.streaks, dateFormatter) }

                ShowCard.HISTORY -> ExpressiveCard { HistoryCardContent(state.history, callbacks.onHistoryEdit, dateFormatter) }

                ShowCard.NOTES -> ExpressiveCard {
                    NotesCardContent(state.notes, MaterialTheme.colorScheme.onSurface)
                }
            }
        }
        // The last card stays above the navigation bar and the keyboard.
        Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.systemBars.union(WindowInsets.ime)))
    }
}

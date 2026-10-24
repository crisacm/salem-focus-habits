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

package io.github.crisacm.salem.activities.habits.show

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.crisacm.salem.activities.habits.show.views.BarCardContent
import io.github.crisacm.salem.activities.habits.show.views.FrequencyCardContent
import io.github.crisacm.salem.activities.habits.show.views.HistoryCardContent
import io.github.crisacm.salem.activities.habits.show.views.NotesCardContent
import io.github.crisacm.salem.activities.habits.show.views.OverviewCardContent
import io.github.crisacm.salem.activities.habits.show.views.ScoreCardContent
import io.github.crisacm.salem.activities.habits.show.views.StreakCardContent
import io.github.crisacm.salem.activities.habits.show.views.SubtitleCardContent
import io.github.crisacm.salem.activities.habits.show.views.TargetCardContent
import io.github.crisacm.salem.core.ui.screens.habits.show.ShowHabitState
import io.github.crisacm.salem.platform.gui.toInt

/** What the cards of the detail screen ask of the presenters. */
class ShowHabitCallbacks(
    val onScoreSpinner: (Int) -> Unit = {},
    val onHistoryEdit: () -> Unit = {},
    val onBoolSpinner: (Int) -> Unit = {},
    val onNumericalSpinner: (Int) -> Unit = {}
)

/** The cards of the detail screen of a habit in a column that scrolls, as the old layout had them. */
@Composable
fun ShowHabitContent(
    state: ShowHabitState,
    callbacks: ShowHabitCallbacks = ShowHabitCallbacks(),
    modifier: Modifier = Modifier
) {
    val theme = state.theme
    Column(
        modifier
            .fillMaxSize()
            .background(Color(theme.appBackgroundColor.toInt()))
            .verticalScroll(rememberScrollState())
            .semantics { testTagsAsResourceId = true }
            .testTag("showHabitCards")
    ) {
        for (card in visibleCards(state.isNumerical, state.notes.description.isNotEmpty())) {
            when (card) {
                ShowCard.SUBTITLE -> Box(
                    Modifier
                        .shadow(2.dp)
                        .background(Color(theme.headerBackgroundColor.toInt()))
                        .fillMaxWidth()
                        .padding(start = 60.dp, top = 15.dp, end = 10.dp, bottom = 10.dp)
                ) { SubtitleCardContent(state.subtitle) }

                ShowCard.NOTES -> HabitCard(theme.cardBackgroundColor.toInt()) {
                    NotesCardContent(state.notes, Color(theme.highContrastTextColor.toInt()))
                }

                ShowCard.OVERVIEW -> HabitCard(theme.cardBackgroundColor.toInt(), top = 12.dp) {
                    OverviewCardContent(state.overview)
                }

                ShowCard.TARGET -> HabitCard(theme.cardBackgroundColor.toInt(), top = 12.dp) {
                    TargetCardContent(state.target)
                }

                ShowCard.SCORE -> HabitCard(theme.cardBackgroundColor.toInt()) {
                    ScoreCardContent(state.scores, callbacks.onScoreSpinner)
                }

                ShowCard.BAR -> HabitCard(theme.cardBackgroundColor.toInt()) {
                    BarCardContent(state.bar, callbacks.onBoolSpinner, callbacks.onNumericalSpinner)
                }

                ShowCard.HISTORY -> HabitCard(theme.cardBackgroundColor.toInt(), bottom = 0.dp) {
                    HistoryCardContent(state.history, callbacks.onHistoryEdit)
                }

                ShowCard.STREAK -> HabitCard(theme.cardBackgroundColor.toInt()) { StreakCardContent(state.streaks) }

                ShowCard.FREQUENCY -> HabitCard(theme.cardBackgroundColor.toInt()) { FrequencyCardContent(state.frequency) }
            }
        }
        // The last card stays above the navigation bar and the keyboard.
        Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.systemBars.union(WindowInsets.ime)))
    }
}

/** A card of the detail screen: the colour of the card, a little elevation and the padding of the old `Card` style. */
@Composable
private fun HabitCard(
    background: Int,
    top: Dp = 16.dp,
    bottom: Dp = 16.dp,
    content: @Composable () -> Unit
) {
    Box(
        Modifier
            .padding(start = 3.dp, end = 3.dp, bottom = 1.dp)
            .shadow(1.dp)
            .background(Color(background))
            .fillMaxWidth()
            .padding(start = 16.dp, top = top, end = 4.dp, bottom = bottom)
    ) { content() }
}

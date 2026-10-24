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

import android.content.Context
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ViewCompositionStrategy
import io.github.crisacm.salem.core.ui.screens.habits.show.ShowHabitPresenter
import io.github.crisacm.salem.core.ui.screens.habits.show.ShowHabitState
import io.github.crisacm.salem.databinding.ShowHabitBinding
import io.github.crisacm.salem.ui.compose.SalemTheme
import io.github.crisacm.salem.utils.applyToolbarInsets
import io.github.crisacm.salem.utils.setupToolbar

/** The detail screen of a habit: the toolbar is still a View, the cards are Compose. */
class ShowHabitView(context: Context) : FrameLayout(context) {
    private val binding = ShowHabitBinding.inflate(LayoutInflater.from(context))
    private val state = mutableStateOf<ShowHabitState?>(null)
    private val callbacks = mutableStateOf(ShowHabitCallbacks())

    init {
        binding.toolbar.applyToolbarInsets()
        binding.cards.setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        binding.cards.setContent {
            SalemTheme { state.value?.let { ShowHabitContent(it, callbacks.value) } }
        }
        addView(binding.root)
    }

    fun setState(data: ShowHabitState) {
        setupToolbar(
            binding.toolbar,
            title = data.title,
            color = data.color,
            theme = data.theme
        )
        state.value = data
    }

    fun setListener(presenter: ShowHabitPresenter) {
        callbacks.value = ShowHabitCallbacks(
            onScoreSpinner = presenter.scoreCardPresenter::onSpinnerPosition,
            onHistoryEdit = presenter.historyCardPresenter::onClickEditButton,
            onBoolSpinner = presenter.barCardPresenter::onBoolSpinnerPosition,
            onNumericalSpinner = presenter.barCardPresenter::onNumericalSpinnerPosition
        )
    }
}

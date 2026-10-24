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

package io.github.crisacm.salem.activities.habits.show.views

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import io.github.crisacm.salem.core.ui.screens.habits.show.views.ScoreCardPresenter
import io.github.crisacm.salem.core.ui.screens.habits.show.views.ScoreCardState
import io.github.crisacm.salem.ui.compose.SalemTheme

/** The score card of the detail screen: the card itself (padding, background) is still this View. */
class ScoreCardView(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {
    private val state = mutableStateOf<ScoreCardState?>(null)
    private var presenter: ScoreCardPresenter? = null

    init {
        addView(
            ComposeView(context).apply {
                setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
                setContent {
                    SalemTheme {
                        state.value?.let { ScoreCardContent(it, onSpinner = { position -> presenter?.onSpinnerPosition(position) }) }
                    }
                }
            },
            MATCH_PARENT,
            WRAP_CONTENT
        )
    }

    fun setState(state: ScoreCardState) {
        this.state.value = state
    }

    fun setListener(presenter: ScoreCardPresenter) {
        this.presenter = presenter
    }
}

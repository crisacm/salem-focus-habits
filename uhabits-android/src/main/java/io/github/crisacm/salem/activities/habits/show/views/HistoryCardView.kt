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
import android.view.LayoutInflater
import android.widget.LinearLayout
import io.github.crisacm.salem.platform.gui.toInt
import io.github.crisacm.salem.platform.time.JavaLocalDateFormatter
import io.github.crisacm.salem.core.ui.screens.habits.show.views.HistoryCardPresenter
import io.github.crisacm.salem.core.ui.screens.habits.show.views.HistoryCardState
import io.github.crisacm.salem.core.ui.views.HistoryChart
import io.github.crisacm.salem.databinding.ShowHabitHistoryBinding
import java.util.Locale

class HistoryCardView(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    private var binding = ShowHabitHistoryBinding.inflate(LayoutInflater.from(context), this)

    fun setState(state: HistoryCardState) {
        val androidColor = state.theme.color(state.color).toInt()
        binding.title.setTextColor(androidColor)
        binding.chart.view = HistoryChart(
            today = state.today,
            paletteColor = state.color,
            theme = state.theme,
            dateFormatter = JavaLocalDateFormatter(Locale.getDefault()),
            series = state.series,
            defaultSquare = state.defaultSquare,
            notesIndicators = state.notesIndicators,
            firstWeekday = state.firstWeekday
        )
        binding.chart.postInvalidate()
    }

    fun setListener(presenter: HistoryCardPresenter) {
        binding.edit.setOnClickListener { presenter.onClickEditButton() }
    }
}

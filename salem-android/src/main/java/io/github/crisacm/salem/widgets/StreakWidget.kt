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

package io.github.crisacm.salem.widgets

import android.app.PendingIntent
import android.content.Context
import android.view.View
import android.view.ViewGroup.LayoutParams
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import io.github.crisacm.salem.core.models.Habit
import io.github.crisacm.salem.core.ui.views.StreakChart
import io.github.crisacm.salem.core.ui.views.WidgetTheme
import io.github.crisacm.salem.platform.gui.CoreWidgetView
import io.github.crisacm.salem.platform.time.JavaLocalDateFormatter
import io.github.crisacm.salem.widgets.views.GraphWidgetView
import java.util.Locale
import kotlin.math.floor

class StreakWidget(
    context: Context,
    id: Int,
    private val habit: Habit,
    stacked: Boolean = false
) : BaseWidget(context, id, stacked) {
    override val defaultHeight: Int = 200
    override val defaultWidth: Int = 200

    override fun getOnClickPendingIntent(context: Context): PendingIntent =
        pendingIntentFactory.showHabit(habit)

    override fun refreshData(view: View) {
        val widgetView = view as GraphWidgetView
        widgetView.setBackgroundAlpha(preferedBackgroundAlpha)
        if (preferedBackgroundAlpha >= 255) widgetView.setShadowAlpha(0x4f)
        val theme = WidgetTheme()
        val chartView = widgetView.dataView as CoreWidgetView
        // As many rows as fit in the height of the widget; the View was measured before the data is set.
        val rowHeight = StreakChart.ROW_HEIGHT * context.resources.displayMetrics.density
        val maxStreakCount = floor(chartView.measuredHeight / rowHeight).toInt()
        chartView.view = StreakChart(
            theme = theme,
            formatDate = JavaLocalDateFormatter(Locale.getDefault())::longFormat,
            streaks = habit.streaks.getBest(maxStreakCount),
            color = theme.color(habit.color)
        )
    }

    override fun buildView(): View {
        return GraphWidgetView(context, CoreWidgetView(context)).apply {
            setTitle(habit.name)
            layoutParams = LayoutParams(MATCH_PARENT, MATCH_PARENT)
        }
    }
}

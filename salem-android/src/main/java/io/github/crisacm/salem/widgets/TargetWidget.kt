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
import io.github.crisacm.salem.activities.habits.list.toShortString
import io.github.crisacm.salem.activities.habits.show.views.intervalToLabel
import io.github.crisacm.salem.core.models.Habit
import io.github.crisacm.salem.core.ui.screens.habits.show.views.TargetCardPresenter
import io.github.crisacm.salem.core.ui.views.TargetChart
import io.github.crisacm.salem.core.ui.views.WidgetTheme
import io.github.crisacm.salem.platform.gui.CoreWidgetView
import io.github.crisacm.salem.widgets.views.GraphWidgetView
import kotlinx.coroutines.runBlocking

class TargetWidget(
    context: Context,
    id: Int,
    private val habit: Habit,
    stacked: Boolean = false
) : BaseWidget(context, id, stacked) {
    override val defaultHeight: Int = 200
    override val defaultWidth: Int = 200

    override fun getOnClickPendingIntent(context: Context): PendingIntent =
        pendingIntentFactory.showHabit(habit)

    override fun refreshData(view: View) = runBlocking {
        val widgetView = view as GraphWidgetView
        widgetView.setBackgroundAlpha(preferedBackgroundAlpha)
        if (preferedBackgroundAlpha >= 255) widgetView.setShadowAlpha(0x4f)
        val theme = WidgetTheme()
        val data = TargetCardPresenter.buildState(
            habit = habit,
            firstWeekday = prefs.firstWeekdayInt,
            theme = theme
        )
        (widgetView.dataView as CoreWidgetView).view = TargetChart(
            theme = theme,
            labels = data.intervals.map { intervalToLabel(context.resources, it) },
            values = data.values,
            targets = data.targets,
            color = theme.color(habit.color),
            formatValue = { it.toShortString() },
            // The widget shares its whole height among the rows.
            stretchRows = true
        )
    }

    override fun buildView(): View {
        return GraphWidgetView(context, CoreWidgetView(context)).apply {
            setTitle(habit.name)
            layoutParams = LayoutParams(MATCH_PARENT, MATCH_PARENT)
        }
    }
}

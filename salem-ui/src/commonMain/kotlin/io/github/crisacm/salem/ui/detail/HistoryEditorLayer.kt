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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.backhandler.BackHandler
import io.github.crisacm.salem.core.ui.screens.habits.show.views.HistoryCardState
import io.github.crisacm.salem.core.ui.views.OnDateClickedListener
import io.github.crisacm.salem.platform.time.LocalDateFormatter

/**
 * Whether the history editor is open over the detail screen, and who hears the taps on its days. It is a layer of its
 * own, not one of the `ListOverlays`, because the popup of a day opens above the editor and must not replace it.
 */
class HistoryEditorLayer {
    var listener by mutableStateOf<OnDateClickedListener?>(null)
        private set

    val isOpen: Boolean get() = listener != null

    /** Opens the editor; a second call only changes the listener, the editor does not stack. */
    fun show(listener: OnDateClickedListener) {
        this.listener = listener
    }

    fun dismiss() {
        listener = null
    }
}

/** Draws the history editor of [layer] with the [state] of the history card; back closes it. */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun HistoryEditorHost(layer: HistoryEditorLayer, state: HistoryCardState?, dateFormatter: LocalDateFormatter) {
    val listener = layer.listener ?: return
    val current = state ?: return
    BackHandler(onBack = layer::dismiss)
    HistoryEditorContent(current, listener, dateFormatter, onDismiss = layer::dismiss)
}

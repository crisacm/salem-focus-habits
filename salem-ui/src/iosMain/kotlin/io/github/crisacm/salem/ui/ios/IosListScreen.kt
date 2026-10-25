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

package io.github.crisacm.salem.ui.ios

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.github.crisacm.salem.core.io.Logger
import io.github.crisacm.salem.core.models.Habit
import io.github.crisacm.salem.core.models.PaletteColor
import io.github.crisacm.salem.core.ui.ThemeSwitcher
import io.github.crisacm.salem.core.ui.screens.habits.list.ListHabitsBehavior
import io.github.crisacm.salem.core.ui.screens.habits.list.ListHabitsMenuBehavior
import io.github.crisacm.salem.core.ui.screens.habits.list.ListHabitsSelectionMenuBehavior

/**
 * The actions of the habit list that need another screen. The iOS app shows the list and marks days for now: the
 * others (detail, edit, settings, dialogs, sharing) arrive with their screens, so they only leave a line in the log.
 * [lastMessage] holds the last message of the core, and [themeVersion] changes when the theme was applied again.
 */
class IosListScreen(
    private val logger: Logger,
    private val themeSwitcher: ThemeSwitcher
) : ListHabitsBehavior.Screen,
    ListHabitsMenuBehavior.Screen,
    ListHabitsSelectionMenuBehavior.Screen {

    var lastMessage by mutableStateOf<ListHabitsBehavior.Message?>(null)
        private set
    var themeVersion by mutableIntStateOf(0)
        private set

    private fun todo(what: String) = logger.info("Not available on iOS yet: $what")

    // ListHabitsBehavior.Screen
    override fun showHabitScreen(h: Habit) = todo("habit screen of ${h.name}")

    override fun showIntroScreen() = todo("intro")

    override fun showMessage(m: ListHabitsBehavior.Message) {
        lastMessage = m
        logger.info("Message: $m")
    }

    override fun showNumberPopup(value: Double, notes: String, callback: io.github.crisacm.salem.core.ui.callbacks.NumberPickerCallback) =
        todo("number popup")

    override fun showCheckmarkPopup(
        selectedValue: Int,
        notes: String,
        color: PaletteColor,
        callback: io.github.crisacm.salem.core.ui.callbacks.CheckMarkDialogCallback
    ) = todo("checkmark popup")

    override fun showSendBugReportToDeveloperScreen(log: String) = todo("send bug report")

    override fun showSendFileScreen(filename: String) = todo("share $filename")

    override fun showConfetti(color: PaletteColor, x: Float, y: Float) {}

    // ListHabitsMenuBehavior.Screen
    override fun applyTheme() {
        themeSwitcher.apply()
        themeVersion++
    }

    override fun showAboutScreen() = todo("about")

    override fun showFAQScreen() = todo("help")

    override fun showSettingsScreen() = todo("settings")

    override fun showSelectHabitTypeDialog() = todo("new habit")

    // ListHabitsSelectionMenuBehavior.Screen
    override fun showColorPicker(
        defaultColor: PaletteColor,
        callback: io.github.crisacm.salem.core.ui.callbacks.OnColorPickedCallback
    ) = todo("colour picker")

    override fun showDeleteConfirmationScreen(callback: io.github.crisacm.salem.core.ui.callbacks.OnConfirmedCallback, quantity: Int) =
        todo("delete $quantity habits")

    override fun showEditHabitsScreen(selected: List<Habit>) = todo("edit ${selected.size} habits")
}

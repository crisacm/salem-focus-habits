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
import io.github.crisacm.salem.ui.AppScreen
import io.github.crisacm.salem.ui.ScreenNavigation
import io.github.crisacm.salem.ui.about.SalemLinks
import io.github.crisacm.salem.ui.detail.DetailNavigation
import io.github.crisacm.salem.ui.list.ListOverlays

/**
 * The actions of the habit list that need another screen. The dialogs, the popups and the editor are opened through
 * [overlays]; the others (settings, sharing, intro) arrive with their screens, so they only leave a
 * line in the log. [lastMessage] holds the last message of the core, and [themeVersion] changes when the theme was
 * applied again.
 */
class IosListScreen(
    private val logger: Logger,
    private val themeSwitcher: ThemeSwitcher,
    private val overlays: ListOverlays,
    private val navigation: DetailNavigation,
    private val screens: ScreenNavigation,
    private val openUrl: (String) -> Unit
) : ListHabitsBehavior.Screen,
    ListHabitsMenuBehavior.Screen,
    ListHabitsSelectionMenuBehavior.Screen {

    var lastMessage by mutableStateOf<ListHabitsBehavior.Message?>(null)
        private set
    var themeVersion by mutableIntStateOf(0)
        private set

    private fun todo(what: String) = logger.info("Not available on iOS yet: $what")

    // ListHabitsBehavior.Screen
    override fun showHabitScreen(h: Habit) = navigation.open(h)

    override fun showIntroScreen() = todo("intro")

    override fun showMessage(m: ListHabitsBehavior.Message) {
        lastMessage = m
        logger.info("Message: $m")
    }

    override fun showNumberPopup(value: Double, notes: String, callback: io.github.crisacm.salem.core.ui.callbacks.NumberPickerCallback) =
        overlays.showNumber(value, notes, callback)

    override fun showCheckmarkPopup(
        selectedValue: Int,
        notes: String,
        color: PaletteColor,
        callback: io.github.crisacm.salem.core.ui.callbacks.CheckMarkDialogCallback
    ) = overlays.showCheckmark(selectedValue, notes, color, callback)

    override fun showSendBugReportToDeveloperScreen(log: String) = todo("send bug report")

    override fun showSendFileScreen(filename: String) = todo("share $filename")

    override fun showConfetti(color: PaletteColor, x: Float, y: Float) {}

    // ListHabitsMenuBehavior.Screen
    override fun applyTheme() {
        themeSwitcher.apply()
        themeVersion++
    }

    override fun showAboutScreen() = screens.open(AppScreen.ABOUT)

    override fun showFAQScreen() = openUrl(SalemLinks.HELP)

    override fun showSettingsScreen() = todo("settings")

    override fun showSelectHabitTypeDialog() = overlays.showHabitType()

    // ListHabitsSelectionMenuBehavior.Screen
    override fun showColorPicker(
        defaultColor: PaletteColor,
        callback: io.github.crisacm.salem.core.ui.callbacks.OnColorPickedCallback
    ) = overlays.showColor(defaultColor, callback)

    override fun showDeleteConfirmationScreen(callback: io.github.crisacm.salem.core.ui.callbacks.OnConfirmedCallback, quantity: Int) =
        overlays.showDelete(quantity, callback)

    // As on Android, only the first selected habit is edited
    override fun showEditHabitsScreen(selected: List<Habit>) {
        selected.firstOrNull()?.let { overlays.showEditor(it.id, it.type) }
    }
}

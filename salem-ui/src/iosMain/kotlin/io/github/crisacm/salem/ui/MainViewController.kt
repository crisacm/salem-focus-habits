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

package io.github.crisacm.salem.ui

import androidx.compose.ui.window.ComposeUIViewController
import io.github.crisacm.salem.ui.ios.IosListRoot
import io.github.crisacm.salem.ui.ios.IosNotificationRouter
import platform.UIKit.UIViewController

/**
 * The root view controller that the iOS app shows. It also takes the notification center's delegate right away, so a
 * tap on a reminder that launches the app is not lost. [onHabitsChanged] is called whenever the habits change (a
 * command, an import), so the shell can tell the widget to draw again.
 */
fun MainViewController(onHabitsChanged: () -> Unit): UIViewController {
    IosNotificationRouter.install()
    return ComposeUIViewController { IosListRoot(onHabitsChanged) }
}

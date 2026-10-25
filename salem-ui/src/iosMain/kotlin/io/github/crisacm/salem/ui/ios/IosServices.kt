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

import io.github.crisacm.salem.core.ui.screens.habits.list.ListHabitsBehavior
import io.github.crisacm.salem.core.ui.screens.habits.show.ShowHabitMenuPresenter
import io.github.crisacm.salem.platform.io.FileOpener
import io.github.crisacm.salem.platform.io.UserFile
import platform.Foundation.NSBundle
import platform.UIKit.UIDevice

/** The report of an iOS app is its version and the device; there is no log file to dump yet. */
class IosBugReporter : ListHabitsBehavior.BugReporter {
    override fun dumpBugReportToFile() {}

    override fun getBugReport(): String {
        val info = NSBundle.mainBundle.infoDictionary
        val version = info?.get("CFBundleShortVersionString") ?: "unknown"
        val device = UIDevice.currentDevice
        return "Salem $version\n${device.systemName} ${device.systemVersion}, ${device.model}\n"
    }
}

/** The folder of the CSV export, inside the files of the app. */
class IosDirFinder(private val fileOpener: FileOpener) : ListHabitsBehavior.DirFinder, ShowHabitMenuPresenter.System {
    override fun getCSVOutputDir(): UserFile = fileOpener.openUserFile("CSV")
}

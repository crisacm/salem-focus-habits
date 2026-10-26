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
import io.github.crisacm.salem.platform.io.AppleUserFile
import io.github.crisacm.salem.platform.io.UserFile
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSBundle
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenNotificationSettingsURLString
import platform.UIKit.UIDevice

/** The version of the app as the user sees it, from the Info.plist. */
fun iosVersionName(): String = NSBundle.mainBundle.infoDictionary?.get("CFBundleShortVersionString") as? String ?: "unknown"

/** Opens [url] in the browser (or the app that handles it); a malformed address does nothing. */
fun openUrl(url: String) {
    val target = NSURL.URLWithString(url) ?: return
    UIApplication.sharedApplication.openURL(target, emptyMap<Any?, Any?>(), null)
}

/** Opens the notification settings of the app (iOS 16 or later, which is the minimum of the app). */
fun openNotificationSettings() = openUrl(UIApplicationOpenNotificationSettingsURLString)

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

/**
 * The folder where the files to share are written: in the caches of the app, which the system may empty and the Files
 * app does not show. [freshFolder] empties it first, so the exports do not pile up.
 */
object IosExports {
    @OptIn(ExperimentalForeignApi::class)
    fun freshFolder(): UserFile {
        val manager = NSFileManager.defaultManager
        val caches = manager.URLsForDirectory(NSCachesDirectory, NSUserDomainMask).first() as NSURL
        val folder = "${checkNotNull(caches.path) { "No caches directory" }}/Exports"
        manager.removeItemAtPath(folder, null)
        check(manager.createDirectoryAtPath(folder, withIntermediateDirectories = true, attributes = null, error = null)) {
            "Cannot create $folder"
        }
        return AppleUserFile(folder)
    }
}

/** The CSV export of one habit or of all of them is written to the folder of the exports. */
class IosDirFinder : ListHabitsBehavior.DirFinder, ShowHabitMenuPresenter.System {
    override fun getCSVOutputDir(): UserFile = IosExports.freshFolder()
}

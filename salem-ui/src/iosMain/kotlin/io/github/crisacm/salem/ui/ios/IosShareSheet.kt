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

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSURL
import platform.UIKit.UIActivityViewController
import platform.UIKit.popoverPresentationController

/**
 * The share sheet of the system (AirDrop, Messages, Save to Files...). Call it from the main thread. The files shared
 * are in the folder of the exports of the app ([IosExports]), which is emptied before the next export, so they are not
 * deleted here: the receiving app may still be reading one when the sheet closes.
 */
@OptIn(ExperimentalForeignApi::class)
object IosShareSheet {
    fun shareFile(path: String) = present(NSURL.fileURLWithPath(path))

    fun shareText(text: String) = present(text)

    private fun present(item: Any) {
        val host = topViewController() ?: return
        val sheet = UIActivityViewController(activityItems = listOf(item), applicationActivities = null)
        // On an iPad the sheet is a popover, which crashes without an anchor; with no arrow it is centred on the view
        sheet.popoverPresentationController?.let {
            it.sourceView = host.view
            it.sourceRect = host.view.bounds
            it.permittedArrowDirections = 0u
        }
        host.presentViewController(sheet, animated = true, completion = null)
    }
}

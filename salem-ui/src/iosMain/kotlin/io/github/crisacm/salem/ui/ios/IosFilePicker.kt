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

import io.github.crisacm.salem.platform.io.AppleUserFile
import io.github.crisacm.salem.platform.io.UserFile
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSURL
import platform.UIKit.UIDocumentPickerDelegateProtocol
import platform.UIKit.UIDocumentPickerViewController
import platform.UniformTypeIdentifiers.UTTypeItem
import platform.darwin.NSObject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Lets the user choose a file from the Files app. The system gives the app a copy of the file in its own temporary
 * folder (`asCopy`), so nothing needs security-scoped access and the importers, which migrate the file in place, never
 * touch the original. The caller deletes the copy. Call it from the main thread.
 */
object IosFilePicker {
    // The picker keeps its delegate weakly
    private var delegate: PickerDelegate? = null

    /** The copy of the chosen file, or null when the user cancels. */
    suspend fun pickFile(): UserFile? = suspendCancellableCoroutine { continuation ->
        val host = topViewController()
        if (host == null) {
            continuation.resumeWithException(IllegalStateException("There is no window to show the file picker"))
            return@suspendCancellableCoroutine
        }
        val picker = UIDocumentPickerViewController(forOpeningContentTypes = listOf(UTTypeItem), asCopy = true)
        picker.allowsMultipleSelection = false
        val pickerDelegate = PickerDelegate { path ->
            delegate = null
            if (continuation.isActive) continuation.resume(path?.let { AppleUserFile(it) })
        }
        delegate = pickerDelegate
        picker.delegate = pickerDelegate
        continuation.invokeOnCancellation {
            delegate = null
            picker.dismissViewControllerAnimated(true, completion = null)
        }
        host.presentViewController(picker, animated = true, completion = null)
    }
}

private class PickerDelegate(private val onResult: (String?) -> Unit) : NSObject(), UIDocumentPickerDelegateProtocol {
    override fun documentPicker(controller: UIDocumentPickerViewController, didPickDocumentsAtURLs: List<*>) {
        onResult((didPickDocumentsAtURLs.firstOrNull() as? NSURL)?.path)
    }

    override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) = onResult(null)
}

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

package io.github.crisacm.salem.acceptance

import androidx.test.filters.LargeTest
import io.github.crisacm.salem.BaseUserInterfaceTest
import io.github.crisacm.salem.acceptance.steps.CommonSteps.clickComposeText
import io.github.crisacm.salem.acceptance.steps.CommonSteps.launchApp
import io.github.crisacm.salem.acceptance.steps.CommonSteps.longClickText
import io.github.crisacm.salem.acceptance.steps.CommonSteps.verifyDisplaysText
import io.github.crisacm.salem.acceptance.steps.CommonSteps.verifyDoesNotDisplayText
import io.github.crisacm.salem.acceptance.steps.ListHabitsSteps.MenuItem.DELETE
import io.github.crisacm.salem.acceptance.steps.ListHabitsSteps.clickMenu
import io.github.crisacm.salem.acceptance.steps.clearBackupFolder
import io.github.crisacm.salem.acceptance.steps.clearDownloadFolder
import io.github.crisacm.salem.acceptance.steps.clearPublicBackupFolderSelection
import io.github.crisacm.salem.acceptance.steps.copyBackupToDownloadFolder
import io.github.crisacm.salem.acceptance.steps.exportFullBackup
import io.github.crisacm.salem.acceptance.steps.importBackupFromDownloadFolder
import io.github.crisacm.salem.acceptance.steps.selectPublicBackupFolder
import io.github.crisacm.salem.acceptance.steps.verifyBackupInDownloadFolder
import org.junit.Test

@LargeTest
class BackupTest : BaseUserInterfaceTest() {
    @Test
    fun shouldExportAndImportBackup() {
        launchApp()
        clearDownloadFolder()
        clearBackupFolder()
        exportFullBackup()
        copyBackupToDownloadFolder()
        longClickText("Wake up early")
        clickMenu(DELETE)
        clickComposeText("Yes")
        verifyDoesNotDisplayText("Wake up early")
        importBackupFromDownloadFolder()
        verifyDisplaysText("Wake up early")
    }

    @Test
    fun shouldExportBackupToPublicFolder() {
        launchApp()
        clearDownloadFolder()
        clearBackupFolder()
        selectPublicBackupFolder()
        exportFullBackup()
        verifyBackupInDownloadFolder()
        clearPublicBackupFolderSelection()
    }
}

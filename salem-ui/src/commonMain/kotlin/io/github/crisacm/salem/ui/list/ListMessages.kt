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
package io.github.crisacm.salem.ui.list

import io.github.crisacm.salem.core.ui.screens.habits.list.ListHabitsBehavior.Message
import io.github.crisacm.salem.ui.resources.Res
import io.github.crisacm.salem.ui.resources.bug_report_failed
import io.github.crisacm.salem.ui.resources.could_not_export
import io.github.crisacm.salem.ui.resources.could_not_import
import io.github.crisacm.salem.ui.resources.database_repaired
import io.github.crisacm.salem.ui.resources.file_not_recognized
import io.github.crisacm.salem.ui.resources.habits_imported
import org.jetbrains.compose.resources.StringResource

/** The text of a message of the habit list behavior, the same ones the Android screen shows. */
fun listMessage(message: Message): StringResource = when (message) {
    Message.COULD_NOT_EXPORT -> Res.string.could_not_export
    Message.IMPORT_SUCCESSFUL -> Res.string.habits_imported
    Message.IMPORT_FAILED -> Res.string.could_not_import
    Message.DATABASE_REPAIRED -> Res.string.database_repaired
    Message.COULD_NOT_GENERATE_BUG_REPORT -> Res.string.bug_report_failed
    Message.FILE_NOT_RECOGNIZED -> Res.string.file_not_recognized
}

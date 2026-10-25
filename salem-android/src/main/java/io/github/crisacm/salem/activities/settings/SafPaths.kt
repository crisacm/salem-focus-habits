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

package io.github.crisacm.salem.activities.settings

/** Path of a SAF tree document id such as `primary:Backups` or `1234-ABCD:Docs`. */
fun treeDocumentPath(documentId: String, primaryStorage: String): String {
    val parts = documentId.split(":", limit = 2)
    val type = parts[0]
    val relative = parts.getOrElse(1) { "" }
    val base = if (type.equals("primary", true)) primaryStorage else "/storage/$type"
    return if (relative.isEmpty()) base else "$base/$relative"
}

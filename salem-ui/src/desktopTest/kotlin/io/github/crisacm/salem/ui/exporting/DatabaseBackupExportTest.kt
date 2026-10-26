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
package io.github.crisacm.salem.ui.exporting

import io.github.crisacm.salem.core.ui.screens.habits.list.ListHabitsBehavior
import io.github.crisacm.salem.platform.io.JavaUserFile
import io.github.crisacm.salem.platform.time.LocalDate
import io.github.crisacm.salem.ui.list.listMessage
import io.github.crisacm.salem.ui.resources.Res
import io.github.crisacm.salem.ui.resources.could_not_export
import io.github.crisacm.salem.ui.resources.database_repaired
import kotlinx.coroutines.runBlocking
import org.junit.Test
import java.nio.file.Files
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DatabaseBackupExportTest {
    private val folder = JavaUserFile(Files.createTempDirectory("exports").resolve("Exports"))
    private val source = JavaUserFile(Files.createTempFile("app", ".db"))
    private val shared = mutableListOf<String>()
    private var failures = 0

    private fun export(from: JavaUserFile = source, shareFails: Boolean = false) = DatabaseBackupExport(
        source = from,
        folder = { folder },
        today = { LocalDate(2026, 10, 25) },
        share = {
            if (shareFails) throw IllegalStateException("No window")
            shared += it
        },
        onFailed = { failures++ }
    )

    @Test
    fun copiesTheDatabaseAndSharesTheCopy() = runBlocking {
        runBlocking { source.writeBytes(byteArrayOf(1, 2, 3, 4)) }
        export().start()
        assertEquals(1, shared.size)
        assertTrue(shared.single().endsWith("Salem Backup 2026-10-25.db"))
        assertEquals(listOf<Byte>(1, 2, 3, 4), java.io.File(shared.single()).readBytes().toList())
        assertEquals(0, failures)
    }

    @Test
    fun aDatabaseThatCannotBeCopiedIsAFailureAndNothingIsShared() = runBlocking {
        export(from = JavaUserFile(Files.createTempDirectory("nothing").resolve("missing.db"))).start()
        assertTrue(shared.isEmpty())
        assertEquals(1, failures)
    }

    @Test
    fun aShareThatFailsIsAFailure() = runBlocking {
        runBlocking { source.writeBytes(byteArrayOf(9)) }
        export(shareFails = true).start()
        assertEquals(1, failures)
    }

    @Test
    fun everyMessageOfTheCoreHasItsText() {
        val texts = ListHabitsBehavior.Message.entries.map { listMessage(it) }
        assertEquals(ListHabitsBehavior.Message.entries.size, texts.toSet().size)
        assertEquals(Res.string.could_not_export, listMessage(ListHabitsBehavior.Message.COULD_NOT_EXPORT))
        assertEquals(Res.string.database_repaired, listMessage(ListHabitsBehavior.Message.DATABASE_REPAIRED))
    }
}

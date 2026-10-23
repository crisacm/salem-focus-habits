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

package io.github.crisacm.salem.platform.io

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SqliterDatabaseTest {
    private val opener = SqliterDatabaseOpener()

    private fun newPath(name: String): String = tempPath(name).also {
        deleteIfExists(it)
        deleteIfExists("$it-wal")
        deleteIfExists("$it-shm")
    }

    @Test
    fun testNewFileReportsVersionZero() {
        val path = newPath("sqliter_new.db")
        val db = opener.open(path)
        assertEquals(0, db.getVersion())
        db.close()
        assertTrue(fileExists(path))
    }

    @Test
    fun testExistingDatabasesKeepTheirVersion() {
        val files = listOf("loop.db" to 12, "rewire.db" to 24, "tickmate.db" to 13, "databases/021.db" to 21)
        for ((file, version) in files) {
            val path = copyAssetToTemp("test/$file", "sqliter_foreign.db")
            val db = opener.open(path)
            assertEquals(version, db.getVersion(), file)
            db.close()
            assertEquals(version, peekUserVersion(path), "$file after closing")
        }
    }

    @Test
    fun testFileWithVersionZeroStaysAtZero() {
        val path = newPath("sqliter_zero.db")
        val first = opener.open(path)
        first.run("create table t(x integer)")
        first.run("insert into t values (1)")
        first.close()
        val second = opener.open(path)
        assertEquals(0, second.getVersion())
        assertEquals(1, second.queryInt("select count(*) from t"))
        second.close()
        assertEquals(0, peekUserVersion(path))
    }

    @Test
    fun testSetVersionPersistsAcrossReopen() {
        val path = newPath("sqliter_version.db")
        val first = opener.open(path)
        first.setVersion(7)
        first.close()
        val second = opener.open(path)
        assertEquals(7, second.getVersion())
        second.close()
    }

    @Test
    fun testNoJournalFilesAreLeftBehind() {
        val path = newPath("sqliter_journal.db")
        val db = opener.open(path)
        db.run("create table t(x integer)")
        db.run("insert into t values (1)")
        assertFalse(fileExists("$path-wal"))
        assertFalse(fileExists("$path-shm"))
        db.close()
    }

    @Test
    fun testDatabaseInWalModeIsOpenedAndConvertedToASingleFile() {
        val path = newPath("sqliter_wal.db")
        val first = opener.open(path)
        first.run("PRAGMA journal_mode = WAL")
        first.run("create table x(v integer)")
        first.run("insert into x values (1)")
        first.close()
        val second = opener.open(path)
        assertEquals(1, second.queryInt("select count(*) from x"))
        assertFalse(fileExists("$path-wal"))
        second.close()
    }

    @Test
    fun testTypedBindsRoundTrip() {
        val db = opener.open(newPath("sqliter_binds.db"))
        db.run("create table x(i integer, r real, t text, n text)")
        val insert = db.prepareStatement("insert into x(i, r, t, n) values (?, ?, ?, ?)")
        insert.bindLong(1, 1_700_000_000_000L)
        insert.bindReal(2, 2.5)
        insert.bindText(3, "héllo ✓")
        insert.bindNull(4)
        insert.step()
        insert.finalize()
        val select = db.prepareStatement("select i, r, t, n from x")
        assertEquals(StepResult.ROW, select.step())
        assertEquals(1_700_000_000_000L, select.getLong(0))
        assertEquals(2.5, select.getReal(1))
        assertEquals("héllo ✓", select.getText(2))
        assertNull(select.getTextOrNull(3))
        assertEquals(StepResult.DONE, select.step())
        select.finalize()
        db.close()
    }

    @Test
    fun testNestedTransactionsCommitWhenTheOutermostEnds() {
        val path = newPath("sqliter_tx.db")
        val db = opener.open(path)
        db.run("create table x(v integer)")
        // Opening a connection changes the journal mode, which needs the database unlocked, so the
        // reader is opened before the transaction starts
        val reader = opener.open(path)
        db.begin()
        db.begin()
        db.run("insert into x values (1)")
        db.commit()
        assertEquals(0, reader.queryInt("select count(*) from x"), "the inner commit must not commit")
        db.commit()
        assertEquals(1, reader.queryInt("select count(*) from x"), "the outermost commit must commit")
        reader.close()
        db.close()
    }

    @Test
    fun testCommitWithoutBeginIsIgnored() {
        val db = opener.open(newPath("sqliter_commit.db"))
        db.commit()
        db.run("create table x(v integer)")
        db.close()
    }

    @Test
    fun testLastInsertRowIdInsideTransaction() {
        val db = opener.open(newPath("sqliter_rowid.db"))
        db.run("create table x(id integer primary key autoincrement, v text)")
        db.begin()
        db.run("insert into x(v) values (?)") { bindText(1, "a") }
        assertEquals(1, db.queryLong("select last_insert_rowid()"))
        assertEquals(1, db.queryInt("select count(*) from x"))
        db.commit()
        db.close()
    }

    @Test
    fun testTwoStatementsCanBeAliveAtTheSameTime() {
        val db = opener.open(newPath("sqliter_two.db"))
        db.run("create table x(v integer)")
        for (v in 1..3) db.run("insert into x values (?)") { bindInt(1, v) }
        val ascending = db.prepareStatement("select v from x order by v")
        val descending = db.prepareStatement("select v from x order by v desc")
        assertEquals(StepResult.ROW, ascending.step())
        assertEquals(StepResult.ROW, descending.step())
        assertEquals(1, ascending.getInt(0))
        assertEquals(3, descending.getInt(0))
        assertEquals(StepResult.ROW, ascending.step())
        assertEquals(StepResult.ROW, descending.step())
        assertEquals(2, ascending.getInt(0))
        assertEquals(2, descending.getInt(0))
        ascending.finalize()
        descending.finalize()
        db.close()
    }

    @Test
    fun testInMemoryDatabasesAreIndependent() {
        val a = opener.open(":memory:")
        a.run("create table x(v integer)")
        a.run("insert into x values (1)")
        val b = opener.open(":memory:")
        assertEquals(0, b.queryInt("select count(*) from sqlite_master"))
        assertEquals(1, a.queryInt("select count(*) from x"))
        a.close()
        b.close()
    }

    @Test
    fun testInMemoryDatabasesFromDifferentOpenersAreIndependent() {
        // TestDatabaseHelper and createTestDatabaseOpener create a new opener for every database
        val a = SqliterDatabaseOpener().open(":memory:")
        a.run("create table x(v integer)")
        a.run("insert into x values (1)")
        val b = SqliterDatabaseOpener().open(":memory:")
        assertEquals(0, b.queryInt("select count(*) from sqlite_master"))
        assertEquals(1, a.queryInt("select count(*) from x"))
        a.close()
        b.close()
    }

    @Test
    fun testClosingWithALiveStatementDoesNotCrash() {
        val db = opener.open(newPath("sqliter_close.db"))
        db.run("create table x(v integer)")
        db.run("insert into x values (1)")
        val select = db.prepareStatement("select v from x")
        assertEquals(StepResult.ROW, select.step())
        db.close()
    }
}

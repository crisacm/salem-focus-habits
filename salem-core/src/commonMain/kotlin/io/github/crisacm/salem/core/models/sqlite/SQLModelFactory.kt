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
package io.github.crisacm.salem.core.models.sqlite

import io.github.crisacm.salem.core.database.EntryRepository
import io.github.crisacm.salem.core.database.HabitRepository
import io.github.crisacm.salem.core.models.EntryList
import io.github.crisacm.salem.core.models.ModelFactory
import io.github.crisacm.salem.core.models.ScoreList
import io.github.crisacm.salem.core.models.StreakList
import me.tatarka.inject.annotations.Inject

/**
 * Factory that provides models backed by an SQLite database.
 */
@Inject
class SQLModelFactory(
    val database: io.github.crisacm.salem.platform.io.Database
) : ModelFactory {
    val habitRepository = HabitRepository(database)
    val entryRepository = EntryRepository(database)

    override fun buildOriginalEntries() = SQLiteEntryList(entryRepository)
    override fun buildComputedEntries() = EntryList()
    override fun buildHabitList() = SQLiteHabitList(this)
    override fun buildScoreList() = ScoreList()
    override fun buildStreakList() = StreakList()
}

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

import io.github.crisacm.salem.core.models.HabitList
import io.github.crisacm.salem.core.models.ModelFactory
import io.github.crisacm.salem.core.models.PaletteColor
import io.github.crisacm.salem.core.test.HabitFixtures
import platform.Foundation.NSProcessInfo
import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.Platform

private const val SEED_VARIABLE = "SALEM_SEED_DEBUG_DATA"

/**
 * Example habits to look at while the iOS app cannot create habits: only when the environment variable
 * `SALEM_SEED_DEBUG_DATA` is `1` (`SIMCTL_CHILD_SALEM_SEED_DEBUG_DATA=1` with `simctl launch`), the list is empty and
 * the binary is a debug one.
 */
@OptIn(ExperimentalNativeApi::class)
fun seedDebugHabitsIfRequested(modelFactory: ModelFactory, habitList: HabitList) {
    // Never in a release binary, whatever the environment says
    if (!Platform.isDebugBinary) return
    if (NSProcessInfo.processInfo.environment[SEED_VARIABLE] != "1") return
    if (habitList.count() > 0) return
    val fixtures = HabitFixtures(modelFactory, habitList)
    fixtures.createLongHabit().also { it.name = "Meditate" }
    fixtures.createNumericalHabit()
    fixtures.createShortHabit().also {
        it.name = "Wake up early"
        it.color = PaletteColor(8)
    }
    fixtures.createEmptyHabit("Read a book", PaletteColor(11), 3)
    habitList.update(habitList.toList())
}

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

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Brush
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.CleaningServices
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Extension
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.LocalCafe
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.MonitorHeart
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.Park
import androidx.compose.material.icons.outlined.PedalBike
import androidx.compose.material.icons.outlined.Pets
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.SelfImprovement
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * The icons a habit can wear in the list. Habits do not store an icon yet, so [habitIconFor] picks one
 * from the pool; the pool only grows at its end so that the icon of a habit never changes.
 */
val HabitIconPool: List<ImageVector> by lazy {
    listOf(
        Icons.Outlined.WbSunny,
        Icons.Outlined.Restaurant,
        Icons.Outlined.Edit,
        Icons.Outlined.Schedule,
        Icons.Outlined.Eco,
        Icons.Outlined.MonitorHeart,
        Icons.Outlined.MenuBook,
        Icons.Outlined.Language,
        Icons.Outlined.Extension,
        Icons.Outlined.MusicNote,
        Icons.Outlined.Call,
        Icons.Outlined.FitnessCenter,
        Icons.Outlined.SelfImprovement,
        Icons.Outlined.WaterDrop,
        Icons.Outlined.Bedtime,
        Icons.Outlined.LocalCafe,
        Icons.Outlined.Brush,
        Icons.Outlined.Code,
        Icons.Outlined.Savings,
        Icons.Outlined.Pets,
        Icons.Outlined.Park,
        Icons.Outlined.Spa,
        Icons.Outlined.PedalBike,
        Icons.Outlined.Translate,
        Icons.Outlined.School,
        Icons.Outlined.CleaningServices,
        Icons.Outlined.Psychology,
        Icons.Outlined.Bolt
    )
}

/** The position in a pool of [size] icons for a habit identified by [key]; the same key always gives the same one. */
fun habitIconIndex(key: String, size: Int): Int {
    // A simple polynomial hash instead of String.hashCode, so the result does not depend on the platform.
    var hash = 17
    for (char in key) hash = hash * 31 + char.code
    return (hash and Int.MAX_VALUE) % size
}

/** The icon of a habit: stable for its uuid (or its id when it has none), without storing anything. */
fun habitIconFor(uuid: String?, id: Long?): ImageVector {
    val pool = HabitIconPool
    return pool[habitIconIndex(uuid ?: id?.toString() ?: "", pool.size)]
}

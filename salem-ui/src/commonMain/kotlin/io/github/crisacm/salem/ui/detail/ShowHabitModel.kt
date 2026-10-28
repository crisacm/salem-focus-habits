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

package io.github.crisacm.salem.ui.detail

import io.github.crisacm.salem.core.ui.views.toShortString

/** The cards of the detail screen, in the order they are shown. */
enum class ShowCard { SUBTITLE, OVERVIEW, TARGET, SCORE, BAR, FREQUENCY, STREAK, HISTORY, NOTES }

/**
 * The cards to show: a numerical habit has a target card and no overview, a yes or no habit the other way
 * round, and the notes only appear when there is a description.
 */
fun visibleCards(isNumerical: Boolean, hasNotes: Boolean): List<ShowCard> = ShowCard.entries.filter {
    when (it) {
        ShowCard.NOTES -> hasNotes
        ShowCard.OVERVIEW -> !isNumerical
        ShowCard.TARGET -> isNumerical
        else -> true
    }
}

/** The target of a numerical habit as the subtitle shows it: the short number and its unit. */
fun targetText(value: Double, unit: String): String = "${value.toShortString()} $unit"

/**
 * The id of the habit in the address that opens the detail screen (`content://.../habit/<id>`), or null if the
 * address has none, so a wrong or missing address does not crash the screen.
 */
fun habitIdFrom(lastPathSegment: String?): Long? = lastPathSegment?.toLongOrNull()

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

package io.github.crisacm.salem.activities.intro

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import io.github.crisacm.salem.R

/** [color] is an ARGB value, kept as a Long so it can be written as 0xFF...... */
class IntroSlide(
    @StringRes val title: Int,
    @StringRes val description: Int,
    @DrawableRes val icon: Int,
    val color: Long
)

// There is no slide number 3 in the resources; the numbering is kept as it was.
val introSlides = listOf(
    IntroSlide(R.string.intro_title_1, R.string.intro_description_1, R.drawable.intro_icon_1, 0xFF194673),
    IntroSlide(R.string.intro_title_2, R.string.intro_description_2, R.drawable.intro_icon_2, 0xFFFFA726),
    IntroSlide(R.string.intro_title_4, R.string.intro_description_4, R.drawable.intro_icon_4, 0xFF9575CD)
)

class IntroPager(private val pageCount: Int) {
    fun isLast(page: Int) = page >= pageCount - 1

    fun showSkip(page: Int) = !isLast(page)

    fun next(page: Int) = if (isLast(page)) page else page + 1

    fun previous(page: Int) = if (page <= 0) 0 else page - 1
}

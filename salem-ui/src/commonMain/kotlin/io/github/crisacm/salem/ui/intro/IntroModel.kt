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

package io.github.crisacm.salem.ui.intro

import io.github.crisacm.salem.ui.resources.Res
import io.github.crisacm.salem.ui.resources.intro_description_1
import io.github.crisacm.salem.ui.resources.intro_description_2
import io.github.crisacm.salem.ui.resources.intro_description_4
import io.github.crisacm.salem.ui.resources.intro_icon_1
import io.github.crisacm.salem.ui.resources.intro_icon_2
import io.github.crisacm.salem.ui.resources.intro_icon_4
import io.github.crisacm.salem.ui.resources.intro_title_1
import io.github.crisacm.salem.ui.resources.intro_title_2
import io.github.crisacm.salem.ui.resources.intro_title_4
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

/** [color] is an ARGB value, kept as a Long so it can be written as 0xFF...... */
class IntroSlide(
    val title: StringResource,
    val description: StringResource,
    val icon: DrawableResource,
    val color: Long
)

// There is no slide number 3 in the resources; the numbering is kept as it was.
val introSlides = listOf(
    IntroSlide(Res.string.intro_title_1, Res.string.intro_description_1, Res.drawable.intro_icon_1, 0xFF194673),
    IntroSlide(Res.string.intro_title_2, Res.string.intro_description_2, Res.drawable.intro_icon_2, 0xFFFFA726),
    IntroSlide(Res.string.intro_title_4, Res.string.intro_description_4, Res.drawable.intro_icon_4, 0xFF9575CD)
)

class IntroPager(private val pageCount: Int) {
    fun isLast(page: Int) = page >= pageCount - 1

    fun showSkip(page: Int) = !isLast(page)

    fun next(page: Int) = if (isLast(page)) page else page + 1

    fun previous(page: Int) = if (page <= 0) 0 else page - 1
}

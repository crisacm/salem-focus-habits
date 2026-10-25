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

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

/**
 * The action icons of the list bars, as vectors: the same paths as the Android drawables they replace
 * (`ic_action_*_dark`), so every platform draws the same icon. The 32 dp ones keep the glyph 4 units from the edge.
 */
object ListIcons {
    val Add: ImageVector by lazy {
        ImageVector.Builder("add", 32.dp, 32.dp, 32f, 32f).apply {
            addGroup(translationX = 4f, translationY = 4f)
            addPath(addPathNodes("M19,13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"), fill = SolidColor(Color.White))
        }.build()
    }

    val Filter: ImageVector by lazy {
        ImageVector.Builder("filter", 24.dp, 24.dp, 24f, 24f).apply {
            addPath(addPathNodes("M10,18h4v-2h-4v2zM3,6v2h18L21,6L3,6zM6,13h12v-2L6,11v2z"), fill = SolidColor(Color.White))
        }.build()
    }

    val Edit: ImageVector by lazy {
        ImageVector.Builder("edit", 32.dp, 32.dp, 32f, 32f).apply {
            addGroup(translationX = 4f, translationY = 4f)
            addPath(addPathNodes("M3,17.25V21h3.75L17.81,9.94l-3.75,-3.75L3,17.25zM20.71,7.04c0.39,-0.39 0.39,-1.02 0,-1.41l-2.34,-2.34c-0.39,-0.39 -1.02,-0.39 -1.41,0l-1.83,1.83 3.75,3.75 1.83,-1.83z"), fill = SolidColor(Color.White))
        }.build()
    }

    val Palette: ImageVector by lazy {
        ImageVector.Builder("palette", 32.dp, 32.dp, 32f, 32f).apply {
            addGroup(translationX = 4f, translationY = 4f)
            addPath(addPathNodes("M12,3c-4.97,0 -9,4.03 -9,9s4.03,9 9,9c0.83,0 1.5,-0.67 1.5,-1.5 0,-0.39 -0.15,-0.74 -0.39,-1.01 -0.23,-0.26 -0.38,-0.61 -0.38,-0.99 0,-0.83 0.67,-1.5 1.5,-1.5L16,16c2.76,0 5,-2.24 5,-5 0,-4.42 -4.03,-8 -9,-8zM6.5,12c-0.83,0 -1.5,-0.67 -1.5,-1.5S5.67,9 6.5,9 8,9.67 8,10.5 7.33,12 6.5,12zM9.5,8C8.67,8 8,7.33 8,6.5S8.67,5 9.5,5s1.5,0.67 1.5,1.5S10.33,8 9.5,8zM14.5,8c-0.83,0 -1.5,-0.67 -1.5,-1.5S13.67,5 14.5,5s1.5,0.67 1.5,1.5S15.33,8 14.5,8zM17.5,12c-0.83,0 -1.5,-0.67 -1.5,-1.5S16.67,9 17.5,9s1.5,0.67 1.5,1.5 -0.67,1.5 -1.5,1.5z"), fill = SolidColor(Color.White))
        }.build()
    }
}

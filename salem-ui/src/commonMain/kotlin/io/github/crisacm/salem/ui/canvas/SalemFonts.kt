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

package io.github.crisacm.salem.ui.canvas

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import io.github.crisacm.salem.ui.resources.Res
import io.github.crisacm.salem.ui.resources.fontawesome
import io.github.crisacm.salem.ui.resources.notosans_bold
import io.github.crisacm.salem.ui.resources.notosans_regular
import org.jetbrains.compose.resources.Font

/**
 * The fonts of the core canvas: the same Noto Sans and FontAwesome files on every platform, so the text of a chart
 * measures the same everywhere (the system font would differ between Android and iOS).
 */
class SalemFonts(val regular: FontFamily, val bold: FontFamily, val fontAwesome: FontFamily)

@Composable
fun rememberSalemFonts(): SalemFonts {
    val regular = FontFamily(Font(Res.font.notosans_regular))
    val bold = FontFamily(Font(Res.font.notosans_bold, FontWeight.Bold))
    val fontAwesome = FontFamily(Font(Res.font.fontawesome))
    return remember(regular, bold, fontAwesome) { SalemFonts(regular, bold, fontAwesome) }
}

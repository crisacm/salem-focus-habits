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

package io.github.crisacm.salem.ui.compose

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import io.github.crisacm.salem.core.ui.views.LightTheme
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalTestApi::class)
class SalemThemeTest {
    @Test
    fun usesTheExpressiveMotionSchemeAndTheSalemColors() = runComposeUiTest {
        val theme = LightTheme()
        setContent {
            SalemTheme(theme) {
                assertEquals(MotionScheme.expressive(), MaterialTheme.motionScheme)
                assertEquals(theme.toColorScheme().primary, MaterialTheme.colorScheme.primary)
            }
        }
    }
}

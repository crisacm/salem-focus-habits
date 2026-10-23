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
package io.github.crisacm.salem.acceptance

import androidx.test.filters.LargeTest
import io.github.crisacm.salem.BaseUserInterfaceTest
import io.github.crisacm.salem.acceptance.steps.CommonSteps.clickText
import io.github.crisacm.salem.acceptance.steps.CommonSteps.launchApp
import io.github.crisacm.salem.acceptance.steps.CommonSteps.pressHome
import io.github.crisacm.salem.acceptance.steps.CommonSteps.verifyDisplaysText
import io.github.crisacm.salem.acceptance.steps.WidgetSteps.clickCheckmarkWidget
import io.github.crisacm.salem.acceptance.steps.WidgetSteps.dragCheckmarkWidgetToHomeScreen
import io.github.crisacm.salem.acceptance.steps.WidgetSteps.verifyCheckmarkWidgetIsShown
import org.junit.Ignore
import org.junit.Test

@LargeTest
@Ignore("Flaky")
class WidgetTest : BaseUserInterfaceTest() {
    @Test
    @Throws(Exception::class)
    fun shouldCreateAndToggleCheckmarkWidget() {
        dragCheckmarkWidgetToHomeScreen()
        Thread.sleep(3000)
        clickText("Wake up early")
        clickText("Save")
        verifyCheckmarkWidgetIsShown()
        clickCheckmarkWidget()
        launchApp()
        clickText("Wake up early")
        verifyDisplaysText("5%")
        pressHome()
        clickCheckmarkWidget()
        launchApp()
        clickText("Wake up early")
        verifyDisplaysText("0%")
    }
}

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

package org.isoron.platform.notifications

import org.isoron.platform.time.DateUtils
import org.isoron.platform.time.LocalDate
import org.isoron.platform.time.utcTime
import org.isoron.uhabits.core.BaseUnitTest
import org.isoron.uhabits.core.models.Habit
import org.isoron.uhabits.core.models.NumericalHabitType
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

private class FakeDeliveredCenter : DeliveredNotificationCenter {
    val shown = LinkedHashMap<String, ShownNotification>()
    val removed = mutableListOf<String>()

    override fun post(notification: ShownNotification) {
        shown[notification.identifier] = notification
    }

    override fun removeDelivered(identifier: String) {
        removed.add(identifier)
        shown.remove(identifier)
    }
}

class AppleSystemTrayTest : BaseUnitTest() {
    private val center = FakeDeliveredCenter()
    private val logs = mutableListOf<String>()
    private lateinit var tray: AppleSystemTray

    @BeforeTest
    override fun setUp() {
        super.setUp()
        tray = AppleSystemTray(center, "Time to check in") { logs.add(it) }
    }

    @AfterTest
    fun tearDown() {
        DateUtils.setFixedTimeZone(null)
    }

    private fun habit(id: Long, question: String = "Did you read?", numerical: Boolean = false): Habit {
        val habit = if (numerical) fixtures.createEmptyNumericalHabit(NumericalHabitType.AT_LEAST) else fixtures.createEmptyHabit()
        habit.id = id
        habit.name = "Read"
        habit.question = question
        return habit
    }

    // 2015-01-26
    private val date = LocalDate(2015, 1, 26)
    private val dayStart = utcTime(2015, 1, 26)

    @Test
    fun testShowsTheReminderWithActionsForYesNoHabits() {
        tray.showNotification(habit(7), 7, date, utcTime(2015, 1, 26, 8, 30))
        assertEquals(
            ShownNotification("reminder-7", "Read", "Did you read?", 7, dayStart, true),
            center.shown["reminder-7"]
        )
    }

    @Test
    fun testNumericalHabitsHaveNoYesNoActions() {
        tray.showNotification(habit(7, numerical = true), 7, date, 0)
        assertEquals(false, center.shown["reminder-7"]?.withActions)
    }

    @Test
    fun testUsesTheDefaultBodyWithoutQuestion() {
        tray.showNotification(habit(7, question = ""), 7, date, 0)
        assertEquals("Time to check in", center.shown["reminder-7"]?.body)
    }

    @Test
    fun testShowingTwiceKeepsOneNotification() {
        tray.showNotification(habit(7), 7, date, 0)
        tray.showNotification(habit(7), 7, date, 0)
        assertEquals(1, center.shown.size)
    }

    @Test
    fun testRemovesTheDeliveredNotificationOfTheHabit() {
        tray.showNotification(habit(7), 7, date, 0)
        tray.removeNotification(7)
        assertEquals(listOf("reminder-7"), center.removed)
        assertEquals(0, center.shown.size)
    }

    @Test
    fun testRemovingAnUnknownIdUsesTheHabitIdentifier() {
        tray.removeNotification(12)
        assertEquals(listOf("reminder-12"), center.removed)
    }

    @Test
    fun testLogsGoToTheLogger() {
        tray.log("hello")
        assertEquals(listOf("AppleSystemTray: hello"), logs)
    }
}

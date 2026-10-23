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

package io.github.crisacm.salem.core.utils

import io.github.crisacm.salem.core.AppScope
import io.github.crisacm.salem.core.io.Logging
import io.github.crisacm.salem.core.preferences.Preferences
import io.github.crisacm.salem.platform.Synchronized
import io.github.crisacm.salem.platform.time.DateUtils
import io.github.crisacm.salem.platform.time.LocalDate
import io.github.crisacm.salem.platform.time.computeToday
import io.github.crisacm.salem.platform.time.getToday
import io.github.crisacm.salem.platform.time.setToday
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Inject

/**
 * A class that emits events when a new day starts. It only runs between [onResume] and [onPause],
 * which the app calls when its screen comes to the foreground and leaves it.
 */
@AppScope
@Inject
open class MidnightTimer(
    logging: Logging,
    private val preferences: Preferences,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val todayProvider: () -> LocalDate = { computeToday(preferences.midnightDelayHours, 0) }
) {
    private val listeners = mutableListOf<MidnightListener>()
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)
    private var job: Job? = null
    private val logger = logging.getLogger("MidnightTimer")

    @Synchronized
    open fun addListener(listener: MidnightListener) {
        this.listeners.add(listener)
    }

    @Synchronized
    open fun onPause() {
        logger.info("Pausing timer")
        job?.cancel()
        job = null
    }

    @Synchronized
    open fun onResume(delayOffsetInMillis: Long = DateUtils.SECOND_LENGTH) {
        job?.cancel()
        job = scope.launch {
            while (isActive) {
                // Recomputed every lap, so days of 23 or 25 hours do not shift the notification
                val nextDelay = millisecondsUntilMidnight() + delayOffsetInMillis
                logger.info("Scheduling refresh for $nextDelay ms from now")
                delay(nextDelay)
                notifyListeners()
            }
        }
    }

    /**
     * Runs the midnight refresh now if the day changed since it was last set. For platforms where
     * the app can be suspended across midnight and the timer cannot fire. Returns whether it ran.
     */
    open fun notifyIfDayChanged(): Boolean {
        if (todayProvider() == getToday()) return false
        notifyListeners()
        return true
    }

    @Synchronized
    open fun removeListener(listener: MidnightListener) = this.listeners.remove(listener)

    private fun notifyListeners() {
        logger.info("Midnight refresh")
        setToday(todayProvider())
        val snapshot = synchronizedCopy()
        for (l in snapshot) {
            try {
                l.atMidnight()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                logger.error(e)
            }
        }
    }

    private fun millisecondsUntilMidnight(): Long =
        DateUtils.millisecondsUntilTomorrowWithOffset(preferences.midnightDelayHours, 0)

    @Synchronized
    private fun synchronizedCopy(): List<MidnightListener> = listeners.toList()

    fun interface MidnightListener {
        fun atMidnight()
    }
}

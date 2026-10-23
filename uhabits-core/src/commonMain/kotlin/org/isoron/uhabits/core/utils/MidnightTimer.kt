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

package org.isoron.uhabits.core.utils

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Inject
import org.isoron.platform.Synchronized
import org.isoron.platform.time.DateUtils
import org.isoron.platform.time.computeToday
import org.isoron.platform.time.setToday
import org.isoron.uhabits.core.AppScope
import org.isoron.uhabits.core.io.Logging
import org.isoron.uhabits.core.preferences.Preferences

/**
 * A class that emits events when a new day starts. It only runs between [onResume] and [onPause],
 * which the app calls when its screen comes to the foreground and leaves it.
 */
@AppScope
@Inject
open class MidnightTimer(
    logging: Logging,
    private val preferences: Preferences,
    dispatcher: CoroutineDispatcher = Dispatchers.Default
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
        val initialDelay = DateUtils.millisecondsUntilTomorrowWithOffset(preferences.midnightDelayHours, 0) + delayOffsetInMillis
        logger.info("Scheduling refresh for $initialDelay ms from now")
        job = scope.launch {
            delay(initialDelay)
            while (isActive) {
                notifyListeners()
                delay(DateUtils.DAY_LENGTH)
            }
        }
    }

    @Synchronized
    open fun removeListener(listener: MidnightListener) = this.listeners.remove(listener)

    private fun notifyListeners() {
        logger.info("Midnight refresh")
        setToday(computeToday(preferences.midnightDelayHours, 0))
        val snapshot = synchronizedCopy()
        for (l in snapshot) {
            l.atMidnight()
        }
    }

    @Synchronized
    private fun synchronizedCopy(): List<MidnightListener> = listeners.toList()

    fun interface MidnightListener {
        fun atMidnight()
    }
}

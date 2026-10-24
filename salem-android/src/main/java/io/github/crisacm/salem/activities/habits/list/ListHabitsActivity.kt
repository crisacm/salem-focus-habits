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

package io.github.crisacm.salem.activities.habits.list

import android.Manifest.permission.POST_NOTIFICATIONS
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import io.github.crisacm.salem.BaseExceptionHandler
import io.github.crisacm.salem.HabitsApplication
import io.github.crisacm.salem.core.preferences.Preferences
import io.github.crisacm.salem.core.tasks.TaskRunner
import io.github.crisacm.salem.core.ui.ThemeSwitcher.Companion.THEME_DARK
import io.github.crisacm.salem.core.utils.MidnightTimer
import io.github.crisacm.salem.database.AutoBackup
import io.github.crisacm.salem.inject.HabitsActivityComponent
import io.github.crisacm.salem.inject.HabitsApplicationComponent
import io.github.crisacm.salem.inject.create
import io.github.crisacm.salem.platform.time.LocalDate
import io.github.crisacm.salem.ui.compose.SalemTheme
import io.github.crisacm.salem.utils.dismissCurrentDialog
import io.github.crisacm.salem.utils.restartWithFade
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class ListHabitsActivity : AppCompatActivity(), Preferences.Listener {

    var pureBlack: Boolean = false
    lateinit var appComponent: HabitsApplicationComponent
    lateinit var component: HabitsActivityComponent
    lateinit var taskRunner: TaskRunner
    lateinit var adapter: HabitListState
    lateinit var host: ListHabitsHost
    lateinit var screen: ListHabitsScreen
    lateinit var prefs: Preferences
    lateinit var midnightTimer: MidnightTimer
    private val scope = CoroutineScope(Dispatchers.Main)

    private var permissionAlreadyRequested = false
    private val permissionLauncher =
        registerForActivityResult(RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                scheduleReminders()
            } else {
                Log.i("ListHabitsActivity", "POST_NOTIFICATIONS denied")
            }
        }

    private lateinit var menu: ListHabitsMenu

    override fun onQuestionMarksChanged() {
        menu.behavior.onPreferencesChanged()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        appComponent = (applicationContext as HabitsApplication).component
        component = HabitsActivityComponent::class.create(
            parent = appComponent,
            activityContext = this
        )
        component.themeSwitcher.apply()

        prefs = appComponent.preferences
        prefs.addListener(this)
        pureBlack = prefs.isPureBlackEnabled
        midnightTimer = appComponent.midnightTimer
        host = component.listHabitsHost
        savedInstanceState?.let { host.restoreDataOffset(it.getInt(KEY_DATA_OFFSET)) }
        screen = component.listHabitsScreen
        adapter = component.habitListState
        taskRunner = appComponent.taskRunner
        menu = component.listHabitsMenu
        Thread.setDefaultUncaughtExceptionHandler(BaseExceptionHandler(this))
        component.listHabitsBehavior.onStartup()
        host.paintStatusBar(this)
        setContent { SalemTheme { ListHabitsContent(host) } }
        addContentView(confettiOverlay(), ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT))
    }

    // A plain View over the composition, padded like the screen, so the confetti falls over everything and
    // never takes a touch.
    private fun confettiOverlay() = FrameLayout(this).apply {
        addView(host.konfettiView, MATCH_PARENT, MATCH_PARENT)
        ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val cutout = insets.getInsets(WindowInsetsCompat.Type.displayCutout())
            view.setPadding(maxOf(bars.left, cutout.left), 0, maxOf(bars.right, cutout.right), 0)
            insets
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(KEY_DATA_OFFSET, host.dataOffset)
    }

    override fun onPause() {
        midnightTimer.onPause()
        screen.onDetached()
        adapter.cancelRefresh()
        dismissCurrentDialog()
        super.onPause()
    }

    override fun onResume() {
        adapter.refresh()
        screen.onAttached()
        midnightTimer.onResume()

        if (appComponent.reminderScheduler.hasHabitsWithReminders()) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                scheduleReminders()
            } else {
                if (checkSelfPermission(this, POST_NOTIFICATIONS) == PERMISSION_GRANTED) {
                    scheduleReminders()
                } else {
                    // If we have not requested the permission yet, request it. Otherwide do
                    // nothing. This check is necessary to avoid an infinite onResume loop in case
                    // the user denies the permission.
                    if (!permissionAlreadyRequested) {
                        Log.i("ListHabitsActivity", "Requestion permission: POST_NOTIFICATIONS")
                        permissionLauncher.launch(POST_NOTIFICATIONS)
                        permissionAlreadyRequested = true
                    }
                }
            }
        }

        taskRunner.run {
            try {
                AutoBackup(this@ListHabitsActivity).run()
                appComponent.widgetUpdater.updateWidgets()
            } catch (e: Exception) {
                Log.e("ListHabitActivity", "TaskRunner failed", e)
            }
        }
        if (prefs.theme == THEME_DARK && prefs.isPureBlackEnabled != pureBlack) {
            restartWithFade(ListHabitsActivity::class.java)
        }
        parseIntents()
        super.onResume()
    }

    private fun scheduleReminders() {
        appComponent.reminderScheduler.scheduleAll()
    }

    private fun parseIntents() {
        if (intent == null) return
        if (intent.action == ACTION_EDIT) {
            val habitId = intent.extras?.getLong("habit")
            val timestampMillis = intent.extras?.getLong("timestamp")
            if (habitId != null && timestampMillis != null) {
                // The habit may have been deleted since the notification or widget was created.
                appComponent.habitList.getById(habitId)?.let { habit ->
                    val date = LocalDate.fromUnixTime(timestampMillis)
                    component.listHabitsBehavior.onEdit(habit, date, 0f, 0f)
                }
            }
        }
        intent = null
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    companion object {
        const val ACTION_EDIT = "org.isoron.uhabits.ACTION_EDIT"
        private const val KEY_DATA_OFFSET = "dataOffset"
    }
}

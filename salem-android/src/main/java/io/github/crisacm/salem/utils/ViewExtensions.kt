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

package io.github.crisacm.salem.utils

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Handler
import android.view.View
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.snackbar.Snackbar
import io.github.crisacm.salem.HabitsApplication
import io.github.crisacm.salem.R
import io.github.crisacm.salem.activities.AndroidThemeSwitcher
import io.github.crisacm.salem.core.models.PaletteColor
import io.github.crisacm.salem.core.ui.views.Theme
import io.github.crisacm.salem.platform.gui.toInt
import java.io.File

fun View.showMessage(msg: String) {
    try {
        val snackbar = Snackbar.make(this, msg, Snackbar.LENGTH_SHORT)
        val tvId = R.id.snackbar_text
        val tv = snackbar.view.findViewById<TextView>(tvId)
        tv?.setTextColor(Color.WHITE)
        snackbar.show()
    } catch (e: IllegalArgumentException) {
        return
    }
}

fun Activity.showMessage(msg: String) {
    this.findViewById<View>(android.R.id.content).showMessage(msg)
}

fun Activity.showSendFileScreen(archiveFilename: String) {
    val uri = Uri.parse(archiveFilename)
    val fileUri = if (uri.scheme == "content") {
        uri
    } else {
        val file = if (uri.scheme == "file") File(uri.path!!) else File(archiveFilename)
        FileProvider.getUriForFile(this, packageName, file)
    }
    this.startActivitySafely(
        Intent().apply {
            action = Intent.ACTION_SEND
            type = "application/zip"
            putExtra(Intent.EXTRA_STREAM, fileUri)
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
    )
}

fun Activity.startActivitySafely(intent: Intent) {
    try {
        startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        this.showMessage(resources.getString(R.string.activity_not_found))
    }
}

fun Activity.showSendEmailScreen(
    @StringRes toId: Int,
    @StringRes subjectId: Int,
    content: String?
) {
    val to = this.getString(toId)
    val subject = this.getString(subjectId)
    this.startActivity(
        Intent().apply {
            action = Intent.ACTION_SEND
            type = "message/rfc822"
            putExtra(Intent.EXTRA_EMAIL, arrayOf(to))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, content)
        }
    )
}

fun Activity.restartWithFade(cls: Class<*>?) {
    Handler().postDelayed(
        {
            finish()
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            startActivity(Intent(this, cls))
        },
        500
    ) // HACK: Let the menu disappear first
}

fun View.setupToolbar(
    toolbar: Toolbar,
    title: String,
    color: PaletteColor,
    theme: Theme,
    displayHomeAsUpEnabled: Boolean = true
) {
    toolbar.elevation = InterfaceUtils.dpToPixels(context, 2f)
    val res = StyledResources(context)
    toolbar.title = title
    val toolbarColor = if (!res.getBoolean(R.attr.useHabitColorAsPrimary)) {
        StyledResources(context).getColor(R.attr.colorPrimary)
    } else {
        theme.color(color).toInt()
    }
    toolbar.background = ColorDrawable(toolbarColor)
    toolbar.applyToolbarInsets()
    val activity = context as AppCompatActivity
    activity.window.statusBarColor = toolbarColor
    activity.setSupportActionBar(toolbar)
    activity.supportActionBar?.setDisplayHomeAsUpEnabled(displayHomeAsUpEnabled)
}

fun View.currentTheme(): Theme {
    val component = (context.applicationContext as HabitsApplication).component
    val themeSwitcher = AndroidThemeSwitcher(context, component.preferences)
    themeSwitcher.apply()
    return themeSwitcher.currentTheme
}

fun View.getFontAwesome() = InterfaceUtils.getFontAwesome(context)!!
fun View.sp(value: Float) = InterfaceUtils.spToPixels(context, value)
fun View.dp(value: Float) = InterfaceUtils.dpToPixels(context, value)

fun View.applyRootViewInsets() {
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
        val systemBarsInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
        val displayCutoutInsets = insets.getInsets(WindowInsetsCompat.Type.displayCutout())
        val left = maxOf(systemBarsInsets.left, displayCutoutInsets.left)
        val right = maxOf(systemBarsInsets.right, displayCutoutInsets.right)
        view.setPadding(left, 0, right, 0)
        view.background = ColorDrawable(Color.BLACK)
        insets
    }
}

fun View.applyToolbarInsets() {
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
        val systemBarsInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
        val displayCutoutInsets = insets.getInsets(WindowInsetsCompat.Type.displayCutout())
        val top = maxOf(systemBarsInsets.top, displayCutoutInsets.top)
        view.setPadding(0, top, 0, 0)
        insets
    }
}

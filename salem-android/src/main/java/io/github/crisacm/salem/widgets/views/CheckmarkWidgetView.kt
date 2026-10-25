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
package io.github.crisacm.salem.widgets.views

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import io.github.crisacm.salem.HabitsApplication
import io.github.crisacm.salem.R
import io.github.crisacm.salem.core.models.Entry.Companion.NO
import io.github.crisacm.salem.core.models.Entry.Companion.SKIP
import io.github.crisacm.salem.core.models.Entry.Companion.UNKNOWN
import io.github.crisacm.salem.core.models.Entry.Companion.YES_AUTO
import io.github.crisacm.salem.core.models.Entry.Companion.YES_MANUAL
import io.github.crisacm.salem.core.preferences.Preferences
import io.github.crisacm.salem.core.ui.views.RingChart
import io.github.crisacm.salem.core.ui.views.toShortString
import io.github.crisacm.salem.inject.HabitsApplicationComponent
import io.github.crisacm.salem.platform.gui.Color
import io.github.crisacm.salem.platform.gui.CoreWidgetView
import io.github.crisacm.salem.utils.InterfaceUtils.getDimension
import io.github.crisacm.salem.utils.PaletteUtils.getAndroidTestColor
import io.github.crisacm.salem.utils.StyledResources
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

class CheckmarkWidgetView : HabitWidgetView {
    var activeColor: Int = 0

    var percentage = 0f
    var name: String? = null
    private lateinit var ring: CoreWidgetView
    private var ringTextSize = 0f
    private var ringThickness = 0f
    private var ringColor = 0
    private lateinit var label: TextView
    var entryValue = 0
    var entryState = 0
    var isNumerical = false
    private var preferences: Preferences? = null

    constructor(context: Context?) : super(context) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    fun refresh() {
        if (backgroundPaint == null || frame == null) return
        val res = StyledResources(context)
        val bgColor: Int
        val fgColor: Int
        setShadowAlpha(0x4f)
        when (entryState) {
            YES_MANUAL, SKIP, YES_AUTO -> {
                bgColor = activeColor
                fgColor = res.getColor(R.attr.contrast0)
                backgroundPaint!!.color = bgColor
                frame!!.setBackgroundDrawable(background)
            }
            NO, UNKNOWN -> {
                bgColor = res.getColor(R.attr.cardBgColor)
                fgColor = res.getColor(R.attr.contrast60)
            }
            else -> {
                bgColor = res.getColor(R.attr.cardBgColor)
                fgColor = res.getColor(R.attr.contrast60)
            }
        }
        ringColor = fgColor
        updateRing()
        label.text = name
        label.setTextColor(fgColor)
        requestLayout()
        postInvalidate()
    }

    private fun updateRing() {
        val density = resources.displayMetrics.density
        ring.view = RingChart(
            color = Color(ringColor),
            percentage = percentage.toDouble(),
            thickness = (ringThickness / density).toDouble(),
            inactiveColor = Color(StyledResources(context).getColor(R.attr.contrast100)).withAlpha(0.15),
            text = text,
            isFontAwesome = true,
            textSize = (ringTextSize / density).toDouble(),
            isTextStroked = strokedTextEnabled
        )
        ring.invalidate()
    }

    private val strokedTextEnabled: Boolean
        get() = if (isNumerical) {
            false
        } else {
            when (entryState) {
                YES_AUTO -> true
                else -> false
            }
        }

    private val text: String
        get() = if (isNumerical) {
            (max(0, entryValue) / 1000.0).toShortString()
        } else {
            when (entryState) {
                YES_MANUAL, YES_AUTO -> resources.getString(R.string.fa_check)
                SKIP -> resources.getString(R.string.fa_skipped)
                UNKNOWN -> {
                    run {
                        if (preferences!!.areQuestionMarksEnabled) {
                            return resources.getString(R.string.fa_question)
                        } else {
                            resources.getString(R.string.fa_times)
                        }
                    }
                    resources.getString(R.string.fa_times)
                }
                NO -> resources.getString(R.string.fa_times)
                else -> resources.getString(R.string.fa_times)
            }
        }

    override val innerLayoutId: Int
        get() = R.layout.widget_checkmark

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var width = MeasureSpec.getSize(widthMeasureSpec)
        var height = MeasureSpec.getSize(heightMeasureSpec)
        if (height >= width) {
            height = min(height, (width * 1.5).roundToInt())
        } else {
            width = min(width, height)
        }
        val textSize = min(0.175f * width, getDimension(context, R.dimen.smallTextSize))
        label.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)
        ringTextSize = if (isNumerical) textSize * 0.9f else textSize
        ringThickness = 0.03f * width
        updateRing()
        super.onMeasure(
            MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY)
        )
    }

    private fun init() {
        val appComponent: HabitsApplicationComponent =
            (context.applicationContext as HabitsApplication).component
        preferences = appComponent.preferences
        ring = CoreWidgetView(context)
        findViewById<FrameLayout>(R.id.scoreRing).addView(
            ring,
            ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        )
        label = findViewById<View>(R.id.label) as TextView
        if (isInEditMode) {
            percentage = 0.75f
            name = "Wake up early"
            activeColor = getAndroidTestColor(6)
            entryValue = YES_MANUAL
            refresh()
        }
    }
}

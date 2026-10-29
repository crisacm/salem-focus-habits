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

package io.github.crisacm.salem.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Contrast
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.QuestionMark
import androidx.compose.material.icons.outlined.SkipNext
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material.icons.outlined.SwapVert
import androidx.compose.material.icons.outlined.TouchApp
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import io.github.crisacm.salem.ui.compose.ChoiceOption
import io.github.crisacm.salem.ui.resources.Res
import io.github.crisacm.salem.ui.resources.about
import io.github.crisacm.salem.ui.resources.development
import io.github.crisacm.salem.ui.resources.first_day_of_the_week
import io.github.crisacm.salem.ui.resources.help
import io.github.crisacm.salem.ui.resources.interface_preferences
import io.github.crisacm.salem.ui.resources.links
import io.github.crisacm.salem.ui.resources.pref_rate_this_app
import org.jetbrains.compose.resources.stringResource

private val INTERFACE_SWITCHES = listOf(
    SettingsSwitch.SHORT_TOGGLE,
    SettingsSwitch.MIDNIGHT_DELAY,
    SettingsSwitch.SKIP,
    SettingsSwitch.UNKNOWN,
    SettingsSwitch.REVERSE_DAYS,
    SettingsSwitch.PURE_BLACK,
    SettingsSwitch.DISABLE_ANIMATION
)

/**
 * The Settings screen: the interface rows, the help and rate links, About and the developer switch, in cards by section.
 * A platform adds its own rows in [interfaceExtras] (after the switches, before the first day of the week) and its own
 * sections, made of [SettingsGroup]s, in [middleSections] (between the interface and the links). [weekdayNames] start
 * at Saturday. The switch that turns the animations off is only shown when [showAnimationsSwitch] is on. The developer
 * section is shown while the developer mode is on. Back calls [onBack].
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SettingsContent(
    state: SettingsState,
    weekdayNames: List<String>,
    showAnimationsSwitch: Boolean,
    onBack: () -> Unit,
    onHelp: () -> Unit,
    onRate: () -> Unit,
    onAbout: () -> Unit,
    interfaceExtras: @Composable ColumnScope.() -> Unit = {},
    middleSections: @Composable ColumnScope.() -> Unit = {}
) {
    BackHandler(onBack = onBack)
    Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
        ) {
            SettingsHeader(onBack)
            SettingsGroup(stringResource(Res.string.interface_preferences), SettingsTone.INTERFACE) {
                INTERFACE_SWITCHES
                    .filter { showAnimationsSwitch || it != SettingsSwitch.DISABLE_ANIMATION }
                    .forEach { SettingsSwitchRow(state, it) }
                interfaceExtras()
                FirstWeekdayRow(state, weekdayNames)
            }
            middleSections()
            SettingsGroup(stringResource(Res.string.links), SettingsTone.LINKS) {
                SettingsClickRow(Icons.Outlined.QuestionMark, stringResource(Res.string.help), onClick = onHelp)
                SettingsClickRow(Icons.Outlined.StarBorder, stringResource(Res.string.pref_rate_this_app), onClick = onRate)
                SettingsClickRow(Icons.Outlined.Info, stringResource(Res.string.about), onClick = onAbout)
            }
            if (state.isOn(SettingsSwitch.DEVELOPER)) {
                SettingsGroup(stringResource(Res.string.development), SettingsTone.TROUBLESHOOTING) {
                    SettingsSwitchRow(state, SettingsSwitch.DEVELOPER)
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

/** The row of one on/off setting. */
@Composable
fun SettingsSwitchRow(state: SettingsState, switch: SettingsSwitch) {
    SettingsToggleRow(
        icon = iconOf(switch),
        title = stringResource(switch.title),
        summary = switch.summary?.let { stringResource(it) },
        checked = state.isOn(switch),
        onCheckedChange = { state.set(switch, it) }
    )
}

private fun iconOf(switch: SettingsSwitch): ImageVector = when (switch) {
    SettingsSwitch.SHORT_TOGGLE -> Icons.Outlined.TouchApp
    SettingsSwitch.MIDNIGHT_DELAY -> Icons.Outlined.Bedtime
    SettingsSwitch.SKIP -> Icons.Outlined.SkipNext
    SettingsSwitch.UNKNOWN -> Icons.Outlined.QuestionMark
    SettingsSwitch.REVERSE_DAYS -> Icons.Outlined.SwapVert
    SettingsSwitch.PURE_BLACK -> Icons.Outlined.Contrast
    SettingsSwitch.DISABLE_ANIMATION -> Icons.Outlined.AutoAwesome
    SettingsSwitch.STICKY_NOTIFICATIONS -> Icons.Outlined.Notifications
    SettingsSwitch.DEVELOPER -> Icons.Outlined.Code
}

@Composable
private fun FirstWeekdayRow(state: SettingsState, weekdayNames: List<String>) {
    val current = state.firstWeekday.daysSinceSunday + 1
    SettingsChoiceRow(
        icon = Icons.Outlined.CalendarToday,
        title = stringResource(Res.string.first_day_of_the_week),
        summary = firstWeekdaySummary(weekdayNames, current),
        options = firstWeekdayOptions(weekdayNames).map { ChoiceOption(it.value, it.name) },
        selected = current.toString(),
        onSelect = { state.selectFirstWeekday(weekdayFromNumber(it)) }
    )
}

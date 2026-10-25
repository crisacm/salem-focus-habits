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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import io.github.crisacm.salem.ui.compose.CategoryHeader
import io.github.crisacm.salem.ui.compose.ChoiceOption
import io.github.crisacm.salem.ui.compose.ChoiceRow
import io.github.crisacm.salem.ui.compose.ClickRow
import io.github.crisacm.salem.ui.compose.SalemTopAppBar
import io.github.crisacm.salem.ui.compose.SwitchRow
import io.github.crisacm.salem.ui.resources.Res
import io.github.crisacm.salem.ui.resources.about
import io.github.crisacm.salem.ui.resources.development
import io.github.crisacm.salem.ui.resources.first_day_of_the_week
import io.github.crisacm.salem.ui.resources.help
import io.github.crisacm.salem.ui.resources.interface_preferences
import io.github.crisacm.salem.ui.resources.links
import io.github.crisacm.salem.ui.resources.pref_rate_this_app
import io.github.crisacm.salem.ui.resources.settings
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
 * The Settings screen: the interface rows, the help and rate links, About and the developer switch. A platform adds its
 * own rows in [interfaceExtras] (after the switches, before the first day of the week) and [middleSections] (between
 * the interface and the links). [weekdayNames] start at Saturday. The switch that turns the animations off is only
 * shown when [showAnimationsSwitch] is on. The developer section is shown while the developer mode is on. Back calls
 * [onBack].
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
    Scaffold(
        topBar = { SalemTopAppBar(stringResource(Res.string.settings), onBack) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            CategoryHeader(stringResource(Res.string.interface_preferences))
            INTERFACE_SWITCHES
                .filter { showAnimationsSwitch || it != SettingsSwitch.DISABLE_ANIMATION }
                .forEach { SettingsSwitchRow(state, it) }
            interfaceExtras()
            FirstWeekdayRow(state, weekdayNames)
            middleSections()
            CategoryHeader(stringResource(Res.string.links))
            ClickRow(stringResource(Res.string.help), onClick = onHelp)
            ClickRow(stringResource(Res.string.pref_rate_this_app), onClick = onRate)
            ClickRow(stringResource(Res.string.about), onClick = onAbout)
            if (state.isOn(SettingsSwitch.DEVELOPER)) {
                CategoryHeader(stringResource(Res.string.development))
                SettingsSwitchRow(state, SettingsSwitch.DEVELOPER)
            }
        }
    }
}

/** The row of one on/off setting. */
@Composable
fun SettingsSwitchRow(state: SettingsState, switch: SettingsSwitch) {
    SwitchRow(
        title = stringResource(switch.title),
        summary = switch.summary?.let { stringResource(it) },
        checked = state.isOn(switch),
        onCheckedChange = { state.set(switch, it) }
    )
}

@Composable
private fun FirstWeekdayRow(state: SettingsState, weekdayNames: List<String>) {
    val current = state.firstWeekday.daysSinceSunday + 1
    ChoiceRow(
        title = stringResource(Res.string.first_day_of_the_week),
        summary = firstWeekdaySummary(weekdayNames, current),
        options = firstWeekdayOptions(weekdayNames).map { ChoiceOption(it.value, it.name) },
        selected = current.toString(),
        onSelect = { state.selectFirstWeekday(weekdayFromNumber(it)) }
    )
}

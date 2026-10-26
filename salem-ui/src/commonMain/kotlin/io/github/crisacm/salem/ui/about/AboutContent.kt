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

package io.github.crisacm.salem.ui.about

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.crisacm.salem.ui.compose.SalemTopAppBar
import io.github.crisacm.salem.ui.resources.Res
import io.github.crisacm.salem.ui.resources.about
import io.github.crisacm.salem.ui.resources.about_based_on
import io.github.crisacm.salem.ui.resources.app_name
import io.github.crisacm.salem.ui.resources.developers
import io.github.crisacm.salem.ui.resources.help_translate
import io.github.crisacm.salem.ui.resources.intro_icon_1
import io.github.crisacm.salem.ui.resources.links
import io.github.crisacm.salem.ui.resources.pref_rate_this_app
import io.github.crisacm.salem.ui.resources.pref_send_feedback
import io.github.crisacm.salem.ui.resources.pref_view_privacy
import io.github.crisacm.salem.ui.resources.pref_view_source_code
import io.github.crisacm.salem.ui.resources.translators
import io.github.crisacm.salem.ui.resources.version_n
import io.github.crisacm.salem.ui.resources.view_all_contributors
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * The About screen: the app, its links, the developers and the translators. [onVersionTap] is called on each tap on
 * the version (`AboutRoute` counts them), [openUrl] opens a page of [SalemLinks] and [snackbar] shows the messages.
 */
@Composable
fun AboutContent(
    versionName: String,
    onBack: () -> Unit,
    onVersionTap: () -> Unit,
    openUrl: (String) -> Unit,
    snackbar: SnackbarHostState = remember { SnackbarHostState() }
) {
    Scaffold(
        topBar = { SalemTopAppBar(stringResource(Res.string.about), onBack) },
        snackbarHost = { SnackbarHost(snackbar) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AboutCard(horizontalAlignment = Alignment.CenterHorizontally) {
                Image(
                    painter = painterResource(Res.drawable.intro_icon_1),
                    contentDescription = null,
                    modifier = Modifier.size(100.dp)
                )
                Text(
                    stringResource(Res.string.app_name),
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(6.dp)
                )
                Text(stringResource(Res.string.version_n, versionName), modifier = Modifier.clickable(onClick = onVersionTap))
                Text(stringResource(Res.string.about_based_on), modifier = Modifier.padding(6.dp))
            }
            AboutCard {
                CardHeader(Res.string.links)
                ClickableItem(Res.string.pref_rate_this_app) { openUrl(SalemLinks.RATE) }
                ClickableItem(Res.string.pref_send_feedback) { openUrl(SalemLinks.FEEDBACK) }
                ClickableItem(Res.string.help_translate) { openUrl(SalemLinks.TRANSLATE) }
                ClickableItem(Res.string.pref_view_source_code) { openUrl(SalemLinks.SOURCE) }
                ClickableItem(Res.string.pref_view_privacy) { openUrl(SalemLinks.PRIVACY) }
            }
            AboutCard {
                CardHeader(Res.string.developers)
                aboutDevelopers.forEach { Text(it, modifier = Modifier.padding(vertical = 6.dp)) }
                ClickableItem(Res.string.view_all_contributors) { openUrl(SalemLinks.CONTRIBUTORS) }
            }
            AboutCard(horizontalAlignment = Alignment.CenterHorizontally) {
                CardHeader(Res.string.translators)
                aboutTranslators.forEach { language ->
                    Text(
                        language.language,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 10.dp, bottom = 2.dp)
                    )
                    language.names.forEach { Text(it, modifier = Modifier.padding(vertical = 2.dp)) }
                }
            }
        }
    }
}

@Composable
private fun AboutCard(
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalAlignment = horizontalAlignment
        ) { content() }
    }
}

@Composable
private fun CardHeader(text: StringResource) {
    Text(
        stringResource(text),
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 6.dp)
    )
}

@Composable
private fun ClickableItem(text: StringResource, onClick: () -> Unit) {
    Text(
        stringResource(text),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp)
    )
}

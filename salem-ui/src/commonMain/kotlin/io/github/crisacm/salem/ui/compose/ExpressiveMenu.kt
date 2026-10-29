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

package io.github.crisacm.salem.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * A menu in the style of Material 3 Expressive: a rounded container in a tonal surface, 8 dp from the button that opens
 * it, whose rows are [ExpressiveMenuItem]s and whose groups are told apart with an [ExpressiveMenuGap].
 */
@Composable
fun ExpressiveMenu(expanded: Boolean, onDismiss: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        modifier = Modifier.widthIn(min = 220.dp),
        offset = DpOffset(0.dp, 8.dp),
        shape = RoundedCornerShape(28.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 0.dp,
        shadowElevation = 6.dp
    ) {
        Column(Modifier.padding(horizontal = 8.dp), content = content)
    }
}

/**
 * A row of an [ExpressiveMenu]: an icon (none for a row inside a group, which is indented instead), the [label] and,
 * when it is [selected] (a checked option), a tonal fill with a [check] at the end. A [destructive] row is drawn in
 * the error colour. [trailing] draws something else at the end.
 */
@Composable
fun ExpressiveMenuItem(
    label: String,
    icon: ImageVector?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    destructive: Boolean = false,
    check: Boolean = selected,
    trailing: (@Composable () -> Unit)? = null
) {
    val scheme = MaterialTheme.colorScheme
    val container = if (selected) scheme.secondaryContainer else Color.Transparent
    val content = when {
        destructive -> scheme.error
        selected -> scheme.onSecondaryContainer
        else -> scheme.onSurface
    }
    val iconTint = when {
        destructive -> scheme.error
        selected -> scheme.onSecondaryContainer
        else -> scheme.onSurfaceVariant
    }
    Row(
        modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(container)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(24.dp))
        } else {
            Spacer(Modifier.size(24.dp))
        }
        Text(label, color = content, fontSize = 16.sp, modifier = Modifier.weight(1f).padding(start = 16.dp, end = 8.dp))
        if (check) Icon(Icons.Filled.Check, contentDescription = null, tint = content, modifier = Modifier.size(20.dp))
        trailing?.invoke()
    }
}

/** The space and the thin line between two groups of rows of an [ExpressiveMenu]. */
@Composable
fun ExpressiveMenuGap() {
    Spacer(Modifier.height(4.dp))
    HorizontalDivider(Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)
    Spacer(Modifier.height(4.dp))
}

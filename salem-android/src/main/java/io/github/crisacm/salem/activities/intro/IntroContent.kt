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

package io.github.crisacm.salem.activities.intro

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.crisacm.salem.R
import kotlinx.coroutines.launch

@Composable
fun IntroContent(onFinish: () -> Unit) {
    val pager = IntroPager(introSlides.size)
    val pagerState = rememberPagerState { introSlides.size }
    val scope = rememberCoroutineScope()
    val page = pagerState.currentPage
    val background by animateColorAsState(Color(introSlides[page].color), label = "introBackground")

    BackHandler(enabled = page > 0) {
        scope.launch { pagerState.animateScrollToPage(pager.previous(page)) }
    }

    Box(modifier = Modifier.fillMaxSize().background(background)) {
        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { index ->
            IntroPage(introSlides[index])
        }
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .safeDrawingPadding()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (pager.showSkip(page)) {
                IntroButton(stringResource(R.string.skip_day), onFinish)
            } else {
                Box(modifier = Modifier.size(48.dp))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(introSlides.size) { index ->
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = if (index == page) 1f else 0.4f))
                    )
                }
            }
            if (pager.isLast(page)) {
                IntroButton(stringResource(R.string.done_label), onFinish)
            } else {
                IntroButton(stringResource(R.string.intro_next)) {
                    scope.launch { pagerState.animateScrollToPage(pager.next(page)) }
                }
            }
        }
    }
}

@Composable
private fun IntroPage(slide: IntroSlide) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 32.dp, vertical = 72.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(painterResource(slide.icon), contentDescription = null, modifier = Modifier.size(200.dp))
        Text(
            stringResource(slide.title),
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 32.dp)
        )
        Text(
            stringResource(slide.description),
            color = Color.White,
            fontSize = 18.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}

@Composable
private fun IntroButton(text: String, onClick: () -> Unit) {
    TextButton(onClick = onClick) { Text(text, color = Color.White) }
}

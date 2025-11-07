package net.lateinit.noiseguard.presentation.ui.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.math.abs

@Composable
fun DurationWheelPicker(
    minutes: Int,
    seconds: Int,
    onMinutesChange: (Int) -> Unit,
    onSecondsChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    itemHeight: Dp = 48.dp,
    visibleCount: Int = 5,
) {
    require(visibleCount % 2 == 1) { "visibleCount must be odd." }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        NumberWheel(
            label = "분",
            range = 0..59,
            value = minutes.coerceIn(0, 59),
            onValueSelected = onMinutesChange,
            itemHeight = itemHeight,
            visibleCount = visibleCount
        )

        NumberWheel(
            label = "초",
            range = 0..59,
            value = seconds.coerceIn(0, 59),
            onValueSelected = onSecondsChange,
            itemHeight = itemHeight,
            visibleCount = visibleCount
        )
    }
}

@Suppress("FrequentlyChangingValue")
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun NumberWheel(
    label: String,
    range: IntRange,
    value: Int,
    onValueSelected: (Int) -> Unit,
    itemHeight: Dp,
    visibleCount: Int,
) {
    val pad = visibleCount / 2
    val values = remember(range) { range.toList() }
    val maxFirstVisible = (values.lastIndex - (visibleCount - 1)).coerceAtLeast(0)

    val initialFirstVisible = remember(range) {
        (value - range.first - pad).coerceIn(0, maxFirstVisible)
    }
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = initialFirstVisible
    )
    val flingBehavior = rememberSnapFlingBehavior(listState)
    val coroutineScope = rememberCoroutineScope()

    var isUserScrolling by remember { mutableStateOf(false) }
    var hasInitialized by remember { mutableStateOf(false) }

    // 중앙 아이템 인덱스 계산
    val centerItemIndex =
        remember(listState.firstVisibleItemIndex, listState.firstVisibleItemScrollOffset) {
            derivedStateOf {
                val layoutInfo = listState.layoutInfo
                if (layoutInfo.visibleItemsInfo.isEmpty()) return@derivedStateOf -1

                val viewportCenter =
                    (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2
                layoutInfo.visibleItemsInfo.minByOrNull { item ->
                    val center = item.offset + item.size / 2
                    abs(center - viewportCenter)
                }?.index ?: -1
            }
        }.value

    val highlightColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
    val backgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    val textColor = MaterialTheme.colorScheme.onSurface
    val textMuted = MaterialTheme.colorScheme.onSurfaceVariant

    LaunchedEffect(value) {
        if (!hasInitialized) {
            hasInitialized = true
            return@LaunchedEffect
        }
        if (listState.isScrollInProgress || isUserScrolling) return@LaunchedEffect
        val desiredFirst = (value - range.first - pad).coerceIn(0, maxFirstVisible)
        if (listState.firstVisibleItemIndex != desiredFirst) {
            listState.animateScrollToItem(desiredFirst)
        }
    }

    LaunchedEffect(listState.isScrollInProgress) {
        if (listState.isScrollInProgress) {
            isUserScrolling = true
        } else {
            delay(50)
            val layoutInfo = listState.layoutInfo
            if (layoutInfo.visibleItemsInfo.isEmpty()) {
                isUserScrolling = false
                return@LaunchedEffect
            }

            val viewportCenter = (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2
            val nearest = layoutInfo.visibleItemsInfo.minByOrNull { item ->
                val center = item.offset + item.size / 2
                abs(center - viewportCenter)
            } ?: run {
                isUserScrolling = false
                return@LaunchedEffect
            }

            val centerIndex = nearest.index
            val snappedValue = values.getOrNull(centerIndex) ?: run {
                isUserScrolling = false
                return@LaunchedEffect
            }

            if (snappedValue != value) {
                onValueSelected(snappedValue)
                delay(100)
            }

            isUserScrolling = false
        }
    }

    Box(
        modifier = Modifier
            .clip(MaterialTheme.shapes.large)
            .background(backgroundColor)
            .height(itemHeight * visibleCount)
            .width(92.dp)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .height(itemHeight)
                .fillMaxWidth()
                .background(
                    color = highlightColor,
                    shape = MaterialTheme.shapes.medium
                )
        )

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxWidth(),
            flingBehavior = flingBehavior,
            contentPadding = PaddingValues(vertical = itemHeight * pad),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            itemsIndexed(values) { index, item ->
                val isSelected = index == centerItemIndex

                val distanceFromCenter = if (centerItemIndex >= 0) {
                    abs(index - centerItemIndex)
                } else {
                    pad
                }

                val alpha = when {
                    distanceFromCenter == 0 -> 1.0f
                    distanceFromCenter <= pad -> 1.0f - (distanceFromCenter.toFloat() / pad.toFloat() * 0.7f)
                    else -> 0.3f
                }.coerceIn(0.3f, 1.0f)

                Box(
                    modifier = Modifier
                        .height(itemHeight)
                        .fillMaxWidth()
                        .clickable {
                            onValueSelected(item)
                            coroutineScope.launch {
                                val desiredFirst = (index - pad).coerceIn(0, maxFirstVisible)
                                listState.animateScrollToItem(desiredFirst)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item.toString().padStart(2, '0'),
                        fontSize = if (isSelected) 24.sp else 18.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                        color = if (isSelected) textColor else textMuted,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .alpha(alpha)
                    )
                }
            }
        }

        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = textMuted,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 12.dp)
        )
    }
}

@Preview
@Composable
private fun DurationWheelPickerPreview() {
    var minutes by rememberSaveable { mutableStateOf(4) }
    var seconds by rememberSaveable { mutableStateOf(20) }

    DurationWheelPicker(
        minutes = minutes,
        seconds = seconds,
        onMinutesChange = { minutes = it },
        onSecondsChange = { seconds = it }
    )
}

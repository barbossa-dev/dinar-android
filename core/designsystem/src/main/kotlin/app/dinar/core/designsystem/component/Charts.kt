package app.dinar.core.designsystem.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class DonutSlice(val value: Float, val color: Color)

/**
 * An animated donut chart. Slices are drawn clockwise from 12 o'clock with a
 * small gap between them; [center] is laid out in the hole.
 */
@Composable
fun DonutChart(
    slices: List<DonutSlice>,
    contentDescription: String,
    modifier: Modifier = Modifier,
    thickness: Dp = 22.dp,
    trackColor: Color = MaterialTheme.colorScheme.surfaceContainerHighest,
    center: @Composable BoxScope.() -> Unit = {},
) {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(slices) {
        progress.snapTo(0f)
        progress.animateTo(1f, tween(durationMillis = 900, easing = FastOutSlowInEasing))
    }
    val total = slices.sumOf { it.value.toDouble() }.toFloat()

    Box(modifier.semantics { this.contentDescription = contentDescription }, contentAlignment = Alignment.Center) {
        Canvas(Modifier.matchParentSize()) {
            val stroke = thickness.toPx()
            val diameter = size.minDimension - stroke
            val topLeft = Offset((size.width - diameter) / 2, (size.height - diameter) / 2)
            val arcSize = Size(diameter, diameter)
            drawArc(trackColor, 0f, 360f, false, topLeft, arcSize, style = Stroke(stroke))
            if (total <= 0f) return@Canvas
            val gap = if (slices.count { it.value > 0 } > 1) 2.5f else 0f
            var start = -90f
            slices.forEach { slice ->
                val sweep = 360f * (slice.value / total) * progress.value
                if (sweep > gap) {
                    drawArc(
                        color = slice.color,
                        startAngle = start + gap / 2,
                        sweepAngle = sweep - gap,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(stroke, cap = StrokeCap.Butt),
                    )
                }
                start += sweep
            }
        }
        center()
    }
}

data class BarGroup(val label: String, val values: List<Float>)

/**
 * Grouped vertical bars, one color per series. The highlighted group's label is
 * emphasised (used for the current month).
 */
@Composable
fun GroupedBarChart(
    groups: List<BarGroup>,
    seriesColors: List<Color>,
    contentDescription: String,
    modifier: Modifier = Modifier,
    highlightIndex: Int = groups.lastIndex,
    barWidth: Dp = 10.dp,
    chartHeight: Dp = 120.dp,
) {
    val max = groups.flatMap { it.values }.maxOrNull()?.takeIf { it > 0f } ?: 1f
    Row(
        modifier = modifier
            .fillMaxWidth()
            .semantics { this.contentDescription = contentDescription },
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom,
    ) {
        groups.forEachIndexed { index, group ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(
                    modifier = Modifier.height(chartHeight),
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                    verticalAlignment = Alignment.Bottom,
                ) {
                    group.values.forEachIndexed { series, value ->
                        val fraction by animateFloatAsState(
                            targetValue = (value / max).coerceIn(0f, 1f),
                            animationSpec = tween(700, easing = FastOutSlowInEasing),
                            label = "bar",
                        )
                        Box(
                            Modifier
                                .width(barWidth)
                                .fillMaxHeight(fraction.coerceAtLeast(0.02f))
                                .background(
                                    seriesColors[series % seriesColors.size],
                                    RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp, bottomStart = 2.dp, bottomEnd = 2.dp),
                                ),
                        )
                    }
                }
                Text(
                    text = group.label,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (index == highlightIndex) FontWeight.Bold else FontWeight.Normal,
                    color = if (index == highlightIndex) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            }
        }
    }
}

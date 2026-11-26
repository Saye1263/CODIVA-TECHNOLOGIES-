package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.ui.theme.CodivaAmber
import com.example.ui.theme.CodivaCobalt
import com.example.ui.theme.CodivaEmerald

@Composable
fun PlatformAnalyticsChartCard(
    visitorsSeries: List<Float>,
    enrollmentsSeries: List<Float>,
    paymentsSeries: List<Float>,
    labels: List<String>,
    modifier: Modifier = Modifier,
    timeframeLabel: String = "Live Firestore",
    seriesLabels: Triple<String, String, String> = Triple("Users", "Enrollments", "Payments")
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Platform Analytics", style = MaterialTheme.typography.titleMedium)
                Text(
                    timeframeLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                LegendDot(seriesLabels.first, CodivaCobalt)
                LegendDot(seriesLabels.second, CodivaEmerald)
                LegendDot(seriesLabels.third, CodivaAmber)
            }

            val gridColor = MaterialTheme.colorScheme.outlineVariant
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(155.dp)
            ) {
                val maxVal = (visitorsSeries + enrollmentsSeries + paymentsSeries).maxOrNull()?.coerceAtLeast(1f) ?: 1f
                val stepX = if (labels.size > 1) size.width / (labels.size - 1) else size.width

                // Horizontal guide lines
                for (i in 0..3) {
                    val y = size.height * (i / 3f)
                    drawLine(
                        color = gridColor,
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                fun drawSeries(values: List<Float>, color: Color) {
                    if (values.isEmpty()) return
                    val path = Path()
                    values.forEachIndexed { idx, v ->
                        val x = idx * stepX
                        val y = size.height - ((v / maxVal) * (size.height * 0.88f)) - (size.height * 0.06f)
                        if (idx == 0) path.moveTo(x, y) else path.lineTo(x, y)
                    }
                    drawPath(
                        path = path,
                        color = color,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )
                    values.forEachIndexed { idx, v ->
                        val x = idx * stepX
                        val y = size.height - ((v / maxVal) * (size.height * 0.88f)) - (size.height * 0.06f)
                        drawCircle(color = color, radius = 4.dp.toPx(), center = Offset(x, y))
                    }
                }

                drawSeries(visitorsSeries, CodivaCobalt)
                drawSeries(enrollmentsSeries, CodivaEmerald)
                drawSeries(paymentsSeries, CodivaAmber)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                labels.forEach { lbl ->
                    Text(
                        text = lbl,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun UserRolesDonutCard(
    slices: List<Triple<String, Float, Color>>,
    totalLabel: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Ecosystem Role Distribution", style = MaterialTheme.typography.titleMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Box(
                    modifier = Modifier.size(124.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(116.dp)) {
                        val total = slices.sumOf { it.second.toDouble() }.toFloat().coerceAtLeast(1f)
                        var startAngle = -90f
                        val strokeWidth = 20.dp.toPx()
                        val inset = strokeWidth / 2f
                        slices.forEach { (_, value, color) ->
                            val sweep = (value / total) * 360f
                            drawArc(
                                color = color,
                                startAngle = startAngle,
                                sweepAngle = (sweep - 3f).coerceAtLeast(1f),
                                useCenter = false,
                                topLeft = Offset(inset, inset),
                                size = Size(size.width - strokeWidth, size.height - strokeWidth),
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                            )
                            startAngle += sweep
                        }
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(totalLabel, style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Accounts",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val total = slices.sumOf { it.second.toDouble() }.toFloat().coerceAtLeast(1f)
                    slices.forEach { (label, value, color) ->
                        val pct = ((value / total) * 100f).toInt()
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(label, style = MaterialTheme.typography.bodySmall)
                            }
                            Text(
                                "$pct%",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LegendDot(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(9.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

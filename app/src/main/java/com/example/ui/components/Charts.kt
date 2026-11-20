package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.ClaimCalculations
import com.example.ui.DealerStat
import com.example.ui.KendalaStat
import com.example.ui.theme.Amber500
import com.example.ui.theme.Blue500
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Indigo600
import com.example.ui.theme.Rose500
import com.example.ui.theme.Slate400
import com.example.ui.theme.Violet500
import kotlin.math.max

fun kendalaColor(kendala: String): Color = when (kendala) {
    "Claim Complete" -> Emerald500
    "Verifikasi Part" -> Amber500
    "Sudah Diinput, Menunggu Diadjust" -> Blue500
    "Dokumen Kurang" -> Rose500
    "Form Claim Tidak Lengkap" -> Violet500
    else -> Slate400
}

@Composable
fun DealerComboChartCard(
    dealerStats: List<DealerStat>,
    onDealerClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedIdx by remember(dealerStats) { mutableIntStateOf(-1) }
    val gridColor = MaterialTheme.colorScheme.outlineVariant

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Nilai Claim & Belum Adjust",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Ketuk bar untuk lihat detail dealer aktif",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = "${dealerStats.size} Dealer",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Legend
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LegendChip(color = Indigo600, label = "Nilai Claim (Rp)")
                LegendChip(color = Amber500, label = "Belum Adjust (Kasus)")
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (dealerStats.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Belum ada data dealer aktif",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                val maxAmount = max(1.0, dealerStats.maxOf { it.amount })
                val maxUnadj = max(1, dealerStats.maxOf { it.unadjusted })

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(190.dp)
                        .pointerInput(dealerStats) {
                            detectTapGestures { offset ->
                                val slotW = size.width.toFloat() / dealerStats.size
                                val tapped = (offset.x / slotW).toInt().coerceIn(0, dealerStats.lastIndex)
                                selectedIdx = if (selectedIdx == tapped) -1 else tapped
                            }
                        }
                ) {
                    val chartBottom = size.height - 10f
                    val chartTop = 14f
                    val chartH = (chartBottom - chartTop).coerceAtLeast(10f)

                    // Horizontal grid lines
                    for (step in 0..3) {
                        val y = chartTop + chartH * (step / 3f)
                        drawLine(
                            color = gridColor,
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = 1.5f
                        )
                    }

                    val slotW = size.width / dealerStats.size
                    val barW = (slotW * 0.56f).coerceIn(12f, 44f)
                    val linePoints = mutableListOf<Offset>()

                    dealerStats.forEachIndexed { idx, stat ->
                        val centerX = slotW * idx + slotW / 2f
                        val barH = ((stat.amount / maxAmount).toFloat() * chartH).coerceAtLeast(4f)
                        val barTop = chartBottom - barH
                        val isSelected = idx == selectedIdx

                        drawRoundRect(
                            color = if (isSelected) Indigo600 else Indigo600.copy(alpha = 0.85f),
                            topLeft = Offset(centerX - barW / 2f, barTop),
                            size = Size(barW, barH),
                            cornerRadius = CornerRadius(10f, 10f)
                        )

                        val lineY = chartBottom - ((stat.unadjusted.toFloat() / maxUnadj) * chartH)
                        linePoints.add(Offset(centerX, lineY))
                    }

                    if (linePoints.size >= 2) {
                        val path = Path().apply {
                            moveTo(linePoints.first().x, linePoints.first().y)
                            for (i in 1 until linePoints.size) {
                                val prev = linePoints[i - 1]
                                val curr = linePoints[i]
                                val midX = (prev.x + curr.x) / 2f
                                cubicTo(midX, prev.y, midX, curr.y, curr.x, curr.y)
                            }
                        }
                        drawPath(
                            path = path,
                            color = Amber500,
                            style = Stroke(width = 5f, cap = StrokeCap.Round)
                        )
                    }

                    linePoints.forEachIndexed { idx, pt ->
                        val radius = if (idx == selectedIdx) 11f else 8f
                        drawCircle(color = Color.White, radius = radius + 3f, center = pt)
                        drawCircle(color = Amber500, radius = radius, center = pt)
                    }
                }

                // X-axis dealer labels
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    dealerStats.forEachIndexed { idx, stat ->
                        val shortLabel = stat.dealer.removePrefix("TB ").trim()
                        Text(
                            text = shortLabel,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (idx == selectedIdx) FontWeight.Bold else FontWeight.Medium,
                            color = if (idx == selectedIdx) Indigo600 else MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onDealerClick(stat.dealer) }
                                .padding(vertical = 4.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }

                // Selected tooltip pill
                if (selectedIdx in dealerStats.indices) {
                    val sel = dealerStats[selectedIdx]
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onDealerClick(sel.dealer) },
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "${sel.dealer} (${sel.partQty} part)",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "${ClaimCalculations.formatRupiah(sel.amount)} • ${sel.unadjusted} belum adjust",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                                )
                            }
                            Text(
                                text = "Detail →",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun KendalaDonutChartCard(
    kendalaStats: List<KendalaStat>,
    totalCases: Int,
    onKendalaClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val activeStats = remember(kendalaStats) { kendalaStats.filter { it.count > 0 } }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Distribusi Kendala Audit Claim",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Proporsi status penyelesaian & kendala lapangan",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (activeStats.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Belum ada data kendala",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(165.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(152.dp)) {
                        val strokeW = 28.dp.toPx()
                        val diameter = size.minDimension - strokeW
                        val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
                        val arcSize = Size(diameter, diameter)
                        val total = activeStats.sumOf { it.count }.coerceAtLeast(1)
                        var startAngle = -90f

                        activeStats.forEach { stat ->
                            val sweep = (stat.count.toFloat() / total) * 360f
                            val gap = if (activeStats.size > 1) 2.5f else 0f
                            drawArc(
                                color = kendalaColor(stat.kendala),
                                startAngle = startAngle,
                                sweepAngle = (sweep - gap).coerceAtLeast(1f),
                                useCenter = false,
                                topLeft = topLeft,
                                size = arcSize,
                                style = Stroke(width = strokeW, cap = StrokeCap.Butt)
                            )
                            startAngle += sweep
                        }
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = totalCases.toString(),
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Kasus",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    activeStats.forEach { stat ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onKendalaClick(stat.kendala) }
                                .padding(horizontal = 6.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(kendalaColor(stat.kendala))
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = stat.kendala,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${stat.count} (${String.format(java.util.Locale.US, "%.1f", stat.percentage)}%)",
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
private fun LegendChip(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
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

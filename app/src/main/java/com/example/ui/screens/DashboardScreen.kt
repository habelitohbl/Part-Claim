package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.ClaimCalculations
import com.example.data.ClaimConstants
import com.example.ui.AuditClaimUiState
import com.example.ui.DealerRecapMode
import com.example.ui.DealerStat
import com.example.ui.KendalaStat
import com.example.ui.components.DealerComboChartCard
import com.example.ui.components.KendalaDonutChartCard
import com.example.ui.components.kendalaColor
import com.example.ui.theme.Amber50
import com.example.ui.theme.Amber600
import com.example.ui.theme.Blue50
import com.example.ui.theme.Blue600
import com.example.ui.theme.Emerald50
import com.example.ui.theme.Emerald600
import com.example.ui.theme.Indigo50
import com.example.ui.theme.Indigo600
import com.example.ui.theme.Rose50
import com.example.ui.theme.Rose600
import com.example.ui.theme.Violet50
import com.example.ui.theme.Violet600
import java.util.Locale

@Composable
fun DashboardScreen(
    state: AuditClaimUiState,
    onFilterByStatus: (String) -> Unit,
    onFilterByDealer: (String) -> Unit,
    onFilterByKendala: (String) -> Unit,
    onDealerRecapModeChange: (DealerRecapMode) -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isWide = maxWidth >= 840.dp
        val isMedium = maxWidth >= 600.dp

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("dashboard_list"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 6 KPI Cards Grid
            item {
                val kpiItems = listOf(
                    KpiCardData(
                        title = "TOTAL PENGAJUAN",
                        value = state.kpis.totalPengajuan.toString(),
                        subtitle = "transaksi",
                        icon = Icons.Rounded.Description,
                        iconBg = Blue50,
                        iconTint = Blue600,
                        valueColor = MaterialTheme.colorScheme.onSurface,
                        borderColor = MaterialTheme.colorScheme.outlineVariant,
                        tag = "kpi_total_pengajuan",
                        onClick = null
                    ),
                    KpiCardData(
                        title = "BELUM ADJUST",
                        value = state.kpis.belumAdjust.toString(),
                        subtitle = "(${String.format(Locale.US, "%.1f", state.kpis.belumPct)}%)",
                        icon = Icons.Rounded.Schedule,
                        iconBg = Amber50,
                        iconTint = Amber600,
                        valueColor = Amber600,
                        borderColor = Amber600.copy(alpha = 0.35f),
                        tag = "kpi_belum_adjust",
                        onClick = { onFilterByStatus(ClaimConstants.STATUS_NOT_ADJUSTED) }
                    ),
                    KpiCardData(
                        title = "SUDAH ADJUST",
                        value = state.kpis.sudahAdjust.toString(),
                        subtitle = "(${String.format(Locale.US, "%.1f", state.kpis.sudahPct)}%)",
                        icon = Icons.Rounded.CheckCircle,
                        iconBg = Emerald50,
                        iconTint = Emerald600,
                        valueColor = Emerald600,
                        borderColor = Emerald600.copy(alpha = 0.35f),
                        tag = "kpi_sudah_adjust",
                        onClick = { onFilterByStatus(ClaimConstants.STATUS_ADJUSTED) }
                    ),
                    KpiCardData(
                        title = "TOTAL PART",
                        value = state.kpis.totalPart.toString(),
                        subtitle = "unit qty",
                        icon = Icons.Rounded.Inventory2,
                        iconBg = Violet50,
                        iconTint = Violet600,
                        valueColor = MaterialTheme.colorScheme.onSurface,
                        borderColor = MaterialTheme.colorScheme.outlineVariant,
                        tag = "kpi_total_part",
                        onClick = null
                    ),
                    KpiCardData(
                        title = "NILAI CLAIM",
                        value = ClaimCalculations.formatRupiah(state.kpis.nilaiClaim),
                        subtitle = "akumulasi",
                        icon = Icons.Rounded.AccountBalanceWallet,
                        iconBg = Indigo50,
                        iconTint = Indigo600,
                        valueColor = MaterialTheme.colorScheme.onSurface,
                        borderColor = MaterialTheme.colorScheme.outlineVariant,
                        tag = "kpi_nilai_claim",
                        onClick = null
                    ),
                    KpiCardData(
                        title = "AVG. LEAD TIME",
                        value = String.format(Locale.US, "%.1f", state.kpis.avgLeadTime),
                        subtitle = "hari",
                        icon = Icons.Rounded.Timer,
                        iconBg = Rose50,
                        iconTint = Rose600,
                        valueColor = MaterialTheme.colorScheme.onSurface,
                        borderColor = MaterialTheme.colorScheme.outlineVariant,
                        tag = "kpi_avg_leadtime",
                        onClick = null
                    )
                )

                val cols = when {
                    isWide -> 3
                    isMedium -> 3
                    else -> 2
                }
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    kpiItems.chunked(cols).forEach { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            rowItems.forEach { card ->
                                KpiCard(
                                    data = card,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            repeat(cols - rowItems.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            // Charts Row
            item {
                if (isWide) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        DealerComboChartCard(
                            dealerStats = state.activeDealerStats,
                            onDealerClick = onFilterByDealer,
                            modifier = Modifier.weight(1.3f)
                        )
                        KendalaDonutChartCard(
                            kendalaStats = state.kendalaStats,
                            totalCases = state.kpis.totalPengajuan,
                            onKendalaClick = onFilterByKendala,
                            modifier = Modifier.weight(1f)
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        DealerComboChartCard(
                            dealerStats = state.activeDealerStats,
                            onDealerClick = onFilterByDealer,
                            modifier = Modifier.fillMaxWidth()
                        )
                        KendalaDonutChartCard(
                            kendalaStats = state.kendalaStats,
                            totalCases = state.kpis.totalPengajuan,
                            onKendalaClick = onFilterByKendala,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Recap Tables Row
            item {
                if (isWide) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        DealerRecapCard(
                            dealerStats = state.displayDealerStats,
                            mode = state.dealerRecapMode,
                            totalPart = state.kpis.totalPart,
                            totalAmount = state.kpis.nilaiClaim,
                            totalUnadjusted = state.kpis.belumAdjust,
                            totalDealersCount = state.dealersMaster.size,
                            onModeChange = onDealerRecapModeChange,
                            onDealerClick = onFilterByDealer,
                            modifier = Modifier.weight(1.3f)
                        )
                        KendalaRecapCard(
                            kendalaStats = state.kendalaStats,
                            totalCount = state.kpis.totalPengajuan,
                            onKendalaClick = onFilterByKendala,
                            modifier = Modifier.weight(1f)
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        DealerRecapCard(
                            dealerStats = state.displayDealerStats,
                            mode = state.dealerRecapMode,
                            totalPart = state.kpis.totalPart,
                            totalAmount = state.kpis.nilaiClaim,
                            totalUnadjusted = state.kpis.belumAdjust,
                            totalDealersCount = state.dealersMaster.size,
                            onModeChange = onDealerRecapModeChange,
                            onDealerClick = onFilterByDealer,
                            modifier = Modifier.fillMaxWidth()
                        )
                        KendalaRecapCard(
                            kendalaStats = state.kendalaStats,
                            totalCount = state.kpis.totalPengajuan,
                            onKendalaClick = onFilterByKendala,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(72.dp)) }
        }
    }
}

private data class KpiCardData(
    val title: String,
    val value: String,
    val subtitle: String,
    val icon: ImageVector,
    val iconBg: Color,
    val iconTint: Color,
    val valueColor: Color,
    val borderColor: Color,
    val tag: String,
    val onClick: (() -> Unit)?
)

@Composable
private fun KpiCard(
    data: KpiCardData,
    modifier: Modifier = Modifier
) {
    val cardModifier = if (data.onClick != null) {
        modifier
            .testTag(data.tag)
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = data.onClick)
    } else {
        modifier.testTag(data.tag)
    }

    Surface(
        modifier = cardModifier,
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        border = BorderStroke(1.dp, data.borderColor)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = data.title,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(data.iconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = data.icon,
                        contentDescription = data.title,
                        tint = data.iconTint,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = data.value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = data.valueColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = data.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DealerRecapCard(
    dealerStats: List<DealerStat>,
    mode: DealerRecapMode,
    totalPart: Int,
    totalAmount: Double,
    totalUnadjusted: Int,
    totalDealersCount: Int,
    onModeChange: (DealerRecapMode) -> Unit,
    onDealerClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Rekapitulasi Claim per Dealer",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Ketuk baris dealer untuk melihat detail transaksi",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    SegmentedButton(
                        selected = mode == DealerRecapMode.ACTIVE_ONLY,
                        onClick = { onModeChange(DealerRecapMode.ACTIVE_ONLY) },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                        modifier = Modifier.testTag("btn_dealer_active")
                    ) {
                        Text("Dealer Aktif", maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    SegmentedButton(
                        selected = mode == DealerRecapMode.ALL_DEALERS,
                        onClick = { onModeChange(DealerRecapMode.ALL_DEALERS) },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                        modifier = Modifier.testTag("btn_dealer_all")
                    ) {
                        Text("Semua $totalDealersCount Dealer", maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // Table Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("DEALER", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1.2f))
                Text("PART", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.End, modifier = Modifier.weight(0.6f))
                Text("NILAI CLAIM", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.End, modifier = Modifier.weight(1.5f))
                Text("BLM ADJ", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center, modifier = Modifier.weight(0.8f))
            }

            dealerStats.forEach { d ->
                val dotColor = when {
                    d.unadjusted > 0 -> Amber600
                    d.count > 0 -> Emerald600
                    else -> MaterialTheme.colorScheme.outline
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onDealerClick(d.dealer) }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1.2f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(dotColor)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = d.dealer,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = if (d.count > 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = d.partQty.toString(),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.End,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(0.6f)
                    )
                    Text(
                        text = ClaimCalculations.formatRupiah(d.amount),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.End,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1.5f)
                    )
                    Box(
                        modifier = Modifier.weight(0.8f),
                        contentAlignment = Alignment.Center
                    ) {
                        if (d.unadjusted > 0) {
                            Surface(
                                shape = CircleShape,
                                color = Amber50
                            ) {
                                Text(
                                    text = d.unadjusted.toString(),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Amber600,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        } else {
                            Text(
                                text = "0",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            }

            // Grand Total Footer
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("GRAND TOTAL", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.2f))
                Text(totalPart.toString(), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, textAlign = TextAlign.End, modifier = Modifier.weight(0.6f))
                Text(ClaimCalculations.formatRupiah(totalAmount), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, textAlign = TextAlign.End, modifier = Modifier.weight(1.5f))
                Text(totalUnadjusted.toString(), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = Amber600, textAlign = TextAlign.Center, modifier = Modifier.weight(0.8f))
            }
        }
    }
}

@Composable
private fun KendalaRecapCard(
    kendalaStats: List<KendalaStat>,
    totalCount: Int,
    onKendalaClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Rekapitulasi Kategori Kendala",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Ketuk kendala untuk memfilter transaksi terkait",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("KATEGORI KENDALA", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(2f))
                Text("KASUS", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.End, modifier = Modifier.weight(0.6f))
                Text("%", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.End, modifier = Modifier.weight(0.7f))
            }

            kendalaStats.forEach { k ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onKendalaClick(k.kendala) }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(2f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(9.dp)
                                .clip(CircleShape)
                                .background(kendalaColor(k.kendala))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = k.kendala,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = if (k.count > 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        text = k.count.toString(),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.End,
                        modifier = Modifier.weight(0.6f)
                    )
                    Text(
                        text = "${String.format(Locale.US, "%.1f", k.percentage)}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.End,
                        modifier = Modifier.weight(0.7f)
                    )
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("TOTAL KESELURUHAN", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(2f))
                Text(totalCount.toString(), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, textAlign = TextAlign.End, modifier = Modifier.weight(0.6f))
                Text("100%", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, textAlign = TextAlign.End, modifier = Modifier.weight(0.7f))
            }
        }
    }
}

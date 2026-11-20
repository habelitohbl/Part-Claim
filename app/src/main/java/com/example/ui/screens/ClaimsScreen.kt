package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.FilterAltOff
import androidx.compose.material.icons.rounded.Inbox
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.ClaimCalculations
import com.example.data.ClaimConstants
import com.example.data.EnrichedClaim
import com.example.ui.AuditClaimUiState
import com.example.ui.components.kendalaColor
import com.example.ui.theme.Amber50
import com.example.ui.theme.Amber600
import com.example.ui.theme.Emerald50
import com.example.ui.theme.Emerald600
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.Rose600

@Composable
fun ClaimsScreen(
    state: AuditClaimUiState,
    onSearchChange: (String) -> Unit,
    onDealerFilterChange: (String) -> Unit,
    onStatusFilterChange: (String) -> Unit,
    onKendalaFilterChange: (String) -> Unit,
    onResetFilters: () -> Unit,
    onQuickAdjust: (Int) -> Unit,
    onEditClaim: (EnrichedClaim) -> Unit,
    onDeleteClaim: (EnrichedClaim) -> Unit,
    modifier: Modifier = Modifier
) {
    val allDealers = remember(state.dealersMaster, state.allClaims) {
        (state.dealersMaster + state.allClaims.map { it.dealer }).distinct().sorted()
    }
    var showDealerMenu by remember { mutableStateOf(false) }
    var showStatusMenu by remember { mutableStateOf(false) }
    var showKendalaMenu by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxSize()) {
        // Search & Filter Header
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 1.dp,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = state.filterState.searchQuery,
                    onValueChange = onSearchChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("claims_search_input"),
                    placeholder = { Text("Cari No, Part Number, Nama Part, Dealer...") },
                    leadingIcon = {
                        Icon(Icons.Rounded.Search, contentDescription = "Cari")
                    },
                    trailingIcon = {
                        if (state.filterState.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchChange("") }) {
                                Icon(Icons.Rounded.Close, contentDescription = "Hapus pencarian")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Dealer Filter Chip + Dropdown
                    Box {
                        FilterChip(
                            selected = state.filterState.dealerFilter.isNotEmpty(),
                            onClick = { showDealerMenu = true },
                            label = {
                                Text(
                                    text = state.filterState.dealerFilter.ifEmpty { "Semua Dealer" },
                                    maxLines = 1
                                )
                            },
                            trailingIcon = {
                                Icon(Icons.Rounded.ArrowDropDown, contentDescription = null)
                            },
                            modifier = Modifier.testTag("filter_dealer_chip")
                        )
                        DropdownMenu(
                            expanded = showDealerMenu,
                            onDismissRequest = { showDealerMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Semua Dealer") },
                                onClick = {
                                    onDealerFilterChange("")
                                    showDealerMenu = false
                                }
                            )
                            allDealers.forEach { d ->
                                DropdownMenuItem(
                                    text = { Text(d) },
                                    onClick = {
                                        onDealerFilterChange(d)
                                        showDealerMenu = false
                                    }
                                )
                            }
                        }
                    }

                    // Status Filter Chip + Dropdown
                    Box {
                        FilterChip(
                            selected = state.filterState.statusFilter.isNotEmpty(),
                            onClick = { showStatusMenu = true },
                            label = {
                                Text(
                                    text = when (state.filterState.statusFilter) {
                                        ClaimConstants.STATUS_NOT_ADJUSTED -> "Not Adjusted"
                                        ClaimConstants.STATUS_ADJUSTED -> "Adjusted"
                                        else -> "Semua Status"
                                    },
                                    maxLines = 1
                                )
                            },
                            trailingIcon = {
                                Icon(Icons.Rounded.ArrowDropDown, contentDescription = null)
                            },
                            modifier = Modifier.testTag("filter_status_chip")
                        )
                        DropdownMenu(
                            expanded = showStatusMenu,
                            onDismissRequest = { showStatusMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Semua Status") },
                                onClick = {
                                    onStatusFilterChange("")
                                    showStatusMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Not Adjusted (Belum)") },
                                onClick = {
                                    onStatusFilterChange(ClaimConstants.STATUS_NOT_ADJUSTED)
                                    showStatusMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Adjusted (Sudah)") },
                                onClick = {
                                    onStatusFilterChange(ClaimConstants.STATUS_ADJUSTED)
                                    showStatusMenu = false
                                }
                            )
                        }
                    }

                    // Kendala Filter Chip + Dropdown
                    Box {
                        FilterChip(
                            selected = state.filterState.kendalaFilter.isNotEmpty(),
                            onClick = { showKendalaMenu = true },
                            label = {
                                Text(
                                    text = state.filterState.kendalaFilter.ifEmpty { "Semua Kendala" },
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            trailingIcon = {
                                Icon(Icons.Rounded.ArrowDropDown, contentDescription = null)
                            },
                            modifier = Modifier.testTag("filter_kendala_chip")
                        )
                        DropdownMenu(
                            expanded = showKendalaMenu,
                            onDismissRequest = { showKendalaMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Semua Kendala") },
                                onClick = {
                                    onKendalaFilterChange("")
                                    showKendalaMenu = false
                                }
                            )
                            state.allKendalaOptions.forEach { k ->
                                DropdownMenuItem(
                                    text = { Text(k) },
                                    onClick = {
                                        onKendalaFilterChange(k)
                                        showKendalaMenu = false
                                    }
                                )
                            }
                        }
                    }

                    val hasAnyFilter = state.filterState.searchQuery.isNotEmpty() ||
                        state.filterState.dealerFilter.isNotEmpty() ||
                        state.filterState.statusFilter.isNotEmpty() ||
                        state.filterState.kendalaFilter.isNotEmpty()

                    if (hasAnyFilter) {
                        TextButton(
                            onClick = onResetFilters,
                            modifier = Modifier.testTag("reset_filters_button")
                        ) {
                            Icon(
                                Icons.Rounded.FilterAltOff,
                                contentDescription = "Reset Filter",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Reset")
                        }
                    }
                }

                // Active Filter Info Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${state.filteredClaims.size} dari ${state.allClaims.size} data",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Subtotal: ${ClaimCalculations.formatRupiah(state.filteredSubtotalAmount)} (${state.filteredSubtotalQty} qty)",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Claims List
        if (state.filteredClaims.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Rounded.Inbox,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Tidak ada data claim yang cocok dengan filter.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("claims_list"),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.filteredClaims, key = { it.no }) { claim ->
                    ClaimItemCard(
                        claim = claim,
                        onQuickAdjust = { onQuickAdjust(claim.no) },
                        onEdit = { onEditClaim(claim) },
                        onDelete = { onDeleteClaim(claim) }
                    )
                }
                item { Spacer(modifier = Modifier.height(76.dp)) }
            }
        }
    }
}

@Composable
private fun ClaimItemCard(
    claim: EnrichedClaim,
    onQuickAdjust: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val isAdjusted = claim.status == ClaimConstants.STATUS_ADJUSTED
    val leadTimeColor = when {
        claim.leadTime > 120 -> Rose600
        claim.leadTime > 60 -> Amber600
        else -> MaterialTheme.colorScheme.onSurface
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("claim_card_${claim.no}"),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Top Row: #No + Dealer Badge + Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "#${claim.no}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = claim.dealer,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    Text(
                        text = "${claim.leadTime} hr",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = leadTimeColor
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = if (isAdjusted) Emerald50 else Amber50,
                    border = BorderStroke(
                        1.dp,
                        if (isAdjusted) Emerald600.copy(alpha = 0.3f) else Amber600.copy(alpha = 0.3f)
                    )
                ) {
                    Text(
                        text = claim.status,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isAdjusted) Emerald600 else Amber600,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Part Number + Nama Part
            Text(
                text = claim.partNumber,
                fontFamily = JetBrainsMonoFontFamily,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = claim.namaPart,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Dates & Amount row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Ajuan: ${ClaimCalculations.formatDateDisplay(claim.tglPengajuan)}  •  Audit: ${ClaimCalculations.formatDateDisplay(claim.tglAudit)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Adjust: ${ClaimCalculations.formatDateDisplay(claim.tglAdjust)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = ClaimCalculations.formatRupiah(claim.orderAmount),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Qty: ${claim.qty} unit",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
            Spacer(modifier = Modifier.height(8.dp))

            // Kendala + Actions Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(kendalaColor(claim.kendala))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = claim.kendala,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!isAdjusted) {
                        IconButton(
                            onClick = onQuickAdjust,
                            modifier = Modifier.testTag("btn_quick_adjust_${claim.no}")
                        ) {
                            Icon(
                                Icons.Rounded.CheckCircle,
                                contentDescription = "Quick Adjust Hari Ini",
                                tint = Emerald600
                            )
                        }
                    }
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.testTag("btn_edit_claim_${claim.no}")
                    ) {
                        Icon(
                            Icons.Rounded.Edit,
                            contentDescription = "Edit Claim",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.testTag("btn_delete_claim_${claim.no}")
                    ) {
                        Icon(
                            Icons.Rounded.Delete,
                            contentDescription = "Hapus Claim",
                            tint = Rose600
                        )
                    }
                }
            }
        }
    }
}

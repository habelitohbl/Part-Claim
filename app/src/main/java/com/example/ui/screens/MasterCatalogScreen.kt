package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.ClaimCalculations
import com.example.ui.AuditClaimUiState
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.Rose600

@Composable
fun MasterCatalogScreen(
    state: AuditClaimUiState,
    onPartSearchChange: (String) -> Unit,
    onSavePart: (String, String, Double) -> Unit,
    onDeletePart: (String) -> Unit,
    onAddDealer: (String) -> Unit,
    onDeleteDealer: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedSection by remember { mutableIntStateOf(0) } // 0 = Parts, 1 = Dealers

    var newPartNumber by remember { mutableStateOf("") }
    var newPartName by remember { mutableStateOf("") }
    var newPartPrice by remember { mutableStateOf("") }
    var newDealerCode by remember { mutableStateOf("") }

    Column(modifier = modifier.fillMaxSize()) {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 1.dp,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    SegmentedButton(
                        selected = selectedSection == 0,
                        onClick = { selectedSection = 0 },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                        modifier = Modifier.testTag("master_tab_parts")
                    ) {
                        Text("Katalog Part (${state.partsMaster.size})")
                    }
                    SegmentedButton(
                        selected = selectedSection == 1,
                        onClick = { selectedSection = 1 },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                        modifier = Modifier.testTag("master_tab_dealers")
                    ) {
                        Text("Daftar Dealer (${state.dealersMaster.size})")
                    }
                }
            }
        }

        if (selectedSection == 0) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "Tambah / Perbarui Master Part",
                                style = MaterialTheme.typography.titleSmall
                            )
                            OutlinedTextField(
                                value = newPartNumber,
                                onValueChange = { newPartNumber = it.uppercase() },
                                label = { Text("Part Number") },
                                placeholder = { Text("Mis. B6YH58101100") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                                modifier = Modifier.fillMaxWidth().testTag("input_master_part_number")
                            )
                            OutlinedTextField(
                                value = newPartName,
                                onValueChange = { newPartName = it.uppercase() },
                                label = { Text("Nama Part") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                                modifier = Modifier.fillMaxWidth().testTag("input_master_part_name")
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = newPartPrice,
                                    onValueChange = { newPartPrice = it.filter { ch -> ch.isDigit() || ch == '.' } },
                                    label = { Text("Harga Ref (Rp)") },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f).testTag("input_master_part_price")
                                )
                                Button(
                                    onClick = {
                                        if (newPartNumber.isNotBlank() && newPartName.isNotBlank()) {
                                            onSavePart(
                                                newPartNumber,
                                                newPartName,
                                                newPartPrice.toDoubleOrNull() ?: 0.0
                                            )
                                            newPartNumber = ""
                                            newPartName = ""
                                            newPartPrice = ""
                                        }
                                    },
                                    enabled = newPartNumber.isNotBlank() && newPartName.isNotBlank(),
                                    modifier = Modifier
                                        .height(52.dp)
                                        .testTag("btn_save_master_part")
                                ) {
                                    Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Simpan")
                                }
                            }
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = state.masterPartSearch,
                        onValueChange = onPartSearchChange,
                        placeholder = { Text("Cari part number atau nama part...") },
                        leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                        trailingIcon = {
                            if (state.masterPartSearch.isNotEmpty()) {
                                IconButton(onClick = { onPartSearchChange("") }) {
                                    Icon(Icons.Rounded.Close, contentDescription = "Hapus")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth().testTag("search_master_part")
                    )
                }

                items(state.filteredPartsMaster, key = { it.partNumber }) { part ->
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = part.partNumber,
                                    fontFamily = JetBrainsMonoFontFamily,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = part.namaPart,
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Harga Ref: ${ClaimCalculations.formatRupiah(part.unitPrice)}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(
                                onClick = { onDeletePart(part.partNumber) },
                                modifier = Modifier.testTag("btn_delete_part_${part.partNumber}")
                            ) {
                                Icon(Icons.Rounded.Delete, contentDescription = "Hapus Part", tint = Rose600)
                            }
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(72.dp)) }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = newDealerCode,
                                onValueChange = { newDealerCode = it.uppercase() },
                                label = { Text("Kode Dealer Baru") },
                                placeholder = { Text("Mis. TB XYZ") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                                modifier = Modifier.weight(1f).testTag("input_new_dealer")
                            )
                            Button(
                                onClick = {
                                    if (newDealerCode.isNotBlank()) {
                                        onAddDealer(newDealerCode)
                                        newDealerCode = ""
                                    }
                                },
                                enabled = newDealerCode.isNotBlank(),
                                modifier = Modifier
                                    .height(52.dp)
                                    .testTag("btn_add_dealer")
                            ) {
                                Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Tambah")
                            }
                        }
                    }
                }

                item {
                    val pairs = state.dealersMaster.chunked(2)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        pairs.forEach { rowDealers ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                rowDealers.forEach { code ->
                                    val count = state.allClaims.count { it.dealer == code }
                                    Surface(
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(14.dp),
                                        color = MaterialTheme.colorScheme.surface,
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(start = 12.dp, end = 4.dp, top = 6.dp, bottom = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = code,
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    text = "$count claim",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            IconButton(
                                                onClick = { onDeleteDealer(code) },
                                                modifier = Modifier.testTag("btn_delete_dealer_$code")
                                            ) {
                                                Icon(
                                                    Icons.Rounded.Close,
                                                    contentDescription = "Hapus Dealer $code",
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                                if (rowDealers.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(72.dp)) }
            }
        }
    }
}

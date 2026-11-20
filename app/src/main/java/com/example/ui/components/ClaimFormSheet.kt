package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.ClaimCalculations
import com.example.data.ClaimConstants
import com.example.data.EnrichedClaim
import com.example.data.local.PartCatalogEntity
import com.example.ui.theme.Amber50
import com.example.ui.theme.Amber600
import com.example.ui.theme.Emerald50
import com.example.ui.theme.Emerald600
import com.example.ui.theme.JetBrainsMonoFontFamily
import java.util.Locale
import kotlin.math.roundToLong

private enum class DateFieldTarget {
    PENGAJUAN, AUDIT, ADJUST
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClaimFormSheet(
    editingClaim: EnrichedClaim?,
    dealersMaster: List<String>,
    partsMaster: List<PartCatalogEntity>,
    onDismiss: () -> Unit,
    onSave: (
        editNo: Int?,
        tglPengajuan: String,
        dealer: String,
        tglAudit: String,
        partNumber: String,
        namaPart: String,
        qty: Int,
        orderAmount: Double,
        tglAdjust: String,
        kendala: String
    ) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val today = remember { ClaimCalculations.getTodayIso() }

    var tglPengajuan by remember(editingClaim) {
        mutableStateOf(editingClaim?.tglPengajuan ?: today)
    }
    var dealer by remember(editingClaim) {
        mutableStateOf(editingClaim?.dealer ?: "")
    }
    var tglAudit by remember(editingClaim) {
        mutableStateOf(editingClaim?.tglAudit ?: today)
    }
    var partNumber by remember(editingClaim) {
        mutableStateOf(editingClaim?.partNumber ?: "")
    }
    var namaPart by remember(editingClaim) {
        mutableStateOf(editingClaim?.namaPart ?: "")
    }
    var qtyText by remember(editingClaim) {
        mutableStateOf((editingClaim?.qty ?: 1).toString())
    }
    var orderAmountText by remember(editingClaim) {
        mutableStateOf(
            editingClaim?.let {
                if (it.orderAmount % 1.0 == 0.0) it.orderAmount.toLong().toString()
                else it.orderAmount.toString()
            } ?: ""
        )
    }
    var tglAdjust by remember(editingClaim) {
        mutableStateOf(editingClaim?.tglAdjust ?: "")
    }

    val initialKendala = editingClaim?.kendala ?: "Sudah Diinput, Menunggu Diadjust"
    val isStandard = ClaimConstants.STANDARD_KENDALA_LIST.contains(initialKendala)
    var selectedKendalaOption by remember(editingClaim) {
        mutableStateOf(if (isStandard) initialKendala else "__CUSTOM__")
    }
    var customKendala by remember(editingClaim) {
        mutableStateOf(if (isStandard) "" else initialKendala)
    }

    var showDealerDropdown by remember { mutableStateOf(false) }
    var showPartDropdown by remember { mutableStateOf(false) }
    var showKendalaDropdown by remember { mutableStateOf(false) }
    var activeDateTarget by remember { mutableStateOf<DateFieldTarget?>(null) }

    val matchedPart = remember(partNumber, partsMaster) {
        val clean = partNumber.trim().uppercase(Locale.US)
        partsMaster.find { it.partNumber.equals(clean, ignoreCase = true) }
    }

    val previewStatus = ClaimCalculations.calculateStatus(tglAdjust)
    val previewLeadTime = ClaimCalculations.calculateLeadTime(tglPengajuan, tglAdjust)

    val filteredDealers = remember(dealer, dealersMaster) {
        val q = dealer.trim().lowercase(Locale.US)
        if (q.isEmpty()) dealersMaster else dealersMaster.filter { it.lowercase(Locale.US).contains(q) }
    }
    val filteredParts = remember(partNumber, partsMaster) {
        val q = partNumber.trim().lowercase(Locale.US)
        if (q.isEmpty()) partsMaster.take(15) else partsMaster.filter {
            it.partNumber.lowercase(Locale.US).contains(q) || it.namaPart.lowercase(Locale.US).contains(q)
        }.take(15)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (editingClaim != null) "Edit Data Claim #${editingClaim.no}" else "Input Pengajuan Claim Baru",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = "Status & Lead Time dihitung otomatis",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Rounded.Close, contentDescription = "Tutup")
                }
            }

            // Live Calculation Banner
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Status: ",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Surface(
                            shape = CircleShape,
                            color = if (previewStatus == ClaimConstants.STATUS_ADJUSTED) Emerald50 else Amber50
                        ) {
                            Text(
                                text = previewStatus,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (previewStatus == ClaimConstants.STATUS_ADJUSTED) Emerald600 else Amber600,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                    Text(
                        text = "Lead Time: $previewLeadTime Hari",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Dates row: Pengajuan & Audit
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                DatePickerField(
                    label = "Tgl Pengajuan *",
                    value = tglPengajuan,
                    onClick = { activeDateTarget = DateFieldTarget.PENGAJUAN },
                    modifier = Modifier.weight(1f).testTag("form_tgl_pengajuan")
                )
                DatePickerField(
                    label = "Tgl Audit *",
                    value = tglAudit,
                    onClick = { activeDateTarget = DateFieldTarget.AUDIT },
                    modifier = Modifier.weight(1f).testTag("form_tgl_audit")
                )
            }

            // Dealer Input with Dropdown
            Box {
                OutlinedTextField(
                    value = dealer,
                    onValueChange = { dealer = it.uppercase() },
                    label = { Text("Kode Dealer *") },
                    placeholder = { Text("Pilih atau ketik (mis. TB BSK)") },
                    trailingIcon = {
                        IconButton(onClick = { showDealerDropdown = true }) {
                            Icon(Icons.Rounded.ArrowDropDown, contentDescription = "Pilih Dealer")
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                    modifier = Modifier.fillMaxWidth().testTag("form_dealer_input")
                )
                DropdownMenu(
                    expanded = showDealerDropdown && filteredDealers.isNotEmpty(),
                    onDismissRequest = { showDealerDropdown = false }
                ) {
                    filteredDealers.take(12).forEach { code ->
                        DropdownMenuItem(
                            text = { Text(code) },
                            onClick = {
                                dealer = code
                                showDealerDropdown = false
                            }
                        )
                    }
                }
            }

            // Part Number with smart catalog autocomplete
            Box {
                Column {
                    OutlinedTextField(
                        value = partNumber,
                        onValueChange = { input ->
                            val upper = input.uppercase()
                            partNumber = upper
                            val found = partsMaster.find { it.partNumber.equals(upper.trim(), ignoreCase = true) }
                            if (found != null) {
                                namaPart = found.namaPart
                                val q = qtyText.toIntOrNull()?.coerceAtLeast(1) ?: 1
                                if (found.unitPrice > 0) {
                                    orderAmountText = (found.unitPrice * q).roundToLong().toString()
                                }
                            }
                        },
                        label = { Text("Part Number *") },
                        placeholder = { Text("Pilih atau ketik part baru...") },
                        trailingIcon = {
                            IconButton(onClick = { showPartDropdown = true }) {
                                Icon(Icons.Rounded.ArrowDropDown, contentDescription = "Pilih Part")
                            }
                        },
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyLarge.copy(fontFamily = JetBrainsMonoFontFamily),
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                        modifier = Modifier.fillMaxWidth().testTag("form_part_number_input")
                    )
                    val hintText = when {
                        matchedPart != null -> "Ditemukan di katalog (Ref: ${ClaimCalculations.formatRupiah(matchedPart.unitPrice)}/unit)"
                        partNumber.isNotBlank() -> "Part Number baru (otomatis disimpan ke katalog)"
                        else -> "Ketik part baru untuk otomatis masuk katalog"
                    }
                    Text(
                        text = hintText,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (matchedPart != null) Emerald600 else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                    )
                }

                DropdownMenu(
                    expanded = showPartDropdown && filteredParts.isNotEmpty(),
                    onDismissRequest = { showPartDropdown = false }
                ) {
                    filteredParts.forEach { p ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(p.partNumber, fontWeight = FontWeight.Bold)
                                    Text(
                                        "${p.namaPart} (${ClaimCalculations.formatRupiah(p.unitPrice)})",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            },
                            onClick = {
                                partNumber = p.partNumber
                                namaPart = p.namaPart
                                val q = qtyText.toIntOrNull()?.coerceAtLeast(1) ?: 1
                                if (p.unitPrice > 0) {
                                    orderAmountText = (p.unitPrice * q).roundToLong().toString()
                                }
                                showPartDropdown = false
                            }
                        )
                    }
                }
            }

            // Nama Part
            OutlinedTextField(
                value = namaPart,
                onValueChange = { namaPart = it.uppercase() },
                label = { Text("Nama Part *") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                modifier = Modifier.fillMaxWidth().testTag("form_nama_part_input")
            )

            // Qty + Order Amount
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = qtyText,
                    onValueChange = { input ->
                        val digits = input.filter { it.isDigit() }
                        qtyText = digits
                        val q = digits.toIntOrNull()?.coerceAtLeast(1) ?: 1
                        if (matchedPart != null && matchedPart.unitPrice > 0) {
                            orderAmountText = (matchedPart.unitPrice * q).roundToLong().toString()
                        }
                    },
                    label = { Text("Qty *") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(0.4f).testTag("form_qty_input")
                )
                OutlinedTextField(
                    value = orderAmountText,
                    onValueChange = { input ->
                        orderAmountText = input.filter { it.isDigit() || it == '.' }
                    },
                    label = { Text("Order Amount (Rp) *") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(0.6f).testTag("form_amount_input")
                )
            }

            // Tanggal Adjust (Optional, with Clear button)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DatePickerField(
                    label = "Tgl Adjust (Opsional)",
                    value = tglAdjust,
                    onClick = { activeDateTarget = DateFieldTarget.ADJUST },
                    modifier = Modifier.weight(1f).testTag("form_tgl_adjust")
                )
                if (tglAdjust.isNotEmpty()) {
                    TextButton(
                        onClick = { tglAdjust = "" },
                        modifier = Modifier.padding(top = 6.dp)
                    ) {
                        Text("Kosongkan")
                    }
                }
            }

            // Kendala Selector
            Box {
                val displayKendala = if (selectedKendalaOption == "__CUSTOM__") {
                    "+ Tambah Kendala Kustom..."
                } else {
                    selectedKendalaOption
                }
                OutlinedTextField(
                    value = displayKendala,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Kendala / Keterangan Audit *") },
                    trailingIcon = {
                        Icon(Icons.Rounded.ArrowDropDown, contentDescription = "Pilih Kendala")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showKendalaDropdown = true }
                        .testTag("form_kendala_selector"),
                    enabled = false
                )
                // Overlay clickable box to ensure dropdown opens cleanly on disabled/readonly field
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable { showKendalaDropdown = true }
                )
                DropdownMenu(
                    expanded = showKendalaDropdown,
                    onDismissRequest = { showKendalaDropdown = false }
                ) {
                    ClaimConstants.STANDARD_KENDALA_LIST.forEach { k ->
                        DropdownMenuItem(
                            text = { Text(k) },
                            onClick = {
                                selectedKendalaOption = k
                                showKendalaDropdown = false
                            }
                        )
                    }
                    DropdownMenuItem(
                        text = { Text("+ Tambah Kendala Kustom...") },
                        onClick = {
                            selectedKendalaOption = "__CUSTOM__"
                            showKendalaDropdown = false
                        }
                    )
                }
            }

            if (selectedKendalaOption == "__CUSTOM__") {
                OutlinedTextField(
                    value = customKendala,
                    onValueChange = { customKendala = it },
                    label = { Text("Keterangan Kendala Baru *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("form_kendala_custom")
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            val finalKendala = if (selectedKendalaOption == "__CUSTOM__") customKendala.trim() else selectedKendalaOption
            val isValid = tglPengajuan.isNotBlank() &&
                dealer.isNotBlank() &&
                tglAudit.isNotBlank() &&
                partNumber.isNotBlank() &&
                namaPart.isNotBlank() &&
                (qtyText.toIntOrNull() ?: 0) >= 1 &&
                finalKendala.isNotBlank()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f).height(48.dp)
                ) {
                    Text("Batal")
                }
                Button(
                    onClick = {
                        onSave(
                            editingClaim?.no,
                            tglPengajuan,
                            dealer,
                            tglAudit,
                            partNumber,
                            namaPart,
                            (qtyText.toIntOrNull() ?: 1).coerceAtLeast(1),
                            orderAmountText.toDoubleOrNull() ?: 0.0,
                            tglAdjust,
                            finalKendala
                        )
                    },
                    enabled = isValid,
                    modifier = Modifier
                        .weight(1.4f)
                        .height(48.dp)
                        .testTag("btn_submit_claim")
                ) {
                    Icon(Icons.Rounded.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Simpan Data")
                }
            }
        }
    }

    // M3 DatePickerDialog
    activeDateTarget?.let { target ->
        val currentIso = when (target) {
            DateFieldTarget.PENGAJUAN -> tglPengajuan
            DateFieldTarget.AUDIT -> tglAudit
            DateFieldTarget.ADJUST -> tglAdjust.ifBlank { today }
        }
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = ClaimCalculations.isoToEpochMillis(currentIso)
        )
        DatePickerDialog(
            onDismissRequest = { activeDateTarget = null },
            confirmButton = {
                TextButton(
                    onClick = {
                        val pickedMillis = datePickerState.selectedDateMillis
                        if (pickedMillis != null) {
                            val pickedIso = ClaimCalculations.epochMillisToIso(pickedMillis)
                            when (target) {
                                DateFieldTarget.PENGAJUAN -> tglPengajuan = pickedIso
                                DateFieldTarget.AUDIT -> tglAudit = pickedIso
                                DateFieldTarget.ADJUST -> {
                                    tglAdjust = pickedIso
                                    selectedKendalaOption = "Claim Complete"
                                }
                            }
                        }
                        activeDateTarget = null
                    }
                ) {
                    Text("Pilih")
                }
            },
            dismissButton = {
                TextButton(onClick = { activeDateTarget = null }) {
                    Text("Batal")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun DatePickerField(
    label: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        OutlinedTextField(
            value = if (value.isBlank()) "Belum diisi" else ClaimCalculations.formatDateDisplay(value),
            onValueChange = {},
            readOnly = true,
            enabled = false,
            label = { Text(label) },
            trailingIcon = {
                Icon(Icons.Rounded.CalendarToday, contentDescription = label, modifier = Modifier.size(18.dp))
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable(onClick = onClick)
        )
    }
}

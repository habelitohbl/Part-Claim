package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CloudDone
import androidx.compose.material.icons.rounded.CloudDownload
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.CloudUpload
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.SyncSettings
import com.example.ui.theme.Emerald50
import com.example.ui.theme.Emerald600
import com.example.ui.theme.Rose50
import com.example.ui.theme.Rose600

private const val APPS_SCRIPT_TEMPLATE = """function doGet() {
  var ss = SpreadsheetApp.getActiveSpreadsheet();
  var sheet = ss.getSheetByName('Data') || ss.getSheets()[0];
  var rows = sheet.getDataRange().getValues();
  var claims = [];
  for (var i = 1; i < rows.length; i++) {
    if (!rows[i][2]) continue;
    claims.push({
      no: Number(rows[i][0]) || i,
      tglPengajuan: String(rows[i][1]),
      dealer: String(rows[i][2]),
      tglAudit: String(rows[i][3]),
      partNumber: String(rows[i][4]),
      namaPart: String(rows[i][5]),
      qty: Number(rows[i][6]) || 1,
      orderAmount: Number(rows[i][7]) || 0,
      tglAdjust: String(rows[i][8] || ''),
      kendala: String(rows[i][11] || 'Verifikasi Part')
    });
  }
  return ContentService.createTextOutput(JSON.stringify({ status: 'ok', claims: claims }))
    .setMimeType(ContentService.MimeType.JSON);
}

function doPost(e) {
  var body = JSON.parse(e.postData.contents);
  var ss = SpreadsheetApp.getActiveSpreadsheet();
  var sheet = ss.getSheetByName('Data') || ss.insertSheet('Data');
  sheet.clear();
  sheet.appendRow(['No','Tanggal Pengajuan Claim','Dealer','Tanggal Audit','Part Number','Nama Part','Qty','Order Amount','Tanggal Adjust','Status','Lead Time','Kendala']);
  body.claims.forEach(function(c) {
    sheet.appendRow([c.no, c.tglPengajuan, c.dealer, c.tglAudit, c.partNumber, c.namaPart, c.qty, c.orderAmount, c.tglAdjust, '', '', c.kendala]);
  });
  return ContentService.createTextOutput(JSON.stringify({ status: 'ok', message: 'Tersimpan ' + body.claims.length + ' claim ke Google Sheets' }))
    .setMimeType(ContentService.MimeType.JSON);
}"""

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnlineSyncSheet(
    isOnline: Boolean,
    isSyncing: Boolean,
    syncSettings: SyncSettings,
    onDismiss: () -> Unit,
    onShareExcel: () -> Unit,
    onPushOnline: (String) -> Unit,
    onPullOnline: (String) -> Unit,
    onSaveUrl: (String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var urlInput by remember(syncSettings.endpointUrl) {
        mutableStateOf(syncSettings.endpointUrl)
    }
    var showScriptHelp by remember { mutableStateOf(false) }
    @Suppress("DEPRECATION")
    val clipboard = LocalClipboardManager.current

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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Koneksi Online & Bagikan Laporan",
                style = MaterialTheme.typography.titleLarge
            )

            // Network status pill
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (isOnline) Emerald50 else Rose50,
                border = BorderStroke(
                    1.dp,
                    if (isOnline) Emerald600.copy(alpha = 0.3f) else Rose600.copy(alpha = 0.3f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = if (isOnline) Icons.Rounded.CloudDone else Icons.Rounded.CloudOff,
                        contentDescription = null,
                        tint = if (isOnline) Emerald600 else Rose600
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isOnline) "Perangkat Online" else "Perangkat Sedang Offline",
                            style = MaterialTheme.typography.titleSmall,
                            color = if (isOnline) Emerald600 else Rose600
                        )
                        Text(
                            text = if (syncSettings.lastSyncTime.isNotBlank()) {
                                "Sinkronisasi terakhir: ${syncSettings.lastSyncTime}"
                            } else {
                                "Database lokal SQLite aktif • Siap berbagi atau sinkronisasi API"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Share Excel Online via WhatsApp / Drive / Email
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Bagikan Laporan Excel (.xlsx) Secara Online",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = "Kirim laporan 2-sheet (Dashboard + Data) langsung ke WhatsApp, Email, Telegram, atau Google Drive.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(
                        onClick = {
                            onShareExcel()
                            onDismiss()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("btn_share_excel_online")
                    ) {
                        Icon(Icons.Rounded.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Bagikan File Excel (.xlsx)")
                    }
                }
            }

            HorizontalDivider()

            // Custom REST / Google Apps Script Endpoint Sync
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Sinkronisasi Cloud Server / Google Sheets API",
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = "Masukkan URL endpoint REST API atau Web App Google Apps Script Anda untuk Push/Pull data tim secara online.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = urlInput,
                    onValueChange = { urlInput = it },
                    label = { Text("URL Endpoint / Google Apps Script") },
                    placeholder = { Text("https://script.google.com/macros/s/.../exec") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_sync_url")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onPushOnline(urlInput) },
                        enabled = isOnline && !isSyncing && urlInput.isNotBlank(),
                        modifier = Modifier.weight(1f).height(48.dp).testTag("btn_push_online")
                    ) {
                        if (isSyncing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Icon(Icons.Rounded.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Push Server")
                        }
                    }

                    OutlinedButton(
                        onClick = { onPullOnline(urlInput) },
                        enabled = isOnline && !isSyncing && urlInput.isNotBlank(),
                        modifier = Modifier.weight(1f).height(48.dp).testTag("btn_pull_online")
                    ) {
                        Icon(Icons.Rounded.CloudDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Pull Server")
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = { showScriptHelp = !showScriptHelp }) {
                        Text(if (showScriptHelp) "Sembunyikan Panduan Script" else "Cara Pakai Google Sheets Gratis")
                    }
                    if (urlInput.trim() != syncSettings.endpointUrl) {
                        TextButton(onClick = { onSaveUrl(urlInput) }) {
                            Text("Simpan URL")
                        }
                    }
                }

                if (showScriptHelp) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "1. Buka Google Sheets → Extensions → Apps Script\n2. Tempel kode berikut lalu Deploy as Web App (Anyone)\n3. Salin URL /exec ke kolom di atas.",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedButton(
                                onClick = {
                                    clipboard.setText(AnnotatedString(APPS_SCRIPT_TEMPLATE))
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Rounded.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Salin Kode Google Apps Script")
                            }
                        }
                    }
                }
            }
        }
    }
}

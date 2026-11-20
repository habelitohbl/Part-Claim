package com.example.data.excel

import android.content.Context
import android.net.Uri
import android.util.Xml
import com.example.data.ClaimCalculations
import com.example.data.ClaimConstants
import com.example.data.EnrichedClaim
import com.example.data.local.ClaimEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParser
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import kotlin.math.roundToLong

class ExcelXlsxManager(private val context: Context) {

    suspend fun exportToUri(
        uri: Uri,
        claims: List<EnrichedClaim>,
        allDealerCodes: List<String>
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openOutputStream(uri)?.use { out ->
                writeXlsx(out, claims, allDealerCodes)
            } ?: return@withContext Result.failure(IllegalStateException("Tidak dapat membuka file tujuan."))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createShareableXlsxFile(
        claims: List<EnrichedClaim>,
        allDealerCodes: List<String>
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
            val fileName = "Audit_Claim_Report_${ClaimCalculations.getTodayIso()}.xlsx"
            val targetFile = File(exportDir, fileName)
            targetFile.outputStream().use { out ->
                writeXlsx(out, claims, allDealerCodes)
            }
            Result.success(targetFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun importFromUri(uri: Uri): Result<List<ClaimEntity>> = withContext(Dispatchers.IO) {
        try {
            val mime = context.contentResolver.getType(uri).orEmpty()
            val nameHint = uri.lastPathSegment.orEmpty().lowercase(Locale.US)
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                ?: return@withContext Result.failure(IllegalStateException("Gagal membaca file."))

            val parsed = if (mime.contains("csv") || nameHint.endsWith(".csv")) {
                parseCsv(bytes.toString(Charsets.UTF_8))
            } else {
                parseXlsx(bytes)
            }

            if (parsed.isEmpty()) {
                Result.failure(IllegalStateException("Format kolom Excel tidak sesuai atau data kosong."))
            } else {
                Result.success(parsed)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun writeXlsx(
        outputStream: OutputStream,
        claims: List<EnrichedClaim>,
        allDealerCodes: List<String>
    ) {
        val totalPengajuan = claims.size
        val belumAdjust = claims.count { it.status == ClaimConstants.STATUS_NOT_ADJUSTED }
        val sudahAdjust = claims.count { it.status == ClaimConstants.STATUS_ADJUSTED }
        val totalPart = claims.sumOf { it.qty }
        val nilaiClaim = claims.sumOf { it.orderAmount }.roundToLong()
        val avgLeadTime = if (totalPengajuan > 0) {
            (claims.sumOf { it.leadTime }.toDouble() / totalPengajuan * 10.0).roundToLong() / 10.0
        } else 0.0

        val dashboardRows = mutableListOf<List<Any?>>()
        dashboardRows.add(listOf("DASHBOARD AUDIT CLAIM - II26-HabelImprovement"))
        dashboardRows.add(listOf("Tanggal Export", ClaimCalculations.getTodayIso()))
        dashboardRows.add(emptyList())
        dashboardRows.add(listOf("RINGKASAN KPI UTAMA", "NILAI"))
        dashboardRows.add(listOf("Total Pengajuan", totalPengajuan))
        dashboardRows.add(listOf("Belum Adjust", belumAdjust))
        dashboardRows.add(listOf("Sudah Adjust", sudahAdjust))
        dashboardRows.add(listOf("Total Part", totalPart))
        dashboardRows.add(listOf("Nilai Claim (Rp)", nilaiClaim))
        dashboardRows.add(listOf("Avg. Lead Time (Hari)", avgLeadTime))
        dashboardRows.add(emptyList())
        dashboardRows.add(listOf("REKAPITULASI PER DEALER", "Jumlah Part", "Nilai Claim (Rp)", "Belum Adjust"))

        val mergedDealers = (allDealerCodes + claims.map { it.dealer }).distinct()
        mergedDealers.forEach { d ->
            val rows = claims.filter { it.dealer == d }
            dashboardRows.add(
                listOf(
                    d,
                    rows.sumOf { it.qty },
                    rows.sumOf { it.orderAmount }.roundToLong(),
                    rows.count { it.status == ClaimConstants.STATUS_NOT_ADJUSTED }
                )
            )
        }

        dashboardRows.add(emptyList())
        dashboardRows.add(listOf("REKAPITULASI KENDALA", "Jumlah Kasus", "Persentase (%)"))
        val kendalaSet = (ClaimConstants.STANDARD_KENDALA_LIST + claims.map { it.kendala }).distinct()
        kendalaSet.forEach { k ->
            val cnt = claims.count { it.kendala == k }
            val pct = if (totalPengajuan > 0) {
                (cnt.toDouble() / totalPengajuan * 1000.0).roundToLong() / 10.0
            } else 0.0
            dashboardRows.add(listOf(k, cnt, pct))
        }

        val dataRows = mutableListOf<List<Any?>>()
        dataRows.add(
            listOf(
                "No",
                "Tanggal Pengajuan Claim",
                "Dealer",
                "Tanggal Audit",
                "Part Number",
                "Nama Part",
                "Qty",
                "Order Amount",
                "Tanggal Adjust",
                "Status",
                "Lead Time",
                "Kendala"
            )
        )
        claims.forEach { r ->
            dataRows.add(
                listOf(
                    r.no,
                    r.tglPengajuan,
                    r.dealer,
                    r.tglAudit,
                    r.partNumber,
                    r.namaPart,
                    r.qty,
                    r.orderAmount,
                    r.tglAdjust,
                    r.status,
                    r.leadTime,
                    r.kendala
                )
            )
        }

        ZipOutputStream(outputStream).use { zip ->
            fun addEntry(name: String, content: String) {
                zip.putNextEntry(ZipEntry(name))
                zip.write(content.toByteArray(Charsets.UTF_8))
                zip.closeEntry()
            }

            addEntry(
                "[Content_Types].xml",
                """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
  <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
  <Default Extension="xml" ContentType="application/xml"/>
  <Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>
  <Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
  <Override PartName="/xl/worksheets/sheet2.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
  <Override PartName="/xl/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml"/>
</Types>"""
            )

            addEntry(
                "_rels/.rels",
                """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/>
</Relationships>"""
            )

            addEntry(
                "xl/workbook.xml",
                """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
  <sheets>
    <sheet name="Dashboard" sheetId="1" r:id="rId1"/>
    <sheet name="Data" sheetId="2" r:id="rId2"/>
  </sheets>
</workbook>"""
            )

            addEntry(
                "xl/_rels/workbook.xml.rels",
                """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/>
  <Relationship Id="rId2" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet2.xml"/>
  <Relationship Id="rId3" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles" Target="styles.xml"/>
</Relationships>"""
            )

            addEntry(
                "xl/styles.xml",
                """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<styleSheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
  <fonts count="2">
    <font><sz val="11"/><name val="Calibri"/></font>
    <font><b/><sz val="11"/><name val="Calibri"/></font>
  </fonts>
  <fills count="2">
    <fill><patternFill patternType="none"/></fill>
    <fill><patternFill patternType="solid"><fgColor rgb="FFEEF2FF"/></patternFill></fill>
  </fills>
  <borders count="1"><border/></borders>
  <cellXfs count="2">
    <xf fontId="0" fillId="0" borderId="0"/>
    <xf fontId="1" fillId="1" borderId="0" applyFont="1" applyFill="1"/>
  </cellXfs>
</styleSheet>"""
            )

            addEntry("xl/worksheets/sheet1.xml", buildSheetXml(dashboardRows, listOf(42, 18, 22, 16), headerStyleRows = setOf(0, 3, 11)))
            addEntry(
                "xl/worksheets/sheet2.xml",
                buildSheetXml(
                    dataRows,
                    listOf(6, 22, 12, 15, 18, 34, 8, 16, 15, 15, 12, 42),
                    headerStyleRows = setOf(0)
                )
            )
        }
    }

    private fun buildSheetXml(
        rows: List<List<Any?>>,
        colWidths: List<Int>,
        headerStyleRows: Set<Int>
    ): String {
        val sb = StringBuilder()
        sb.append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>""")
        sb.append("""<worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">""")
        sb.append("<cols>")
        colWidths.forEachIndexed { idx, w ->
            val colIdx = idx + 1
            sb.append("""<col min="$colIdx" max="$colIdx" width="$w" customWidth="1"/>""")
        }
        sb.append("</cols><sheetData>")
        rows.forEachIndexed { rIdx, row ->
            val rowNum = rIdx + 1
            sb.append("""<row r="$rowNum">""")
            val styleAttr = if (rIdx in headerStyleRows) """ s="1"""" else ""
            row.forEachIndexed { cIdx, cellVal ->
                if (cellVal != null && cellVal.toString().isNotEmpty()) {
                    val colRef = colIndexToLetters(cIdx) + rowNum
                    when (cellVal) {
                        is Number -> {
                            sb.append("""<c r="$colRef"$styleAttr><v>$cellVal</v></c>""")
                        }
                        else -> {
                            val escaped = escapeXml(cellVal.toString())
                            sb.append("""<c r="$colRef" t="inlineStr"$styleAttr><is><t>$escaped</t></is></c>""")
                        }
                    }
                }
            }
            sb.append("</row>")
        }
        sb.append("</sheetData></worksheet>")
        return sb.toString()
    }

    private fun colIndexToLetters(index: Int): String {
        var i = index
        val sb = StringBuilder()
        while (i >= 0) {
            sb.insert(0, ('A' + (i % 26)))
            i = (i / 26) - 1
        }
        return sb.toString()
    }

    private fun escapeXml(raw: String): String {
        return raw
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
    }

    private fun parseXlsx(bytes: ByteArray): List<ClaimEntity> {
        val entries = HashMap<String, ByteArray>()
        ZipInputStream(bytes.inputStream()).use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
                if (!entry.isDirectory) {
                    val buf = ByteArrayOutputStream()
                    zis.copyTo(buf)
                    entries[entry.name] = buf.toByteArray()
                }
                zis.closeEntry()
                entry = zis.nextEntry
            }
        }

        val sharedStrings = entries["xl/sharedStrings.xml"]?.let { parseSharedStrings(it) } ?: emptyList()

        // Find target sheet ("Data" if present, else first sheet)
        val sheetNamesMap = entries["xl/workbook.xml"]?.let { parseWorkbookSheetIds(it) } ?: emptyMap()
        val relsMap = entries["xl/_rels/workbook.xml.rels"]?.let { parseWorkbookRels(it) } ?: emptyMap()

        val targetRid = sheetNamesMap["Data"] ?: sheetNamesMap.values.firstOrNull()
        val targetPath = targetRid?.let { relsMap[it] }?.let { relTarget ->
            if (relTarget.startsWith("/")) relTarget.drop(1)
            else if (relTarget.startsWith("xl/")) relTarget
            else "xl/$relTarget"
        } ?: entries.keys.filter { it.startsWith("xl/worksheets/sheet") && it.endsWith(".xml") }.sorted().firstOrNull()
            ?: return emptyList()

        val sheetBytes = entries[targetPath] ?: return emptyList()
        val tableRows = parseWorksheetRows(sheetBytes, sharedStrings)
        return mapRowsToClaims(tableRows)
    }

    private fun parseSharedStrings(bytes: ByteArray): List<String> {
        val result = mutableListOf<String>()
        val parser = Xml.newPullParser()
        parser.setInput(bytes.inputStream(), "UTF-8")
        var event = parser.eventType
        var inSi = false
        var inT = false
        val current = StringBuilder()
        while (event != XmlPullParser.END_DOCUMENT) {
            when (event) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "si" -> {
                            inSi = true
                            current.setLength(0)
                        }
                        "t" -> if (inSi) inT = true
                    }
                }
                XmlPullParser.TEXT -> {
                    if (inSi && inT) current.append(parser.text)
                }
                XmlPullParser.END_TAG -> {
                    when (parser.name) {
                        "t" -> inT = false
                        "si" -> {
                            result.add(current.toString())
                            inSi = false
                        }
                    }
                }
            }
            event = parser.next()
        }
        return result
    }

    private fun parseWorkbookSheetIds(bytes: ByteArray): Map<String, String> {
        val map = LinkedHashMap<String, String>()
        val parser = Xml.newPullParser()
        parser.setInput(bytes.inputStream(), "UTF-8")
        var event = parser.eventType
        while (event != XmlPullParser.END_DOCUMENT) {
            if (event == XmlPullParser.START_TAG && parser.name == "sheet") {
                val name = parser.getAttributeValue(null, "name")
                var rId: String? = null
                for (i in 0 until parser.attributeCount) {
                    if (parser.getAttributeName(i).endsWith("id") && parser.getAttributeName(i) != "sheetId") {
                        rId = parser.getAttributeValue(i)
                    }
                }
                if (!name.isNullOrBlank() && !rId.isNullOrBlank()) {
                    map[name] = rId
                }
            }
            event = parser.next()
        }
        return map
    }

    private fun parseWorkbookRels(bytes: ByteArray): Map<String, String> {
        val map = HashMap<String, String>()
        val parser = Xml.newPullParser()
        parser.setInput(bytes.inputStream(), "UTF-8")
        var event = parser.eventType
        while (event != XmlPullParser.END_DOCUMENT) {
            if (event == XmlPullParser.START_TAG && parser.name == "Relationship") {
                val id = parser.getAttributeValue(null, "Id")
                val target = parser.getAttributeValue(null, "Target")
                if (!id.isNullOrBlank() && !target.isNullOrBlank()) {
                    map[id] = target
                }
            }
            event = parser.next()
        }
        return map
    }

    private fun parseWorksheetRows(bytes: ByteArray, sharedStrings: List<String>): List<List<String>> {
        val rows = mutableListOf<List<String>>()
        val parser = Xml.newPullParser()
        parser.setInput(bytes.inputStream(), "UTF-8")
        var event = parser.eventType

        var currentRow = mutableListOf<String>()
        var currentColIdx = -1
        var cellType: String? = null
        var inValue = false
        var inInlineText = false
        val valueBuilder = StringBuilder()

        while (event != XmlPullParser.END_DOCUMENT) {
            when (event) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "row" -> currentRow = mutableListOf()
                        "c" -> {
                            val ref = parser.getAttributeValue(null, "r")
                            currentColIdx = if (!ref.isNullOrBlank()) colRefToIndex(ref) else currentRow.size
                            cellType = parser.getAttributeValue(null, "t")
                            valueBuilder.setLength(0)
                        }
                        "v" -> inValue = true
                        "t" -> if (cellType == "inlineStr") inInlineText = true
                    }
                }
                XmlPullParser.TEXT -> {
                    if (inValue || inInlineText) valueBuilder.append(parser.text)
                }
                XmlPullParser.END_TAG -> {
                    when (parser.name) {
                        "v" -> inValue = false
                        "t" -> inInlineText = false
                        "c" -> {
                            val raw = valueBuilder.toString()
                            val decoded = when (cellType) {
                                "s" -> raw.toIntOrNull()?.let { sharedStrings.getOrNull(it) }.orEmpty()
                                else -> raw
                            }
                            while (currentRow.size <= currentColIdx) currentRow.add("")
                            if (currentColIdx >= 0) currentRow[currentColIdx] = decoded
                        }
                        "row" -> {
                            if (currentRow.any { it.isNotBlank() }) {
                                rows.add(currentRow)
                            }
                        }
                    }
                }
            }
            event = parser.next()
        }
        return rows
    }

    private fun colRefToIndex(ref: String): Int {
        var col = 0
        for (ch in ref) {
            if (ch.isLetter()) {
                col = col * 26 + (ch.uppercaseChar() - 'A' + 1)
            } else {
                break
            }
        }
        return col - 1
    }

    private fun parseCsv(text: String): List<ClaimEntity> {
        val lines = text.lines().filter { it.isNotBlank() }
        val rows = lines.map { line ->
            val delimiter = if (line.count { it == ';' } > line.count { it == ',' }) ';' else ','
            line.split(delimiter).map { it.trim().removeSurrounding("\"") }
        }
        return mapRowsToClaims(rows)
    }

    private fun mapRowsToClaims(rows: List<List<String>>): List<ClaimEntity> {
        if (rows.size < 2) return emptyList()
        // Find header row
        val headerIdx = rows.indexOfFirst { row ->
            val joined = row.joinToString(" ").lowercase(Locale.US)
            joined.contains("dealer") && (joined.contains("part number") || joined.contains("part"))
        }.takeIf { it >= 0 } ?: 0

        val headers = rows[headerIdx].map { it.trim().lowercase(Locale.US) }
        fun idxOf(vararg candidates: String): Int {
            return headers.indexOfFirst { h -> candidates.any { c -> h == c || h.contains(c) } }
        }

        val noIdx = idxOf("no")
        val tglPengajuanIdx = idxOf("tanggal pengajuan claim", "tgl pengajuan")
        val dealerIdx = idxOf("dealer")
        val tglAuditIdx = idxOf("tanggal audit", "tgl audit")
        val partNumberIdx = idxOf("part number")
        val namaPartIdx = idxOf("nama part")
        val qtyIdx = idxOf("qty")
        val orderAmountIdx = idxOf("order amount", "amount", "nilai")
        val tglAdjustIdx = idxOf("tanggal adjust", "tgl adjust")
        val kendalaIdx = idxOf("kendala")

        if (dealerIdx < 0 || partNumberIdx < 0) return emptyList()

        val result = mutableListOf<ClaimEntity>()
        for (i in (headerIdx + 1) until rows.size) {
            val row = rows[i]
            fun cell(idx: Int): String = if (idx in row.indices) row[idx].trim() else ""
            val dealer = cell(dealerIdx).uppercase(Locale.US)
            val partNumber = cell(partNumberIdx).uppercase(Locale.US)
            if (dealer.isEmpty() && partNumber.isEmpty()) continue

            val no = cell(noIdx).toDoubleOrNull()?.toInt() ?: (result.size + 1)
            val tglPengajuan = parseDateCell(cell(tglPengajuanIdx))
            val tglAudit = parseDateCell(cell(tglAuditIdx))
            val namaPart = cell(namaPartIdx).uppercase(Locale.US)
            val qty = (cell(qtyIdx).toDoubleOrNull()?.toInt() ?: 1).coerceAtLeast(1)
            val orderAmount = cell(orderAmountIdx).replace(",", "").toDoubleOrNull() ?: 0.0
            val tglAdjust = parseDateCell(cell(tglAdjustIdx))
            val kendala = cell(kendalaIdx).ifBlank { "Verifikasi Part" }

            result.add(
                ClaimEntity(
                    no = no,
                    tglPengajuan = tglPengajuan.ifBlank { ClaimCalculations.getTodayIso() },
                    dealer = dealer,
                    tglAudit = tglAudit.ifBlank { ClaimCalculations.getTodayIso() },
                    partNumber = partNumber,
                    namaPart = namaPart,
                    qty = qty,
                    orderAmount = orderAmount,
                    tglAdjust = tglAdjust,
                    kendala = kendala
                )
            )
        }
        return result
    }

    private fun parseDateCell(raw: String): String {
        val trimmed = raw.trim()
        if (trimmed.isEmpty()) return ""
        if (Regex("^\\d{4}-\\d{2}-\\d{2}$").matches(trimmed)) return trimmed

        // Excel serial date
        val serial = trimmed.toDoubleOrNull()
        if (serial != null && serial > 20000 && serial < 90000) {
            val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            cal.set(1899, Calendar.DECEMBER, 30, 0, 0, 0)
            cal.set(Calendar.MILLISECOND, 0)
            cal.add(Calendar.DAY_OF_YEAR, serial.toInt())
            val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            return fmt.format(cal.time)
        }

        val candidates = listOf("dd/MM/yyyy", "dd-MM-yyyy", "MM/dd/yyyy", "yyyy/MM/dd")
        for (pattern in candidates) {
            try {
                val parser = SimpleDateFormat(pattern, Locale.US).apply { isLenient = false }
                val parsed = parser.parse(trimmed)
                if (parsed != null) {
                    return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(parsed)
                }
            } catch (_: Exception) {
            }
        }
        return ""
    }
}

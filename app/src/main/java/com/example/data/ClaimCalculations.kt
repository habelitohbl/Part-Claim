package com.example.data

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.roundToLong

object ClaimConstants {
    val STANDARD_KENDALA_LIST = listOf(
        "Claim Complete",
        "Verifikasi Part",
        "Sudah Diinput, Menunggu Diadjust",
        "Dokumen Kurang",
        "Form Claim Tidak Lengkap",
        "Salah kirim form claim, petugas claim tidak follow up ke dealer"
    )

    const val STATUS_ADJUSTED = "Adjusted"
    const val STATUS_NOT_ADJUSTED = "Not Adjusted"
}

data class EnrichedClaim(
    val no: Int,
    val tglPengajuan: String,
    val dealer: String,
    val tglAudit: String,
    val partNumber: String,
    val namaPart: String,
    val qty: Int,
    val orderAmount: Double,
    val tglAdjust: String,
    val kendala: String,
    val status: String,
    val leadTime: Int
)

object ClaimCalculations {
    private val isoFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
        isLenient = false
    }
    private val displayFormat = SimpleDateFormat("dd/MM/yyyy", Locale.US)

    fun getTodayIso(): String {
        return synchronized(isoFormat) {
            isoFormat.format(Date())
        }
    }

    fun calculateStatus(tglAdjust: String?): String {
        return if (!tglAdjust.isNullOrBlank()) {
            ClaimConstants.STATUS_ADJUSTED
        } else {
            ClaimConstants.STATUS_NOT_ADJUSTED
        }
    }

    fun calculateLeadTime(tglPengajuan: String?, tglAdjust: String?): Int {
        if (tglPengajuan.isNullOrBlank()) return 0
        return try {
            val start: Date?
            val end: Date?
            val endStr = if (!tglAdjust.isNullOrBlank()) tglAdjust.trim() else getTodayIso()
            synchronized(isoFormat) {
                start = isoFormat.parse(tglPengajuan.trim())
                end = isoFormat.parse(endStr)
            }
            if (start == null || end == null) return 0
            val diffMs = end.time - start.time
            TimeUnit.MILLISECONDS.toDays(diffMs).toInt()
        } catch (_: Exception) {
            0
        }
    }

    fun formatRupiah(amount: Double): String {
        val rounded = amount.roundToLong()
        val idLocale = Locale.Builder().setLanguage("id").setRegion("ID").build()
        val nf = NumberFormat.getNumberInstance(idLocale)
        return "Rp ${nf.format(rounded)}"
    }

    fun formatDateDisplay(isoStr: String?): String {
        if (isoStr.isNullOrBlank()) return "-"
        return try {
            val parsed = synchronized(isoFormat) { isoFormat.parse(isoStr.trim()) }
            if (parsed != null) {
                synchronized(displayFormat) { displayFormat.format(parsed) }
            } else {
                isoStr
            }
        } catch (_: Exception) {
            isoStr
        }
    }

    fun epochMillisToIso(millis: Long): String {
        return synchronized(isoFormat) { isoFormat.format(Date(millis)) }
    }

    fun isoToEpochMillis(isoStr: String?): Long? {
        if (isoStr.isNullOrBlank()) return null
        return try {
            synchronized(isoFormat) { isoFormat.parse(isoStr.trim())?.time }
        } catch (_: Exception) {
            null
        }
    }
}

package com.example.data.repository

import com.example.data.SeedData
import com.example.data.local.AuditClaimDao
import com.example.data.local.ClaimEntity
import com.example.data.local.DealerEntity
import com.example.data.local.PartCatalogEntity
import com.example.data.local.SyncPreferences
import com.example.data.local.SyncSettings
import com.example.data.remote.OnlineSyncService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToLong

class AuditClaimRepository(
    private val dao: AuditClaimDao,
    private val syncPrefs: SyncPreferences,
    private val onlineSyncService: OnlineSyncService
) {
    val claimsFlow: Flow<List<ClaimEntity>> = dao.observeClaims()
    val dealersFlow: Flow<List<DealerEntity>> = dao.observeDealers()
    val partsFlow: Flow<List<PartCatalogEntity>> = dao.observeParts()
    val syncSettingsFlow: Flow<SyncSettings> = syncPrefs.settingsFlow
    val isOnlineFlow: Flow<Boolean> = onlineSyncService.observeNetworkOnline()

    suspend fun ensureSeeded() {
        val settings = syncPrefs.settingsFlow.first()
        if (!settings.initialized && dao.getClaimsCount() == 0) {
            val claims = SeedData.INITIAL_CLAIMS
            val dealers = SeedData.buildDefaultDealers()
            val parts = SeedData.buildDefaultPartsCatalog(claims)
            dao.replaceAllData(claims, dealers, parts)
            syncPrefs.markInitialized()
        }
    }

    suspend fun saveClaim(
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
    ): Int {
        val cleanDealer = dealer.trim().uppercase(Locale.US)
        val cleanPn = partNumber.trim().uppercase(Locale.US)
        val cleanName = namaPart.trim().uppercase(Locale.US)
        val safeQty = qty.coerceAtLeast(1)
        val safeAmount = orderAmount.coerceAtLeast(0.0)

        if (cleanDealer.isNotEmpty()) {
            dao.insertDealer(DealerEntity(cleanDealer))
        }

        if (cleanPn.isNotEmpty()) {
            val existingParts = dao.getPartsOnce()
            if (existingParts.none { it.partNumber == cleanPn }) {
                val unitPrice = (safeAmount / safeQty * 100.0).roundToLong() / 100.0
                dao.upsertPart(PartCatalogEntity(cleanPn, cleanName, unitPrice))
            }
        }

        val targetNo = editNo ?: (dao.getMaxClaimNo() + 1)
        dao.upsertClaim(
            ClaimEntity(
                no = targetNo,
                tglPengajuan = tglPengajuan,
                dealer = cleanDealer,
                tglAudit = tglAudit,
                partNumber = cleanPn,
                namaPart = cleanName,
                qty = safeQty,
                orderAmount = safeAmount,
                tglAdjust = tglAdjust,
                kendala = kendala.trim()
            )
        )
        return targetNo
    }

    suspend fun quickAdjustClaim(no: Int, todayIso: String): Boolean {
        val existing = dao.getClaimsOnce().find { it.no == no } ?: return false
        dao.upsertClaim(
            existing.copy(
                tglAdjust = todayIso,
                kendala = "Claim Complete"
            )
        )
        return true
    }

    suspend fun deleteClaim(no: Int) {
        dao.deleteClaimByNo(no)
    }

    suspend fun saveMasterPart(partNumber: String, namaPart: String, unitPrice: Double) {
        val cleanPn = partNumber.trim().uppercase(Locale.US)
        val cleanName = namaPart.trim().uppercase(Locale.US)
        if (cleanPn.isBlank()) return
        dao.upsertPart(PartCatalogEntity(cleanPn, cleanName, unitPrice.coerceAtLeast(0.0)))
    }

    suspend fun deleteMasterPart(partNumber: String) {
        dao.deletePart(partNumber)
    }

    suspend fun addMasterDealer(code: String): Boolean {
        val cleanCode = code.trim().uppercase(Locale.US)
        if (cleanCode.isBlank()) return false
        val exists = dao.getDealersOnce().any { it.code == cleanCode }
        if (exists) return false
        dao.insertDealer(DealerEntity(cleanCode))
        return true
    }

    suspend fun deleteMasterDealer(code: String) {
        dao.deleteDealer(code)
    }

    suspend fun importClaims(imported: List<ClaimEntity>) {
        if (imported.isEmpty()) return
        val existingDealers = dao.getDealersOnce().map { it.code }.toMutableSet()
        imported.forEach { if (it.dealer.isNotBlank()) existingDealers.add(it.dealer) }

        val existingParts = dao.getPartsOnce().associateBy { it.partNumber }.toMutableMap()
        val importedParts = SeedData.buildDefaultPartsCatalog(imported)
        importedParts.forEach { part ->
            if (!existingParts.containsKey(part.partNumber)) {
                existingParts[part.partNumber] = part
            }
        }

        dao.replaceAllData(
            claims = imported,
            dealers = existingDealers.sorted().map { DealerEntity(it) },
            parts = existingParts.values.sortedBy { it.partNumber }
        )
    }

    suspend fun resetToInitialData() {
        val claims = SeedData.INITIAL_CLAIMS
        val dealers = SeedData.buildDefaultDealers()
        val parts = SeedData.buildDefaultPartsCatalog(claims)
        dao.replaceAllData(claims, dealers, parts)
        syncPrefs.markInitialized()
    }

    suspend fun saveSyncEndpointUrl(url: String) {
        syncPrefs.setEndpointUrl(url)
    }

    suspend fun pushOnline(endpointUrl: String): Result<String> {
        val claims = dao.getClaimsOnce()
        val dealers = dao.getDealersOnce()
        val parts = dao.getPartsOnce()
        val res = onlineSyncService.pushData(endpointUrl, claims, dealers, parts)
        if (res.isSuccess) {
            val ts = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US).format(Date())
            syncPrefs.setLastSyncTime("Push $ts")
        }
        return res
    }

    suspend fun pullOnline(endpointUrl: String): Result<Int> {
        val res = onlineSyncService.pullData(endpointUrl)
        return res.mapCatching { body ->
            val pulledClaims = body.claims.orEmpty()
            val mergedDealers = (
                (body.dealers ?: emptyList()) +
                    pulledClaims.map { it.dealer } +
                    SeedData.INITIAL_DEALERS
                ).filter { it.isNotBlank() }.distinct().sorted().map { DealerEntity(it) }

            val pulledParts = if (!body.parts.isNullOrEmpty()) {
                body.parts
            } else {
                SeedData.buildDefaultPartsCatalog(pulledClaims)
            }

            dao.replaceAllData(pulledClaims, mergedDealers, pulledParts)
            val ts = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US).format(Date())
            syncPrefs.setLastSyncTime("Pull $ts")
            pulledClaims.size
        }
    }
}

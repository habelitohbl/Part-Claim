package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.ClaimCalculations
import com.example.data.EnrichedClaim
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
@Entity(tableName = "claims")
data class ClaimEntity(
    @PrimaryKey val no: Int,
    val tglPengajuan: String,
    val dealer: String,
    val tglAudit: String,
    val partNumber: String,
    val namaPart: String,
    val qty: Int,
    val orderAmount: Double,
    val tglAdjust: String,
    val kendala: String
) {
    fun toEnriched(): EnrichedClaim = EnrichedClaim(
        no = no,
        tglPengajuan = tglPengajuan,
        dealer = dealer,
        tglAudit = tglAudit,
        partNumber = partNumber,
        namaPart = namaPart,
        qty = qty,
        orderAmount = orderAmount,
        tglAdjust = tglAdjust,
        kendala = kendala,
        status = ClaimCalculations.calculateStatus(tglAdjust),
        leadTime = ClaimCalculations.calculateLeadTime(tglPengajuan, tglAdjust)
    )
}

@JsonClass(generateAdapter = true)
@Entity(tableName = "dealers")
data class DealerEntity(
    @PrimaryKey val code: String
)

@JsonClass(generateAdapter = true)
@Entity(tableName = "parts_catalog")
data class PartCatalogEntity(
    @PrimaryKey val partNumber: String,
    val namaPart: String,
    val unitPrice: Double
)

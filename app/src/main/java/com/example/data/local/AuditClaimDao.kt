package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface AuditClaimDao {
    @Query("SELECT * FROM claims ORDER BY no ASC")
    fun observeClaims(): Flow<List<ClaimEntity>>

    @Query("SELECT * FROM claims ORDER BY no ASC")
    suspend fun getClaimsOnce(): List<ClaimEntity>

    @Query("SELECT COUNT(*) FROM claims")
    suspend fun getClaimsCount(): Int

    @Query("SELECT COALESCE(MAX(no), 0) FROM claims")
    suspend fun getMaxClaimNo(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertClaim(claim: ClaimEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertClaims(claims: List<ClaimEntity>)

    @Query("DELETE FROM claims WHERE no = :no")
    suspend fun deleteClaimByNo(no: Int)

    @Query("DELETE FROM claims")
    suspend fun clearClaims()

    @Query("SELECT * FROM dealers ORDER BY code ASC")
    fun observeDealers(): Flow<List<DealerEntity>>

    @Query("SELECT * FROM dealers ORDER BY code ASC")
    suspend fun getDealersOnce(): List<DealerEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDealer(dealer: DealerEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDealers(dealers: List<DealerEntity>)

    @Query("DELETE FROM dealers WHERE code = :code")
    suspend fun deleteDealer(code: String)

    @Query("DELETE FROM dealers")
    suspend fun clearDealers()

    @Query("SELECT * FROM parts_catalog ORDER BY partNumber ASC")
    fun observeParts(): Flow<List<PartCatalogEntity>>

    @Query("SELECT * FROM parts_catalog ORDER BY partNumber ASC")
    suspend fun getPartsOnce(): List<PartCatalogEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPart(part: PartCatalogEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertParts(parts: List<PartCatalogEntity>)

    @Query("DELETE FROM parts_catalog WHERE partNumber = :partNumber")
    suspend fun deletePart(partNumber: String)

    @Query("DELETE FROM parts_catalog")
    suspend fun clearParts()

    @Transaction
    suspend fun replaceAllData(
        claims: List<ClaimEntity>,
        dealers: List<DealerEntity>,
        parts: List<PartCatalogEntity>
    ) {
        clearClaims()
        clearDealers()
        clearParts()
        upsertClaims(claims)
        upsertDealers(dealers)
        upsertParts(parts)
    }
}

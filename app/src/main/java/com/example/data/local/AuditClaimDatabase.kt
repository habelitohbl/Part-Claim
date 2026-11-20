package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [ClaimEntity::class, DealerEntity::class, PartCatalogEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AuditClaimDatabase : RoomDatabase() {
    abstract fun auditClaimDao(): AuditClaimDao

    companion object {
        @Volatile
        private var INSTANCE: AuditClaimDatabase? = null

        fun getInstance(context: Context): AuditClaimDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AuditClaimDatabase::class.java,
                    "audit_claim_db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}

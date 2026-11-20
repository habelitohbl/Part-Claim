package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.syncDataStore: DataStore<Preferences> by preferencesDataStore(name = "audit_claim_sync_prefs")

data class SyncSettings(
    val endpointUrl: String = "",
    val lastSyncTime: String = "",
    val initialized: Boolean = false
)

class SyncPreferences(private val context: Context) {
    private object Keys {
        val ENDPOINT_URL = stringPreferencesKey("sync_endpoint_url")
        val LAST_SYNC_TIME = stringPreferencesKey("last_sync_time")
        val INITIALIZED = booleanPreferencesKey("seed_initialized")
    }

    val settingsFlow: Flow<SyncSettings> = context.syncDataStore.data.map { prefs ->
        SyncSettings(
            endpointUrl = prefs[Keys.ENDPOINT_URL] ?: "",
            lastSyncTime = prefs[Keys.LAST_SYNC_TIME] ?: "",
            initialized = prefs[Keys.INITIALIZED] ?: false
        )
    }

    suspend fun setEndpointUrl(url: String) {
        context.syncDataStore.edit { prefs ->
            prefs[Keys.ENDPOINT_URL] = url.trim()
        }
    }

    suspend fun setLastSyncTime(timestamp: String) {
        context.syncDataStore.edit { prefs ->
            prefs[Keys.LAST_SYNC_TIME] = timestamp
        }
    }

    suspend fun markInitialized() {
        context.syncDataStore.edit { prefs ->
            prefs[Keys.INITIALIZED] = true
        }
    }
}

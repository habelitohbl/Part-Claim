package com.example.data.remote

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import com.example.data.local.ClaimEntity
import com.example.data.local.DealerEntity
import com.example.data.local.PartCatalogEntity
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class SyncPayload(
    val action: String = "sync_push",
    val project: String = "II26-HabelImprovement",
    val claims: List<ClaimEntity> = emptyList(),
    val dealers: List<String> = emptyList(),
    val parts: List<PartCatalogEntity> = emptyList()
)

@JsonClass(generateAdapter = true)
data class SyncResponse(
    val status: String? = null,
    val message: String? = null,
    val claims: List<ClaimEntity>? = null,
    val dealers: List<String>? = null,
    val parts: List<PartCatalogEntity>? = null
)

class OnlineSyncService(private val context: Context) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(25, TimeUnit.SECONDS)
        .writeTimeout(25, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    private val moshi = Moshi.Builder().build()
    private val payloadAdapter = moshi.adapter(SyncPayload::class.java)
    private val responseAdapter = moshi.adapter(SyncResponse::class.java)
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    fun observeNetworkOnline(): Flow<Boolean> = callbackFlow {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        fun checkOnline(): Boolean {
            val active = cm.activeNetwork ?: return false
            val caps = cm.getNetworkCapabilities(active) ?: return false
            return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        }

        trySend(checkOnline())

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(true)
            }

            override fun onLost(network: Network) {
                trySend(checkOnline())
            }

            override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                val valid = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                trySend(valid)
            }
        }

        cm.registerDefaultNetworkCallback(callback)
        awaitClose { cm.unregisterNetworkCallback(callback) }
    }.distinctUntilChanged()

    suspend fun pushData(
        endpointUrl: String,
        claims: List<ClaimEntity>,
        dealers: List<DealerEntity>,
        parts: List<PartCatalogEntity>
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val trimmed = endpointUrl.trim()
            require(trimmed.startsWith("https://") || trimmed.startsWith("http://")) {
                "URL server harus diawali https:// atau http://"
            }
            val payload = SyncPayload(
                action = "sync_push",
                claims = claims,
                dealers = dealers.map { it.code },
                parts = parts
            )
            val bodyJson = payloadAdapter.toJson(payload)
            val request = Request.Builder()
                .url(trimmed)
                .post(bodyJson.toRequestBody(jsonMediaType))
                .build()

            client.newCall(request).execute().use { response ->
                val respBody = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    return@withContext Result.failure(
                        IllegalStateException("HTTP ${response.code}: ${respBody.take(160)}")
                    )
                }
                val parsed = runCatching { responseAdapter.fromJson(respBody) }.getOrNull()
                Result.success(parsed?.message ?: "Push ${claims.size} data claim ke server berhasil.")
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun pullData(endpointUrl: String): Result<SyncResponse> = withContext(Dispatchers.IO) {
        try {
            val trimmed = endpointUrl.trim()
            require(trimmed.startsWith("https://") || trimmed.startsWith("http://")) {
                "URL server harus diawali https:// atau http://"
            }
            val request = Request.Builder()
                .url(trimmed)
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                val respBody = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    return@withContext Result.failure(
                        IllegalStateException("HTTP ${response.code}: ${respBody.take(160)}")
                    )
                }
                val parsed = responseAdapter.fromJson(respBody)
                    ?: return@withContext Result.failure(IllegalStateException("Respons JSON dari server tidak valid."))
                if (parsed.claims.isNullOrEmpty()) {
                    return@withContext Result.failure(IllegalStateException("Server tidak mengembalikan data claim."))
                }
                Result.success(parsed)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

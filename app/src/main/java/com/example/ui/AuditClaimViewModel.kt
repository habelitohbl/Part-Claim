package com.example.ui

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.ClaimCalculations
import com.example.data.ClaimConstants
import com.example.data.EnrichedClaim
import com.example.data.excel.ExcelXlsxManager
import com.example.data.local.PartCatalogEntity
import com.example.data.local.SyncSettings
import com.example.data.repository.AuditClaimRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.util.Locale

enum class AppTab(val label: String) {
    DASHBOARD("Dashboard"),
    CLAIMS("Data Claim"),
    MASTER("Master")
}

enum class DealerRecapMode {
    ACTIVE_ONLY,
    ALL_DEALERS
}

data class DealerStat(
    val dealer: String,
    val count: Int,
    val partQty: Int,
    val amount: Double,
    val unadjusted: Int
)

data class KendalaStat(
    val kendala: String,
    val count: Int,
    val percentage: Double
)

data class DashboardKpis(
    val totalPengajuan: Int = 0,
    val belumAdjust: Int = 0,
    val sudahAdjust: Int = 0,
    val belumPct: Double = 0.0,
    val sudahPct: Double = 0.0,
    val totalPart: Int = 0,
    val nilaiClaim: Double = 0.0,
    val avgLeadTime: Double = 0.0
)

data class ClaimFilterState(
    val searchQuery: String = "",
    val dealerFilter: String = "",
    val statusFilter: String = "",
    val kendalaFilter: String = ""
)

data class AuditClaimUiState(
    val isLoading: Boolean = true,
    val currentTab: AppTab = AppTab.DASHBOARD,
    val allClaims: List<EnrichedClaim> = emptyList(),
    val filteredClaims: List<EnrichedClaim> = emptyList(),
    val filteredSubtotalAmount: Double = 0.0,
    val filteredSubtotalQty: Int = 0,
    val dealersMaster: List<String> = emptyList(),
    val partsMaster: List<PartCatalogEntity> = emptyList(),
    val filteredPartsMaster: List<PartCatalogEntity> = emptyList(),
    val masterPartSearch: String = "",
    val filterState: ClaimFilterState = ClaimFilterState(),
    val dealerRecapMode: DealerRecapMode = DealerRecapMode.ACTIVE_ONLY,
    val kpis: DashboardKpis = DashboardKpis(),
    val activeDealerStats: List<DealerStat> = emptyList(),
    val displayDealerStats: List<DealerStat> = emptyList(),
    val kendalaStats: List<KendalaStat> = emptyList(),
    val allKendalaOptions: List<String> = ClaimConstants.STANDARD_KENDALA_LIST,
    val isOnline: Boolean = false,
    val isSyncing: Boolean = false,
    val syncSettings: SyncSettings = SyncSettings()
)

sealed interface UiEvent {
    data class Toast(val message: String, val isError: Boolean = false) : UiEvent
    data class ShareFile(val file: File) : UiEvent
}

private data class LocalUiControls(
    val currentTab: AppTab = AppTab.DASHBOARD,
    val filterState: ClaimFilterState = ClaimFilterState(),
    val dealerRecapMode: DealerRecapMode = DealerRecapMode.ACTIVE_ONLY,
    val masterPartSearch: String = "",
    val isSyncing: Boolean = false
)

class AuditClaimViewModel(
    private val repository: AuditClaimRepository,
    private val excelManager: ExcelXlsxManager
) : ViewModel() {

    private val localControls = MutableStateFlow(LocalUiControls())
    private val _events = MutableSharedFlow<UiEvent>()
    val events = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            repository.ensureSeeded()
        }
    }

    val uiState: StateFlow<AuditClaimUiState> = combine(
        repository.claimsFlow,
        repository.dealersFlow,
        repository.partsFlow,
        repository.syncSettingsFlow,
        repository.isOnlineFlow,
        localControls
    ) { values ->
        @Suppress("UNCHECKED_CAST")
        val rawClaims = values[0] as List<com.example.data.local.ClaimEntity>
        @Suppress("UNCHECKED_CAST")
        val rawDealers = values[1] as List<com.example.data.local.DealerEntity>
        @Suppress("UNCHECKED_CAST")
        val rawParts = values[2] as List<PartCatalogEntity>
        val syncSettings = values[3] as SyncSettings
        val isOnline = values[4] as Boolean
        val controls = values[5] as LocalUiControls

        val enriched = rawClaims.map { it.toEnriched() }
        val dealersMaster = rawDealers.map { it.code }

        // KPIs
        val totalPengajuan = enriched.size
        val belumAdjust = enriched.count { it.status == ClaimConstants.STATUS_NOT_ADJUSTED }
        val sudahAdjust = enriched.count { it.status == ClaimConstants.STATUS_ADJUSTED }
        val totalPart = enriched.sumOf { it.qty }
        val nilaiClaim = enriched.sumOf { it.orderAmount }
        val avgLeadTime = if (totalPengajuan > 0) {
            enriched.sumOf { it.leadTime }.toDouble() / totalPengajuan
        } else 0.0
        val belumPct = if (totalPengajuan > 0) (belumAdjust * 100.0 / totalPengajuan) else 0.0
        val sudahPct = if (totalPengajuan > 0) (sudahAdjust * 100.0 / totalPengajuan) else 0.0

        val kpis = DashboardKpis(
            totalPengajuan = totalPengajuan,
            belumAdjust = belumAdjust,
            sudahAdjust = sudahAdjust,
            belumPct = belumPct,
            sudahPct = sudahPct,
            totalPart = totalPart,
            nilaiClaim = nilaiClaim,
            avgLeadTime = avgLeadTime
        )

        // Dealer stats
        val allDealerCodes = (dealersMaster + enriched.map { it.dealer }).distinct()
        val dealerStats = allDealerCodes.map { dealer ->
            val rows = enriched.filter { it.dealer == dealer }
            DealerStat(
                dealer = dealer,
                count = rows.size,
                partQty = rows.sumOf { it.qty },
                amount = rows.sumOf { it.orderAmount },
                unadjusted = rows.count { it.status == ClaimConstants.STATUS_NOT_ADJUSTED }
            )
        }
        val activeDealerStats = dealerStats.filter { it.count > 0 }.sortedByDescending { it.amount }
        val displayDealerStats = when (controls.dealerRecapMode) {
            DealerRecapMode.ACTIVE_ONLY -> activeDealerStats
            DealerRecapMode.ALL_DEALERS -> dealerStats.sortedWith(
                compareByDescending<DealerStat> { it.amount }.thenBy { it.dealer }
            )
        }

        // Kendala stats
        val allKendala = (ClaimConstants.STANDARD_KENDALA_LIST + enriched.map { it.kendala })
            .filter { it.isNotBlank() }
            .distinct()
        val kendalaStats = allKendala.map { k ->
            val count = enriched.count { it.kendala == k }
            val pct = if (totalPengajuan > 0) (count * 100.0 / totalPengajuan) else 0.0
            KendalaStat(kendala = k, count = count, percentage = pct)
        }.sortedByDescending { it.count }

        // Filtered claims
        val fs = controls.filterState
        val searchLower = fs.searchQuery.trim().lowercase(Locale.US)
        val filteredClaims = enriched.filter { item ->
            val matchDealer = fs.dealerFilter.isEmpty() || item.dealer == fs.dealerFilter
            val matchStatus = fs.statusFilter.isEmpty() || item.status == fs.statusFilter
            val matchKendala = fs.kendalaFilter.isEmpty() || item.kendala == fs.kendalaFilter
            val matchSearch = searchLower.isEmpty() || (
                "${item.no} ${item.dealer} ${item.partNumber} ${item.namaPart} ${item.kendala}"
                    .lowercase(Locale.US)
                    .contains(searchLower)
                )
            matchDealer && matchStatus && matchKendala && matchSearch
        }

        // Filtered master parts
        val partSearchLower = controls.masterPartSearch.trim().lowercase(Locale.US)
        val filteredParts = rawParts.filter { p ->
            partSearchLower.isEmpty() ||
                p.partNumber.lowercase(Locale.US).contains(partSearchLower) ||
                p.namaPart.lowercase(Locale.US).contains(partSearchLower)
        }

        AuditClaimUiState(
            isLoading = false,
            currentTab = controls.currentTab,
            allClaims = enriched,
            filteredClaims = filteredClaims,
            filteredSubtotalAmount = filteredClaims.sumOf { it.orderAmount },
            filteredSubtotalQty = filteredClaims.sumOf { it.qty },
            dealersMaster = dealersMaster.sorted(),
            partsMaster = rawParts,
            filteredPartsMaster = filteredParts,
            masterPartSearch = controls.masterPartSearch,
            filterState = fs,
            dealerRecapMode = controls.dealerRecapMode,
            kpis = kpis,
            activeDealerStats = activeDealerStats,
            displayDealerStats = displayDealerStats,
            kendalaStats = kendalaStats,
            allKendalaOptions = allKendala,
            isOnline = isOnline,
            isSyncing = controls.isSyncing,
            syncSettings = syncSettings
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = AuditClaimUiState()
    )

    fun switchTab(tab: AppTab) {
        localControls.update { it.copy(currentTab = tab) }
    }

    fun setDealerRecapMode(mode: DealerRecapMode) {
        localControls.update { it.copy(dealerRecapMode = mode) }
    }

    fun updateSearchQuery(query: String) {
        localControls.update { it.copy(filterState = it.filterState.copy(searchQuery = query)) }
    }

    fun updateDealerFilter(dealer: String) {
        localControls.update { it.copy(filterState = it.filterState.copy(dealerFilter = dealer)) }
    }

    fun updateStatusFilter(status: String) {
        localControls.update { it.copy(filterState = it.filterState.copy(statusFilter = status)) }
    }

    fun updateKendalaFilter(kendala: String) {
        localControls.update { it.copy(filterState = it.filterState.copy(kendalaFilter = kendala)) }
    }

    fun resetClaimFilters() {
        localControls.update { it.copy(filterState = ClaimFilterState()) }
    }

    fun drillDownDealer(dealer: String) {
        localControls.update {
            it.copy(
                currentTab = AppTab.CLAIMS,
                filterState = ClaimFilterState(dealerFilter = dealer)
            )
        }
    }

    fun drillDownStatus(status: String) {
        localControls.update {
            it.copy(
                currentTab = AppTab.CLAIMS,
                filterState = ClaimFilterState(statusFilter = status)
            )
        }
    }

    fun drillDownKendala(kendala: String) {
        localControls.update {
            it.copy(
                currentTab = AppTab.CLAIMS,
                filterState = ClaimFilterState(kendalaFilter = kendala)
            )
        }
    }

    fun updateMasterPartSearch(query: String) {
        localControls.update { it.copy(masterPartSearch = query) }
    }

    fun saveClaim(
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
    ) {
        viewModelScope.launch {
            val savedNo = repository.saveClaim(
                editNo = editNo,
                tglPengajuan = tglPengajuan,
                dealer = dealer,
                tglAudit = tglAudit,
                partNumber = partNumber,
                namaPart = namaPart,
                qty = qty,
                orderAmount = orderAmount,
                tglAdjust = tglAdjust,
                kendala = kendala
            )
            val msg = if (editNo != null) {
                "Data Claim #$savedNo berhasil diperbarui."
            } else {
                "Pengajuan Claim baru #$savedNo (${dealer.trim().uppercase()}) berhasil ditambahkan."
            }
            _events.emit(UiEvent.Toast(msg))
        }
    }

    fun quickAdjustClaim(no: Int) {
        viewModelScope.launch {
            val ok = repository.quickAdjustClaim(no, ClaimCalculations.getTodayIso())
            if (ok) {
                _events.emit(UiEvent.Toast("Claim #$no ditandai Adjusted per hari ini."))
            }
        }
    }

    fun deleteClaim(no: Int) {
        viewModelScope.launch {
            repository.deleteClaim(no)
            _events.emit(UiEvent.Toast("Data Claim #$no telah dihapus."))
        }
    }

    fun saveMasterPart(partNumber: String, namaPart: String, unitPrice: Double) {
        viewModelScope.launch {
            repository.saveMasterPart(partNumber, namaPart, unitPrice)
            _events.emit(UiEvent.Toast("Part ${partNumber.trim().uppercase()} disimpan ke katalog."))
        }
    }

    fun deleteMasterPart(partNumber: String) {
        viewModelScope.launch {
            repository.deleteMasterPart(partNumber)
            _events.emit(UiEvent.Toast("Part $partNumber dihapus dari katalog."))
        }
    }

    fun addMasterDealer(code: String) {
        viewModelScope.launch {
            val clean = code.trim().uppercase(Locale.US)
            val added = repository.addMasterDealer(clean)
            if (added) {
                _events.emit(UiEvent.Toast("Dealer $clean berhasil ditambahkan."))
            } else {
                _events.emit(UiEvent.Toast("Dealer $clean sudah terdaftar atau kosong."))
            }
        }
    }

    fun deleteMasterDealer(code: String) {
        viewModelScope.launch {
            repository.deleteMasterDealer(code)
            _events.emit(UiEvent.Toast("Dealer $code dihapus dari daftar master."))
        }
    }

    fun resetToInitialData() {
        viewModelScope.launch {
            repository.resetToInitialData()
            localControls.update { it.copy(filterState = ClaimFilterState()) }
            _events.emit(UiEvent.Toast("Data berhasil dikembalikan ke 61 transaksi awal."))
        }
    }

    fun exportToUri(uri: Uri) {
        viewModelScope.launch {
            val state = uiState.value
            val result = excelManager.exportToUri(uri, state.allClaims, state.dealersMaster)
            result.onSuccess {
                _events.emit(UiEvent.Toast("File Excel (.xlsx) dengan 2 sheet berhasil disimpan."))
            }.onFailure { err ->
                _events.emit(UiEvent.Toast(err.localizedMessage ?: "Gagal mengekspor Excel.", isError = true))
            }
        }
    }

    fun shareExcelReport() {
        viewModelScope.launch {
            val state = uiState.value
            val result = excelManager.createShareableXlsxFile(state.allClaims, state.dealersMaster)
            result.onSuccess { file ->
                _events.emit(UiEvent.ShareFile(file))
            }.onFailure { err ->
                _events.emit(UiEvent.Toast(err.localizedMessage ?: "Gagal membuat file Excel.", isError = true))
            }
        }
    }

    fun importFromUri(uri: Uri) {
        viewModelScope.launch {
            val result = excelManager.importFromUri(uri)
            result.onSuccess { imported ->
                repository.importClaims(imported)
                _events.emit(UiEvent.Toast("Berhasil mengimpor ${imported.size} transaksi dari file Excel!"))
            }.onFailure { err ->
                _events.emit(UiEvent.Toast(err.localizedMessage ?: "Gagal membaca file Excel.", isError = true))
            }
        }
    }

    fun saveSyncEndpointUrl(url: String) {
        viewModelScope.launch {
            repository.saveSyncEndpointUrl(url)
            _events.emit(UiEvent.Toast("URL sinkronisasi online disimpan."))
        }
    }

    fun pushOnlineSync(endpointUrl: String) {
        viewModelScope.launch {
            localControls.update { it.copy(isSyncing = true) }
            repository.saveSyncEndpointUrl(endpointUrl)
            val result = repository.pushOnline(endpointUrl)
            localControls.update { it.copy(isSyncing = false) }
            result.onSuccess { msg ->
                _events.emit(UiEvent.Toast(msg))
            }.onFailure { err ->
                _events.emit(UiEvent.Toast("Push gagal: ${err.localizedMessage}", isError = true))
            }
        }
    }

    fun pullOnlineSync(endpointUrl: String) {
        viewModelScope.launch {
            localControls.update { it.copy(isSyncing = true) }
            repository.saveSyncEndpointUrl(endpointUrl)
            val result = repository.pullOnline(endpointUrl)
            localControls.update { it.copy(isSyncing = false) }
            result.onSuccess { count ->
                _events.emit(UiEvent.Toast("Berhasil sinkronisasi $count data claim dari server!"))
            }.onFailure { err ->
                _events.emit(UiEvent.Toast("Pull gagal: ${err.localizedMessage}", isError = true))
            }
        }
    }

    class Factory(
        private val repository: AuditClaimRepository,
        private val excelManager: ExcelXlsxManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AuditClaimViewModel(repository, excelManager) as T
        }
    }
}

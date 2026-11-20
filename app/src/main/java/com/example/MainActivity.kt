package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AssignmentTurnedIn
import androidx.compose.material.icons.rounded.CloudDone
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material.icons.rounded.TableChart
import androidx.compose.material.icons.rounded.UploadFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.ClaimCalculations
import com.example.data.EnrichedClaim
import com.example.data.excel.ExcelXlsxManager
import com.example.data.local.AuditClaimDatabase
import com.example.data.local.SyncPreferences
import com.example.data.remote.OnlineSyncService
import com.example.data.repository.AuditClaimRepository
import com.example.ui.AppTab
import com.example.ui.AuditClaimViewModel
import com.example.ui.UiEvent
import com.example.ui.components.ClaimFormSheet
import com.example.ui.components.OnlineSyncSheet
import com.example.ui.screens.ClaimsScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.MasterCatalogScreen
import com.example.ui.theme.AuditClaimTheme
import com.example.ui.theme.Emerald50
import com.example.ui.theme.Emerald600
import com.example.ui.theme.Rose50
import com.example.ui.theme.Rose600

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AuditClaimTheme {
                AuditClaimApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuditClaimApp() {
    val context = LocalContext.current
    val viewModel: AuditClaimViewModel = viewModel(
        factory = remember(context.applicationContext) {
            val appCtx = context.applicationContext
            val db = AuditClaimDatabase.getInstance(appCtx)
            val prefs = SyncPreferences(appCtx)
            val syncService = OnlineSyncService(appCtx)
            val repo = AuditClaimRepository(db.auditClaimDao(), prefs, syncService)
            val excel = ExcelXlsxManager(appCtx)
            AuditClaimViewModel.Factory(repo, excel)
        }
    )

    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var showOverflowMenu by remember { mutableStateOf(false) }
    var showClaimFormSheet by remember { mutableStateOf(false) }
    var editingClaim by remember { mutableStateOf<EnrichedClaim?>(null) }
    var showOnlineSyncSheet by remember { mutableStateOf(false) }
    var pendingDeleteClaim by remember { mutableStateOf<EnrichedClaim?>(null) }
    var showResetConfirm by remember { mutableStateOf(false) }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        )
    ) { uri ->
        if (uri != null) viewModel.exportToUri(uri)
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) viewModel.importFromUri(uri)
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is UiEvent.Toast -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                is UiEvent.ShareFile -> {
                    val uri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        event.file
                    )
                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                        putExtra(Intent.EXTRA_STREAM, uri)
                        putExtra(Intent.EXTRA_SUBJECT, "Laporan Audit Claim - II26-HabelImprovement")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(
                        Intent.createChooser(sendIntent, "Bagikan Laporan Audit Claim (.xlsx)")
                    )
                }
            }
        }
    }

    // BackHandler returns secondary tabs to Dashboard
    if (state.currentTab != AppTab.DASHBOARD) {
        BackHandler {
            viewModel.switchTab(AppTab.DASHBOARD)
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useNavRail = maxWidth >= 600.dp

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.AssignmentTurnedIn,
                                    contentDescription = "Audit Claim",
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Audit Claim",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "II26-HabelImprovement",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    },
                    actions = {
                        // Online / Sync Pill
                        Surface(
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable { showOnlineSyncSheet = true }
                                .testTag("btn_online_sync_pill"),
                            shape = CircleShape,
                            color = if (state.isOnline) Emerald50 else Rose50
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = if (state.isOnline) Icons.Rounded.CloudDone else Icons.Rounded.CloudOff,
                                    contentDescription = if (state.isOnline) "Online" else "Offline",
                                    tint = if (state.isOnline) Emerald600 else Rose600,
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = if (state.isOnline) "Online" else "Offline",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (state.isOnline) Emerald600 else Rose600
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        Box {
                            IconButton(
                                onClick = { showOverflowMenu = true },
                                modifier = Modifier.testTag("btn_top_menu")
                            ) {
                                Icon(Icons.Rounded.MoreVert, contentDescription = "Menu Lainnya")
                            }
                            DropdownMenu(
                                expanded = showOverflowMenu,
                                onDismissRequest = { showOverflowMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Bagikan Excel (.xlsx)") },
                                    leadingIcon = { Icon(Icons.Rounded.Share, contentDescription = null) },
                                    onClick = {
                                        showOverflowMenu = false
                                        viewModel.shareExcelReport()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Export Excel (.xlsx)") },
                                    leadingIcon = { Icon(Icons.Rounded.Download, contentDescription = null) },
                                    onClick = {
                                        showOverflowMenu = false
                                        exportLauncher.launch("Audit_Claim_Report_${ClaimCalculations.getTodayIso()}.xlsx")
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Import Excel / CSV") },
                                    leadingIcon = { Icon(Icons.Rounded.UploadFile, contentDescription = null) },
                                    onClick = {
                                        showOverflowMenu = false
                                        importLauncher.launch(
                                            arrayOf(
                                                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                                                "application/vnd.ms-excel",
                                                "text/csv",
                                                "text/comma-separated-values",
                                                "*/*"
                                            )
                                        )
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Sinkronisasi Cloud / URL") },
                                    leadingIcon = { Icon(Icons.Rounded.CloudDone, contentDescription = null) },
                                    onClick = {
                                        showOverflowMenu = false
                                        showOnlineSyncSheet = true
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Reset 61 Data Awal", color = Rose600) },
                                    leadingIcon = { Icon(Icons.Rounded.RestartAlt, contentDescription = null, tint = Rose600) },
                                    onClick = {
                                        showOverflowMenu = false
                                        showResetConfirm = true
                                    }
                                )
                            }
                        }
                    }
                )
            },
            bottomBar = {
                if (!useNavRail) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        AppTab.entries.forEach { tab ->
                            val selected = state.currentTab == tab
                            NavigationBarItem(
                                selected = selected,
                                onClick = { viewModel.switchTab(tab) },
                                icon = {
                                    TabIcon(tab = tab, claimsCount = state.allClaims.size)
                                },
                                label = { Text(tab.label, maxLines = 1) },
                                modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                            )
                        }
                    }
                }
            },
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    onClick = {
                        editingClaim = null
                        showClaimFormSheet = true
                    },
                    icon = { Icon(Icons.Rounded.Add, contentDescription = null) },
                    text = { Text("Input Claim") },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.testTag("fab_input_claim")
                )
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (useNavRail) {
                    NavigationRail(
                        modifier = Modifier
                            .fillMaxHeight()
                            .navigationBarsPadding(),
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        Spacer(modifier = Modifier.weight(0.1f))
                        AppTab.entries.forEach { tab ->
                            val selected = state.currentTab == tab
                            NavigationRailItem(
                                selected = selected,
                                onClick = { viewModel.switchTab(tab) },
                                icon = {
                                    TabIcon(tab = tab, claimsCount = state.allClaims.size)
                                },
                                label = { Text(tab.label) },
                                modifier = Modifier.testTag("rail_tab_${tab.name.lowercase()}")
                            )
                        }
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }

                Box(modifier = Modifier.weight(1f).fillMaxSize()) {
                    when (state.currentTab) {
                        AppTab.DASHBOARD -> DashboardScreen(
                            state = state,
                            onFilterByStatus = viewModel::drillDownStatus,
                            onFilterByDealer = viewModel::drillDownDealer,
                            onFilterByKendala = viewModel::drillDownKendala,
                            onDealerRecapModeChange = viewModel::setDealerRecapMode
                        )
                        AppTab.CLAIMS -> ClaimsScreen(
                            state = state,
                            onSearchChange = viewModel::updateSearchQuery,
                            onDealerFilterChange = viewModel::updateDealerFilter,
                            onStatusFilterChange = viewModel::updateStatusFilter,
                            onKendalaFilterChange = viewModel::updateKendalaFilter,
                            onResetFilters = viewModel::resetClaimFilters,
                            onQuickAdjust = viewModel::quickAdjustClaim,
                            onEditClaim = { claim ->
                                editingClaim = claim
                                showClaimFormSheet = true
                            },
                            onDeleteClaim = { claim ->
                                pendingDeleteClaim = claim
                            }
                        )
                        AppTab.MASTER -> MasterCatalogScreen(
                            state = state,
                            onPartSearchChange = viewModel::updateMasterPartSearch,
                            onSavePart = viewModel::saveMasterPart,
                            onDeletePart = viewModel::deleteMasterPart,
                            onAddDealer = viewModel::addMasterDealer,
                            onDeleteDealer = viewModel::deleteMasterDealer
                        )
                    }
                }
            }
        }
    }

    // Input / Edit Claim Sheet
    if (showClaimFormSheet) {
        ClaimFormSheet(
            editingClaim = editingClaim,
            dealersMaster = state.dealersMaster,
            partsMaster = state.partsMaster,
            onDismiss = {
                showClaimFormSheet = false
                editingClaim = null
            },
            onSave = { editNo, tglPengajuan, dealer, tglAudit, partNumber, namaPart, qty, orderAmount, tglAdjust, kendala ->
                viewModel.saveClaim(
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
                showClaimFormSheet = false
                editingClaim = null
            }
        )
    }

    // Online Sync & Share Sheet
    if (showOnlineSyncSheet) {
        OnlineSyncSheet(
            isOnline = state.isOnline,
            isSyncing = state.isSyncing,
            syncSettings = state.syncSettings,
            onDismiss = { showOnlineSyncSheet = false },
            onShareExcel = viewModel::shareExcelReport,
            onPushOnline = viewModel::pushOnlineSync,
            onPullOnline = viewModel::pullOnlineSync,
            onSaveUrl = viewModel::saveSyncEndpointUrl
        )
    }

    // Confirm Delete Claim
    pendingDeleteClaim?.let { target ->
        AlertDialog(
            onDismissRequest = { pendingDeleteClaim = null },
            title = { Text("Hapus Data Claim #${target.no}?") },
            text = {
                Text("Transaksi ${target.dealer} (${target.partNumber}) akan dihapus dari daftar.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteClaim(target.no)
                        pendingDeleteClaim = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Rose600)
                ) {
                    Text("Ya, Hapus")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteClaim = null }) {
                    Text("Batal")
                }
            }
        )
    }

    // Confirm Reset Data Awal
    if (showResetConfirm) {
        AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            title = { Text("Reset ke 61 Data Awal?") },
            text = {
                Text("Semua perubahan atau input baru akan dikembalikan ke dataset awal II26-HabelImprovement.xlsx.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.resetToInitialData()
                        showResetConfirm = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Rose600)
                ) {
                    Text("Ya, Reset")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirm = false }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
private fun TabIcon(tab: AppTab, claimsCount: Int) {
    when (tab) {
        AppTab.DASHBOARD -> Icon(Icons.Rounded.Dashboard, contentDescription = tab.label)
        AppTab.CLAIMS -> BadgedBox(
            badge = {
                if (claimsCount > 0) {
                    Badge { Text(claimsCount.toString()) }
                }
            }
        ) {
            Icon(Icons.Rounded.TableChart, contentDescription = tab.label)
        }
        AppTab.MASTER -> Icon(Icons.Rounded.Storage, contentDescription = tab.label)
    }
}

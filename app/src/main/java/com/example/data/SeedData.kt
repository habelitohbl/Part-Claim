package com.example.data

import com.example.data.local.ClaimEntity
import com.example.data.local.DealerEntity
import com.example.data.local.PartCatalogEntity
import kotlin.math.roundToLong

object SeedData {
    val INITIAL_DEALERS: List<String> = listOf(
        "SY", "TB PRO", "TB PDK", "TB ADS", "TB LBG", "TB BDB", "TB STB", "TB BLB", "TB TBG",
        "TB LBB", "TB LAG", "TB SCC", "TB PRM", "TB KNL", "TB SP4", "TB LSK", "TB PPJ", "TB PDL",
        "TB BSK", "TB SHT", "TB PYK", "TB TJP", "TB SLK", "TB SPR", "TB SBG", "TB PPU", "TB KBR",
        "TB SRB", "TB MLB", "TB SGR", "TB PNN", "TB SRT", "TB AHJ", "TB LNG"
    )

    val INITIAL_CLAIMS: List<ClaimEntity> = listOf(
        ClaimEntity(1, "2026-06-24", "TB BSK", "2026-07-06", "B6YH58101100", "COMMUNICATION CONT. UNIT ASSY", 1, 371000.0, "2026-07-28", "Claim Complete"),
        ClaimEntity(2, "2026-06-24", "TB BSK", "2026-07-06", "B6YH58101100", "COMMUNICATION CONT. UNIT ASSY", 1, 371000.0, "2026-07-28", "Claim Complete"),
        ClaimEntity(3, "2026-06-24", "TB BSK", "2026-07-06", "B6YH58101100", "COMMUNICATION CONT. UNIT ASSY", 1, 371000.0, "2026-10-14", "Claim Complete"),
        ClaimEntity(4, "2026-06-15", "TB BSK", "2026-07-06", "BEJF41100000", "FUEL TANK COMP. (BEJ1)", 1, 235500.0, "2026-07-28", "Claim Complete"),
        ClaimEntity(5, "2026-08-05", "TB BSK", "2026-07-06", "931022081500", "OIL SEAL (2PH1)", 1, 6581.8636, "2026-10-14", "Claim Complete"),
        ClaimEntity(6, "2026-08-05", "TB BSK", "2026-07-06", "931023280700", "OIL SEAL (2DP1)", 1, 9213.0, "2026-10-14", "Claim Complete"),
        ClaimEntity(7, "2026-08-05", "TB BSK", "2026-07-06", "2DPE72112000", "GEAR, 1ST WHEEL (B2T2)", 1, 127508.0, "2026-10-14", "Claim Complete"),
        ClaimEntity(8, "2026-08-05", "TB BSK", "2026-07-06", "2DPE74103100", "MAIN AXLE COMP. (2DP1)", 1, 219638.0, "2026-10-14", "Claim Complete"),
        ClaimEntity(9, "2026-08-05", "TB BSK", "2026-07-06", "B65E61110100", "GEAR, PRIMARY DRIVE (BBP1)", 1, 146671.0, "2026-10-14", "Claim Complete"),
        ClaimEntity(10, "2026-08-05", "TB BSK", "2026-07-06", "93306351Y800", "BEARING", 1, 20637.0, "2026-10-14", "Claim Complete"),
        ClaimEntity(11, "2026-08-05", "TB BSK", "2026-07-06", "93306303YW00", "BEARING", 1, 22111.0, "2026-10-14", "Claim Complete"),
        ClaimEntity(12, "2026-08-05", "TB BSK", "2026-07-06", "93306302YU00", "BEARING", 2, 18242.0, "2026-10-14", "Claim Complete"),
        ClaimEntity(13, "2026-08-05", "TB BSK", "2026-07-06", "93306255YA00", "BEARING (B3F1)", 1, 25059.0, "2026-10-14", "Claim Complete"),
        ClaimEntity(14, "2026-08-05", "TB BSK", "2026-07-06", "93306204XR00", "BEARING (2DP1)", 1, 27823.0, "2026-10-14", "Claim Complete"),
        ClaimEntity(15, "2026-08-05", "TB BSK", "2026-07-06", "B6HE74210000", "AXLE, DRIVE (B6H1)", 1, 197527.0, "2026-10-14", "Claim Complete"),
        ClaimEntity(16, "2026-07-11", "TB PYK", "2026-07-20", "B6YH58101100", "COMMUNICATION CONT. UNIT ASSY", 1, 371000.0, "2026-07-28", "Claim Complete"),
        ClaimEntity(17, "2026-07-11", "TB PYK", "2026-07-20", "B6YH58101100", "COMMUNICATION CONT. UNIT ASSY", 1, 371000.0, "2026-07-28", "Claim Complete"),
        ClaimEntity(18, "2026-07-11", "TB PYK", "2026-07-20", "B6YH58101100", "COMMUNICATION CONT. UNIT ASSY", 1, 371000.0, "2026-07-28", "Claim Complete"),
        ClaimEntity(19, "2026-07-11", "TB PYK", "2026-07-20", "B6YH58101100", "COMMUNICATION CONT. UNIT ASSY", 1, 371000.0, "2026-07-28", "Claim Complete"),
        ClaimEntity(20, "2026-07-11", "TB PYK", "2026-07-20", "B6YH58101300", "COMMUNICATION CONT. UNIT ASSY", 1, 371000.0, "2026-07-28", "Claim Complete"),
        ClaimEntity(21, "2026-07-11", "TB PYK", "2026-07-20", "B6YH58101300", "COMMUNICATION CONT. UNIT ASSY", 1, 371000.0, "2026-07-28", "Claim Complete"),
        ClaimEntity(22, "2026-07-11", "TB PYK", "2026-07-20", "B6YH58101300", "COMMUNICATION CONT. UNIT ASSY", 1, 371000.0, "2026-07-28", "Claim Complete"),
        ClaimEntity(23, "2026-06-06", "TB PYK", "2026-07-20", "2DPWB0730000", "TOOL KIT", 1, 11500.0, "2026-09-05", "Claim Complete"),
        ClaimEntity(24, "2026-06-06", "TB PYK", "2026-07-20", "2DPWB0730000", "TOOL KIT", 1, 11500.0, "2026-09-05", "Claim Complete"),
        ClaimEntity(25, "2026-03-03", "TB PYK", "2026-07-20", "BN1H21000000", "BATTERY ASSY YTZ6V", 1, 305500.0, "2026-07-28", "Claim Complete"),
        ClaimEntity(26, "2026-05-03", "TB PYK", "2026-07-20", "BN1H21000000", "BATTERY ASSY YTZ6V", 1, 305500.0, "", "Verifikasi Part"),
        ClaimEntity(27, "2026-07-10", "TB SP4", "2026-07-27", "901090803400", "BOLT (ALFA,FRC1,Y125Z)", 1, 21000.0, "2026-07-29", "Claim Complete"),
        ClaimEntity(28, "2026-06-01", "TB SP4", "2026-07-27", "B6YH58101100", "COMMUNICATION CONT. UNIT ASSY", 1, 371000.0, "2026-07-29", "Claim Complete"),
        ClaimEntity(29, "2026-06-10", "TB SP4", "2026-07-27", "B6YH58101100", "COMMUNICATION CONT. UNIT ASSY", 1, 371000.0, "2026-07-29", "Claim Complete"),
        ClaimEntity(30, "2026-07-02", "TB SP4", "2026-07-27", "B6YH58101100", "COMMUNICATION CONT. UNIT ASSY", 1, 371000.0, "2026-09-05", "Claim Complete"),
        ClaimEntity(31, "2026-07-05", "TB SP4", "2026-07-27", "B6YH58101100", "COMMUNICATION CONT. UNIT ASSY", 1, 371000.0, "2026-09-05", "Claim Complete"),
        ClaimEntity(32, "2026-07-06", "TB SP4", "2026-07-27", "B6YH58101100", "COMMUNICATION CONT. UNIT ASSY", 1, 371000.0, "2026-09-05", "Claim Complete"),
        ClaimEntity(33, "2026-07-07", "TB SP4", "2026-07-27", "B6YH58101100", "COMMUNICATION CONT. UNIT ASSY", 1, 371000.0, "2026-09-05", "Claim Complete"),
        ClaimEntity(34, "2026-07-14", "TB SP4", "2026-07-27", "2DPWB0730000", "TOOL KIT", 1, 11500.0, "2026-07-29", "Claim Complete"),
        ClaimEntity(35, "2026-07-10", "TB SP4", "2026-07-27", "2PVF74110000", "FOOTREST 1 (2PV1)", 1, 166000.0, "2026-07-29", "Claim Complete"),
        ClaimEntity(36, "2026-07-10", "TB SP4", "2026-07-27", "BPAH594A1000", "STARTER GEN. CONT. UNIT ASSY", 1, 2265500.0, "", "Verifikasi Part"),
        ClaimEntity(37, "2025-11-11", "TB SP4", "2026-07-27", "BN1H21000000", "BATTERY ASSY YTZ6V", 1, 305500.0, "", "Sudah Diinput, Menunggu Diadjust"),
        ClaimEntity(38, "2026-06-20", "TB LAG", "2026-08-03", "2DPWB0730000", "TOOL KIT", 1, 11500.0, "", "Sudah Diinput, Menunggu Diadjust"),
        ClaimEntity(39, "2026-06-23", "TB LAG", "2026-08-03", "2DPWB0730000", "TOOL KIT", 1, 11500.0, "", "Sudah Diinput, Menunggu Diadjust"),
        ClaimEntity(40, "2026-07-30", "TB LBB", "2026-08-31", "BN1H21000000", "BATTERY ASSY YTZ6V", 1, 305500.0, "", "Sudah Diinput, Menunggu Diadjust"),
        ClaimEntity(41, "2026-07-30", "TB LBB", "2026-08-31", "B6YH58101000", "COMMUNICATION CONT. UNIT ASSY", 1, 371000.0, "2026-09-05", "Claim Complete"),
        ClaimEntity(42, "2026-07-30", "TB LBB", "2026-08-31", "B6YH58101000", "COMMUNICATION CONT. UNIT ASSY", 1, 371000.0, "2026-10-14", "Claim Complete"),
        ClaimEntity(43, "2026-03-10", "TB LSK", "2026-09-07", "BPAH35100000", "SPEEDOMETER ASSY", 1, 1677500.0, "", "Dokumen Kurang"),
        ClaimEntity(44, "2026-08-04", "TB KBR", "2026-10-26", "90793AJ82100", "YAMALUBE FRONT FORK OIL G-10", 1, 22500.0, "", "Verifikasi Part"),
        ClaimEntity(45, "2026-08-04", "TB KBR", "2026-10-26", "3AYF31450000", "OIL SEAL (T105)", 1, 15000.0, "", "Verifikasi Part"),
        ClaimEntity(46, "2026-08-04", "TB KBR", "2026-10-26", "B65F31100000", "INNER TUBE COMP.1 (B651)", 1, 327500.0, "", "Verifikasi Part"),
        ClaimEntity(47, "2026-08-04", "TB KBR", "2026-10-26", "2PVH43102000", "HEADLIGHT UNIT ASSY (2PV9)", 1, 1048000.0, "", "Verifikasi Part"),
        ClaimEntity(48, "2026-08-28", "TB SLK", "2026-11-02", "54PE21190000", "SEAL, VALVE STEM (54P1)", 2, 31000.0, "2026-11-04", "Claim Complete"),
        ClaimEntity(49, "2026-10-13", "TB SLK", "2026-11-02", "BEJE21700000", "CAMSHAFT ASSY 1 (BEJ1)", 1, 343000.0, "", "Sudah Diinput, Menunggu Diadjust"),
        ClaimEntity(50, "2025-11-17", "TB SLK", "2026-11-02", "BN1H21000000", "BATTERY ASSY YTZ6V", 1, 305500.0, "", "Verifikasi Part"),
        ClaimEntity(51, "2025-07-29", "TB SLK", "2026-11-02", "BN1H21000000", "BATTERY ASSY YTZ6V", 1, 305500.0, "", "Verifikasi Part"),
        ClaimEntity(52, "2025-08-13", "TB SLK", "2026-11-02", "BN1H21000000", "BATTERY ASSY YTZ6V", 1, 305500.0, "", "Verifikasi Part"),
        ClaimEntity(53, "2025-12-09", "TB SLK", "2026-11-02", "BN1H21000000", "BATTERY ASSY YTZ6V", 1, 305500.0, "", "Verifikasi Part"),
        ClaimEntity(54, "2026-07-21", "TB SLK", "2026-11-02", "BN1H21000000", "BATTERY ASSY YTZ6V", 1, 305500.0, "", "Verifikasi Part"),
        ClaimEntity(55, "2026-07-21", "TB SLK", "2026-11-02", "BPAF22100000", "SHOCK ABSORBER ASSY, REAR", 1, 727500.0, "2026-11-04", "Claim Complete"),
        ClaimEntity(56, "2026-07-13", "TB SLK", "2026-11-02", "B3FF22100000", "SHOCK ABSORBER ASSY, REAR (B3F1)", 1, 280000.0, "2026-11-04", "Claim Complete"),
        ClaimEntity(57, "2026-07-31", "TB SLK", "2026-11-02", "BPAH594A2000", "STARTER GEN. CONT. UNIT ASSY", 1, 2265500.0, "2026-11-04", "Claim Complete"),
        ClaimEntity(58, "2026-08-03", "TB SLK", "2026-11-02", "BTEH594A2000", "STARTER GEN. CONT. UNIT ASSY", 1, 2199500.0, "2026-11-04", "Claim Complete"),
        ClaimEntity(59, "2026-07-28", "TB SLK", "2026-11-02", "B8AE39070000", "FUEL PUMP COMP.", 1, 799500.0, "", "Sudah Diinput, Menunggu Diadjust"),
        ClaimEntity(60, "2026-08-07", "TB SLK", "2026-11-02", "B8AE39070000", "FUEL PUMP COMP.", 1, 799500.0, "2026-11-04", "Claim Complete"),
        ClaimEntity(61, "2026-05-19", "TB PPJ", "2026-11-04", "B3FE39070100", "FUEL PUMP COMP. (B3F1)", 1, 795500.0, "", "Verifikasi Part")
    )

    fun buildDefaultDealers(): List<DealerEntity> = INITIAL_DEALERS.map { DealerEntity(it) }

    fun buildDefaultPartsCatalog(claims: List<ClaimEntity> = INITIAL_CLAIMS): List<PartCatalogEntity> {
        val map = LinkedHashMap<String, PartCatalogEntity>()
        claims.forEach { item ->
            val pn = item.partNumber.trim().uppercase()
            if (pn.isNotEmpty() && !map.containsKey(pn)) {
                val unitPrice = if (item.qty > 0) {
                    (item.orderAmount / item.qty * 100.0).roundToLong() / 100.0
                } else {
                    item.orderAmount
                }
                map[pn] = PartCatalogEntity(
                    partNumber = pn,
                    namaPart = item.namaPart.trim().uppercase(),
                    unitPrice = unitPrice
                )
            }
        }
        return map.values.sortedBy { it.partNumber }
    }
}

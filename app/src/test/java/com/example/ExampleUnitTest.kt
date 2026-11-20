package com.example

import com.example.data.ClaimCalculations
import com.example.data.ClaimConstants
import com.example.data.SeedData
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.math.roundToLong

class ExampleUnitTest {
    @Test
    fun seedData_matchesInitialAuditClaimSummary() {
        val enriched = SeedData.INITIAL_CLAIMS.map { it.toEnriched() }
        assertEquals(61, enriched.size)
        assertEquals(19, enriched.count { it.status == ClaimConstants.STATUS_NOT_ADJUSTED })
        assertEquals(42, enriched.count { it.status == ClaimConstants.STATUS_ADJUSTED })
        assertEquals(63, enriched.sumOf { it.qty })
        assertEquals(24325511L, enriched.sumOf { it.orderAmount }.roundToLong())
        assertEquals(34, SeedData.INITIAL_DEALERS.size)
    }

    @Test
    fun calculations_computeStatusAndLeadTimeAccurately() {
        assertEquals(ClaimConstants.STATUS_ADJUSTED, ClaimCalculations.calculateStatus("2026-07-28"))
        assertEquals(ClaimConstants.STATUS_NOT_ADJUSTED, ClaimCalculations.calculateStatus(""))
        assertEquals(34, ClaimCalculations.calculateLeadTime("2026-06-24", "2026-07-28"))
        assertEquals("28/07/2026", ClaimCalculations.formatDateDisplay("2026-07-28"))
    }
}

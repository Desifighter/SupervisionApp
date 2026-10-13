package com.supervisionapp.lab

import org.junit.Assert.assertTrue
import org.junit.Test

class RiskRegisterTest {
    @Test
    fun riskRegisterListsCurrentRisksAndOwners() {
        val diagnostics = DiagnosticsState(
            usageAccess = "GRANTED",
            accessibility = "ENABLED",
            profile = "WORK",
            deviceOwner = "YES",
            profileOwner = "YES"
        )

        val register = buildRiskRegister(diagnostics)

        assertTrue(register.contains("Risk Register"))
        assertTrue(register.contains("READY_FOR_REVIEW"))
        assertTrue(register.contains("Owner"))
    }

    @Test
    fun riskRegisterHighlightsBlockedStateWhenSupportIsMissing() {
        val diagnostics = DiagnosticsState(
            usageAccess = "NOT GRANTED",
            accessibility = "DISABLED",
            profile = "PERSONAL",
            deviceOwner = "NO",
            profileOwner = "NO"
        )

        val register = buildRiskRegister(diagnostics)

        assertTrue(register.contains("BLOCKED"))
        assertTrue(register.contains("Risk"))
    }
}

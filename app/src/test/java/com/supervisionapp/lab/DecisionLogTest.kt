package com.supervisionapp.lab

import org.junit.Assert.assertTrue
import org.junit.Test

class DecisionLogTest {
    @Test
    fun decisionLogIncludesStatusAndReason() {
        val diagnostics = DiagnosticsState(
            usageAccess = "GRANTED",
            accessibility = "ENABLED",
            profile = "WORK",
            deviceOwner = "YES",
            profileOwner = "YES"
        )

        val log = buildDecisionLog(diagnostics)

        assertTrue(log.contains("Decision Log"))
        assertTrue(log.contains("READY_FOR_REVIEW"))
    }

    @Test
    fun decisionLogFlagsBlockersClearly() {
        val diagnostics = DiagnosticsState(
            usageAccess = "NOT GRANTED",
            accessibility = "DISABLED",
            profile = "PERSONAL",
            deviceOwner = "NO",
            profileOwner = "NO"
        )

        val log = buildDecisionLog(diagnostics)

        assertTrue(log.contains("BLOCKED"))
        assertTrue(log.contains("Usage Access"))
    }
}

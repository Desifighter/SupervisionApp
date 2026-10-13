package com.supervisionapp.lab

import org.junit.Assert.assertTrue
import org.junit.Test

class ExecutiveBriefingTest {
    @Test
    fun briefingContainsExecutiveSummaryAndBlockers() {
        val diagnostics = DiagnosticsState(
            usageAccess = "GRANTED",
            accessibility = "ENABLED",
            profile = "WORK",
            deviceOwner = "YES",
            profileOwner = "YES"
        )

        val briefing = buildExecutiveBriefing(diagnostics)

        assertTrue(briefing.contains("SupervisionAPP Executive Briefing"))
        assertTrue(briefing.contains("READY_FOR_REVIEW"))
    }

    @Test
    fun briefingMarksBlockedStatusWhenRequirementsAreMissing() {
        val diagnostics = DiagnosticsState(
            usageAccess = "NOT GRANTED",
            accessibility = "DISABLED",
            profile = "PERSONAL",
            deviceOwner = "NO",
            profileOwner = "NO"
        )

        val briefing = buildExecutiveBriefing(diagnostics)

        assertTrue(briefing.contains("BLOCKED"))
        assertTrue(briefing.contains("Usage Access"))
    }
}

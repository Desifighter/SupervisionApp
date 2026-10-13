package com.supervisionapp.lab

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StatusBundleTest {
    @Test
    fun bundlesCurrentStatusForReviewExport() {
        val diagnostics = DiagnosticsState(
            usageAccess = "GRANTED",
            accessibility = "ENABLED",
            profile = "WORK",
            deviceOwner = "YES",
            profileOwner = "YES"
        )

        val bundle = buildStatusBundle(diagnostics)

        assertEquals("READY_FOR_REVIEW", bundle.deploymentState)
        assertTrue(bundle.summary.contains("READY_FOR_REVIEW"))
    }

    @Test
    fun includesWarningsWhenNotReady() {
        val diagnostics = DiagnosticsState(
            usageAccess = "NOT GRANTED",
            accessibility = "DISABLED",
            profile = "PERSONAL",
            deviceOwner = "NO",
            profileOwner = "NO"
        )

        val bundle = buildStatusBundle(diagnostics)

        assertEquals("BLOCKED", bundle.deploymentState)
        assertTrue(bundle.warnings.isNotEmpty())
    }
}

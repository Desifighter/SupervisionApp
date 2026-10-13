package com.supervisionapp.lab

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReviewExportTest {
    @Test
    fun createsAReadableReviewExport() {
        val diagnostics = DiagnosticsState(
            usageAccess = "GRANTED",
            accessibility = "ENABLED",
            profile = "WORK",
            deviceOwner = "YES",
            profileOwner = "YES"
        )

        val export = buildReviewExport(diagnostics)

        assertTrue(export.contains("SupervisionAPP Review Export"))
        assertTrue(export.contains("READY_FOR_REVIEW"))
    }

    @Test
    fun includesAllImportantStatusFields() {
        val diagnostics = DiagnosticsState(
            usageAccess = "NOT GRANTED",
            accessibility = "DISABLED",
            profile = "PERSONAL",
            deviceOwner = "NO",
            profileOwner = "NO"
        )

        val export = buildReviewExport(diagnostics)

        assertTrue(export.contains("Usage Access"))
        assertTrue(export.contains("Accessibility"))
        assertTrue(export.contains("Current Profile"))
        assertTrue(export.contains("BLOCKED"))
    }
}

package com.supervisionapp.lab

import org.junit.Assert.assertTrue
import org.junit.Test

class RecommendationSheetTest {
    @Test
    fun recommendationSheetContainsSummaryAndNextStep() {
        val diagnostics = DiagnosticsState(
            usageAccess = "GRANTED",
            accessibility = "ENABLED",
            profile = "WORK",
            deviceOwner = "YES",
            profileOwner = "YES"
        )

        val sheet = buildRecommendationSheet(diagnostics)

        assertTrue(sheet.contains("Recommendation Sheet"))
        assertTrue(sheet.contains("READY_FOR_REVIEW"))
        assertTrue(sheet.contains("Next step"))
    }

    @Test
    fun recommendationSheetFlagsBlockersWhenNotReady() {
        val diagnostics = DiagnosticsState(
            usageAccess = "NOT GRANTED",
            accessibility = "DISABLED",
            profile = "PERSONAL",
            deviceOwner = "NO",
            profileOwner = "NO"
        )

        val sheet = buildRecommendationSheet(diagnostics)

        assertTrue(sheet.contains("BLOCKED"))
        assertTrue(sheet.contains("Missing"))
    }
}

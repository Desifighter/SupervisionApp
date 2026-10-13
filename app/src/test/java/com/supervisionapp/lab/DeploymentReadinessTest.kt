package com.supervisionapp.lab

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DeploymentReadinessTest {
    @Test
    fun readinessIsBlockedWhenRuntimeRequirementsAreMissing() {
        val diagnostics = DiagnosticsState(
            usageAccess = "NOT GRANTED",
            accessibility = "DISABLED",
            profile = "PERSONAL",
            deviceOwner = "NO",
            profileOwner = "NO"
        )

        val result = evaluateDeploymentReadiness(diagnostics)

        assertEquals("BLOCKED", result.state)
        assertTrue(result.warnings.isNotEmpty())
    }

    @Test
    fun readinessRequiresManagedEnvironmentForPilotReview() {
        val diagnostics = DiagnosticsState(
            usageAccess = "GRANTED",
            accessibility = "ENABLED",
            profile = "WORK",
            deviceOwner = "YES",
            profileOwner = "YES"
        )

        val result = evaluateDeploymentReadiness(diagnostics)

        assertEquals("READY_FOR_REVIEW", result.state)
        assertTrue(result.readyForManagedDeployment)
    }
}

package com.supervisionapp.lab

import org.junit.Assert.assertEquals
import org.junit.Test

class PolicyEngineTest {
    @Test
    fun usageAndAccessibilityAreRequiredForReadyState() {
        val diagnostics = DiagnosticsState(
            usageAccess = "GRANTED",
            accessibility = "ENABLED",
            profile = "PERSONAL",
            deviceOwner = "NO",
            profileOwner = "NO"
        )

        val results = evaluatePolicyRules(diagnostics)
        val usageRule = results.first { it.rule.id == "usage_access" }
        val accessibilityRule = results.first { it.rule.id == "accessibility" }
        val managedProfileRule = results.first { it.rule.id == "managed_profile" }

        assertEquals(PolicyStatus.READY, usageRule.status)
        assertEquals(PolicyStatus.READY, accessibilityRule.status)
        assertEquals(PolicyStatus.UNSUPPORTED, managedProfileRule.status)
    }

    @Test
    fun blockedWhenRuntimePermissionIsMissing() {
        val diagnostics = DiagnosticsState(
            usageAccess = "NOT GRANTED",
            accessibility = "DISABLED",
            profile = "PERSONAL",
            deviceOwner = "NO",
            profileOwner = "NO"
        )

        val results = evaluatePolicyRules(diagnostics)
        assertEquals(PolicyStatus.BLOCKED, results.first { it.rule.id == "usage_access" }.status)
        assertEquals(PolicyStatus.BLOCKED, results.first { it.rule.id == "accessibility" }.status)
    }
}

package com.supervisionapp.lab

import org.junit.Assert.assertEquals
import org.junit.Test

class GuardedActionTest {
    @Test
    fun actionIsReadyOnlyWhenAllRequiredCapabilitiesExist() {
        val ready = DiagnosticsState(
            usageAccess = "GRANTED",
            accessibility = "ENABLED",
            profile = "WORK",
            deviceOwner = "YES",
            profileOwner = "YES"
        )

        assertEquals(GuardedActionStatus.READY, evaluateGuardedAction(ready).status)
    }

    @Test
    fun actionIsBlockedWhenPlatformPermissionsAreMissing() {
        val blocked = DiagnosticsState(
            usageAccess = "NOT GRANTED",
            accessibility = "DISABLED",
            profile = "WORK",
            deviceOwner = "YES",
            profileOwner = "YES"
        )

        assertEquals(GuardedActionStatus.BLOCKED, evaluateGuardedAction(blocked).status)
    }

    @Test
    fun actionIsUnsupportedWhenManagedProfileSupportIsMissing() {
        val unsupported = DiagnosticsState(
            usageAccess = "GRANTED",
            accessibility = "ENABLED",
            profile = "PERSONAL",
            deviceOwner = "NO",
            profileOwner = "NO"
        )

        assertEquals(GuardedActionStatus.UNSUPPORTED, evaluateGuardedAction(unsupported).status)
    }
}

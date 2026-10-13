package com.supervisionapp.lab

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReadinessPacketTest {
    @Test
    fun packetIncludesReviewSummaryAndWarnings() {
        val diagnostics = DiagnosticsState(
            usageAccess = "GRANTED",
            accessibility = "ENABLED",
            profile = "WORK",
            deviceOwner = "YES",
            profileOwner = "YES"
        )

        val packet = buildReadinessPacket(diagnostics)

        assertTrue(packet.contains("SupervisionAPP Readiness Packet"))
        assertTrue(packet.contains("READY_FOR_REVIEW"))
    }

    @Test
    fun packetShowsBlockedStateWhenSupportIsMissing() {
        val diagnostics = DiagnosticsState(
            usageAccess = "NOT GRANTED",
            accessibility = "DISABLED",
            profile = "PERSONAL",
            deviceOwner = "NO",
            profileOwner = "NO"
        )

        val packet = buildReadinessPacket(diagnostics)

        assertTrue(packet.contains("BLOCKED"))
        assertTrue(packet.contains("Usage Access"))
    }
}

package com.supervisionapp.lab

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LocalReportTest {
    @Test
    fun summarizesEntriesAcrossStatuses() {
        val entries = listOf(
            AuditEntry("2026-10-13T09:00:00Z", "READY", "Ready for evaluation"),
            AuditEntry("2026-10-13T09:05:00Z", "BLOCKED", "Usage access missing"),
            AuditEntry("2026-10-13T09:10:00Z", "UNSUPPORTED", "Profile not managed"),
            AuditEntry("2026-10-13T09:15:00Z", "READY", "Evaluated successfully")
        )

        val summary = summarizeAuditEntries(entries)

        assertEquals(4, summary.total)
        assertEquals(2, summary.ready)
        assertEquals(1, summary.blocked)
        assertEquals(1, summary.unsupported)
    }

    @Test
    fun createsReadableReportText() {
        val entries = listOf(
            AuditEntry("2026-10-13T09:00:00Z", "READY", "Ready for evaluation")
        )

        val text = buildAuditReportText(entries)

        assertTrue(text.contains("SupervisionAPP Local Report"))
        assertTrue(text.contains("READY"))
        assertTrue(text.contains("Ready for evaluation"))
    }
}

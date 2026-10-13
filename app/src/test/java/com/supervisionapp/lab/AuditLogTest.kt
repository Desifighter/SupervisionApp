package com.supervisionapp.lab

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AuditLogTest {
    @Test
    fun appendsNewEntriesInLatestFirstOrder() {
        val entries = listOf(
            AuditEntry(
                timestamp = "2026-10-13T09:00:00Z",
                status = "READY",
                summary = "Current diagnostic set is ready"
            )
        )

        val updated = appendAuditEntry(entries, AuditEntry(
            timestamp = "2026-10-13T09:05:00Z",
            status = "BLOCKED",
            summary = "Usage access missing"
        ))

        assertEquals(2, updated.size)
        assertEquals("BLOCKED", updated.first().status)
        assertEquals("READY", updated[1].status)
    }

    @Test
    fun serializesAuditEntriesForLocalStorage() {
        val entries = listOf(
            AuditEntry(
                timestamp = "2026-10-13T09:00:00Z",
                status = "READY",
                summary = "Ready for guarded evaluation"
            )
        )

        val encoded = encodeAuditEntries(entries)
        val decoded = decodeAuditEntries(encoded)

        assertTrue(encoded.isNotEmpty())
        assertEquals(1, decoded.size)
        assertEquals("READY", decoded.first().status)
    }
}

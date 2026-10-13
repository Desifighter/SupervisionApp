package com.supervisionapp.lab

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PolicyHistoryStoreTest {
    @Test
    fun historyKeepsNewestEvaluationFirst() {
        val first = PolicySnapshot(
            timestamp = "2026-10-13T09:00:00Z",
            status = "BLOCKED",
            summary = "Usage access missing",
            profile = "PERSONAL"
        )
        val second = PolicySnapshot(
            timestamp = "2026-10-13T09:05:00Z",
            status = "READY",
            summary = "All checks passed",
            profile = "WORK"
        )

        val history = appendPolicySnapshot(listOf(first), second)

        assertEquals(2, history.size)
        assertEquals("READY", history.first().status)
        assertEquals("BLOCKED", history.last().status)
    }

    @Test
    fun historySerializesAndDeserializesCleanly() {
        val history = listOf(
            PolicySnapshot(
                timestamp = "2026-10-13T09:00:00Z",
                status = "READY",
                summary = "Ready for guarded evaluation",
                profile = "WORK"
            )
        )

        val encoded = encodePolicySnapshots(history)
        val decoded = decodePolicySnapshots(encoded)

        assertTrue(encoded.isNotEmpty())
        assertEquals(1, decoded.size)
        assertEquals("READY", decoded.first().status)
    }
}

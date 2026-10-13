package com.supervisionapp.lab

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

class PersonalUsageTrackerTest {

    @Test
    fun windowStartUsesFourPmIstReset() {
        val now = ZonedDateTime.of(
            LocalDateTime.of(2026, 10, 13, 18, 30),
            ZoneId.of("Asia/Kolkata")
        )
        val start = PersonalUsageTracker.calculateWindowStart(now)
        assertEquals(2026, start.year)
        assertEquals(10, start.month.value)
        assertEquals(13, start.dayOfMonth)
        assertEquals(16, start.hour)
        assertEquals(0, start.minute)
    }

    @Test
    fun usageMinutesAreCappedAtDailyLimit() {
        val usage = PersonalUsageTracker.computeUsageMinutes(45, 30)
        assertEquals(30, usage)
    }

    @Test
    fun beforeFourPmWindowStaysOnPreviousDayReset() {
        val now = ZonedDateTime.of(
            LocalDateTime.of(2026, 10, 13, 15, 59),
            ZoneId.of("Asia/Kolkata")
        )
        val start = PersonalUsageTracker.calculateWindowStart(now)
        assertTrue(start.isBefore(now))
        assertEquals(16, start.hour)
    }
}

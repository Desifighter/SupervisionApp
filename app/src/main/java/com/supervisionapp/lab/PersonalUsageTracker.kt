package com.supervisionapp.lab

import android.app.usage.UsageStatsManager
import android.content.Context
import java.time.Duration
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import kotlin.math.min

object PersonalUsageTracker {
    private val IST_ZONE = ZoneId.of("Asia/Kolkata")

    fun calculateWindowStart(now: ZonedDateTime): ZonedDateTime {
        val localNow = now.withZoneSameInstant(IST_ZONE)
        val resetToday = localNow.toLocalDate().atTime(16, 0).atZone(IST_ZONE)
        return if (localNow.isBefore(resetToday)) {
            resetToday.minusDays(1)
        } else {
            resetToday
        }
    }

    fun computeUsageMinutes(usedMinutes: Int, dailyLimit: Int): Int = min(usedMinutes, dailyLimit)

    fun isWithinInstagramWindow(now: ZonedDateTime): Boolean {
        val localNow = now.withZoneSameInstant(IST_ZONE)
        return localNow.hour >= 16
    }

    fun usageWindowFor(now: ZonedDateTime): Pair<ZonedDateTime, ZonedDateTime> {
        val start = calculateWindowStart(now)
        val end = now.withZoneSameInstant(IST_ZONE)
        return start to end
    }

    fun getInstagramUsageMinutes(context: Context, now: ZonedDateTime = ZonedDateTime.now(IST_ZONE)): Int {
        val usageManager = context.getSystemService(UsageStatsManager::class.java) ?: return 0
        val windowStart = calculateWindowStart(now)
        val startMs = windowStart.toInstant().toEpochMilli()
        val endMs = now.toInstant().toEpochMilli()

        val stats = usageManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startMs,
            endMs
        ) ?: return 0

        val instagram = stats.filter { it.packageName == "com.instagram.android" }
        val totalMs = instagram.sumOf { it.totalTimeInForeground }
        val minutes = Duration.ofMillis(totalMs).toMinutes().toInt()
        return minutes.coerceAtLeast(0)
    }

    fun getRestrictionDecision(usedMinutes: Int, dailyLimit: Int = 30, now: ZonedDateTime = ZonedDateTime.now(IST_ZONE)): String {
        val capped = computeUsageMinutes(usedMinutes, dailyLimit)
        return if (!isWithinInstagramWindow(now)) {
            "Instagram is not available until 4:00 PM IST."
        } else if (capped >= dailyLimit) {
            "Instagram quota reached. Daily reset is at 4:00 PM IST."
        } else {
            "Instagram is available for ${dailyLimit - capped} minutes today."
        }
    }
}

package com.supervisionapp.lab

import java.net.URI
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.math.max

private val IST_ZONE = ZoneId.of("Asia/Kolkata")

private val socialPackages = listOf(
    "com.instagram.android",
    "com.facebook.katana",
    "com.facebook.orca",
    "com.snapchat.android",
    "com.twitter.android",
    "com.reddit.frontpage",
    "com.linkedin.android",
    "com.pinterest",
    "com.zhiliaoapp.musically",
    "com.ss.android.ugc.trill",
    "com.google.android.youtube"
)

private val blockedDomains = listOf(
    "pornhub.com",
    "xvideos.com",
    "xnxx.com",
    "redtube.com",
    "youporngay.com",
    "onlyfans.com",
    "sex.com",
    "tube8.com",
    "spankbang.com",
    "playboy.com",
    "livejasmin.com",
    "brazzers.com",
    "myfreecams.com",
    "r18.com",
    "javhd.com",
    "ebay.com"
)

private val softPornKeywords = listOf(
    "porn",
    "sex",
    "xxx",
    "adult",
    "milf",
    "hentai",
    "camgirl",
    "lesbian",
    "nude",
    "fetish",
    "horny",
    "threesome",
    "shemale",
    "onlyfans",
    "teens",
    "playboy"
)

private val blockedKeywords = softPornKeywords + listOf("pornhub", "xvideos", "xnxx", "redtube")

data class AppAccessDecision(
    val packageName: String,
    val isAllowed: Boolean,
    val remainingMinutes: Int,
    val reason: String
)

data class UrlBlockDecision(
    val isBlocked: Boolean,
    val category: String,
    val matchedPattern: String,
    val message: String
)

data class PackageBlockDecision(
    val isBlocked: Boolean,
    val category: String,
    val message: String
)

object PersonalSupervisionRuleEngine {
    const val DAILY_MINUTES = 30
    const val RESET_HOUR_IST = 16
    const val RESET_MINUTE_IST = 0

    fun nowInIst(): ZonedDateTime = ZonedDateTime.now(IST_ZONE)

    fun resetTimeFor(now: ZonedDateTime = nowInIst()): ZonedDateTime {
        val local = now.withZoneSameInstant(IST_ZONE)
        val todayReset = local.toLocalDate().atTime(RESET_HOUR_IST, RESET_MINUTE_IST).atZone(IST_ZONE)
        return if (local.isBefore(todayReset)) {
            todayReset.minusDays(1)
        } else {
            todayReset
        }
    }

    fun remainingMinutes(now: ZonedDateTime, usedMinutes: Int): Int {
        return max(0, DAILY_MINUTES - usedMinutes)
    }

    fun evaluateAppAccess(
        packageName: String,
        usedMinutes: Int,
        now: ZonedDateTime = nowInIst()
    ): AppAccessDecision {
        val localNow = now.withZoneSameInstant(IST_ZONE)
        val isInstagram = packageName == "com.instagram.android"

        if (isInstagram) {
            val allowedAfterFourPm = localNow.hour >= RESET_HOUR_IST
            if (!allowedAfterFourPm) {
                return AppAccessDecision(
                    packageName = packageName,
                    isAllowed = false,
                    remainingMinutes = remainingMinutes(localNow, usedMinutes),
                    reason = "Instagram is only allowed after 4:00 PM IST. Current time is ${localNow.toLocalTime()}."
                )
            }

            val quotaRemaining = remainingMinutes(localNow, usedMinutes)
            return if (usedMinutes >= DAILY_MINUTES) {
                AppAccessDecision(
                    packageName = packageName,
                    isAllowed = false,
                    remainingMinutes = 0,
                    reason = "Instagram quota reached. The daily allowance resets at 4:00 PM IST."
                )
            } else {
                AppAccessDecision(
                    packageName = packageName,
                    isAllowed = true,
                    remainingMinutes = quotaRemaining,
                    reason = "Instagram is available within the daily window. Remaining quota: ${quotaRemaining} minutes."
                )
            }
        }

        val packageDecision = evaluatePackage(packageName)
        if (packageDecision.isBlocked) {
            return AppAccessDecision(
                packageName = packageName,
                isAllowed = false,
                remainingMinutes = remainingMinutes(localNow, usedMinutes),
                reason = packageDecision.message
            )
        }

        return AppAccessDecision(
            packageName = packageName,
            isAllowed = true,
            remainingMinutes = remainingMinutes(localNow, usedMinutes),
            reason = "App is not in the restricted social blocklist."
        )
    }

    fun evaluatePackage(packageName: String): PackageBlockDecision {
        val lowerPackage = packageName.lowercase()
        val blockedByPackage = socialPackages.firstOrNull { it.lowercase() == lowerPackage }
        return if (blockedByPackage != null) {
            PackageBlockDecision(
                isBlocked = true,
                category = "social",
                message = "Blocked by the social media rule list."
            )
        } else {
            PackageBlockDecision(
                isBlocked = false,
                category = "allowed",
                message = "Allowed package."
            )
        }
    }

    fun evaluateUrl(rawUrl: String): UrlBlockDecision {
        val normalized = rawUrl.lowercase()
        val host = try {
            URI(rawUrl).host?.lowercase() ?: rawUrl.lowercase()
        } catch (_: Exception) {
            rawUrl.lowercase()
        }

        val domainMatch = blockedDomains.firstOrNull { host.contains(it, ignoreCase = true) }
        if (domainMatch != null) {
            return UrlBlockDecision(
                isBlocked = true,
                category = "adult",
                matchedPattern = domainMatch,
                message = "Blocked because the domain matches the adult content list."
            )
        }

        val keywordMatch = blockedKeywords.firstOrNull { normalized.contains(it, ignoreCase = true) }
        if (keywordMatch != null) {
            return UrlBlockDecision(
                isBlocked = true,
                category = "adult",
                matchedPattern = keywordMatch,
                message = "Blocked because the URL contains a porn or soft-porn keyword."
            )
        }

        return UrlBlockDecision(
            isBlocked = false,
            category = "allowed",
            matchedPattern = "",
            message = "URL is not in the blocked list."
        )
    }
}

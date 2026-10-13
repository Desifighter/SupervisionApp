package com.supervisionapp.lab

import java.net.URI

object PersonalBlockerEngine {
    private val restrictedPackages = listOf(
        "com.instagram.android",
        "com.facebook.katana",
        "com.facebook.orca",
        "com.snapchat.android",
        "com.twitter.android",
        "com.reddit.frontpage",
        "com.youtube.android",
        "com.xvideos.",
        "com.pornhub."
    )

    private val restrictedDomains = listOf(
        "pornhub.com",
        "xvideos.com",
        "xnxx.com",
        "redtube.com",
        "spankbang.com",
        "tube8.com",
        "sex.com",
        "onlyfans.com",
        "brazzers.com",
        "livejasmin.com",
        "playboy.com",
        "r18.com"
    )

    private val adultKeywordHints = listOf(
        "porn",
        "sex",
        "xxx",
        "adult",
        "nude",
        "milf",
        "hentai",
        "lesbian",
        "fetish",
        "camgirl",
        "onlyfans"
    )

    data class BlockDecision(
        val isBlocked: Boolean,
        val reason: String,
        val category: String,
        val matchedValue: String = ""
    )

    fun evaluatePackage(packageName: String): BlockDecision {
        val normalized = packageName.lowercase()
        val matched = restrictedPackages.firstOrNull { normalized.contains(it.lowercase()) }
        return if (matched != null) {
            BlockDecision(
                isBlocked = true,
                reason = "Blocked by local social and adult-content policy.",
                category = "restricted_app",
                matchedValue = matched
            )
        } else {
            BlockDecision(
                isBlocked = false,
                reason = "Allowed by current local rules.",
                category = "allowed_app",
                matchedValue = packageName
            )
        }
    }

    fun evaluateUrl(rawUrl: String): BlockDecision {
        val normalized = rawUrl.lowercase()
        val host = try {
            URI(rawUrl).host?.lowercase() ?: normalized
        } catch (_: Exception) {
            normalized
        }

        val domainMatch = restrictedDomains.firstOrNull { host.contains(it, ignoreCase = true) }
        if (domainMatch != null) {
            return BlockDecision(
                isBlocked = true,
                reason = "Adult domain blocked by local filter list.",
                category = "adult_domain",
                matchedValue = domainMatch
            )
        }

        val keywordMatch = adultKeywordHints.firstOrNull { normalized.contains(it, ignoreCase = true) }
        if (keywordMatch != null) {
            return BlockDecision(
                isBlocked = true,
                reason = "Adult-content keyword detected in URL.",
                category = "adult_keyword",
                matchedValue = keywordMatch
            )
        }

        return BlockDecision(
            isBlocked = false,
            reason = "URL is not blocked by local adult-content policy.",
            category = "allowed_url",
            matchedValue = rawUrl
        )
    }
}

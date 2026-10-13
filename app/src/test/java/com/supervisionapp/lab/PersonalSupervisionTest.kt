package com.supervisionapp.lab

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

class PersonalSupervisionTest {

    @Test
    fun instagramQuotaResetsAtFourPmIst() {
        val beforeReset = ZonedDateTime.of(
            LocalDateTime.of(2026, 10, 13, 15, 59),
            ZoneId.of("Asia/Kolkata")
        )
        val afterReset = ZonedDateTime.of(
            LocalDateTime.of(2026, 10, 13, 16, 0),
            ZoneId.of("Asia/Kolkata")
        )

        assertEquals(30, PersonalSupervisionRuleEngine.remainingMinutes(beforeReset, 0))
        assertEquals(30, PersonalSupervisionRuleEngine.remainingMinutes(afterReset, 0))
    }

    @Test
    fun quotaBlocksInstagramAfterThirtyMinutes() {
        val decision = PersonalSupervisionRuleEngine.evaluateAppAccess(
            packageName = "com.instagram.android",
            usedMinutes = 30,
            now = ZonedDateTime.of(
                LocalDateTime.of(2026, 10, 13, 18, 0),
                ZoneId.of("Asia/Kolkata")
            )
        )

        assertFalse(decision.isAllowed)
        assertEquals(0, decision.remainingMinutes)
        assertTrue(decision.reason.contains("Instagram quota"))
    }

    @Test
    fun pornAndSoftPornDomainsAreBlocked() {
        val pornResult = PersonalSupervisionRuleEngine.evaluateUrl("https://www.pornhub.com/video")
        val softPornResult = PersonalSupervisionRuleEngine.evaluateUrl("https://www.xvideos.com")

        assertTrue(pornResult.isBlocked)
        assertTrue(softPornResult.isBlocked)
    }

    @Test
    fun socialMediaAppsAreBlockedByPackageList() {
        val blocked = PersonalSupervisionRuleEngine.evaluatePackage("com.facebook.katana")
        val allowed = PersonalSupervisionRuleEngine.evaluatePackage("com.whatsapp")

        assertTrue(blocked.isBlocked)
        assertFalse(allowed.isBlocked)
    }
}

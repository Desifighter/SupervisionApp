package com.supervisionapp.lab

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PersonalBlockerEngineTest {

    @Test
    fun blockedPackagesAreRejected() {
        val result = PersonalBlockerEngine.evaluatePackage("com.instagram.android")
        assertTrue(result.isBlocked)
    }

    @Test
    fun allowedPackageIsNotBlocked() {
        val result = PersonalBlockerEngine.evaluatePackage("com.google.android.apps.messaging")
        assertFalse(result.isBlocked)
    }

    @Test
    fun restrictedDomainsAreBlocked() {
        val result = PersonalBlockerEngine.evaluateUrl("https://www.pornhub.com/video")
        assertTrue(result.isBlocked)
    }
}

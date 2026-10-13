package com.supervisionapp.lab

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ConsentPolicyTest {
    @Test
    fun onboardingIsRequiredUntilConsentIsAccepted() {
        assertTrue(shouldShowOnboarding(false))
        assertFalse(shouldShowOnboarding(true))
    }
}

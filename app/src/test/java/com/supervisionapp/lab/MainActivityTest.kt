package com.supervisionapp.lab

import org.junit.Assert.assertEquals
import org.junit.Test

class MainActivityTest {
    @Test
    fun vpnStateWithoutNetworkPermissionIsUnavailable() {
        val result = resolveVpnState(false)
        assertEquals("UNAVAILABLE", result.first)
        assertEquals("ACCESS_NETWORK_STATE permission not granted", result.second)
    }
}

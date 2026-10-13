package com.supervisionapp.lab

import android.Manifest
import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.os.Process
import android.provider.Settings
import android.os.UserManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private const val CONSENT_PREFERENCES = "phase1_consent"
private const val CONSENT_KEY = "accepted"

internal fun shouldShowOnboarding(consentAccepted: Boolean): Boolean = !consentAccepted

data class DiagnosticsState(
    val androidVersion: String = "UNKNOWN",
    val apiLevel: Int = Build.VERSION.SDK_INT,
    val usageAccess: String = "UNKNOWN",
    val accessibility: String = "UNKNOWN",
    val currentApp: String = "UNKNOWN",
    val profile: String = "UNKNOWN",
    val vpnState: String = "UNKNOWN",
    val vpnPackage: String = "UNAVAILABLE",
    val deviceOwner: String = "UNKNOWN",
    val profileOwner: String = "UNKNOWN",
    val packageVisibility: String = "UNKNOWN",
    val monitoring: String = "NOT STARTED"
)

private fun isUsageAccessGranted(context: Context): Boolean {
    val appOps = context.getSystemService(AppOpsManager::class.java) ?: return false
    val mode = appOps.checkOpNoThrow(
        AppOpsManager.OPSTR_GET_USAGE_STATS,
        Process.myUid(),
        context.packageName
    )
    return mode == AppOpsManager.MODE_ALLOWED
}

private fun isAccessibilityEnabled(context: Context): Boolean {
    val enabledServices = Settings.Secure.getString(
        context.contentResolver,
        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
    ) ?: return false

    val accessibilityServiceName = "${context.packageName}/${SupervisionAccessibilityService::class.java.name}"
    return enabledServices.split(':').any { it == accessibilityServiceName }
}

private fun getCurrentForegroundApp(context: Context): String {
    val usageStatsManager = context.getSystemService(UsageStatsManager::class.java)
        ?: return "UNAVAILABLE"
    val end = System.currentTimeMillis()
    val start = end - 60L * 60L * 1000L

    val stats = usageStatsManager.queryUsageStats(
        UsageStatsManager.INTERVAL_DAILY,
        start,
        end
    ) ?: return "UNAVAILABLE"

    val top = stats.maxByOrNull { it.lastTimeUsed } ?: return "UNKNOWN"
    val packageName = top.packageName

    val label = try {
        context.packageManager.getApplicationLabel(
            context.packageManager.getApplicationInfo(packageName, 0)
        ).toString()
    } catch (_: Exception) {
        packageName
    }

    return "$label ($packageName)"
}

private fun getProfileType(context: Context): String {
    val userManager = context.getSystemService(UserManager::class.java)
    return when {
        userManager?.isManagedProfile == true -> "WORK"
        userManager != null -> "PERSONAL"
        else -> "UNKNOWN"
    }
}

internal fun resolveVpnState(hasPermission: Boolean): Pair<String, String> {
    if (!hasPermission) {
        return "UNAVAILABLE" to "ACCESS_NETWORK_STATE permission not granted"
    }

    return "DISCONNECTED" to "UNAVAILABLE"
}

private fun getVpnState(context: Context): Pair<String, String> {
    val hasPermission = context.checkSelfPermission(Manifest.permission.ACCESS_NETWORK_STATE) ==
        PackageManager.PERMISSION_GRANTED

    if (!hasPermission) {
        return resolveVpnState(false)
    }

    return try {
        val connectivityManager = context.getSystemService(ConnectivityManager::class.java)
        val caps = connectivityManager?.getNetworkCapabilities(connectivityManager.activeNetwork)
        val isVpn = caps?.hasTransport(NetworkCapabilities.TRANSPORT_VPN) == true

        if (isVpn) {
            "CONNECTED" to "VPN active: Android exposes transport state, but package identity is not reliably available to a normal app"
        } else {
            "DISCONNECTED" to "UNAVAILABLE"
        }
    } catch (_: SecurityException) {
        resolveVpnState(false)
    }
}

private fun getDeviceOwnerState(context: Context): String {
    val devicePolicyManager = context.getSystemService(DevicePolicyManager::class.java)
    return if (devicePolicyManager?.isDeviceOwnerApp(context.packageName) == true) {
        "YES"
    } else {
        "NO"
    }
}

private fun getProfileOwnerState(context: Context): String {
    val devicePolicyManager = context.getSystemService(DevicePolicyManager::class.java)
    return if (devicePolicyManager?.isProfileOwnerApp(context.packageName) == true) {
        "YES"
    } else {
        "NO"
    }
}

private fun getPackageVisibilityState(): String {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        "RESTRICTED BY ANDROID 11+ PACKAGE VISIBILITY RULES"
    } else {
        "AVAILABLE"
    }
}

private fun buildDiagnostics(context: Context): DiagnosticsState {
    val usageAccess = if (isUsageAccessGranted(context)) "GRANTED" else "NOT GRANTED"
    val accessibility = if (isAccessibilityEnabled(context)) "ENABLED" else "DISABLED"
    val vpnState = getVpnState(context)

    return DiagnosticsState(
        androidVersion = Build.VERSION.RELEASE,
        apiLevel = Build.VERSION.SDK_INT,
        usageAccess = usageAccess,
        accessibility = accessibility,
        currentApp = getCurrentForegroundApp(context),
        profile = getProfileType(context),
        vpnState = vpnState.first,
        vpnPackage = vpnState.second,
        deviceOwner = getDeviceOwnerState(context),
        profileOwner = getProfileOwnerState(context),
        packageVisibility = getPackageVisibilityState(),
        monitoring = "RUNNING"
    )
}

@Composable
fun ConsentOnboardingScreen(onAccept: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "Phase 1: Safe Observability",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "SupervisionAPP is designed to validate device capabilities before any enforcement step.",
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "This app does not claim protection beyond what Android grants. It does not manipulate the Work Profile or managed security controls.",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Current mode: observation, diagnostics, and policy evaluation only.",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onAccept,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("I understand and continue")
            }
        }
    }
}

@Composable
fun DiagnosticsScreen() {
    val context = LocalContext.current
    var state by remember { mutableStateOf(buildDiagnostics(context)) }

    LaunchedEffect(context) {
        state = buildDiagnostics(context)
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "SupervisionAPP Lab",
                    style = MaterialTheme.typography.headlineMedium
                )
            }
            item { DiagnosticRow(label = "Mode", value = "OBSERVATION_ONLY") }
            item { DiagnosticRow(label = "Android", value = "${state.androidVersion} / API ${state.apiLevel}") }
            item { DiagnosticRow(label = "Usage Access", value = state.usageAccess) }
            item { DiagnosticRow(label = "Accessibility", value = state.accessibility) }
            item { DiagnosticRow(label = "Current Application", value = state.currentApp) }
            item { DiagnosticRow(label = "Current Profile", value = state.profile) }
            item { DiagnosticRow(label = "VPN", value = state.vpnState) }
            item { DiagnosticRow(label = "VPN Package", value = state.vpnPackage) }
            item { DiagnosticRow(label = "Device Owner", value = state.deviceOwner) }
            item { DiagnosticRow(label = "Profile Owner", value = state.profileOwner) }
            item { DiagnosticRow(label = "Package Visibility", value = state.packageVisibility) }
            item { DiagnosticRow(label = "Monitoring", value = state.monitoring) }
            item {
                Text(
                    text = "This app performs diagnostics only. It does not enforce restrictions or manipulate the Work Profile.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun DiagnosticRow(label: String, value: String) {
    Surface(
        tonalElevation = 1.dp,
        shadowElevation = 0.dp,
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label)
            Text(value)
        }
    }
}

@Composable
fun ConsentGateScreen() {
    val context = LocalContext.current
    val sharedPreferences = remember { context.getSharedPreferences(CONSENT_PREFERENCES, Context.MODE_PRIVATE) }
    var consentAccepted by remember { mutableStateOf(sharedPreferences.getBoolean(CONSENT_KEY, false)) }

    if (shouldShowOnboarding(consentAccepted)) {
        ConsentOnboardingScreen(
            onAccept = {
                sharedPreferences.edit().putBoolean(CONSENT_KEY, true).apply()
                consentAccepted = true
            }
        )
    } else {
        DiagnosticsScreen()
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                ConsentGateScreen()
            }
        }
    }
}

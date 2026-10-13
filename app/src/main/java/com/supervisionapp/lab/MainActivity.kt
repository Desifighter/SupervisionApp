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
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import kotlinx.coroutines.launch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
    val policyResults = remember(state) { evaluatePolicyRules(state) }
    val guardedAction = remember(state) { evaluateGuardedAction(state) }
    val deploymentReadiness = remember(state) { evaluateDeploymentReadiness(state) }
    val statusBundle = remember(state) { buildStatusBundle(state) }
    val reviewExport = remember(state) { buildReviewExport(state) }
    val readinessPacket = remember(state) { buildReadinessPacket(state) }
    val executiveBriefing = remember(state) { buildExecutiveBriefing(state) }
    val decisionLog = remember(state) { buildDecisionLog(state) }
    val recommendationSheet = remember(state) { buildRecommendationSheet(state) }
    val riskRegister = remember(state) { buildRiskRegister(state) }
    val auditEntries = remember(state) {
        val status = guardedAction.status.name
        val summary = guardedAction.reason
        val timestamp = java.time.Instant.now().toString()
        appendAuditEntry(
            emptyList(),
            AuditEntry(timestamp = timestamp, status = status, summary = summary)
        )
    }
    val auditSummary = remember(auditEntries) { summarizeAuditEntries(auditEntries) }
    val auditReportText = remember(auditEntries) { buildAuditReportText(auditEntries) }

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
                    text = "Phase 2: Policy Evaluation",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            items(policyResults) { result ->
                PolicyRow(result = result)
            }

            item {
                Text(
                    text = "Phase 3: Guarded Action Gate",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                GuardedActionRow(result = guardedAction)
            }

            item {
                Text(
                    text = "Phase 4: Local Audit Trail",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                Text(
                    text = "Local report summary: ${auditSummary.total} entries, READY=${auditSummary.ready}, BLOCKED=${auditSummary.blocked}, UNSUPPORTED=${auditSummary.unsupported}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            item {
                Text(
                    text = auditReportText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            item {
                Text(
                    text = "Phase 5: Deployment Readiness Review",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                DeploymentReadinessRow(result = deploymentReadiness)
            }

            item {
                StatusBundleRow(bundle = statusBundle)
            }

            item {
                ReviewExportRow(exportText = reviewExport)
            }

            item {
                ReadinessPacketRow(packetText = readinessPacket)
            }

            item {
                ExecutiveBriefingRow(briefingText = executiveBriefing)
            }

            item {
                DecisionLogRow(logText = decisionLog)
            }

            item {
                RecommendationSheetRow(sheetText = recommendationSheet)
            }

            item {
                RiskRegisterRow(registerText = riskRegister)
            }

            items(auditEntries) { entry ->
                AuditEntryRow(entry = entry)
            }

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
private fun PolicyRow(result: PolicyEvaluation) {
    Surface(
        tonalElevation = 1.dp,
        shadowElevation = 0.dp,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(result.rule.title)
                Text(result.status.name)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = result.reason,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun GuardedActionRow(result: GuardedActionEvaluation) {
    Surface(
        tonalElevation = 1.dp,
        shadowElevation = 0.dp,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(result.action.title)
                Text(result.status.name)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = result.reason,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DeploymentReadinessRow(result: DeploymentReadiness) {
    Surface(
        tonalElevation = 1.dp,
        shadowElevation = 0.dp,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Deployment Status")
                Text(result.state)
            }
            Spacer(modifier = Modifier.height(8.dp))
            result.warnings.forEach { warning ->
                Text(
                    text = "• $warning",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun StatusBundleRow(bundle: StatusBundle) {
    Surface(
        tonalElevation = 1.dp,
        shadowElevation = 0.dp,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = bundle.summary,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Policy: ${bundle.policySummary}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Warnings: ${bundle.warnings.joinToString(separator = "; ")}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ReviewExportRow(exportText: String) {
    Surface(
        tonalElevation = 1.dp,
        shadowElevation = 0.dp,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "SupervisionAPP Review Export",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = exportText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ReadinessPacketRow(packetText: String) {
    Surface(
        tonalElevation = 1.dp,
        shadowElevation = 0.dp,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "SupervisionAPP Readiness Packet",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = packetText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ExecutiveBriefingRow(briefingText: String) {
    Surface(
        tonalElevation = 1.dp,
        shadowElevation = 0.dp,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "SupervisionAPP Executive Briefing",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = briefingText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DecisionLogRow(logText: String) {
    Surface(
        tonalElevation = 1.dp,
        shadowElevation = 0.dp,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Decision Log",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = logText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun RecommendationSheetRow(sheetText: String) {
    Surface(
        tonalElevation = 1.dp,
        shadowElevation = 0.dp,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Recommendation Sheet",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = sheetText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun RiskRegisterRow(registerText: String) {
    Surface(
        tonalElevation = 1.dp,
        shadowElevation = 0.dp,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Risk Register",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = registerText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AuditEntryRow(entry: AuditEntry) {
    Surface(
        tonalElevation = 1.dp,
        shadowElevation = 0.dp,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(entry.status)
                Text(entry.timestamp)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = entry.summary,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
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
        PersonalSupervisionDashboard()
    }
}

@Composable
fun PersonalSupervisionDashboard() {
    val context = LocalContext.current
    val settingsStore = remember { PersonalSettingsStore(context.applicationContext) }
    var settings by remember { mutableStateOf(PersonalRuleSettings()) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        settings = settingsStore.read()
    }

    val now = remember(settings) { PersonalSupervisionRuleEngine.nowInIst() }
    val actualInstagramUsage = remember(context, now) {
        PersonalUsageTracker.getInstagramUsageMinutes(context, now)
    }
    val effectiveUsedMinutes = remember(actualInstagramUsage, settings.instagramUsedMinutes) {
        maxOf(actualInstagramUsage, settings.instagramUsedMinutes)
    }
    val instagramStatus = remember(effectiveUsedMinutes, now) {
        PersonalSupervisionRuleEngine.evaluateAppAccess(
            packageName = "com.instagram.android",
            usedMinutes = effectiveUsedMinutes,
            now = now
        )
    }
    val blockedApps = remember {
        listOf(
            "com.instagram.android",
            "com.facebook.katana",
            "com.facebook.orca",
            "com.snapchat.android",
            "com.twitter.android",
            "com.reddit.frontpage",
            "com.youtube.android"
        )
    }
    val sampleUrls = remember {
        listOf(
            "https://www.instagram.com/",
            "https://www.pornhub.com/video",
            "https://www.xvideos.com/video123",
            "https://www.youtube.com/watch?v=abc",
            "https://www.google.com"
        )
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
                    text = "SupervisionAPP",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                Text(
                    text = "Personal focus mode",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            item {
                Surface(
                    tonalElevation = 2.dp,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text("Instagram daily quota")
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Limit")
                            Text("${settings.instagramDailyLimitMinutes} min / day")
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Used")
                            Text("${effectiveUsedMinutes} min")
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Remaining")
                            Text("${instagramStatus.remainingMinutes} min")
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Reset")
                            Text("${settings.resetHour}:00 PM IST")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = instagramStatus.reason,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    scope.launch {
                                        val next = (settings.instagramUsedMinutes + 5).coerceAtMost(settings.instagramDailyLimitMinutes)
                                        settings = settings.copy(instagramUsedMinutes = next)
                                        settingsStore.save(settings)
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Add 5 min")
                            }
                            Button(
                                onClick = {
                                    scope.launch {
                                        settings = settings.copy(instagramUsedMinutes = 0)
                                        settingsStore.save(settings)
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Reset day")
                            }
                        }
                    }
                }
            }

            item {
                Surface(
                    tonalElevation = 1.dp,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Blocked social apps",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        blockedApps.forEach { appPackage ->
                            val decision = PersonalSupervisionRuleEngine.evaluatePackage(appPackage)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(appPackage)
                                Text(if (decision.isBlocked) "BLOCKED" else "ALLOWED")
                            }
                        }
                    }
                }
            }

            item {
                Surface(
                    tonalElevation = 1.dp,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Adult content filter",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        sampleUrls.forEach { url ->
                            val decision = PersonalSupervisionRuleEngine.evaluateUrl(url)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(url)
                                Text(if (decision.isBlocked) "BLOCK" else "ALLOW")
                            }
                            Text(
                                text = decision.message,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }
                }
            }

            item {
                Surface(
                    tonalElevation = 1.dp,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Restriction layer",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        val blockedInstagram = PersonalBlockerEngine.evaluatePackage("com.instagram.android")
                        val blockedPinterest = PersonalBlockerEngine.evaluatePackage("com.pinterest")
                        val blockedUrl = PersonalBlockerEngine.evaluateUrl("https://www.pornhub.com/video")
                        Text("• Instagram block decision: ${if (blockedInstagram.isBlocked) "BLOCKED" else "ALLOW"}")
                        Text("• Pinterest block decision: ${if (blockedPinterest.isBlocked) "BLOCKED" else "ALLOW"}")
                        Text("• Pornhub URL decision: ${if (blockedUrl.isBlocked) "BLOCKED" else "ALLOW"}")
                        Text("• Local rule engine: active")
                        Text("• Android enforcement boundary: remains platform-limited")
                    }
                }
            }

            item {
                Surface(
                    tonalElevation = 1.dp,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Personal rules",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("• Instagram: ${settings.instagramDailyLimitMinutes} minutes per day after ${settings.resetHour}:00 PM IST")
                        Text("• Actual tracked usage: ${actualInstagramUsage} minutes")
                        Text("• Porn and soft-porn domains are filtered and blocked")
                        Text("• Social apps default to restricted mode")
                        Text("• App and URL policy is local and reviewable")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Storage: local preferences enabled",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                PersonalSupervisionDashboard()
            }
        }
    }
}

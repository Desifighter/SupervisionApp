package com.supervisionapp.lab

fun buildReadinessPacket(diagnostics: DiagnosticsState): String {
    val readiness = evaluateDeploymentReadiness(diagnostics)
    val statusBundle = buildStatusBundle(diagnostics)

    return buildString {
        appendLine("SupervisionAPP Readiness Packet")
        appendLine("==============================")
        appendLine("Deployment State: ${readiness.state}")
        appendLine("Profile: ${diagnostics.profile}")
        appendLine("Usage Access: ${diagnostics.usageAccess}")
        appendLine("Accessibility: ${diagnostics.accessibility}")
        appendLine("Device Owner: ${diagnostics.deviceOwner}")
        appendLine("Profile Owner: ${diagnostics.profileOwner}")
        appendLine("Status Summary: ${statusBundle.summary}")
        appendLine("Policy Summary: ${statusBundle.policySummary}")
        appendLine("Warnings: ${statusBundle.warnings.joinToString(separator = "; ")}")
    }
}

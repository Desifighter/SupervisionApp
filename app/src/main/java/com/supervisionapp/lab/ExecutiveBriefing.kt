package com.supervisionapp.lab

fun buildExecutiveBriefing(diagnostics: DiagnosticsState): String {
    val readiness = evaluateDeploymentReadiness(diagnostics)
    val statusBundle = buildStatusBundle(diagnostics)

    return buildString {
        appendLine("SupervisionAPP Executive Briefing")
        appendLine("================================")
        appendLine("Current state: ${readiness.state}")
        appendLine("Managed profile context: ${diagnostics.profile}")
        appendLine("Usage Access: ${diagnostics.usageAccess}")
        appendLine("Accessibility: ${diagnostics.accessibility}")
        appendLine("Device Owner: ${diagnostics.deviceOwner}")
        appendLine("Profile Owner: ${diagnostics.profileOwner}")
        appendLine("Policy summary: ${statusBundle.policySummary}")
        appendLine("Key blockers: ${statusBundle.warnings.joinToString(separator = "; ")}")
        appendLine("Summary: ${statusBundle.summary}")
    }
}

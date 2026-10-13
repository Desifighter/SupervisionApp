package com.supervisionapp.lab

fun buildRiskRegister(diagnostics: DiagnosticsState): String {
    val readiness = evaluateDeploymentReadiness(diagnostics)
    val warnings = readiness.warnings.ifEmpty { listOf("No active risk blockers detected.") }

    return buildString {
        appendLine("Risk Register")
        appendLine("=============")
        appendLine("State: ${readiness.state}")
        appendLine("Owner: Platform capability review")
        appendLine("Summary: ${readiness.state} - ${warnings.joinToString(separator = "; ")}")
        appendLine("Risk: Missing managed-device support may block production enforcement.")
        appendLine("Usage Access: ${diagnostics.usageAccess}")
        appendLine("Accessibility: ${diagnostics.accessibility}")
        appendLine("Current Profile: ${diagnostics.profile}")
    }
}

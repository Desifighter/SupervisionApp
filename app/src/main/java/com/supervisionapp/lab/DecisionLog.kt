package com.supervisionapp.lab

fun buildDecisionLog(diagnostics: DiagnosticsState): String {
    val readiness = evaluateDeploymentReadiness(diagnostics)
    val statusBundle = buildStatusBundle(diagnostics)

    return buildString {
        appendLine("Decision Log")
        appendLine("============")
        appendLine("State: ${readiness.state}")
        appendLine("Reason: ${statusBundle.summary}")
        appendLine("Usage Access: ${diagnostics.usageAccess}")
        appendLine("Accessibility: ${diagnostics.accessibility}")
        appendLine("Current Profile: ${diagnostics.profile}")
        appendLine("Warnings: ${statusBundle.warnings.joinToString(separator = "; ")}")
    }
}

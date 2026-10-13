package com.supervisionapp.lab

fun buildRecommendationSheet(diagnostics: DiagnosticsState): String {
    val readiness = evaluateDeploymentReadiness(diagnostics)
    val statusBundle = buildStatusBundle(diagnostics)

    return buildString {
        appendLine("Recommendation Sheet")
        appendLine("===================")
        appendLine("Current state: ${readiness.state}")
        appendLine("Summary: ${statusBundle.summary}")
        appendLine("Next step: review platform support and managed-profile conditions before any enforcement action.")
        appendLine("Missing or invalid signals: ${statusBundle.warnings.joinToString(separator = "; ")}")
        appendLine("Usage Access: ${diagnostics.usageAccess}")
        appendLine("Accessibility: ${diagnostics.accessibility}")
        appendLine("Current Profile: ${diagnostics.profile}")
    }
}

package com.supervisionapp.lab

fun buildReviewExport(diagnostics: DiagnosticsState): String {
    val readiness = evaluateDeploymentReadiness(diagnostics)
    val statusBundle = buildStatusBundle(diagnostics)
    val policyResults = evaluatePolicyRules(diagnostics)

    return buildString {
        appendLine("SupervisionAPP Review Export")
        appendLine("===========================")
        appendLine("Deployment State: ${readiness.state}")
        appendLine("Current Profile: ${diagnostics.profile}")
        appendLine("Usage Access: ${diagnostics.usageAccess}")
        appendLine("Accessibility: ${diagnostics.accessibility}")
        appendLine("Device Owner: ${diagnostics.deviceOwner}")
        appendLine("Profile Owner: ${diagnostics.profileOwner}")
        appendLine("Summary: ${statusBundle.summary}")
        appendLine("Policy: ${statusBundle.policySummary}")
        appendLine("Warnings: ${statusBundle.warnings.joinToString(separator = "; ")}")
        appendLine("Rules:")
        policyResults.forEach { result ->
            appendLine("- ${result.rule.title}: ${result.status.name}")
        }
    }
}

package com.supervisionapp.lab

data class StatusBundle(
    val deploymentState: String,
    val warnings: List<String>,
    val summary: String,
    val policySummary: String,
    val managedContext: String
)

fun buildStatusBundle(diagnostics: DiagnosticsState): StatusBundle {
    val readiness = evaluateDeploymentReadiness(diagnostics)
    val policyResults = evaluatePolicyRules(diagnostics)

    val policySummary = policyResults.joinToString(separator = " | ") { result ->
        "${result.rule.id}:${result.status.name}"
    }

    val warnings = readiness.warnings.ifEmpty { listOf("No warnings") }
    val summary = buildString {
        append("SupervisionAPP Status Bundle: ")
        append(readiness.state)
        append(" | profile=")
        append(diagnostics.profile)
        append(" | usage=")
        append(diagnostics.usageAccess)
        append(" | accessibility=")
        append(diagnostics.accessibility)
    }

    return StatusBundle(
        deploymentState = readiness.state,
        warnings = warnings,
        summary = summary,
        policySummary = policySummary,
        managedContext = diagnostics.profile
    )
}

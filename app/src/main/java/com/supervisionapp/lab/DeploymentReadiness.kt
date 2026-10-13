package com.supervisionapp.lab

data class DeploymentReadiness(
    val state: String,
    val warnings: List<String>,
    val readyForManagedDeployment: Boolean
)

fun evaluateDeploymentReadiness(diagnostics: DiagnosticsState): DeploymentReadiness {
    val warnings = mutableListOf<String>()

    if (diagnostics.usageAccess != "GRANTED") {
        warnings += "Usage access is not granted."
    }

    if (diagnostics.accessibility != "ENABLED") {
        warnings += "Accessibility is not enabled."
    }

    if (diagnostics.profile != "WORK") {
        warnings += "The app is not running in a managed work-profile context."
    }

    if (diagnostics.deviceOwner != "YES") {
        warnings += "Device owner status is not available."
    }

    if (diagnostics.profileOwner != "YES") {
        warnings += "Profile owner status is not available."
    }

    return when {
        diagnostics.usageAccess != "GRANTED" || diagnostics.accessibility != "ENABLED" -> DeploymentReadiness(
            state = "BLOCKED",
            warnings = warnings,
            readyForManagedDeployment = false
        )
        diagnostics.profile == "WORK" && diagnostics.deviceOwner == "YES" && diagnostics.profileOwner == "YES" -> DeploymentReadiness(
            state = "READY_FOR_REVIEW",
            warnings = listOf("Safe for review only. This is not a claim of production enforcement."),
            readyForManagedDeployment = true
        )
        else -> DeploymentReadiness(
            state = "REQUIRES_MANAGED_CONTEXT",
            warnings = warnings,
            readyForManagedDeployment = false
        )
    }
}

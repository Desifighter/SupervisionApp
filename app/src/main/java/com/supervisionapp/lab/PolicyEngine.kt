package com.supervisionapp.lab

enum class PolicyStatus {
    READY,
    BLOCKED,
    UNSUPPORTED
}

data class PolicyRule(
    val id: String,
    val title: String,
    val description: String,
    val requiresUsageAccess: Boolean = false,
    val requiresAccessibility: Boolean = false,
    val requiresManagedProfile: Boolean = false,
    val requiresDeviceOwner: Boolean = false,
    val requiresProfileOwner: Boolean = false
)

data class PolicyEvaluation(
    val rule: PolicyRule,
    val status: PolicyStatus,
    val reason: String
)

fun evaluatePolicyRules(diagnostics: DiagnosticsState): List<PolicyEvaluation> {
    val rules = listOf(
        PolicyRule(
            id = "usage_access",
            title = "Usage Access",
            description = "App can read foreground app usage data.",
            requiresUsageAccess = true
        ),
        PolicyRule(
            id = "accessibility",
            title = "Accessibility",
            description = "Accessibility events and app interaction tracing are available.",
            requiresAccessibility = true
        ),
        PolicyRule(
            id = "managed_profile",
            title = "Managed Profile",
            description = "Work profile and managed-device context are available for policy review.",
            requiresManagedProfile = true
        ),
        PolicyRule(
            id = "device_owner",
            title = "Device Owner",
            description = "The device is owner-managed and supports stronger control paths.",
            requiresDeviceOwner = true
        ),
        PolicyRule(
            id = "profile_owner",
            title = "Profile Owner",
            description = "The work profile is owner-managed for policy enforcement tasks.",
            requiresProfileOwner = true
        )
    )

    return rules.map { rule ->
        val status = when {
            rule.requiresUsageAccess && diagnostics.usageAccess != "GRANTED" -> PolicyStatus.BLOCKED
            rule.requiresAccessibility && diagnostics.accessibility != "ENABLED" -> PolicyStatus.BLOCKED
            rule.requiresManagedProfile && diagnostics.profile != "WORK" -> PolicyStatus.UNSUPPORTED
            rule.requiresDeviceOwner && diagnostics.deviceOwner != "YES" -> PolicyStatus.UNSUPPORTED
            rule.requiresProfileOwner && diagnostics.profileOwner != "YES" -> PolicyStatus.UNSUPPORTED
            else -> PolicyStatus.READY
        }

        val reason = when (status) {
            PolicyStatus.READY -> "Platform capability is available."
            PolicyStatus.BLOCKED -> "Required runtime permission is missing or disabled."
            PolicyStatus.UNSUPPORTED -> "This device or profile does not provide the required support."
        }

        PolicyEvaluation(rule, status, reason)
    }
}

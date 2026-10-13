package com.supervisionapp.lab

enum class PolicyStatus {
    READY,
    BLOCKED,
    UNSUPPORTED
}

enum class GuardedActionStatus {
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

data class GuardedAction(
    val id: String = "guarded_action",
    val title: String = "Reinforced supervision action",
    val description: String = "Only runs after Android support and managed-profile requirements are satisfied."
)

data class GuardedActionEvaluation(
    val action: GuardedAction,
    val status: GuardedActionStatus,
    val reason: String
)

fun evaluateGuardedAction(diagnostics: DiagnosticsState): GuardedActionEvaluation {
    val action = GuardedAction()

    return when {
        diagnostics.usageAccess != "GRANTED" || diagnostics.accessibility != "ENABLED" -> GuardedActionEvaluation(
            action = action,
            status = GuardedActionStatus.BLOCKED,
            reason = "Usage access and accessibility are both required before a supervised action can be considered."
        )
        diagnostics.profile != "WORK" || diagnostics.deviceOwner != "YES" || diagnostics.profileOwner != "YES" -> GuardedActionEvaluation(
            action = action,
            status = GuardedActionStatus.UNSUPPORTED,
            reason = "This device or profile does not provide the managed-device support needed for enforcement."
        )
        else -> GuardedActionEvaluation(
            action = action,
            status = GuardedActionStatus.READY,
            reason = "All required capabilities are present and the action is safe to proceed with a guarded enforcement path."
        )
    }
}

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

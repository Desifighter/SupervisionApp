package com.supervisionapp.lab

data class AuditEntry(
    val timestamp: String,
    val status: String,
    val summary: String
)

data class PolicySnapshot(
    val timestamp: String,
    val status: String,
    val summary: String,
    val profile: String
)

data class AuditSummary(
    val total: Int,
    val ready: Int,
    val blocked: Int,
    val unsupported: Int
)

fun appendAuditEntry(entries: List<AuditEntry>, newEntry: AuditEntry): List<AuditEntry> {
    return listOf(newEntry) + entries
}

fun encodeAuditEntries(entries: List<AuditEntry>): String {
    if (entries.isEmpty()) {
        return ""
    }

    return entries.joinToString(separator = "\n") { entry ->
        "${entry.timestamp} | ${entry.status} | ${entry.summary}"
    }
}

fun decodeAuditEntries(raw: String): List<AuditEntry> {
    if (raw.isBlank()) {
        return emptyList()
    }

    return raw.split("\n")
        .filter { it.isNotBlank() }
        .map { line ->
            val parts = line.split(" | ", limit = 3)
            AuditEntry(
                timestamp = parts.getOrElse(0) { "UNKNOWN" },
                status = parts.getOrElse(1) { "UNKNOWN" },
                summary = parts.getOrElse(2) { "UNKNOWN" }
            )
        }
}

fun appendPolicySnapshot(history: List<PolicySnapshot>, newSnapshot: PolicySnapshot): List<PolicySnapshot> {
    return listOf(newSnapshot) + history
}

fun encodePolicySnapshots(history: List<PolicySnapshot>): String {
    if (history.isEmpty()) {
        return ""
    }

    return history.joinToString(separator = "\n") { snapshot ->
        "${snapshot.timestamp} | ${snapshot.status} | ${snapshot.summary} | ${snapshot.profile}"
    }
}

fun decodePolicySnapshots(raw: String): List<PolicySnapshot> {
    if (raw.isBlank()) {
        return emptyList()
    }

    return raw.split("\n")
        .filter { it.isNotBlank() }
        .map { line ->
            val parts = line.split(" | ", limit = 4)
            PolicySnapshot(
                timestamp = parts.getOrElse(0) { "UNKNOWN" },
                status = parts.getOrElse(1) { "UNKNOWN" },
                summary = parts.getOrElse(2) { "UNKNOWN" },
                profile = parts.getOrElse(3) { "UNKNOWN" }
            )
        }
}

fun summarizeAuditEntries(entries: List<AuditEntry>): AuditSummary {
    val ready = entries.count { it.status == "READY" }
    val blocked = entries.count { it.status == "BLOCKED" }
    val unsupported = entries.count { it.status == "UNSUPPORTED" }

    return AuditSummary(
        total = entries.size,
        ready = ready,
        blocked = blocked,
        unsupported = unsupported
    )
}

fun buildAuditReportText(entries: List<AuditEntry>): String {
    val summary = summarizeAuditEntries(entries)
    val lines = mutableListOf(
        "SupervisionAPP Local Report",
        "Total entries: ${summary.total}",
        "READY: ${summary.ready} | BLOCKED: ${summary.blocked} | UNSUPPORTED: ${summary.unsupported}",
        ""
    )

    entries.forEach { entry ->
        lines += "${entry.timestamp} [${entry.status}] ${entry.summary}"
    }

    return lines.joinToString(separator = "\n")
}

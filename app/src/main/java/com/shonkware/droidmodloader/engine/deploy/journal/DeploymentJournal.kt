package com.shonkware.droidmodloader.engine.deploy.journal

enum class DeploymentJournalStatus {
    STARTED,
    COMPLETED,
    FAILED,
    REVIEWED
}

data class DeploymentJournalTargetState(
    val targetType: String,
    val gameId: String,
    val mode: String,
    val target: String,
    val identityKey: String,
    val manifestFilePath: String,
    val baselineFilePath: String?,
    val backupDirectoryPath: String
) {
    fun toDebugSummary(label: String): String {
        return buildString {
            appendLine("$label:")
            appendLine("  Target type: $targetType")
            appendLine("  Game: $gameId")
            appendLine("  Mode: $mode")
            appendLine("  Target: $target")
            appendLine("  Identity key: $identityKey")
            appendLine("  Manifest: $manifestFilePath")
            appendLine("  Baseline: ${baselineFilePath ?: "not applicable"}")
            appendLine("  Backups: $backupDirectoryPath")
        }
    }
}

data class DeploymentJournalRecord(
    val schemaVersion: Int = 2,
    val operationId: String,
    val gameId: String,
    val profileId: String,
    val status: DeploymentJournalStatus,
    val startedAtEpochMillis: Long,
    val completedAtEpochMillis: Long?,
    val planSummary: DeploymentJournalPlanSummary,
    val resultSummary: DeploymentJournalResultSummary?,
    val failureMessage: String?,
    val dataTarget: DeploymentJournalTargetState? = null,
    val rootTarget: DeploymentJournalTargetState? = null
) {
    fun toDebugSummary(): String {
        return buildString {
            appendLine("Deploy Journal")
            appendLine("  Schema: $schemaVersion")
            appendLine("  Operation ID: $operationId")
            appendLine("  Game: $gameId")
            appendLine("  Profile: $profileId")
            appendLine("  Status: $status")
            appendLine("  Started: $startedAtEpochMillis")
            appendLine("  Completed: ${completedAtEpochMillis ?: "not completed"}")

            if (dataTarget != null || rootTarget != null) {
                appendLine()
                dataTarget?.let { append(it.toDebugSummary("Data target")) }
                rootTarget?.let { append(it.toDebugSummary("Game Root target")) }
            } else {
                appendLine()
                appendLine("Target ownership:")
                appendLine("  Not recorded by this legacy journal schema.")
            }

            appendLine()
            appendLine("Plan:")
            appendLine("  Data operations: ${planSummary.dataOperationCount}")
            appendLine("  Game Root operations: ${planSummary.rootOperationCount}")
            appendLine("  Total operations: ${planSummary.totalOperationCount}")
            appendLine("  Data copy bytes: ${planSummary.dataEstimatedCopyBytes ?: "unknown"}")
            appendLine("  Game Root copy bytes: ${planSummary.rootEstimatedCopyBytes ?: "unknown"}")
            appendLine("  Preflight can deploy: ${planSummary.preflightCanDeploy}")
            appendLine("  Preflight errors: ${planSummary.preflightErrorCount}")
            appendLine("  Preflight warnings: ${planSummary.preflightWarningCount}")

            if (resultSummary != null) {
                appendLine()
                appendLine("Result:")
                appendLine("  Adds: ${resultSummary.addCount}")
                appendLine("  Updates: ${resultSummary.updateCount}")
                appendLine("  Removes: ${resultSummary.removeCount}")
                appendLine("  Backups created: ${resultSummary.backupCount}")
                appendLine("  Backups restored: ${resultSummary.restoreCount}")
                appendLine("  Protected conflicts: ${resultSummary.protectedConflictCount}")
                appendLine("  Final file count: ${resultSummary.finalRecordCount}")
            }

            if (!failureMessage.isNullOrBlank()) {
                appendLine()
                appendLine("Failure:")
                appendLine("  $failureMessage")
            }
        }
    }
}

data class DeploymentJournalPlanSummary(
    val dataOperationCount: Int,
    val rootOperationCount: Int,
    val totalOperationCount: Int,
    val dataEstimatedCopyBytes: Long?,
    val rootEstimatedCopyBytes: Long?,
    val preflightCanDeploy: Boolean,
    val preflightErrorCount: Int,
    val preflightWarningCount: Int
)

data class DeploymentJournalResultSummary(
    val addCount: Int,
    val updateCount: Int,
    val removeCount: Int,
    val backupCount: Int,
    val restoreCount: Int,
    val protectedConflictCount: Int,
    val finalRecordCount: Int
)

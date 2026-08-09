package com.shonkware.droidmodloader.engine.deploy

import java.io.File

internal data class ResolvedDeploymentTarget(
    val targetType: GameTargetType,
    val identity: DeploymentTargetIdentity,
    val deployDirectory: File?,
    val manifestFile: File,
    val backupDirectory: File,
    val baselineFile: File?,
    val validation: GameTargetValidationResult?,
    val unavailableReason: String?
) {
    val canDeploy: Boolean
        get() = deployDirectory != null

    fun requireDeployDirectory(): File {
        return checkNotNull(deployDirectory) {
            buildString {
                append("Resolved ${targetType.displayName} target is unavailable")
                if (!unavailableReason.isNullOrBlank()) {
                    append(": $unavailableReason")
                }
            }
        }
    }

    fun toDebugSummary(): String {
        return buildString {
            appendLine("${targetType.displayName} target context:")
            appendLine("  ${identity.displaySummary()}")
            appendLine("  Deploy directory: ${deployDirectory?.absolutePath ?: "unavailable"}")
            appendLine("  Manifest file: ${manifestFile.name}")
            if (baselineFile != null) {
                appendLine("  Baseline file: ${baselineFile.name}")
            }
            appendLine("  Backup directory: ${backupDirectory.absolutePath}")
            if (!unavailableReason.isNullOrBlank()) {
                appendLine("  Unavailable reason: $unavailableReason")
            }
        }
    }
}

internal data class ResolvedDeploymentTargets(
    val gameId: String,
    val configSnapshot: com.shonkware.droidmodloader.engine.model.GameDeploymentConfig?,
    val data: ResolvedDeploymentTarget,
    val root: ResolvedDeploymentTarget
)

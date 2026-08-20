package com.shonkware.droidmodloader.engine.deploy

import java.io.File

data class ResolvedGameInstallation(
    val gameId: String,
    val gameRootPath: String,
    val dataPath: String
)

data class GameInstallationResolution(
    val installation: ResolvedGameInstallation?,
    val findings: List<GameTargetValidationFinding>
) {
    val isResolved: Boolean
        get() = installation != null &&
            findings.none { it.severity == GameTargetValidationSeverity.ERROR }
}

/**
 * Resolves the normal DML deployment targets from one explicitly selected game
 * installation folder. Path validity and game identity remain authoritative in
 * [GameTargetValidator].
 */
class GameInstallationResolver(
    private val targetValidator: GameTargetValidator = GameTargetValidator()
) {
    fun resolve(
        gameId: String,
        selectedGameRootPath: String
    ): GameInstallationResolution {
        val rootResult = targetValidator.validateTarget(
            gameId = gameId,
            targetType = GameTargetType.GAME_ROOT,
            path = selectedGameRootPath
        )

        if (!rootResult.canDeploy || rootResult.canonicalPath == null) {
            return GameInstallationResolution(
                installation = null,
                findings = rootResult.findings
            )
        }

        val dataCandidate = resolveDataFolder(File(rootResult.canonicalPath))
        val dataResult = targetValidator.validateTarget(
            gameId = gameId,
            targetType = GameTargetType.DATA,
            path = dataCandidate.absolutePath
        )

        val targetFindings = rootResult.findings + dataResult.findings
        if (!dataResult.canDeploy || dataResult.canonicalPath == null) {
            return GameInstallationResolution(
                installation = null,
                findings = targetFindings
            )
        }

        val relationshipFindings = targetValidator.validateRelationship(
            dataResult = dataResult,
            rootResult = rootResult
        )
        val findings = targetFindings + relationshipFindings
        if (relationshipFindings.any { it.severity == GameTargetValidationSeverity.ERROR }) {
            return GameInstallationResolution(
                installation = null,
                findings = findings
            )
        }

        return GameInstallationResolution(
            installation = ResolvedGameInstallation(
                gameId = gameId,
                gameRootPath = rootResult.canonicalPath,
                dataPath = dataResult.canonicalPath
            ),
            findings = findings
        )
    }

    private fun resolveDataFolder(gameRoot: File): File {
        val children = gameRoot.listFiles().orEmpty()
        return children.firstOrNull { child ->
            child.isDirectory && child.name == "Data"
        } ?: children.firstOrNull { child ->
            child.isDirectory && child.name.equals("Data", ignoreCase = true)
        } ?: File(gameRoot, "Data")
    }
}

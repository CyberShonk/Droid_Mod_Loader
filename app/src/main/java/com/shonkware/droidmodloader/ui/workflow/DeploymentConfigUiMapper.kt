package com.shonkware.droidmodloader.ui.workflow

import com.shonkware.droidmodloader.engine.model.GameDeploymentConfig
import com.shonkware.droidmodloader.engine.model.GameProfile

internal object DeploymentConfigUiMapper {
    const val DATA_FOLDER_NOT_DETECTED = "Not detected yet"
    const val GAME_FOLDER_NOT_SELECTED = "No folder selected"
    const val DATA_FOLDER_WAITING_FOR_GAME_FOLDER = "Waiting for game folder"
    const val GAME_FOLDER_RESELECTION_REQUIRED = "Needs to be selected again"

    fun emptyState(): DeploymentConfigUiState {
        return DeploymentConfigUiState(
            targetDataPath = "",
            realDeployEnabled = false,
            targetRootPath = "",
            dataPathReselectionRequired = false,
            rootPathReselectionRequired = false
        )
    }

    fun fromConfig(config: GameDeploymentConfig): DeploymentConfigUiState {
        return DeploymentConfigUiState(
            targetDataPath = config.targetDataPath,
            realDeployEnabled = config.realDeployEnabled,
            targetRootPath = config.targetRootPath,
            dataPathReselectionRequired = config.dataPathReselectionRequired,
            rootPathReselectionRequired = config.rootPathReselectionRequired
        )
    }

    fun fromProfile(profile: GameProfile): DeploymentConfigUiState {
        return DeploymentConfigUiState(
            targetDataPath = profile.targetDataPath,
            realDeployEnabled = profile.realDeployEnabled,
            targetRootPath = profile.targetRootPath,
            dataPathReselectionRequired = profile.dataPathReselectionRequired,
            rootPathReselectionRequired = profile.rootPathReselectionRequired
        )
    }

    fun configFromUi(
        selectedGameId: String,
        displayName: String,
        targetPathText: String,
        realDeployEnabled: Boolean,
        rootTargetPathText: String,
        dataPathReselectionRequired: Boolean,
        rootPathReselectionRequired: Boolean
    ): GameDeploymentConfig {
        val dataPath = targetPathText.trim()
        val rootPath = rootTargetPathText.trim()

        return GameDeploymentConfig(
            gameId = selectedGameId,
            displayName = displayName,
            targetDataPath = dataPath,
            realDeployEnabled = realDeployEnabled,
            targetRootPath = rootPath,
            dataPathReselectionRequired = dataPathReselectionRequired && dataPath.isBlank(),
            rootPathReselectionRequired = rootPathReselectionRequired && rootPath.isBlank()
        )
    }

    fun configFromProfile(profile: GameProfile): GameDeploymentConfig {
        return GameDeploymentConfig(
            gameId = profile.gameId,
            displayName = profile.gameDisplayName,
            targetDataPath = profile.targetDataPath,
            realDeployEnabled = profile.realDeployEnabled,
            targetRootPath = profile.targetRootPath,
            dataPathReselectionRequired = profile.dataPathReselectionRequired,
            rootPathReselectionRequired = profile.rootPathReselectionRequired
        )
    }

    fun dataPathDisplayText(
        path: String,
        reselectionRequired: Boolean
    ): String {
        return when {
            path.isNotBlank() -> path
            reselectionRequired -> DATA_FOLDER_WAITING_FOR_GAME_FOLDER
            else -> DATA_FOLDER_NOT_DETECTED
        }
    }

    fun rootPathDisplayText(
        path: String,
        reselectionRequired: Boolean
    ): String {
        return when {
            path.isNotBlank() -> path
            reselectionRequired -> GAME_FOLDER_RESELECTION_REQUIRED
            else -> GAME_FOLDER_NOT_SELECTED
        }
    }

    fun isTargetReady(
        path: String,
        reselectionRequired: Boolean
    ): Boolean {
        return path.isNotBlank() && !reselectionRequired
    }

    fun isGameInstallationResolved(
        gameRootPath: String,
        dataPath: String
    ): Boolean {
        return gameRootPath.isNotBlank() && dataPath.isNotBlank()
    }
}

internal data class DeploymentConfigUiState(
    val targetDataPath: String,
    val realDeployEnabled: Boolean,
    val targetRootPath: String,
    val dataPathReselectionRequired: Boolean,
    val rootPathReselectionRequired: Boolean
)

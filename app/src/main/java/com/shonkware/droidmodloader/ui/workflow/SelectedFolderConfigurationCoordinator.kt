package com.shonkware.droidmodloader.ui.workflow

import com.shonkware.droidmodloader.engine.deploy.ResolvedGameInstallation
import com.shonkware.droidmodloader.ui.MainActivityUiState

internal class SelectedFolderConfigurationCoordinator(
    private val state: MainActivityUiState,
    private val runOnUiThreadBlocking: (() -> Unit) -> Unit,
    private val saveSelectedGameConfig: () -> Unit,
    private val saveActiveProfile: () -> Unit,
    private val ensureDataBaselineIfMissing: (String) -> Unit,
    private val refreshDashboard: () -> Unit,
    private val appendLog: (String) -> Unit,
    private val onGameInstallationResolved: () -> Unit = {}
) {
    fun saveGameInstallation(installation: ResolvedGameInstallation) {
        if (installation.gameId != state.selectedGameId) {
            appendLog(
                "Refused resolved game installation because the selected game changed " +
                    "from ${installation.gameId} to ${state.selectedGameId}."
            )
            return
        }

        runOnUiThreadBlocking {
            state.targetPathText = installation.dataPath
            state.selectedDataPathText = installation.dataPath
            state.rootTargetPathText = installation.gameRootPath
            state.selectedRootPathText = installation.gameRootPath
            state.dataPathReselectionRequired = false
            state.rootPathReselectionRequired = false
            state.realDeployEnabledState = true
        }

        saveSelectedGameConfig()
        saveActiveProfile()
        onGameInstallationResolved()
        ensureDataBaselineIfMissing("game installation selected")
        refreshDashboard()
        appendLog(
            "Saved resolved game installation for ${state.selectedGameId}: " +
                "${installation.gameRootPath} (Data: ${installation.dataPath})"
        )
    }

    fun saveDataFolder(path: String) {
        runOnUiThreadBlocking {
            state.targetPathText = path
            state.selectedDataPathText = path
            state.dataPathReselectionRequired = false
            state.realDeployEnabledState = true
        }

        saveSelectedGameConfig()
        saveActiveProfile()
        ensureDataBaselineIfMissing("target folder selected")
        refreshDashboard()
        appendLog("Saved direct Data folder path for ${state.selectedGameId}: $path")
    }

    fun saveGameRoot(path: String) {
        runOnUiThreadBlocking {
            state.rootTargetPathText = path
            state.selectedRootPathText = path
            state.rootPathReselectionRequired = false
            state.realDeployEnabledState = true
        }

        saveSelectedGameConfig()
        saveActiveProfile()
        refreshDashboard()
        appendLog("Saved direct Game Root path for ${state.selectedGameId}: $path")
    }
}

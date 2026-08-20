package com.shonkware.droidmodloader.ui.workflow

import com.shonkware.droidmodloader.engine.deploy.ResolvedGameInstallation

internal class FolderPickerWorkflowController(
    private val runInBackground: (() -> Unit) -> Unit,
    private val saveFirstSetupDataPath: (String) -> Unit,
    private val savePickedDataFolderToSelectedGameConfig: (String) -> Unit,
    private val savePickedRootFolderToSelectedGameConfig: (String) -> Unit,
    private val setNewProfileDataPathText: (String) -> Unit,
    private val saveArchiveLibraryPath: (String) -> Unit,
    private val appendLog: (String) -> Unit,
    private val saveFirstSetupGameInstallation: (ResolvedGameInstallation) -> Unit = {},
    private val saveActiveGameInstallation: (ResolvedGameInstallation) -> Unit = {},
    private val setNewProfileGameInstallation: (ResolvedGameInstallation) -> Unit = {}
) {

    fun handlePickedFolder(
        mode: FolderPickMode,
        path: String
    ) {
        runInBackground {
            when (mode) {
                FolderPickMode.FirstSetupDataFolder -> {
                    saveFirstSetupDataPath(path)
                    appendLog("Selected Data folder for first setup.")
                }

                FolderPickMode.ActiveDataFolder -> {
                    savePickedDataFolderToSelectedGameConfig(path)
                }

                FolderPickMode.ActiveGameRootFolder -> {
                    savePickedRootFolderToSelectedGameConfig(path)
                }

                FolderPickMode.NewProfileDataFolder -> {
                    setNewProfileDataPathText(path)
                    appendLog("Selected Data folder for new profile.")
                }

                FolderPickMode.ArchiveLibraryFolder -> {
                    saveArchiveLibraryPath(path)
                    appendLog("Selected Archive Library folder.")
                }

                FolderPickMode.FirstSetupGameFolder,
                FolderPickMode.ActiveGameFolder,
                FolderPickMode.NewProfileGameFolder -> {
                    appendLog("Ignored unresolved game folder selection for $mode.")
                }
            }
        }
    }

    fun handlePickedGameInstallation(
        mode: FolderPickMode,
        installation: ResolvedGameInstallation
    ) {
        runInBackground {
            when (mode) {
                FolderPickMode.FirstSetupGameFolder -> {
                    saveFirstSetupGameInstallation(installation)
                    appendLog("Resolved game installation for first setup.")
                }

                FolderPickMode.ActiveGameFolder -> {
                    saveActiveGameInstallation(installation)
                }

                FolderPickMode.NewProfileGameFolder -> {
                    setNewProfileGameInstallation(installation)
                    appendLog("Resolved game installation for new profile.")
                }

                else -> {
                    appendLog("Ignored resolved game installation for unsupported folder mode: $mode")
                }
            }
        }
    }
}

package com.shonkware.droidmodloader.ui.workflow

internal enum class FolderPickMode {
    FirstSetupGameFolder,
    ActiveGameFolder,
    NewProfileGameFolder,

    // Legacy direct target modes remain internal while existing callers/tests
    // migrate. Normal user flows no longer expose them.
    FirstSetupDataFolder,
    ActiveDataFolder,
    ActiveGameRootFolder,
    NewProfileDataFolder,
    ArchiveLibraryFolder
}

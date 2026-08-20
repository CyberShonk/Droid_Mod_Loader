package com.shonkware.droidmodloader.ui.workflow

import com.shonkware.droidmodloader.engine.model.GameProfile
import com.shonkware.droidmodloader.engine.profile.ProfileRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class ProfileManagementResolvedInstallationTest {

    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun `first setup persists resolved game root and data`() {
        val repository = repository("first")
        val workflow = workflow(
            repository = repository,
            firstSetupInput = FirstSetupInput(
                profileNameText = "Default",
                gameId = "fallout_nv",
                targetDataPath = "/games/FNV/Data",
                realDeployEnabled = true,
                targetRootPath = "/games/FNV"
            )
        )

        workflow.completeFirstSetup()

        val profile = repository.loadProfiles().single()
        assertEquals("/games/FNV", profile.targetRootPath)
        assertEquals("/games/FNV/Data", profile.targetDataPath)
        assertTrue(profile.realDeployEnabled)
    }

    @Test
    fun `additional profile persists resolved game root and data`() {
        val repository = repository("additional")
        val workflow = workflow(
            repository = repository,
            additionalProfileInput = AdditionalProfileInput(
                profileNameText = "FNV",
                gameId = "fallout_nv",
                targetDataPath = "/games/FNV/Data",
                realDeployEnabled = true,
                targetRootPath = "/games/FNV"
            )
        )

        workflow.createAdditionalProfile()

        val profile = repository.loadProfiles().single()
        assertEquals("/games/FNV", profile.targetRootPath)
        assertEquals("/games/FNV/Data", profile.targetDataPath)
        assertTrue(profile.realDeployEnabled)
    }

    @Test
    fun `first setup refuses unresolved installation`() {
        val repository = repository("first-unresolved")
        var applied = false
        val errors = mutableListOf<String>()
        val statuses = mutableListOf<String>()
        val workflow = workflow(
            repository = repository,
            applyFirstSetupUiState = { _, _ -> applied = true },
            appendError = errors::add,
            updateLastOperationStatus = statuses::add
        )

        workflow.completeFirstSetup()

        assertTrue(repository.loadProfiles().isEmpty())
        assertFalse(repository.loadSetupState().setupComplete)
        assertFalse(applied)
        assertTrue(errors.any { it.contains("valid Game Folder") })
        assertTrue(statuses.any { it.contains("valid Game Folder") })
    }

    @Test
    fun `additional profile refuses unresolved installation`() {
        val repository = repository("additional-unresolved")
        var applied = false
        val errors = mutableListOf<String>()
        val statuses = mutableListOf<String>()
        val workflow = workflow(
            repository = repository,
            applyCreatedProfileUiState = { _, _ -> applied = true },
            appendError = errors::add,
            updateLastOperationStatus = statuses::add
        )

        workflow.createAdditionalProfile()

        assertTrue(repository.loadProfiles().isEmpty())
        assertFalse(repository.loadSetupState().setupComplete)
        assertFalse(applied)
        assertTrue(errors.any { it.contains("valid Game Folder") })
        assertTrue(statuses.any { it.contains("valid Game Folder") })
    }

    private fun repository(name: String): ProfileRepository {
        val directory = temporaryFolder.newFolder(name)
        return ProfileRepository(
            profilesFile = File(directory, "profiles.json"),
            setupStateFile = File(directory, "app_setup.json")
        )
    }

    private fun workflow(
        repository: ProfileRepository,
        firstSetupInput: FirstSetupInput = FirstSetupInput(
            profileNameText = "Default",
            gameId = "skyrim_le",
            targetDataPath = "",
            realDeployEnabled = false
        ),
        additionalProfileInput: AdditionalProfileInput = AdditionalProfileInput(
            profileNameText = "New Profile",
            gameId = "skyrim_le",
            targetDataPath = "",
            realDeployEnabled = false
        ),
        applyFirstSetupUiState: (List<GameProfile>, GameProfile) -> Unit = { _, _ -> },
        applyCreatedProfileUiState: (List<GameProfile>, GameProfile) -> Unit = { _, _ -> },
        appendError: (String) -> Unit = {},
        updateLastOperationStatus: (String) -> Unit = {}
    ): ProfileManagementWorkflow {
        return ProfileManagementWorkflow(
            repositoryProvider = { repository },
            currentTimeMillis = { 1234L },
            gameDisplayNameProvider = { it },
            firstSetupInputProvider = { firstSetupInput },
            additionalProfileInputProvider = { additionalProfileInput },
            activeProfileIdProvider = { null },
            dashboardProfileInputProvider = {
                DashboardProfileInput(
                    targetPathText = "",
                    rootTargetPathText = "",
                    realDeployEnabled = false,
                    dataPathReselectionRequired = false,
                    rootPathReselectionRequired = false
                )
            },
            applyFirstSetupUiState = applyFirstSetupUiState,
            applyCreatedProfileUiState = applyCreatedProfileUiState,
            applySwitchedProfileUiState = { _ -> },
            applySavedProfileUiState = { _, _ -> },
            applyDeletedProfileUiStateAsync = { _, _ -> },
            applyDeletedProfileUiStateBlocking = { _, _ -> },
            saveSelectedGameConfigFromUi = { },
            loadSelectedGameConfigIntoUi = { },
            recoverActiveProfile = { },
            syncPluginsFromCurrentState = { },
            refreshDashboard = { },
            appendLog = { _ -> },
            appendError = appendError,
            updateLastOperationStatus = updateLastOperationStatus
        )
    }
}

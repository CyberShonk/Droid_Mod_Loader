package com.shonkware.droidmodloader.ui.workflow

import com.shonkware.droidmodloader.engine.deploy.ResolvedGameInstallation
import com.shonkware.droidmodloader.ui.MutableMainActivityUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ResolvedGameInstallationConfigurationCoordinatorTest {

    @Test
    fun `resolved installation updates root and data in one persistence cycle`() {
        val state = MutableMainActivityUiState().apply {
            selectedGameId = "fallout_nv"
            dataPathReselectionRequired = true
            rootPathReselectionRequired = true
        }
        val events = mutableListOf<String>()
        val coordinator = SelectedFolderConfigurationCoordinator(
            state = state,
            runOnUiThreadBlocking = { action ->
                events += "ui"
                action()
            },
            saveSelectedGameConfig = { events += "save-config" },
            saveActiveProfile = { events += "save-profile" },
            ensureDataBaselineIfMissing = { reason -> events += "baseline:$reason" },
            refreshDashboard = { events += "refresh" },
            appendLog = { message -> events += "log:$message" }
        )

        coordinator.saveGameInstallation(
            ResolvedGameInstallation(
                gameId = "fallout_nv",
                gameRootPath = "/games/FNV",
                dataPath = "/games/FNV/Data"
            )
        )

        assertEquals("/games/FNV", state.rootTargetPathText)
        assertEquals("/games/FNV", state.selectedRootPathText)
        assertEquals("/games/FNV/Data", state.targetPathText)
        assertEquals("/games/FNV/Data", state.selectedDataPathText)
        assertFalse(state.dataPathReselectionRequired)
        assertFalse(state.rootPathReselectionRequired)
        assertTrue(state.realDeployEnabledState)
        assertEquals(1, events.count { it == "save-config" })
        assertEquals(1, events.count { it == "save-profile" })
        assertEquals(1, events.count { it == "baseline:game installation selected" })
    }

    @Test
    fun `resolved installation is refused if selected game changed`() {
        val state = MutableMainActivityUiState().apply {
            selectedGameId = "skyrim_le"
        }
        val events = mutableListOf<String>()
        val coordinator = SelectedFolderConfigurationCoordinator(
            state = state,
            runOnUiThreadBlocking = { action ->
                events += "ui"
                action()
            },
            saveSelectedGameConfig = { events += "save-config" },
            saveActiveProfile = { events += "save-profile" },
            ensureDataBaselineIfMissing = { reason -> events += "baseline:$reason" },
            refreshDashboard = { events += "refresh" },
            appendLog = { message -> events += "log:$message" }
        )

        coordinator.saveGameInstallation(
            ResolvedGameInstallation(
                gameId = "fallout_nv",
                gameRootPath = "/games/FNV",
                dataPath = "/games/FNV/Data"
            )
        )

        assertEquals("", state.rootTargetPathText)
        assertEquals("", state.targetPathText)
        assertFalse(state.realDeployEnabledState)
        assertFalse(events.any { it == "save-config" })
        assertTrue(events.any { it.contains("selected game changed") })
    }
}

package com.shonkware.droidmodloader.ui

import com.shonkware.droidmodloader.engine.storage.DirectFolderBrowserState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MainActivityUiStateInstallationProjectionTest {

    @Test
    fun `fresh installation state stores raw absence and projects clear status text`() {
        val state = MutableMainActivityUiState()

        assertEquals("", state.newProfileRootPathText)
        assertEquals("", state.newProfileDataPathText)
        assertEquals("", state.selectedRootPathText)
        assertEquals("", state.selectedDataPathText)

        val dashboard = state.toDashboardUiState(
            secondScreenEnabled = false,
            allFilesAccessRequired = false,
            directFolderState = directFolderState()
        )

        assertEquals("No folder selected", dashboard.selectedRootPathText)
        assertEquals("Not detected yet", dashboard.selectedDataPathText)
        assertFalse(dashboard.setupInstallationResolved)
        assertFalse(dashboard.newProfileInstallationResolved)
        assertFalse(dashboard.rootTargetReady)
        assertFalse(dashboard.dataTargetReady)
    }

    @Test
    fun `resolved setup readiness is derived from the root and data pair`() {
        val state = MutableMainActivityUiState().apply {
            setupRootTargetPathText = "/games/FNV"
            setupTargetPathText = "/games/FNV/Data"
            newProfileRootPathText = "/games/FNV"
            newProfileDataPathText = "/games/FNV/Data"
        }

        val dashboard = state.toDashboardUiState(
            secondScreenEnabled = false,
            allFilesAccessRequired = false,
            directFolderState = directFolderState()
        )

        assertTrue(dashboard.setupInstallationResolved)
        assertTrue(dashboard.newProfileInstallationResolved)
    }

    @Test
    fun `active target readiness respects reselection flags`() {
        val state = MutableMainActivityUiState().apply {
            rootTargetPathText = "/games/FNV"
            targetPathText = "/games/FNV/Data"
            rootPathReselectionRequired = true
            dataPathReselectionRequired = true
        }

        val dashboard = state.toDashboardUiState(
            secondScreenEnabled = false,
            allFilesAccessRequired = false,
            directFolderState = directFolderState()
        )

        assertFalse(dashboard.rootTargetReady)
        assertFalse(dashboard.dataTargetReady)
    }

    private fun directFolderState(): DirectFolderSelectionUiState {
        return DirectFolderSelectionUiState(
            allFilesAccessGranted = true,
            showBrowser = false,
            browserTitle = "Choose Folder",
            browserRequiresWritable = false,
            browserState = DirectFolderBrowserState()
        )
    }
}

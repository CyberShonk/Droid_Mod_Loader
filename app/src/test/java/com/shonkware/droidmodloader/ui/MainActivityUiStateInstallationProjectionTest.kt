package com.shonkware.droidmodloader.ui

import com.shonkware.droidmodloader.attention.AppAttentionId
import com.shonkware.droidmodloader.attention.AppAttentionKind
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

        val dashboard = project(state)

        assertEquals("No folder selected", dashboard.selectedRootPathText)
        assertEquals("Not detected yet", dashboard.selectedDataPathText)
        assertFalse(dashboard.setupInstallationResolved)
        assertFalse(dashboard.newProfileInstallationResolved)
        assertFalse(dashboard.rootTargetReady)
        assertFalse(dashboard.dataTargetReady)
        assertTrue(dashboard.appAttention.isEmpty())
    }

    @Test
    fun `resolved setup readiness is derived from the root and data pair`() {
        val state = MutableMainActivityUiState().apply {
            setupRootTargetPathText = "/games/FNV"
            setupTargetPathText = "/games/FNV/Data"
            newProfileRootPathText = "/games/FNV"
            newProfileDataPathText = "/games/FNV/Data"
        }

        val dashboard = project(state)

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

        val dashboard = project(state)

        assertFalse(dashboard.rootTargetReady)
        assertFalse(dashboard.dataTargetReady)
    }

    @Test
    fun `active profile reselection projects one game installation attention condition`() {
        val state = MutableMainActivityUiState().apply {
            setupComplete = true
            activeProfileId = "profile-a"
            selectedGameId = "fallout_nv"
            rootPathReselectionRequired = true
            dataPathReselectionRequired = true
        }

        val dashboard = project(state)

        assertEquals(1, dashboard.appAttention.size)
        assertEquals(
            AppAttentionKind.GAME_INSTALLATION_RESELECTION,
            dashboard.appAttention.single().kind
        )
    }

    @Test
    fun `deployment recovery attention is scoped to active profile and game`() {
        val state = MutableMainActivityUiState().apply {
            setupComplete = true
            activeProfileId = "profile-a"
            selectedGameId = "fallout_nv"
            deployRecoveryAttentionRequired = true
            deployRecoveryAttentionProfileId = "profile-a"
            deployRecoveryAttentionGameId = "fallout_nv"
        }

        assertEquals(
            AppAttentionKind.DEPLOY_RECOVERY,
            project(state).appAttention.single().kind
        )

        state.activeProfileId = "profile-b"

        assertTrue(project(state).appAttention.isEmpty())
        assertFalse(project(state).deployRecoveryAttentionRequired)
    }

    @Test
    fun `session suppression does not remove authoritative projected attention`() {
        val state = MutableMainActivityUiState().apply {
            setupComplete = true
            activeProfileId = "profile-a"
            selectedGameId = "fallout_nv"
            rootPathReselectionRequired = true
        }
        val first = project(state)
        val attentionId = first.appAttention.single().id

        state.suppressedAttentionPromptIds = setOf(attentionId)
        state.dismissedAttentionIds = setOf(AppAttentionId("other"))

        val second = project(state)

        assertEquals(first.appAttention, second.appAttention)
        assertEquals(setOf(attentionId), second.suppressedAttentionPromptIds)
    }

    private fun project(state: MutableMainActivityUiState): DashboardUiState {
        return state.toDashboardUiState(
            secondScreenEnabled = false,
            allFilesAccessRequired = false,
            directFolderState = directFolderState()
        )
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

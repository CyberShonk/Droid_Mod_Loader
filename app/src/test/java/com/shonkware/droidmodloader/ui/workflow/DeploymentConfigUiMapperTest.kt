package com.shonkware.droidmodloader.ui.workflow

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DeploymentConfigUiMapperTest {

    @Test
    fun `unconfigured root driven targets use selection and detection wording`() {
        assertEquals(
            "No folder selected",
            DeploymentConfigUiMapper.rootPathDisplayText("", reselectionRequired = false)
        )
        assertEquals(
            "Not detected yet",
            DeploymentConfigUiMapper.dataPathDisplayText("", reselectionRequired = false)
        )
    }

    @Test
    fun `reselection state uses one game folder recovery concept`() {
        assertEquals(
            "Needs to be selected again",
            DeploymentConfigUiMapper.rootPathDisplayText("", reselectionRequired = true)
        )
        assertEquals(
            "Waiting for game folder",
            DeploymentConfigUiMapper.dataPathDisplayText("", reselectionRequired = true)
        )
    }

    @Test
    fun `target readiness comes from path and reselection state not display text`() {
        assertTrue(
            DeploymentConfigUiMapper.isTargetReady(
                "/games/FNV/Data",
                reselectionRequired = false
            )
        )
        assertFalse(
            DeploymentConfigUiMapper.isTargetReady(
                "/games/FNV/Data",
                reselectionRequired = true
            )
        )
        assertFalse(
            DeploymentConfigUiMapper.isTargetReady(
                "",
                reselectionRequired = false
            )
        )
    }

    @Test
    fun `installation resolution requires both root and data paths`() {
        assertTrue(
            DeploymentConfigUiMapper.isGameInstallationResolved(
                "/games/FNV",
                "/games/FNV/Data"
            )
        )
        assertFalse(
            DeploymentConfigUiMapper.isGameInstallationResolved(
                "/games/FNV",
                ""
            )
        )
        assertFalse(
            DeploymentConfigUiMapper.isGameInstallationResolved(
                "",
                "/games/FNV/Data"
            )
        )
    }
}

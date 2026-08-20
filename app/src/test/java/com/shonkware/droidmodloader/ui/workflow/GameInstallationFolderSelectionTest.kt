package com.shonkware.droidmodloader.ui.workflow

import com.shonkware.droidmodloader.engine.deploy.GameInstallationResolver
import com.shonkware.droidmodloader.engine.deploy.ResolvedGameInstallation
import com.shonkware.droidmodloader.engine.storage.DirectFolderBrowser
import com.shonkware.droidmodloader.engine.storage.DirectPathValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.nio.file.Files

class GameInstallationFolderSelectionTest {

    @Test
    fun `valid game folder is resolved before picker closes`() {
        val root = Files.createTempDirectory("dml-game-folder").toFile()
        File(root, "FalloutNV.exe").writeText("")
        val data = File(root, "Data").apply { mkdirs() }
        File(data, "FalloutNV.esm").writeText("")
        val validator = DirectPathValidator()
        val selected = mutableListOf<ResolvedGameInstallation>()

        val coordinator = DirectFolderSelectionCoordinator(
            accessGrantedProvider = { true },
            browser = DirectFolderBrowser(listOf(root), validator),
            pathValidator = validator,
            currentPathProvider = { root.absolutePath },
            requestAllFilesAccess = {},
            handlePickedFolder = { _, _ -> },
            gameInstallationResolver = GameInstallationResolver(),
            selectedGameIdProvider = { "fallout_nv" },
            handlePickedGameInstallation = { _, installation ->
                selected += installation
            }
        )

        coordinator.open(FolderPickMode.FirstSetupGameFolder)
        assertEquals("Choose Game Folder", coordinator.browserTitle)
        coordinator.selectCurrent()

        assertFalse(coordinator.showBrowser)
        assertEquals(1, selected.size)
        assertEquals(root.canonicalPath, selected.single().gameRootPath)
        assertEquals(data.canonicalPath, selected.single().dataPath)
    }

    @Test
    fun `invalid game folder keeps picker open and exposes validator reason`() {
        val root = Files.createTempDirectory("dml-game-folder-invalid").toFile()
        val data = File(root, "Data").apply { mkdirs() }
        File(data, "FalloutNV.esm").writeText("")
        val validator = DirectPathValidator()

        val coordinator = DirectFolderSelectionCoordinator(
            accessGrantedProvider = { true },
            browser = DirectFolderBrowser(listOf(root), validator),
            pathValidator = validator,
            currentPathProvider = { data.absolutePath },
            requestAllFilesAccess = {},
            handlePickedFolder = { _, _ -> },
            gameInstallationResolver = GameInstallationResolver(),
            selectedGameIdProvider = { "fallout_nv" }
        )

        coordinator.open(FolderPickMode.FirstSetupGameFolder)
        coordinator.selectCurrent()

        assertTrue(coordinator.showBrowser)
        assertTrue(
            coordinator.browserState.errorMessage.orEmpty()
                .contains("looks like Data")
        )
    }
}

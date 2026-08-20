package com.shonkware.droidmodloader.engine.deploy

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class GameInstallationResolverTest {

    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private val resolver = GameInstallationResolver()

    @Test
    fun `valid fallout new vegas root resolves canonical root and data`() {
        val root = falloutNewVegasInstallation("fnv-valid")

        val result = resolver.resolve("fallout_nv", root.absolutePath)

        assertTrue(result.isResolved)
        assertEquals(root.canonicalPath, result.installation?.gameRootPath)
        assertEquals(File(root, "Data").canonicalPath, result.installation?.dataPath)
        assertTrue(
            result.findings.any { it.code == "TARGET_RELATIONSHIP_VALID" }
        )
    }

    @Test
    fun `data folder selected as game root is refused`() {
        val root = falloutNewVegasInstallation("fnv-data-as-root")

        val result = resolver.resolve(
            "fallout_nv",
            File(root, "Data").absolutePath
        )

        assertFalse(result.isResolved)
        assertNull(result.installation)
        assertTrue(result.findings.any { it.code == "TARGET_TYPE_MISMATCH" })
    }

    @Test
    fun `wrong game root is refused`() {
        val root = temporaryFolder.newFolder("skyrim-root")
        File(root, "TESV.exe").writeText("")
        val data = File(root, "Data").apply { mkdirs() }
        File(data, "Skyrim.esm").writeText("")

        val result = resolver.resolve("fallout_nv", root.absolutePath)

        assertFalse(result.isResolved)
        assertNull(result.installation)
        assertTrue(result.findings.any { it.code == "WRONG_GAME_TARGET" })
    }

    @Test
    fun `missing data folder leaves installation unresolved`() {
        val root = temporaryFolder.newFolder("fnv-no-data")
        File(root, "FalloutNV.exe").writeText("")

        val result = resolver.resolve("fallout_nv", root.absolutePath)

        assertFalse(result.isResolved)
        assertNull(result.installation)
        assertTrue(result.findings.any { it.code == "TARGET_PATH_INVALID" })
    }

    @Test
    fun `existing data directory is found without case sensitivity`() {
        val root = temporaryFolder.newFolder("fnv-lowercase-data")
        File(root, "FalloutNV.exe").writeText("")
        val data = File(root, "data").apply { mkdirs() }
        File(data, "FalloutNV.esm").writeText("")

        val result = resolver.resolve("fallout_nv", root.absolutePath)

        assertTrue(result.isResolved)
        assertEquals(data.canonicalPath, result.installation?.dataPath)
    }

    private fun falloutNewVegasInstallation(name: String): File {
        val root = temporaryFolder.newFolder(name)
        File(root, "FalloutNV.exe").writeText("")
        val data = File(root, "Data").apply { mkdirs() }
        File(data, "FalloutNV.esm").writeText("")
        return root
    }
}

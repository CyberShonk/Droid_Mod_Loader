package com.shonkware.droidmodloader.engine.index

import com.shonkware.droidmodloader.engine.model.DeployScope
import com.shonkware.droidmodloader.engine.model.Mod
import com.shonkware.droidmodloader.engine.model.ModType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.nio.file.Files

class ModContentIndexerTest {

    @Test
    fun `indexer derives deployability from physical deployment scope`() {
        val root = Files.createTempDirectory("dml-content-indexer").toFile()

        try {
            writeFixture(root, "uio/supported.txt")
            writeFixture(root, "nvse/plugins/script.txt")
            writeFixture(root, "custom/info.xml")
            writeFixture(root, "notes.md")
            writeFixture(root, "source/scripts/example.psc")
            writeFixture(root, "docs/readme.txt")
            writeFixture(root, "fomod/moduleconfig.xml")
            writeFixture(root, "custom/thing.bin")

            val mod = Mod(
                id = "classifier-fixture",
                name = "Classifier Fixture",
                installPath = root.absolutePath,
                enabled = true,
                priority = 1,
                modType = ModType.LOOSE
            )

            val index = ModContentIndexer().indexMod(mod)
            val entries = index.entries.associateBy { it.normalizedPath }

            assertEntry(
                entries = entries,
                path = "uio/supported.txt",
                category = ModContentCategory.DOCUMENTATION,
                scope = DeployScope.DATA,
                deployable = true
            )

            assertEntry(
                entries = entries,
                path = "nvse/plugins/script.txt",
                category = ModContentCategory.DOCUMENTATION,
                scope = DeployScope.DATA,
                deployable = true
            )

            assertEntry(
                entries = entries,
                path = "custom/info.xml",
                category = ModContentCategory.SETUP_ONLY,
                scope = DeployScope.DATA,
                deployable = true
            )

            assertEntry(
                entries = entries,
                path = "notes.md",
                category = ModContentCategory.DOCUMENTATION,
                scope = DeployScope.DATA,
                deployable = true
            )

            assertEntry(
                entries = entries,
                path = "source/scripts/example.psc",
                category = ModContentCategory.GAME_FILE,
                scope = DeployScope.DATA,
                deployable = true
            )

            assertEntry(
                entries = entries,
                path = "docs/readme.txt",
                category = ModContentCategory.DOCUMENTATION,
                scope = DeployScope.DATA,
                deployable = true
            )

            assertEntry(
                entries = entries,
                path = "fomod/moduleconfig.xml",
                category = ModContentCategory.SETUP_ONLY,
                scope = DeployScope.MANAGER_ONLY,
                deployable = false
            )

            assertEntry(
                entries = entries,
                path = "custom/thing.bin",
                category = ModContentCategory.UNKNOWN,
                scope = DeployScope.DATA,
                deployable = true
            )
        } finally {
            root.deleteRecursively()
        }
    }

    private fun writeFixture(root: File, relativePath: String) {
        File(root, relativePath).apply {
            parentFile?.mkdirs()
            writeText("fixture")
        }
    }

    private fun assertEntry(
        entries: Map<String, ModContentEntry>,
        path: String,
        category: ModContentCategory,
        scope: DeployScope,
        deployable: Boolean
    ) {
        val entry = entries.getValue(path)

        assertEquals(category, entry.category)
        assertEquals(scope, entry.deployScope)

        if (deployable) {
            assertTrue(entry.isDeployable)
        } else {
            assertFalse(entry.isDeployable)
        }
    }
}

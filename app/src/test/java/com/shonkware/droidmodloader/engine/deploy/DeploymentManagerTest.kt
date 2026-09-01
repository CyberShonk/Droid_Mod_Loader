package com.shonkware.droidmodloader.engine.deploy

import com.shonkware.droidmodloader.engine.model.DeploymentRecord
import com.shonkware.droidmodloader.engine.model.FileRecord
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.nio.file.Files

class DeploymentManagerTest {
    @Test
    fun `first deploy claims identical pre-existing target without rewriting it`() {
        withFixture("identical-pre-existing") { fixture ->
            val source = fixture.sourceFile("managed payload")
            val target = fixture.targetFile("managed payload")

            assertTrue(target.setLastModified(SENTINEL_MTIME))
            val targetMtimeBeforeDeploy = target.lastModified()

            val manager = fixture.manager()
            val (records, result) = manager.deploy(
                oldRecords = emptyList(),
                newFileRecords = listOf(fileRecord(source))
            )

            val deployedRecord = records.single()
            val backupFile = File(requireNotNull(deployedRecord.backupFilePath))

            assertEquals("managed payload", target.readText())
            assertEquals(targetMtimeBeforeDeploy, target.lastModified())
            assertTrue(deployedRecord.hadPreExistingTargetFile)
            assertTrue(backupFile.isFile)
            assertEquals("managed payload", backupFile.readText())
            assertEquals(1, result.addCount)
            assertEquals(1, result.backupCount)

            val (_, removeResult) = manager.deploy(
                oldRecords = records,
                newFileRecords = emptyList()
            )

            assertEquals("managed payload", target.readText())
            assertFalse(backupFile.exists())
            assertEquals(1, removeResult.removeCount)
            assertEquals(1, removeResult.restoreCount)
        }
    }

    @Test
    fun `first deploy still replaces different pre-existing target`() {
        withFixture("different-pre-existing") { fixture ->
            val source = fixture.sourceFile("managed payload")
            val target = fixture.targetFile("original payload")

            assertTrue(target.setLastModified(SENTINEL_MTIME))
            val targetMtimeBeforeDeploy = target.lastModified()

            val (records, result) = fixture.manager().deploy(
                oldRecords = emptyList(),
                newFileRecords = listOf(fileRecord(source))
            )

            val deployedRecord = records.single()
            val backupFile = File(requireNotNull(deployedRecord.backupFilePath))

            assertEquals("managed payload", target.readText())
            assertNotEquals(targetMtimeBeforeDeploy, target.lastModified())
            assertEquals("original payload", backupFile.readText())
            assertTrue(deployedRecord.hadPreExistingTargetFile)
            assertEquals(1, result.addCount)
            assertEquals(1, result.backupCount)
        }
    }

    @Test
    fun `existing managed update still rewrites identical target`() {
        withFixture("managed-update") { fixture ->
            val oldSource = fixture.sourceFile("managed payload", "old-source.txt")
            val newSource = fixture.sourceFile("managed payload", "new-source.txt")
            val target = fixture.targetFile("managed payload")

            assertTrue(target.setLastModified(SENTINEL_MTIME))
            val targetMtimeBeforeDeploy = target.lastModified()

            val oldRecord = DeploymentRecord(
                normalizedPath = MANAGED_PATH,
                winningModId = "fixture-mod",
                winningModName = "Fixture Mod",
                sourceFilePath = oldSource.absolutePath,
                hash = "old-hash"
            )

            val (_, result) = fixture.manager().deploy(
                oldRecords = listOf(oldRecord),
                newFileRecords = listOf(
                    fileRecord(
                        source = newSource,
                        hash = "new-hash"
                    )
                )
            )

            assertEquals("managed payload", target.readText())
            assertNotEquals(targetMtimeBeforeDeploy, target.lastModified())
            assertEquals(0, result.addCount)
            assertEquals(1, result.updateCount)
            assertEquals(0, result.backupCount)
        }
    }

    private fun fileRecord(
        source: File,
        hash: String = "fixture-hash"
    ): FileRecord {
        return FileRecord(
            normalizedPath = MANAGED_PATH,
            winningModId = "fixture-mod",
            winningModName = "Fixture Mod",
            sourceFilePath = source.absolutePath,
            hash = hash
        )
    }

    private fun withFixture(
        name: String,
        block: (Fixture) -> Unit
    ) {
        val root = Files.createTempDirectory("dml-deployment-manager-$name").toFile()

        try {
            block(Fixture(root))
        } finally {
            root.deleteRecursively()
        }
    }

    private class Fixture(
        val root: File
    ) {
        val deployRoot = File(root, "deploy").apply { mkdirs() }
        val backupRoot = File(root, "backups").apply { mkdirs() }

        fun manager(): DeploymentManager {
            return DeploymentManager(
                deployRootDir = deployRoot,
                backupRootDir = backupRoot
            )
        }

        fun sourceFile(
            content: String,
            name: String = "source.txt"
        ): File {
            return File(root, "sources/$name").apply {
                requireNotNull(parentFile).mkdirs()
                writeText(content)
            }
        }

        fun targetFile(content: String): File {
            return File(deployRoot, MANAGED_PATH).apply {
                requireNotNull(parentFile).mkdirs()
                writeText(content)
            }
        }
    }

    companion object {
        private const val MANAGED_PATH = "uio/settings.ini"
        private const val SENTINEL_MTIME = 946684800000L
    }
}

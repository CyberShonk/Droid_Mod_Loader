package com.shonkware.droidmodloader.engine.deploy.journal

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.io.File
import java.nio.file.Files

class DeploymentJournalRepositoryTest {
    @Test
    fun `schema two round trip preserves target ownership`() {
        val file = journalFile("round-trip")
        val repository = DeploymentJournalRepository(file)
        val record = record("operation-a")

        repository.saveStarted(record)

        val loaded = repository.load()!!
        assertEquals(2, loaded.schemaVersion)
        assertEquals(record.dataTarget, loaded.dataTarget)
        assertEquals(record.rootTarget, loaded.rootTarget)
        assertTrue(loaded.toDebugSummary().contains("Identity key"))
    }

    @Test
    fun `legacy journal remains readable without invented target ownership`() {
        val file = journalFile("legacy")
        file.writeText(
            """
            {
              "schemaVersion": 1,
              "operationId": "legacy-operation",
              "gameId": "fallout_nv",
              "profileId": "profile-a",
              "status": "STARTED",
              "startedAtEpochMillis": 1234,
              "completedAtEpochMillis": null,
              "planSummary": {
                "dataOperationCount": 1,
                "rootOperationCount": 0,
                "totalOperationCount": 1,
                "dataEstimatedCopyBytes": 10,
                "rootEstimatedCopyBytes": null,
                "preflightCanDeploy": true,
                "preflightErrorCount": 0,
                "preflightWarningCount": 0
              },
              "resultSummary": null,
              "failureMessage": null
            }
            """.trimIndent()
        )

        val loaded = DeploymentJournalRepository(file).load()!!

        assertEquals(1, loaded.schemaVersion)
        assertNull(loaded.dataTarget)
        assertNull(loaded.rootTarget)
        assertTrue(loaded.toDebugSummary().contains("legacy journal schema"))
    }

    @Test
    fun `unfinished journal cannot be overwritten before review`() {
        val file = journalFile("unfinished")
        val repository = DeploymentJournalRepository(file)
        val first = record("operation-a")
        repository.saveStarted(first)

        try {
            repository.saveStarted(record("operation-b"))
            fail("Expected unfinished journal overwrite to be refused.")
        } catch (expected: IllegalStateException) {
            assertTrue(expected.message.orEmpty().contains("Refusing to overwrite"))
        }

        assertEquals("operation-a", repository.load()!!.operationId)

        repository.markReviewed(first)
        repository.saveStarted(record("operation-b"))
        assertEquals("operation-b", repository.load()!!.operationId)
    }

    private fun journalFile(name: String): File {
        val root = Files.createTempDirectory("dml-journal-$name").toFile()
        return File(root, "deployment_journal_fallout_nv.json")
    }

    private fun record(operationId: String): DeploymentJournalRecord {
        return DeploymentJournalRecord(
            operationId = operationId,
            gameId = "fallout_nv",
            profileId = "profile-a",
            status = DeploymentJournalStatus.STARTED,
            startedAtEpochMillis = 1000,
            completedAtEpochMillis = null,
            planSummary = DeploymentJournalPlanSummary(
                dataOperationCount = 1,
                rootOperationCount = 0,
                totalOperationCount = 1,
                dataEstimatedCopyBytes = 10,
                rootEstimatedCopyBytes = null,
                preflightCanDeploy = true,
                preflightErrorCount = 0,
                preflightWarningCount = 0
            ),
            resultSummary = null,
            failureMessage = null,
            dataTarget = targetState(
                targetType = "DATA",
                mode = "real_path",
                target = "/target-a/Data",
                manifest = "/state/manifest-a.json",
                baseline = "/state/baseline-a.json",
                backups = "/state/backups-a"
            ),
            rootTarget = targetState(
                targetType = "GAME_ROOT",
                mode = "root_real_path",
                target = "/target-a",
                manifest = "/state/root-manifest-a.json",
                baseline = null,
                backups = "/state/root-backups-a"
            )
        )
    }

    private fun targetState(
        targetType: String,
        mode: String,
        target: String,
        manifest: String,
        baseline: String?,
        backups: String
    ): DeploymentJournalTargetState {
        return DeploymentJournalTargetState(
            targetType = targetType,
            gameId = "fallout_nv",
            mode = mode,
            target = target,
            identityKey = "fallout_nv|$mode|$target",
            manifestFilePath = manifest,
            baselineFilePath = baseline,
            backupDirectoryPath = backups
        )
    }
}

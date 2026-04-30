package com.shonkware.droidmodloader.engine.deploy

import com.shonkware.droidmodloader.engine.build.DiffEngine
import com.shonkware.droidmodloader.engine.build.FileChange
import com.shonkware.droidmodloader.engine.model.DeploymentRecord
import com.shonkware.droidmodloader.engine.model.FileRecord
import java.io.File

data class DeploymentResult(
    val addCount: Int,
    val removeCount: Int,
    val updateCount: Int,
    val finalRecordCount: Int
)

class DeploymentManager(
    private val deployRootDir: File
) {

    fun deploy(
        oldRecords: List<DeploymentRecord>,
        newFileRecords: List<FileRecord>
    ): Pair<List<DeploymentRecord>, DeploymentResult> {
        deployRootDir.mkdirs()

        val oldFileRecords = oldRecords.map {
            FileRecord(
                normalizedPath = it.normalizedPath,
                winningModId = it.winningModId,
                winningModName = it.winningModId,
                sourceFilePath = it.sourceFilePath,
                hash = it.hash
            )
        }

        val diffEngine = DiffEngine()
        val changes = diffEngine.diff(oldFileRecords, newFileRecords)

        for (change in changes) {
            when (change) {
                is FileChange.Add -> {
                    copyIntoDeployRoot(change.record)
                }
                is FileChange.Update -> {
                    copyIntoDeployRoot(change.newRecord)
                }
                is FileChange.Remove -> {
                    val targetFile = File(deployRootDir, change.normalizedPath)
                    if (targetFile.exists()) {
                        targetFile.delete()
                    }
                }
            }
        }

        val newDeploymentRecords = newFileRecords.map {
            DeploymentRecord(
                normalizedPath = it.normalizedPath,
                winningModId = it.winningModId,
                sourceFilePath = it.sourceFilePath,
                hash = it.hash,
                deployedRelativePath = it.normalizedPath
            )
        }

        val result = DeploymentResult(
            addCount = changes.count { it is FileChange.Add },
            removeCount = changes.count { it is FileChange.Remove },
            updateCount = changes.count { it is FileChange.Update },
            finalRecordCount = newDeploymentRecords.size
        )

        return Pair(newDeploymentRecords, result)
    }

    private fun copyIntoDeployRoot(record: FileRecord) {
        val sourceFile = File(record.sourceFilePath)
        if (!sourceFile.exists() || !sourceFile.isFile) return

        val targetFile = File(deployRootDir, record.normalizedPath)
        targetFile.parentFile?.mkdirs()
        sourceFile.copyTo(targetFile, overwrite = true)
    }
}
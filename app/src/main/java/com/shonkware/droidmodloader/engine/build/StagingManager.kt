package com.shonkware.droidmodloader.engine.build

import com.shonkware.droidmodloader.engine.model.FileRecord
import java.io.File

class StagingManager(
    private val stagingDir: File
) {

    fun rebuildStaging(records: List<FileRecord>) {
        if (stagingDir.exists()) {
            stagingDir.deleteRecursively()
        }
        stagingDir.mkdirs()

        for (record in records) {
            copyRecordToStaging(record)
        }
    }

    fun applyChanges(changes: List<FileChange>) {
        stagingDir.mkdirs()

        for (change in changes) {
            when (change) {
                is FileChange.Add -> {
                    copyRecordToStaging(change.record)
                }

                is FileChange.Remove -> {
                    val destinationFile = File(stagingDir, change.normalizedPath)
                    if (destinationFile.exists()) {
                        destinationFile.delete()
                    }
                }

                is FileChange.Update -> {
                    copyRecordToStaging(change.newRecord)
                }
            }
        }
    }

    private fun copyRecordToStaging(record: FileRecord) {
        val sourceFile = File(record.sourceFilePath)
        if (!sourceFile.exists() || !sourceFile.isFile) {
            return
        }

        val destinationFile = File(stagingDir, record.normalizedPath)
        destinationFile.parentFile?.mkdirs()
        sourceFile.copyTo(destinationFile, overwrite = true)
    }
}
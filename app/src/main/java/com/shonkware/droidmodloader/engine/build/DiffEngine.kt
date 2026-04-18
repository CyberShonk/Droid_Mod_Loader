package com.shonkware.droidmodloader.engine.build

import com.shonkware.droidmodloader.engine.model.FileRecord

sealed class FileChange {
    data class Add(val record: FileRecord) : FileChange()
    data class Remove(val normalizedPath: String) : FileChange()
    data class Update(val oldRecord: FileRecord, val newRecord: FileRecord) : FileChange()
}

class DiffEngine {

    fun diff(oldRecords: List<FileRecord>, newRecords: List<FileRecord>): List<FileChange> {
        val oldMap = oldRecords.associateBy { it.normalizedPath }
        val newMap = newRecords.associateBy { it.normalizedPath }

        val allPaths = (oldMap.keys + newMap.keys).sorted()
        val changes = mutableListOf<FileChange>()

        for (path in allPaths) {
            val oldRecord = oldMap[path]
            val newRecord = newMap[path]

            when {
                oldRecord == null && newRecord != null -> {
                    changes.add(FileChange.Add(newRecord))
                }

                oldRecord != null && newRecord == null -> {
                    changes.add(FileChange.Remove(path))
                }

                oldRecord != null && newRecord != null -> {
                    val changed = oldRecord.hash != newRecord.hash ||
                            oldRecord.winningModId != newRecord.winningModId ||
                            oldRecord.sourceFilePath != newRecord.sourceFilePath

                    if (changed) {
                        changes.add(FileChange.Update(oldRecord, newRecord))
                    }
                }
            }
        }

        return changes
    }
}
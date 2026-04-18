package com.shonkware.droidmodloader.engine.conflict

import com.shonkware.droidmodloader.engine.model.FileRecord
import com.shonkware.droidmodloader.engine.model.Mod
import com.shonkware.droidmodloader.engine.model.ModFile
import java.io.File

class ConflictResolver {

    fun resolve(mods: List<Mod>, modFiles: List<ModFile>): List<FileRecord> {
        val enabledMods = mods
            .filter { it.enabled }
            .sortedBy { it.priority }

        val modById = enabledMods.associateBy { it.id }

        val winningFiles = linkedMapOf<String, FileRecord>()

        for (mod in enabledMods) {
            val filesForMod = modFiles.filter { it.modId == mod.id }

            for (modFile in filesForMod) {
                val sourceFilePath = File(mod.installPath, modFile.originalPath).absolutePath

                val record = FileRecord(
                    normalizedPath = modFile.normalizedPath,
                    winningModId = mod.id,
                    winningModName = mod.name,
                    sourceFilePath = sourceFilePath,
                    hash = modFile.hash
                )

                winningFiles[modFile.normalizedPath] = record
            }
        }

        return winningFiles.values.toList()
    }
}
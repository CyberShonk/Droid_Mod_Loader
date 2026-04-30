package com.shonkware.droidmodloader.engine

import com.shonkware.droidmodloader.engine.build.StagingManager
import com.shonkware.droidmodloader.engine.build.DiffEngine
import com.shonkware.droidmodloader.engine.build.FileChange
import com.shonkware.droidmodloader.engine.conflict.ConflictResolver
import com.shonkware.droidmodloader.engine.data.ModStateRepository
import com.shonkware.droidmodloader.engine.install.ModExtractor
import com.shonkware.droidmodloader.engine.io.FileScanner
import com.shonkware.droidmodloader.engine.data.InstalledModRecordRepository
import com.shonkware.droidmodloader.engine.model.InstalledModRecord
import com.shonkware.droidmodloader.engine.model.FileRecord
import com.shonkware.droidmodloader.engine.model.Mod
import com.shonkware.droidmodloader.engine.model.ModFile
import com.shonkware.droidmodloader.engine.model.ModType
import java.io.File

data class UninstallResult(
    val removed: Boolean,
    val removedModId: String,
    val addCount: Int,
    val removeCount: Int,
    val updateCount: Int
)

class ModEngine(
    private val tempDir: File,
    private val modsDir: File,
    private val stagingDir: File,
    private val stateFile: File
) {

    private val extractor = ModExtractor(tempDir, modsDir)
    private val resolver = ConflictResolver()
    private val stagingManager = StagingManager(stagingDir)
    private val stateRepository = ModStateRepository(stateFile)
    private val installedModRecordRepository = InstalledModRecordRepository()

    fun installArchive(archive: File, priority: Int, enabled: Boolean = true): Mod {
        val extractedDir = extractor.extractArchive(archive)
        return buildModFromInstalledFolder(extractedDir, priority, enabled)
    }

    fun buildModFromInstalledFolder(
        modDir: File,
        priority: Int,
        enabled: Boolean = true
    ): Mod {
        return Mod(
            id = modDir.name,
            name = modDir.name,
            installPath = modDir.absolutePath,
            enabled = enabled,
            priority = priority,
            modType = detectModType(modDir)
        )
    }

    fun scanMod(mod: Mod): List<ModFile> {
        val modDir = File(mod.installPath)
        val scanner = FileScanner()
        scanner.scanDirectory(modDir, modDir, mod.name)

        return convertScannerResultsToModFiles(
            modId = mod.id,
            sourceModName = mod.name,
            fileMap = scanner.getFileMap()
        )
    }

    fun scanMods(mods: List<Mod>): List<ModFile> {
        val allModFiles = mutableListOf<ModFile>()

        for (mod in mods) {
            allModFiles.addAll(scanMod(mod))
        }

        return allModFiles
    }

    fun resolve(mods: List<Mod>): List<FileRecord> {
        val modFiles = scanMods(mods)
        return resolver.resolve(mods, modFiles)
    }

    fun rebuildStaging(mods: List<Mod>): List<FileRecord> {
        val winningRecords = resolve(mods)
        stagingManager.rebuildStaging(winningRecords)
        return winningRecords
    }

    fun saveMods(mods: List<Mod>) {
        stateRepository.saveMods(mods)
    }

    fun loadMods(): List<Mod> {
        return stateRepository.loadMods()
    }

    fun getInstalledModsFromFolders(): List<Mod> {
        val modDirs = modsDir.listFiles()
            ?.filter { it.isDirectory }
            ?.sortedBy { it.name.lowercase() }
            ?: emptyList()

        return modDirs.mapIndexed { index, modDir ->
            Mod(
                id = modDir.name,
                name = modDir.name,
                installPath = modDir.absolutePath,
                enabled = true,
                priority = (index + 1) * 10,
                modType = detectModType(modDir)
            )
        }
    }

    fun rebuildStagingFromInstalledMods(): List<FileRecord> {
        val mods = getInstalledModsFromFolders()
        return rebuildStaging(mods)
    }

    fun saveInstalledModsFromFolders(): List<Mod> {
        val mods = getInstalledModsFromFolders()
        saveMods(mods)
        return mods
    }

    fun getCurrentMods(): List<Mod> {
        val savedMods = loadMods().sortedBy { it.priority }
        return if (savedMods.isNotEmpty()) {
            savedMods
        } else {
            getInstalledModsFromFolders()
        }
    }

    fun saveCurrentMods(mods: List<Mod>) {
        saveMods(mods.sortedBy { it.priority })
    }

    fun rebuildStagingFromCurrentState(): List<FileRecord> {
        val mods = getCurrentMods()
        return rebuildStaging(mods)
    }

    fun uninstallModAndApplyDiff(modId: String): UninstallResult {
        val currentMods = getCurrentMods().sortedBy { it.priority }
        val modToRemove = currentMods.firstOrNull { it.id == modId }
            ?: return UninstallResult(
                removed = false,
                removedModId = modId,
                addCount = 0,
                removeCount = 0,
                updateCount = 0
            )

        val oldWinningRecords = resolve(currentMods)

        val remainingMods = currentMods
            .filterNot { it.id == modId }
            .mapIndexed { index, mod ->
                mod.copy(priority = (index + 1) * 10)
            }

        saveCurrentMods(remainingMods)

        val newWinningRecords = resolve(remainingMods)

        val diffEngine = DiffEngine()
        val changes = diffEngine.diff(oldWinningRecords, newWinningRecords)

        stagingManager.applyChanges(changes)

        val modDir = File(modToRemove.installPath)
        if (modDir.exists()) {
            modDir.deleteRecursively()
        }

        return UninstallResult(
            removed = true,
            removedModId = modId,
            addCount = changes.count { it is FileChange.Add },
            removeCount = changes.count { it is FileChange.Remove },
            updateCount = changes.count { it is FileChange.Update }
        )
    }

    fun resetAllAppData(importsDir: File): Boolean {
        return try {
            if (tempDir.exists()) tempDir.deleteRecursively()
            if (modsDir.exists()) modsDir.deleteRecursively()
            if (stagingDir.exists()) stagingDir.deleteRecursively()
            if (importsDir.exists()) importsDir.deleteRecursively()
            if (stateFile.exists()) stateFile.delete()

            tempDir.mkdirs()
            modsDir.mkdirs()
            stagingDir.mkdirs()
            stateFile.parentFile?.mkdirs()

            true
        } catch (e: Exception) {
            false
        }
    }

    fun hasSavedState(): Boolean {
        return stateFile.exists() && stateFile.readText().isNotBlank()
    }

    fun getCurrentModSummary(): Triple<Int, Int, Boolean> {
        val mods = getCurrentMods()
        val installedCount = mods.size
        val enabledCount = mods.count { it.enabled }
        val hasSaved = hasSavedState()

        return Triple(installedCount, enabledCount, hasSaved)
    }

    private fun detectModType(modDir: File): ModType {
        val allPaths = modDir.walkTopDown()
            .filter { it.isFile }
            .map { it.relativeTo(modDir).path.lowercase().replace("\\", "/") }
            .toList()

        val hasLooseGameFiles = allPaths.any {
            it.startsWith("data/") ||
                    it.startsWith("meshes/") ||
                    it.startsWith("textures/") ||
                    it.startsWith("scripts/") ||
                    it.startsWith("interface/")
        }

        val hasArchiveFiles = allPaths.any {
            it.endsWith(".esp") || it.endsWith(".esm") || it.endsWith(".bsa")
        }

        return when {
            hasLooseGameFiles && hasArchiveFiles -> ModType.MIXED
            hasLooseGameFiles -> ModType.LOOSE
            else -> ModType.ARCHIVE
        }
    }

    private fun convertScannerResultsToModFiles(
        modId: String,
        sourceModName: String,
        fileMap: Map<String, List<com.shonkware.droidmodloader.engine.io.FileInfo>>): List<ModFile> {
        val results = mutableListOf<ModFile>()

        for ((_, infos) in fileMap) {
            for (info in infos) {
                results.add(
                    ModFile(
                        modId = modId,
                        sourceModName = sourceModName,
                        originalPath = info.originalPath,
                        normalizedPath = info.normalizedPath,
                        hash = info.hash
                    )
                )
            }
        }

        return results
    }

    private fun writeInstalledModRecord(
        modDir: File,
        sourceType: String,
        sourceArchiveName: String?) {
        val record = InstalledModRecord(
            modId = modDir.name,
            displayName = modDir.name,
            installPath = modDir.absolutePath,
            sourceType = sourceType,
            sourceArchiveName = sourceArchiveName,
            installedAtEpochMillis = System.currentTimeMillis()
        )

        installedModRecordRepository.saveRecord(modDir, record)
    }
    fun installArchiveWithRecord(
        archive: File,
        priority: Int,
        enabled: Boolean = true,
        sourceType: String = "imported_zip"): Mod {
        val extractedDir = extractor.extractArchive(archive)
        writeInstalledModRecord(
            modDir = extractedDir,
            sourceType = sourceType,
            sourceArchiveName = archive.name
        )
        return buildModFromInstalledFolder(extractedDir, priority, enabled)
    }

    fun registerExistingInstalledFolderWithRecord(
        modDir: File,
        priority: Int,
        enabled: Boolean = true,
        sourceType: String): Mod {
        writeInstalledModRecord(
            modDir = modDir,
            sourceType = sourceType,
            sourceArchiveName = null
        )
        return buildModFromInstalledFolder(modDir, priority, enabled)
    }

    fun loadInstalledModRecord(mod: Mod): InstalledModRecord? {
        val modDir = File(mod.installPath)
        return installedModRecordRepository.loadRecord(modDir)
    }

    fun loadInstalledModRecords(mods: List<Mod>): Map<String, InstalledModRecord> {
        val results = mutableMapOf<String, InstalledModRecord>()

        for (mod in mods) {
            val record = loadInstalledModRecord(mod)
            if (record != null) {
                results[mod.id] = record
            }
        }

        return results
    }


}
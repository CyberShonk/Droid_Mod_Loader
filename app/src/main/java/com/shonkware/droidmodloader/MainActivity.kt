package com.shonkware.droidmodloader

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import android.widget.Button
import android.widget.TextView
import java.io.File
import com.shonkware.droidmodloader.engine.util.PathUtils
import com.shonkware.droidmodloader.engine.install.ModExtractor
import com.shonkware.droidmodloader.engine.io.FileScanner
import com.shonkware.droidmodloader.engine.model.FileRecord
import com.shonkware.droidmodloader.engine.model.Mod
import com.shonkware.droidmodloader.engine.model.ModFile
import com.shonkware.droidmodloader.engine.model.ModType
import com.shonkware.droidmodloader.engine.conflict.ConflictResolver
import com.shonkware.droidmodloader.engine.build.DiffEngine
import com.shonkware.droidmodloader.engine.build.FileChange
import com.shonkware.droidmodloader.engine.build.StagingManager
import com.shonkware.droidmodloader.engine.data.ModStateRepository

class MainActivity : ComponentActivity() {

    companion object {
        private const val TAG = "DroidModLoader"
    }

    private lateinit var logTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        logTextView = findViewById(R.id.logTextView)

        val buttonSmokeTest: Button = findViewById(R.id.buttonSmokeTest)
        val buttonPathUtilsTest: Button = findViewById(R.id.buttonPathUtilsTest)
        val buttonExtractorTest: Button = findViewById(R.id.buttonExtractorTest)
        val buttonScannerTest: Button = findViewById(R.id.buttonScannerTest)
        val buttonConflictTest: Button = findViewById(R.id.buttonConflictTest)
        val buttonStagingTest: Button = findViewById(R.id.buttonStagingTest)
        val buttonDiffTest: Button = findViewById(R.id.buttonDiffTest)
        val buttonClearLog: Button = findViewById(R.id.buttonClearLog)
        val buttonStateTest: Button = findViewById(R.id.buttonStateTest)

        buttonSmokeTest.setOnClickListener {
            runInBackground { runSmokeTest() }
        }

        buttonPathUtilsTest.setOnClickListener {
            runInBackground { runPathUtilsLessonTest() }
        }

        buttonExtractorTest.setOnClickListener {
            runInBackground { runModExtractorLessonTest() }
        }

        buttonScannerTest.setOnClickListener {
            runInBackground { runFileScannerLessonTest() }
        }

        buttonConflictTest.setOnClickListener {
            runInBackground { runConflictResolverLessonTest() }
        }

        buttonStagingTest.setOnClickListener {
            runInBackground { runStagingManagerLessonTest() }
        }

        buttonDiffTest.setOnClickListener {
            runInBackground { runDiffEngineLessonTest() }
        }

        buttonStateTest.setOnClickListener {
            runInBackground { runModStateLessonTest() }
        }

        buttonClearLog.setOnClickListener {
            logTextView.text = ""
        }

        appendLog("UI ready. Tap a test button.")
    }

    private fun runInBackground(block: () -> Unit) {
        Thread {
            block()
        }.start()
    }

    private fun appendLog(message: String) {
        Log.d(TAG, message)
        runOnUiThread {
            logTextView.append(message + "\n")
        }
    }

    private fun appendError(message: String, throwable: Throwable? = null) {
        if (throwable != null) {
            Log.e(TAG, message, throwable)
        } else {
            Log.e(TAG, message)
        }

        runOnUiThread {
            logTextView.append("ERROR: $message\n")
        }
    }

    private fun runSmokeTest() {
        val baseDir = getExternalFilesDir(null)

        if (baseDir == null) {
            appendError("Base directory is null")
            return
        }

        val tempDir = File(baseDir, "temp")
        val modsDir = File(baseDir, "mods")

        val tempCreated = tempDir.mkdirs()
        val modsCreated = modsDir.mkdirs()

        appendLog("Base directory: ${baseDir.absolutePath}")
        appendLog("Temp directory: ${tempDir.absolutePath}")
        appendLog("Mods directory: ${modsDir.absolutePath}")
        appendLog("temp.mkdirs() returned: $tempCreated")
        appendLog("mods.mkdirs() returned: $modsCreated")
        appendLog("Temp exists: ${tempDir.exists()}")
        appendLog("Mods exists: ${modsDir.exists()}")
        appendLog("Smoke test complete")
    }

    private fun runPathUtilsLessonTest() {
        val tests = listOf(
            Pair("Meshes\\Armor\\Iron\\helmet.nif", "meshes/armor/iron/helmet.nif"),
            Pair("meshes/armor/iron/helmet.nif", "meshes/armor/iron/helmet.nif"),
            Pair("./Data/Meshes/Armor/Iron/helmet.nif", "meshes/armor/iron/helmet.nif"),
            Pair("Data\\Meshes\\Armor\\Iron\\helmet.nif", "meshes/armor/iron/helmet.nif"),
            Pair("/meshes//armor///iron/helmet.nif", "meshes/armor/iron/helmet.nif"),
            Pair("__MACOSX/Data/Meshes/Armor/helmet.nif", null),
            Pair("./.hidden/file.txt", null),
            Pair("textures/ui/汉字.dds", "textures/ui/汉字.dds")
        )

        appendLog("----- PathUtils Lesson Test Start -----")

        for (test in tests) {
            val input = test.first
            val expected = test.second
            val actual = PathUtils.normalize(input)
            val passed = actual == expected

            appendLog("INPUT: $input")
            appendLog("EXPECTED: $expected")
            appendLog("ACTUAL: $actual")
            appendLog("RESULT: ${if (passed) "PASS" else "FAIL"}")
            appendLog("--------------------------------------")
        }

        appendLog("----- PathUtils Lesson Test End -----")
    }

    private fun runModExtractorLessonTest() {
        appendLog("----- ModExtractor Lesson Test Start -----")

        val baseDir = filesDir
        val tempDir = File(baseDir, "temp")
        val modsDir = File(baseDir, "mods")

        tempDir.mkdirs()
        modsDir.mkdirs()

        val archive = File(baseDir, "SkyUI.zip")

        try {
            copyAssetToInternalStorage("SkyUI.zip", archive)

            appendLog("Archive copied to: ${archive.absolutePath}")
            appendLog("Archive exists: ${archive.exists()}")
            appendLog("Archive canRead: ${archive.canRead()}")

            val extractor = ModExtractor(tempDir, modsDir)
            val modDir = extractor.extractArchive(archive)

            appendLog("Extracted mod dir: ${modDir.absolutePath}")
            appendLog("Extracted mod exists: ${modDir.exists()}")

            val extractedFiles = modDir.walkTopDown().toList()
            appendLog("Extracted item count: ${extractedFiles.size}")

            for (file in extractedFiles.take(20)) {
                appendLog("ITEM: ${file.absolutePath}")
            }

            appendLog("RESULT: PASS")
        } catch (e: Exception) {
            appendError("Extraction failed: ${e.message}", e)
            appendLog("RESULT: FAIL")
        }

        appendLog("----- ModExtractor Lesson Test End -----")
    }

    private fun copyAssetToInternalStorage(assetName: String, destinationFile: File) {
        assets.open(assetName).use { inputStream ->
            destinationFile.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
    }

    private fun createLooseTestMod(modsDir: File): File {
        val modDir = File(modsDir, "LooseTestMod")

        if (modDir.exists()) {
            modDir.deleteRecursively()
        }

        File(modDir, "Data/Meshes/Armor/Iron").mkdirs()
        File(modDir, "Data/Textures/UI").mkdirs()
        File(modDir, "Data/Scripts").mkdirs()

        File(modDir, "Data/Meshes/Armor/Iron/helmet.nif")
            .writeText("fake mesh data")

        File(modDir, "Data/Textures/UI/icon.dds")
            .writeText("fake texture data")

        File(modDir, "Data/Scripts/TestScript.psc")
            .writeText("script source code")

        return modDir
    }

    private fun logScanResults(scanner: FileScanner, label: String) {
        val fileMap = scanner.getFileMap()

        appendLog("----- Scan Results for $label -----")
        appendLog("Normalized path count: ${fileMap.size}")

        for ((path, infos) in fileMap) {
            appendLog("PATH: $path")
            for (info in infos) {
                appendLog("  MOD: ${info.sourceMod}, ORIGINAL: ${info.originalPath}, HASH: ${info.hash}")
            }
        }

        appendLog("----- End Scan Results for $label -----")
    }

    private fun runFileScannerLessonTest() {
        appendLog("----- FileScanner Lesson Test Start -----")

        val baseDir = filesDir
        val modsDir = File(baseDir, "mods")
        modsDir.mkdirs()

        val skyUiDir = File(modsDir, "SkyUI")
        val looseTestModDir = createLooseTestMod(modsDir)

        if (!skyUiDir.exists()) {
            appendError("SkyUI mod folder not found: ${skyUiDir.absolutePath}")
            appendLog("Run Chapter 3 extraction test first.")
            appendLog("----- FileScanner Lesson Test End -----")
            return
        }

        try {
            val skyUiScanner = FileScanner()
            skyUiScanner.scanDirectory(skyUiDir, skyUiDir, "SkyUI")
            logScanResults(skyUiScanner, "SkyUI")

            val looseScanner = FileScanner()
            looseScanner.scanDirectory(looseTestModDir, looseTestModDir, "LooseTestMod")
            logScanResults(looseScanner, "LooseTestMod")

            appendLog("RESULT: PASS")
        } catch (e: Exception) {
            appendError("FileScanner test failed: ${e.message}", e)
            appendLog("RESULT: FAIL")
        }

        appendLog("----- FileScanner Lesson Test End -----")
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
        fileMap: Map<String, List<com.shonkware.droidmodloader.engine.io.FileInfo>>
    ): List<ModFile> {
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

    private fun logModelResults(mod: Mod, modFiles: List<ModFile>) {
        appendLog("----- Model Results for ${mod.name} -----")
        appendLog("MOD OBJECT: $mod")
        appendLog("Mod file count: ${modFiles.size}")

        for (modFile in modFiles.take(20)) {
            appendLog("MOD FILE: $modFile")
        }

        appendLog("----- End Model Results for ${mod.name} -----")
    }

    private fun runModelLessonTest() {
        appendLog("----- Model Lesson Test Start -----")

        val baseDir = filesDir
        val modsDir = File(baseDir, "mods")
        modsDir.mkdirs()

        val skyUiDir = File(modsDir, "SkyUI")
        val looseTestModDir = File(modsDir, "LooseTestMod")

        if (!skyUiDir.exists() || !looseTestModDir.exists()) {
            appendError("Required mod folders are missing.")
            appendLog("Run Chapter 3 and Chapter 4 tests first.")
            appendLog("----- Model Lesson Test End -----")
            return
        }

        try {
            val skyUiMod = Mod(
                id = "SkyUI",
                name = "SkyUI",
                installPath = skyUiDir.absolutePath,
                enabled = true,
                priority = 10,
                modType = detectModType(skyUiDir)
            )

            val looseTestMod = Mod(
                id = "LooseTestMod",
                name = "LooseTestMod",
                installPath = looseTestModDir.absolutePath,
                enabled = true,
                priority = 20,
                modType = detectModType(looseTestModDir)
            )

            val skyUiScanner = FileScanner()
            skyUiScanner.scanDirectory(skyUiDir, skyUiDir, "SkyUI")
            val skyUiModFiles = convertScannerResultsToModFiles(
                modId = skyUiMod.id,
                sourceModName = skyUiMod.name,
                fileMap = skyUiScanner.getFileMap()
            )

            val looseScanner = FileScanner()
            looseScanner.scanDirectory(looseTestModDir, looseTestModDir, "LooseTestMod")
            val looseModFiles = convertScannerResultsToModFiles(
                modId = looseTestMod.id,
                sourceModName = looseTestMod.name,
                fileMap = looseScanner.getFileMap()
            )

            logModelResults(skyUiMod, skyUiModFiles)
            logModelResults(looseTestMod, looseModFiles)

            val sampleRecord = FileRecord(
                normalizedPath = "textures/ui/icon.dds",
                winningModId = looseTestMod.id,
                winningModName = looseTestMod.name,
                sourceFilePath = File(
                    looseTestModDir,
                    "Data/Textures/UI/icon.dds"
                ).absolutePath,
                hash = looseModFiles.firstOrNull {
                    it.normalizedPath == "textures/ui/icon.dds"
                }?.hash ?: "MISSING_HASH"
            )

            appendLog("SAMPLE FILE RECORD: $sampleRecord")
            appendLog("RESULT: PASS")
        } catch (e: Exception) {
            appendError("Model lesson test failed: ${e.message}", e)
            appendLog("RESULT: FAIL")
        }

        appendLog("----- Model Lesson Test End -----")
    }

    private fun createOverwriteTestMod(modsDir: File): File {
        val modDir = File(modsDir, "OverwriteTestMod")

        if (modDir.exists()) {
            modDir.deleteRecursively()
        }

        File(modDir, "Data/Textures/UI").mkdirs()
        File(modDir, "Data/Scripts").mkdirs()

        File(modDir, "Data/Textures/UI/icon.dds")
            .writeText("overwrite texture data")

        File(modDir, "Data/Scripts/AnotherScript.psc")
            .writeText("another script source")

        return modDir
    }

    private fun logConflictResults(results: List<FileRecord>, label: String) {
        appendLog("----- Conflict Results for $label -----")
        appendLog("Winning record count: ${results.size}")

        for (record in results.sortedBy { it.normalizedPath }) {
            appendLog("WINNER: $record")
        }

        appendLog("----- End Conflict Results for $label -----")
    }

    private fun runConflictResolverLessonTest() {
        appendLog("----- ConflictResolver Lesson Test Start -----")

        val baseDir = filesDir
        val modsDir = File(baseDir, "mods")
        modsDir.mkdirs()

        val skyUiDir = File(modsDir, "SkyUI")
        val looseTestModDir = File(modsDir, "LooseTestMod")
        val overwriteTestModDir = createOverwriteTestMod(modsDir)

        if (!skyUiDir.exists() || !looseTestModDir.exists()) {
            appendError("Required mod folders are missing.")
            appendLog("Run earlier lessons first.")
            appendLog("----- ConflictResolver Lesson Test End -----")
            return
        }

        try {
            val skyUiMod = Mod(
                id = "SkyUI",
                name = "SkyUI",
                installPath = skyUiDir.absolutePath,
                enabled = true,
                priority = 10,
                modType = detectModType(skyUiDir)
            )

            val looseTestMod = Mod(
                id = "LooseTestMod",
                name = "LooseTestMod",
                installPath = looseTestModDir.absolutePath,
                enabled = true,
                priority = 20,
                modType = detectModType(looseTestModDir)
            )

            val overwriteTestMod = Mod(
                id = "OverwriteTestMod",
                name = "OverwriteTestMod",
                installPath = overwriteTestModDir.absolutePath,
                enabled = true,
                priority = 30,
                modType = detectModType(overwriteTestModDir)
            )

            val mods = listOf(skyUiMod, looseTestMod, overwriteTestMod)

            val allModFiles = mutableListOf<ModFile>()

            val skyUiScanner = FileScanner()
            skyUiScanner.scanDirectory(skyUiDir, skyUiDir, skyUiMod.name)
            allModFiles.addAll(
                convertScannerResultsToModFiles(
                    modId = skyUiMod.id,
                    sourceModName = skyUiMod.name,
                    fileMap = skyUiScanner.getFileMap()
                )
            )

            val looseScanner = FileScanner()
            looseScanner.scanDirectory(looseTestModDir, looseTestModDir, looseTestMod.name)
            allModFiles.addAll(
                convertScannerResultsToModFiles(
                    modId = looseTestMod.id,
                    sourceModName = looseTestMod.name,
                    fileMap = looseScanner.getFileMap()
                )
            )

            val overwriteScanner = FileScanner()
            overwriteScanner.scanDirectory(overwriteTestModDir, overwriteTestModDir, overwriteTestMod.name)
            allModFiles.addAll(
                convertScannerResultsToModFiles(
                    modId = overwriteTestMod.id,
                    sourceModName = overwriteTestMod.name,
                    fileMap = overwriteScanner.getFileMap()
                )
            )

            val resolver = ConflictResolver()
            val results = resolver.resolve(mods, allModFiles)

            logConflictResults(results, "All Mods")

            val iconWinner = results.firstOrNull {
                it.normalizedPath == "textures/ui/icon.dds"
            }

            if (iconWinner == null) {
                appendLog("Expected winner for textures/ui/icon.dds was not found")
                appendLog("RESULT: FAIL")
            } else if (iconWinner.winningModId != "OverwriteTestMod") {
                appendLog("Wrong winner for textures/ui/icon.dds: ${iconWinner.winningModId}")
                appendLog("RESULT: FAIL")
            } else {
                appendLog("Correct winner for textures/ui/icon.dds: ${iconWinner.winningModId}")
                appendLog("RESULT: PASS")
            }

        } catch (e: Exception) {
            appendError("ConflictResolver test failed: ${e.message}", e)
            appendLog("RESULT: FAIL")
        }

        appendLog("----- ConflictResolver Lesson Test End -----")
    }

    private fun logStagingContents(stagingDir: File, label: String) {
        appendLog("----- Staging Contents for $label -----")
        appendLog("Staging exists: ${stagingDir.exists()}")

        val items = if (stagingDir.exists()) stagingDir.walkTopDown().toList() else emptyList()
        appendLog("Staging item count: ${items.size}")

        for (item in items.take(30)) {
            appendLog("STAGING ITEM: ${item.absolutePath}")
        }

        appendLog("----- End Staging Contents for $label -----")
    }

    private fun runStagingManagerLessonTest() {
        appendLog("----- StagingManager Lesson Test Start -----")

        val baseDir = filesDir
        val modsDir = File(baseDir, "mods")
        val stagingDir = File(baseDir, "staging")

        modsDir.mkdirs()
        stagingDir.mkdirs()

        val skyUiDir = File(modsDir, "SkyUI")
        val looseTestModDir = File(modsDir, "LooseTestMod")
        val overwriteTestModDir = createOverwriteTestMod(modsDir)

        if (!skyUiDir.exists() || !looseTestModDir.exists()) {
            appendError("Required mod folders are missing.")
            appendLog("Run earlier lessons first.")
            appendLog("----- StagingManager Lesson Test End -----")
            return
        }

        try {
            val skyUiMod = Mod(
                id = "SkyUI",
                name = "SkyUI",
                installPath = skyUiDir.absolutePath,
                enabled = true,
                priority = 10,
                modType = detectModType(skyUiDir)
            )

            val looseTestMod = Mod(
                id = "LooseTestMod",
                name = "LooseTestMod",
                installPath = looseTestModDir.absolutePath,
                enabled = true,
                priority = 20,
                modType = detectModType(looseTestModDir)
            )

            val overwriteTestMod = Mod(
                id = "OverwriteTestMod",
                name = "OverwriteTestMod",
                installPath = overwriteTestModDir.absolutePath,
                enabled = true,
                priority = 30,
                modType = detectModType(overwriteTestModDir)
            )

            val mods = listOf(skyUiMod, looseTestMod, overwriteTestMod)
            val allModFiles = mutableListOf<ModFile>()

            val skyUiScanner = FileScanner()
            skyUiScanner.scanDirectory(skyUiDir, skyUiDir, skyUiMod.name)
            allModFiles.addAll(
                convertScannerResultsToModFiles(
                    modId = skyUiMod.id,
                    sourceModName = skyUiMod.name,
                    fileMap = skyUiScanner.getFileMap()
                )
            )

            val looseScanner = FileScanner()
            looseScanner.scanDirectory(looseTestModDir, looseTestModDir, looseTestMod.name)
            allModFiles.addAll(
                convertScannerResultsToModFiles(
                    modId = looseTestMod.id,
                    sourceModName = looseTestMod.name,
                    fileMap = looseScanner.getFileMap()
                )
            )

            val overwriteScanner = FileScanner()
            overwriteScanner.scanDirectory(overwriteTestModDir, overwriteTestModDir, overwriteTestMod.name)
            allModFiles.addAll(
                convertScannerResultsToModFiles(
                    modId = overwriteTestMod.id,
                    sourceModName = overwriteTestMod.name,
                    fileMap = overwriteScanner.getFileMap()
                )
            )

            val resolver = ConflictResolver()
            val winningRecords = resolver.resolve(mods, allModFiles)

            val stagingManager = StagingManager(stagingDir)
            stagingManager.rebuildStaging(winningRecords)

            logStagingContents(stagingDir, "Merged Output")

            val stagedIcon = File(stagingDir, "textures/ui/icon.dds")
            if (!stagedIcon.exists()) {
                appendLog("Missing staged file: ${stagedIcon.absolutePath}")
                appendLog("RESULT: FAIL")
                appendLog("----- StagingManager Lesson Test End -----")
                return
            }

            val stagedIconContent = stagedIcon.readText()
            appendLog("Staged icon content: $stagedIconContent")

            if (stagedIconContent != "overwrite texture data") {
                appendLog("Wrong staged content for textures/ui/icon.dds")
                appendLog("RESULT: FAIL")
            } else {
                appendLog("Correct staged content for textures/ui/icon.dds")
                appendLog("RESULT: PASS")
            }

        } catch (e: Exception) {
            appendError("StagingManager test failed: ${e.message}", e)
            appendLog("RESULT: FAIL")
        }

        appendLog("----- StagingManager Lesson Test End -----")
    }

    private fun logDiffResults(changes: List<FileChange>, label: String) {
        appendLog("----- Diff Results for $label -----")
        appendLog("Change count: ${changes.size}")

        for (change in changes) {
            when (change) {
                is FileChange.Add -> appendLog("ADD: ${change.record.normalizedPath}")
                is FileChange.Remove -> appendLog("REMOVE: ${change.normalizedPath}")
                is FileChange.Update -> appendLog(
                    "UPDATE: ${change.newRecord.normalizedPath} " +
                        "(old=${change.oldRecord.winningModId}, new=${change.newRecord.winningModId})"
                )
            }
        }

        appendLog("----- End Diff Results for $label -----")
    }

    private fun createUpdatedOverwriteTestMod(modsDir: File): File {
        val modDir = File(modsDir, "OverwriteTestMod")

        if (modDir.exists()) {
            modDir.deleteRecursively()
        }

        File(modDir, "Data/Textures/UI").mkdirs()
        File(modDir, "Data/Scripts").mkdirs()

        File(modDir, "Data/Textures/UI/icon.dds")
            .writeText("updated overwrite texture data")

        File(modDir, "Data/Scripts/AnotherScript.psc")
            .writeText("another script source")

        File(modDir, "Data/Scripts/TestScript.psc")
            .writeText("updated overwritten script source")

        return modDir
    }

    private fun runDiffEngineLessonTest() {
        appendLog("----- DiffEngine Lesson Test Start -----")

        val baseDir = filesDir
        val modsDir = File(baseDir, "mods")
        val stagingDir = File(baseDir, "staging")

        modsDir.mkdirs()
        stagingDir.mkdirs()

        val skyUiDir = File(modsDir, "SkyUI")
        val looseTestModDir = File(modsDir, "LooseTestMod")
        val overwriteTestModDir = createOverwriteTestMod(modsDir)

        if (!skyUiDir.exists() || !looseTestModDir.exists()) {
            appendError("Required mod folders are missing.")
            appendLog("Run earlier lessons first.")
            appendLog("----- DiffEngine Lesson Test End -----")
            return
        }

        try {
            val initialMods = listOf(
                Mod(
                    id = "SkyUI",
                    name = "SkyUI",
                    installPath = skyUiDir.absolutePath,
                    enabled = true,
                    priority = 10,
                    modType = detectModType(skyUiDir)
                ),
                Mod(
                    id = "LooseTestMod",
                    name = "LooseTestMod",
                    installPath = looseTestModDir.absolutePath,
                    enabled = true,
                    priority = 20,
                    modType = detectModType(looseTestModDir)
                ),
                Mod(
                    id = "OverwriteTestMod",
                    name = "OverwriteTestMod",
                    installPath = overwriteTestModDir.absolutePath,
                    enabled = true,
                    priority = 30,
                    modType = detectModType(overwriteTestModDir)
                )
            )

            val initialFiles = collectAllModFiles(initialMods)
            val resolver = ConflictResolver()
            val oldRecords = resolver.resolve(initialMods, initialFiles)

            val stagingManager = StagingManager(stagingDir)
            stagingManager.rebuildStaging(oldRecords)

            val updatedOverwriteDir = createUpdatedOverwriteTestMod(modsDir)

            val updatedMods = listOf(
                Mod(
                    id = "SkyUI",
                    name = "SkyUI",
                    installPath = skyUiDir.absolutePath,
                    enabled = true,
                    priority = 10,
                    modType = detectModType(skyUiDir)
                ),
                Mod(
                    id = "LooseTestMod",
                    name = "LooseTestMod",
                    installPath = looseTestModDir.absolutePath,
                    enabled = true,
                    priority = 20,
                    modType = detectModType(looseTestModDir)
                ),
                Mod(
                    id = "OverwriteTestMod",
                    name = "OverwriteTestMod",
                    installPath = updatedOverwriteDir.absolutePath,
                    enabled = true,
                    priority = 30,
                    modType = detectModType(updatedOverwriteDir)
                )
            )

            val newFiles = collectAllModFiles(updatedMods)
            val newRecords = resolver.resolve(updatedMods, newFiles)

            val diffEngine = DiffEngine()
            val changes = diffEngine.diff(oldRecords, newRecords)

            logDiffResults(changes, "Old vs New Winning Records")

            stagingManager.applyChanges(changes)

            val stagedIcon = File(stagingDir, "textures/ui/icon.dds")
            if (!stagedIcon.exists()) {
                appendLog("Missing staged icon after incremental update")
                appendLog("RESULT: FAIL")
                appendLog("----- DiffEngine Lesson Test End -----")
                return
            }

            val stagedIconContent = stagedIcon.readText()
            appendLog("Staged icon content after diff apply: $stagedIconContent")

            if (stagedIconContent != "updated overwrite texture data") {
                appendLog("Incremental update failed for textures/ui/icon.dds")
                appendLog("RESULT: FAIL")
            } else {
                appendLog("Incremental update succeeded for textures/ui/icon.dds")
                appendLog("RESULT: PASS")
            }

        } catch (e: Exception) {
            appendError("DiffEngine test failed: ${e.message}", e)
            appendLog("RESULT: FAIL")
        }

        appendLog("----- DiffEngine Lesson Test End -----")
    }

    private fun collectAllModFiles(mods: List<Mod>): List<ModFile> {
        val allModFiles = mutableListOf<ModFile>()

        for (mod in mods) {
            val modDir = File(mod.installPath)
            val scanner = FileScanner()
            scanner.scanDirectory(modDir, modDir, mod.name)

            allModFiles.addAll(
                convertScannerResultsToModFiles(
                    modId = mod.id,
                    sourceModName = mod.name,
                    fileMap = scanner.getFileMap()
                )
            )
        }

        return allModFiles
    }
    private fun buildInstalledModsFromFolders(modsDir: File): List<Mod> {
        val modDirs = modsDir.listFiles()
            ?.filter { it.isDirectory }
            ?.sortedBy { it.name.lowercase() }
            ?: emptyList()

        val mods = mutableListOf<Mod>()
        var priority = 10

        for (modDir in modDirs) {
            mods.add(
                Mod(
                    id = modDir.name,
                    name = modDir.name,
                    installPath = modDir.absolutePath,
                    enabled = true,
                    priority = priority,
                    modType = detectModType(modDir)
                )
            )
            priority += 10
        }

        return mods
    }
    private fun runModStateLessonTest() {
        appendLog("----- Mod State Lesson Test Start -----")

        val baseDir = getExternalFilesDir(null)
        if (baseDir == null) {
            appendError("External files directory is null")
            appendLog("RESULT: FAIL")
            appendLog("----- Mod State Lesson Test End -----")
            return
        }

        val modsDir = File(baseDir, "mods")
        val stateDir = File(baseDir, "state")
        val stateFile = File(stateDir, "installed_mods.json")

        modsDir.mkdirs()
        stateDir.mkdirs()

        createLooseTestMod(modsDir)
        createOverwriteTestMod(modsDir)

        val modsToSave = buildInstalledModsFromFolders(modsDir)

        if (modsToSave.isEmpty()) {
            appendError("No mod folders found to save.")
            appendLog("RESULT: FAIL")
            appendLog("----- Mod State Lesson Test End -----")
            return
        }

        try {
            val repository = ModStateRepository(stateFile)

            repository.saveMods(modsToSave)
            appendLog("State file absolute path: ${stateFile.absolutePath}")
            appendLog("State file exists: ${stateFile.exists()}")
            appendLog("State file contents:")
            appendLog(stateFile.readText())

            val loadedMods = repository.loadMods()
            appendLog("Loaded mod count: ${loadedMods.size}")

            for (mod in loadedMods) {
                appendLog("LOADED MOD: $mod")
            }

            val sameCount = modsToSave.size == loadedMods.size
            val sameNames = modsToSave.map { it.name } == loadedMods.map { it.name }
            val sameTypes = modsToSave.map { it.modType } == loadedMods.map { it.modType }

            if (sameCount && sameNames && sameTypes) {
                appendLog("Mod state save/load matched expected results.")
                appendLog("RESULT: PASS")
            } else {
                appendLog("Mismatch detected between saved and loaded mod state.")
                appendLog("RESULT: FAIL")
            }

        } catch (e: Exception) {
            appendError("Mod state test failed: ${e.message}", e)
            appendLog("RESULT: FAIL")
        }

        appendLog("----- Mod State Lesson Test End -----")
    }
}

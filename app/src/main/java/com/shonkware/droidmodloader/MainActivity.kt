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
import com.shonkware.droidmodloader.engine.ModEngine
import com.shonkware.droidmodloader.engine.model.DeployScope
import com.shonkware.droidmodloader.engine.rules.DeployFileClassifier
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts
import android.widget.LinearLayout
import android.app.AlertDialog


class MainActivity : ComponentActivity() {

    companion object {
        private const val TAG = "DroidModLoader"
    }

    private lateinit var logTextView: TextView

    private val importZipLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri == null) {
            appendLog("No file selected.")
            return@registerForActivityResult
        }

        runInBackground {
            handleImportedZip(uri)
        }
    }
    private lateinit var installedModsContainer: LinearLayout
    private lateinit var summaryTextView: TextView
    private lateinit var lessonToolsContainer: LinearLayout
    private var lessonToolsVisible = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        logTextView = findViewById(R.id.logTextView)
        installedModsContainer = findViewById(R.id.installedModsContainer)
        summaryTextView = findViewById(R.id.summaryTextView)
        lessonToolsContainer = findViewById(R.id.lessonToolsContainer)

        val buttonRefreshDashboard: Button = findViewById(R.id.buttonRefreshDashboard)
        val buttonToggleLessonTools: Button = findViewById(R.id.buttonToggleLessonTools)
        val buttonRefreshModsPanel: Button = findViewById(R.id.buttonRefreshModsPanel)
        val buttonSmokeTest: Button = findViewById(R.id.buttonSmokeTest)
        val buttonPathUtilsTest: Button = findViewById(R.id.buttonPathUtilsTest)
        val buttonExtractorTest: Button = findViewById(R.id.buttonExtractorTest)
        val buttonScannerTest: Button = findViewById(R.id.buttonScannerTest)
        val buttonConflictTest: Button = findViewById(R.id.buttonConflictTest)
        val buttonStagingTest: Button = findViewById(R.id.buttonStagingTest)
        val buttonDiffTest: Button = findViewById(R.id.buttonDiffTest)
        val buttonClearLog: Button = findViewById(R.id.buttonClearLog)
        val buttonStateTest: Button = findViewById(R.id.buttonStateTest)
        val buttonEngineTest: Button = findViewById(R.id.buttonEngineTest)
        val buttonInstallArchiveWorkflow: Button = findViewById(R.id.buttonInstallArchiveWorkflow)
        val buttonInstallLooseWorkflow: Button = findViewById(R.id.buttonInstallLooseWorkflow)
        val buttonListInstalledMods: Button = findViewById(R.id.buttonListInstalledMods)
        val buttonSaveInstalledMods: Button = findViewById(R.id.buttonSaveInstalledMods)
        val buttonLoadSavedMods: Button = findViewById(R.id.buttonLoadSavedMods)
        val buttonRebuildInstalledStaging: Button = findViewById(R.id.buttonRebuildInstalledStaging)
        val buttonImportZip: Button = findViewById(R.id.buttonImportZip)
        val buttonDeleteModTest: Button = findViewById(R.id.buttonDeleteModTest)
        val buttonResetAppData: Button = findViewById(R.id.buttonResetAppData)
        val buttonInstalledRecordTest: Button = findViewById(R.id.buttonInstalledRecordTest)
        val buttonDeployScopeTest: Button = findViewById(R.id.buttonDeployScopeTest)
        val buttonDeployCurrentState: Button = findViewById(R.id.buttonDeployCurrentState)
        val buttonDeploymentManifestTest: Button = findViewById(R.id.buttonDeploymentManifestTest)

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

        buttonEngineTest.setOnClickListener {
            runInBackground { runModEngineLessonTest() }
        }

        buttonInstallArchiveWorkflow.setOnClickListener {
            runInBackground { runInstallArchiveWorkflow() }
        }

        buttonInstallLooseWorkflow.setOnClickListener {
            runInBackground { runInstallLooseWorkflow() }
        }

        buttonListInstalledMods.setOnClickListener {
            runInBackground { runListInstalledModsWorkflow() }
        }

        buttonSaveInstalledMods.setOnClickListener {
            runInBackground { runSaveInstalledModsWorkflow() }
        }

        buttonLoadSavedMods.setOnClickListener {
            runInBackground { runLoadSavedModsWorkflow() }
        }

        buttonRebuildInstalledStaging.setOnClickListener {
            runInBackground { runRebuildInstalledStagingWorkflow() }
        }

        buttonImportZip.setOnClickListener {
            appendLog("Opening document picker...")
            importZipLauncher.launch(arrayOf("*/*"))
        }

        buttonRefreshModsPanel.setOnClickListener {
            runInBackground { refreshInstalledModsPanel() }
        }

        buttonDeleteModTest.setOnClickListener {
            runInBackground { runDeleteModLessonTest() }
        }

        buttonResetAppData.setOnClickListener {
            runInBackground { runResetAppDataWorkflow() }
        }

        buttonRefreshDashboard.setOnClickListener {
            runInBackground { refreshDashboard() }
        }

        buttonToggleLessonTools.setOnClickListener {
            lessonToolsVisible = !lessonToolsVisible
            lessonToolsContainer.visibility =
                if (lessonToolsVisible) android.view.View.VISIBLE else android.view.View.GONE

            buttonToggleLessonTools.text =
                if (lessonToolsVisible) "Hide Lesson / Test Tools" else "Show Lesson / Test Tools"
        }

        buttonInstalledRecordTest.setOnClickListener {
            runInBackground { runInstalledRecordLessonTest() }
        }

        buttonDeployScopeTest.setOnClickListener {
            runInBackground { runDeployScopeLessonTest() }
        }

        buttonDeployCurrentState.setOnClickListener {
            runInBackground { runDeployCurrentStateWorkflow() }
        }

        buttonDeploymentManifestTest.setOnClickListener {
            runInBackground { runDeploymentManifestLessonTest() }
        }

        appendLog("UI ready. Use the workflow buttons or manage installed mods below.")
        runInBackground { refreshDashboard() }
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
        val baseDir = filesDir

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

        try {
            val archive = createTestArchiveZip(baseDir)

            appendLog("Generated archive at: ${archive.absolutePath}")
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

        val externalBaseDir = getExternalFilesDir(null)
        if (externalBaseDir == null) {
            appendError("External files directory is null")
            appendLog("RESULT: FAIL")
            appendLog("----- Mod State Lesson Test End -----")
            return
        }

        val modsDir = File(filesDir, "mods")
        val stateDir = File(externalBaseDir, "state")
        val stateFile = File(stateDir, "installed_mods.json")

        modsDir.mkdirs()
        stateDir.mkdirs()

        createArchiveStyleTestMod(modsDir, "ArchiveStateTestMod")
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

            appendLog("Saved mod count: ${modsToSave.size}")
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

    private fun createTestArchiveZip(baseDir: File): File {
        val zipFile = File(baseDir, "SkyUI.zip")

        if (zipFile.exists()) {
            zipFile.delete()
        }

        ZipOutputStream(zipFile.outputStream()).use { zos ->
            addZipEntry(zos, "SkyUI/")
            addZipEntry(zos, "SkyUI/fomod/")
            addZipEntry(zos, "SkyUI/fomod/info.xml", "<fomod><name>SkyUI Test</name></fomod>")
            addZipEntry(zos, "SkyUI/fomod/script.cs", "// fake fomod script")
            addZipEntry(zos, "SkyUI/fomod/screenshot.jpg", "fake screenshot bytes")
            addZipEntry(zos, "SkyUI/SkyUI.bsa", "fake bsa data")
            addZipEntry(zos, "SkyUI/SkyUI.esp", "fake esp data")
        }

        return zipFile
    }
    private fun addZipEntry(zos: ZipOutputStream, entryName: String, content: String? = null) {
        val entry = ZipEntry(entryName)
        zos.putNextEntry(entry)

        if (content != null) {
            zos.write(content.toByteArray())
        }

        zos.closeEntry()
    }

    private fun createArchiveStyleTestMod(modsDir: File, modName: String = "ArchiveStateTestMod"): File {
        val modDir = File(modsDir, modName)

        if (modDir.exists()) {
            modDir.deleteRecursively()
        }

        File(modDir, "fomod").mkdirs()

        File(modDir, "fomod/info.xml")
            .writeText("<fomod><name>$modName</name></fomod>")

        File(modDir, "fomod/script.cs")
            .writeText("// fake fomod script")

        File(modDir, "fomod/screenshot.jpg")
            .writeText("fake screenshot bytes")

        File(modDir, "$modName.bsa")
            .writeText("fake bsa data")

        File(modDir, "$modName.esp")
            .writeText("fake esp data")

        return modDir
    }

    private fun runModEngineLessonTest() {
        appendLog("----- ModEngine Lesson Test Start -----")

        val externalBaseDir = getExternalFilesDir(null)
        if (externalBaseDir == null) {
            appendError("External files directory is null")
            appendLog("RESULT: FAIL")
            appendLog("----- ModEngine Lesson Test End -----")
            return
        }

        val internalBaseDir = filesDir

        val testRoot = File(internalBaseDir, "lesson_tests/mod_engine")
        val externalTestRoot = File(externalBaseDir, "lesson_tests/mod_engine")

        val tempDir = File(testRoot, "temp")
        val modsDir = File(testRoot, "mods")
        val stagingDir = File(testRoot, "staging")
        val archiveDir = File(testRoot, "archives")

        val stateDir = File(externalTestRoot, "state")
        val stateFile = File(stateDir, "installed_mods.json")
        val deploymentManifestFile = File(stateDir, "deployment_manifest.json")

        val deployRootDir = File(testRoot, "deploy_root")

        fun ensureDir(dir: File) {
            if (!dir.exists() && !dir.mkdirs()) {
                throw IllegalStateException("Could not create directory: ${dir.absolutePath}")
            }

            if (!dir.isDirectory) {
                throw IllegalStateException("Path exists but is not a directory: ${dir.absolutePath}")
            }
        }

        try {
            appendLog("Cleaning previous ModEngine lesson test files...")

            if (testRoot.exists() && !testRoot.deleteRecursively()) {
                throw IllegalStateException("Could not delete old test root: ${testRoot.absolutePath}")
            }

            if (externalTestRoot.exists() && !externalTestRoot.deleteRecursively()) {
                throw IllegalStateException("Could not delete old external test root: ${externalTestRoot.absolutePath}")
            }

            ensureDir(tempDir)
            ensureDir(modsDir)
            ensureDir(stagingDir)
            ensureDir(archiveDir)
            ensureDir(stateDir)

            appendLog("Test root: ${testRoot.absolutePath}")
            appendLog("External test root: ${externalTestRoot.absolutePath}")
            appendLog("Temp dir: ${tempDir.absolutePath}")
            appendLog("Mods dir: ${modsDir.absolutePath}")
            appendLog("Staging dir: ${stagingDir.absolutePath}")
            appendLog("State file: ${stateFile.absolutePath}")

            val engine = ModEngine(
                tempDir = tempDir,
                modsDir = modsDir,
                stagingDir = stagingDir,
                stateFile = stateFile,
                deploymentManifestFile = deploymentManifestFile,
                deployRootDir = deployRootDir
            )

            val archive = createTestArchiveZip(archiveDir)
            appendLog("Created test archive: ${archive.absolutePath}")
            appendLog("Archive exists: ${archive.exists()}")
            appendLog("Archive size: ${archive.length()} bytes")

            val archiveMod = engine.installArchive(archive, priority = 10)
            appendLog("Installed archive mod through ModEngine:")
            appendLog("$archiveMod")

            val looseDir = createLooseTestMod(modsDir)
            appendLog("Created loose test mod folder: ${looseDir.absolutePath}")

            val overwriteDir = createOverwriteTestMod(modsDir)
            appendLog("Created overwrite test mod folder: ${overwriteDir.absolutePath}")

            val looseMod = engine.buildModFromInstalledFolder(looseDir, priority = 20)
            appendLog("Built loose mod through ModEngine:")
            appendLog("$looseMod")

            val overwriteMod = engine.buildModFromInstalledFolder(overwriteDir, priority = 30)
            appendLog("Built overwrite mod through ModEngine:")
            appendLog("$overwriteMod")

            val mods = listOf(archiveMod, looseMod, overwriteMod)

            appendLog("Saving ${mods.size} mods through ModEngine...")
            engine.saveMods(mods)

            appendLog("State file exists after save: ${stateFile.exists()}")
            appendLog("State file size after save: ${stateFile.length()} bytes")

            val loadedMods = engine.loadMods()
            appendLog("Loaded mods through ModEngine: ${loadedMods.size}")

            for (mod in loadedMods) {
                appendLog("ENGINE LOADED MOD: $mod")
            }

            if (loadedMods.size != 3) {
                appendError("Expected 3 loaded mods, but got ${loadedMods.size}")
                appendLog("RESULT: FAIL")
                appendLog("----- ModEngine Lesson Test End -----")
                return
            }

            appendLog("Rebuilding staging through ModEngine...")

            if (stagingDir.exists() && !stagingDir.deleteRecursively()) {
                throw IllegalStateException("Could not clean staging before rebuild: ${stagingDir.absolutePath}")
            }

            ensureDir(stagingDir)

            val winningRecords = engine.rebuildStaging(loadedMods)
            appendLog("Winning record count from ModEngine: ${winningRecords.size}")

            val stagedIcon = File(stagingDir, "textures/ui/icon.dds")
            appendLog("Expected staged icon path: ${stagedIcon.absolutePath}")
            appendLog("Staged icon exists: ${stagedIcon.exists()}")

            if (!stagedIcon.exists()) {
                appendError("Staged icon not found after engine rebuild.")
                appendLog("RESULT: FAIL")
                appendLog("----- ModEngine Lesson Test End -----")
                return
            }

            val stagedIconContent = stagedIcon.readText()
            appendLog("Engine staged icon content: $stagedIconContent")

            if (stagedIconContent != "overwrite texture data") {
                appendError("Engine staging produced wrong winner for textures/ui/icon.dds")
                appendError("Expected: overwrite texture data")
                appendError("Actual: $stagedIconContent")
                appendLog("RESULT: FAIL")
            } else {
                appendLog("Engine staging produced correct winner for textures/ui/icon.dds")
                appendLog("State file path: ${stateFile.absolutePath}")
                appendLog("State file exists: ${stateFile.exists()}")

                if (stateFile.exists()) {
                    appendLog("State file contents:")
                    appendLog(stateFile.readText())
                }

                appendLog("RESULT: PASS")
            }

        } catch (e: Exception) {
            appendError("ModEngine test failed: ${e.message}", e)
            appendLog("RESULT: FAIL")
        }

        appendLog("----- ModEngine Lesson Test End -----")
    }

    private fun createModEngineForWorkflows(): ModEngine? {
        val externalBaseDir = getExternalFilesDir(null)
        if (externalBaseDir == null) {
            appendError("External files directory is null")
            return null
        }

        val internalBaseDir = filesDir

        val tempDir = File(internalBaseDir, "temp")
        val modsDir = File(internalBaseDir, "mods")
        val stagingDir = File(internalBaseDir, "staging")
        val stateDir = File(externalBaseDir, "state")
        val stateFile = File(stateDir, "installed_mods.json")

        val deployDir = File(externalBaseDir, "deploy_target/Skyrim/Data")
        val deploymentManifestFile = File(externalBaseDir, "state/deployment_manifest.json")

        tempDir.mkdirs()
        modsDir.mkdirs()
        stagingDir.mkdirs()
        stateDir.mkdirs()
        deployDir.mkdirs()

        return ModEngine(
            tempDir = tempDir,
            modsDir = modsDir,
            stagingDir = stagingDir,
            stateFile = stateFile,
            deploymentManifestFile = deploymentManifestFile,
            deployRootDir = deployDir
        )
    }

    private fun runInstallArchiveWorkflow() {
        appendLog("----- Install Archive Workflow Start -----")

        val engine = createModEngineForWorkflows() ?: return
        val internalBaseDir = filesDir

        try {
            val archive = createTestArchiveZip(internalBaseDir)
            val mod = engine.installArchiveWithRecord(
                archive = archive,
                priority = 10,
                sourceType = "generated_archive"
            )

            appendLog("Installed archive mod: $mod")
            appendLog("RESULT: PASS")
        } catch (e: Exception) {
            appendError("Install archive workflow failed: ${e.message}", e)
            appendLog("RESULT: FAIL")
        }

        refreshDashboard()

        appendLog("----- Install Archive Workflow End -----")
    }

    private fun runInstallLooseWorkflow() {
        appendLog("----- Install Loose Mods Workflow Start -----")

        val engine = createModEngineForWorkflows() ?: return
        val modsDir = File(filesDir, "mods")

        try {
            val looseDir = createLooseTestMod(modsDir)
            val overwriteDir = createOverwriteTestMod(modsDir)

            val looseMod = engine.registerExistingInstalledFolderWithRecord(
                modDir = looseDir,
                priority = 20,
                sourceType = "generated_loose"
            )

            val overwriteMod = engine.registerExistingInstalledFolderWithRecord(
                modDir = overwriteDir,
                priority = 30,
                sourceType = "generated_loose"
            )

            appendLog("Installed/generated loose mod: $looseMod")
            appendLog("Installed/generated loose mod: $overwriteMod")
            appendLog("RESULT: PASS")
        } catch (e: Exception) {
            appendError("Install loose workflow failed: ${e.message}", e)
            appendLog("RESULT: FAIL")
        }

        refreshDashboard()

        appendLog("----- Install Loose Mods Workflow End -----")
    }

    private fun runListInstalledModsWorkflow() {
        appendLog("----- List Installed Mods Workflow Start -----")

        val engine = createModEngineForWorkflows() ?: return

        try {
            val mods = engine.getInstalledModsFromFolders()

            appendLog("Installed mod count: ${mods.size}")
            for (mod in mods) {
                appendLog("INSTALLED MOD: $mod")
            }

            appendLog("RESULT: PASS")
        } catch (e: Exception) {
            appendError("List installed mods workflow failed: ${e.message}", e)
            appendLog("RESULT: FAIL")
        }

        appendLog("----- List Installed Mods Workflow End -----")
    }

    private fun runSaveInstalledModsWorkflow() {
        appendLog("----- Save Installed Mods Workflow Start -----")

        val engine = createModEngineForWorkflows() ?: return
        val externalBaseDir = getExternalFilesDir(null)
        if (externalBaseDir == null) {
            appendError("External files directory is null")
            appendLog("RESULT: FAIL")
            appendLog("----- Save Installed Mods Workflow End -----")
            return
        }

        val stateFile = File(File(externalBaseDir, "state"), "installed_mods.json")

        try {
            val savedMods = engine.saveInstalledModsFromFolders()

            appendLog("Saved installed mod count: ${savedMods.size}")
            appendLog("State file path: ${stateFile.absolutePath}")
            appendLog("State file exists: ${stateFile.exists()}")

            if (stateFile.exists()) {
                appendLog("State file contents:")
                appendLog(stateFile.readText())
            }

            appendLog("RESULT: PASS")
        } catch (e: Exception) {
            appendError("Save installed mods workflow failed: ${e.message}", e)
            appendLog("RESULT: FAIL")
        }

        refreshDashboard()

        appendLog("----- Save Installed Mods Workflow End -----")
    }

    private fun runLoadSavedModsWorkflow() {
        appendLog("----- Load Saved Mods Workflow Start -----")

        val engine = createModEngineForWorkflows() ?: return

        try {
            val loadedMods = engine.loadMods()

            appendLog("Loaded mod count: ${loadedMods.size}")
            for (mod in loadedMods) {
                appendLog("LOADED MOD: $mod")
            }

            appendLog("RESULT: PASS")
        } catch (e: Exception) {
            appendError("Load saved mods workflow failed: ${e.message}", e)
            appendLog("RESULT: FAIL")
        }

        refreshDashboard()

        appendLog("----- Load Saved Mods Workflow End -----")
    }

    private fun runRebuildInstalledStagingWorkflow() {
        appendLog("----- Rebuild Staging From Current State Workflow Start -----")

        val engine = createModEngineForWorkflows() ?: return
        val stagingDir = File(filesDir, "staging")

        try {
            val records = engine.rebuildStagingFromCurrentState()

            appendLog("Winning record count: ${records.size}")

            val stagedIcon = File(stagingDir, "textures/ui/icon.dds")
            appendLog("Staged icon exists: ${stagedIcon.exists()}")

            if (stagedIcon.exists()) {
                appendLog("Staged icon content: ${stagedIcon.readText()}")
            }

            appendLog("RESULT: PASS")
        } catch (e: Exception) {
            appendError("Rebuild staging workflow failed: ${e.message}", e)
            appendLog("RESULT: FAIL")
        }

        appendLog("----- Rebuild Staging From Current State Workflow End -----")
    }

    private fun copyUriToAppFile(uri: Uri, destinationFile: File) {
        contentResolver.openInputStream(uri).use { inputStream ->
            if (inputStream == null) {
                throw IllegalStateException("Could not open input stream for selected file.")
            }

            destinationFile.parentFile?.mkdirs()

            destinationFile.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
    }

    private fun handleImportedZip(uri: Uri) {
        appendLog("----- Import ZIP Workflow Start -----")

        val engine = createModEngineForWorkflows() ?: return
        val externalBaseDir = getExternalFilesDir(null)
        if (externalBaseDir == null) {
            appendError("External files directory is null")
            appendLog("RESULT: FAIL")
            appendLog("----- Import ZIP Workflow End -----")
            return
        }

        val importsDir = File(externalBaseDir, "imports")
        importsDir.mkdirs()

        val importedZip = File(importsDir, "imported_mod.zip")

        try {
            copyUriToAppFile(uri, importedZip)

            appendLog("Imported file copied to: ${importedZip.absolutePath}")
            appendLog("Imported file exists: ${importedZip.exists()}")
            appendLog("Imported file size: ${importedZip.length()} bytes")

            val existingMods = engine.getInstalledModsFromFolders()
            val nextPriority = if (existingMods.isEmpty()) 10 else (existingMods.maxOf { it.priority } + 10)

            val installedMod = engine.installArchiveWithRecord(
                archive = importedZip,
                priority = nextPriority,
                sourceType = "imported_zip"
            )

            val savedMods = engine.saveInstalledModsFromFolders()
            appendLog("Saved installed mod count after import: ${savedMods.size}")

            appendLog("RESULT: PASS")
        } catch (e: Exception) {
            appendError("Import ZIP workflow failed: ${e.message}", e)
            appendLog("RESULT: FAIL")
        }

        refreshDashboard()

        appendLog("----- Import ZIP Workflow End -----")
    }

    private fun normalizePriorities(mods: List<Mod>): List<Mod> {
        return mods.mapIndexed { index, mod ->
            mod.copy(priority = (index + 1) * 10)
        }
    }

    private fun toggleModEnabled(modId: String) {
        val engine = createModEngineForWorkflows() ?: return
        val mods = engine.getCurrentMods().sortedBy { it.priority }.toMutableList()

        val index = mods.indexOfFirst { it.id == modId }
        if (index == -1) {
            appendError("Could not find mod: $modId")
            return
        }

        mods[index] = mods[index].copy(enabled = !mods[index].enabled)
        engine.saveCurrentMods(normalizePriorities(mods))

        appendLog("Toggled enabled state for $modId")
        refreshDashboard()
    }

    private fun moveModUp(modId: String) {
        val engine = createModEngineForWorkflows() ?: return
        val mods = engine.getCurrentMods().sortedBy { it.priority }.toMutableList()

        val index = mods.indexOfFirst { it.id == modId }
        if (index <= 0) {
            appendLog("Cannot move up: $modId")
            return
        }

        val temp = mods[index - 1]
        mods[index - 1] = mods[index]
        mods[index] = temp

        engine.saveCurrentMods(normalizePriorities(mods))

        appendLog("Moved up: $modId")
        refreshDashboard()
    }

    private fun moveModDown(modId: String) {
        val engine = createModEngineForWorkflows() ?: return
        val mods = engine.getCurrentMods().sortedBy { it.priority }.toMutableList()

        val index = mods.indexOfFirst { it.id == modId }
        if (index == -1 || index >= mods.lastIndex) {
            appendLog("Cannot move down: $modId")
            return
        }

        val temp = mods[index + 1]
        mods[index + 1] = mods[index]
        mods[index] = temp

        engine.saveCurrentMods(normalizePriorities(mods))

        appendLog("Moved down: $modId")
        refreshDashboard()
    }

    private fun createModRow(mod: Mod): LinearLayout {

        val engine = createModEngineForWorkflows()
        val record = engine?.loadInstalledModRecord(mod)
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 0, 0, 24)
        }

        val installedSource = record?.sourceType ?: "unknown"
        val archiveName = record?.sourceArchiveName ?: "none"
        val installedAt = record?.installedAtEpochMillis ?: 0L

        val infoText = TextView(this).apply {
            text = buildString {
                appendLine("Priority ${mod.priority} | ${mod.name} | ${mod.modType} | ${if (mod.enabled) "ENABLED" else "DISABLED"}")
                appendLine("Source: $installedSource")
                append("Archive: $archiveName")
                if (installedAt > 0L) {
                    append(" | InstalledAt: $installedAt")
                }
            }
        }

        val buttonRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        val toggleButton = Button(this).apply {
            text = if (mod.enabled) "Disable" else "Enable"
            setOnClickListener {
                runInBackground { toggleModEnabled(mod.id) }
            }
        }

        val upButton = Button(this).apply {
            text = "Up"
            setOnClickListener {
                runInBackground { moveModUp(mod.id) }
            }
        }

        val downButton = Button(this).apply {
            text = "Down"
            setOnClickListener {
                runInBackground { moveModDown(mod.id) }
            }
        }

        val deleteButton = Button(this).apply {
            text = "Delete"
            setOnClickListener {
                showDeleteConfirmDialog(mod)
            }
        }

        buttonRow.addView(toggleButton)
        buttonRow.addView(upButton)
        buttonRow.addView(downButton)
        buttonRow.addView(deleteButton)

        row.addView(infoText)
        row.addView(buttonRow)

        return row
    }

    private fun refreshInstalledModsPanel() {
        val engine = createModEngineForWorkflows() ?: return
        val mods = engine.getCurrentMods().sortedBy { it.priority }

        runOnUiThread {
            installedModsContainer.removeAllViews()

            if (mods.isEmpty()) {
                val emptyText = TextView(this).apply {
                    text = "No installed mods found."
                }
                installedModsContainer.addView(emptyText)
                return@runOnUiThread
            }

            for (mod in mods) {
                installedModsContainer.addView(createModRow(mod))
            }
        }
    }

    private fun deleteInstalledMod(modId: String) {
        appendLog("----- Delete Installed Mod Workflow Start -----")
        appendLog("Requested delete for mod: $modId")

        val engine = createModEngineForWorkflows() ?: return

        try {
            val result = engine.uninstallModAndApplyDiff(modId)

            if (!result.removed) {
                appendError("Could not remove mod: $modId")
                appendLog("RESULT: FAIL")
                appendLog("----- Delete Installed Mod Workflow End -----")
                return
            }

            appendLog("Deleted mod: ${result.removedModId}")
            appendLog("Staging changes after delete:")
            appendLog("  Adds: ${result.addCount}")
            appendLog("  Removes: ${result.removeCount}")
            appendLog("  Updates: ${result.updateCount}")
            appendLog("RESULT: PASS")

            refreshDashboard()
        } catch (e: Exception) {
            appendError("Delete installed mod workflow failed: ${e.message}", e)
            appendLog("RESULT: FAIL")
        }

        appendLog("----- Delete Installed Mod Workflow End -----")
    }

    private fun runDeleteModLessonTest() {
        appendLog("----- Delete Mod Lesson Test Start -----")

        val engine = createModEngineForWorkflows() ?: return
        val stagingDir = File(filesDir, "staging")

        try {
            val modsBefore = engine.getCurrentMods().sortedBy { it.priority }
            appendLog("Current mod count before delete: ${modsBefore.size}")
            for (mod in modsBefore) {
                appendLog("BEFORE DELETE MOD: $mod")
            }

            val overwriteMod = modsBefore.firstOrNull { it.id == "OverwriteTestMod" }
            if (overwriteMod == null) {
                appendError("OverwriteTestMod not found. Install the test mods first.")
                appendLog("RESULT: FAIL")
                appendLog("----- Delete Mod Lesson Test End -----")
                return
            }

            val result = engine.uninstallModAndApplyDiff("OverwriteTestMod")
            if (!result.removed) {
                appendError("Failed to uninstall OverwriteTestMod")
                appendLog("RESULT: FAIL")
                appendLog("----- Delete Mod Lesson Test End -----")
                return
            }

            appendLog("Delete diff stats:")
            appendLog("  Adds: ${result.addCount}")
            appendLog("  Removes: ${result.removeCount}")
            appendLog("  Updates: ${result.updateCount}")

            val modsAfter = engine.getCurrentMods().sortedBy { it.priority }
            appendLog("Current mod count after delete: ${modsAfter.size}")
            for (mod in modsAfter) {
                appendLog("AFTER DELETE MOD: $mod")
            }

            val stagedIcon = File(stagingDir, "textures/ui/icon.dds")
            if (!stagedIcon.exists()) {
                appendError("Staged icon missing after delete")
                appendLog("RESULT: FAIL")
                appendLog("----- Delete Mod Lesson Test End -----")
                return
            }

            val stagedIconContent = stagedIcon.readText()
            appendLog("Staged icon content after delete: $stagedIconContent")

            if (stagedIconContent != "fake texture data") {
                appendError("Delete fallback failed. Expected LooseTestMod to win.")
                appendLog("RESULT: FAIL")
            } else {
                appendLog("Delete fallback succeeded. LooseTestMod now wins.")
                appendLog("RESULT: PASS")
            }

            refreshDashboard()
        } catch (e: Exception) {
            appendError("Delete mod lesson test failed: ${e.message}", e)
            appendLog("RESULT: FAIL")
        }

        appendLog("----- Delete Mod Lesson Test End -----")
    }

    private fun getImportsDir(): File? {
        val externalBaseDir = getExternalFilesDir(null) ?: return null
        return File(externalBaseDir, "imports")
    }

    private fun runResetAppDataWorkflow() {
        appendLog("----- Reset App Data Workflow Start -----")

        val engine = createModEngineForWorkflows() ?: return
        val importsDir = getImportsDir()

        if (importsDir == null) {
            appendError("Imports directory could not be created.")
            appendLog("RESULT: FAIL")
            appendLog("----- Reset App Data Workflow End -----")
            return
        }

        try {
            appendLog("Reset will remove mods, staging, temp, imports, and saved state.")

            val success = engine.resetAllAppData(importsDir)

            if (!success) {
                appendError("Engine reset failed.")
                appendLog("RESULT: FAIL")
                appendLog("----- Reset App Data Workflow End -----")
                return
            }

            appendLog("App data reset completed.")
            appendLog("Mods dir exists after reset: ${File(filesDir, "mods").exists()}")
            appendLog("Staging dir exists after reset: ${File(filesDir, "staging").exists()}")

            val externalBaseDir = getExternalFilesDir(null)
            if (externalBaseDir != null) {
                val stateFile = File(File(externalBaseDir, "state"), "installed_mods.json")
                appendLog("State file exists after reset: ${stateFile.exists()}")
            }

            refreshDashboard()
            appendLog("Installed mods panel refreshed.")
            appendLog("RESULT: PASS")
        } catch (e: Exception) {
            appendError("Reset app data workflow failed: ${e.message}", e)
            appendLog("RESULT: FAIL")
        }

        appendLog("----- Reset App Data Workflow End -----")
    }

    private fun refreshSummaryPanel() {
        val engine = createModEngineForWorkflows() ?: return

        val mods = engine.getCurrentMods().sortedBy { it.priority }
        val installedCount = mods.size
        val enabledCount = mods.count { it.enabled }
        val savedStateExists = engine.hasSavedState()

        val stateSourceText = when {
            savedStateExists -> "Saved state present"
            installedCount > 0 -> "Using folder-discovered state"
            else -> "No current mod state"
        }

        val highestPriorityMod = mods.lastOrNull()?.name ?: "None"

        val summaryText = buildString {
            appendLine("Installed mods: $installedCount")
            appendLine("Enabled mods: $enabledCount")
            appendLine("State source: $stateSourceText")
            appendLine("Highest priority mod: $highestPriorityMod")
        }

        runOnUiThread {
            summaryTextView.text = summaryText
        }
    }

    private fun refreshDashboard() {
        refreshSummaryPanel()
        refreshInstalledModsPanel()
        appendLog("Dashboard refreshed.")
    }

    private fun showDeleteConfirmDialog(mod: Mod) {
        runOnUiThread {
            AlertDialog.Builder(this)
                .setTitle("Delete Mod")
                .setMessage(
                    "Are you sure you want to delete '${mod.name}'?\n\n" +
                            "This will permanently remove the installed mod folder and update staging."
                )
                .setPositiveButton("Delete") { _, _ ->
                    runInBackground { deleteInstalledMod(mod.id) }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun runInstalledRecordLessonTest() {
        appendLog("----- Installed Record Lesson Test Start -----")

        val engine = createModEngineForWorkflows() ?: return

        try {
            runInstallArchiveWorkflow()
            runInstallLooseWorkflow()

            val mods = engine.getCurrentMods().sortedBy { it.priority }
            appendLog("Current mod count: ${mods.size}")

            for (mod in mods) {
                val record = engine.loadInstalledModRecord(mod)
                appendLog("MOD: $mod")
                appendLog("RECORD: $record")
            }

            val recordsLoaded = mods.count { engine.loadInstalledModRecord(it) != null }

            if (recordsLoaded == mods.size && mods.isNotEmpty()) {
                appendLog("All installed mods have metadata records.")
                appendLog("RESULT: PASS")
            } else {
                appendLog("Some installed mods are missing metadata records.")
                appendLog("RESULT: FAIL")
            }
        } catch (e: Exception) {
            appendError("Installed record lesson test failed: ${e.message}", e)
            appendLog("RESULT: FAIL")
        }

        refreshDashboard()
        appendLog("----- Installed Record Lesson Test End -----")
    }

    private fun createDeployScopeTestMod(modsDir: File): File {
        val modDir = File(modsDir, "DeployScopeTestMod")

        if (modDir.exists()) {
            modDir.deleteRecursively()
        }

        File(modDir, "Data/Textures/UI").mkdirs()
        File(modDir, "fomod").mkdirs()
        File(modDir, "Docs").mkdirs()

        File(modDir, "Data/Textures/UI/scope_test.dds")
            .writeText("deploy this texture")

        File(modDir, "fomod/info.xml")
            .writeText("<fomod><name>DeployScopeTestMod</name></fomod>")

        File(modDir, "readme.txt")
            .writeText("do not deploy this")

        File(modDir, "plugins.txt")
            .writeText("*DeployScopeTestMod.esp")

        File(modDir, "d3d11.dll")
            .writeText("root file placeholder")

        return modDir
    }
    private fun runDeployScopeLessonTest() {
        appendLog("----- Deploy Scope Lesson Test Start -----")

        val engine = createModEngineForWorkflows() ?: return
        val modsDir = File(filesDir, "mods")
        val stagingDir = File(filesDir, "staging")

        try {
            val testDir = createDeployScopeTestMod(modsDir)

            val mod = engine.registerExistingInstalledFolderWithRecord(
                modDir = testDir,
                priority = 10,
                sourceType = "generated_loose"
            )

            engine.saveCurrentMods(listOf(mod))

            val scannedFiles = engine.scanMod(mod)
            appendLog("Scanned file count: ${scannedFiles.size}")

            val classified = engine.classifyModFiles(scannedFiles)

            for (scope in DeployScope.values()) {
                val filesForScope = classified[scope].orEmpty()
                appendLog("$scope count: ${filesForScope.size}")
                for (file in filesForScope) {
                    appendLog("  $scope -> ${file.normalizedPath}")
                }
            }

            val winningRecords = engine.rebuildStagingFromCurrentState()
            appendLog("Winning record count: ${winningRecords.size}")

            val stagedDataFile = File(stagingDir, "textures/ui/scope_test.dds")
            val stagedFomodFile = File(stagingDir, "fomod/info.xml")
            val stagedReadme = File(stagingDir, "readme.txt")
            val stagedPluginsTxt = File(stagingDir, "plugins.txt")
            val stagedRootDll = File(stagingDir, "d3d11.dll")

            appendLog("stagedDataFile exists: ${stagedDataFile.exists()}")
            appendLog("stagedFomodFile exists: ${stagedFomodFile.exists()}")
            appendLog("stagedReadme exists: ${stagedReadme.exists()}")
            appendLog("stagedPluginsTxt exists: ${stagedPluginsTxt.exists()}")
            appendLog("stagedRootDll exists: ${stagedRootDll.exists()}")

            val passed =
                stagedDataFile.exists() &&
                        !stagedFomodFile.exists() &&
                        !stagedReadme.exists() &&
                        !stagedPluginsTxt.exists() &&
                        !stagedRootDll.exists()

            if (passed) {
                appendLog("Deploy scope filtering worked.")
                appendLog("RESULT: PASS")
            } else {
                appendLog("Deploy scope filtering failed.")
                appendLog("RESULT: FAIL")
            }

        } catch (e: Exception) {
            appendError("Deploy scope lesson test failed: ${e.message}", e)
            appendLog("RESULT: FAIL")
        }

        refreshDashboard()
        appendLog("----- Deploy Scope Lesson Test End -----")
    }

    private fun runDeployCurrentStateWorkflow() {
        appendLog("----- Deploy Current State Workflow Start -----")

        val engine = createModEngineForWorkflows() ?: return
        val externalBaseDir = getExternalFilesDir(null)
        if (externalBaseDir == null) {
            appendError("External files directory is null")
            appendLog("RESULT: FAIL")
            appendLog("----- Deploy Current State Workflow End -----")
            return
        }

        val deployRootDir = File(externalBaseDir, "deploy_target/Skyrim/Data")
        val manifestFile = File(externalBaseDir, "state/deployment_manifest.json")

        try {
            val result = engine.deployCurrentState()

            appendLog("Deploy target: ${deployRootDir.absolutePath}")
            appendLog("Deployment manifest: ${manifestFile.absolutePath}")
            appendLog("Manifest exists: ${manifestFile.exists()}")
            appendLog("Adds: ${result.addCount}")
            appendLog("Removes: ${result.removeCount}")
            appendLog("Updates: ${result.updateCount}")
            appendLog("Final deployed file count: ${result.finalRecordCount}")

            val deployedItems = if (deployRootDir.exists()) deployRootDir.walkTopDown().toList() else emptyList()
            appendLog("Deploy target item count: ${deployedItems.size}")

            for (item in deployedItems.take(25)) {
                appendLog("DEPLOY ITEM: ${item.absolutePath}")
            }

            appendLog("RESULT: PASS")
        } catch (e: Exception) {
            appendError("Deploy current state workflow failed: ${e.message}", e)
            appendLog("RESULT: FAIL")
        }

        appendLog("----- Deploy Current State Workflow End -----")
    }

    private fun runDeploymentManifestLessonTest() {
        appendLog("----- Deployment Manifest Lesson Test Start -----")

        val engine = createModEngineForWorkflows() ?: return

        try {
            runInstallArchiveWorkflow()
            runInstallLooseWorkflow()
            runSaveInstalledModsWorkflow()

            val firstDeploy = engine.deployCurrentState()
            appendLog("First deploy -> Adds: ${firstDeploy.addCount}, Removes: ${firstDeploy.removeCount}, Updates: ${firstDeploy.updateCount}")

            val secondDeploy = engine.deployCurrentState()
            appendLog("Second deploy -> Adds: ${secondDeploy.addCount}, Removes: ${secondDeploy.removeCount}, Updates: ${secondDeploy.updateCount}")

            val passed =
                secondDeploy.addCount == 0 &&
                        secondDeploy.removeCount == 0 &&
                        secondDeploy.updateCount == 0

            if (passed) {
                appendLog("Second deploy correctly detected no changes.")
                appendLog("RESULT: PASS")
            } else {
                appendLog("Second deploy unexpectedly changed files.")
                appendLog("RESULT: FAIL")
            }
        } catch (e: Exception) {
            appendError("Deployment manifest lesson test failed: ${e.message}", e)
            appendLog("RESULT: FAIL")
        }

        appendLog("----- Deployment Manifest Lesson Test End -----")
    }

}

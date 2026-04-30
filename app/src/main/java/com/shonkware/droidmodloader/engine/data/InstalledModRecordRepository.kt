package com.shonkware.droidmodloader.engine.data

import com.shonkware.droidmodloader.engine.model.InstalledModRecord
import org.json.JSONObject
import java.io.File

class InstalledModRecordRepository {

    companion object {
        private const val RECORD_FILE_NAME = ".dml_mod.json"
    }

    fun getRecordFile(modDir: File): File {
        return File(modDir, RECORD_FILE_NAME)
    }

    fun saveRecord(modDir: File, record: InstalledModRecord) {
        val json = JSONObject().apply {
            put("modId", record.modId)
            put("displayName", record.displayName)
            put("installPath", record.installPath)
            put("sourceType", record.sourceType)
            put("sourceArchiveName", record.sourceArchiveName)
            put("installedAtEpochMillis", record.installedAtEpochMillis)
        }

        getRecordFile(modDir).writeText(json.toString(2))
    }

    fun loadRecord(modDir: File): InstalledModRecord? {
        val file = getRecordFile(modDir)
        if (!file.exists()) return null

        val text = file.readText()
        if (text.isBlank()) return null

        val json = JSONObject(text)

        return InstalledModRecord(
            modId = json.optString("modId", modDir.name),
            displayName = json.optString("displayName", modDir.name),
            installPath = json.optString("installPath", modDir.absolutePath),
            sourceType = json.optString("sourceType", "unknown"),
            sourceArchiveName = if (json.isNull("sourceArchiveName")) null else json.optString("sourceArchiveName", null),
            installedAtEpochMillis = json.optLong("installedAtEpochMillis", 0L)
        )
    }
}
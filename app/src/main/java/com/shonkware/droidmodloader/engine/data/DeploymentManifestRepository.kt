package com.shonkware.droidmodloader.engine.data

import com.shonkware.droidmodloader.engine.model.DeploymentRecord
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class DeploymentManifestRepository(
    private val manifestFile: File
) {

    fun save(records: List<DeploymentRecord>) {
        val array = JSONArray()

        for (record in records) {
            val obj = JSONObject()
            obj.put("normalizedPath", record.normalizedPath)
            obj.put("winningModId", record.winningModId)
            obj.put("sourceFilePath", record.sourceFilePath)
            obj.put("hash", record.hash)
            obj.put("deployedRelativePath", record.deployedRelativePath)
            array.put(obj)
        }

        manifestFile.parentFile?.mkdirs()
        manifestFile.writeText(array.toString(2))
    }

    fun load(): List<DeploymentRecord> {
        if (!manifestFile.exists()) return emptyList()

        val text = manifestFile.readText()
        if (text.isBlank()) return emptyList()

        val array = JSONArray(text)
        val results = mutableListOf<DeploymentRecord>()

        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)

            results.add(
                DeploymentRecord(
                    normalizedPath = obj.optString("normalizedPath", ""),
                    winningModId = obj.optString("winningModId", ""),
                    sourceFilePath = obj.optString("sourceFilePath", ""),
                    hash = obj.optString("hash", ""),
                    deployedRelativePath = obj.optString("deployedRelativePath", "")
                )
            )
        }

        return results
    }

    fun clear() {
        if (manifestFile.exists()) {
            manifestFile.delete()
        }
    }
}